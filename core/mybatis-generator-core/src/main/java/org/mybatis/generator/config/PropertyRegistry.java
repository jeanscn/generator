package org.mybatis.generator.config;

/**
 * This class holds constants for all properties recognized by the different
 * configuration elements. This helps document and maintain the different
 * properties, and helps to avoid spelling errors.
 *
 * @author Jeff Butler
 *
 */
public class PropertyRegistry {

    private PropertyRegistry() {}

    public static final String ANY_ENABLE_SUB_PACKAGES = "enableSubPackages"; //$NON-NLS-1$

    /**
     * recognized by table and java model generator.
     */
    public static final String ROOT_CLASS = "rootClass";
    public static final String  ROOT_CLASS_TYPE_ARGUMENTS = "rootClassTypeArguments";
    public static final String  ROOT_SUPER_INTERFACE = "superInterface";
    public static final String ANY_IMMUTABLE = "immutable";
    public static final String ANY_CONSTRUCTOR_BASED = "constructorBased";

    /**
     * recognized by table and java client generator.
     */
    public static final String ANY_ROOT_INTERFACE = "rootInterface"; //$NON-NLS-1$

    public static final String TABLE_USE_COLUMN_INDEXES = "useColumnIndexes"; //$NON-NLS-1$
    public static final String TABLE_USE_ACTUAL_COLUMN_NAMES = "useActualColumnNames"; //$NON-NLS-1$
    public static final String TABLE_USE_COMPOUND_PROPERTY_NAMES = "useCompoundPropertyNames"; //$NON-NLS-1$
    public static final String TABLE_IGNORE_QUALIFIERS_AT_RUNTIME = "ignoreQualifiersAtRuntime"; //$NON-NLS-1$
    public static final String TABLE_RUNTIME_CATALOG = "runtimeCatalog"; //$NON-NLS-1$
    public static final String TABLE_RUNTIME_SCHEMA = "runtimeSchema"; //$NON-NLS-1$
    public static final String TABLE_IGNORE = "ignore";
    public static final String TABLE_RUNTIME_TABLE_NAME = "runtimeTableName"; //$NON-NLS-1$
    public static final String TABLE_MODEL_ONLY = "modelOnly"; //$NON-NLS-1$
    public static final String TABLE_SELECT_ALL_ORDER_BY_CLAUSE = "selectAllOrderByClause"; //$NON-NLS-1$
    public static final String TABLE_DYNAMIC_SQL_SUPPORT_CLASS_NAME = "dynamicSqlSupportClassName"; //$NON-NLS-1$
    public static final String TABLE_DYNAMIC_SQL_TABLE_OBJECT_NAME = "dynamicSqlTableObjectName"; //$NON-NLS-1$
    public static final String CONTEXT_BEGINNING_DELIMITER = "beginningDelimiter"; //$NON-NLS-1$
    public static final String CONTEXT_ENDING_DELIMITER = "endingDelimiter"; //$NON-NLS-1$
    public static final String CONTEXT_AUTO_DELIMIT_KEYWORDS = "autoDelimitKeywords"; //$NON-NLS-1$
    public static final String CONTEXT_JAVA_FILE_ENCODING = "javaFileEncoding"; //$NON-NLS-1$
    public static final String CONTEXT_JAVA_FORMATTER = "javaFormatter"; //$NON-NLS-1$
    public static final String CONTEXT_XML_FORMATTER = "xmlFormatter"; //$NON-NLS-1$
    public static final String CONTEXT_HTML_FORMATTER = "htmlFormatter"; //$NON-NLS-1$
    public static final String CONTEXT_TARGET_JAVA8 = "targetJava8"; //$NON-NLS-1$
    public static final String CONTEXT_KOTLIN_FORMATTER = "kotlinFormatter"; //$NON-NLS-1$
    public static final String CONTEXT_KOTLIN_FILE_ENCODING = "kotlinFileEncoding"; //$NON-NLS-1$
    public static final String CLIENT_DYNAMIC_SQL_SUPPORT_PACKAGE = "dynamicSqlSupportPackage"; //$NON-NLS-1$
    public static final String CLIENT_USE_LEGACY_BUILDER = "useLegacyBuilder"; //$NON-NLS-1$
    public static final String TYPE_RESOLVER_FORCE_BIG_DECIMALS = "forceBigDecimals"; //$NON-NLS-1$
    public static final String TYPE_RESOLVER_USE_JSR310_TYPES = "useJSR310Types"; //$NON-NLS-1$
    public static final String MODEL_GENERATOR_TRIM_STRINGS = "trimStrings"; //$NON-NLS-1$
    public static final String MODEL_GENERATOR_EXAMPLE_PACKAGE = "exampleTargetPackage"; //$NON-NLS-1$
    public static final String MODEL_GENERATOR_EXAMPLE_PROJECT = "exampleTargetProject"; //$NON-NLS-1$
    public static final String COMMENT_GENERATOR_SUPPRESS_DATE = "suppressDate"; //$NON-NLS-1$
    public static final String COMMENT_GENERATOR_SUPPRESS_ALL_COMMENTS = "suppressAllComments"; //$NON-NLS-1$
    public static final String COMMENT_GENERATOR_ADD_REMARK_COMMENTS = "addRemarkComments"; //$NON-NLS-1$
    public static final String COMMENT_GENERATOR_DATE_FORMAT = "dateFormat"; //$NON-NLS-1$
    public static final String COMMENT_GENERATOR_USE_LEGACY_GENERATED_ANNOTATION = "useLegacyGeneratedAnnotation"; //$NON-NLS-1$
    public static final String COLUMN_OVERRIDE_FORCE_JAVA_TYPE = "forceJavaTypeIntoMapping"; //$NON-NLS-1$

