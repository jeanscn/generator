package org.mybatis.generator.dbstrategy.introspectorstrategy;

import com.vgosoft.core.db.model.metadata.Column;
import com.vgosoft.core.db.model.metadata.Table;
import com.vgosoft.core.db.query.DatabaseQuery;
import com.vgosoft.core.db.query.DatabaseQueryFactory;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.dbstrategy.DatabaseIntrospectorStrategy;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

abstract class QueryBackedRemarksIntrospectorStrategy implements DatabaseIntrospectorStrategy {
    @Override
    public Map<String, String> getColumnRemarks(Connection conn, String tableName) {
        if (!stringHasValue(tableName)) {
            return Collections.emptyMap();
        }

        try {
            List<? extends Column> columns = createDatabaseQuery(conn).getTableColumns(tableName);
            Map<String, String> remarks = new HashMap<>();
            for (Column column : columns) {
                if (stringHasValue(column.getColumnName()) && stringHasValue(column.getRemarks())) {
                    remarks.put(column.getColumnName(), column.getRemarks());
                }
            }
            return remarks;
        } catch (RuntimeException e) {
            return Collections.emptyMap();
        }
    }

    @Override
    public void updateTableRemark(AbstractIntrospectedTable abstractIntrospectedTable, Connection conn) {
        String tableName = abstractIntrospectedTable.getFullyQualifiedTable().getIntrospectedTableName();
        if (!stringHasValue(tableName)) {
            return;
        }

        try {
            for (Table table : createDatabaseQuery(conn).getTables()) {
                if (sameIdentifier(tableName, table.getTableName()) && stringHasValue(table.getRemarks())) {
                    abstractIntrospectedTable.setRemarks(table.getRemarks());
                    return;
                }
            }
        } catch (RuntimeException e) {
            // fall back to JDBC metadata remark already loaded by DatabaseIntrospector
        }
    }

    protected DatabaseQuery createDatabaseQuery(Connection conn) {
        return DatabaseQueryFactory.create(new ConnectionBackedDataSource(conn));
    }

    protected boolean sameIdentifier(String source, String target) {
        return source != null && source.equalsIgnoreCase(target);
    }

    private static final class ConnectionBackedDataSource implements DataSource {
        private final Connection proxyConnection;

        private ConnectionBackedDataSource(Connection connection) {
            this.proxyConnection = (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();
                        switch (methodName) {
                            case "close" -> {
                                return null;
                            }
                            case "unwrap" -> {
                                Class<?> iface = (Class<?>) args[0];
                                if (iface.isInstance(connection) || iface == Connection.class) {
                                    return connection;
                                }
                                return connection.unwrap(iface);
                            }
                            case "isWrapperFor" -> {
                                Class<?> iface = (Class<?>) args[0];
                                return iface == Connection.class || iface.isInstance(connection) || connection.isWrapperFor(iface);
                            }
                            case "equals" -> {
                                return proxy == args[0];
                            }
                            case "hashCode" -> {
                                return System.identityHashCode(proxy);
                            }
                            case "toString" -> {
                                return connection.toString();
                            }
                            default -> {
                                try {
                                    return method.invoke(connection, args);
                                } catch (InvocationTargetException e) {
                                    throw e.getTargetException();
                                }
                            }
                        }
                    });
        }

        @Override
        public Connection getConnection() {
            return proxyConnection;
        }

        @Override
        public Connection getConnection(String username, String password) {
            return proxyConnection;
        }

        @Override
        public PrintWriter getLogWriter() {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) {
            // no-op
        }

        @Override
        public void setLoginTimeout(int seconds) {
            // no-op
        }

        @Override
        public int getLoginTimeout() {
            return 0;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            if (iface.isInstance(this)) {
                return iface.cast(this);
            }
            throw new SQLException("Unsupported unwrap type: " + iface.getName());
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return iface.isInstance(this);
        }
    }
}
