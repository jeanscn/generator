package org.mybatis.generator.internal.util;

import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.core.constant.enums.IBaseEnum;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import com.vgosoft.core.constant.enums.view.ViewDefaultToolBarsEnum;
import com.vgosoft.mybatis.generate.GenerateSqlTemplate;
import com.vgosoft.mybatis.sqlbuilder.InsertSqlBuilder;
import com.vgosoft.tool.core.ObjectFactory;
import com.vgosoft.tool.core.VMD5Util;
import com.vgosoft.tool.core.VStringUtil;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.ConstantsUtil;
import org.mybatis.generator.custom.annotations.HtmlButtonDesc;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static com.vgosoft.tool.core.VStringUtil.stringHasValue;
import static com.vgosoft.tool.core.VStringUtil.toHyphenCase;
import static org.mybatis.generator.custom.ConstantsUtil.ANNOTATION_TRANSACTIONAL;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-03 14:53
 * @version 3.0
 */
public class Mb3GenUtil {

    private Mb3GenUtil() {
    }

    public static String getParentMenuId(IntrospectedTable introspectedTable, VoViewGeneratorConfiguration voViewGeneratorConfiguration) {
        String parentMenuId = voViewGeneratorConfiguration.getParentMenuId();
        String contextParentMenuId = introspectedTable.getContext().getParentMenuId();
        return VStringUtil.stringHasValue(parentMenuId)?parentMenuId:VStringUtil.stringHasValue(contextParentMenuId)?contextParentMenuId:null;
    }

    public static String getControllerBaseMappingPath(IntrospectedTable introspectedTable) {
        String beanName = ConfigUtil.getIntrospectedTableBeanName(introspectedTable.getTableConfiguration());
        String basePath = introspectedTable.getTableConfiguration().getServiceApiBasePath();
        if (StringUtility.stringHasValue(basePath)) {
            return basePath + "/" + toHyphenCase(beanName);
        } else {
            return toHyphenCase(beanName);
        }
    }

    public static String getModelCateId(Context context) {
        String aCase = StringUtils.lowerCase(context.getModuleKeyword());
        if (VStringUtil.stringHasValue(aCase)) {
            return VMD5Util.MD5_15(aCase);
        }
        return null;
    }

    public static String getModelKey(IntrospectedTable introspectedTable) {
        return StringUtils.lowerCase(introspectedTable.getContext().getModuleKeyword() + "_" + introspectedTable.getTableConfiguration().getDomainObjectName());
    }

    public static String getModelId(IntrospectedTable introspectedTable) {
        String modelKey = getModelKey(introspectedTable);
        return VMD5Util.MD5_15(modelKey);
    }


    public static String getDefaultViewId(IntrospectedTable introspectedTable) {
        String aCase = StringUtils.lowerCase(introspectedTable.getControllerBeanName() + GlobalConstant.DEFAULT_VIEW_ID_SUFFIX);
        if (VStringUtil.stringHasValue(aCase)) {
            return VMD5Util.MD5_15(aCase);
        }
        return null;
    }

    public static String getDefaultHtmlKey(IntrospectedTable introspectedTable) {
        String controllerBaseMappingPath = Mb3GenUtil.getControllerBaseMappingPath(introspectedTable);
        return controllerBaseMappingPath.replace("/", "-");
    }

    public static boolean isRequiredColumn(IntrospectedColumn introspectedColumn) {
        return !introspectedColumn.isNullable() && introspectedColumn.getDefaultValue() == null && !introspectedColumn.isAutoIncrement() && !introspectedColumn.isGeneratedColumn() && !introspectedColumn.isSequenceColumn();
    }

    public static boolean isInDefaultFields(IntrospectedTable introspectedTable, String fieldName) {
        if (GenerateUtils.isWorkflowInstance(introspectedTable) && ConstantsUtil.DEFAULT_WORKFLOW_FIELDS.contains(fieldName)) {
            return true;
        } else return ConstantsUtil.DEFAULT_CORE_FIELDS.contains(fieldName);
    }

    public static String getInnerListFragmentFileName(@Nullable HtmlElementInnerListConfiguration listConfiguration, IntrospectedTable introspectedTable) {
        if (listConfiguration != null) {
            String listKey = stringHasValue(listConfiguration.getListKey()) ? "_" + listConfiguration.getListKey() : "";
            return listConfiguration.getSourceViewPath() + listKey + "_" + ConstantsUtil.SUFFIX_INNER_LIST_FRAGMENTS + ".html";
        } else {
            return introspectedTable.getTableConfiguration().getTableName() + "_" + ConstantsUtil.SUFFIX_INNER_LIST_FRAGMENTS + ".html";
        }
    }

