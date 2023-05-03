package org.mybatis.generator.config;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.List;

import org.mybatis.generator.internal.util.StringUtility;

public class ConnectionFactoryConfiguration extends TypedPropertyHolder {

    public ConnectionFactoryConfiguration() {
        super();
    }

    public void validate(List<String> errors) {
        if (getConfigurationType() == null || "DEFAULT".equals(getConfigurationType())) { //$NON-NLS-1$
            if (!StringUtility.stringHasValue(getProperty("driverClass"))) { //$NON-NLS-1$
                errors.add(getString("ValidationError.18", //$NON-NLS-1$
                        "connectionFactory", //$NON-NLS-1$
                        "driverClass")); //$NON-NLS-1$
            }

            if (!StringUtility.stringHasValue(getProperty("connectionURL"))) { //$NON-NLS-1$
                errors.add(getString("ValidationError.18", //$NON-NLS-1$
                        "connectionFactory", //$NON-NLS-1$
                        "connectionURL")); //$NON-NLS-1$
            }
        }
    }
}
