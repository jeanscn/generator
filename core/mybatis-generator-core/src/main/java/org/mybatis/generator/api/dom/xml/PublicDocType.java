package org.mybatis.generator.api.dom.xml;

public class PublicDocType implements DocType {
    private final String dtdLocation;
    private final String dtdName;

    public PublicDocType(String dtdName, String dtdLocation) {
        super();
        this.dtdName = dtdName;
        this.dtdLocation = dtdLocation;
    }

    public String getDtdLocation() {
        return dtdLocation;
    }

    public String getDtdName() {
        return dtdName;
    }

    @Override
    public <R> R accept(DocTypeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
