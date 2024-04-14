package org.mybatis.generator.config;

/**
 * 附近上传页面组件配置
 *
 */
public class HtmlFileAttachmentConfiguration extends PropertyHolder{

    private String value = "";

    private boolean generate = true;

    private boolean exclusive = true;

    private boolean multiple = true;

    private String type = "text";

    private String location = "table";

    private Integer limit = 0;

    private String tip = "";

    private String restBasePath = "/bizcore/vbiz-file-attachment-impl";

    private String label = "附件";

    private String afterColumn;


    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    public String getAfterColumn() {
        return afterColumn;
    }

    public void setAfterColumn(String afterColumn) {
        this.afterColumn = afterColumn;
    }

    public boolean isGenerate() {
        return generate;
    }

    public void setGenerate(boolean generate) {
        this.generate = generate;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRestBasePath() {
        return restBasePath;
    }

    public void setRestBasePath(String restBasePath) {
        this.restBasePath = restBasePath;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
