package org.mybatis.generator.api.dom.html;

public class PublicDocType implements DocType {
    private String dtdLocation;
    private String dtdName;

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
