package org.mybatis.generator.internal.rules;

import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.IntrospectedTable.TargetRuntime;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.TableTypeEnum;
import org.mybatis.generator.config.RelationGeneratorConfiguration;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.Arrays;

/**
 * This class centralizes all the rules related to code generation - including
 * the methods and objects to create, and certain attributes related to those
 * objects.
 *
 * @author Jeff Butler
 */
public abstract class BaseRules implements Rules {

    protected final TableConfiguration tc;

    protected final IntrospectedTable introspectedTable;

    protected final boolean isModelOnly;

    protected final boolean isGenerateCont;

    protected boolean isGenerateHtml = false;

    protected final boolean generateService;

    protected final boolean generateDao;

    protected final boolean noMetaAnnotation;

    protected final boolean noSwaggerAnnotation;

    protected final boolean noServiceAnnotation;

    protected final boolean integrateMybatisPlus;

    protected final boolean integrateSpringSecurity;

    protected final boolean generateUnitTest;

    public BaseRules(IntrospectedTable introspectedTable) {
        super();
        this.introspectedTable = introspectedTable;
        this.tc = introspectedTable.getTableConfiguration();
        String modelOnly = tc.getProperty(PropertyRegistry.TABLE_MODEL_ONLY);
        isModelOnly = StringUtility.isTrue(modelOnly);

        JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration = tc.getJavaControllerGeneratorConfiguration();
        isGenerateCont = javaControllerGeneratorConfiguration != null && javaControllerGeneratorConfiguration.isGenerate();

        for (HtmlGeneratorConfiguration htmlGeneratorConfiguration : tc.getHtmlMapGeneratorConfigurations()) {
            if (htmlGeneratorConfiguration.isGenerate()) {
                isGenerateHtml = true;
                break;
            }
        }

        JavaServiceImplGeneratorConfiguration javaServiceImplGeneratorConfiguration = tc.getJavaServiceImplGeneratorConfiguration();
        generateService = javaServiceImplGeneratorConfiguration != null && javaServiceImplGeneratorConfiguration.isGenerate();

        JavaClientGeneratorConfiguration javaClientGeneratorConfiguration = tc.getJavaClientGeneratorConfiguration();
        generateDao = javaClientGeneratorConfiguration != null && javaClientGeneratorConfiguration.isGenerate();

        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = tc.getJavaModelGeneratorConfiguration();
        noMetaAnnotation = javaModelGeneratorConfiguration != null && javaModelGeneratorConfiguration.isNoMetaAnnotation();

        noSwaggerAnnotation = javaControllerGeneratorConfiguration != null && javaControllerGeneratorConfiguration.isNoSwaggerAnnotation();

        noServiceAnnotation = javaServiceImplGeneratorConfiguration != null && javaServiceImplGeneratorConfiguration.isNoServiceAnnotation();

        this.integrateMybatisPlus = introspectedTable.getContext().isIntegrateMybatisPlus();

        this.integrateSpringSecurity = introspectedTable.getContext().isIntegrateSpringSecurity();

        this.generateUnitTest = true;
    }

    public boolean generateController() {
        if (isModelOnly) {
            return false;
        }
        return isGenerateCont;
    }

    public boolean generateService() {
        if (isModelOnly) {
            return false;
        }
        return generateService;
    }

    public boolean isNoMetaAnnotation() {
        return noMetaAnnotation;
    }

    public boolean isNoSwaggerAnnotation() {
        return noSwaggerAnnotation;
    }

    public boolean isNoServiceAnnotation() {
        return noServiceAnnotation;
    }

    /**
     * Implements the rule for generating the insert SQL Map element and DAO
     * method. If the insert statement is allowed, then generate the element and
     * method.
     *
     * @return true if the element and method should be generated
     */
    @Override
    public boolean generateInsert() {
        if (isModelOnly) {
            return false;
        }

        return tc.isInsertStatementEnabled();
    }

    @Override
    public boolean generateInsertBatch() {
        if (isModelOnly) {
            return false;
        }

        return tc.isInsertBatchStatementEnabled();
    }

    @Override
    public boolean generateInsertOrUpdate() {
        if (isModelOnly) {
            return false;
        }

        return tc.isInsertOrUpdateStatementEnabled();
    }

    @Override
    public boolean createEnableSelective() {
        return tc.getVoGeneratorConfiguration().getVoCreateConfiguration().isEnableSelective();
    }

