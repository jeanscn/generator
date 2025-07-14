package org.mybatis.generator.internal.rules;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;

/**
 * This interface centralizes all the rules related to code generation -
 * including the methods and objects to create, and certain attributes related
 * to those objects.
 *
 * @author Jeff Butler
 */
public interface Rules {

    /**
     * Implements the rule for generating the insert SQL Map element and DAO
     * method. If the insert statement is allowed, then generate the element and
     * method.
     *
     * @return true if the element and method should be generated
     */
    boolean generateInsert();

    /**
     * Implements the rule for generating the insert selective SQL Map element
     * and DAO method. If the insert statement is allowed, then generate the
     * element and method.
     *
     * @return true if the element and method should be generated
     */
    boolean generateInsertSelective();

    /**
     * Calculates the class that contains all fields. This class is used as the
     * insert statement parameter, as well as the returned value from the select
     * by primary key method. The actual class depends on how the domain model
     * is generated.
     *
     * @return the type of the class that holds all fields
     */
    FullyQualifiedJavaType calculateAllFieldsClass();

    /**
     * Implements the rule for generating the update by primary key without
     * BLOBs SQL Map element and DAO method. If the table has a primary key as
     * well as other non-BLOB fields, and the updateByPrimaryKey statement is
     * allowed, then generate the element and method.
     *
     * @return true if the element and method should be generated
     */
    boolean generateUpdateByPrimaryKeyWithoutBLOBs();

    /**
     * Implements the rule for generating the update by primary key with BLOBs
     * SQL Map element and DAO method. If the table has a primary key as well as
     * other BLOB fields, and the updateByPrimaryKey statement is allowed, then
     * generate the element and method.
     *
     * @return true if the element and method should be generated
     */
    boolean generateUpdateByPrimaryKeyWithBLOBs();

    /**
     * Implements the rule for generating the update by primary key selective
     * SQL Map element and DAO method. If the table has a primary key as well as
     * other fields, and the updateByPrimaryKey statement is allowed, then
     * generate the element and method.
     *
     * @return true if the element and method should be generated
     */
    boolean generateUpdateByPrimaryKeySelective();

    /**
     * Implements the rule for generating the delete by primary key SQL Map
     * element and DAO method. If the table has a primary key, and the
     * deleteByPrimaryKey statement is allowed, then generate the element and
     * method.
     *
     * @return true if the element and method should be generated
     */
    boolean generateDeleteByPrimaryKey();

    /**
     * Implements the rule for generating the delete by example SQL Map element
     * and DAO method. If the deleteByExample statement is allowed, then
     * generate the element and method.
     *
     * @return true if the element and method should be generated
     */
    boolean generateDeleteByExample();

    /**
     * Implements the rule for generating the result map without BLOBs. If
     * either select method is allowed, then generate the result map.
     *
     * @return true if the result map should be generated
     */
    boolean generateBaseResultMap();

    /**
     * Implements the rule for generating the result map with BLOBs. If the
     * table has BLOB columns, and either select method is allowed, then
     * generate the result map.
     *
     * @return true if the result map should be generated
     */
    boolean generateResultMapWithBLOBs();

    /**
     * Implements the rule for generating the SQL example where clause element.
     *
     * <p>In MyBatis3, generate the element if the selectByExample,
     * deleteByExample, or countByExample statements are allowed.
     *
     * @return true if the SQL where clause element should be generated
     */
    boolean generateSQLExampleWhereClause();

    /**
     * Implements the rule for generating the SQL example where clause element
     * specifically for use in the update by example methods.
     *
     * <p>In MyBatis, generate the element if the updateByExample statements are
     * allowed.
     *
     * @return true if the SQL where clause element should be generated
     */
    boolean generateMyBatis3UpdateByExampleWhereClause();

    /**
     * Implements the rule for generating the SQL base column list element.
     * Generate the element if any of the select methods are enabled.
     *
     * @return true if the SQL base column list element should be generated
     */
    boolean generateBaseColumnList();

    /**
     * Implements the rule for generating the SQL blob column list element.
     * Generate the element if any of the select methods are enabled, and the
     * table contains BLOB columns.
     *
     * @return true if the SQL blob column list element should be generated
     */
    boolean generateBlobColumnList();

