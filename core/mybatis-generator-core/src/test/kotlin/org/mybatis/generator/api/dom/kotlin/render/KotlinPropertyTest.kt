package org.mybatis.generator.api.dom.kotlin.render

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.generator.api.dom.kotlin.KotlinModifier
import org.mybatis.generator.api.dom.kotlin.KotlinProperty
import java.util.stream.Collectors

class KotlinPropertyTest {
    @Test
    fun testVal() {
        val obj = KotlinProperty.newVal("id")
                .withDataType("Int?")
                .build()

        val renderedObj = KotlinPropertyRenderer().render(obj).stream()
                .collect(Collectors.joining(System.getProperty("line.separator")))

        assertThat(renderedObj).isEqualTo("val id: Int?")
    }

    @Test
    fun testValWithInitializer() {
        val obj = KotlinProperty.newVal("id")
                .withDataType("Int?")
                .withInitializationString("null")
                .build()

        val renderedObj = KotlinPropertyRenderer().render(obj).stream()
                .collect(Collectors.joining(System.getProperty("line.separator")))

        assertThat(renderedObj).isEqualTo("val id: Int? = null")
    }

    @Test
    fun testPrivateVal() {
        val obj = KotlinProperty.newVal("id")
                .withDataType("Int?")
                .withModifier(KotlinModifier.PRIVATE)
                .build()

        val renderedObj = KotlinPropertyRenderer().render(obj).stream()
                .collect(Collectors.joining(System.getProperty("line.separator")))

        assertThat(renderedObj).isEqualTo("private val id: Int?")
    }

    @Test
    fun testVar() {
        val obj = KotlinProperty.newVar("id")
                .withDataType("Int?")
                .build()

        val renderedObj = KotlinPropertyRenderer().render(obj).stream()
                .collect(Collectors.joining(System.getProperty("line.separator")))

        assertThat(renderedObj).isEqualTo("var id: Int?")
    }

    @Test
    fun testVarWithInitializer() {
        val obj = KotlinProperty.newVar("id")
                .withDataType("Int?")
                .withInitializationString("null")
                .build()

        val renderedObj = KotlinPropertyRenderer().render(obj).stream()
                .collect(Collectors.joining(System.getProperty("line.separator")))

        assertThat(renderedObj).isEqualTo("var id: Int? = null")
    }

    @Test
    fun testPrivateVar() {
        val obj = KotlinProperty.newVar("id")
                .withDataType("Int?")
                .withModifier(KotlinModifier.PRIVATE)
                .build()

        val renderedObj = KotlinPropertyRenderer().render(obj).stream()
                .collect(Collectors.joining(System.getProperty("line.separator")))

        assertThat(renderedObj).isEqualTo("private var id: Int?")
    }

    @Test
    fun testNoDataType() {
        val obj = KotlinProperty.newVal("id")
                .withInitializationString("33")
                .build()

        val renderedObj = KotlinPropertyRenderer().render(obj).stream()
                .collect(Collectors.joining(System.getProperty("line.separator")))

        assertThat(renderedObj).isEqualTo("val id = 33")
    }

    @Test
    fun testAnnotation() {
        val obj = KotlinProperty.newVal("id")
                .withInitializationString("33")
                .withAnnotation("@SomeAnnotation")
                .build()

        val renderedObj = KotlinPropertyRenderer().render(obj).stream()
                .collect(Collectors.joining(System.getProperty("line.separator")))

        assertThat(renderedObj).isEqualToNormalizingNewlines("""
            |@SomeAnnotation
            |val id = 33
        """.trimMargin())
    }
}
