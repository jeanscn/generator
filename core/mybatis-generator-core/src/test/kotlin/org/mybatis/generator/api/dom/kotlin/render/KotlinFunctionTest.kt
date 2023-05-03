package org.mybatis.generator.api.dom.kotlin.render

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.generator.api.dom.kotlin.KotlinArg
import org.mybatis.generator.api.dom.kotlin.KotlinFunction
import java.util.stream.Collectors

class KotlinFunctionTest {
    @Test
    fun testMultilineFunction() {
        val kf = KotlinFunction.newMultiLineFunction("add")
                .withArgument(KotlinArg.newArg("a").withDataType("Int").build())
                .withArgument(KotlinArg.newArg("b").withDataType("Int").build())
                .withExplicitReturnType("Int")
                .withCodeLine("val answer = a + b")
                .withCodeLine("return answer")
                .build()

        val renderedFunction = KotlinFunctionRenderer().render(kf).stream()
                .collect(Collectors.joining(System.getProperty("line.separator")))

        assertThat(renderedFunction).isEqualToNormalizingNewlines("""
            |fun add(a: Int, b: Int): Int {
            |    val answer = a + b
            |    return answer
            |}
            """.trimMargin())
    }

    @Test
    fun testMultilineFunctionWithDefaultValue() {
        val kf = KotlinFunction.newMultiLineFunction("add")
                .withArgument(KotlinArg.newArg("a").withDataType("Int").withInitializationString("1").build())
                .withArgument(KotlinArg.newArg("b").withDataType("Int").withInitializationString(("2")).build())
                .withExplicitReturnType("Int")
                .withCodeLine("val answer = a + b")
                .withCodeLine("return answer")
                .build()

        val renderedFunction = KotlinFunctionRenderer().render(kf).stream()
                .collect(Collectors.joining(System.getProperty("line.separator")))

        assertThat(renderedFunction).isEqualToNormalizingNewlines("""
            |fun add(a: Int = 1, b: Int = 2): Int {
            |    val answer = a + b
            |    return answer
            |}
            """.trimMargin())
    }

    @Test
    fun testMultilineFunctionWithAnnotation() {
        val kf = KotlinFunction.newMultiLineFunction("add")
                .withArgument(KotlinArg.newArg("a").withDataType("Int").withInitializationString("1")
                        .withAnnotation("@Param(\"a\")")
                        .build())
                .withArgument(KotlinArg.newArg("b").withDataType("Int").withInitializationString(("2")).build())
                .withExplicitReturnType("Int")
                .withCodeLine("val answer = a + b")
                .withCodeLine("return answer")
                .build()

        val renderedFunction = KotlinFunctionRenderer().render(kf).stream()
                .collect(Collectors.joining(System.getProperty("line.separator")))

        assertThat(renderedFunction).isEqualToNormalizingNewlines("""
            |fun add(@Param("a") a: Int = 1, b: Int = 2): Int {
            |    val answer = a + b
            |    return answer
            |}
            """.trimMargin())
    }
}
