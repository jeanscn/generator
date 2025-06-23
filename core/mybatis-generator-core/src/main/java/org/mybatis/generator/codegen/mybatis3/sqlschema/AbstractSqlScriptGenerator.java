package org.mybatis.generator.codegen.mybatis3.sqlschema;

import lombok.Getter;
import org.mybatis.generator.codegen.AbstractGenerator;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class AbstractSqlScriptGenerator extends AbstractGenerator {

    protected List<String> lines = new ArrayList<>();


    public abstract String getSqlScript();

    public AbstractSqlScriptGenerator addLine(String line) {
        this.lines.add(line );
        return this;
    }
}
