package org.mybatis.generator.api.dom.xml;

public class SystemDocType implements DocType {
    private final String dtdLocation;

    public SystemDocType(String dtdLocation) {
        super();
        this.dtdLocation = dtdLocation;
    }

    public String getDtdLocation() {
        return dtdLocation;
    }

    @Override
    public <R> R accept(DocTypeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
