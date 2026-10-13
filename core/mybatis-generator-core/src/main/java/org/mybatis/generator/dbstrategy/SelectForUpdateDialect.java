package org.mybatis.generator.dbstrategy;

import com.vgosoft.core.db.query.DatabaseType;

/**
 * 维护锁定查询的原生锁语法分组，供配置预检与 XML 渲染共享。
 *
 * @author vgosoft
 */
public final class SelectForUpdateDialect {
    private SelectForUpdateDialect() {
    }

    /**
     * 判断是否使用 SQL Server 表提示，未经验证的方言显式拒绝。
     *
     * @param database 目标数据库方言
     * @return 是否使用表提示而非尾部锁子句
     */
    public static boolean usesTableHint(DatabaseType database) {
        return switch (database) {
            case MYSQL, MARIADB, POSTGRE_SQL, ORACLE, H2 -> false;
            case SQL_SERVER, SQL_SERVER2005 -> true;
            default -> throw new IllegalArgumentException("enableSelectForUpdate 不支持方言 " + database
                    + "：通用锁定查询尚未验证，不能提供等价兜底");
        };
    }
}
