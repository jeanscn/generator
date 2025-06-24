package org.mybatis.generator.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * button生成配置类
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class HtmlButtonGeneratorConfiguration extends TypedPropertyHolder{

    private String id;
    private String type = "primary";
    private boolean text;
    private String title;
    private String label;
    private String icon;
    private String elIcon;
    private String classes;
    private String handler;

    private boolean isLink = false;
    private boolean  isRound = false;
    private boolean  isCircle = false;
    private boolean isPlain = false;
    private String css;
    private String showCondition = "true";
    private String disabledCondition = "false";
    private boolean configurable = false;
    private String localeKey;
    private String componentType = "button"; // 组件类型

    public HtmlButtonGeneratorConfiguration(String id) {
       super();
       this.id = id;
    }
}
