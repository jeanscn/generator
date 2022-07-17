package org.mybatis.generator.custom.db;

public enum DatabaseDDLDialects {

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

    public String getColumnModifyStatement() {
        return columnModifyStatement;
    }

    public String getCreateStatement() {
        return createStatement;
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