    /**
     * Implements the rule for generating the select by primary key SQL Map
     * element and DAO method. If the table has a primary key as well as other
     * fields, and the selectByPrimaryKey statement is allowed, then generate
     * the element and method.
     *
     * @return true if the element and method should be generated
     */
    boolean generateSelectByPrimaryKey();

    /**
     * Implements the rule for generating the select by example without BLOBs
     * SQL Map element and DAO method. If the selectByExample statement is
     * allowed, then generate the element and method.
     *
     * @return true if the element and method should be generated
     */
    boolean generateSelectByExampleWithoutBLOBs();

    /**
     * Implements the rule for generating the select by example with BLOBs SQL
     * Map element and DAO method. If the table has BLOB fields and the
     * selectByExample statement is allowed, then generate the element and
     * method.
     *
     * @return true if the element and method should be generated
     */
    boolean generateSelectByExampleWithBLOBs();

    /**
     * Implements the rule for generating an example class. The class should be
     * generated if the selectByExample or deleteByExample or countByExample
     * methods are allowed.
     *
     * @return true if the example class should be generated
     */
    boolean generateExampleClass();

    /**
     * Implements the rule for generating a count by example SQL Map element and
     * DAO method. If the countByExample statement is allowed, then generate the
     * element and method.
     *
     * @return true if the element and method should be generated
     */
    boolean generateCountByExample();

    /**
     * Implements the rule for generating an update by example selective SQL Map
     * element and DAO method. If the updateByExampleSelective statement is
     * allowed, then generate the element and method.
     *
     * @return true if the element and method should be generated
     */
    boolean generateUpdateByExampleSelective();

    /**
     * Implements the rule for generating an update by example without BLOBs
     * SQL Map element and DAO method. If the updateByExampleWithoutBLOBs
     * statement is allowed, then generate the element and method.
     *
     * @return true if the element and method should be generated
     */
    boolean generateUpdateByExampleWithoutBLOBs();

    /**
     * Implements the rule for generating an update by example with BLOBs SQL
     * Map element and DAO method. If the table has BLOB columns and the
     * updateByExampleWithBLOBs statement is allowed, then generate the element
     * and method.
     *
     * @return true if the element and method should be generated
     */
    boolean generateUpdateByExampleWithBLOBs();

    /**
     * Implements the rule for determining whether to generate a primary key
     * class. If you return false from this method, and the table has primary
     * key columns, then the primary key columns will be added to the base
     * class.
     *
     * @return true if a separate primary key class should be generated
     */
    boolean generatePrimaryKeyClass();

    /**
     * Implements the rule for generating a base record.
     *
     * @return true if the class should be generated
     */
    boolean generateBaseRecordClass();

    /**
     * Implements the rule for generating a record with BLOBs. If you return
     * false from this method, and the table had BLOB columns, then the BLOB
     * columns will be added to the base class.
     *
     * @return true if the record with BLOBs class should be generated
     */
    boolean generateRecordWithBLOBsClass();

    /**
     * Implements the rule for generating a Java client.  This rule is
     * only active when a javaClientGenerator configuration has been
     * specified, but the table is designated as "modelOnly".  Do not
     * generate the client if the table is designated as modelOnly.
     *
     * @return true if the Java client should be generated
     */
    boolean generateJavaClient();

    /**
     * Returns the introspected table for which these rules apply.
     * @return the introspected table
     */
    IntrospectedTable getIntrospectedTable();

    /**
     * 是否生成Mapper接口
     *  @return true if the Mapper interface should be generated
     */
     boolean generateRelationWithSubSelected();

    /**
     * 是否集成MybatisPlus
     * @return true if MybatisPlus is integrated
     * */
    boolean isIntegrateMybatisPlus();
    /**
     * 是否集成spring security。默认为true
     * @return true if Spring Security is integrated
     * */
    boolean isIntegrateSpringSecurity();

    /**
     * 是否集成spring security。默认为true
     * @return true if Spring Security is integrated
     */
    boolean isGenerateServiceUnitTest();

    /**
     * 是否生成Controller单元测试
     * @return true if the Controller unit test should be generated
     */
    boolean isGenerateControllerUnitTest();

    /**
     * 是否生成Service单元测试
     * @param element the element to check
     * @return true if the Service unit test should be generated
     */
    boolean isForceGenerateScalableElement(String element);

    /**
     * 是否需要生成前端的元素
     * @return true if the element should be force generated
     */
    boolean isGenerateVueEnd();

