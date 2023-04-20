package org.mybatis.generator.config;

import org.mybatis.generator.custom.ConstantsUtil;

import java.util.ArrayList;
import java.util.List;

public class HtmlLayoutDescriptor {

    //指定页面打开方式：pop-小弹窗，inner-页面嵌入，full-全屏弹窗，默认full
    private String loadingFrameType = "pop";

    private int pageColumnsNum = 2;

    private String barPosition = "bottom";

    //页面模板名称 layui、zui。默认layui
    private String uiFrameType = "layui";

    private List<String> exclusiveColumns = new ArrayList<>();

    private int borderWidth;

    private String borderColor;

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
}
