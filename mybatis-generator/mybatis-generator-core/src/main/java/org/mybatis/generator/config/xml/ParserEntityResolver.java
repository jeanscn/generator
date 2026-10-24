package org.mybatis.generator.config.xml;

import org.mybatis.generator.codegen.XmlConstants;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.InputStream;

/**
 * @author cen_c
 */
public class ParserEntityResolver implements EntityResolver {

    public ParserEntityResolver() {
        super();
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
        if (XmlConstants.MYBATIS_GENERATOR_CONFIG_PUBLIC_ID
                .equalsIgnoreCase(publicId)) {
            String dtd = systemId != null && systemId.endsWith("mybatis-generator-config_1_8_0.dtd")
                    ? "mybatis-generator-config_1_8_0.dtd" : "mybatis-generator-config_1_7_0.dtd";
            InputStream is = getClass()
                    .getClassLoader()
                    .getResourceAsStream(
                            "org/mybatis/generator/config/xml/" + dtd);
            return new InputSource(is);
        } else {
            return null;
        }
    }
}
