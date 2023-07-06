package org.mybatis.generator.config;

/**
 * 附近上传页面组件配置
 *
 */
public class HtmlFileAttachmentConfiguration extends PropertyHolder{

    private boolean generate = true;

    private boolean exclusive = true;

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
}
