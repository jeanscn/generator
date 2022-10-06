package org.mybatis.generator.custom.annotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-03 16:54
 * @version 3.0
 */
public abstract class AbstractAnnotation implements IAnnotation{

    protected Set<String> imports = new TreeSet<>();

    protected List<String> items = new ArrayList<>();

    public AbstractAnnotation() {
    }

    public String[] multipleImports(){
         return this.imports.toArray(new String[imports.size()]);
    }

    public Set<String> getImports() {
        return imports;
    }

    protected void addImports(String imports) {
        this.imports.add(imports);
    }
}
