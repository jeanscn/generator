package org.mybatis.generator.custom.pojo;

import org.mybatis.generator.config.PropertyHolder;

import java.util.ArrayList;
import java.util.List;

public class HtmlDescriptor extends PropertyHolder {

    private boolean generate;

    private String viewPath;

    private String htmlFileName;

    private String targetProject = "src/main/resources/templates";

    private String targetPackage;

    //指定页面打开方式：pop-小弹窗，inner-页面嵌入，full-全屏弹窗，默认full
    private String loadingFrameType;

    //页面模板名称 layui、zui。默认layui
    private String uiFrameType;

    private int pageColumnsNum;

    private String barPosition;

    private List<String> hiddenColumns;

    //指定页面表单不允许为空的字段
    private List<String> elementRequired;

    private List<HtmlElementDescriptor> elementDescriptors;

    public HtmlDescriptor() {
        targetProject = "src/main/resources/templates";
        loadingFrameType = "full";
        uiFrameType = "layui";
        pageColumnsNum = 2;
        barPosition = "bottom";
        hiddenColumns = new ArrayList<>();
        elementRequired = new ArrayList<>();
        elementDescriptors = new ArrayList<>();
    }

    public boolean isGenerate() {
        return generate;
    }

    public void setGenerate(boolean generate) {
        this.generate = generate;
    }

    public String getViewPath() {
        return viewPath;
    }

    public void setViewPath(String viewPath) {
        this.viewPath = viewPath;
    }

    public String getTargetProject() {
        return targetProject;
    }

    public void setTargetProject(String targetProject) {
        this.targetProject = targetProject;
    }

    public String getLoadingFrameType() {
        return loadingFrameType;
    }

    public void setLoadingFrameType(String loadingFrameType) {
        this.loadingFrameType = loadingFrameType;
    }

    public String getUiFrameType() {
        return uiFrameType;
    }

    public void setUiFrameType(String uiFrameType) {
        this.uiFrameType = uiFrameType;
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

    public List<String> getHiddenColumns() {
        return hiddenColumns;
    }

    public void setHiddenColumns(List<String> hiddenColumns) {
        this.hiddenColumns = hiddenColumns;
    }

    public List<String> getElementRequired() {
        return elementRequired;
    }

    public void setElementRequired(List<String> elementRequired) {
        this.elementRequired = elementRequired;
    }

    public List<HtmlElementDescriptor> getElementDescriptors() {
        return elementDescriptors;
    }

    public List<HtmlElementDescriptor> addElementDescriptors(HtmlElementDescriptor htmlElementDescriptor) {
        this.elementDescriptors.add(htmlElementDescriptor);
        return this.elementDescriptors;
    }

    public List<String> addHiddenColumns(String column) {
        this.hiddenColumns.add(column);
        return this.hiddenColumns;
    }


    public String getHtmlFileName() {
        return htmlFileName;
    }

    public void setHtmlFileName(String htmlFileName) {
        this.htmlFileName = htmlFileName;
    }

    public String getTargetPackage() {
        return targetPackage;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }
}
