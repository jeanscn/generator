package org.mybatis.generator.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VoAdditionalPropertyGeneratorConfiguration extends TypedPropertyHolder {

    private final TableConfiguration tc;

    private final Context context;

    private String name;

    private String type = "String";

    private List<String> typeArguments = new ArrayList<>();

    private boolean isFinal = false;

    private List<String> annotations  = new ArrayList<>();

    private String initializationString;

    private List<String> importedTypes = new ArrayList<>();

    private String visibility = "private";

    private String remark;


    public VoAdditionalPropertyGeneratorConfiguration(Context context, TableConfiguration tc) {
        super();
        this.context = context;
        this.tc = tc;
    }

    public TableConfiguration getTc() {
        return tc;
    }

    public Context getContext() {
        return context;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getTypeArguments() {
        return typeArguments;
    }

    public void setTypeArguments(List<String> typeArguments) {
        this.typeArguments = typeArguments;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    public String getInitializationString() {
        return initializationString;
    }

    public void setInitializationString(String initializationString) {
        this.initializationString = initializationString;
    }

    public List<String> getImportedTypes() {
        return importedTypes;
    }

    public void setImportedTypes(List<String> importedTypes) {
        this.importedTypes = importedTypes;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VoAdditionalPropertyGeneratorConfiguration)) return false;
        VoAdditionalPropertyGeneratorConfiguration that = (VoAdditionalPropertyGeneratorConfiguration) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
