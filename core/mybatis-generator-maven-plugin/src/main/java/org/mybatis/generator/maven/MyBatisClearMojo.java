package org.mybatis.generator.maven;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.mybatis.generator.api.MyBatisGeneratorClean;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.util.ClassloaderUtility;
import org.mybatis.generator.internal.util.StringUtility;
import org.mybatis.generator.internal.util.messages.Messages;
import org.mybatis.generator.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Goal which generates MyBatis artifacts.
 */
@Mojo(name = "generate-clean", defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.TEST)
public class MyBatisClearMojo extends AbstractMojo {

    private ThreadLocal<ClassLoader> savedClassloader = new ThreadLocal<>();

    /**
     * Maven Project.
     *
     */
    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    /**
     * Output Directory.
     */
    @Parameter(property = "mybatis.generator.outputDirectory",
            defaultValue = "${project.build.directory}/generated-sources/mybatis-generator", required = true)
    private File outputDirectory;

    /**
     * Location of the configuration file.
     */
    @Parameter(property = "mybatis.generator.configurationFile",
            defaultValue = "${project.basedir}/src/main/resources/generatorConfig.xml", required = true)
    private File configurationFile;

    /**
     * Specifies whether the mojo writes progress messages to the log.
     */
    @Parameter(property = "mybatis.generator.verbose", defaultValue = "false")
    private boolean verbose;

    /**
     * Specifies whether the mojo overwrites existing Java files. Default is false.
     * <br>
     * Note that XML files are always merged.
     */
    @Parameter(property = "mybatis.generator.overwrite", defaultValue = "false")
    private boolean overwrite;

    /**
     * Location of a SQL script file to run before generating code. If null,
     * then no script will be run. If not null, then jdbcDriver, jdbcURL must be
     * supplied also, and jdbcUserId and jdbcPassword may be supplied.
     */
    @Parameter(property = "mybatis.generator.sqlScript")
    private String sqlScript;

    /**
     * JDBC Driver to use if a sql.script.file is specified.
     */
    @Parameter(property = "mybatis.generator.jdbcDriver")
    private String jdbcDriver;

    /**
     * JDBC URL to use if a sql.script.file is specified.
     */
    @Parameter(property = "mybatis.generator.jdbcURL")
    private String jdbcURL;

    /**
     * JDBC user ID to use if a sql.script.file is specified.
     */
    @Parameter(property = "mybatis.generator.jdbcUserId")
    private String jdbcUserId;

    /**
     * JDBC password to use if a sql.script.file is specified.
     */
    @Parameter(property = "mybatis.generator.jdbcPassword")
    private String jdbcPassword;

    /**
     * Comma-delimited list of table names to generate.
     */
    @Parameter(property = "mybatis.generator.tableNames")
    private String tableNames;

    @Parameter(property = "mybatis.generator.clean.dropTables")
    private boolean dropTables;
    @Parameter(property = "mybatis.generator.clean.tableNames")
    private String cleanTableNames;
    @Parameter(property = "mybatis.generator.clean.modelNames")
    private String cleanModelNames;

    /**
     * Comma-delimited list of contexts to generate.
     */
    @Parameter(property = "mybatis.generator.contexts")
    private String contexts;

    /**
     * Skip generator.
     */
    @Parameter(property = "mybatis.generator.skip", defaultValue = "false")
    private boolean skip;

    /**
     * If true, then dependencies in scope compile, provided, and system scopes will be
     * added to the classpath of the generator.  These dependencies will be searched for
     * JDBC drivers, root classes, root interfaces, generator plugins, etc.
     */
    @Parameter(property = "mybatis.generator.includeCompileDependencies", defaultValue = "false")
    private boolean includeCompileDependencies;

