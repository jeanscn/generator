package org.mybatis.generator.custom;

import org.mybatis.generator.custom.enums.TestClassMapEnum;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 集中管理常量，如引用的类路径等
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-05-04 00:55
 * @version 3.0
 */
public class ConstantsUtil {

    public static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public static final String GENERATED_FLAG = "@VCF.generated";
    //实体接口
    public static final String I_BASE_DTO = "com.vgosoft.core.pojo.IBaseDTO";
    public static final String I_SHOW_IN_VIEW = "com.vgosoft.core.entity.IShowInView";
    public static final String I_SIMPLE_KVP = "com.vgosoft.core.pojo.ISimpleKVP";
    public static final String I_PERSISTENCE_BASIC = "com.vgosoft.core.entity.IPersistenceBasic";
    public static final String I_SORTABLE_ENTITY = "com.vgosoft.core.entity.ISortableEntity";
    public static final String I_WORK_FLOW_BASE_ENTITY = "com.vgosoft.core.entity.IWorkflowBaseEntity";
    public static final String I_PERSISTENCE_BLOB = "com.vgosoft.core.entity.upload.IPersistenceBlob";
    public static final String I_BUSINESS_ENTITY = "com.vgosoft.core.entity.IBusinessEntity";

    //mapper接口
    public static final String MBG_MAPPER_INTERFACE = "com.vgosoft.mybatis.inf.MBGMapperInterface";
    public static final String MBG_MAPPER_BLOB_INTERFACE = "com.vgosoft.mybatis.inf.MBGMapperBlobInterface";

    //service接口父类
    public static final String MBG_SERVICE_INTERFACE = "com.vgosoft.mybatis.inf.IMybatisBGService";
    public static final String MBG_BLOB_SERVICE_INTERFACE = "com.vgosoft.mybatis.inf.IMybatisBGBlobService";
    public static final String MBG_BLOB_FILE_SERVICE = "com.vgosoft.mybatis.inf.IMybatisBGBlobFileService";
    public static final String MBG_BLOB_BYTES_SERVICE = "com.vgosoft.mybatis.inf.IMybatisBGBlobBytesService";
    public static final String MBG_BLOB_STRING_SERVICE = "com.vgosoft.mybatis.inf.IMybatisBGBlobStringService";


    //service实现抽象父类
    public static final String ABSTRACT_MBG_SERVICE_INTERFACE = "com.vgosoft.mybatis.abs.AbstractMybatisBGService";
    public static final String ABSTRACT_MBG_BLOB_SERVICE_INTERFACE = "com.vgosoft.mybatis.abs.AbstractMybatisBGBlobBaseService";
    public static final String ABSTRACT_MBG_BLOB_FILE_SERVICE = "com.vgosoft.mybatis.abs.AbstractMybatisBGBlobFileService";
    public static final String ABSTRACT_MBG_BLOB_BYTES_SERVICE = "com.vgosoft.mybatis.abs.AbstractMybatisBGBlobBytesService";
    public static final String ABSTRACT_MBG_BLOB_STRING_SERVICE = "com.vgosoft.mybatis.abs.AbstractMybatisBGBlobStringService";
    public static final String ABSTRACT_SERVICE_BUSINESS = "com.vgosoft.mybatis.abs.AbstractMybatisServiceBusiness";
    public static final String ABSTRACT_BLOB_FILE_SERVICE_BUSINESS = "com.vgosoft.mybatis.abs.AbstractBlobFileServiceBusiness";
    public static final String ABSTRACT_BLOB_BYTES_SERVICE_BUSINESS = "com.vgosoft.mybatis.abs.AbstractBlobBytesServiceBusiness";
    public static final String ABSTRACT_BLOB_STRING_SERVICE_BUSINESS = "com.vgosoft.mybatis.abs.AbstractBlobStringServiceBusiness";

    //对象类
    public static final String RESPONSE_SIMPLE = "com.vgosoft.core.adapter.web.respone.ResponseSimple";
    public static final String DATATABLES_CONFIG = "com.vgosoft.web.plugins.datatables.DataTablesConfig";
    public final static String SERVICE_RESULT = "com.vgosoft.core.adapter.ServiceResult";
    public final static String RESPONSE_RESULT = "com.vgosoft.core.adapter.web.respone.ResponseResult";
    public final static String RESPONSE_PAGEHELPER_RESULT = "com.vgosoft.mybatis.pojo.ResponsePagehelperResult";
    public static final String MODEL_AND_VIEW = "org.springframework.web.servlet.ModelAndView";
    public static final String VIEW_DT_TABLE = "com.vgosoft.web.entity.ViewDtTable";

    public static final String VIEW_DT_TABLE_VO = "com.vgosoft.web.pojo.vo.ViewDtTableVO";
    public static final String MULTIPART_FILE = "org.springframework.web.multipart.MultipartFile";
    public static final String SPRING_CONTEXT_HOLDER = "com.vgosoft.tool.core.SpringContextHolder";

    public static final String Z_TREE_DATA_SIMPLE = "com.vgosoft.core.pojo.ztree.ZtreeDataSimple";
    public static final String Z_TREE_DATA_SIMPLE_CATE = "com.vgosoft.core.pojo.ztree.ZtreeDataViewCate";

    //工具类
    public static final String DATATABLES_UTIL = "com.vgosoft.web.plugins.datatables.util.DataTablesUtil";