    public static String getHtmlInnerListFragmentFileName(@Nullable InnerListViewConfiguration innerListViewConfiguration, IntrospectedTable introspectedTable) {
        if (innerListViewConfiguration != null) {
            String listKey = stringHasValue(innerListViewConfiguration.getListKey()) ? "_" + innerListViewConfiguration.getListKey() : "";
            return innerListViewConfiguration.getEditExtendsForm() + listKey + "_" + ConstantsUtil.SUFFIX_INNER_LIST_FRAGMENTS + ".html";
        } else {
            return introspectedTable.getTableConfiguration().getTableName() + "_" + ConstantsUtil.SUFFIX_INNER_LIST_FRAGMENTS + ".html";
        }
    }

    public static String getDateType(@Nullable HtmlElementDescriptor htmlElementDescriptor, IntrospectedColumn introspectedColumn) {
        String dateType = htmlElementDescriptor != null && htmlElementDescriptor.getDataFormat() != null ? htmlElementDescriptor.getDataFormat() : null;
        if (introspectedColumn.getJdbcType() == 93) {
            dateType = "datetime";
        } else if (introspectedColumn.getJdbcType() == 91) {
            dateType = "date";
        } else if (introspectedColumn.getJdbcType() == 92) {
            dateType = "time";
        } else {
            if (StringUtility.stringHasValue(dateType)) {
                //年|年月|日期|日期时间|时间
                switch (dateType) {
                    case "年":
                        dateType = "year";
                        break;
                    case "年月":
                        dateType = "month";
                        break;
                    case "年周":
                        dateType = "week";
                        break;
                    case "日期时间":
                        dateType = "datetime";
                        break;
                    case "时间":
                        dateType = "time";
                        break;
                    case "日期":
                    default:
                        dateType = "date";
                        break;
                }
            } else {
                dateType = "date";
            }
        }
        return dateType;
    }

    public static String getDateFormat(@Nullable HtmlElementDescriptor htmlElementDescriptor, IntrospectedColumn introspectedColumn) {
        if (htmlElementDescriptor != null && StringUtility.stringHasValue(htmlElementDescriptor.getDataFmt())) {
            return htmlElementDescriptor.getDataFmt();
        } else {
            if (introspectedColumn.getJdbcType() == 93) {
                return "yyyy-MM-dd HH:mm:ss";
            } else if (introspectedColumn.getJdbcType() == 91) {
                return "yyyy-MM-dd";
            } else if (introspectedColumn.getJdbcType() == 92) {
                return "HH:mm:ss";
            } else {
                return "yyyy-MM-dd";
            }
        }
    }

    public static String getEnumClassNameByDataFmt(String dataFormat) {
        switch (dataFormat) {
            case "exist":
            case "有":
            case "有无":
                return "com.vgosoft.core.constant.enums.core.ExistOrNotEnum";
            case "yes":
            case "true":
            case "是":
            case "是否":
                return "com.vgosoft.core.constant.enums.core.YesNoEnum";
            case "sex":
            case "性别":
                return "com.vgosoft.core.constant.enums.core.GenderEnum";
            case "启停":
            case "启用停用":
            case "state":
                return "com.vgosoft.core.constant.enums.core.CommonStatusEnum";
            case "急":
            case "缓急":
                return "com.vgosoft.core.constant.enums.core.UrgencyEnum";
            case "level":
            case "级别":
                return "com.vgosoft.core.constant.enums.core.LevelListEnum";
            default:
                return null;
        }
    }

    //获得switch的lay-text属性值
    public static String getSwitchTextByDataFmt(String dataFormat) {
        return getSwitchTextByEnumClassName(getEnumClassNameByDataFmt(dataFormat));
    }

    public static String getSwitchTextByEnumClassName(String enumClassName) {
        if (enumClassName != null) {
            Class<?> aClass = ObjectFactory.internalClassForName(enumClassName);
            if (aClass.isEnum() && IBaseEnum.class.isAssignableFrom(aClass)) {
                Object[] enumConstants = aClass.getEnumConstants();
                if (enumConstants.length > 1) {
                    return ((IBaseEnum<?>) enumConstants[0]).codeName() + "|" + ((IBaseEnum<?>) enumConstants[1]).codeName();
                }
            }
        }
        return null;
    }

    public static Map<String,String> getColumnRenderFunMap(IntrospectedTable introspectedTable) {
        // 列渲染
        Map<String, String> columnRenderFunMap = new HashMap<>();
        if (introspectedTable.getRules().isGenerateViewVo()) {
            VoViewGeneratorConfiguration viewConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
            viewConfiguration.getVoColumnRenderFunGeneratorConfigurations()
                    .forEach(config -> config.getFieldNames().forEach(fieldName -> columnRenderFunMap.putIfAbsent(fieldName, config.getRenderFun())));        }
        return columnRenderFunMap;
    }

