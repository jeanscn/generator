package org.mybatis.generator.custom.annotations.swagger3;

import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-02-20 14:17
 * @version 3.0
 */
@Getter
@Setter
public class SchemaPropertyDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@Schema";
    public static final  String SCHEMA_PROPERTY = "io.swagger.v3.oas.annotations.media.Schema";

    private final String description;

    private String example;

    private String hidden;

    private String requiredMode;


    public static SchemaPropertyDesc create(String value){
        return new SchemaPropertyDesc(value);
    }

    public SchemaPropertyDesc(String value) {
        super();
        this.description = value;
        this.addImports(SCHEMA_PROPERTY);
    }

    public SchemaPropertyDesc(String value, String example) {
        this.description = value;
        this.example = example;
        this.addImports(SCHEMA_PROPERTY);
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(description)) {
            this.items.add(VStringUtil.format("description = \"{0}\"", this.description));
        }
        if (VStringUtil.isNotBlank(hidden)) {
            this.items.add(VStringUtil.format("hidden = {0}", this.hidden));
        }
        if (VStringUtil.isNotBlank(example)) {
            this.items.add(VStringUtil.format("example = \"{0}\"", this.example));
        }
        if (VStringUtil.isNotBlank(requiredMode)) {
            this.items.add(VStringUtil.format("requiredMode = {0}", "Schema.RequiredMode.REQUIRED"));
        }
        return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
    }

}
