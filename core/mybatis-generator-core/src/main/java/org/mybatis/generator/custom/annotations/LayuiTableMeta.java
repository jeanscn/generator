package org.mybatis.generator.custom.annotations;

import com.vgosoft.tool.core.VStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.vgosoft.tool.core.VStringUtil.*;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-06-02 19:50
 * @version 4.0
 */
public class LayuiTableMeta extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@LayuiTableMeta";

    private List<String> defaultToolbar = new ArrayList<>();

    private int width;
    private String height;
    private boolean totalRow;
    private String enablePage;
    private String title;
    private String skin;
    private String size;
    private boolean even;

    public LayuiTableMeta() {
        super();
        this.addImports("com.vgosoft.core.annotation.LayuiTableMeta");
    }

    @Override
    public String toAnnotation() {
        if (defaultToolbar.size() > 0) {
            items.add("defaultToolbar = \"" + String.join(",", defaultToolbar) + "\"");
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
        if (stringHasValue(enablePage)) {
            items.add("page = \"" + enablePage + "\"");
        }
        if (stringHasValue(title)) {
            items.add("title = \"" + title + "\"");
        }
        if (stringHasValue(skin)) {
            items.add("skin = \"" + skin + "\"");
        }
        if (stringHasValue(size)) {
            items.add("size = \"" + size + "\"");
        }
        if (even) {
            items.add("even = true");
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
}
