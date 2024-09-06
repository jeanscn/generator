package org.mybatis.generator.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapstructMappingConfiguration extends TypedPropertyHolder {

    private String sourceType;

    private String targetType;

    private List<String> sourceArguments = new ArrayList<>();

    private List<String> targetArguments = new ArrayList<>();

    private String type = "single";

    private Set<String> ignoreFields = new HashSet<>();

    private boolean ignoreDefault = true;

    private boolean ignoreBusiness = true;

    private  List<String> additionalMappings = new ArrayList<>();

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

    public Set<String> getIgnoreFields() {
        return ignoreFields;
    }

    public void setIgnoreFields(Set<String> ignoreFields) {
        this.ignoreFields = ignoreFields;
    }

    public List<String> getAdditionalMappings() {
        return additionalMappings;
    }

    public void setAdditionalMappings(List<String> additionalMappings) {
        this.additionalMappings = additionalMappings;
    }

    public boolean isIgnoreDefault() {
        return ignoreDefault;
    }

    public void setIgnoreDefault(boolean ignoreDefault) {
        this.ignoreDefault = ignoreDefault;
    }

    public boolean isIgnoreBusiness() {
        return ignoreBusiness;
    }

    public void setIgnoreBusiness(boolean ignoreBusiness) {
        this.ignoreBusiness = ignoreBusiness;
    }
}
