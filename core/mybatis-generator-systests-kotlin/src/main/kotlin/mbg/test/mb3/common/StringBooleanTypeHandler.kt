package mbg.test.mb3.common

import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.TypeHandler

class StringBooleanTypeHandler : TypeHandler<Boolean> {

    override fun getResult(cs: CallableStatement, columnIndex: Int): Boolean =
        cs.getString(columnIndex).isTrue()

    override fun getResult(rs: ResultSet, columnName: String): Boolean =
        rs.getString(columnName).isTrue()

    override fun getResult(rs: ResultSet, columnIndex: Int): Boolean =
        rs.getString(columnIndex).isTrue()

    private fun String?.isTrue(): Boolean = this?.equals("Y")?: false

    override fun setParameter(ps: PreparedStatement, columnIndex: Int, parameter: Boolean?,
                              jdbcType: JdbcType) {
        val s = when(parameter) {
            true -> "Y"
            false -> "N"
            null -> "N"
        }
        ps.setString(columnIndex, s)
    }
}