    /**
     * If true, then dependencies in all scopes will be
     * added to the classpath of the generator.  These dependencies will be searched for
     * JDBC drivers, root classes, root interfaces, generator plugins, etc.
     */
    @Parameter(property = "mybatis.generator.includeAllDependencies", defaultValue = "false")
    private boolean includeAllDependencies;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("MyBatis generator is skipped.");
            return;
        }

        saveClassLoader();

        LogFactory.setLogFactory(new MavenLogFactory(this));

        calculateClassPath();

        List<Resource> resources = project.getResources();
        List<String> resourceDirectories = new ArrayList<>();
        for (Resource resource: resources) {
            resourceDirectories.add(resource.getDirectory());
        }
        ClassLoader cl = ClassloaderUtility.getCustomClassloader(resourceDirectories);
        ObjectFactory.addExternalClassLoader(cl);

        if (configurationFile == null) {
            throw new MojoExecutionException(
                    Messages.getString("RuntimeError.0")); //$NON-NLS-1$
        }

        List<String> warnings = new ArrayList<>();

        if (!configurationFile.exists()) {
            throw new MojoExecutionException(Messages.getString(
                    "RuntimeError.1", configurationFile.toString())); //$NON-NLS-1$
        }

        runScriptIfNecessary();

        Set<String> fullyqualifiedTables = new HashSet<>();
        if (StringUtility.stringHasValue(tableNames)) {
            StringTokenizer st = new StringTokenizer(tableNames, ","); //$NON-NLS-1$
            while (st.hasMoreTokens()) {
                String s = st.nextToken().trim();
                if (!s.isEmpty()) {
                    fullyqualifiedTables.add(s);
                }
            }
        }

        Set<String> contextsToRun = new HashSet<>();
        if (StringUtility.stringHasValue(contexts)) {
            StringTokenizer st = new StringTokenizer(contexts, ","); //$NON-NLS-1$
            while (st.hasMoreTokens()) {
                String s = st.nextToken().trim();
                if (!s.isEmpty()) {
                    contextsToRun.add(s);
                }
            }
        }

        try {
            ConfigurationParser cp = new ConfigurationParser(project.getProperties(), warnings);
            Configuration config = cp.parseConfiguration(configurationFile);
            cp.customConfig(config);
            ShellCallback callback = new MavenCleanCallback(this, overwrite);

            MyBatisGeneratorClean myBatisGeneratorClean = new MyBatisGeneratorClean(config,callback, warnings);
            myBatisGeneratorClean.clean(null, contextsToRun, fullyqualifiedTables, true);

        } catch (XMLParserException e) {
            for (String error : e.getErrors()) {
                getLog().error(error);
            }
            throw new MojoExecutionException(e.getMessage());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        } catch (InvalidConfigurationException | SQLException | InterruptedException | ShellException e) {
            throw new RuntimeException(e);
        }

        for (String error : warnings) {
            getLog().warn(error);
        }

        if (project != null && outputDirectory != null && outputDirectory.exists()) {
            project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
            Resource resource = new Resource();
            resource.setDirectory(outputDirectory.getAbsolutePath());
            resource.addInclude("**/*.xml");
            project.addResource(resource);
        }
        restoreClassLoader();
    }

    private void calculateClassPath() throws MojoExecutionException {
        if (includeCompileDependencies || includeAllDependencies) {
            try {
                // add the project compile classpath to the plugin classpath,
                // so that the project dependency classes can be found
                // directly, without adding the classpath to configuration's classPathEntries
                // repeatedly.Examples are JDBC drivers, root classes, root interfaces, etc.
                Set<String> entries = new HashSet<>();
                if (includeCompileDependencies) {
                    entries.addAll(project.getCompileClasspathElements());
                }

                if (includeAllDependencies) {
                    entries.addAll(project.getTestClasspathElements());
                }

                // remove the output directories (target/classes and target/test-classes)
                // because this mojo runs in the generate-sources phase and
                // those directories have not been created yet (typically)
                entries.remove(project.getBuild().getOutputDirectory());
                entries.remove(project.getBuild().getTestOutputDirectory());

                ClassLoader contextClassLoader = ClassloaderUtility.getCustomClassloader(entries);
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            } catch (DependencyResolutionRequiredException e) {
                throw new MojoExecutionException("Dependency Resolution Required", e);
            }
        }
    }

    private void runScriptIfNecessary() throws MojoExecutionException {
        if (sqlScript == null) {
            return;
        }

        SqlScriptRunner scriptRunner = new SqlScriptRunner(sqlScript,
                jdbcDriver, jdbcURL, jdbcUserId, jdbcPassword);
        scriptRunner.setLog(getLog());
        scriptRunner.executeScript();
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    private void saveClassLoader() {
        savedClassloader.set(Thread.currentThread().getContextClassLoader());
    }

    private void restoreClassLoader() {
        Thread.currentThread().setContextClassLoader(savedClassloader.get());
    }
}
