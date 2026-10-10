package org.mybatis.generator.custom.annotations.swagger3;

import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-03 16:33
 * @version 3.0
 */
@Getter
public class OperationDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@Operation";
    public static final String API_OPERATION = "io.swagger.v3.oas.annotations.Operation";

    private final String summary;
    @Setter
    private  String description;
    @Setter
    private  List<String> tags = new ArrayList<>();

    public static OperationDesc create(String summary, String description){
        return new OperationDesc(summary,description);
    }

    public OperationDesc(String summary, String description) {
        super();
        this.summary = summary;
        this.description = description;
        this.addImports(API_OPERATION);
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(summary)) {
            this.items.add(VStringUtil.format("summary = \"{0}\"", this.summary));
        }
        if (VStringUtil.isNotBlank(description)) {
            this.items.add(VStringUtil.format("description = \"{0}\"", this.description));
        }
        return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
    }

}
