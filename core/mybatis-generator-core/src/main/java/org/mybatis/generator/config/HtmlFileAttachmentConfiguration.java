package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 附近上传页面组件配置
 *
 */
@Getter
@Setter
public class HtmlFileAttachmentConfiguration extends PropertyHolder{

    private String elementKey;

    private boolean generate = true;

    private boolean exclusive = true;

    private boolean multiple = true;

    private String type = "text";

    private String location = "table";

    private Integer limit = 0;

    private String tips = "";

    private String restBasePath = "/bizcore/vbiz-file-attachment-impl";

    private String label = "附件";

    private String afterColumn;

    private int order =10;

    private String hideExpression;

    private String disableExpression;

}
