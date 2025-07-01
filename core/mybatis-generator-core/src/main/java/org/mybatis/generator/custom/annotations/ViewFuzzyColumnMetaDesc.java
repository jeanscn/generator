package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.ViewFuzzyColumnMeta;
import com.vgosoft.core.constant.enums.core.QueryModesEnum;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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

}
