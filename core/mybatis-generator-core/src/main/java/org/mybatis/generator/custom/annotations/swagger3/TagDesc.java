package org.mybatis.generator.custom.annotations.swagger3;

import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-02-20 15:49
 * @version 3.0
 */
@Setter
@Getter
public class TagDesc extends AbstractAnnotation {
    public static final String ANNOTATION_NAME = "@Tag";
    public static final String ANNOTATIONS_TAG = "io.swagger.v3.oas.annotations.tags.Tag";
    private String description ;
    private String name;

    public static TagDesc create(String name) {
        return new TagDesc(name);
    }
   public TagDesc(String name) {
        super();
        this.name = name;
        this.addImports(ANNOTATIONS_TAG);
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(description)) {
            this.items.add(VStringUtil.format("description = \"{0}\"", this.description));
        }
        if (VStringUtil.isNotBlank(name)) {
            this.items.add(VStringUtil.format("name = \"{0}\"", this.name));
        } else {
            this.items.add("name = \"\"");
        }

        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }

}
