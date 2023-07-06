package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.TableMeta;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedTable;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-07 03:59
 * @version 3.0
 */
public class TableMetaDesc extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@TableMeta";

    private final String value;

    private final String alias;

    private String descript;

    private String beanname;

    private boolean summary;

    private String voClass;

    private String superClass;

    private String[] superInterfaces;

    public static TableMetaDesc create(IntrospectedTable introspectedTable){
        return new TableMetaDesc(introspectedTable);
    }

    public TableMetaDesc(IntrospectedTable introspectedTable) {
        super();
        this.value = introspectedTable.getTableConfiguration().getTableName();
        this.alias = introspectedTable.getTableConfiguration().getAlias();
        this.descript = introspectedTable.getRemarks(true);
        this.beanname = introspectedTable.getControllerBeanName();
        this.summary = true;
        if (introspectedTable.getVoModelType() != null) {
            this.voClass = introspectedTable.getVoModelType().getFullyQualifiedName();
        }
        this.addImports(TableMeta.class.getCanonicalName());
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
        if (VStringUtil.isNotBlank(this.getVoClass())) {
            items.add(VStringUtil.format("voClass = \"{0}\"", this.getVoClass()));
        }

        if (VStringUtil.isNotBlank(this.getSuperClass())) {
            items.add(VStringUtil.format("superClass = {0}", this.getSuperClass()));
        }
        if (this.getSuperInterfaces() != null && this.getSuperInterfaces().length > 0) {
            items.add(VStringUtil.format("superInterfaces = '{'{0}'}'", String.join(", ", this.getSuperInterfaces())));
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

    public String getVoClass() {
        return voClass;
    }

    public void setVoClass(String voClass) {
        this.voClass = voClass;
    }

    public String getSuperClass() {
        return superClass;
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    public String[] getSuperInterfaces() {
        return superInterfaces;
    }

    public void setSuperInterfaces(String[] superInterfaces) {
        this.superInterfaces = superInterfaces;
    }

    public void addSuperInterface(String superInterface) {
        if (this.superInterfaces == null) {
            this.superInterfaces = new String[0];
        }
        String[] newSuperInterfaces = new String[this.superInterfaces.length + 1];
        System.arraycopy(this.superInterfaces, 0, newSuperInterfaces, 0, this.superInterfaces.length);
        newSuperInterfaces[this.superInterfaces.length] = superInterface;
        this.superInterfaces = newSuperInterfaces;
    }
}
