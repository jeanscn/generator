package org.mybatis.generator.custom.db;

import lombok.Getter;

/**
 * @author cen_c
 */

@Getter
public enum DatabaseDDLDialects {

    /**
     * 数据库方言对应的ddl语句模板
     * {0}操作类型 ADD|MODIFY|CHANGE|DROP
     * {1}列名
     * {2}列类型
     * {3}列长度
     * {4}字符集
     * {5}非空
     * {6}备注
     * {7}位置
     */
    DB2("",""),
    MYSQL("{0} COLUMN `{1}` {2}{3}{4}{5}COMMENT ''{6}''{7}","    `{0}` {1}{2}{3}{4}COMMENT ''{5}''"),
    SQLSERVER("",""),
    CLOUDSCAPE("",""),
    DERBY("",""),
    HSQLDB("",""),
    SYBASE("",""),
    DB2_MF("",""),
    H2("","    `{0}` {1}{2}{3}{4}COMMENT ''{5}''"),
    INFORMIX("","");

    private final String columnModifyStatement;
    private final String createStatement;

    DatabaseDDLDialects(String columnModifyStatement,String createStatement) {
        this.columnModifyStatement = columnModifyStatement;
        this.createStatement = createStatement;
    }

    /**
     * 获取数据库方言。
     * @param database 数据库
     * @return 为所选数据库创建数据库方言。如果所选数据库没有已知方言，则可能返回null
     */
    public static DatabaseDDLDialects getDatabaseDialect(String database) {
        return getDialects(database);
    }

    private static DatabaseDDLDialects getDialects(String database) {
        DatabaseDDLDialects returnValue = null;
        if ("DB2".equalsIgnoreCase(database)) {
            returnValue = DB2;
        } else if ("MySQL".equalsIgnoreCase(database)) {
            returnValue = MYSQL;
        } else if ("SqlServer".equalsIgnoreCase(database)) {
            returnValue = SQLSERVER;
        } else if ("Cloudscape".equalsIgnoreCase(database)) {
            returnValue = CLOUDSCAPE;
        } else if ("Derby".equalsIgnoreCase(database)) {
            returnValue = DERBY;
        } else if ("HSQLDB".equalsIgnoreCase(database)) {
            returnValue = HSQLDB;
        } else if ("SYBASE".equalsIgnoreCase(database)) {
            returnValue = SYBASE;
        } else if ("DB2_MF".equalsIgnoreCase(database)) {
            returnValue = DB2_MF;
        } else if ("Informix".equalsIgnoreCase(database)) {
            returnValue = INFORMIX;
        } else if("H2".equalsIgnoreCase(database)) {
            returnValue = H2;
        }
        return returnValue;
    }
}
