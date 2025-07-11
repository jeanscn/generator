package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.VueFormItemMeta;
import com.vgosoft.core.annotation.VueFormItemRule;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.IntrospectedColumn;

import java.util.HashSet;
import java.util.Set;

/**
 * ElementPlus表单元数据描述注解
 *
 * @version 6.0
 */
@Getter
@Setter
public class VueFormItemMetaDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@" + VueFormItemMeta.class.getSimpleName();

    private String fieldValue;
    private final String fieldName;
    private String otherFieldName;
    private final String fieldLabel;
    private final String fieldSubLabel;
    private Integer span = 24;
    private String tips;
    private String component;
    private String rules;
    private String hideHandle;
    private String requiredHandle;
    private Boolean defaultHidden = false; // 默认隐藏
    // 以下为options属性
    private String placeholder; // 提示文字
    private String maxlength; // 最大长度
    private String minlength; // 最小长度
    private Boolean multiple = false; // 是否允许多选
    // 类型,input-可能是text、password、url、textarea；date-可能是time、datetime
    private String type;
    private String valueFormat; // 日期格式化
    private boolean dateRange = false; // 是否为日期范围
    private boolean remoteApiParse = false; // 是否解析远程数据源的结果，如果为true，则远程数据源返回的数据需要进行转换label和value。如果指定了keyMapLabel和keyMapValue，则按照keyMap转换，否则按照默认规则转换label-name，value-id
    private boolean remoteToTree = false; // 远程数据源转树形结构
    private String remoteValueType = "string"; // 远程数据源返回的数据中，value对应的字段类型,remoteApiParse为true时有效
    private boolean remoteAsync = false; // 远程数据源是否异步加载
    private boolean excludeSelf = false; // 远程数据源是否排除自身

    private String keyMapLabel = "name"; // 远程数据源返回的数据中，label对应的字段名,remoteApiParse为true时有效
    private String keyMapValue = "id"; // 远程数据源返回的数据中，value对应的字段名,remoteApiParse为true时有效
    private String dataUrl = ""; // 远程数据源的URL
    private String dataUrlParams = ""; // 数据源
    private String dataSource = "";
    private String beanName = "";

    private String applyProperty = "";
    private String applyPropertyKey = "";
    private String enumClassFullName = "";

    private String dictCode = "";
    private String callback = "";

    private String labelCss = "";
    private String elementCss = "";
    private String switchText = "";

    private String listKey = "";
    private String sourceListViewClass = "";
    private String parentFormKey = "";

    private String designIdField = "";
    private String configJsonfield = "";
    private boolean enablePager;

    private String defaultFilterExpr = "";
    private String hideExpression = "";
    private String disabledExpression = "";
    private Set<String> watchFields = new HashSet<>();

    private boolean renderHref = false;
    private String hrefDataKeyField = "";
    private String tsType = "";

    private String columnActions;
    private String toolbarActions;

    private String designRestBasePath = "";

    public static VueFormItemMetaDesc create(IntrospectedColumn introspectedColumn) {
        return new VueFormItemMetaDesc(introspectedColumn);
    }

    public VueFormItemMetaDesc(IntrospectedColumn introspectedColumn) {
        super();
        this.fieldName = introspectedColumn.getJavaProperty();
        items.add(VStringUtil.format("fieldName = \"{0}\"", this.getFieldName()));
        this.fieldLabel = introspectedColumn.getRemarks(true);
        items.add(VStringUtil.format("fieldLabel = \"{0}\"", this.getFieldLabel()));
        this.fieldSubLabel = introspectedColumn.getSubRemarks();
        this.addImports(VueFormItemMeta.class.getCanonicalName());
    }

    @Override
    public String toAnnotation() {
        if (this.span != 24) {
            items.add(VStringUtil.format("span = {0}", this.getSpan()));
        }
        if (VStringUtil.stringHasValue(this.getFieldValue())) {
            items.add(VStringUtil.format("fieldValue = \"{0}\"", this.getFieldValue()));
        }
        if (VStringUtil.stringHasValue(this.getFieldSubLabel())) {
            items.add(VStringUtil.format("fieldSubLabel = \"{0}\"", this.getFieldSubLabel()));
        }
        if(VStringUtil.stringHasValue(this.getOtherFieldName())){
            items.add(VStringUtil.format("otherFieldName = \"{0}\"", this.getOtherFieldName()));
        }
        if (VStringUtil.stringHasValue(this.getTips())) {
            items.add(VStringUtil.format("tips = \"{0}\"", this.getTips()));
        }
        if (VStringUtil.stringHasValue(this.getComponent()) && !"input".equals(this.getComponent())) {
            items.add(VStringUtil.format("component = \"{0}\"", this.getComponent()));
        }
        if (VStringUtil.stringHasValue(this.getRules())) {
            this.addImports(VueFormItemRule.class.getCanonicalName());
            items.add(VStringUtil.format("\n            rules = '{'{0}'}'\n            ", this.getRules()));
        }
        if (VStringUtil.stringHasValue(this.getHideHandle())) {
            items.add(VStringUtil.format("hideHandle = \"{0}\"", this.getHideHandle()));
        }
        if (VStringUtil.stringHasValue(this.getRequiredHandle())) {
            items.add(VStringUtil.format("requiredHandle = \"{0}\"", this.getRequiredHandle()));
        }
        if (this.defaultHidden) {
            items.add(VStringUtil.format("defaultHidden = true"));
        }
        if (VStringUtil.stringHasValue(this.getPlaceholder())) {
            items.add(VStringUtil.format("placeholder = \"{0}\"", this.getPlaceholder()));
        }
        if (VStringUtil.stringHasValue(this.getMaxlength())) {
            items.add(VStringUtil.format("maxlength = \"{0}\"", this.getMaxlength()));
        }
        if (VStringUtil.stringHasValue(this.getMinlength()) && !"0".equals(this.getMinlength())) {
            items.add(VStringUtil.format("minlength = \"{0}\"", this.getMinlength()));
        }
        if (this.getMultiple() != null && this.getMultiple()){
            items.add(VStringUtil.format("multiple = {0}", this.getMultiple().toString().toLowerCase()));
        }
        if (VStringUtil.stringHasValue(this.getType())) {
            items.add(VStringUtil.format("type = \"{0}\"", this.getType()));
        }
        if (VStringUtil.stringHasValue(this.getTsType())) {
            items.add(VStringUtil.format("tsType = \"{0}\"", this.getTsType()));
        }
        if (VStringUtil.stringHasValue(this.getValueFormat())) {
            items.add(VStringUtil.format("valueFormat = \"{0}\"", this.getValueFormat()));
        }
        if (this.isDateRange()) {
            items.add(VStringUtil.format("dateRange = true"));
        }
        if (this.isRemoteApiParse()) {
            items.add(VStringUtil.format("remoteApiParse = true"));
        }
        if (this.isRemoteToTree()) {
            items.add(VStringUtil.format("remoteToTree = true"));
        }
        if (VStringUtil.isNotBlank(this.getRemoteValueType()) && !"".equals(this.getRemoteValueType())){
            items.add(VStringUtil.format("remoteValueType = \"{0}\"", this.getRemoteValueType()));
        }
        if (this.isRemoteAsync()) {
            items.add(VStringUtil.format("remoteAsync = true"));
        }
        if (this.isExcludeSelf()) {
            items.add(VStringUtil.format("excludeSelf = true"));
        }
        if (VStringUtil.isNotBlank(this.getKeyMapLabel()) && !"name".equals(this.getKeyMapLabel())){
            items.add(VStringUtil.format("keyMapLabel = \"{0}\"", this.getKeyMapLabel()));
        }
        if (VStringUtil.isNotBlank(this.getKeyMapValue()) && !"id".equals(this.getKeyMapValue())){
            items.add(VStringUtil.format("keyMapValue = \"{0}\"", this.getKeyMapValue()));
        }
        if (VStringUtil.isNotBlank(this.getDataUrl())) {
            items.add(VStringUtil.format("dataUrl = \"{0}\"", this.getDataUrl()));
        }
        if (VStringUtil.isNotBlank(this.getDataUrlParams())) {
            items.add(VStringUtil.format("dataUrlParams = \"{0}\"", this.getDataUrlParams()));
        }
        if (VStringUtil.isNotBlank(this.getDataSource())) {
            items.add(VStringUtil.format("dataSource = \"{0}\"", this.getDataSource()));
        }
        if (VStringUtil.isNotBlank(this.getBeanName())) {
            items.add(VStringUtil.format("beanName = \"{0}\"", this.getBeanName()));
        }
        if (VStringUtil.isNotBlank(this.getApplyProperty()) && ! "name".equals(this.getApplyProperty())) {
            items.add(VStringUtil.format("applyProperty = \"{0}\"", this.getApplyProperty()));
        }
        if (VStringUtil.isNotBlank(this.getApplyPropertyKey()) && ! "id".equals(this.getApplyPropertyKey())) {
            items.add(VStringUtil.format("applyPropertyKey = \"{0}\"", this.getApplyPropertyKey()));
        }
        if (VStringUtil.isNotBlank(this.getEnumClassFullName())) {
            items.add(VStringUtil.format("enumClassFullName = \"{0}\"", this.getEnumClassFullName()));
        }
        if (VStringUtil.isNotBlank(this.getDictCode())) {
            items.add(VStringUtil.format("dictCode = \"{0}\"", this.getDictCode()));
        }
        if (VStringUtil.isNotBlank(this.getCallback())) {
            items.add(VStringUtil.format("callback = \"{0}\"", this.getCallback()));
        }
        if (VStringUtil.isNotBlank(this.getLabelCss())) {
            items.add(VStringUtil.format("labelCss = \"{0}\"", this.getLabelCss()));
        }
        if (VStringUtil.isNotBlank(this.getElementCss())) {
            items.add(VStringUtil.format("elementCss = \"{0}\"", this.getElementCss()));
        }
        if (VStringUtil.isNotBlank(this.getSwitchText())) {
            items.add(VStringUtil.format("switchText = \"{0}\"", this.getSwitchText()));
        }
        if (VStringUtil.isNotBlank(this.getListKey())) {
            items.add(VStringUtil.format("listKey = \"{0}\"", this.getListKey()));
        }
        if (VStringUtil.isNotBlank(this.getSourceListViewClass())) {
            items.add(VStringUtil.format("sourceListViewClass = \"{0}\"", this.getSourceListViewClass()));
        }
        if (VStringUtil.isNotBlank(this.getParentFormKey())) {
            items.add(VStringUtil.format("parentFormKey = \"{0}\"", this.getParentFormKey()));
        }
        if (VStringUtil.isNotBlank(this.getDesignIdField())) {
            items.add(VStringUtil.format("designIdField = \"{0}\"", this.getDesignIdField()));
        }
        if (VStringUtil.isNotBlank(this.getConfigJsonfield())) {
            items.add(VStringUtil.format("configJsonfield = \"{0}\"", this.getConfigJsonfield()));
        }
        if (VStringUtil.isNotBlank(this.getDesignRestBasePath())) {
            items.add(VStringUtil.format("dataUrl = \"{0}\"", this.getDesignRestBasePath()));
        }
        if (!this.enablePager) {
            items.add(VStringUtil.format("enablePager = false"));
        }
        if (VStringUtil.isNotBlank(this.toolbarActions)) {
            items.add(VStringUtil.format("toolbarActions = \"{0}\"", this.toolbarActions));
        }
        if (VStringUtil.isNotBlank(this.columnActions)) {
            items.add(VStringUtil.format("columnActions = \"{0}\"", this.columnActions));
        }
        if (VStringUtil.isNotBlank(this.defaultFilterExpr)) {
            items.add(VStringUtil.format("defaultFilterExpr = \"{0}\"", this.defaultFilterExpr));
        }
        if (VStringUtil.isNotBlank(this.hideExpression)) {
            items.add(VStringUtil.format("hideExpression = \"{0}\"", this.hideExpression));
        }
        if (VStringUtil.isNotBlank(this.disabledExpression)) {
            items.add(VStringUtil.format("disabledExpression = \"{0}\"", this.disabledExpression));
        }
        if (this.renderHref) {
            items.add(VStringUtil.format("renderHref = true"));
        }
        if (VStringUtil.isNotBlank(this.hrefDataKeyField)) {
            items.add(VStringUtil.format("hrefDataKeyField = \"{0}\"", this.hrefDataKeyField));
        }
        if (!this.watchFields.isEmpty()) {
            items.add(VStringUtil.format("watchFields = \"{0}\"", String.join(",", this.watchFields)));
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }
}
