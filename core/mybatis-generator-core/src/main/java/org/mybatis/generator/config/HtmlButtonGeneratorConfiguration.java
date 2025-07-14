package org.mybatis.generator.config;

import lombok.*;

import java.util.Objects;

/**
 * button生成配置类
 *
 * @author cen_c
 */
@Getter
@Setter
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
    /**
     * 组件类型
     */
    private String componentType = "button";

    public HtmlButtonGeneratorConfiguration(String id) {
       super();
       this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) { return false; }
        HtmlButtonGeneratorConfiguration that = (HtmlButtonGeneratorConfiguration) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
