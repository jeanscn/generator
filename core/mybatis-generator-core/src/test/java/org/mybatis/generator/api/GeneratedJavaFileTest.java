package org.mybatis.generator.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;

class GeneratedJavaFileTest {

    @Test
    void testReqularInterface() {
        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType("org.mybatis.test.TestInterface");
        Interface ifc = new Interface(fqjt);
        JavaFormatter jf = new DefaultJavaFormatter();
        GeneratedJavaFile gjf = new GeneratedJavaFile(ifc, "src", jf);

        assertThat(gjf.getFileName()).isEqualTo("TestInterface.java");
        assertThat(gjf.getTargetPackage()).isEqualTo("org.mybatis.test");
    }

    @Test
    void testGenericInterface() {
        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType("org.mybatis.test.TestInterface");
        fqjt.addTypeArgument(new FullyQualifiedJavaType("T"));
        Interface ifc = new Interface(fqjt);
        JavaFormatter jf = new DefaultJavaFormatter();
        GeneratedJavaFile gjf = new GeneratedJavaFile(ifc, "src", jf);

        assertThat(gjf.getFileName()).isEqualTo("TestInterface.java");
        assertThat(gjf.getTargetPackage()).isEqualTo("org.mybatis.test");
    }
}
