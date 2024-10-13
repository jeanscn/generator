package org.mybatis.generator.api.dom.java;

import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.*;
import java.util.stream.Collectors;

public class TopLevelClass extends InnerClass implements CompilationUnit {

    private final Set<FullyQualifiedJavaType> importedTypes = new TreeSet<>();

    private final Set<String> staticImports = new TreeSet<>();

    private final List<String> fileCommentLines = new ArrayList<>();

    public TopLevelClass(FullyQualifiedJavaType type) {
        super(type);
    }

    public TopLevelClass(String typeName) {
        this(new FullyQualifiedJavaType(typeName));
    }

    @Override
    public Set<FullyQualifiedJavaType> getImportedTypes() {
        return importedTypes;
    }

    public void addImportedType(String importedType) {
        addImportedType(new FullyQualifiedJavaType(importedType));
    }

    @Override
    public void addImportedType(FullyQualifiedJavaType importedType) {
        if (importedType != null
                && importedType.isExplicitlyImported()
                && !importedType.getPackageName().equals(
                getType().getPackageName())
                && !importedType.getShortName().equals(getType().getShortName())) {
            importedTypes.add(importedType);
        }
    }

    @Override
    public void addFileCommentLine(String commentLine) {
        fileCommentLines.add(commentLine);
    }

    @Override
    public List<String> getFileCommentLines() {
        return fileCommentLines;
    }

    @Override
    public void addImportedTypes(Set<FullyQualifiedJavaType> importedTypes) {
        this.importedTypes.addAll(importedTypes);
    }

    @Override
    public Set<String> getStaticImports() {
        return staticImports;
    }

    @Override
    public void addStaticImport(String staticImport) {
        staticImports.add(staticImport);
    }

    @Override
    public void addStaticImports(Set<String> staticImports) {
        this.staticImports.addAll(staticImports);
    }
    @Override
    public List<Field> getAdditionalPropertiesFields(TreeSet<VoAdditionalPropertyGeneratorConfiguration> configurations) {
        List<Field> fields = new ArrayList<>();
        //configurations 去重
        //configurations = configurations.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(VoAdditionalPropertyGeneratorConfiguration::getName))), ArrayList::new));
        configurations.forEach(c -> {
            FullyQualifiedJavaType type = new FullyQualifiedJavaType(c.getType());
            Field field = this.getFields().stream().filter(f -> f.getName().equals(c.getName())).findFirst().orElse(new Field(c.getName(), type));
            this.addImportedType(type);
            field.setVisibility(JavaVisibility.ofCode(c.getVisibility() + " "));
            if (c.isFinal()) {
                field.setFinal(true);
            }
            if (!c.getTypeArguments().isEmpty()) {
                for (String typeArgument : c.getTypeArguments()) {
                    type.addTypeArgument(new FullyQualifiedJavaType(typeArgument));
                    this.addImportedType(typeArgument);
                }
            }
            if (!c.getAnnotations().isEmpty()) {
                c.getAnnotations().forEach(field::addAnnotation);
            }
            c.getInitializationString().ifPresent(field::setInitializationString);
            if (!c.getImportedTypes().isEmpty()) {
                c.getImportedTypes().forEach(this::addImportedType);
            }
            field.setRemark(c.getRemark());
            if (!this.isContainField(field.getName())) {
                fields.add(field);
            }
        });
        return fields;
    }

    public void addSerialVersionUID() {
        Field field = new Field("serialVersionUID", new FullyQualifiedJavaType("long"));
        field.setFinal(true);
        field.setInitializationString("1L");
        field.setStatic(true);
        field.setVisibility(JavaVisibility.PRIVATE);
        this.addField(field);
    }

    @Override
    public <R> R accept(CompilationUnitVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public void addMultipleImports(String... types) {
        for (String type : types) {
            switch (type) {
                case "lombok":
                    this.addImportedType("lombok.*");
                    this.addAnnotation("@Data");
                    this.addAnnotation("@ToString(callSuper = true)");
                    break;
                case "ApiModel":
                    this.addImportedType("io.swagger.annotations.ApiModel");
                    break;
                case "ApiModelProperty":
                    this.addImportedType("io.swagger.annotations.ApiModelProperty");
                    break;
                case "ExcelProperty":
                    this.addImportedType("com.alibaba.excel.annotation.ExcelProperty");
                    break;
                case "ViewTableMeta":
                    this.addImportedType("com.vgosoft.core.annotation.ViewTableMeta");
                    break;
                case "ViewColumnMeta":
                    this.addImportedType("com.vgosoft.core.annotation.ViewColumnMeta");
                    break;
                case "TableField":
                    this.addImportedType("com.baomidou.mybatisplus.annotation.TableField");
                    break;
                case "Valid":
                    this.addImportedType("javax.validation.Valid");
                    break;
                case "ResponseResult":
                    this.addStaticImport("com.vgosoft.core.adapter.web.respone.ResponseResult.*");
                    break;
                default:
                    this.addImportedType(type);
            }
        }
    }
}
