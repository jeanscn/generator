package org.mybatis.generator.codegen.mybatis3.freemaker.html.layui;

import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-06-06 16:30
 * @version 4.0
 */
@Setter
@Getter
public class InnerListEditTemplate {

    private String fieldName;

    private String thisFieldName;

    private String otherFieldName;

    private String type;

    private String template;

    private String fieldType;

    private String dataType;

    private String dataUrl;

    private String title;

    private String dateType = "date";

    private String dateFormat = "yyyy-MM-dd";

    private String listKey;

    private String listViewClass;

    private String callback;


}
