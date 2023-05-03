package mbg.test.mb3.common;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Calendar;

import mbg.test.common.MyTime;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

/**
 * @author Jeff Butler
 *
 */
public class MyTimeTypeHandler implements TypeHandler<MyTime> {

    /**
     *
     */
    public MyTimeTypeHandler() {
        super();
    }

    @Override
    public MyTime getResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        MyTime answer = null;
        Time time = cs.getTime(columnIndex);
        if (time != null) {
            answer = new MyTime();

            Calendar c = Calendar.getInstance();
            c.setTime(time);

            answer.setHours(c.get(Calendar.HOUR_OF_DAY));
            answer.setMinutes(c.get(Calendar.MINUTE));
            answer.setSeconds(c.get(Calendar.SECOND));
        }

        return answer;
    }

    @Override
    public MyTime getResult(ResultSet rs, String columnName)
            throws SQLException {
        MyTime answer = null;
        Time time = rs.getTime(columnName);
        if (time != null) {
            answer = new MyTime();

            Calendar c = Calendar.getInstance();
            c.setTime(time);

            answer.setHours(c.get(Calendar.HOUR_OF_DAY));
            answer.setMinutes(c.get(Calendar.MINUTE));
            answer.setSeconds(c.get(Calendar.SECOND));
        }

        return answer;
    }

    @Override
    public MyTime getResult(ResultSet rs, int columnIndex)
            throws SQLException {
        MyTime answer = null;
        Time time = rs.getTime(columnIndex);
        if (time != null) {
            answer = new MyTime();

            Calendar c = Calendar.getInstance();
            c.setTime(time);

            answer.setHours(c.get(Calendar.HOUR_OF_DAY));
            answer.setMinutes(c.get(Calendar.MINUTE));
            answer.setSeconds(c.get(Calendar.SECOND));
        }

        return answer;
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, MyTime parameter,
            JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, jdbcType.TYPE_CODE);
        } else {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, parameter.getHours());
            c.set(Calendar.MINUTE, parameter.getMinutes());
            c.set(Calendar.SECOND, parameter.getSeconds());

            Time time = new Time(c.getTime().getTime());

            ps.setTime(i, time);
        }
    }
}
