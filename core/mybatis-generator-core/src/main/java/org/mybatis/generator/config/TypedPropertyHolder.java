package org.mybatis.generator.config;

public abstract class TypedPropertyHolder extends PropertyHolder {

    private String configurationType;

    protected TypedPropertyHolder() {
        super();
    }

    public String getConfigurationType() {
        return configurationType;
    }

    /**
     * Sets the value of the type specified in the configuration. If the special
     * value DEFAULT is specified, then the value will be ignored.
     *
     * @param configurationType
     *            the type specified in the configuration
     */
    public void setConfigurationType(String configurationType) {
        if (!"DEFAULT".equalsIgnoreCase(configurationType)) { //$NON-NLS-1$
            this.configurationType = configurationType;
        }
    }
}
