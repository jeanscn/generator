package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.enums.HtmlDocumentTypeEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class HtmlGeneratorConfiguration extends AbstractGeneratorConfiguration {

    private HtmlDocumentTypeEnum type;

    private String viewPath;

    private String title;

    private String simpleViewPath;

    private String htmlFileName;

    private boolean overWriteHtmlFile;

    private boolean overWriteCssFile;

    private boolean overWriteJsFile;

    private boolean defaultConfig = true;

    private boolean overWriteVueView = true;

    private boolean overWriteVueEdit = true;

    private boolean overWriteVueDetail = true;

    private final Set<String> hiddenColumnNames = new HashSet<>();

    private final Set<String> hiddenFieldNames = new HashSet<>();

    private final Set<String> readonlyFields = new HashSet<>();

    private final Set<String> displayOnlyFields = new HashSet<>();

    private final Set<String> hiddenFields = new HashSet<>();

    //指定页面表单不允许为空的字段
    private final Set<String> elementRequired = new HashSet<>();

    private final List<HtmlElementDescriptor> elementDescriptors = new ArrayList<>();

    private HtmlLayoutDescriptor layoutDescriptor;

    private List<HtmlElementInnerListConfiguration> htmlElementInnerListConfiguration = new ArrayList<>();

    private String htmlBaseTargetPackage;

    private List<HtmlFileAttachmentConfiguration> htmlFileAttachmentConfiguration = new ArrayList<>();

    private final List<HtmlApprovalCommentConfiguration> htmlApprovalCommentConfigurations = new ArrayList<>();

    private final Set<HtmlButtonGeneratorConfiguration> htmlButtons = new HashSet<>();

    public HtmlGeneratorConfiguration(Context context, TableConfiguration tc) {
        super();
        overWriteHtmlFile = false;
        overWriteCssFile = false;
        overWriteJsFile = false;
        targetProject = "src/main/resources/templates";
        this.context = context;
        type = HtmlDocumentTypeEnum.EDITABLE;
    }

    @Override
    protected void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "HtmlMapGeneratorConfiguration");
    }

    public List<HtmlElementDescriptor> addElementDescriptors(HtmlElementDescriptor htmlElementDescriptor) {
        this.elementDescriptors.add(htmlElementDescriptor);
        return this.elementDescriptors;
    }

    public void addHtmlApprovalCommentConfiguration(HtmlApprovalCommentConfiguration htmlApprovalCommentConfiguration) {
        this.htmlApprovalCommentConfigurations.add(htmlApprovalCommentConfiguration);
    }

}
