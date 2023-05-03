package mbg.test.mb3.common

import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Time
import java.util.Calendar

import mbg.test.common.MyTime

import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.TypeHandler

/**
 * @author Jeff Butler
 */
/**
 *
 */
class MyTimeTypeHandler : TypeHandler<MyTime> {

    override fun getResult(cs: CallableStatement, columnIndex: Int): MyTime? =
        cs.getTime(columnIndex)?.toMyTime()

    override fun getResult(rs: ResultSet, columnName: String): MyTime? =
        rs.getTime(columnName)?.toMyTime()

    override fun getResult(rs: ResultSet, columnIndex: Int): MyTime? =
        rs.getTime(columnIndex)?.toMyTime()

    private fun Time.toMyTime(): MyTime =
        MyTime().also {
            val c = Calendar.getInstance()
            c.time = this

            it.hours = c.get(Calendar.HOUR_OF_DAY)
            it.minutes = c.get(Calendar.MINUTE)
            it.seconds = c.get(Calendar.SECOND)
        }

    override fun setParameter(ps: PreparedStatement, i: Int, parameter: MyTime?,
                              jdbcType: JdbcType) {
        if (parameter == null) {
            ps.setNull(i, jdbcType.TYPE_CODE)
        } else {
            with(Calendar.getInstance()) {
                set(Calendar.HOUR_OF_DAY, parameter.hours)
                set(Calendar.MINUTE, parameter.minutes)
                set(Calendar.SECOND, parameter.seconds)
                ps.setTime(i, Time(timeInMillis))
            }
        }
    }
}
