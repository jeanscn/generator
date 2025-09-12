package org.mybatis.generator.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

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
    public void initialized(AbstractIntrospectedTable introspectedTable) {
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
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(AbstractIntrospectedTable abstractIntrospectedTable) {
        return plugins.stream()
                .map(p -> p.contextGenerateAdditionalJavaFiles(abstractIntrospectedTable))
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
    public List<GeneratedKotlinFile> contextGenerateAdditionalKotlinFiles(AbstractIntrospectedTable abstractIntrospectedTable) {
        return plugins.stream()
                .map(p -> p.contextGenerateAdditionalKotlinFiles(abstractIntrospectedTable))
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
    public List<GeneratedFile> contextGenerateAdditionalFiles(AbstractIntrospectedTable abstractIntrospectedTable) {
        return plugins.stream()
                .map(p -> p.contextGenerateAdditionalFiles(abstractIntrospectedTable))
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
    public List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles(AbstractIntrospectedTable abstractIntrospectedTable) {
        return plugins.stream()
                .map(p -> p.contextGenerateAdditionalXmlFiles(abstractIntrospectedTable))
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
    public List<GeneratedFile> contextGenerateAdditionalWebFiles(AbstractIntrospectedTable abstractIntrospectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        return plugins.stream()
                .map(p -> p.contextGenerateAdditionalWebFiles(abstractIntrospectedTable,htmlGeneratorConfiguration))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public boolean clientGenerated(Interface interfaze, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGenerated(interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean subClientGenerated(Interface interfaze, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.subClientGenerated(interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean clientBasicInsertMethodGenerated(Method method, Interface interfaze,
                                                    AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicInsertMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean clientBasicInsertMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                    AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicInsertMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean clientBasicInsertMultipleMethodGenerated(Method method, Interface interfaze,
                                                            AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicInsertMultipleMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean clientBasicInsertMultipleMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                            AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicInsertMultipleMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean clientBasicSelectManyMethodGenerated(Method method, Interface interfaze,
                                                        AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicSelectManyMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientBasicSelectManyMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                        AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicSelectManyMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientBasicSelectOneMethodGenerated(Method method, Interface interfaze,
                                                       AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicSelectOneMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientBasicSelectOneMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                       AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientBasicSelectOneMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientCountByExampleMethodGenerated(Method method, Interface interfaze,
                                                       AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientCountByExampleMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientDeleteByExampleMethodGenerated(Method method, Interface interfaze,
                                                        AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientDeleteByExampleMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze,
                                                           AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientDeleteByPrimaryKeyMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                           AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientDeleteByPrimaryKeyMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralCountMethodGenerated(Method method, Interface interfaze,
                                                     AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralCountMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientInsertOrDeleteByTableMethodGenerated(Method method, Interface interfaze,
                                                              AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertOrDeleteByTableMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralCountMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                     AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralCountMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralDeleteMethodGenerated(Method method, Interface interfaze,
                                                      AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralDeleteMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralDeleteMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                      AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralDeleteMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralSelectDistinctMethodGenerated(Method method, Interface interfaze,
                                                              AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralSelectDistinctMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralSelectDistinctMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                              AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralSelectDistinctMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralSelectMethodGenerated(Method method, Interface interfaze,
                                                      AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralSelectMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralSelectMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                      AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralSelectMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralUpdateMethodGenerated(Method method, Interface interfaze,
                                                      AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralUpdateMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientGeneralUpdateMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                      AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientGeneralUpdateMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze,
                                               AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientInsertOrUpdateMethodGenerated(Method method, Interface interfaze, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertOrUpdateMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean clientInsertMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                               AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientInsertMultipleMethodGenerated(Method method, Interface interfaze,
                                                       AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertMultipleMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientInsertMultipleMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                       AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertMultipleMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method, Interface interfaze,
                                                        AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertSelectiveMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;

    }

    @Override
    public boolean clientInsertBatchMethodGenerated(Method method, Interface interfaze, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertBatchMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                        AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertSelectiveMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze,
                                                                 AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientSelectByExampleWithBLOBsMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
                                                                    AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientSelectByExampleWithoutBLOBsMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze,
                                                           AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientSelectByPrimaryKeyMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientSelectByKeysDicMethodGenerated(Method method, Interface interfaze, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientSelectByKeysDicMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                           AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientSelectByPrimaryKeyMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientSelectListFieldGenerated(Field field, Interface interfaze,
                                                  AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientSelectListFieldGenerated(field, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientSelectOneMethodGenerated(Method method, Interface interfaze,
                                                  AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientSelectOneMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientSelectOneMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                  AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientSelectOneMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze,
                                                                 AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateByExampleSelectiveMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateAllColumnsMethodGenerated(Method method, Interface interfaze,
                                                         AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateAllColumnsMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateAllColumnsMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                         AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateAllColumnsMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateSelectiveColumnsMethodGenerated(Method method, Interface interfaze,
                                                               AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateSelectiveColumnsMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateSelectiveColumnsMethodGenerated(KotlinFunction kotlinFunction, KotlinFile
            kotlinFile,
                                                               AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateSelectiveColumnsMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze,
                                                                 AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateByExampleWithBLOBsMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
                                                                    AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateByExampleWithoutBLOBsMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze,
                                                                    AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateByPrimaryKeySelectiveMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateBatchMethodGenerated(Method method, Interface interfaze, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateBatchMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(KotlinFunction kotlinFunction,
                                                                    KotlinFile kotlinFile, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateByPrimaryKeySelectiveMethodGenerated(kotlinFunction, kotlinFile,
                    abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze,
                                                                    AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
                                                                       AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientSelectAllMethodGenerated(Method method, Interface interfaze,
                                                  AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientSelectAllMethodGenerated(method, interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass,
                                       IntrospectedColumn introspectedColumn, AbstractIntrospectedTable abstractIntrospectedTable,
                                       ModelClassType modelClassType) {
        for (Plugin plugin : plugins) {
            if (!plugin.modelFieldGenerated(field, topLevelClass, introspectedColumn, abstractIntrospectedTable,
                    modelClassType)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voAbstractFieldGenerated(field, topLevelClass, introspectedColumn, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelFieldGenerated(field, topLevelClass, introspectedColumn, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voUpdateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voUpdateFieldGenerated(field, topLevelClass, introspectedColumn, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voCreateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voCreateFieldGenerated(field, topLevelClass, introspectedColumn, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelGetterMethodGenerated(Method method,
                                                 TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                                 AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelGetterMethodGenerated(method, topLevelClass, introspectedColumn, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voCreateGetterMethodGenerated(Method method,
                                                TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                                AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voCreateGetterMethodGenerated(method, topLevelClass, introspectedColumn, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voUpdateGetterMethodGenerated(Method method,
                                                 TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                                 AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voUpdateGetterMethodGenerated(method, topLevelClass, introspectedColumn, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voViewFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voViewFieldGenerated(field, topLevelClass, introspectedColumn, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voExcelFieldGenerated(Field field, TopLevelClass topLevelClass,
                                         IntrospectedColumn introspectedColumn,
                                         AbstractIntrospectedTable abstractIntrospectedTable, int index) {
        for (Plugin plugin : plugins) {
            if (!plugin.voExcelFieldGenerated(field, topLevelClass, introspectedColumn, abstractIntrospectedTable,index)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voExcelImportFieldGenerated(Field field, TopLevelClass topLevelClass,
                                               IntrospectedColumn introspectedColumn,
                                               AbstractIntrospectedTable abstractIntrospectedTable, int index) {
        for (Plugin plugin : plugins) {
            if (!plugin.voExcelImportFieldGenerated(field, topLevelClass, introspectedColumn, abstractIntrospectedTable,index)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voRequestFieldGenerated(Field field, TopLevelClass topLevelClass,
                                         IntrospectedColumn introspectedColumn,
                                         AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voRequestFieldGenerated(field, topLevelClass, introspectedColumn, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass,
                                              IntrospectedColumn introspectedColumn, AbstractIntrospectedTable abstractIntrospectedTable,
                                              ModelClassType modelClassType) {
        for (Plugin plugin : plugins) {
            if (!plugin.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, abstractIntrospectedTable,
                    modelClassType)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass,
                                              IntrospectedColumn introspectedColumn, AbstractIntrospectedTable abstractIntrospectedTable,
                                              ModelClassType modelClassType) {
        for (Plugin plugin : plugins) {
            if (!plugin.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, abstractIntrospectedTable,
                    modelClassType)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.modelPrimaryKeyClassGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.modelBaseRecordClassGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelAbstractClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelAbstractClassGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelRecordClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelRecordClassGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelCreateClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelCreateClassGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelUpdateClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelUpdateClassGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelViewClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelViewClassGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelExcelClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelExcelClassGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelExcelImportClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelExcelImportClassGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelRequestClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelRequestClassGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean voModelCacheClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.voModelCacheClassGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass,
                                                      AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.modelRecordWithBLOBsClassGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.modelExampleClassGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapGenerated(sqlMap, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapDocumentGenerated(document, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean htmlMapDocumentGenerated(org.mybatis.generator.api.dom.html.Document document, AbstractIntrospectedTable abstractIntrospectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        for (Plugin plugin : plugins) {
            if (!plugin.htmlMapDocumentGenerated(document, abstractIntrospectedTable, htmlGeneratorConfiguration)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean htmlMapGenerated(GeneratedHtmlFile htmlMap, AbstractIntrospectedTable abstractIntrospectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        for (Plugin plugin : plugins) {
            if (!plugin.htmlMapGenerated(htmlMap, abstractIntrospectedTable, htmlGeneratorConfiguration)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean sqlMapResultMapWithoutBLOBsElementGenerated(XmlElement element,
                                                               AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapResultMapWithoutBLOBsElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapCountByExampleElementGenerated(XmlElement element, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapCountByExampleElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapDeleteByExampleElementGenerated(XmlElement element, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapDeleteByExampleElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapDeleteByPrimaryKeyElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapExampleWhereClauseElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapBaseColumnListElementGenerated(XmlElement element, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapBaseColumnListElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapBlobColumnListElementGenerated(XmlElement element, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapBlobColumnListElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapInsertElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapInsertSelectiveElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapInsertOrUpdateSelectiveElementGenerated(XmlElement element, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapInsertOrUpdateSelectiveElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapResultMapWithBLOBsElementGenerated(XmlElement element, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapResultMapWithBLOBsElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapSelectAllElementGenerated(XmlElement element, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapSelectAllElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapSelectByPrimaryKeyElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapSelectByKeysDictElementGenerated(XmlElement element, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapSelectByKeysDictElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element,
                                                                     AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapSelectByExampleWithoutBLOBsElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element,
                                                                  AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapSelectByExampleWithBLOBsElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element,
                                                                  AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapUpdateByExampleSelectiveElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element,
                                                                  AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapUpdateByExampleWithBLOBsElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element,
                                                                     AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element,
                                                                     AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapUpdateByPrimaryKeySelectiveElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapUpdateBatchElementGenerated(XmlElement element,
                                                     AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapUpdateBatchElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element,
                                                                     AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element,
                                                                        AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(element, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerApplyWhereMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                     AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerApplyWhereMethodGenerated(method, topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerCountByExampleMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                         AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerCountByExampleMethodGenerated(method, topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerDeleteByExampleMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                          AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerDeleteByExampleMethodGenerated(method, topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerInsertSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                          AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerInsertSelectiveMethodGenerated(method, topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerSelectByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                                   AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerSelectByExampleWithBLOBsMethodGenerated(method, topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerSelectByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                                      AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerSelectByExampleWithoutBLOBsMethodGenerated(method, topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerUpdateByExampleSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                                   AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerUpdateByExampleSelectiveMethodGenerated(method, topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerUpdateByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                                   AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerUpdateByExampleWithBLOBsMethodGenerated(method, topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerUpdateByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                                      AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerUpdateByExampleWithoutBLOBsMethodGenerated(method, topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean providerUpdateByPrimaryKeySelectiveMethodGenerated(Method method, TopLevelClass topLevelClass,
                                                                      AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.providerUpdateByPrimaryKeySelectiveMethodGenerated(method, topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean dynamicSqlSupportGenerated(TopLevelClass supportClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.dynamicSqlSupportGenerated(supportClass, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override

    public boolean dynamicSqlSupportGenerated(KotlinFile kotlinFile, KotlinType outerSupportObject,
                                              KotlinType innerSupportClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.dynamicSqlSupportGenerated(kotlinFile, outerSupportObject, innerSupportClass,
                    abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean mapperGenerated(KotlinFile mapperFile, KotlinType mapper, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.mapperGenerated(mapperFile, mapper, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean kotlinDataClassGenerated(KotlinFile kotlinFile, KotlinType dataClass,
                                            AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.kotlinDataClassGenerated(kotlinFile, dataClass, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientColumnListPropertyGenerated(KotlinProperty kotlinProperty, KotlinFile kotlinFile,
                                                     AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientColumnListPropertyGenerated(kotlinProperty, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }

        return true;

    }

    @Override
    public boolean clientInsertMultipleVarargMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                             AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientInsertMultipleVarargMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                           AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.clientUpdateByPrimaryKeyMethodGenerated(kotlinFunction, kotlinFile, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean serviceGenerated(Interface interfaze, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.serviceGenerated(interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean subServiceGenerated(Interface interfaze, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.subServiceGenerated(interfaze, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean serviceImplGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.serviceImplGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean subServiceImplGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.subServiceImplGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean controllerGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.controllerGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean subControllerGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.subControllerGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean serviceUnitTestGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        for (Plugin plugin : plugins) {
            if (!plugin.serviceUnitTestGenerated(topLevelClass, abstractIntrospectedTable)) {
                return false;
            }
        }
        return true;
    }
}
