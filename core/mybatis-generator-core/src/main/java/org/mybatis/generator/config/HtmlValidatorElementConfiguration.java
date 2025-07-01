package org.mybatis.generator.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HtmlValidatorElementConfiguration   extends TypedPropertyHolder{
    private String type;

    private Boolean required;

    private String message;

    private String trigger;

    private String min;

    private String max;

    private Integer len;

    private String pattern;

    private Boolean whitespace;

    private String enumList;

    private String transform;

    private String validator;

    private String column;

    private int scope = 0;
}