    @Override
    public boolean updateEnableSelective() {
        return tc.getVoGeneratorConfiguration().getVoUpdateConfiguration().isEnableSelective();
    }

    /**
     * Implements the rule for generating the insert selective SQL Map element
     * and DAO method. If the insert statement is allowed, then generate the
     * element and method.
     *
     * @return true if the element and method should be generated
     */
    @Override
    public boolean generateInsertSelective() {
        return generateInsert();
    }

    /**
     * Calculates the class that contains all fields. This class is used as the
     * insert statement parameter, as well as the returned value from the select
     * by primary key method. The actual class depends on how the domain model
     * is generated.
     *
     * @return the type of the class that holds all fields
     */
    @Override
    public FullyQualifiedJavaType calculateAllFieldsClass() {

        String answer;

        if (generateRecordWithBLOBsClass()) {
            answer = introspectedTable.getRecordWithBLOBsType();
        } else if (generateBaseRecordClass()) {
            answer = introspectedTable.getBaseRecordType();
        } else {
            answer = introspectedTable.getPrimaryKeyType();
        }

        return new FullyQualifiedJavaType(answer);
    }

    /**
     * Implements the rule for generating the update by primary key without
     * BLOBs SQL Map element and DAO method. If the table has a primary key as
     * well as other non-BLOB fields, and the updateByPrimaryKey statement is
     * allowed, then generate the element and method.
     *
     * @return true if the element and method should be generated
     */
    @Override
    public boolean generateUpdateByPrimaryKeyWithoutBLOBs() {
        if (isModelOnly) {
            return false;
        }

        if (ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getBaseColumns()).isEmpty()) {
            return false;
        }

