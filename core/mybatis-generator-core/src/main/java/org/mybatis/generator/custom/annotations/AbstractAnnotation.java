package org.mybatis.generator.custom.annotations;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-03 16:54
 * @version 3.0
 */
public abstract class AbstractAnnotation implements IAnnotation{

    protected List<String> imports = new ArrayList<>();

    public AbstractAnnotation() {
    }

    public String[] multipleImports(){
         return this.imports.toArray(new String[imports.size()]);
    };

    protected List<String> getImports() {
        return imports;
    }

    protected void addImports(String imports) {
        this.imports.add(imports);
    }
}
