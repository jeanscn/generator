package org.mybatis.generator.codegen.mybatis3;

import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.tool.core.VArrayUtil;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.HtmlApprovalCommentConfiguration;
import org.mybatis.generator.config.HtmlElementInnerListConfiguration;
import org.mybatis.generator.config.HtmlFileAttachmentConfiguration;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.HtmlGroupContainerConfiguration;
import org.mybatis.generator.config.HtmlLayoutDescriptor;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.VoModelGeneratorConfiguration;
import org.mybatis.generator.custom.ConstantsUtil;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * 核验 generateHtml 表单布局中的字段名与元素引用，名称不一致时拒绝生成。
 * 容器 includeElements 引用了不存在的元素时，运行时该容器元素列表被过滤为空，
 * 前端会回退渲染全部表单项导致字段重复显示。
 * 校验集合与运行时 FormVO.updateContainersFormItems 的匹配语义保持一致：
 * 物理列名（生成非隐藏表单项的列）、上传/内置列表/审批意见组件的元素键、直接子容器 elementKey。
 *
 * @author vgosoft-agent
 */
public final class VueFormLayoutValidator {
    private VueFormLayoutValidator() { }

    /** 在表内省和配置合并完成后验证布局引用。 */
    public static void validate(AbstractIntrospectedTable table) {
        if (!table.getRules().isGenerateVoModel()) {
            return;
        }
        HtmlGeneratorConfiguration config = emittedConfig(table);
        if (config == null) {
            return;
        }
        Set<String> formItemColumnIds = formItemColumnIds(table, config);
        Set<String> elementIds = new LinkedHashSet<>(formItemColumnIds);
        elementIds.addAll(uploadElementIds(config));
        elementIds.addAll(innerListElementIds(config, table));
        elementIds.addAll(approvalElementIds(config, table));

        List<String> errors = new ArrayList<>();
        HtmlLayoutDescriptor layout = config.getLayoutDescriptor();
        if (layout != null && !layout.getGroupContainerConfigurations().isEmpty()) {
            validateContainers(layout.getGroupContainerConfigurations(), elementIds, errors);
            validateExclusiveColumns(layout, formItemColumnIds, errors);
        }
        validateSpecialElementAfterColumns(config, elementIds, errors);
        validateElementRequired(config, table, errors);
        validateTableFieldLists(table, config, errors);
        if (!errors.isEmpty()) {
            throw failure(table, errors);
        }
    }

    /** VueFormMeta 仅由默认（或首个）页面配置输出，与其保持同一选取逻辑。 */
    private static HtmlGeneratorConfiguration emittedConfig(AbstractIntrospectedTable table) {
        List<HtmlGeneratorConfiguration> configs = table.getTableConfiguration().getHtmlMapGeneratorConfigurations();
        if (configs.isEmpty()) {
            return null;
        }
        return configs.stream().filter(HtmlGeneratorConfiguration::isDefaultConfig)
                .findFirst().orElse(configs.get(0));
    }

    /** 生成非隐藏表单项的列名集合，与 VueHtmMetaAnnotationPlugin 的过滤逻辑一致。 */
    private static Set<String> formItemColumnIds(AbstractIntrospectedTable table, HtmlGeneratorConfiguration config) {
        Set<String> ids = new LinkedHashSet<>();
        VoModelGeneratorConfiguration voModel = table.getTableConfiguration()
                .getVoGeneratorConfiguration().getVoModelConfiguration();
        for (IntrospectedColumn column : table.getNonBlobColumns()) {
            if (isIgnoredVoColumn(column, voModel)
                    && !column.isPrimaryKey()
                    && !"version_".equalsIgnoreCase(column.getActualColumnName())) {
                continue;
            }
            if (!GenerateUtils.isHiddenColumn(table, column, config)) {
                ids.add(column.getActualColumnName());
            }
        }
        return ids;
    }

