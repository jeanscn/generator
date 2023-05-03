package org.mybatis.generator.api.dom.java;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.dom.java.render.TopLevelInterfaceRenderer;

class InterfaceTest {

    @Test
    void testConstructor() {
        Interface interfaze = new Interface("com.foo.UserInterface");
        assertNotNull(interfaze);
    }

    @Test
    void testAddImportedType() {
        Interface interfaze = new Interface("com.foo.UserInterface");
        FullyQualifiedJavaType arrayList = FullyQualifiedJavaType.getNewArrayListInstance();
        interfaze.addImportedType(arrayList);

        assertNotNull(interfaze.getImportedTypes());
        assertEquals(1, interfaze.getImportedTypes().size());
        assertTrue(interfaze.getImportedTypes().contains(arrayList));
    }

    @Test
    void testAddImportedTypes() {
        Interface interfaze = new Interface("com.foo.UserInterface");
        Set<FullyQualifiedJavaType> importedTypes = new HashSet<>();

        FullyQualifiedJavaType arrayList = FullyQualifiedJavaType.getNewArrayListInstance();
        FullyQualifiedJavaType hashMap = FullyQualifiedJavaType.getNewHashMapInstance();

        importedTypes.add(arrayList);
        importedTypes.add(hashMap);
        interfaze.addImportedTypes(importedTypes);

        assertNotNull(interfaze.getImportedTypes());
        assertEquals(2, interfaze.getImportedTypes().size());
        assertTrue(interfaze.getImportedTypes().contains(arrayList));
        assertTrue(interfaze.getImportedTypes().contains(hashMap));
    }

    @Test
    void testAddFileCommentLine() {
        Interface interfaze = new Interface("com.foo.UserInterface");
        interfaze.addFileCommentLine("test");

        assertNotNull(interfaze.getFileCommentLines());
        assertEquals(1, interfaze.getFileCommentLines().size());
        assertEquals("test", interfaze.getFileCommentLines().get(0));
    }

    @Test
    void testAddStaticImport() {
        Interface interfaze = new Interface("com.foo.UserInterface");
        interfaze.addStaticImport("com.foo.StaticUtil");

        assertNotNull(interfaze.getStaticImports());
        assertEquals(1, interfaze.getStaticImports().size());
        assertTrue(interfaze.getStaticImports().contains("com.foo.StaticUtil"));
    }

    @Test
    void testAddStaticImports() {
        Interface interfaze = new Interface("com.foo.UserInterface");
        Set<String> staticImports = new HashSet<>();
        staticImports.add("com.foo.StaticUtil1");
        staticImports.add("com.foo.StaticUtil2");
        interfaze.addStaticImports(staticImports);

        assertNotNull(interfaze.getStaticImports());
        assertEquals(2, interfaze.getStaticImports().size());
        assertTrue(interfaze.getStaticImports().contains("com.foo.StaticUtil1"));
        assertTrue(interfaze.getStaticImports().contains("com.foo.StaticUtil2"));
    }

    @Test
    void testInterfaceFields() {
        Interface interfaze = new Interface("foo.Bar");
        interfaze.setVisibility(JavaVisibility.PUBLIC);

        Field field = new Field("EMPTY_STRING", FullyQualifiedJavaType.getStringInstance());
        field.setInitializationString("\"\"");
        interfaze.addField(field);

        field = new Field("ONE", FullyQualifiedJavaType.getStringInstance());
        field.setInitializationString("\"one\"");
        interfaze.addField(field);

        String expected = "package foo;" + System.getProperty("line.separator")
            + System.getProperty("line.separator")
            + "public interface Bar {" + System.getProperty("line.separator")
            + "    String EMPTY_STRING = \"\";" + System.getProperty("line.separator") + System.getProperty("line.separator")
            + "    String ONE = \"one\";" + System.getProperty("line.separator")
            + "}";

        TopLevelInterfaceRenderer renderer = new TopLevelInterfaceRenderer();
        assertThat(renderer.render(interfaze)).isEqualTo(expected);
    }
}
