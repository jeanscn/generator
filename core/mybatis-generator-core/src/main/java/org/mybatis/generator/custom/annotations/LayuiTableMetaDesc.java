package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.CompositeQuery;
import com.vgosoft.core.annotation.LayuiTableMeta;
import com.vgosoft.tool.core.VStringUtil;

import java.util.ArrayList;
import java.util.List;

import static com.vgosoft.tool.core.VStringUtil.stringHasValue;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-06-02 19:50
 * @version 4.0
 */
public class LayuiTableMetaDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@" + LayuiTableMeta.class.getSimpleName();

    private List<String> defaultToolbar = new ArrayList<>();

    private String value;

    private int width;
    private String height;
    private boolean totalRow;
    private String enablePage;
    private String title;
    private String skin;
    private String size;
    private boolean even;

    private List<String> toolbar = new ArrayList<>();

    private List<String> actionColumn = new ArrayList<>();

    private String indexColumn;

    private String[] querys = new String[0];


    public LayuiTableMetaDesc() {
        super();
        this.addImports(LayuiTableMeta.class.getCanonicalName());
    }

    @Override
    public String toAnnotation() {
        items.clear();
        if(stringHasValue(value)){
            items.add("value = \"" + value + "\"");
        }
        if (width > 0) {
            items.add("width = " + width);
        }
        if (stringHasValue(height)) {
            items.add("height = \"" + height + "\"");
        }
        if (totalRow) {
            items.add("totalRow = true");
        }
        if (stringHasValue(enablePage) && !"false".equals(enablePage)) {
            items.add("page = \"" + enablePage + "\"");
        }
        if (stringHasValue(title)) {
            items.add("title = \"" + title + "\"");
        }
        if (stringHasValue(skin) && !"grid".equals(skin)) {
            items.add("skin = \"" + skin + "\"");
        }
        if (stringHasValue(size) && !"md".equals(size)) {
            items.add("size = \"" + size + "\"");
        }
        if (!even) {
            items.add("even = false");
        }
        if (defaultToolbar.contains("NONE") && toolbar.contains("NONE")) {
            items.add("toolbar = \"false\"");
        } else {
            if (!defaultToolbar.isEmpty() && defaultToolbar.size()!=3) {
                items.add("defaultToolbar = \"" + String.join(",", defaultToolbar) + "\"");
            }
            if (!toolbar.isEmpty()){
                items.add("toolbar = \"" + String.join(",", toolbar) + "\"");
            }
        }
        if (!actionColumn.isEmpty()) {
            items.add("actionColumn = \"" + String.join(",", actionColumn) + "\"");
        }
        if (stringHasValue(indexColumn) && !"CHECKBOX".equals(indexColumn)) {
            items.add("indexColumn = \"" + indexColumn + "\"");
        }
        if (this.querys.length>0) {
            items.add(VStringUtil.format("querys = '{'{0}'}'",String.join("\n        , ", this.querys)));
            this.addImports(CompositeQuery.class.getCanonicalName());
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }

    public List<String> getDefaultToolbar() {
        return defaultToolbar;
    }

    public void setDefaultToolbar(List<String> defaultToolbar) {
        this.defaultToolbar = defaultToolbar;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public boolean isTotalRow() {
        return totalRow;
    }

    public void setTotalRow(boolean totalRow) {
        this.totalRow = totalRow;
    }

    public String getEnablePage() {
        return enablePage;
    }

    public void setEnablePage(String enablePage) {
        this.enablePage = enablePage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public boolean isEven() {
        return even;
    }

    public void setEven(boolean even) {
        this.even = even;
    }

    public List<String> getToolbar() {
        return toolbar;
    }

    public void setToolbar(List<String> toolbar) {
        this.toolbar = toolbar;
    }

    public List<String> getActionColumn() {
        return actionColumn;
    }

    public void setActionColumn(List<String> actionColumn) {
        this.actionColumn = actionColumn;
    }

    public String getIndexColumn() {
        return indexColumn;
    }

    public void setIndexColumn(String indexColumn) {
        this.indexColumn = indexColumn;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String[] getQuerys() {
        return querys;
    }

    public void setQuerys(String[] querys) {
        this.querys = querys;
    }
}