    /**
     * 添加事务注解
     *
     * @param parentElement 类
     * @param method        方法
     * @param isolation     事务隔离级别：DEFAULT，READ_UNCOMMITTED，READ_COMMITTED，REPEATABLE_READ,SERIALIZABLE
     */
    public static void addTransactionalAnnotation(TopLevelClass parentElement, Method method, @Nullable String isolation) {
        parentElement.addImportedType(ANNOTATION_TRANSACTIONAL);
        parentElement.addImportedType("java.lang.Exception");
        if (isolation != null && !"DEFAULT".equals(isolation)) {
            parentElement.addImportedType("org.springframework.transaction.annotation.Isolation");
            method.addAnnotation("@Transactional(rollbackFor = Exception.class, isolation = Isolation." + isolation + ")");
        } else {
            method.addAnnotation("@Transactional(rollbackFor = Exception.class)" );
        }

    }

    /**
     * 设置权限数据sql脚本
     * @param introspectedTable IntrospectedTable 内省表对象
     * @param levels 权限数据
     */
    public static void setPermissionSqlData(IntrospectedTable introspectedTable, Map<String, String> levels) {
        int index = 0;
        List<String> keys = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, String> entry : levels.entrySet()) {
            String code = index == 0 ? entry.getKey() : keys.get(index - 1) + ":" + entry.getKey();
            String id = VMD5Util.MD5_15(code);
            keys.add(code);
            String name = index == 0 ? entry.getValue() : names.get(index - 1) + ":" + entry.getValue();
            names.add(name);
            InsertSqlBuilder sqlBuilder = GenerateSqlTemplate.insertSqlForPermission();
            sqlBuilder.updateStringValues("id_", id);
            sqlBuilder.updateValues("sort_", String.valueOf(introspectedTable.getPermissionDataScriptLines().size() + 1));
            if (index > 0) {
                sqlBuilder.updateStringValues("parent_id", VMD5Util.MD5_15(keys.get(index - 1)));
            } else {
                sqlBuilder.updateStringValues("parent_id", "0");
            }
            sqlBuilder.updateStringValues("code_", code);
            sqlBuilder.updateStringValues("name_", name);
            introspectedTable.addPermissionDataScriptLines(id, sqlBuilder.toSql()+";");
            index++;
        }
    }

    /**
     * 通过操作的id集合，生成HtmlButtonDesc注解
     * @param buttonIds 操作id集合
     * @param htmlButtonGeneratorConfigurations HtmlButtonGeneratorConfiguration 按钮生成配置集合
     * @return List<String> 按钮注解字符串的集合
     */
    public static List<String> genHtmlButtonAnnotationDescFromKeys(IntrospectedTable introspectedTable,List<String> buttonIds, Set<HtmlButtonGeneratorConfiguration> htmlButtonGeneratorConfigurations, @Nullable String parentMenuId){
        return buttonIds.stream().filter(VStringUtil::stringHasValue).map(buttonId -> {
           // 先检查配置中是否存在
            HtmlButtonGeneratorConfiguration htmlButtonGeneratorConfiguration = htmlButtonGeneratorConfigurations.stream()
                    .filter(configuration -> buttonId.equals(configuration.getId()))
                    .findFirst().orElse(null);
            if (htmlButtonGeneratorConfiguration != null) {
                HtmlButtonDesc htmlButtonDesc = HtmlButtonDesc.create(htmlButtonGeneratorConfiguration);
                if (htmlButtonDesc.isConfigurable() && VStringUtil.stringHasValue(parentMenuId)) {
                    addActionPermissionSqlData(introspectedTable, htmlButtonDesc,parentMenuId);
                }
                return "\n                                "+htmlButtonDesc.toAnnotation();
            }
            ViewDefaultToolBarsEnum viewDefaultToolBarsEnum = ViewDefaultToolBarsEnum.ofCode(buttonId);
            if (viewDefaultToolBarsEnum != null) {
                HtmlButtonDesc htmlButtonDesc = new HtmlButtonDesc(viewDefaultToolBarsEnum);
                if (StringUtility.stringHasValue(viewDefaultToolBarsEnum.elIcon())) {
                    htmlButtonDesc.setIcon(viewDefaultToolBarsEnum.elIcon());
                }else{
                    htmlButtonDesc.setIcon(viewDefaultToolBarsEnum.icon());
                }
                if (htmlButtonDesc.isConfigurable() && VStringUtil.stringHasValue(parentMenuId)) {
                    addActionPermissionSqlData(introspectedTable, htmlButtonDesc,parentMenuId);
                }
                return "\n                                "+htmlButtonDesc.toAnnotation();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());

    }

    /**
     * 添加操作权限数据
     * @param introspectedTable IntrospectedTable 内省表对象
     * @param htmlButtonDesc HtmlButtonDesc 按钮描述对象
     * @param parentMenuId 父级菜单ID
     */
    private static void addActionPermissionSqlData(IntrospectedTable introspectedTable, HtmlButtonDesc htmlButtonDesc, String parentMenuId) {
        String l2 = introspectedTable.getControllerBeanName().toLowerCase();
        String l3 = htmlButtonDesc.getId().toLowerCase();
        Map<String, String> mapAction = new LinkedHashMap<>();
        mapAction.put(l2, introspectedTable.getRemarks(true));
        mapAction.put(l3, htmlButtonDesc.getTitle()!=null?htmlButtonDesc.getTitle():htmlButtonDesc.getLabel());
        setPermissionActionSqlData(introspectedTable, mapAction,parentMenuId);
    }

    /**
     * 设置权限操作数据sql脚本
     * @param introspectedTable IntrospectedTable 内省表对象
     * @param levels 权限数据
     * @param parentMenuId 父级菜单ID
     */
    private static void setPermissionActionSqlData(IntrospectedTable introspectedTable, Map<String, String> levels, String parentMenuId) {
        int index = 0;
        List<String> keys = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, String> entry : levels.entrySet()) {
            String code = index == 0 ? entry.getKey() : keys.get(index - 1) + ":" + entry.getKey();
            String id = VMD5Util.MD5_15(code);
            keys.add(code);
            String name = index == 0 ? entry.getValue() : names.get(index - 1) + ":" + entry.getValue();
            names.add(name);
            InsertSqlBuilder sqlBuilder = GenerateSqlTemplate.insertSqlForPermissionAction();
            sqlBuilder.updateStringValues("id_", id);
            sqlBuilder.updateValues("sort_", String.valueOf(introspectedTable.getPermissionActionDataScriptLines().size() + 1));
            sqlBuilder.updateStringValues("code_", code);
            sqlBuilder.updateStringValues("name_", name);
            if (index > 0) {
                sqlBuilder.updateStringValues("parent_id", VMD5Util.MD5_15(keys.get(index - 1)));
                introspectedTable.addPermissionActionDataScriptLines(id, sqlBuilder.toSql()+";");
            } else {
                sqlBuilder.updateStringValues("parent_id", parentMenuId);
                introspectedTable.addPermissionActionDataScriptLines(id, sqlBuilder.toSql()+";");
            }
            index++;
        }
    }

    public static VoAdditionalPropertyGeneratorConfiguration generateAdditionalPropertyFromDefaultColumnNameEnum(IntrospectedTable introspectedTable,
                                                                                                           DefaultColumnNameEnum defaultColumnNameEnum,
                                                                                                           String initializationString,
                                                                                                           List<String> annotations) {
        VoAdditionalPropertyGeneratorConfiguration leafAdditionalPropertyConfiguration = new VoAdditionalPropertyGeneratorConfiguration(introspectedTable.getContext(), introspectedTable.getTableConfiguration());
        leafAdditionalPropertyConfiguration.setName(defaultColumnNameEnum.fieldName());
        leafAdditionalPropertyConfiguration.setRemark(defaultColumnNameEnum.comment());
        if (initializationString != null) {
            leafAdditionalPropertyConfiguration.setInitializationString(initializationString);
        }
        leafAdditionalPropertyConfiguration.setType(FullyQualifiedJavaType.getBooleanPrimitiveInstance().getFullyQualifiedName());
        if (annotations != null) {
            leafAdditionalPropertyConfiguration.setAnnotations(annotations);
        }
        return leafAdditionalPropertyConfiguration;
    }

    public static void injectionMappingsInstance(TopLevelClass parentElement, FullyQualifiedJavaType mappingsInterface) {
        if (mappingsInterface!=null) {
            String fieldName = mappingsInterface.getShortNameFirstLowCase() + "Impl";
            if (parentElement.isNotContainField(fieldName)) {
                Field mappings = new Field(fieldName, mappingsInterface);
                mappings.setVisibility(JavaVisibility.PROTECTED);
                mappings.addAnnotation("@Resource");
                parentElement.addImportedType(mappingsInterface);
                parentElement.addImportedType("javax.annotation.Resource");
                mappings.addJavaDocLine("/**");
                mappings.addJavaDocLine(" * 注入" + mappingsInterface.getShortName() + " 实例");
                mappings.addJavaDocLine(" */");
                parentElement.addField(mappings);
            }
        }
    }
}
