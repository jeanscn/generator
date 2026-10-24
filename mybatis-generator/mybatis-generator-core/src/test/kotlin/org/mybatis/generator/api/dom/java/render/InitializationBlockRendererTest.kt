package org.mybatis.generator.api.dom.java.render

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.generator.api.dom.java.InitializationBlock

class InitializationBlockRendererTest {

    @Test
    fun testStaticBlock() {
        val block = InitializationBlock(true)

        block.addJavaDocLine("/**")
        block.addJavaDocLine(" * Some Javadoc")
        block.addJavaDocLine(" */")
        block.addBodyLine("i = 3;")

        assertThat(toString(block)).isEqualToNormalizingNewlines("""
                |/**
                | * Some Javadoc
                | */
                |static {
                |    i = 3;
                |}
                """.trimMargin())
    }

    @Test
    fun testNonStaticBlock() {
        val block = InitializationBlock()

        block.addJavaDocLine("/**")
        block.addJavaDocLine(" * Some Javadoc")
        block.addJavaDocLine(" */")
        block.addBodyLine("i = 3;")

        assertThat(toString(block)).isEqualToNormalizingNewlines("""
                |/**
                | * Some Javadoc
                | */
                |{
                |    i = 3;
                |}
                """.trimMargin())
    }

    private fun toString(b: InitializationBlock) = InitializationBlockRenderer().render(b)
                .joinToString(System.getProperty("line.separator"))
}
