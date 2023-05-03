package mbg.test.mb3.common;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import mbg.test.common.FirstName;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

/**
 * @author Jeff Butler
 *
 */
public class FirstNameTypeHandler implements TypeHandler<FirstName> {

    /**
     *
     */
    public FirstNameTypeHandler() {
        super();
    }

    @Override
    public FirstName getResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        FirstName answer = null;
        String string = cs.getString(columnIndex);
        if (string != null) {
            answer = new FirstName();
            answer.setValue(string);
        }

        return answer;
    }

    @Override
    public FirstName getResult(ResultSet rs, String columnName)
            throws SQLException {
        FirstName answer = null;
        String string = rs.getString(columnName);
        if (string != null) {
            answer = new FirstName();
            answer.setValue(string);
        }

        return answer;
    }

    @Override
    public FirstName getResult(ResultSet rs, int columnIndex)
            throws SQLException {
        FirstName answer = null;
        String string = rs.getString(columnIndex);
        if (string != null) {
            answer = new FirstName();
            answer.setValue(string);
        }

        return answer;
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, FirstName parameter,
            JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, jdbcType.TYPE_CODE);
        } else {
            ps.setString(i, parameter.getValue());
        }
    }
}
