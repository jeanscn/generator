package mbg.test.mb3;

import static mbg.test.common.util.TestUtilities.createDatabase;

import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Jeff Butler
 *
 */
public abstract class AbstractTest {

    protected SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    public void setUp() throws Exception {
        createDatabase();

        Reader reader = Resources
                    .getResourceAsReader(getMyBatisConfigFile());
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        reader.close();
    }

    public abstract String getMyBatisConfigFile();
}
