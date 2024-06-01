package org.mybatis.generator.codegen.mybatis3.sqlschema;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;

/**
 * sql脚本生成器
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-06-21 18:53
 * @version 3.0
 */
public class SqlDataSysModuleScriptGenerator extends AbstractSqlScriptGenerator {

    private final DatabaseDDLDialects databaseDDLDialects;

    public SqlDataSysModuleScriptGenerator(Context context, DatabaseDDLDialects databaseDDLDialects) {
        this.context = context;
        this.databaseDDLDialects = databaseDDLDialects;
        lines.add("-- ----------------------------");
        lines.add(VStringUtil.format("-- Module Data for {0}", this.context.getModuleKeyword()));
        lines.add("-- "+this.databaseDDLDialects.name());
        lines.add("-- ----------------------------");
        lines.add("");
    }

    @Override
    public String getSqlScript() {
        if(!this.context.getModuleCateDataScriptLines().isEmpty()){
            lines.add("-- ----------------------------");
            lines.add("-- 模块分类数据");
            lines.add("-- ----------------------------");
            lines.add("");
            lines.addAll(this.context.getModuleCateDataScriptLines().values());
        }
        if (!this.context.getModuleDataScriptLines().isEmpty()) {
            lines.add("-- ----------------------------");
            lines.add("-- 模块数据");
            lines.add("-- ----------------------------");
            lines.add("");
            lines.addAll(this.context.getModuleDataScriptLines().values());
        }
        return String.join("\n", getLines());
    }

}
