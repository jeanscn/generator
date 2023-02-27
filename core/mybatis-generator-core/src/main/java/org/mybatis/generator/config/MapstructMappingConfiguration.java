package org.mybatis.generator.config;

import java.util.ArrayList;
import java.util.List;

public class MapstructMappingConfiguration extends TypedPropertyHolder {

    private String sourceType;

    private String targetType;

    private List<String> sourceArguments = new ArrayList<>();

    private List<String> targetArguments = new ArrayList<>();

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

    public List<String> getSourceArguments() {
        return sourceArguments;
    }

    public void setSourceArguments(List<String> sourceArguments) {
        this.sourceArguments = sourceArguments;
    }

    public List<String> getTargetArguments() {
        return targetArguments;
    }

    public void setTargetArguments(List<String> targetArguments) {
        this.targetArguments = targetArguments;
    }
}
