package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.enums.ViewVoUiFrameEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class AbstractTableListCommonConfiguration extends AbstractModelGeneratorConfiguration{

    protected String title;
    private boolean showTitle;
    protected String size;

    protected String listKey;
    protected String indexColumn;
    protected String indexColumnFixed;

    protected List<String> toolbar = new ArrayList<>();
    protected List<String> actionColumn = new ArrayList<>();

    protected String actionColumnWidth;
    protected String actionColumnFixed;

    protected List<String> queryColumns = new ArrayList<>();
    protected List<String> fuzzyColumns = new ArrayList<>();
    protected List<String> filterColumns = new ArrayList<>();
    protected List<String> defaultDisplayFields = new ArrayList<>();

    protected Set<String> defaultHiddenFields = new HashSet<>();
    protected List<String> defaultToolbar = new ArrayList<>();
    protected boolean enablePager = true;
    protected String parentMenuId;
    protected String viewMenuElIcon;
    protected String categoryTreeUrl;
    protected boolean categoryTreeMultiple;
    protected ViewVoUiFrameEnum uiFrameType;
    protected String tableType;
    protected boolean totalRow;
    protected Set<String> totalFields;
    protected String totalText = "合计";

    protected String defaultFilterExpr;

    protected String defaultSort;

    protected boolean showRowNumber = true;
    protected String showActionColumn = "default";
    protected String editFormIn;
    protected String detailFormIn;

    public AbstractTableListCommonConfiguration() {
        super();
    }
    public AbstractTableListCommonConfiguration(Context context) {
        super(context);
    }
    @Override
    void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "AbstractTableListCommonConfiguration");
    }

}
