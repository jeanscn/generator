package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 页面导航器的生成配置。
 *
 * @author vgosoft-agent
 */
@Getter
@Setter
public class PageNavigatorConfiguration {

    private List<String> columns = new ArrayList<>();

    private String title = "";

    private String trigger = "auto";

    private String placement = "right";

    private int offset;

    private int width = 150;

    private int railWidth = 36;

    private int tickGap = 14;

    private int maxHeight = 640;

    private int edge = 8;

    private String theme = "auto";

    private boolean showTitle = true;

    private String scrollContainer;

    private int zIndex = 120;
}
