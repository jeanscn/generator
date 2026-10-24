package org.mybatis.generator.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StringUtilityTest {

    @Test
    void testNoCatalog() {
        String answer = StringUtility.composeFullyQualifiedTableName(null, "schema", "table", '.');
        assertEquals("schema.table", answer);
    }

    @Test
    void testNoSchema() {
        String answer = StringUtility.composeFullyQualifiedTableName("catalog", null, "table", '.');
        assertEquals("catalog..table", answer);
    }

    @Test
    void testAllPresent() {
        String answer = StringUtility.composeFullyQualifiedTableName("catalog", "schema", "table", '.');
        assertEquals("catalog.schema.table", answer);
    }

    @Test
    void testTableOnly() {
        String answer = StringUtility.composeFullyQualifiedTableName(null, null, "table", '.');
        assertEquals("table", answer);
    }
}
