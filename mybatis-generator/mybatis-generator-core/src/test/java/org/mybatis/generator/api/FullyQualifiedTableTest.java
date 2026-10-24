package org.mybatis.generator.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.config.DomainObjectRenamingRule;

class FullyQualifiedTableTest {

    @Test
    void testNormalCase() {
        FullyQualifiedTable fqt = new FullyQualifiedTable(null, "myschema", "mytable", null, null, false, null, null, null, false, null, null);

        assertThat(fqt.getDomainObjectName()).isEqualTo("Mytable");
    }

    @Test
    void testNormalCaseWithPrefix() {
        FullyQualifiedTable fqt = new FullyQualifiedTable(null, "myschema", "sys_mytable", null, null, false, null, null, null, false, null, null);

        assertThat(fqt.getDomainObjectName()).isEqualTo("SysMytable");
    }

    @Test
    void testRenamingRule() {
        DomainObjectRenamingRule renamingRule = new DomainObjectRenamingRule();
        renamingRule.setSearchString("^Sys");
        renamingRule.setReplaceString("");
        FullyQualifiedTable fqt = new FullyQualifiedTable(null, "myschema", "sys_mytable", null, null, false, null, null, null, false, renamingRule, null);

        assertThat(fqt.getDomainObjectName()).isEqualTo("Mytable");
    }

    @Test
    void testRenamingRule2() {
        DomainObjectRenamingRule renamingRule = new DomainObjectRenamingRule();
        renamingRule.setSearchString("^Sys");
        renamingRule.setReplaceString("");
        FullyQualifiedTable fqt = new FullyQualifiedTable(null, "myschema", "sys_my_table", null, null, false, null, null, null, false, renamingRule, null);

        assertThat(fqt.getDomainObjectName()).isEqualTo("MyTable");
    }

    @Test
    void testRenamingRuleNoUnderscore() {
        DomainObjectRenamingRule renamingRule = new DomainObjectRenamingRule();
        renamingRule.setSearchString("^Sys");
        renamingRule.setReplaceString("");
        FullyQualifiedTable fqt = new FullyQualifiedTable(null, "myschema", "sysmytable", null, null, false, null, null, null, false, renamingRule, null);

        assertThat(fqt.getDomainObjectName()).isEqualTo("Mytable");
    }

    @Test
    void testRenamingRuleNoUnderscore2() {
        DomainObjectRenamingRule renamingRule = new DomainObjectRenamingRule();
        renamingRule.setSearchString("^Sys");
        renamingRule.setReplaceString("");
        FullyQualifiedTable fqt = new FullyQualifiedTable(null, "myschema", "sysmy_table", null, null, false, null, null, null, false, renamingRule, null);

        assertThat(fqt.getDomainObjectName()).isEqualTo("MyTable");
    }
}
