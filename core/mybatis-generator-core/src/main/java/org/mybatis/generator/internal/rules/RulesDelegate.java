package org.mybatis.generator.internal.rules;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;

/**
 * This class can be used by plugins to easily implement a custom rules
 * implementation. Plugins should respect the rules implementation calculated by
 * the generator, as well as implementations from other plugins. In general if
 * something is disabled by the default rules, or is disabled by some other
 * plugin, it should not be re-enabled. Therefore, the following pattern of use
 * is recommended:
 *
 * <pre>
 * public class MyPlugin extends PluginAdapter {
 *   &#64;Override
 *   public void initialized(IntrospectedTable introspectedTable) {
 *     MyRules myRules = new MyRules(introspectedTable.getRules());
 *     introspectedTable.setRules(myRules);
 *   }
 * }
 *
 * public class MyRules extends RulesDelegate (
 *   public MyRules(Rules rules) {
 *     super(rules);
 *   }
 *
 *   &#64;Override
 *   public boolean generateInsert() {
 *     boolean rc = super.generateInsert();
 *     if (rc) {
 *       // Other plugins, and the default rules, enable generation
 *       // of the insert method.  We can decide to disable it here
 *       // if needed.
 *     }
 *
 *     return rc;
 *   }
 * </pre>
 *
 *
 * @author Jeff Butler
 *
 */
public class RulesDelegate implements Rules {
    protected final Rules rules;

    public RulesDelegate(Rules rules) {
        this.rules = rules;
    }

    @Override
    public FullyQualifiedJavaType calculateAllFieldsClass() {
        return rules.calculateAllFieldsClass();
    }

    @Override
    public boolean generateBaseRecordClass() {
        return rules.generateBaseRecordClass();
    }

    @Override
    public boolean generateBaseResultMap() {
        return rules.generateBaseResultMap();
    }

    @Override
    public boolean generateCountByExample() {
        return rules.generateCountByExample();
    }

    @Override
    public boolean generateDeleteByExample() {
        return rules.generateDeleteByExample();
    }

    @Override
    public boolean generateDeleteByPrimaryKey() {
        return rules.generateDeleteByPrimaryKey();
    }

    @Override
    public boolean generateExampleClass() {
        return rules.generateExampleClass();
    }

    @Override
    public boolean generateInsert() {
        return rules.generateInsert();
    }

    @Override
    public boolean generateInsertBatch() {
        return rules.generateInsertBatch();
    }

    @Override
    public boolean generateInsertSelective() {
        return rules.generateInsertSelective();
    }

    @Override
    public boolean generatePrimaryKeyClass() {
        return rules.generatePrimaryKeyClass();
    }

    @Override
    public boolean generateRecordWithBLOBsClass() {
        return rules.generateRecordWithBLOBsClass();
    }

    @Override
    public boolean generateResultMapWithBLOBs() {
        return rules.generateResultMapWithBLOBs();
    }

    @Override
    public boolean generateSelectByExampleWithBLOBs() {
        return rules.generateSelectByExampleWithBLOBs();
    }

    @Override
    public boolean generateSelectByExampleWithoutBLOBs() {
        return rules.generateSelectByExampleWithoutBLOBs();
    }

    @Override
    public boolean generateSelectByPrimaryKey() {
        return rules.generateSelectByPrimaryKey();
    }

    @Override
    public boolean generateSQLExampleWhereClause() {
        return rules.generateSQLExampleWhereClause();
    }

    @Override
    public boolean generateMyBatis3UpdateByExampleWhereClause() {
        return rules.generateMyBatis3UpdateByExampleWhereClause();
    }

    @Override
    public boolean generateUpdateByExampleSelective() {
        return rules.generateUpdateByExampleSelective();
    }

    @Override
    public boolean generateUpdateByExampleWithBLOBs() {
        return rules.generateUpdateByExampleWithBLOBs();
    }

    @Override
    public boolean generateUpdateByExampleWithoutBLOBs() {
        return rules.generateUpdateByExampleWithoutBLOBs();
    }

    @Override
    public boolean generateUpdateByPrimaryKeySelective() {
        return rules.generateUpdateByPrimaryKeySelective();
    }

    @Override
    public boolean generateInsertOrUpdate() {
        return rules.generateInsertOrUpdate();
    }

    @Override
    public boolean createEnableSelective() {
        return rules.createEnableSelective();
    }

    @Override
    public boolean updateEnableSelective() {
        return rules.updateEnableSelective();
    }

    @Override
    public boolean generateUpdateBatch() {
        return rules.generateUpdateBatch();
    }

    @Override
    public boolean generateUpdateByPrimaryKeyWithBLOBs() {
        return rules.generateUpdateByPrimaryKeyWithBLOBs();
    }

    @Override
    public boolean generateUpdateByPrimaryKeyWithoutBLOBs() {
        return rules.generateUpdateByPrimaryKeyWithoutBLOBs();
    }

    @Override
    public IntrospectedTable getIntrospectedTable() {
        return rules.getIntrospectedTable();
    }

    @Override
    public boolean generateRelationWithSubSelected() {
        return false;
    }

    /**
     * 是否集成MybatisPlus
     */
    @Override
    public boolean isIntegrateMybatisPlus() {
        return false;
    }

    /**
     * 是否集成spring security。默认为true
     */
    @Override
    public boolean isIntegrateSpringSecurity() {
        return false;
    }

    @Override
    public boolean generateBaseColumnList() {
        return rules.generateBaseColumnList();
    }

    @Override
    public boolean generateBlobColumnList() {
        return rules.generateBlobColumnList();
    }

    @Override
    public boolean generateJavaClient() {
        return rules.generateJavaClient();
    }

    @Override
    public boolean isGenerateServiceUnitTest() {
        return false;
    }

    @Override
    public boolean isGenerateControllerUnitTest() {
        return false;
    }

    @Override
    public boolean isGenerateVO() {
        return false;
    }

    @Override
    public boolean isGenerateViewVO() {
        return false;
    }

    @Override
    public boolean isGenerateRecycleBin() {
        return false;
    }

    @Override
    public boolean isGenerateHideListBin() {
        return false;
    }

    @Override
    public boolean isGenerateEventListener() {
        return false;
    }

    @Override
    public boolean isGenerateWfEventListener() {
        return false;
    }

    @Override
    public boolean isGenerateInnerTable() {
        return false;
    }

    @Override
    public boolean isAdditionInnerList(HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        return false;
    }

    @Override
    public boolean isGenerateApprovalComment(HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        return false;
    }

    @Override
    public boolean isGenerateExcelVO() {
        return false;
    }

    @Override
    public boolean isGenerateRequestVO() {
        return false;
    }

    @Override
    public boolean isGenerateCachePO() {
        return false;
    }

    @Override
    public boolean isModelEnableChildren() {
        return false;
    }

    @Override
    public boolean isGenerateCachePOWithMultiKey() {
        return false;
    }

    @Override
    public boolean isForceGenerateScalableElement(String element) {
        return false;
    }

    @Override
    public boolean isGenerateVueEnd() {
        return false;
    }

    /**
     * 是生成VO对象
     */
    @Override
    public boolean isGenerateAnyVO() {
        return false;
    }

    @Override
    public boolean isGenerateVoModel() {
        return false;
    }

    @Override
    public boolean isGenerateCreateVO() {
        return false;
    }

    @Override
    public boolean isGenerateUpdateVO() {
        return false;
    }

    @Override
    public boolean generateSelectByColumn() {
        return false;
    }

    @Override
    public boolean generateSelectByTable() {
        return false;
    }

    @Override
    public boolean generateFileUpload() {
        return false;
    }
}
