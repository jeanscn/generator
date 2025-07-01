package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.ConstantsUtil;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class HtmlLayoutDescriptor   extends TypedPropertyHolder{

    //指定页面打开方式：pop-小弹窗，inner-页面嵌入，full-全屏弹窗，默认full
    private String loadingFrameType = "pop";

    private int pageColumnsNum = 2;

    private String barPosition = "bottom";

    //页面模板名称 layui、zui。默认layui
    private String uiFrameType = "layui";

    private List<String> exclusiveColumns = new ArrayList<>();

    private int borderWidth;

    private String borderColor;

    private String labelWidth = "120px";

    private String labelPosition = "right";

    private String size = "default";

    private String popSize = "default";

    private boolean popDraggable = true;

    private List<HtmlGroupContainerConfiguration> groupContainerConfigurations = new ArrayList<>();

    public HtmlLayoutDescriptor() {
        this.borderColor = ConstantsUtil.HTML_BORDER_COLOR_DEFAULT;
        this.borderWidth = ConstantsUtil.HTML_BORDER_WIDTH;
    }

}