    //定制属性
    public static final String CONTEXT_HTML_TARGET_PROJECT = "htmlTargetProject";
    public static final String CONTEXT_HTML_TARGET_PACKAGE = "htmlTargetPackage";
    public static final String CONTEXT_HTML_UI_FRAME = "htmlUiFrame";
    public static final String CONTEXT_HTML_BAR_POSITION = "htmlBarPosition";
    public static final String CONTEXT_HTML_PAGE_COLUMNS_NUM = "htmlPageColumnsNum";
    public static final String CONTEXT_HTML_LOADING_FRAME_TYPE = "htmlLoadingFrameType";

    public static final String ELEMENT_HTML_BUTTON = "htmlButton";
    public static final String CONTEXT_ROOT_MODULE_NAME = "rootModuleName";
    public static final String CONTEXT_SPRING_BOOT_APPLICATION_CLASS = "springBootApplicationClass";

    public static final String CONTEXT_APPLICATION_KEYWORD = "appKeyword";
    public static final String CONTEXT_MODULE_KEYWORD = "moduleKeyword";
    public static final String CONTEXT_MODULE_NAME = "moduleName";
    public static final String CONTEXT_INTEGRATE_MYBATIS_PLUS = "integrateMybatisPlus";
    public static final String CONTEXT_INTEGRATE_SPRING_SECURITY = "integrateSpringSecurity";
    public static final String CONTEXT_FORCE_UPDATE_SCALABLE_ELEMENT = "forceUpdateScalableElement";
    public static final String CONTEXT_FORCE_UPDATE_ELEMENT_LIST = "forceUpdateElementList";

    public static final String ANY_GENERATE = "generate";
    public static final String ANY_TARGET_PROJECT = "targetProject";
    public static final String ANY_TARGET_PACKAGE = "targetPackage";
    public static final String ANY_TARGET_SUB_PACKAGE = "targetSubPackage";
    public static final String ANY_HTML_HIDDEN_COLUMNS = "htmlHiddenColumns";
    public static final String ANY_HTML_READONLY_FIELDS = "htmlReadonlyFields";
    public static final String ANY_HTML_DISPLAY_ONLY_FIELDS = "htmlDisplayOnlyFields";
    public static final String ANY_EQUALS_AND_HASH_CODE = "equalsAndHashCodeColumns";
    public static final String CONTROLLER_ENABLE_SYSLOG_ANNOTATION = "enableSysLogAnnotation";
    public static final String ANY_NO_SWAGGER_ANNOTATION = "noSwaggerAnnotation";
    public static final String SERVICE_NO_SERVICE_ANNOTATION = "noServiceAnnotation";
    public static final String ANY_NO_META_ANNOTATION = "noMetaAnnotation";
    public static final String TABLE_VIEW_PATH = "viewPath";
    public static final String TABLE_HTML_FIE_SUFFIX = "html";
    public static final String TABLE_OVERRIDE_HTML_FILE = "overWriteHtmlFile";
    public static final String TABLE_OVERRIDE_JS_FILE = "overWriteJsFile";
    public static final String TABLE_OVERRIDE_CSS_FILE = "overWriteCssFile";
    public static final String TABLE_JAVA_MODEL_BYTE_STREAM_OUTPUT_MODE = "javaModelByteStreamOutputMode";
    public static final String TABLE_ENABLE_UPDATE_BATCH = "enableUpdateBatch";
    public static final String TABLE_ENABLE_INSERT_BATCH = "enableInsertBatch";
    public static final String TABLE_ENABLE_INSERT_OR_UPDATE = "enableInsertOrUpdate";
    public static final String TABLE_ENABLE_FILE_UPLOAD = "enableFileUpLoad";
    public static final String ELEMENT_HTML_ELEMENT_DESCRIPTOR = "htmlElementDescriptor";
    public static final String ELEMENT_ENABLE_CACHE = "enableCache";

