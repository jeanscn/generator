package org.mybatis.generator.custom.annotations;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-03 11:15
 * @version 3.0
 */
public interface IAnnotation {

    default List<String> toAnnotations(){
        return Collections.singletonList(toAnnotation());
    };

    default String toAnnotation(){
        return toAnnotations().get(0);
    };

    String[] multipleImports();

}
