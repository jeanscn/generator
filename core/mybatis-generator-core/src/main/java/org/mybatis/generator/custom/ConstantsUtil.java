package org.mybatis.generator.custom;

import com.vgosoft.mybatis.abs.AbstractMybatisBGService;

import java.util.EnumSet;

/**
 * 集中管理常量，如引用的类路径等
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-05-04 00:55
 * @version 3.0
 */
public class ConstantsUtil {

    //实体接口
    public static final String I_SHOW_IN_VIEW =             "com.vgosoft.core.entity.IShowInView";
    public static final String I_PERSISTENCE_BASIC =        "com.vgosoft.core.entity.IPersistenceBasic";
    public static final String I_SORTABLE_ENTITY =          "com.vgosoft.core.entity.ISortableEntity";
    public static final String I_WORK_FLOW_BASE_ENTITY =    "com.vgosoft.core.entity.IWorkflowBaseEntity";
    public static final String I_PERSISTENCE_BLOB =         "com.vgosoft.core.entity.IPersistenceBlob";
    public static final String I_BUSINESS_ENTITY =          "com.vgosoft.core.entity.IBusinessEntity";

    //mapper接口
    public static final String MBG_MAPPER_INTERFACE =       "com.vgosoft.mybatis.inf.MBGMapperInterface";
    public static final String MBG_MAPPER_BLOB_INTERFACE =  "com.vgosoft.mybatis.inf.MBGMapperBlobInterface";

    //service接口父类
    public static final String MBG_SERVICE_INTERFACE =      "com.vgosoft.mybatis.inf.IMybatisBGService";
    public static final String MBG_BLOB_SERVICE_INTERFACE = "com.vgosoft.mybatis.inf.IMybatisBGBlobService";
    public static final String MBG_BLOB_FILE_SERVICE =      "com.vgosoft.mybatis.inf.IMybatisBGBlobFileService";
    public static final String MBG_BLOB_BYTES_SERVICE =     "com.vgosoft.mybatis.inf.IMybatisBGBlobBytesService";
    public static final String MBG_BLOB_STRING_SERVICE =    "com.vgosoft.mybatis.inf.IMybatisBGBlobStringService";


    //service实现抽象父类
    public static final String ABSTRACT_MBG_SERVICE_INTERFACE =         "com.vgosoft.mybatis.abs.AbstractMybatisBGService";
    public static final String ABSTRACT_MBG_BLOB_SERVICE_INTERFACE =    "com.vgosoft.mybatis.abs.AbstractMybatisBGBlobBaseService";
    public static final String ABSTRACT_MBG_BLOB_FILE_SERVICE =         "com.vgosoft.mybatis.abs.AbstractMybatisBGBlobFileService";
    public static final String ABSTRACT_MBG_BLOB_BYTES_SERVICE =        "com.vgosoft.mybatis.abs.AbstractMybatisBGBlobBytesService";
    public static final String ABSTRACT_MBG_BLOB_STRING_SERVICE =       "com.vgosoft.mybatis.abs.AbstractMybatisBGBlobStringService";
    public static final String ABSTRACT_SERVICE_BUSINESS =              "com.vgosoft.mybatis.abs.AbstractMybatisServiceBusiness";
    public static final String ABSTRACT_BLOB_FILE_SERVICE_BUSINESS =    "com.vgosoft.mybatis.abs.AbstractBlobFileServiceBusiness";
    public static final String ABSTRACT_BLOB_BYTES_SERVICE_BUSINESS =   "com.vgosoft.mybatis.abs.AbstractBlobBytesServiceBusiness";
    public static final String ABSTRACT_BLOB_STRING_SERVICE_BUSINESS =  "com.vgosoft.mybatis.abs.AbstractBlobStringServiceBusiness";

    //对象类
    public static final String RESPONSE_SIMPLE =        "com.vgosoft.core.adapter.web.respone.ResponseSimple";
    public static final String RESPONSE_SIMPLE_IMPL =   "com.vgosoft.core.adapter.web.respone.ResponseSimpleImpl";
    public static final String RESPONSE_LIST =          "com.vgosoft.core.adapter.web.respone.ResponseList";
    public static final String RESPONSE_SIMPLE_LIST =   "com.vgosoft.core.adapter.web.respone.ResponseSimpleList";
    public final static String SERVICE_RESULT=          "com.vgosoft.core.adapter.ServiceResult";

   //枚举
    public static final String SERVICE_CODE_ENUM = "com.vgosoft.core.constant.enums.ServiceCodeEnum";

    //annotation
    public static final String ANNOTATION_SYSTEM_LOG =      "com.vgosoft.core.annotation.SystemLog";
    public static final String ANNOTATION_TABLE_META =      "com.vgosoft.core.annotation.TableMeta";
    public static final String ANNOTATION_COLUMN_META =     "com.vgosoft.core.annotation.ColumnMeta";

    //其他
    public static final String ABSTRACT_BASE_CONTROLLER =   "com.vgosoft.web.controller.abs.AbstractBaseController";
    public static final String V_STRING_UTIL =              "com.vgosoft.tool.core.VStringUtil";
    public static final String COM_SEL_SQL_PARAMETER =      "com.vgosoft.core.entity.ComSelSqlParameter";

    //spring
    public static final String ANNOTATION_REPOSITORY =  "org.springframework.stereotype.Repository";
    public static final String ANNOTATION_SERVICE =     "org.springframework.stereotype.Service";
    public static final String ANNOTATION_TRANSACTIONAL =   "org.springframework.transaction.annotation.Transactional";

    //属性名
    public static final String PROP_NAME_REST_BASE_PATH =   "restBasePath";
    public static final String PROP_NAME_VIEW_PATH =        "viewPath";

    //参数名称
    public static final String PARAM_NAME_PERSISTENCE_STATUS = "persistenceStatus";

    //测试类名
    public static final String TEST_ABSTRACT_MYBATIS_BG_SERVICE_TEST = "com.vgosoft.test.abs.service.AbstractMybatisBGServiceTest";

    public static final String TEST_MOCKITO_WHEN = "org.mockito.Mockito.when";
    public static final String TEST_ASSERTIONS_ASSERT_THAT = "org.assertj.core.api.Assertions.assertThat";

    public static String getTestClass(String superClass){
        TestClassMap testClassMap = TestClassMap.ofSuperClass(superClass);
        if (testClassMap != null) {
            return testClassMap.testClass;
        }
        return null;
    }

    enum TestClassMap{

        AbstractMybatisBGService(ABSTRACT_MBG_SERVICE_INTERFACE,TEST_ABSTRACT_MYBATIS_BG_SERVICE_TEST);

        private final String superClass;
        private final String testClass;

        TestClassMap(final String superClass,final String testClass){
            this.superClass = superClass;
            this.testClass = testClass;
        }

        public String getSuperClass(){return superClass;}
        public String getTestClass(){return testClass;}

        public static TestClassMap ofSuperClass(final String superClass){
            return EnumSet.allOf(TestClassMap.class).stream()
                    .filter(e->e.superClass.equals(superClass))
                    .findFirst().orElse(null);
        }
    }
}
