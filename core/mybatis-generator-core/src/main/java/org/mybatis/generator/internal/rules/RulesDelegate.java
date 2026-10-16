package org.mybatis.generator.internal.rules;

import com.vgosoft.core.constant.enums.data.StatTypeEnum;
import org.mybatis.generator.api.AbstractIntrospectedTable;
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

    /**
     * 同时尊重被代理规则与当前代理对普通主键查询的限制。
     *
     * @return 是否生成按主键锁定查询
     */
    @Override
    public boolean generateSelectByPrimaryKeyForUpdate() {
        return rules.generateSelectByPrimaryKeyForUpdate() && generateSelectByPrimaryKey();
    }

    /**
     * 同时尊重被代理规则与当前代理对普通条件查询的限制。
     *
     * @return 是否生成按条件锁定查询
     */
    @Override
    public boolean generateSelectByExampleForUpdate() {
        return rules.generateSelectByExampleForUpdate()
                && (generateSelectByExampleWithoutBlobs() || generateSelectByExampleWithBlobs());
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
    public boolean generateCasWriteContract() {
        return rules.generateCasWriteContract();
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
    public boolean generateRecordWithBlobsClass() {
        return rules.generateRecordWithBlobsClass();
    }

    @Override
    public boolean generateResultMapWithBlobs() {
        return rules.generateResultMapWithBlobs();
    }

    @Override
    public boolean generateSelectByExampleWithBlobs() {
        return rules.generateSelectByExampleWithBlobs();
    }

    @Override
    public boolean generateSelectByExampleWithoutBlobs() {
        return rules.generateSelectByExampleWithoutBlobs();
    }

    @Override
    public boolean generateSelectByPrimaryKey() {
        return rules.generateSelectByPrimaryKey();
    }

    @Override
    public boolean generateSqlExampleWhereClause() {
        return rules.generateSqlExampleWhereClause();
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
    public boolean generateUpdateByExampleWithBlobs() {
        return rules.generateUpdateByExampleWithBlobs();
    }

    @Override
    public boolean generateUpdateByExampleWithoutBlobs() {
        return rules.generateUpdateByExampleWithoutBlobs();
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
    public boolean generateAtomicIncrement() {
        return rules.generateAtomicIncrement();
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
    public boolean generateUpdateByPrimaryKeyWithBlobs() {
        return rules.generateUpdateByPrimaryKeyWithBlobs();
    }

    @Override
    public boolean generateUpdateByPrimaryKeyWithoutBlobs() {
        return rules.generateUpdateByPrimaryKeyWithoutBlobs();
    }

    @Override
    public AbstractIntrospectedTable getIntrospectedTable() {
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
    public boolean isGenerateStatisticsElement(StatTypeEnum statType) {
        return false;
    }

    @Override
    public boolean isGenerateCustomStatisticsElement() {
        return false;
    }

    @Override
    public boolean isGenerateControllerUnitTest() {
        return false;
    }

    @Override
    public boolean isGenerateVo() {
        return false;
    }

    @Override
    public boolean isGenerateViewVo() {
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
    public boolean isGenerateExcelVo() {
        return false;
    }

    @Override
    public boolean isGenerateRequestVo() {
        return false;
    }

    @Override
    public boolean isGenerateCachePo() {
        return false;
    }

    @Override
    public boolean isModelEnableChildren() {
        return false;
    }

    @Override
    public boolean isGenerateCachePoWithMultiKey() {
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

    @Override
    public boolean isGenerateEditHtml() {
        return false;
    }

    @Override
    public boolean isGeneratePrintHtml() {
        return false;
    }

    @Override
    public boolean isGenerateViewOnlyHtml() {
        return false;
    }

    /**
     * 是生成Vo对象
     */
    @Override
    public boolean isGenerateAnyVo() {
        return false;
    }

    @Override
    public boolean isGenerateVoModel() {
        return false;
    }

    @Override
    public boolean isGenerateCreateVo() {
        return false;
    }

    @Override
    public boolean isGenerateUpdateVo() {
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
