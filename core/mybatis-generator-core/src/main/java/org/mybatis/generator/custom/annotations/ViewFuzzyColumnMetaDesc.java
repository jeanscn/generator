package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.ViewColumnMeta;
import com.vgosoft.core.annotation.ViewFuzzyColumnMeta;
import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.core.constant.enums.core.QueryModesEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.VOViewGeneratorConfiguration;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.Set;
import java.util.stream.Collectors;

public class ViewFuzzyColumnMetaDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@ViewFuzzyColumnMeta";

    private String value;

    private String title;
    private int order;

    private String comparator;

    public ViewFuzzyColumnMetaDesc(String columnName, String title) {
        super();
        this.value = columnName;
        this.title = title;
        this.addImports(ViewFuzzyColumnMeta.class.getCanonicalName());
    }

    @Override
    public String toAnnotation() {
        items.add(VStringUtil.format("value = \"{0}\"", this.value));
        items.add(VStringUtil.format("title = \"{0}\"", this.title));
        if(this.order != 20){
            items.add(VStringUtil.format("order = {0}", this.order));
        }
        if(VStringUtil.stringHasValue(this.comparator) && !this.comparator.equals(QueryModesEnum.LIKE.name())){
            items.add(VStringUtil.format("comparator = QueryModesEnum.{0}.name()", this.comparator));
            addImports(QueryModesEnum.class.getCanonicalName());
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getComparator() {
        return comparator;
    }

    public void setComparator(String comparator) {
        this.comparator = comparator;
    }

}
