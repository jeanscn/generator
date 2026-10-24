package org.mybatis.generator.custom.annotations.swagger3;

import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-02-20 15:27
 * @version 3.0
 */
@Getter
@Setter
public class SchemaDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@Schema";
    public static final  String ANNOTATIONS_SCHEMA = "io.swagger.v3.oas.annotations.media.Schema";

    private final String name;

    private String description;

    private String implementation;

    public static SchemaDesc create(String name){
        return new SchemaDesc(name);
    }

    public SchemaDesc(String name) {
        super();
        this.name = name;
        this.addImports(ANNOTATIONS_SCHEMA);
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(name)) {
            this.items.add(VStringUtil.format("name = \"{0}\"", this.name));
        }
        if (VStringUtil.isNotBlank(description)) {
            this.items.add(VStringUtil.format("description = \"{0}\"", this.description));
        }
        if (VStringUtil.isNotBlank(implementation)) {
            this.items.add(VStringUtil.format("implementation = {0}", this.implementation));
        }
        return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
    }

}
