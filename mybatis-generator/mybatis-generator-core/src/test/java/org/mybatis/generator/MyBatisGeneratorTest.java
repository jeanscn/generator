package org.mybatis.generator;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.mybatis.generate.GenerateSqlTemplate;
import com.vgosoft.mybatis.sqlbuilder.InsertSqlBuilder;
import com.vgosoft.tool.core.VMD5Util;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.ConnectionFactoryConfiguration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.internal.DefaultShellCallback;

class MyBatisGeneratorTest {

    @Test
    void testGenerateMyBatis3WithInvalidConfig() throws Exception {
        List<String> warnings = new ArrayList<>();
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(this.getClass().getClassLoader().getResourceAsStream("generatorConfigMyBatis3_badConfig.xml"));

        DefaultShellCallback shellCallback = new DefaultShellCallback(true);

        assertDoesNotThrow(() -> {
            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, shellCallback, warnings);
            myBatisGenerator.generate(null, null, null, false);
        });
    }

    @Test
    void testGenerateInvalidConfigWithNoConnectionSources() {
        List<String> warnings = new ArrayList<>();
        Configuration config = new Configuration();
        Context context = new Context(ModelType.HIERARCHICAL);
        context.setId("MyContext");
        config.addContext(context);

        DefaultShellCallback shellCallback = new DefaultShellCallback(true);

        InvalidConfigurationException e =
                assertThrows(InvalidConfigurationException.class, () -> {
                    MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, shellCallback, warnings);
                    myBatisGenerator.generate(null, null, null, false);
                });
        assertEquals(3, e.getErrors().size());
    }

    @Test
    void testGenerateInvalidConfigWithTwoConnectionSources() {
        List<String> warnings = new ArrayList<>();
        Configuration config = new Configuration();
        Context context = new Context(ModelType.HIERARCHICAL);
        context.setId("MyContext");
        context.setConnectionFactoryConfiguration(new ConnectionFactoryConfiguration());
        context.setJdbcConnectionConfiguration(new JDBCConnectionConfiguration());
        config.addContext(context);

        DefaultShellCallback shellCallback = new DefaultShellCallback(true);

        InvalidConfigurationException e =
                assertThrows(InvalidConfigurationException.class, () -> {
                    MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, shellCallback, warnings);
                    myBatisGenerator.generate(null, null, null, false);
                });
        assertEquals(3, e.getErrors().size());
    }

    @Disabled
    @Test
    void testGenerateInitDataInsertSqlForPermission() {
        String code = "organization:authorizationdiagnosticimpl:roleremovalpreview";
        String name = "预览移除角色后的授权影响";
        String parentId = "100000001516504231";
        String id = VMD5Util.MD5_15(code);
        InsertSqlBuilder sqlBuilder = GenerateSqlTemplate.insertSqlForPermission();
        sqlBuilder.updateStringValues("id_", id);
        sqlBuilder.updateValues("sort_", "10");
        sqlBuilder.updateStringValues("parent_id", parentId);
        sqlBuilder.updateStringValues("code_", code);
        sqlBuilder.updateStringValues("name_", name);
        sqlBuilder.updateStringValues("created_id", GlobalConstant.DEFAULT_SYSTEM_ADMIN_ID);
        sqlBuilder.updateStringValues("modified_id", GlobalConstant.DEFAULT_SYSTEM_ADMIN_ID);
        String sql = sqlBuilder.toSql();
        System.out.println(sql);
    }
}