        return tc.isUpdateByPrimaryKeyStatementEnabled()
                && introspectedTable.hasPrimaryKeyColumns()
                && introspectedTable.hasBaseColumns();
    }

    /**
     * Implements the rule for generating the update by primary key with BLOBs
     * SQL Map element and DAO method. If the table has a primary key as well as
     * other BLOB fields, and the updateByPrimaryKey statement is allowed, then
     * generate the element and method.
     *
     * @return true if the element and method should be generated
     */
    @Override
    public boolean generateUpdateByPrimaryKeyWithBLOBs() {
        if (isModelOnly) {
            return false;
        }

        if (ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns()).isEmpty()) {
            return false;
        }

        return tc.isUpdateByPrimaryKeyStatementEnabled()
                && introspectedTable.hasPrimaryKeyColumns()
                && introspectedTable.hasBLOBColumns();
    }

    /**
     * Implements the rule for generating the update by primary key selective
     * SQL Map element and DAO method. If the table has a primary key as well as
     * other fields, and the updateByPrimaryKey statement is allowed, then
     * generate the element and method.
     *
     * @return true if the element and method should be generated
     */
    @Override
    public boolean generateUpdateByPrimaryKeySelective() {
        if (isModelOnly) {
            return false;
        }

        if (ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns()).isEmpty()) {
            return false;
        }

        return tc.isUpdateByPrimaryKeyStatementEnabled()
                && introspectedTable.hasPrimaryKeyColumns()
                && (introspectedTable.hasBLOBColumns() || introspectedTable
                .hasBaseColumns());
    }

    @Override
    public boolean generateUpdateBatch() {
        if (isModelOnly) {
            return false;
        }

        if (ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns()).isEmpty()) {
            return false;
        }

        return tc.isUpdateBatchStatementEnabled()
                && introspectedTable.hasPrimaryKeyColumns()
                && (introspectedTable.hasBLOBColumns() || introspectedTable.hasBaseColumns());
    }

    /**
     * Implements the rule for generating the delete by primary key SQL Map
     * element and DAO method. If the table has a primary key, and the
     * deleteByPrimaryKey statement is allowed, then generate the element and
     * method.
     *
     * @return true if the element and method should be generated
     */
    @Override
    public boolean generateDeleteByPrimaryKey() {
        if (isModelOnly) {
            return false;
        }

        return tc.isDeleteByPrimaryKeyStatementEnabled()
                && introspectedTable.hasPrimaryKeyColumns();
    }

    /**
     * Implements the rule for generating the delete by example SQL Map element
     * and DAO method. If the deleteByExample statement is allowed, then
     * generate the element and method.
     *
     * @return true if the element and method should be generated
     */
    @Override
    public boolean generateDeleteByExample() {
        if (isModelOnly) {
            return false;
        }

        return tc.isDeleteByExampleStatementEnabled();
    }

    /**
     * Implements the rule for generating the result map without BLOBs. If
     * either select method is allowed, then generate the result map.
     *
     * @return true if the result map should be generated
     */
    @Override
    public boolean generateBaseResultMap() {
        if (isModelOnly) {
            return true;
        }

        return tc.isSelectByExampleStatementEnabled()
                || tc.isSelectByPrimaryKeyStatementEnabled();
    }

    /**
     * Implements the rule for generating the result map with BLOBs. If the
     * table has BLOB columns, and either select method is allowed, then
     * generate the result map.
     *
     * @return true if the result map should be generated
     */
    @Override
    public boolean generateResultMapWithBLOBs() {
        boolean rc;

        if (introspectedTable.hasBLOBColumns()) {
            if (isModelOnly) {
                rc = true;
            } else {
                rc = tc.isSelectByExampleStatementEnabled()
                        || tc.isSelectByPrimaryKeyStatementEnabled();
            }
        } else {
            rc = false;
        }

        return rc;
    }

    /**
     * Implements the rule for generating the SQL example where clause element.
     *
     * <p>In MyBatis3, generate the element if the selectByExample,
     * deleteByExample, or countByExample statements are allowed.
     *
     * @return true if the SQL where clause element should be generated
     */
    @Override
    public boolean generateSQLExampleWhereClause() {
        if (isModelOnly) {
            return false;
        }

        return tc.isSelectByExampleStatementEnabled()
                || tc.isDeleteByExampleStatementEnabled()
                || tc.isCountByExampleStatementEnabled();
    }

    /**
     * Implements the rule for generating the SQL example where clause element
     * specifically for use in the update by example methods.
     *
     * <p>In MyBatis3, generate the element if the updateByExample statements are
     * allowed.
     *
     * @return true if the SQL where clause element should be generated
     */
    @Override
    public boolean generateMyBatis3UpdateByExampleWhereClause() {
        if (isModelOnly) {
            return false;
        }

        return introspectedTable.getTargetRuntime() == TargetRuntime.MYBATIS3
                && tc.isUpdateByExampleStatementEnabled();
    }

    /**
     * Implements the rule for generating the select by primary key SQL Map
     * element and DAO method. If the table has a primary key as well as other
     * fields, and the selectByPrimaryKey statement is allowed, then generate
     * the element and method.
     *
     * @return true if the element and method should be generated
     */
    @Override
    public boolean generateSelectByPrimaryKey() {
        if (isModelOnly) {
            return false;
        }

        return tc.isSelectByPrimaryKeyStatementEnabled()
                && introspectedTable.hasPrimaryKeyColumns()
                && (introspectedTable.hasBaseColumns() || introspectedTable
                .hasBLOBColumns());
    }

    /**
     * Implements the rule for generating the select by example without BLOBs
     * SQL Map element and DAO method. If the selectByExample statement is
     * allowed, then generate the element and method.
     *
     * @return true if the element and method should be generated
     */
    @Override
    public boolean generateSelectByExampleWithoutBLOBs() {
        if (isModelOnly) {
            return false;
        }

        return tc.isSelectByExampleStatementEnabled();
    }

    /**
     * Implements the rule for generating the select by example with BLOBs SQL
     * Map element and DAO method. If the table has BLOB fields and the
     * selectByExample statement is allowed, then generate the element and
     * method.
     *
     * @return true if the element and method should be generated
     */
    @Override
    public boolean generateSelectByExampleWithBLOBs() {
        if (isModelOnly) {
            return false;
        }

        return tc.isSelectByExampleStatementEnabled()
                && introspectedTable.hasBLOBColumns();
    }

    /**
     * Implements the rule for generating an example class. The class should be
     * generated if the selectByExample or deleteByExample or countByExample
     * methods are allowed.
     *
     * @return true if the example class should be generated
     */
    @Override
    public boolean generateExampleClass() {
        if (introspectedTable.getContext().getSqlMapGeneratorConfiguration() == null
                && introspectedTable.getContext().getJavaClientGeneratorConfiguration() == null) {
            // this is a model only context - don't generate the example class
            return false;
        }
        return tc.isSelectByExampleStatementEnabled()
                || tc.isDeleteByExampleStatementEnabled()
                || tc.isCountByExampleStatementEnabled()
                || tc.isUpdateByExampleStatementEnabled();
    }

    @Override
    public boolean generateCountByExample() {
        if (isModelOnly) {
            return false;
        }

        return tc.isCountByExampleStatementEnabled();
    }

    @Override
    public boolean generateUpdateByExampleSelective() {
        if (isModelOnly) {
            return false;
        }

        return tc.isUpdateByExampleStatementEnabled();
    }

    @Override
    public boolean generateUpdateByExampleWithoutBLOBs() {
        if (isModelOnly) {
            return false;
        }

        return tc.isUpdateByExampleStatementEnabled()
                && (introspectedTable.hasPrimaryKeyColumns() || introspectedTable
                .hasBaseColumns());
    }

    @Override
    public boolean generateUpdateByExampleWithBLOBs() {
        if (isModelOnly) {
            return false;
        }

        return tc.isUpdateByExampleStatementEnabled()
                && introspectedTable.hasBLOBColumns();
    }

    @Override
    public IntrospectedTable getIntrospectedTable() {
        return introspectedTable;
    }

    @Override
    public boolean generateBaseColumnList() {
        if (isModelOnly) {
            return false;
        }

        return generateSelectByPrimaryKey()
                || generateSelectByExampleWithoutBLOBs();
    }

    @Override
    public boolean generateBlobColumnList() {
        if (isModelOnly) {
            return false;
        }

        return introspectedTable.hasBLOBColumns()
                && (tc.isSelectByExampleStatementEnabled() || tc
                .isSelectByPrimaryKeyStatementEnabled());
    }

    @Override
    public boolean generateJavaClient() {
        return !isModelOnly && this.generateDao;
    }

    @Override
    public boolean generateRelationWithSubSelected() {
        return tc.getRelationGeneratorConfigurations().stream().anyMatch(RelationGeneratorConfiguration::isSubSelected);
    }

    @Override
    public boolean isIntegrateMybatisPlus() {
        return integrateMybatisPlus;
    }

    /**
     * 是否集成spring security。默认为true
     */
    @Override
    public boolean isIntegrateSpringSecurity() {
        return integrateSpringSecurity;
    }

    @Override
    public boolean isForceGenerateScalableElement(final String element) {
        boolean forceUpdateScalableElement = introspectedTable.getContext().isForceUpdateScalableElement();
        String property = introspectedTable.getContext().getProperty(PropertyRegistry.CONTEXT_FORCE_UPDATE_ELEMENT_LIST);
        if (StringUtility.stringHasValue(property)) {
            if (forceUpdateScalableElement && Arrays.stream(property.split(","))
                    .anyMatch(e -> e.equalsIgnoreCase(element))) {
                return true;
            } else {
                return false;
            }
        } else {
            return forceUpdateScalableElement;
        }
    }


    @Override
    public boolean isGenerateServiceUnitTest() {
        JavaServiceImplGeneratorConfiguration javaServiceImplGeneratorConfiguration = tc.getJavaServiceImplGeneratorConfiguration();
        return javaServiceImplGeneratorConfiguration.isGenerate()
                && javaServiceImplGeneratorConfiguration.isGenerateUnitTest();
    }

    @Override
    public boolean isGenerateControllerUnitTest() {
        JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration = tc.getJavaControllerGeneratorConfiguration();
        return javaControllerGeneratorConfiguration != null
                && javaControllerGeneratorConfiguration.isGenerate()
                && javaControllerGeneratorConfiguration.isGenerateUnitTest()
                && tc.getTableType().equals(TableTypeEnum.DATA_TABLE.code());
    }

    @Override
    public boolean isGenerateVueEnd() {
        return VStringUtil.stringHasValue(introspectedTable.getContext().getVueEndProjectPath());
    }

    @Override
    public boolean isGenerateAnyVO() {
        return isGenerateCreateVO() || isGenerateUpdateVO()
                || isGenerateVoModel() || isGenerateExcelVO()
                || isGenerateRequestVO() || isGenerateViewVO();
    }

    @Override
    public boolean isGenerateVO() {
        return tc.getVoGeneratorConfiguration() != null && tc.getVoGeneratorConfiguration().isGenerate()
                && tc.getTableType().equals(TableTypeEnum.DATA_TABLE.code());
    }

    @Override
    public boolean isGenerateVoModel() {
        return tc.getVoGeneratorConfiguration() != null
                && tc.getVoGeneratorConfiguration().isGenerate()
                && tc.getVoGeneratorConfiguration().getVoModelConfiguration() != null
                && tc.getVoGeneratorConfiguration().getVoModelConfiguration().isGenerate();
    }

    @Override
    public boolean isGenerateCreateVO() {
        return tc.getVoGeneratorConfiguration() != null
                && tc.getVoGeneratorConfiguration().isGenerate()
                && tc.getVoGeneratorConfiguration().getVoCreateConfiguration() != null
                && tc.getVoGeneratorConfiguration().getVoCreateConfiguration().isGenerate();
    }

    @Override
    public boolean isGenerateUpdateVO() {
        return tc.getVoGeneratorConfiguration() != null
                && tc.getVoGeneratorConfiguration().isGenerate()
                && tc.getVoGeneratorConfiguration().getVoUpdateConfiguration() != null
                && tc.getVoGeneratorConfiguration().getVoUpdateConfiguration().isGenerate();
    }

    @Override
    public boolean isGenerateExcelVO() {
        return tc.getVoGeneratorConfiguration() != null
                && tc.getVoGeneratorConfiguration().isGenerate()
                && tc.getVoGeneratorConfiguration().getVoExcelConfiguration() != null
                && tc.getVoGeneratorConfiguration().getVoExcelConfiguration().isGenerate();
    }

    @Override
    public boolean isGenerateRequestVO() {
        return tc.getVoGeneratorConfiguration() != null
                && tc.getVoGeneratorConfiguration().isGenerate()
                && tc.getVoGeneratorConfiguration().getVoRequestConfiguration() != null
                && tc.getVoGeneratorConfiguration().getVoRequestConfiguration().isGenerate();
    }

    @Override
    public boolean isGenerateViewVO() {
        return tc.getVoGeneratorConfiguration() != null
                && tc.getVoGeneratorConfiguration().isGenerate()
                && tc.getVoGeneratorConfiguration().getVoViewConfiguration() != null
                && tc.getVoGeneratorConfiguration().getVoViewConfiguration().isGenerate();
    }

    @Override
    public boolean isGenerateEventListener() {
        return tc.getJavaServiceImplGeneratorConfiguration() != null
                && tc.getJavaServiceImplGeneratorConfiguration().isGenerate()
                && !tc.getJavaServiceImplGeneratorConfiguration().getEntityEvent().isEmpty();
    }

    @Override
    public boolean isGenerateWfEventListener() {
        return GenerateUtils.isWorkflowInstance(introspectedTable);
    }

    @Override
    public boolean isGenerateInnerTable() {
        return isGenerateViewVO() && !tc.getVoGeneratorConfiguration().getVoViewConfiguration().getInnerListViewConfigurations().isEmpty();
    }

    @Override
    public boolean isAdditionInnerList(HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        return htmlGeneratorConfiguration != null &&  !htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration().isEmpty();
    }

    @Override
    public boolean isGenerateApprovalComment(HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        return htmlGeneratorConfiguration != null &&  !htmlGeneratorConfiguration.getHtmlApprovalCommentConfigurations().isEmpty();
    }

    @Override
    public boolean isGenerateCachePO() {
        VOCacheGeneratorConfiguration voCacheGeneratorConfiguration = tc.getVoCacheGeneratorConfiguration();
        return voCacheGeneratorConfiguration != null && voCacheGeneratorConfiguration.isGenerate();
    }

    @Override
    public boolean isModelEnableChildren() {
        if (tc.getJavaModelGeneratorConfiguration().isGenerateChildren()) {
            return tc.getFieldNames().contains(DefaultColumnNameEnum.PARENT_ID.fieldName());
        }
        return false;
    }

    @Override
    public boolean isGenerateCachePOWithMultiKey() {
        VOCacheGeneratorConfiguration config = tc.getVoCacheGeneratorConfiguration();
        return config != null && config.isGenerate() && (config.getTypeColumn() != null || config.getKeyColumn() != null);
    }

    @Override
    public boolean generateSelectByColumn() {
        return !tc.getSelectByColumnGeneratorConfigurations().isEmpty();
    }

    @Override
    public boolean generateSelectByTable() {
        return !tc.getSelectByTableGeneratorConfiguration().isEmpty();
    }

    @Override
    public boolean generateFileUpload() {
        return tc.isFileUploadStatementEnabled();
    }
}
