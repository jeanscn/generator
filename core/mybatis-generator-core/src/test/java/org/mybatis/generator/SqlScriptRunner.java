package org.mybatis.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.mybatis.generator.custom.ConstantsUtil.DEFAULT_CHARSET;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * This class is used to execute an SQL script before a code generation
 * run.
 *
 * @author Jeff Butler
 */
public class SqlScriptRunner {
    private final String driver;
    private final String url;
    private final String userid;
    private final String password;
    private final InputStream sourceFile;

    public SqlScriptRunner(InputStream sourceFile, String driver, String url,
            String userId, String password) throws Exception {

        if (!stringHasValue(driver)) {
            throw new Exception("JDBC Driver is required");
        }

        if (!stringHasValue(url)) {
            throw new Exception("JDBC URL is required");
        }

        this.sourceFile = sourceFile;
        this.driver = driver;
        this.url = url;
        this.userid = userId;
        this.password = password;
    }

    public void executeScript() throws Exception {

        Connection connection = null;

        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, userid, password);

            Statement statement = connection.createStatement();

            BufferedReader br = new BufferedReader(new InputStreamReader(sourceFile, DEFAULT_CHARSET));

            String sql;

            while ((sql = readStatement(br)) != null) {
                statement.execute(sql);
            }

            closeStatement(statement);
            connection.commit();
            br.close();
        } finally {
            closeConnection(connection);
        }
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    private void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    private String readStatement(BufferedReader br) throws IOException {
        StringBuilder sb = new StringBuilder();

        String line;

        while ((line = br.readLine()) != null) {
            if (line.startsWith("--")) { //$NON-NLS-1$
                continue;
            }

            if (!stringHasValue(line)) {
                continue;
            }

            if (line.endsWith(";")) { //$NON-NLS-1$
                sb.append(line, 0, line.length() - 1);
                break;
            } else {
                sb.append(' ');
                sb.append(line);
            }
        }

        String s = sb.toString().trim();

        return !s.isEmpty() ? s : null;
    }
}
