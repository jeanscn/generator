package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-07-06 09:58
 * @version 4.0
 */
@Getter
@Setter
public class HtmlApprovalCommentConfiguration extends PropertyHolder{

    private boolean generate;

    private String elementKey;

    private String afterColumn;

    private int rows = 3;

    private String label = "审批意见";

    private String placeholder = "请输入审批意见";

    private String locationTag = "审批意见";

    private int span = 24;

    private String tips = "";

}
