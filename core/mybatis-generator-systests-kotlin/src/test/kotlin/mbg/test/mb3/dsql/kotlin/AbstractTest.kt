package mbg.test.mb3.dsql.kotlin

import mbg.test.mb3.generated.dsql.kotlin.mapper.*
import mbg.test.mb3.generated.dsql.kotlin.mapper.mbgtest.IdMapper
import mbg.test.mb3.generated.dsql.kotlin.mapper.mbgtest.TranslationMapper
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory

import mbg.test.common.util.TestUtilities.createDatabase
import org.apache.ibatis.session.SqlSession

/**
 * @author Jeff Butler
 */
abstract class AbstractTest {

    protected fun openSession(): SqlSession {
        createDatabase()

        val ds = UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "")
        val environment = Environment("test", JdbcTransactionFactory(), ds)
        val config = Configuration(environment)
        config.addMapper(AwfulTableMapper::class.java)
        config.addMapper(FieldsblobsMapper::class.java)
        config.addMapper(FieldsonlyMapper::class.java)
        config.addMapper(PkblobsMapper::class.java)
        config.addMapper(PkfieldsblobsMapper::class.java)
        config.addMapper(PkfieldsMapper::class.java)
        config.addMapper(PkonlyMapper::class.java)
        config.addMapper(TranslationMapper::class.java)
        config.addMapper(IdMapper::class.java)
        return SqlSessionFactoryBuilder().build(config).openSession()
    }

    companion object {
        private const val JDBC_URL = "jdbc:hsqldb:mem:aname"
        private const val JDBC_DRIVER = "org.hsqldb.jdbcDriver"
    }
}
