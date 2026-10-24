package org.mybatis.generator.api.dom.java.render

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.generator.api.dom.java.Field
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType
import org.mybatis.generator.api.dom.java.JavaVisibility

class FieldRendererTest {
    @Test
    fun testBasicField() {
        val f = Field("name", FullyQualifiedJavaType.getStringInstance())
        assertThat(toString(f)).isEqualTo("String name;")
    }

    @Test
    fun testBasicFieldWithInitializationString() {
        val f = Field("name", FullyQualifiedJavaType.getStringInstance())
        f.setInitializationString(""""Fred"""")
        assertThat(toString(f)).isEqualTo("""String name = "Fred";""")
    }

    @Test
    fun testPrivateFieldWithInitializationString() {
        val f = Field("name", FullyQualifiedJavaType.getStringInstance())
        f.visibility = JavaVisibility.PRIVATE
        f.setInitializationString(""""Fred"""")
        assertThat(toString(f)).isEqualTo("""private String name = "Fred";""")
    }

    @Test
    fun testPrivateStaticFieldWithInitializationString() {
        val f = Field("name", FullyQualifiedJavaType.getStringInstance())
        f.visibility = JavaVisibility.PRIVATE
        f.isStatic = true
        f.isFinal = true
        f.setInitializationString(""""Fred"""")
        assertThat(toString(f)).isEqualTo("""private static final String name = "Fred";""")
    }

    @Test
    fun testPrivateTransientFieldWithInitializationString() {
        val f = Field("name", FullyQualifiedJavaType.getStringInstance())
        f.visibility = JavaVisibility.PRIVATE
        f.isTransient = true
        f.isVolatile = true
        f.setInitializationString(""""Fred"""")
        assertThat(toString(f)).isEqualTo("""private transient volatile String name = "Fred";""")
    }

    @Test
    fun testPrivateFieldWithInitializationStringJavadocAndAnnotations() {
        val f = Field("name", FullyQualifiedJavaType.getStringInstance())
        f.visibility = JavaVisibility.PRIVATE
        f.setInitializationString(""""Fred"""")

        f.addAnnotation("@Generated")
        f.addJavaDocLine("/**")
        f.addJavaDocLine(" * Some Javadoc")
        f.addJavaDocLine(" */")

        assertThat(toString(f)).isEqualToNormalizingNewlines("""
                |/**
                | * Some Javadoc
                | */
                |@Generated
                |private String name = "Fred";
                """.trimMargin())
    }

    private fun toString(f: Field) = FieldRenderer().render(f, null)
                .joinToString(System.getProperty("line.separator"))
}
