package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class TypedPropertyHolder extends PropertyHolder {

    private String configurationType;

    protected TypedPropertyHolder() {
        super();
    }

    /**
     * Sets the value of the type specified in the configuration. If the special
     * value DEFAULT is specified, then the value will be ignored.
     *
     * @param configurationType
     *            the type specified in the configuration
     */
    public void setConfigurationType(String configurationType) {
        if (!"DEFAULT".equalsIgnoreCase(configurationType)) {
            this.configurationType = configurationType;
        }
    }
}
