package org.mybatis.generator.custom.annotations;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;

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

    public Set<String> getImports() {
        return imports;
    }

    public void setImports(Set<String> imports) {
        this.imports = imports;
    }

    public void addImports(String imports) {
        this.imports.add(imports);
    }

    // 添加注解到字段上
    public void addAnnotationToField(Field field, TopLevelClass topLevelClass) {
        field.addAnnotation(this.toAnnotation());
        topLevelClass.addImportedTypes(this.getImportedTypes());
    }

    // 添加注解到类上
    public void addAnnotationToTopLevelClass(TopLevelClass topLevelClass){
        topLevelClass.addAnnotation(this.toAnnotation());
        topLevelClass.addImportedTypes(this.getImportedTypes());
    }
}
