package mbg.test.mb3.dsql.kotlin.miscellaneous

import mbg.test.mb3.generated.dsql.kotlin.miscellaneous.mapper.*
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory

import mbg.test.common.util.TestUtilities.createDatabase
import org.apache.ibatis.session.SqlSession

abstract class AbstractAnnotatedMiscellaneousTest {

    protected fun openSession(): SqlSession {
        createDatabase()

        val ds = UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "")
        val environment = Environment("test", JdbcTransactionFactory(), ds)
        val config = Configuration(environment)
        config.addMapper(EnumtestMapper::class.java)
        config.addMapper(EnumordinaltestMapper::class.java)
        config.addMapper(GeneratedalwaystestMapper::class.java)
        config.addMapper(GeneratedalwaystestnoupdatesMapper::class.java)
        config.addMapper(MyObjectMapper::class.java)
        config.addMapper(RegexrenameMapper::class.java)
        return SqlSessionFactoryBuilder().build(config).openSession()
    }

    companion object {
        private const val JDBC_URL = "jdbc:hsqldb:mem:aname"
        private const val JDBC_DRIVER = "org.hsqldb.jdbcDriver"
    }
}
