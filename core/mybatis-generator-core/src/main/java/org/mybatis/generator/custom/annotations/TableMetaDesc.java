package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.TableMeta;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.AbstractIntrospectedTable;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-07 03:59
 * @version 3.0
 */
@Getter
@Setter
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

    public static TableMetaDesc create(AbstractIntrospectedTable abstractIntrospectedTable){
        return new TableMetaDesc(abstractIntrospectedTable);
    }

    public TableMetaDesc(AbstractIntrospectedTable abstractIntrospectedTable) {
        super();
        this.value = abstractIntrospectedTable.getTableConfiguration().getTableName();
        this.alias = abstractIntrospectedTable.getTableConfiguration().getAlias();
        this.descript = abstractIntrospectedTable.getRemarks(true);
        this.beanname = abstractIntrospectedTable.getControllerBeanName();
        this.summary = true;
        if (abstractIntrospectedTable.getVoModelType() != null) {
            this.voClass = abstractIntrospectedTable.getVoModelType().getFullyQualifiedName();
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
