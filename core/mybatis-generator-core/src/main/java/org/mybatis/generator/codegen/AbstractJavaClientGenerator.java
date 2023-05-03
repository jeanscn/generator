package org.mybatis.generator.codegen;

/**
 * This class exists to that Java client generators can specify whether
 * an XML generator is required to match the methods in the
 * Java client.  For example, a Java client built entirely with
 * annotations does not need matching XML.
 *
 * @author Jeff Butler
 *
 */
public abstract class AbstractJavaClientGenerator extends AbstractJavaGenerator {

    private final boolean requiresXMLGenerator;

    protected AbstractJavaClientGenerator(String project, boolean requiresXMLGenerator) {
        super(project);
        this.requiresXMLGenerator = requiresXMLGenerator;
    }

    /**
     * Returns true is a matching XML generator is required.
     *
     * @return true if matching XML is generator required
     */
    public boolean requiresXMLGenerator() {
        return requiresXMLGenerator;
    }

    /**
     * Returns an instance of the XML generator associated
     * with this client generator.
     *
     * @return the matched XML generator.  May return null if no
     *     XML is required by this generator
     */
    public abstract AbstractXmlGenerator getMatchedXMLGenerator();

    public abstract AbstractHtmlGenerator getMatchedHTMLGenerator();
}
