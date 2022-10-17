package org.mybatis.generator.config;

public class MapstructMappingConfiguration extends TypedPropertyHolder {

    private String sourceType;

    private String targetType;

    private String type = "single";

    public MapstructMappingConfiguration() {
        super();
    }

    public MapstructMappingConfiguration(String sourceType,String targetType) {
       super();
       this.sourceType = sourceType;
       this.targetType = targetType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
