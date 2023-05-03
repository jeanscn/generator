package org.mybatis.generator.custom.annotations;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-03 11:15
 * @version 3.0
 */
public interface IAnnotation {

    default List<String> toAnnotations(){
        return Collections.singletonList(toAnnotation());
    }

    default String toAnnotation(){
        return toAnnotations().get(0);
    }

    Set<String> getImports();

    default Set<FullyQualifiedJavaType> getImportedTypes() {
        Set<FullyQualifiedJavaType> imports = new TreeSet<>();
        for (String item : this.getImports()) {
            imports.add(new FullyQualifiedJavaType(item));
        }
        return imports;
    }

}
