package org.mybatis.generator.codegen.mybatis3.sqlschema;

import lombok.Getter;
import org.mybatis.generator.codegen.AbstractGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cen_c
 */
@Getter
public abstract class AbstractSqlScriptGenerator extends AbstractGenerator {

    protected List<String> lines = new ArrayList<>();


    /**
     * 获得sql搅拌字符串
     * @return 脚本字符串
     */
    public abstract String getSqlScript();

    public AbstractSqlScriptGenerator addLine(String line) {
        this.lines.add(line );
        return this;
    }
}
