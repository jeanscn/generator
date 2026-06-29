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
    INFORMIX("",""),
    DM("","    `{0}` {1}{2}{3}{4}COMMENT ''{5}''");

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
        if (database == null) {
            return null;
        }

        String normalized = database.trim().toUpperCase();
        DatabaseDDLDialects returnValue = null;
        if ("DB2".equalsIgnoreCase(normalized)) {
            returnValue = DB2;
        } else if ("MYSQL".equalsIgnoreCase(normalized)) {
            returnValue = MYSQL;
        } else if ("SQLSERVER".equalsIgnoreCase(normalized)
                || normalized.contains("SQL SERVER")) {
            returnValue = SQLSERVER;
        } else if ("CLOUDSCAPE".equalsIgnoreCase(normalized)) {
            returnValue = CLOUDSCAPE;
        } else if ("DERBY".equalsIgnoreCase(normalized)) {
            returnValue = DERBY;
        } else if ("HSQLDB".equalsIgnoreCase(normalized)
                || normalized.contains("HSQL")) {
            returnValue = HSQLDB;
        } else if ("SYBASE".equalsIgnoreCase(normalized)) {
            returnValue = SYBASE;
        } else if ("DB2_MF".equalsIgnoreCase(normalized)) {
            returnValue = DB2_MF;
        } else if ("INFORMIX".equalsIgnoreCase(normalized)) {
            returnValue = INFORMIX;
        } else if("H2".equalsIgnoreCase(normalized)) {
            returnValue = H2;
        } else if("DM DBMS".equalsIgnoreCase(normalized)
                || "DM".equalsIgnoreCase(normalized)) {
            returnValue = DM;
        }
        return returnValue;
    }
}
