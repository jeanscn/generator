package org.mybatis.generator.api;

import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinFunction;
import org.mybatis.generator.api.dom.kotlin.KotlinProperty;
import org.mybatis.generator.api.dom.kotlin.KotlinType;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;

import org.mybatis.generator.codegen.mybatis3.htmlmapper.GeneratedHtmlFile;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * This class implements a composite plugin. It contains a list of plugins for the
 * current context and is used to aggregate plugins together. This class
 * implements the rule that if any plugin returns "false" from a method, then no
 * subsequent plugin is called.
 *
 * @author Jeff Butler
 */
public abstract class CompositePlugin implements Plugin {
    private final List<Plugin> plugins = new ArrayList<>();

    protected CompositePlugin() {
        super();
    }

    public void addPlugin(Plugin plugin) {
        plugins.add(plugin);
    }

    @Override
    public void setContext(Context context) {
        for (Plugin plugin : plugins) {
            plugin.setContext(context);
        }
    }

    @Override
    public void setProperties(Properties properties) {
        for (Plugin plugin : plugins) {
            plugin.setProperties(properties);
        }
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            plugin.initialized(introspectedTable);
        }
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles() {
        return plugins.stream()
                .map(Plugin::contextGenerateAdditionalJavaFiles)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        return plugins.stream()
                .map(p -> p.contextGenerateAdditionalJavaFiles(introspectedTable))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<GeneratedKotlinFile> contextGenerateAdditionalKotlinFiles() {
        return plugins.stream()
                .map(Plugin::contextGenerateAdditionalKotlinFiles)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<GeneratedKotlinFile> contextGenerateAdditionalKotlinFiles(IntrospectedTable introspectedTable) {
        return plugins.stream()
                .map(p -> p.contextGenerateAdditionalKotlinFiles(introspectedTable))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<GeneratedFile> contextGenerateAdditionalFiles() {
        return plugins.stream()
                .map(Plugin::contextGenerateAdditionalFiles)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<GeneratedFile> contextGenerateAdditionalFiles(IntrospectedTable introspectedTable) {
        return plugins.stream()
                .map(p -> p.contextGenerateAdditionalFiles(introspectedTable))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles() {
        return plugins.stream()
                .map(Plugin::contextGenerateAdditionalXmlFiles)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles(IntrospectedTable introspectedTable) {
        return plugins.stream()
                .map(p -> p.contextGenerateAdditionalXmlFiles(introspectedTable))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<GeneratedHtmlFile> contextGenerateAdditionalHtmlFiles() {
        return plugins.stream()
                .map(Plugin::contextGenerateAdditionalHtmlFiles)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<GeneratedFile> contextGenerateAdditionalWebFiles(IntrospectedTable introspectedTable,HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        return plugins.stream()
                .map(p -> p.contextGenerateAdditionalWebFiles(introspectedTable,htmlGeneratorConfiguration))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGenerated(interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean subClientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.subClientGenerated(interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean clientBasicCountMethodGenerated(Method method, Interface interfaze,
                                                   IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicCountMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean clientBasicCountMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                   IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicCountMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean clientBasicDeleteMethodGenerated(Method method, Interface interfaze,
                                                    IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicDeleteMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean clientBasicDeleteMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                    IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicDeleteMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean clientBasicInsertMethodGenerated(Method method, Interface interfaze,
                                                    IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicInsertMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean clientBasicInsertMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                    IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicInsertMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean clientBasicInsertMultipleMethodGenerated(Method method, Interface interfaze,
                                                            IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicInsertMultipleMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean clientBasicInsertMultipleMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                            IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicInsertMultipleMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean clientBasicInsertMultipleHelperMethodGenerated(Method method, Interface interfaze,
                                                                  IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicInsertMultipleHelperMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean clientBasicInsertMultipleHelperMethodGenerated(KotlinFunction kotlinFunction, KotlinFile
            kotlinFile,
                                                                  IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicInsertMultipleHelperMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean clientBasicSelectManyMethodGenerated(Method method, Interface interfaze,
                                                        IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicSelectManyMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientBasicSelectManyMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                        IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicSelectManyMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientBasicSelectOneMethodGenerated(Method method, Interface interfaze,
                                                       IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicSelectOneMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientBasicSelectOneMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                       IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicSelectOneMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientBasicUpdateMethodGenerated(Method method, Interface interfaze,
                                                    IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicUpdateMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean clientBasicUpdateMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                    IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicUpdateMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean clientCountByExampleMethodGenerated(Method method, Interface interfaze,
                                                       IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientCountByExampleMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientDeleteByExampleMethodGenerated(Method method, Interface interfaze,
                                                        IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientDeleteByExampleMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze,
                                                           IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientDeleteByPrimaryKeyMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                           IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientDeleteByPrimaryKeyMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralCountMethodGenerated(Method method, Interface interfaze,
                                                     IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralCountMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientInsertOrDeleteByTableMethodGenerated(Method method, Interface interfaze,
                                                              IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertOrDeleteByTableMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralCountMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                     IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralCountMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralDeleteMethodGenerated(Method method, Interface interfaze,
                                                      IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralDeleteMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralDeleteMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                      IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralDeleteMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralSelectDistinctMethodGenerated(Method method, Interface interfaze,
                                                              IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralSelectDistinctMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralSelectDistinctMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                              IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralSelectDistinctMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralSelectMethodGenerated(Method method, Interface interfaze,
                                                      IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralSelectMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralSelectMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                      IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralSelectMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralUpdateMethodGenerated(Method method, Interface interfaze,
                                                      IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralUpdateMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralUpdateMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                      IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralUpdateMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze,
                                               IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientInsertOrUpdateMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertOrUpdateMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean clientInsertMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                               IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientInsertMultipleMethodGenerated(Method method, Interface interfaze,
                                                       IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertMultipleMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientInsertMultipleMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                       IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertMultipleMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method, Interface interfaze,
                                                        IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertSelectiveMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }
        return true;

    }

    @Override
    public boolean clientInsertBatchMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertBatchMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                        IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertSelectiveMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze,
                                                                 IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientSelectByExampleWithBLOBsMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
                                                                    IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientSelectByExampleWithoutBLOBsMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze,
                                                           IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientSelectByPrimaryKeyMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientSelectByKeysDicMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientSelectByKeysDicMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                           IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientSelectByPrimaryKeyMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientSelectListFieldGenerated(Field field, Interface interfaze,
                                                  IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientSelectListFieldGenerated(field, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientSelectOneMethodGenerated(Method method, Interface interfaze,
                                                  IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientSelectOneMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientSelectOneMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                  IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientSelectOneMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze,
                                                                 IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateByExampleSelectiveMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateAllColumnsMethodGenerated(Method method, Interface interfaze,
                                                         IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateAllColumnsMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateAllColumnsMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                         IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateAllColumnsMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateSelectiveColumnsMethodGenerated(Method method, Interface interfaze,
                                                               IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateSelectiveColumnsMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateSelectiveColumnsMethodGenerated(KotlinFunction kotlinFunction, KotlinFile
            kotlinFile,
                                                               IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateSelectiveColumnsMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze,
                                                                 IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateByExampleWithBLOBsMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
                                                                    IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateByExampleWithoutBLOBsMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze,
                                                                    IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateByPrimaryKeySelectiveMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateBatchMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateBatchMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(KotlinFunction kotlinFunction,
                                                                    KotlinFile kotlinFile, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateByPrimaryKeySelectiveMethodGenerated(kotlinFunction, kotlinFile,
                    introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze,
                                                                    IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
                                                                       IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientSelectAllMethodGenerated(Method method, Interface interfaze,
                                                  IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientSelectAllMethodGenerated(method, interfaze, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass,
                                       IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable,
                                       ModelClassType modelClassType) {
        for (Plugin plugin : plugins) {
            if (!plugin.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable,
                    modelClassType)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voAbstractFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voUpdateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voUpdateFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voCreateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voCreateFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelGetterMethodGenerated(Method method,
                                                 TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                                 IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voCreateGetterMethodGenerated(Method method,
                                                TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                                IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voCreateGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voUpdateGetterMethodGenerated(Method method,
                                                 TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                                 IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voUpdateGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voViewFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voViewFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voExcelFieldGenerated(Field field, TopLevelClass topLevelClass,
                                          IntrospectedColumn introspectedColumn,
                                          IntrospectedTable introspectedTable,int index) {
        for (Plugin plugin : plugins) {
            if (!plugin.voExcelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable,index)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voExcelImportFieldGenerated(Field field, TopLevelClass topLevelClass,
                                         IntrospectedColumn introspectedColumn,
                                         IntrospectedTable introspectedTable,int index) {
        for (Plugin plugin : plugins) {
            if (!plugin.voExcelImportFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable,index)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voRequestFieldGenerated(Field field, TopLevelClass topLevelClass,
                                         IntrospectedColumn introspectedColumn,
                                         IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voRequestFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass,
                                              IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable,
                                              ModelClassType modelClassType) {
        for (Plugin plugin : plugins) {
            if (!plugin.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable,
                    modelClassType)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass,
                                              IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable,
                                              ModelClassType modelClassType) {
        for (Plugin plugin : plugins) {
            if (!plugin.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable,
                    modelClassType)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.modelPrimaryKeyClassGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.modelBaseRecordClassGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelAbstractClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelAbstractClassGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelRecordClassGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelCreateClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelCreateClassGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelUpdateClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelUpdateClassGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelViewClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelViewClassGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelExcelClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelExcelClassGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelExcelImportClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelExcelImportClassGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelRequestClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelRequestClassGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelCacheClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelCacheClassGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass,
                                                      IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.modelRecordWithBLOBsClassGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.modelExampleClassGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapGenerated(sqlMap, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapDocumentGenerated(document, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean htmlMapDocumentGenerated(org.mybatis.generator.api.dom.html.Document document, IntrospectedTable introspectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        for (Plugin plugin : plugins) {
            if (!plugin.htmlMapDocumentGenerated(document, introspectedTable, htmlGeneratorConfiguration)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean htmlMapGenerated(GeneratedHtmlFile htmlMap, IntrospectedTable introspectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        for (Plugin plugin : plugins) {
            if (!plugin.htmlMapGenerated(htmlMap, introspectedTable, htmlGeneratorConfiguration)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean sqlMapResultMapWithoutBLOBsElementGenerated(XmlElement element,
                                                               IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapResultMapWithoutBLOBsElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapCountByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapCountByExampleElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapDeleteByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapDeleteByExampleElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapDeleteByPrimaryKeyElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapExampleWhereClauseElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapBaseColumnListElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapBaseColumnListElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapBlobColumnListElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapBlobColumnListElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapInsertElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapInsertSelectiveElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapInsertOrUpdateSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapInsertOrUpdateSelectiveElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapResultMapWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapResultMapWithBLOBsElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapSelectAllElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapSelectAllElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapSelectByPrimaryKeyElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapSelectByKeysDictElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapSelectByKeysDictElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element,
                                                                     IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapSelectByExampleWithoutBLOBsElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element,
                                                                  IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapSelectByExampleWithBLOBsElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element,
                                                                  IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapUpdateByExampleSelectiveElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element,
                                                                  IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapUpdateByExampleWithBLOBsElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element,
                                                                     IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element,
                                                                     IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapUpdateByPrimaryKeySelectiveElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapUpdateBatchElementGenerated(XmlElement element,
                                                     IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapUpdateBatchElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element,
                                                                     IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element,
                                                                        IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(element, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerApplyWhereMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                     IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerApplyWhereMethodGenerated(method, topLevelClass, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerCountByExampleMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                         IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerCountByExampleMethodGenerated(method, topLevelClass, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerDeleteByExampleMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                          IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerDeleteByExampleMethodGenerated(method, topLevelClass, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerInsertSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                          IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerInsertSelectiveMethodGenerated(method, topLevelClass, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerSelectByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                                   IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerSelectByExampleWithBLOBsMethodGenerated(method, topLevelClass, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerSelectByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                                      IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerSelectByExampleWithoutBLOBsMethodGenerated(method, topLevelClass, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerUpdateByExampleSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                                   IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerUpdateByExampleSelectiveMethodGenerated(method, topLevelClass, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerUpdateByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                                   IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerUpdateByExampleWithBLOBsMethodGenerated(method, topLevelClass, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerUpdateByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                                      IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerUpdateByExampleWithoutBLOBsMethodGenerated(method, topLevelClass, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerUpdateByPrimaryKeySelectiveMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                                      IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerUpdateByPrimaryKeySelectiveMethodGenerated(method, topLevelClass, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean dynamicSqlSupportGenerated(TopLevelClass supportClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.dynamicSqlSupportGenerated(supportClass, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override

    public boolean dynamicSqlSupportGenerated(KotlinFile kotlinFile, KotlinType outerSupportObject,
                                              KotlinType innerSupportClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.dynamicSqlSupportGenerated(kotlinFile, outerSupportObject, innerSupportClass,
                    introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean mapperGenerated(KotlinFile mapperFile, KotlinType mapper, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.mapperGenerated(mapperFile, mapper, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean kotlinDataClassGenerated(KotlinFile kotlinFile, KotlinType dataClass,
                                            IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.kotlinDataClassGenerated(kotlinFile, dataClass, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientColumnListPropertyGenerated(KotlinProperty kotlinProperty, KotlinFile kotlinFile,
                                                     IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientColumnListPropertyGenerated(kotlinProperty, kotlinFile, introspectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientInsertMultipleVarargMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                             IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertMultipleVarargMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                           IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateByPrimaryKeyMethodGenerated(kotlinFunction, kotlinFile, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean serviceGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.serviceGenerated(interfaze, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean subServiceGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.subServiceGenerated(interfaze, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean serviceImplGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.serviceImplGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean subServiceImplGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.subServiceImplGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean controllerGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.controllerGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean subControllerGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.subControllerGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean serviceUnitTestGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.serviceUnitTestGenerated(topLevelClass, introspectedTable)) {
                return false;
            }
        }
        return true;
    }
}