    /**
     * 是否生成前端的页面
     * @return true if the Vue end should be generated
     */
    boolean isGenerateEditHtml();

    /**
     * 是否生成前端的打印页面
     * @return true if the HTML should be generated
     */
    boolean isGeneratePrintHtml();

    /**
     * 是否生成前端的查看页面
     * @return true if the view HTML should be generated
     */
    boolean isGenerateViewOnlyHtml();

    /**
     * 是生成Vo对象
     * @return true if the Vo object should be generated
     */
    boolean isGenerateAnyVo();

    /**
     * 是否生成Vo对象
     * @return true if the Vo model should be generated
     */
    boolean isGenerateVoModel();

    /**
     * 是否生成CreateVo对象
     * @return true if the CreateVo object should be generated
     */
    boolean isGenerateCreateVo();

    /**
     * 是否生成UpdateVo对象
     * @return true if the UpdateVo object should be generated
     */
    boolean isGenerateUpdateVo();

    /**
     * 是否生成ViewVo对象
     * @return true if the ViewVo object should be generated
     */
    boolean isGenerateViewVo();

    /**
     * 是否生成ListVo对象
     * @return true if the ListVo object should be generated
     */
    boolean isGenerateRecycleBin();

    /**
     * 是否生成显示隐藏功能
     * @return true if the RecycleBin Vo object should be generated
     */
    boolean isGenerateHideListBin();

    /**
     * 是否生成EventListener
     * @return true if the EventListener should be generated
     */
    boolean isGenerateEventListener();

    /**
     * 是否生成工作流内部实体声明周期事件监听器
     * @return true if the workflow event listener should be generated
     */
    boolean isGenerateWfEventListener();

    /**
     * 是否生成内部（子表）列表
     * @return true if the inner table should be generated
     */
    boolean isGenerateInnerTable();

    /**
     * 是否生成AdditionInnerList
     * @param htmlGeneratorConfiguration the HTML generator configuration
     * @return true if the inner list should be generated
     */
    boolean isAdditionInnerList(HtmlGeneratorConfiguration htmlGeneratorConfiguration);

    /**
     * 是否生成审批意见组件
     * @param htmlGeneratorConfiguration the HTML generator configuration
     * @return true if the approval comment should be generated
     */
    boolean isGenerateApprovalComment(HtmlGeneratorConfiguration htmlGeneratorConfiguration);

    /**
     * 是否生成Excel导出Vo
     * @return true if the Excel Vo should be generated
     */
    boolean isGenerateExcelVo();

    /**
     * 是否生成RequestVo
     * @return true if the Request Vo should be generated
     */
    boolean isGenerateRequestVo();

    /**
     * 是否生成Vo
     * @return true if the Vo should be generated
     */
    boolean isGenerateVo();

    /**
     * 是否生成缓存实体对象及缓存相关的代码
     * @return true if the Vo should be generated
     */
    boolean isGenerateCachePo();

    /**
     * 是否允许实体内部子数据关联的生成
     * @return true if the cache PO should be generated
     */
    boolean isModelEnableChildren();

    /**
     * 是否生成多键缓存PO
     * @return true if the multi-key cache PO should be generated
     */
    boolean isGenerateCachePoWithMultiKey();

    /**
     * 是否生成InsertBatch
     * @return true if the method should be generated
     */
    boolean generateInsertBatch();

    /**
     * 是否生成UpdateBatch
     * @return true if the method should be generated
     */
    boolean generateUpdateBatch();

    /**
     * 是否生成InsertOrUpdate
     * @return true if the method should be generated
     */
    boolean generateInsertOrUpdate();

    /**
     * 是否生创建新对象时，使用Selective选择性插入
     * @return true if the method should be generated
     */
    boolean createEnableSelective();

    /**
     * 是否更新时，使用Selective选择性更新
     * @return true if the method should be generated
     */
    boolean updateEnableSelective();

    /**
     * 是否生成基于列的查询方法及相关
     * @return true if the method should be generated
     */
    boolean generateSelectByColumn();

    /**
     * 是否生成基于表的查询方法及相关
     * @return true if the method should be generated
     */
    boolean generateSelectByTable();

    /**
     * 是否生成文件上传及相关
     * @return true if the method should be generated
     */
    boolean generateFileUpload();
}
