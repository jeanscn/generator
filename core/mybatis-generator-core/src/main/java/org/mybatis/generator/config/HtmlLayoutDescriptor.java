package org.mybatis.generator.config;

import org.mybatis.generator.custom.ConstantsUtil;

import java.util.ArrayList;
import java.util.List;

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

    public String getLoadingFrameType() {
        return loadingFrameType;
    }

    public void setLoadingFrameType(String loadingFrameType) {
        this.loadingFrameType = loadingFrameType;
    }

    public int getPageColumnsNum() {
        return pageColumnsNum;
    }

    public void setPageColumnsNum(int pageColumnsNum) {
        this.pageColumnsNum = pageColumnsNum;
    }

    public String getBarPosition() {
        return barPosition;
    }

    public void setBarPosition(String barPosition) {
        this.barPosition = barPosition;
    }

    public String getUiFrameType() {
        return uiFrameType;
    }

    public void setUiFrameType(String uiFrameType) {
        this.uiFrameType = uiFrameType;
    }

    public List<String> getExclusiveColumns() {
        return exclusiveColumns;
    }

    public void setExclusiveColumns(List<String> exclusiveColumns) {
        this.exclusiveColumns = exclusiveColumns;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    public String getLabelWidth() {
        return labelWidth;
    }

    public void setLabelWidth(String labelWidth) {
        this.labelWidth = labelWidth;
    }

    public String getLabelPosition() {
        return labelPosition;
    }

    public void setLabelPosition(String labelPosition) {
        this.labelPosition = labelPosition;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPopSize() {
        return popSize;
    }

    public void setPopSize(String popSize) {
        this.popSize = popSize;
    }

    public List<HtmlGroupContainerConfiguration> getGroupContainerConfigurations() {
        return groupContainerConfigurations;
    }

    public void setGroupContainerConfigurations(List<HtmlGroupContainerConfiguration> groupContainerConfigurations) {
        this.groupContainerConfigurations = groupContainerConfigurations;
    }

    public boolean isPopDraggable() {
        return popDraggable;
    }

    public void setPopDraggable(boolean popDraggable) {
        this.popDraggable = popDraggable;
    }
}