    //枚举
    public static final String SERVICE_CODE_ENUM = "com.vgosoft.core.constant.enums.core.ServiceCodeEnum";
    public static final String API_CODE_ENUM = "com.vgosoft.core.constant.enums.core.ApiCodeEnum";
    public static final String ACTION_CATE_ENUM = "com.vgosoft.core.constant.enums.core.ActionCateEnum";
    public static final String LOG_TYPES_ENUM = "com.vgosoft.core.constant.enums.log.LogTypesEnum";
    public static final String LOG_TARGET_TABLE_ENUM = "com.vgosoft.core.constant.enums.log.LogTargetTableEnum";

    //annotation
    //public static final String ANNOTATION_SYSTEM_LOG = "com.vgosoft.core.annotation.SystemLog";
    //public static final String ANNOTATION_TABLE_META = "com.vgosoft.core.annotation.TableMeta";
    //public static final String ANNOTATION_COLUMN_META = "com.vgosoft.core.annotation.ColumnMeta";
    public static final String ANNOTATION_REQUEST_PARAM_SPLIT = "com.vgosoft.web.resolver.annotation.RequestParamSplit";

    //其他
    public static final String ABSTRACT_BASE_CONTROLLER = "com.vgosoft.web.controller.abs.AbstractBaseController";
    public static final String V_STRING_UTIL = "com.vgosoft.tool.core.VStringUtil";
    public static final String V_DATE_UTILS = "com.vgosoft.tool.core.VDateUtils";
    public static final String COM_SEL_SQL_PARAMETER = "com.vgosoft.core.entity.ComSelSqlParameter";

    //spring
    public static final String ANNOTATION_REPOSITORY = "org.springframework.stereotype.Repository";
    public static final String ANNOTATION_SERVICE = "org.springframework.stereotype.Service";
    public static final String ANNOTATION_TRANSACTIONAL = "org.springframework.transaction.annotation.Transactional";

    //属性名
    public static final String PROP_NAME_REST_BASE_PATH = "restBasePath";
    public static final String PROP_NAME_VIEW_PATH = "viewPath";

    //参数名称
    public static final String PARAM_NAME_PERSISTENCE_STATUS = "persistenceStatus";

    //测试类名
    public static final String TEST_ABSTRACT_MYBATIS_BG_SERVICE_TEST = "com.vgosoft.test.abs.service.AbstractMybatisBGServiceTest";

    public static final String TEST_MOCKITO_WHEN = "org.mockito.Mockito.when";
    public static final String TEST_ASSERTIONS_ASSERT_THAT = "org.assertj.core.api.Assertions.assertThat";

    //easyExcel
    //public static final String EXCEL_PROPERTY = "com.alibaba.excel.annotation.ExcelProperty";

    public static final Map<String,String> childrenGenericClasses = new HashMap<>();

    public static final String HTML_BORDER_COLOR_DEFAULT = "#eee";
    public static final int HTML_BORDER_WIDTH = 1;

    static{
        childrenGenericClasses.put("IDepartment","com.vgosoft.core.adapter.organization.entity.IDepartment");
        childrenGenericClasses.put("IGroup","com.vgosoft.core.adapter.organization.entity.IGroup");
        childrenGenericClasses.put("IOrganization","com.vgosoft.core.adapter.organization.entity.IOrganization");
        childrenGenericClasses.put("IRole","com.vgosoft.core.adapter.organization.entity.IRole");
        childrenGenericClasses.put("IUser","com.vgosoft.core.adapter.organization.entity.IUser");
    }

    public static String getTestClass(String superClass) {
       return Objects.requireNonNull(TestClassMapEnum.ofSuperClass(superClass).orElse(null)).getTestClass();
    }

    public static final String MAPPINGS_CACHE_PO_KEY = "CachePo";

    public static final String MAPSTRUCT_MAPPER = "org.mapstruct.Mapper";
    public static final String MAPSTRUCT_FACTORY_MAPPERS = "org.mapstruct.factory.Mappers";
    public static final String MAPSTRUCT_REPORTING_POLICY = "org.mapstruct.ReportingPolicy";

    public static final String ANNOTATION_NULLABLE = "javax.annotation.Nullable";

    public static final String SUFFIX_INNER_LIST_FRAGMENTS = "inner_list_fragments";

    public static final List<String> DEFAULT_CORE_FIELDS = Arrays.asList("id", "version", "numberRule", "restBasePath", "persistenceBeanName"
            , "fileCategoryId", "moduleTag", "defaultTitle", "currentUserOrgId", "currentOrgFullName", "currentOrgShortName"
            , "currentUserId", "currentAccountName", "currentRealName", "currentUserId", "currentMainDeptId", "commentId"
            , "signDeptId", "refreshPortlet");
    public static final List<String> DEFAULT_WORKFLOW_FIELDS = Arrays.asList("id", "version", "numberRule", "restBasePath", "persistenceBeanName"
            , "fileCategoryId", "moduleTag", "defaultTitle", "currentUserOrgId", "currentOrgFullName", "currentOrgShortName"
            , "currentUserId", "currentAccountName", "currentRealName", "currentUserId", "currentMainDeptId", "commentId"
            , "signDeptId", "refreshPortlet", "supperEditor", "moduleId","fileCategory","viewStatus","wfState"
            ,"curProcessors","parentBusinessKey","branchCount","branchCompleteCount","sectionHidden","rootProcessInstanceId"
            ,"processInstanceId","isSubflow","subFlowElement","subFlowProcessInstanceId","taskDefinitionKey","taskId"
            ,"taskName","nodeExecNumber","actionList","sectionReadonly","sectionKeepEditable","sectionShow","assigneeType"
            ,"assigmentType","assignee","enableEdit","addtionalSave","enableTransfer","enableTackback","enableSendback"
            ,"allowCancel","lastAssignee","lastTaskId");
}
