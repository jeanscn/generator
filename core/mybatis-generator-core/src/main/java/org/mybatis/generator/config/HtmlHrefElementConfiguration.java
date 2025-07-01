package org.mybatis.generator.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class HtmlHrefElementConfiguration {
    private String href;

    private String target = "_blank";

    private String type = "slideLeft";

    private String text;

    private String icon;

    private String title;

    private String method;

    private String keySelector = "#id";

    private String hideExpression;
}