    public static final String ELEMENT_HTML_ELEMENT_INNER_LIST = "htmlElementInnerList";

    public static final String ELEMENT_HTML_LAYOUT = "layout";

    public static final String ELEMENT_HTML_FILE_ATTACHMENT = "htmlFileAttachment";

    public static final String ELEMENT_APPROVAL_COMMENT = "approvalComment";
    public static final String ELEMENT_IGNORE_COLUMNS = "ignoreColumns";
    public static final String ELEMENT_EXCLUDE_COLUMNS = "excludeColumns";
    public static final String ELEMENT_INCLUDE_COLUMNS = "includeColumns";

    public static final String ELEMENT_IMPORT_INCLUDE_COLUMNS = "importIncludeColumns";

    public static final String ELEMENT_IMPORT_EXCLUDE_COLUMNS = "importExcludeColumns";

    public static final String ELEMENT_IGNORE_FIELDS = "ignoreFields";
    public static final String ELEMENT_IMPORT_IGNORE_FIELDS = "importIgnoreFields";
    public static final String ELEMENT_REQUIRED_COLUMNS = "requiredColumns";
    public static final String ELEMENT_TYPE_COLUMN = "typeColumn";
    public static final String ELEMENT_KEY_COLUMN = "keyColumn";
    public static final String ELEMENT_VALUE_COLUMN = "valueColumn";
    public static final String ELEMENT_VALIDATE_IGNORE_COLUMNS = "validateIgnoreColumns";

    public static final String ELEMENT_SOURCE_COLUMN = "sourceColumn";
    public static final String ELEMENT_TARGET_COLUMN = "targetColumn";
    public static final String ELEMENT_TARGET_PROPERTY = "targetProperty";
    public static final String ELEMENT_TARGET_PROPERTY_TYPE = "targetPropertyType";
    public static final String ELEMENT_ANNOTATION_TYPE = "annotationType";
    public static final String ELEMENT_ANNOTATION_BEAN_NAME = "beanName";
    public static final String ELEMENT_APPLY_PROPERTY_VALUE = "applyProperty";
    public static final String ELEMENT_TYPE_VALUE = "typeValue";
    public static final String ELEMENT_FIELD_REMARK = "remark";

    public static final String ELEMENT_ENABLE_SELECTIVE = "enableSelective";

    public static final String ELEMENT_ID_PROPERTY = "idProperty";
    public static final String ELEMENT_NAME_PROPERTY = "nameProperty";

    public static final String ELEMENT_SPEL_EXPRESSION = "SPeLExpression";

    public static final String ELEMENT_PATH_KEYWORD = "pathKeyWord";

    public static final String ELEMENT_ENUM_CLASS_FULL_NAME = "enumClassFullName";

    public static final String ELEMENT_SWITCH_TEXT = "switchText";

    public static final String ELEMENT_DICT_CODE = "dictCode";

    public static final String ELEMENT_FIELD_NAMES = "fieldNames";

    public static final String ELEMENT_RENDER_FUN = "renderFun";

    public static final String ELEMENT_EXTEND_FUNC_OTHER = "extendFuncOther";

    public static final String ELEMENT_INITIALIZATION_STRING = "initializationString";

    public static final String ELEMENT_IMPORT_TYPE = "importType";



}
