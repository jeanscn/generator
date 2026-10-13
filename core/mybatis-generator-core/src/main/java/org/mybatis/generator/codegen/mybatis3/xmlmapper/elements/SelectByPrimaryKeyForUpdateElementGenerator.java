package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import com.vgosoft.core.db.query.DatabaseType;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.SelectForUpdateValidator;

/**
 * 生成指定方言的完整主键锁定查询 XML 元素。
 *
 * @author vgosoft
 */
public class SelectByPrimaryKeyForUpdateElementGenerator extends AbstractSelectForUpdateElementGenerator {
    private final DatabaseType database;

    /**
     * 固定本次执行的数据库分支，以保留原多方言语句输出顺序。
     *
     * @param database 目标数据库方言
     */
    public SelectByPrimaryKeyForUpdateElementGenerator(DatabaseType database) {
        this.database = database;
    }

    @Override
    public void addElements(XmlElement parentElement) {
        SelectForUpdateValidator.validate(introspectedTable);
        if (introspectedTable.getRules().generateSelectByPrimaryKeyForUpdate()) {
            var element = select(database, false);
            if (context.getPlugins().sqlMapSelectByPrimaryKeyForUpdateElementGenerated(element, introspectedTable)) {
                parentElement.addElement(element);
            }
        }
    }
}
