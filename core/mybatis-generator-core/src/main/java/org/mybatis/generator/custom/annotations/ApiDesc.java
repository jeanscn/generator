package org.mybatis.generator.custom.annotations;

import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-02-20 15:49
 * @version 3.0
 */
@Setter
@Getter
public class ApiDesc extends AbstractAnnotation {
    public static final String ANNOTATION_NAME = "@Api";
    public static final String ANNOTATIONS_API = "io.swagger.annotations.Api";
    private String value;
    private String tags;
    private String hidden;
    private String produces;
    private String consumes;
    private String protocols;

    public static ApiDesc create(String tags) {
        return new ApiDesc(tags);
    }
   public ApiDesc(String tags) {
        super();
        this.tags = tags;
        this.addImports(ANNOTATIONS_API);
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(value)) {
            this.items.add(VStringUtil.format("value = \"{0}\"", this.value));
        }
        if (VStringUtil.isNotBlank(tags)) {
            this.items.add(VStringUtil.format("tags = \"{0}\"", this.tags));
        }
        if (VStringUtil.isNotBlank(produces)) {
            this.items.add(VStringUtil.format("produces = \"{0}\"", this.produces));
        }
        if (VStringUtil.isNotBlank(consumes)) {
            this.items.add(VStringUtil.format("consumes = \"{0}\"", this.consumes));
        }
        if (VStringUtil.isNotBlank(protocols)) {
            this.items.add(VStringUtil.format("protocols = \"{0}\"", this.protocols));
        }
        if (VStringUtil.isNotBlank(hidden)) {
            this.items.add(VStringUtil.format("hidden = {0}", this.hidden));
        }

        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }

}
