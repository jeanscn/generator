package org.mybatis.generator.config;

import com.vgosoft.tool.core.VStringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbstractHtmlElementDescriptor  extends PropertyHolder{
    protected boolean enablePager;
    protected String defaultFilterExpr;
    protected String dataUrl;
    protected String dataUrlParams;
    protected String hideExpression;
    protected String disabledExpression;
    protected String listKey;
    protected List<String> vxeListButtons = new ArrayList<>();
    protected List<String> actionColumn = new ArrayList<>();
    protected final Set<HtmlButtonGeneratorConfiguration> htmlButtons = new HashSet<>();

    public AbstractHtmlElementDescriptor() {
        super();
    }

    public boolean isEnablePager() {
        return enablePager;
    }

    public void setEnablePager(boolean enablePager) {
        this.enablePager = enablePager;
    }

    public String getDefaultFilterExpr() {
        return defaultFilterExpr;
    }

    public void setDefaultFilterExpr(String defaultFilterExpr) {
        this.defaultFilterExpr = defaultFilterExpr;
    }

    public String getDataUrl() {
        //如果不是以“/”开头，则加上
        if (VStringUtil.stringHasValue(dataUrl) && !dataUrl.startsWith("/")) {
            dataUrl = "/" + dataUrl;
        }
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public String getDataUrlParams() {
        return dataUrlParams;
    }

    public void setDataUrlParams(String dataUrlParams) {
        this.dataUrlParams = dataUrlParams;
    }

    public String getHideExpression() {
        return hideExpression;
    }

    public void setHideExpression(String hideExpression) {
        this.hideExpression = hideExpression;
    }

    public String getDisabledExpression() {
        return disabledExpression;
    }

    public void setDisabledExpression(String disabledExpression) {
        this.disabledExpression = disabledExpression;
    }

    public String getListKey() {
        return listKey;
    }

    public void setListKey(String listKey) {
        this.listKey = listKey;
    }

    public List<String> getVxeListButtons() {
        return vxeListButtons;
    }

    public void setVxeListButtons(List<String> vxeListButtons) {
        this.vxeListButtons = vxeListButtons;
    }

    public Set<HtmlButtonGeneratorConfiguration> getHtmlButtons() {
        return htmlButtons;
    }

    public List<String> getActionColumn() {
        return actionColumn;
    }

    public void setActionColumn(List<String> actionColumn) {
        this.actionColumn = actionColumn;
    }
}
