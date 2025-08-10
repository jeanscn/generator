package org.mybatis.generator.config;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.List;

import org.mybatis.generator.internal.util.StringUtility;

/**
 * @author cen_c
 */
public class ConnectionFactoryConfiguration extends TypedPropertyHolder {

    public ConnectionFactoryConfiguration() {
        super();
    }

    public void validate(List<String> errors) {
        if (getConfigurationType() == null || "DEFAULT".equals(getConfigurationType())) {
            if (!StringUtility.stringHasValue(getProperty("driverClass"))) {
                errors.add(getString("ValidationError.18",
                        "connectionFactory",
                        "driverClass"));
            }

            if (!StringUtility.stringHasValue(getProperty("connectionURL"))) {
                errors.add(getString("ValidationError.18",
                        "connectionFactory",
                        "connectionURL"));
            }
        }
    }
}
