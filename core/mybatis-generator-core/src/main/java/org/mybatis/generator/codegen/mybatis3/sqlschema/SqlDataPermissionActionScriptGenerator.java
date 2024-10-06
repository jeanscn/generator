package org.mybatis.generator.codegen.mybatis3.sqlschema;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;

/**
 * sql脚本生成器
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-06-21 18:53
 * @version 3.0
 */
public class SqlDataPermissionActionScriptGenerator extends AbstractSqlScriptGenerator {

    private final IntrospectedTable introspectedTable;
    private final DatabaseDDLDialects databaseDDLDialects;

    public SqlDataPermissionActionScriptGenerator(IntrospectedTable introspectedTable, DatabaseDDLDialects databaseDDLDialects) {
        this.introspectedTable = introspectedTable;
        this.databaseDDLDialects = databaseDDLDialects;
        lines.add("-- ----------------------------");
        lines.add(VStringUtil.format("-- Permission Action Data for {0}", introspectedTable.getTableConfiguration().getTableName()));
        lines.add("-- ----------------------------");
        lines.add("");
    }

    @Override
    public String getSqlScript() {
        if (!introspectedTable.getPermissionActionDataScriptLines().isEmpty()) {
            lines.addAll(introspectedTable.getPermissionActionDataScriptLines().values());
        }
        return String.join("\n", getLines());
    }

}
