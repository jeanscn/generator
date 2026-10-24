package org.mybatis.generator.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public interface ConnectionFactory {

    /**
     * Should return a connection to the database in use for this context.
     * The generator will call this method only one time for each context.
     * The generator will close the connection.
     *
     * @return the connection
     * @throws SQLException if there is some error obtaining the connection
     */
    Connection getConnection() throws SQLException;

    /**
     * Adds properties for this instance from any properties configured in the
     * ConnectionFactory.
     *
     * <p>This method will be called before any of the get methods.
     *
     * @param properties
     *            All properties from the configuration
     */
    void addConfigurationProperties(Properties properties);
}
