package org.mybatis.generator.plugins.dsql;

import java.util.List;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinFunction;

/**
 * Disables all update methods in the MyBatis3DynamicSQL and MyBatis3Kotlin runtimes.
 *
 * @author Jeff Butler
 *
 */
public class DisableUpdatePlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean clientGeneralUpdateMethodGenerated(Method method, Interface interfaze,
            AbstractIntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientGeneralUpdateMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            AbstractIntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBlobsMethodGenerated(Method method, Interface interfaze,
                                                                    AbstractIntrospectedTable abstractIntrospectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateAllColumnsMethodGenerated(Method method, Interface interfaze,
            AbstractIntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateAllColumnsMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            AbstractIntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateSelectiveColumnsMethodGenerated(Method method, Interface interfaze,
            AbstractIntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateSelectiveColumnsMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            AbstractIntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze,
            AbstractIntrospectedTable abstractIntrospectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateBatchMethodGenerated(Method method, Interface interfaze, AbstractIntrospectedTable abstractIntrospectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(KotlinFunction kotlinFunction,
            KotlinFile kotlinFile, AbstractIntrospectedTable abstractIntrospectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            AbstractIntrospectedTable abstractIntrospectedTable) {
        return false;
    }
}
