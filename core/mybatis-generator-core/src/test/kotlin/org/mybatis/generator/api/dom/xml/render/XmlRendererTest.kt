package org.mybatis.generator.api.dom.xml.render

import org.assertj.core.api.Assertions.assertThat

import org.junit.jupiter.api.Test
import org.mybatis.generator.api.dom.DefaultXmlFormatter
import org.mybatis.generator.api.dom.xml.Attribute
import org.mybatis.generator.api.dom.xml.Document
import org.mybatis.generator.api.dom.xml.TextElement
import org.mybatis.generator.api.dom.xml.XmlElement

class XmlRendererTest {

    @Test
    fun testNoDoctypeAndEmptyRoot() {
        val doc = Document()
        val root = XmlElement("root")
        doc.rootElement = root

        val expected = """
                |<?xml version="1.0" encoding="UTF-8"?>
                |<!DOCTYPE root>
                |<root />""".trimMargin()

        val formatter = DefaultXmlFormatter()
        assertThat(formatter.getFormattedContent(doc)).isEqualToNormalizingNewlines(expected)
    }

    @Test
    fun testSystemDoctypeAndEmptyRoot() {
        val doc = Document("http://somedtd.com")
        val root = XmlElement("root")
        doc.rootElement = root

        val expected = """
                |<?xml version="1.0" encoding="UTF-8"?>
                |<!DOCTYPE root SYSTEM "http://somedtd.com">
                |<root />""".trimMargin()

        val formatter = DefaultXmlFormatter()
        assertThat(formatter.getFormattedContent(doc)).isEqualToNormalizingNewlines(expected)
    }

    @Test
    fun testDoctypeAndEmptyRoot() {
        val doc = Document("--/PublicId", "http://somedtd.com")
        val root = XmlElement("root")
        doc.rootElement = root

        val expected = """
                |<?xml version="1.0" encoding="UTF-8"?>
                |<!DOCTYPE root PUBLIC "--/PublicId" "http://somedtd.com">
                |<root />""".trimMargin()

        val formatter = DefaultXmlFormatter()
        assertThat(formatter.getFormattedContent(doc)).isEqualToNormalizingNewlines(expected)
    }

    @Test
    fun testDoctypeAndRootWithAttribute() {
        val doc = Document("--/PublicId", "http://somedtd.com")
        val root = XmlElement("root")
        root.addAttribute(Attribute("name", "fred"))
        doc.rootElement = root

        val expected = """
                |<?xml version="1.0" encoding="UTF-8"?>
                |<!DOCTYPE root PUBLIC "--/PublicId" "http://somedtd.com">
                |<root name="fred" />""".trimMargin()

        val formatter = DefaultXmlFormatter()
        assertThat(formatter.getFormattedContent(doc)).isEqualToNormalizingNewlines(expected)
    }

    @Test
    fun testDoctypeAndRootWithAttributes() {
        val doc = Document("--/PublicId", "http://somedtd.com")
        val root = XmlElement("root")
        root.addAttribute(Attribute("firstName", "Fred"))
        root.addAttribute(Attribute("lastName", "Flintstone"))
        doc.rootElement = root

        val expected = """
                |<?xml version="1.0" encoding="UTF-8"?>
                |<!DOCTYPE root PUBLIC "--/PublicId" "http://somedtd.com">
                |<root firstName="Fred" lastName="Flintstone" />""".trimMargin()

        val formatter = DefaultXmlFormatter()
        assertThat(formatter.getFormattedContent(doc)).isEqualToNormalizingNewlines(expected)
    }

    @Test
    fun testDoctypeAndRootWithTextChild() {
        val doc = Document("--/PublicId", "http://somedtd.com")
        val root = XmlElement("root")
        root.addAttribute(Attribute("firstName", "Fred"))
        root.addAttribute(Attribute("lastName", "Flintstone"))

        root.addElement(TextElement("some content"))

        doc.rootElement = root

        val expected = """
                |<?xml version="1.0" encoding="UTF-8"?>
                |<!DOCTYPE root PUBLIC "--/PublicId" "http://somedtd.com">
                |<root firstName="Fred" lastName="Flintstone">
                |  some content
                |</root>""".trimMargin()

        val formatter = DefaultXmlFormatter()
        assertThat(formatter.getFormattedContent(doc)).isEqualToNormalizingNewlines(expected)
    }

    @Test
    fun testFullDocument() {
        val doc = Document("--/PublicId", "http://somedtd.com")
        val root = XmlElement("root")
        root.addAttribute(Attribute("firstName", "Fred"))
        root.addAttribute(Attribute("lastName", "Flintstone"))

        root.addElement(TextElement("some content"))

        val child = XmlElement("child")
        child.addAttribute(Attribute("firstName", "Pebbles"))
        child.addAttribute(Attribute("lastName", "Flintstone"))
        root.addElement(child)

        val pet = XmlElement("pet")
        val fn = XmlElement("firstName")
        fn.addElement(TextElement("Dino"))
        pet.addElement(fn)

        val ln = XmlElement("lastName")
        ln.addElement(TextElement("Flintstone"))
        pet.addElement(ln)

        root.addElement(pet)

        doc.rootElement = root

        val expected = """
                |<?xml version="1.0" encoding="UTF-8"?>
                |<!DOCTYPE root PUBLIC "--/PublicId" "http://somedtd.com">
                |<root firstName="Fred" lastName="Flintstone">
                |  some content
                |  <child firstName="Pebbles" lastName="Flintstone" />
                |  <pet>
                |    <firstName>
                |      Dino
                |    </firstName>
                |    <lastName>
                |      Flintstone
                |    </lastName>
                |  </pet>
                |</root>""".trimMargin()

        val formatter = DefaultXmlFormatter()
        assertThat(formatter.getFormattedContent(doc)).isEqualToNormalizingNewlines(expected)
    }
}
