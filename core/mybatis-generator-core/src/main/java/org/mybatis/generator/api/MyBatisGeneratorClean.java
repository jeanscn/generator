package org.mybatis.generator.api;

import org.mybatis.generator.config.*;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.util.StringUtility;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mybatis.generator.internal.util.ClassloaderUtility.getCustomClassloader;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class MyBatisGeneratorClean {

    private static final ProgressCallback NULL_PROGRESS_CALLBACK = new ProgressCallback() {
    };

    private final Configuration configuration;

    private final ShellCallback shellCallback;

    private final List<String> warnings;

    private final Set<String> projects = new HashSet<>();

    public MyBatisGeneratorClean(Configuration configuration, ShellCallback shellCallback,
                                 List<String> warnings) throws InvalidConfigurationException {
        super();
        if (configuration == null) {
            throw new IllegalArgumentException(getString("RuntimeError.2")); //$NON-NLS-1$
        } else {
            this.configuration = configuration;
        }

        if (shellCallback == null) {
            this.shellCallback = new DefaultShellCallback(false);
        } else {
            this.shellCallback = shellCallback;
        }

        if (warnings == null) {
            this.warnings = new ArrayList<>();
        } else {
            this.warnings = warnings;
        }

        this.configuration.validate();
    }

    public void clean(ProgressCallback callback,
                      Set<String> contextIds,
                      Set<String> fullyQualifiedTableNames,
                      boolean writeFiles) throws SQLException,
            IOException, InterruptedException, ShellException {

        if (callback == null) {
            callback = NULL_PROGRESS_CALLBACK;
        }

        // calculate the contexts to run
        List<Context> contextsToRun;
        if (contextIds == null || contextIds.isEmpty()) {
            contextsToRun = configuration.getContexts();
        } else {
            contextsToRun = new ArrayList<>();
            for (Context context : configuration.getContexts()) {
                if (contextIds.contains(context.getId())) {
                    contextsToRun.add(context);
                }
            }
        }

        // setup custom classloader if required
        if (!configuration.getClassPathEntries().isEmpty()) {
            ClassLoader classLoader = getCustomClassloader(configuration.getClassPathEntries());
            ObjectFactory.addExternalClassLoader(classLoader);
        }

        int totalTables = 0;
        for (Context context : contextsToRun) {
            List<TableConfiguration> configurationList = context.getTableConfigurations().stream()
                    .filter(TableConfiguration::isCleanAllGeneratedElements).collect(Collectors.toList());
            context.getTableConfigurations().clear();
            context.getTableConfigurations().addAll(configurationList);
            totalTables += context.getTableConfigurations().size();
        }

        if (totalTables == 0) {
            warnings.add(getString("Warning.103")); //$NON-NLS-1$
            return;
        }

        // now run the introspections...
        int totalSteps = 0;
        for (Context context : contextsToRun) {
            totalSteps += context.getIntrospectionSteps();
        }
        callback.introspectionStarted(totalSteps);

        // 清理文件
        for (Context context : contextsToRun) {
            // 清理文件
            cleanFiles(context, callback);
            callback.checkCancel();
        }
        callback.done();
    }

    private void cleanFiles(Context context, ProgressCallback callback) throws ShellException {
        // 清理文件
        for (TableConfiguration tc : context.getTableConfigurations()) {
            this.warnings.add(getString("Warning.100", tc.getTableName())); //$NON-NLS-1$

            //清理entity
            JavaModelGeneratorConfiguration modelConfig = context.getJavaModelGeneratorConfiguration();
            File allEntityFiles = this.shellCallback.getDirectory(modelConfig.getTargetProject(), modelConfig.getTargetPackage());
            File[] files = allEntityFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + ".java"));
            deleteFiles(files);

            String baseTargetPackage = modelConfig.getTargetPackage().substring(0, modelConfig.getTargetPackage().lastIndexOf("."));

            //清理example类
            File allExampleFiles = this.shellCallback.getDirectory(modelConfig.getTargetProject(), modelConfig.getTargetPackage() + ".example");
            files = allExampleFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "Example.java"));
            deleteFiles(files);
            //清理codegen下的文件
            String codegenPath = baseTargetPackage + ".codegen";
            File allCodegenControllerFiles = this.shellCallback.getDirectory(modelConfig.getTargetProject(), codegenPath + ".controller");
            files = allCodegenControllerFiles.listFiles((dir, name) -> name.equals("Abstract" + tc.getDomainObjectName() + "Controller.java"));
            deleteFiles(files);
            File allCodegenServiceFiles = this.shellCallback.getDirectory(modelConfig.getTargetProject(), codegenPath + ".service");
            files = allCodegenServiceFiles.listFiles((dir, name) -> name.equals("IGen" + tc.getDomainObjectName() + ".java"));
            deleteFiles(files);
            File allCodegenServiceImplFiles = this.shellCallback.getDirectory(modelConfig.getTargetProject(), codegenPath + ".service.impl");
            files = allCodegenServiceImplFiles.listFiles((dir, name) -> name.equals("Abstract" + tc.getDomainObjectName() + "Impl.java"));
            deleteFiles(files);
            File allCodegenMapperFiles = this.shellCallback.getDirectory(modelConfig.getTargetProject(), codegenPath + ".dao");
            files = allCodegenMapperFiles.listFiles((dir, name) -> name.equals("Gen" + tc.getDomainObjectName() + "Mapper.java"));
            deleteFiles(files);

            //清理pojo下的文件
            File allVoFiles = this.shellCallback.getDirectory(modelConfig.getTargetProject(), baseTargetPackage + ".pojo.vo");
            files = allVoFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "CreateVo.java"));
            deleteFiles(files);
            files = allVoFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "UpdateVo.java"));
            deleteFiles(files);
            files = allVoFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "Vo.java"));
            deleteFiles(files);
            files = allVoFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "ExcelImportVo.java"));
            deleteFiles(files);
            files = allVoFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "ExcelVo.java"));
            deleteFiles(files);
            files = allVoFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "RequestVo.java"));
            deleteFiles(files);
            files = allVoFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "RequestVo.java"));
            deleteFiles(files);
            files = allVoFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "ViewVo.java"));
            deleteFiles(files);
            File allPoFiles = this.shellCallback.getDirectory(modelConfig.getTargetProject(), baseTargetPackage + ".pojo.po");
            files = allPoFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "CachePo.java"));
            deleteFiles(files);
            File allMapsFiles = this.shellCallback.getDirectory(modelConfig.getTargetProject(), baseTargetPackage + ".pojo.maps");
            files = allMapsFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "Mappings.java"));
            deleteFiles(files);
            files = allMapsFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "CachePoMappings.java"));
            deleteFiles(files);
            File allAbsVoFiles = this.shellCallback.getDirectory(modelConfig.getTargetProject(), baseTargetPackage + ".pojo.abs");
            files = allAbsVoFiles.listFiles((dir, name) -> name.equals("Abstract" + tc.getDomainObjectName() + "Vo.java"));
            deleteFiles(files);

            //清理controller
            File allControllerFiles = this.shellCallback.getDirectory(modelConfig.getTargetProject(), baseTargetPackage + ".controller");
            files = allControllerFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "Controller.java"));
            deleteFiles(files);
            //清理service
            File allServiceFiles = this.shellCallback.getDirectory(modelConfig.getTargetProject(), baseTargetPackage + ".service");
            files = allServiceFiles.listFiles((dir, name) -> name.equals("I" + tc.getDomainObjectName() + ".java"));
            deleteFiles(files);
            File allServiceImplFiles = this.shellCallback.getDirectory(modelConfig.getTargetProject(), baseTargetPackage + ".service.impl");
            files = allServiceImplFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "Impl.java"));
            deleteFiles(files);
            //清理dao
            File allDaoFiles2 = this.shellCallback.getDirectory(modelConfig.getTargetProject(), baseTargetPackage + ".dao");
            files = allDaoFiles2.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "Mapper.java"));
            deleteFiles(files);

            //清理listener
            File allListenerFiles = this.shellCallback.getDirectory(modelConfig.getTargetProject(), baseTargetPackage + ".listener");
            files = allListenerFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "EventListener.java"));
            deleteFiles(files);
            File allFlowListenerFiles = this.shellCallback.getDirectory(modelConfig.getTargetProject(), baseTargetPackage + ".listener.flowable");
            files = allFlowListenerFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "FlowableEventListener.java"));
            deleteFiles(files);
            //清理xml
            String resourceTargetProject = tc.getSqlMapGeneratorConfiguration().getTargetProject();
            File allCodegenMapperXmlFiles = this.shellCallback.getDirectory(resourceTargetProject, tc.getSqlMapGeneratorConfiguration().getTargetPackage());
            files = allCodegenMapperXmlFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "Mapper.xml"));
            deleteFiles(files);
            //清理sql schema
            File allH2SchemaFiles = this.shellCallback.getDirectory(resourceTargetProject, "sql/h2");
            files = allH2SchemaFiles.listFiles((dir, name) -> name.equals("schema_" + tc.getTableName().toLowerCase() + ".sql"));
            deleteFiles(files);
            File allMysqlSchemaFiles = this.shellCallback.getDirectory(resourceTargetProject, "sql/mysql");
            files = allMysqlSchemaFiles.listFiles((dir, name) -> name.equals("schema_" + tc.getTableName().toLowerCase() + ".sql"));
            deleteFiles(files);
            File allInitDataFiles = this.shellCallback.getDirectory(resourceTargetProject, "sql/init");
            files = allInitDataFiles.listFiles((dir, name) -> name.equals("data-permission-" + tc.getTableName().toLowerCase() + ".sql"));
            deleteFiles(files);
            //清理js文件
            File allJsFiles = this.shellCallback.getDirectory(resourceTargetProject, "static/js/"+context.getModuleKeyword());
            files = allJsFiles.listFiles((dir, name) -> name.equals(tc.getTableName().toLowerCase() + ".js"));
            deleteFiles(files);
            //清理html文件
            File allHtmlFiles = this.shellCallback.getDirectory(resourceTargetProject, "templates/"+context.getModuleKeyword());
            files = allHtmlFiles.listFiles((dir, name) -> name.equals(tc.getTableName().toLowerCase() + ".html"));
            deleteFiles(files);
            //删除fragments片段html
            File allFragmentsFiles = this.shellCallback.getDirectory(resourceTargetProject, "templates/"+context.getModuleKeyword()+"/fragments");
            files = allFragmentsFiles.listFiles((dir, name) -> name.equals(tc.getTableName().toLowerCase() + "-list_inner_list_fragments.html"));
            deleteFiles(files);
            //清理vue文件
            String vueEndProjectPath = context.getVueEndProjectPath();
            String vueFilePath = vueEndProjectPath + "/src/modules/"+context.getModuleKeyword().toLowerCase();
            File file = new File(vueFilePath+"/"+tc.getDomainObjectName().toLowerCase());

            //清理modal
            File modalsPath = this.shellCallback.getDirectory(vueFilePath, "modals");
            File[] modals = modalsPath.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "Modal.vue"));
            deleteFiles(modals);

            if (file.exists()) {
                //清理components types
                File components = this.shellCallback.getDirectory(vueFilePath+"/"+tc.getDomainObjectName().toLowerCase(), "components");
                deleteFiles(components.listFiles());
                //清理types
                File types = this.shellCallback.getDirectory(vueFilePath+"/"+tc.getDomainObjectName().toLowerCase(), "types");
                deleteFiles(types.listFiles());

                File vueOther = this.shellCallback.getDirectory(vueFilePath, tc.getDomainObjectName().toLowerCase());
                deleteFiles(vueOther.listFiles());
                if (vueOther.isDirectory()) {
                    if (vueOther.delete()) {
                        warnings.add(getString("Warning.101", vueOther.getAbsolutePath()));
                    } else {
                        warnings.add(getString("Warning.102", vueOther.getAbsolutePath()));
                    }
                }
            }
            File views = this.shellCallback.getDirectory(vueFilePath, "views");
            files = views.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + ".vue"));
            deleteFiles(files);
            //清理启动包中的controller的单元测试类
            String targetProject = "src/test/java";
            String property = context.getProperty(PropertyRegistry.CONTEXT_ROOT_MODULE_NAME);
            if (StringUtility.stringHasValue(property)) {
                String tmpStr = StringUtility.getTargetProject("$PROJECT_DIR$\\" + property);
                targetProject = tmpStr + "\\" + targetProject;
                String path = baseTargetPackage.substring(0, baseTargetPackage.lastIndexOf("."));
                File allTestFiles = this.shellCallback.getDirectory(targetProject, String.join(".",path,context.getModuleKeyword(),"controller"));
                files = allTestFiles.listFiles((dir, name) -> name.equals(tc.getDomainObjectName() + "ControllerTest.java"));
                deleteFiles(files);
            }
        }
    }

    private void deleteFiles(File[] files) {
        if (files != null) {
            for (File file : files) {
                if (file.delete()) {
                    warnings.add(getString("Warning.101", file.getAbsolutePath()));
                } else {
                    warnings.add(getString("Warning.102", file.getAbsolutePath()));
                }
            }
        }
    }

    private void checkFileDir(GeneratedFile gf) {
        File gfDirector = new File(gf.getTargetProject() + File.separator + gf.getTargetPackage());
        if (!gfDirector.isDirectory()) {
            final boolean mkDirs = gfDirector.mkdirs();
            if (!mkDirs) {
                warnings.add(getString("Warning.10", gf.getTargetProject() + File.separator + gf.getTargetPackage()));
            }
        }
    }
}