    private static boolean isIgnoredVoColumn(IntrospectedColumn column, VoModelGeneratorConfiguration voModel) {
        String ignoreColumns = voModel.getProperty(PropertyRegistry.ELEMENT_IGNORE_COLUMNS);
        boolean configured = stringHasValue(ignoreColumns)
                && VArrayUtil.contains(ignoreColumns.split(","), column.getActualColumnName());
        return configured
                || EntityAbstractParentEnum.ABSTRACT_PERSISTENCE_LOCK_ENTITY.fields().contains(column.getJavaProperty());
    }

    /** 上传组件表单项 id：elementKey 优先，缺省回退 label（同 FormUtil）。 */
    private static Set<String> uploadElementIds(HtmlGeneratorConfiguration config) {
        Set<String> ids = new LinkedHashSet<>();
        for (HtmlFileAttachmentConfiguration attachment : config.getHtmlFileAttachmentConfiguration()) {
            String id = stringHasValue(attachment.getElementKey()) ? attachment.getElementKey() : attachment.getLabel();
            if (stringHasValue(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    /** 内置列表表单项 id：elementKey 优先，缺省按 moduleKeyword_默认页面键_序号 派生（同 VueFormInnerListMetaDesc）。 */
    private static Set<String> innerListElementIds(HtmlGeneratorConfiguration config, AbstractIntrospectedTable table) {
        Set<String> ids = new LinkedHashSet<>();
        if (!table.getRules().isAdditionInnerList(config)) {
            return ids;
        }
        List<HtmlElementInnerListConfiguration> innerLists = config.getHtmlElementInnerListConfiguration();
        for (int i = 0; i < innerLists.size(); i++) {
            HtmlElementInnerListConfiguration innerList = innerLists.get(i);
            ids.add(stringHasValue(innerList.getElementKey()) ? innerList.getElementKey()
                    : innerList.getModuleKeyword() + "_" + Mb3GenUtil.getDefaultHtmlKey(table) + "_" + (i + 1));
        }
        return ids;
    }

    private static Set<String> approvalElementIds(HtmlGeneratorConfiguration config, AbstractIntrospectedTable table) {
        Set<String> ids = new LinkedHashSet<>();
        if (!table.getRules().isGenerateApprovalComment(config)) {
            return ids;
        }
        for (HtmlApprovalCommentConfiguration approval : config.getHtmlApprovalCommentConfigurations()) {
            if (stringHasValue(approval.getElementKey())) {
                ids.add(approval.getElementKey());
            }
        }
        return ids;
    }

    /** 按运行时 DFS 先占先得语义校验 includeElements：存在性、重复声明、容器 afterColumn 定位。 */
    private static void validateContainers(List<HtmlGroupContainerConfiguration> roots,
            Set<String> elementIds, List<String> errors) {
        Set<String> elementKeys = new LinkedHashSet<>();
        collectElementKeys(roots, elementKeys, errors);
        elementKeys.stream().filter(elementIds::contains)
                .forEach(key -> errors.add("groupContainer elementKey 与列/组件标识重复：" + key));
        Set<String> earlierRootKeys = new LinkedHashSet<>();
        Map<String, String> claimedBy = new HashMap<>();
        for (HtmlGroupContainerConfiguration root : roots) {
            validateContainerAfterColumn(root, Set.of(), earlierRootKeys, "顶层容器", errors);
            validateContainer(root, elementIds, claimedBy, errors);
            if (stringHasValue(root.getElementKey())) {
                earlierRootKeys.add(root.getElementKey());
            }
        }
    }

    private static void collectElementKeys(List<HtmlGroupContainerConfiguration> containers,
            Set<String> elementKeys, List<String> errors) {
        for (HtmlGroupContainerConfiguration container : containers) {
            if (!stringHasValue(container.getElementKey())) {
                errors.add("groupContainer 缺少必填的 elementKey（type=" + container.getType() + "）");
            } else if (!elementKeys.add(container.getElementKey())) {
                errors.add("groupContainer elementKey 重复：" + container.getElementKey());
            }
            collectElementKeys(container.getGroupContainerConfigurations(), elementKeys, errors);
        }
    }

    private static void validateContainer(HtmlGroupContainerConfiguration container,
            Set<String> elementIds, Map<String, String> claimedBy, List<String> errors) {
        Set<String> childKeys = container.getGroupContainerConfigurations().stream()
                .map(HtmlGroupContainerConfiguration::getElementKey)
                .filter(VStringUtil::stringHasValue)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> ownElements = new LinkedHashSet<>();
        for (String element : container.getIncludeElements()) {
            if (!stringHasValue(element)) {
                continue;
            }
            if (!elementIds.contains(element) && !childKeys.contains(element)) {
                errors.add("groupContainer[" + container.getElementKey() + "] includeElements 引用了不存在的元素：" + element);
            } else if (claimedBy.putIfAbsent(element, container.getElementKey()) != null) {
                errors.add("groupContainer[" + container.getElementKey() + "] includeElements 元素 " + element
                        + " 已在 groupContainer[" + claimedBy.get(element) + "] 中声明，重复指定将被忽略");
            }
            if (elementIds.contains(element)) {
                ownElements.add(element);
            }
        }
        Set<String> earlierChildKeys = new LinkedHashSet<>();
        for (HtmlGroupContainerConfiguration child : container.getGroupContainerConfigurations()) {
            validateContainerAfterColumn(child, ownElements, earlierChildKeys,
                    "groupContainer[" + container.getElementKey() + "] 的子容器", errors);
            validateContainer(child, elementIds, claimedBy, errors);
            if (stringHasValue(child.getElementKey())) {
                earlierChildKeys.add(child.getElementKey());
            }
        }
    }

    /** 容器 afterColumn：顶层容器仅可引用 "first" 或先行同级容器 elementKey；子容器还可引用父容器已包含的元素。 */
    private static void validateContainerAfterColumn(HtmlGroupContainerConfiguration container,
            Set<String> availableElements, Set<String> earlierSiblingKeys, String scope, List<String> errors) {
        String afterColumn = container.getAfterColumn();
        if (!stringHasValue(afterColumn) || "first".equalsIgnoreCase(afterColumn)) {
            return;
        }
        if (!availableElements.contains(afterColumn) && !earlierSiblingKeys.contains(afterColumn)) {
            errors.add(scope + " groupContainer[" + container.getElementKey() + "] afterColumn 引用了不可定位的元素："
                    + afterColumn + "（可用：\"first\"、已包含的表单项或先行同级容器 elementKey，缺省/无效将排列到末尾）");
        }
    }

    /** 上传/内置列表/审批意见组件的 afterColumn 锚定页面元素：生成表单项的列名或已配置组件的元素键。 */
    private static void validateSpecialElementAfterColumns(HtmlGeneratorConfiguration config,
            Set<String> anchorIds, List<String> errors) {
        for (HtmlFileAttachmentConfiguration attachment : config.getHtmlFileAttachmentConfiguration()) {
            checkAnchorAfterColumn("htmlFileAttachment", attachment.getElementKey(), attachment.getAfterColumn(),
                    anchorIds, errors);
        }
        for (HtmlElementInnerListConfiguration innerList : config.getHtmlElementInnerListConfiguration()) {
            checkAnchorAfterColumn("htmlElementInnerList", innerList.getElementKey(), innerList.getAfterColumn(),
                    anchorIds, errors);
        }
        for (HtmlApprovalCommentConfiguration approval : config.getHtmlApprovalCommentConfigurations()) {
            checkAnchorAfterColumn("approvalComment", approval.getElementKey(), approval.getAfterColumn(),
                    anchorIds, errors);
        }
    }

    private static void checkAnchorAfterColumn(String element, String elementKey, String afterColumn,
            Set<String> anchorIds, List<String> errors) {
        if (stringHasValue(afterColumn) && !anchorIds.contains(afterColumn)) {
            errors.add(element + "[" + elementKey + "] afterColumn 引用了不存在的列/元素：" + afterColumn);
        }
    }

    private static void validateExclusiveColumns(HtmlLayoutDescriptor layout,
            Set<String> formItemColumnIds, List<String> errors) {
        for (String column : layout.getExclusiveColumns()) {
            if (stringHasValue(column) && !formItemColumnIds.contains(column)) {
                errors.add("layout.exclusiveColumns 引用了未生成表单项的列名：" + column);
            }
        }
    }

    /** requiredColumns 等注入的必填列名必须为表的非 Blob 列。 */
    private static void validateElementRequired(HtmlGeneratorConfiguration config,
            AbstractIntrospectedTable table, List<String> errors) {
        Set<String> columnNames = table.getNonBlobColumns().stream()
                .map(IntrospectedColumn::getActualColumnName)
                .collect(Collectors.toSet());
        for (String column : config.getElementRequired()) {
            if (!columnNames.contains(column)) {
                errors.add("elementRequired 引用了不存在的列名：" + column);
            }
        }
    }

    /**
     * 隐藏/只读/只显字段名校验 table 与 generateHtml 级 property；context 级跨表生效无法逐表判定。
     * 合法名集合除本表列名/属性名外，含全部继承抽象父类的列名与字段名及默认字段集，
     * 配置模板跨表复用时对标准公共字段的引用视为有效。
     */
    private static void validateTableFieldLists(AbstractIntrospectedTable table,
            HtmlGeneratorConfiguration config, List<String> errors) {
        Set<String> names = table.getAllColumns().stream()
                .flatMap(column -> java.util.stream.Stream.of(column.getActualColumnName(), column.getJavaProperty()))
                .collect(Collectors.toSet());
        for (EntityAbstractParentEnum parent : EntityAbstractParentEnum.values()) {
            names.addAll(parent.columnNames());
            names.addAll(parent.fields());
        }
        names.addAll(ConstantsUtil.DEFAULT_CORE_FIELDS);
        names.addAll(ConstantsUtil.DEFAULT_WORKFLOW_FIELDS);
        checkFieldList(table.getTableConfiguration().getProperty(PropertyRegistry.ANY_HTML_HIDDEN_FIELDS),
                "table", PropertyRegistry.ANY_HTML_HIDDEN_FIELDS, names, errors);
        checkFieldList(table.getTableConfiguration().getProperty(PropertyRegistry.ANY_HTML_READONLY_FIELDS),
                "table", PropertyRegistry.ANY_HTML_READONLY_FIELDS, names, errors);
        checkFieldList(table.getTableConfiguration().getProperty(PropertyRegistry.ANY_HTML_DISPLAY_ONLY_FIELDS),
                "table", PropertyRegistry.ANY_HTML_DISPLAY_ONLY_FIELDS, names, errors);
        checkFieldList(config.getProperties().getProperty(PropertyRegistry.ANY_HTML_HIDDEN_FIELDS),
                "generateHtml", PropertyRegistry.ANY_HTML_HIDDEN_FIELDS, names, errors);
        checkFieldList(config.getProperties().getProperty(PropertyRegistry.ANY_HTML_READONLY_FIELDS),
                "generateHtml", PropertyRegistry.ANY_HTML_READONLY_FIELDS, names, errors);
        checkFieldList(config.getProperties().getProperty(PropertyRegistry.ANY_HTML_DISPLAY_ONLY_FIELDS),
                "generateHtml", PropertyRegistry.ANY_HTML_DISPLAY_ONLY_FIELDS, names, errors);
    }

    private static void checkFieldList(String property, String scope, String propertyName,
            Set<String> names, List<String> errors) {
        if (!stringHasValue(property)) {
            return;
        }
        for (String field : VStringUtil.splitToSet(property)) {
            if (!names.contains(field)) {
                errors.add(scope + " property " + propertyName + " 引用了不存在的字段：" + field);
            }
        }
    }

    private static IllegalArgumentException failure(AbstractIntrospectedTable table, List<String> errors) {
        return new IllegalArgumentException("表 " + table.getTableConfiguration().getTableName()
                + " generateHtml 表单布局配置校验失败（引用名称与实际列/元素不一致，不允许生成）："
                + System.lineSeparator() + String.join(System.lineSeparator(), errors));
    }
}
