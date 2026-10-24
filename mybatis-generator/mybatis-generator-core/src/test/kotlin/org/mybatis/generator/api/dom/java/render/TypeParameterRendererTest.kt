package org.mybatis.generator.api.dom.java.render

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType
import org.mybatis.generator.api.dom.java.TopLevelClass
import org.mybatis.generator.api.dom.java.TypeParameter

class TypeParameterRendererTest {
    @Test
    fun testSimpleTypeParameterRender() {
        val renderer = TypeParameterRenderer()
        val tp = TypeParameter("someName")
        assertThat(renderer.render(tp, null)).isEqualTo("someName")
    }

    @Test
    fun testComplexTypeParameterRenderNoCU() {
        val renderer = TypeParameterRenderer()
	val extendsTypes = listOf(FullyQualifiedJavaType("com.foo.Bar"))
        val tp = TypeParameter("someName", extendsTypes)
        assertThat(renderer.render(tp, null)).isEqualTo("someName extends Bar")
    }

    @Test
    fun testComplexTypeParameterRenderWithCU() {
        val renderer = TypeParameterRenderer()
	val tlc = TopLevelClass(FullyQualifiedJavaType("com.foo.Baz"))
	tlc.addImportedType("com.bar.Foo2")
	val extendsTypes = listOf(FullyQualifiedJavaType("java.lang.String"),
	        FullyQualifiedJavaType("com.foo.Bar"), FullyQualifiedJavaType("com.bar.Foo"),
	        FullyQualifiedJavaType("com.bar.Foo2"))
        val tp = TypeParameter("someName", extendsTypes)
        assertThat(renderer.render(tp, tlc)).isEqualTo("someName extends String & Bar & com.bar.Foo & Foo2")
    }
}
