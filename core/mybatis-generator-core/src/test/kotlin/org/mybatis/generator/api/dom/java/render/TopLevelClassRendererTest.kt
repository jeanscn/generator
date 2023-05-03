package org.mybatis.generator.api.dom.java.render

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.generator.api.dom.java.Field
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType
import org.mybatis.generator.api.dom.java.JavaVisibility
import org.mybatis.generator.api.dom.java.TopLevelClass

class TopLevelClassRendererTest {

    @Test
    fun testGH467() {
        val topLevelClass = TopLevelClass("com.test.GH467")
        topLevelClass.visibility = JavaVisibility.PUBLIC
        topLevelClass.addImportedType(FullyQualifiedJavaType("java.util.List<java.math.BigDecimal>"))

        val field = Field("listVal", FullyQualifiedJavaType("java.util.List<java.math.BigDecimal>"))
        field.visibility = JavaVisibility.PRIVATE
        topLevelClass.addField(field)

        val renderedTlc = TopLevelClassRenderer().render(topLevelClass)
        assertThat(renderedTlc).isEqualToNormalizingNewlines("""
                |package com.test;
                |
                |import java.math.BigDecimal;
                |import java.util.List;
                |
                |public class GH467 {
                |    private List<BigDecimal> listVal;
                |}
                """.trimMargin())
    }
}
