package org.mybatis.generator.custom.annotations;

import com.vgosoft.tool.core.VStringUtil;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-02-20 15:49
 * @version 3.0
 */
public class Api extends AbstractAnnotation {
    public static final String ANNOTATION_NAME = "@Api";
    public static final String importClass = "io.swagger.annotations.Api";
    private String value;
    private String tags;
    private String hidden;
    private String produces;
    private String consumes;
    private String protocols;

    public static Api create(String tags) {
        return new Api(tags);
    }
   public Api(String tags) {
        super();
        this.tags = tags;
        this.addImports(importClass);
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

    public String getValue() {
        return value;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getHidden() {
        return hidden;
    }

    public void setHidden(String hidden) {
        this.hidden = hidden;
    }

    public String getProduces() {
        return produces;
    }

    public void setProduces(String produces) {
        this.produces = produces;
    }

    public String getConsumes() {
        return consumes;
    }

    public void setConsumes(String consumes) {
        this.consumes = consumes;
    }

    public String getProtocols() {
        return protocols;
    }

    public void setProtocols(String protocols) {
        this.protocols = protocols;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
