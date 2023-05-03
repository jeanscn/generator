package mbg.test.mb3.common

import mbg.test.common.FirstName
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.TypeHandler
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * @author Jeff Butler
 */
class FirstNameTypeHandler : TypeHandler<FirstName> {

    override fun getResult(cs: CallableStatement, columnIndex: Int): FirstName? =
        cs.getString(columnIndex)?.toFirstName()

    override fun getResult(rs: ResultSet, columnName: String): FirstName? =
        rs.getString(columnName)?.toFirstName()

    override fun getResult(rs: ResultSet, columnIndex: Int): FirstName? =
        rs.getString(columnIndex)?.toFirstName()

    private fun String.toFirstName(): FirstName =
        FirstName().also {
            it.value = this
        }

    override fun setParameter(ps: PreparedStatement, i: Int, parameter: FirstName?,
                              jdbcType: JdbcType) {
        if (parameter == null) {
            ps.setNull(i, jdbcType.TYPE_CODE)
        } else {
            ps.setString(i, parameter.value)
        }
    }
}
