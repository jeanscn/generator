package org.mybatis.generator.custom.annotations;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.custom.ConstantsUtil;

import static org.mybatis.generator.custom.ConstantsUtil.ANNOTATION_TABLE_META;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-07 03:59
 * @version 3.0
 */
public class TableMeta extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@TableMeta";

    private final String value;

    private final String alias;

    private String descript;

    private String beanname;

    private boolean summary;

    public static TableMeta create(IntrospectedTable introspectedTable){
        return new TableMeta(introspectedTable);
    }

    public TableMeta(IntrospectedTable introspectedTable) {
        super();
        this.value = introspectedTable.getTableConfiguration().getTableName();
        this.alias = introspectedTable.getTableConfiguration().getAlias();
        this.descript = introspectedTable.getRemarks(true);
        this.beanname = introspectedTable.getControllerBeanName();
        this.summary = true;
        this.addImports(ConstantsUtil.ANNOTATION_TABLE_META);
    }

    @Override
    public String toAnnotation() {
        items.add(VStringUtil.format("value = \"{0}\"", this.getValue()));
        items.add(VStringUtil.format("alias = \"{0}\"", this.getAlias()));
        if (VStringUtil.isNotBlank(this.getBeanname())) {
            items.add(VStringUtil.format("beanname = \"{0}\"", this.getBeanname()));
        }
        if (VStringUtil.isNotBlank(this.getDescript())) {
            items.add(VStringUtil.format("descript = \"{0}\"", this.getDescript()));
        }
        if (!this.isSummary()) {
            items.add("summary = false");
        }
        return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
    }

    public String getValue() {
        return value;
    }

    public String getAlias() {
        return alias;
    }

    public String getDescript() {
        return descript;
    }

    public void setDescript(String descript) {
        this.descript = descript;
    }

    public String getBeanname() {
        return beanname;
    }

    public void setBeanname(String beanname) {
        this.beanname = beanname;
    }

    public boolean isSummary() {
        return summary;
    }

    public void setSummary(boolean summary) {
        this.summary = summary;
    }
}
