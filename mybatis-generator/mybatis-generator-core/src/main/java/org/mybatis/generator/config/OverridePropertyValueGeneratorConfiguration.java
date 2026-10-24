package org.mybatis.generator.config;

import com.vgosoft.core.constant.GlobalConstant;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author cen_c
 */
public class OverridePropertyValueGeneratorConfiguration extends TypedPropertyHolder {

    @Getter
    private final TableConfiguration tc;

    @Getter
    private final Context context;

    @Getter
    private final String sourceColumnName;

    @Setter
    @Getter
    private String targetColumnName;

    @Setter
    @Getter
    private String targetPropertyName;

    @Setter
    private String targetPropertyType;

    @Setter
    @Getter
    private String typeValue;

    @Setter
    @Getter
    private String annotationType;

    @Setter
    @Getter
    private String beanName;

    @Setter
    @Getter
    private String applyProperty = GlobalConstant.CACHE_PO_DEFAULT_VALUE_TEXT;

    @Setter
    @Getter
    private String remark;

    @Setter
    private String initializationString;

    @Getter
    private final Set<String> importTypes = new HashSet<>();

    @Setter
    @Getter
    private String enumClassName;

    @Setter
    @Getter
    private HtmlElementDescriptor elementDescriptor;


    public OverridePropertyValueGeneratorConfiguration(Context context, TableConfiguration tc,String sourceColumnNames) {
        super();
        this.context = context;
        this.tc = tc;
        this.sourceColumnName = sourceColumnNames;
    }

    public String getTargetPropertyType() {
        return targetPropertyType==null? FullyQualifiedJavaType.getStringInstance().getFullyQualifiedName():targetPropertyType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof OverridePropertyValueGeneratorConfiguration that)) { return false;}
        return Objects.equals(getSourceColumnName(), that.getSourceColumnName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSourceColumnName());
    }

    public Optional<String> getInitializationString() {
        return Optional.ofNullable(initializationString);
    }

    public void addImportTypes(String importType) {
        this.importTypes.add(importType);
    }

}
