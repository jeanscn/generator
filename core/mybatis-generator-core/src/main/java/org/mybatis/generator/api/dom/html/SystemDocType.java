package org.mybatis.generator.api.dom.html;

public class SystemDocType implements DocType {
    private String dtdLocation;

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
