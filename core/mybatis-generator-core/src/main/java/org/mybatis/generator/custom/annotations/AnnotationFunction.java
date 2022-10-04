package org.mybatis.generator.custom.annotations;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-03 17:43
 * @version 3.0
 */

@FunctionalInterface
public interface AnnotationFunction<M,P> {

    void accept(M m,P p);

}
