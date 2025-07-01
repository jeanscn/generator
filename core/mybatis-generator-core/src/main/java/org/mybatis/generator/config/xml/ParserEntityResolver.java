package org.mybatis.generator.config.xml;

import org.mybatis.generator.codegen.XmlConstants;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.InputStream;

public class ParserEntityResolver implements EntityResolver {

    public ParserEntityResolver() {
        super();
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
        if (XmlConstants.MYBATIS_GENERATOR_CONFIG_PUBLIC_ID
                .equalsIgnoreCase(publicId)) {
            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream(
                            "org/mybatis/generator/config/xml/mybatis-generator-config_1_6_0.dtd"); //$NON-NLS-1$
            return new InputSource(is);
        } else {
            return null;
        }
    }
}
