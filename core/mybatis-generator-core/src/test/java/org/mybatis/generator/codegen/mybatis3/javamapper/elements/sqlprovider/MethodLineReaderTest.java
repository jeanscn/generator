package org.mybatis.generator.codegen.mybatis3.javamapper.elements.sqlprovider;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

class MethodLineReaderTest {

    @Test
    void testTotalLines() {
        List<String> lines = ProviderApplyWhereMethodGenerator.getMethodLines();
        assertEquals(90, lines.size());
        assertEquals("firstCriteria = false;", lines.get(33));
        assertEquals("}", lines.get(89));
    }
}
