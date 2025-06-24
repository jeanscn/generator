package org.mybatis.generator.config;

import com.vgosoft.tool.core.VStringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
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

    public String getDataUrl() {
        //如果不是以“/”开头，则加上
        if (VStringUtil.stringHasValue(dataUrl) && !dataUrl.startsWith("/")) {
            dataUrl = "/" + dataUrl;
        }
        return dataUrl;
    }
}
