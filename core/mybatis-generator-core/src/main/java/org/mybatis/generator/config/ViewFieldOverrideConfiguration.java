package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-09-20 12:30
 * @version 4.0
 */
@Setter
@Getter
public class ViewFieldOverrideConfiguration extends TypedPropertyHolder {

    private List<String> fields = new ArrayList<>();
    private String label;
    private String width;

    private String minWidth;

    private String align = "left";

    private String fixed;
    private String headerAlign = "left";

    /**
     * 是否允许排序
      */
    private boolean sort = true;

    /**
     * 是否隐藏
     */
    private boolean hide = false;

    /**
     * 是否可编辑
     */
    private boolean edit = false;

    public ViewFieldOverrideConfiguration() {
        super();
    }
}
