package org.mybatis.generator.api.dom.java;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.annotations.IAnnotation;

import java.text.MessageFormat;
import java.util.*;

@Getter
@Setter
public class Method extends JavaElement {

    private final List<String> bodyLines = new ArrayList<>();

    private boolean constructor;

    private FullyQualifiedJavaType returnType;

    private String name;

    private final List<TypeParameter> typeParameters = new ArrayList<>();

    private final List<Parameter> parameters = new ArrayList<>();

    private final List<FullyQualifiedJavaType> exceptions = new ArrayList<>();

    private boolean isSynchronized;

    private boolean isNative;

    private boolean isDefault;

    private boolean isAbstract;

    private boolean isFinal;

    private String returnRemark;

    private String exceptionRemark;

    public Method(String name) {
        this.name = name;
    }

    /**
     * Copy constructor. Not a truly deep copy, but close enough for most purposes.
     *
     * @param original
     *            the original
     */
    public Method(Method original) {
        super(original);
        this.bodyLines.addAll(original.bodyLines);
        this.constructor = original.constructor;
        this.exceptions.addAll(original.exceptions);
        this.name = original.name;
        this.typeParameters.addAll(original.typeParameters);
        this.parameters.addAll(original.parameters);
        this.returnType = original.returnType;
        this.isNative = original.isNative;
        this.isSynchronized = original.isSynchronized;
        this.isDefault = original.isDefault;
        this.isAbstract = original.isAbstract;
        this.isFinal = original.isFinal;
    }

    public void addBodyLine(String line) {
        bodyLines.add(line);
    }

    public void addBodyLine(String pattern, Object ... arguments){
        addBodyLine(MessageFormat.format(pattern, arguments));
    }

    public void addBodyLine(int index, String line) {
        bodyLines.add(index, line);
    }

    public void addBodyLines(Collection<String> lines) {
        bodyLines.addAll(lines);
    }

    public void addBodyLines(int index, Collection<String> lines) {
        bodyLines.addAll(index, lines);
    }

    public void addTypeParameter(TypeParameter typeParameter) {
        typeParameters.add(typeParameter);
    }

    public void addTypeParameter(int index, TypeParameter typeParameter) {
        typeParameters.add(index, typeParameter);
    }

    public void addParameter(Parameter parameter) {
        parameters.add(parameter);
    }

    public void addParameter(int index, Parameter parameter) {
        parameters.add(index, parameter);
    }

    public Optional<FullyQualifiedJavaType> getReturnType() {
        return Optional.ofNullable(returnType);
    }

    public void addException(FullyQualifiedJavaType exception) {
        exceptions.add(exception);
    }

    public boolean isSynchronized() {
        return isSynchronized;
    }

    public void setSynchronized(boolean isSynchronized) {
        this.isSynchronized = isSynchronized;
    }

    public boolean isNative() {
        return isNative;
    }

    public void setNative(boolean isNative) {
        this.isNative = isNative;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public Optional<String> getExceptionRemark() {
        return Optional.ofNullable(exceptionRemark);
    }

    public void addAnnotation(IAnnotation annotation, TopLevelClass parent){
        annotation.toAnnotations().forEach(this::addAnnotation);
        parent.addImportedTypes(annotation.getImportedTypes());
    }
}
