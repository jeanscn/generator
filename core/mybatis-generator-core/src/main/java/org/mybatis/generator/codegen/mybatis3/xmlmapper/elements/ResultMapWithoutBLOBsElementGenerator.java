package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import java.util.List;

import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.RelationGeneratorConfiguration;
import org.mybatis.generator.custom.RelationTypeEnum;

import java.util.stream.Collectors;

public class ResultMapWithoutBLOBsElementGenerator extends AbstractXmlElementGenerator {

    private final boolean isSimple;

    public ResultMapWithoutBLOBsElementGenerator(boolean isSimple) {
        super();
        this.isSimple = isSimple;
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("resultMap"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("id", introspectedTable.getBaseResultMapId())); //$NON-NLS-1$

        String returnType;
        if (isSimple) {
            returnType = introspectedTable.getBaseRecordType();
        } else {
            if (introspectedTable.getRules().generateBaseRecordClass()) {
                returnType = introspectedTable.getBaseRecordType();
            } else {
                returnType = introspectedTable.getPrimaryKeyType();
            }
        }

        answer.addAttribute(new Attribute("type", returnType)); //$NON-NLS-1$

        context.getCommentGenerator().addComment(answer);

        if (introspectedTable.isConstructorBased()) {
            addResultMapConstructorElements(answer);
        } else {
            addResultMapElements(answer);
        }

        if (context.getPlugins().sqlMapResultMapWithoutBLOBsElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }

        //根据属性生成联查的ResultMap
        List<RelationGeneratorConfiguration> collect = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                .filter(RelationGeneratorConfiguration::isSubSelected)
                .collect(Collectors.toList());
        if (!collect.isEmpty()) {
            XmlElement resultMapWithRelation = new XmlElement("resultMap"); //$NON-NLS-1$
            resultMapWithRelation.addAttribute(new Attribute("id",introspectedTable.getRelationResultMapId()));
            resultMapWithRelation.addAttribute(new Attribute("extends",introspectedTable.getBaseResultMapId()));
            resultMapWithRelation.addAttribute(new Attribute("type",returnType));
            context.getCommentGenerator().addComment(resultMapWithRelation);
            for (RelationGeneratorConfiguration relationProperty : collect) {
                XmlElement relationElement;
                if (relationProperty.getType().equals(RelationTypeEnum.collection)) {
                    relationElement = new XmlElement("collection");
                    if (relationProperty.getJavaType() != null) {
                        relationElement.addAttribute(new Attribute("javaType",relationProperty.getJavaType()));
                    }else{
                        relationElement.addAttribute(new Attribute("javaType","ArrayList"));
                    }
                    relationElement.addAttribute(new Attribute("ofType",relationProperty.getModelTye()));
                }else{
                    relationElement = new XmlElement("association");
                }
                relationElement.addAttribute(new Attribute("property", relationProperty.getPropertyName()));
                relationElement.addAttribute(new Attribute("select", relationProperty.getSelect()));
                StringBuilder sb = new StringBuilder();
                String alias = introspectedTable.getTableConfiguration().getAlias();
                if (alias != null) {
                    sb.append(alias).append("_");
                }
                sb.append(relationProperty.getColumn());
                relationElement.addAttribute(new Attribute("column",sb.toString()));
                resultMapWithRelation.addElement(relationElement);
            }
            parentElement.addElement(resultMapWithRelation);
        }

        //根据childrenCount属性生成ResultMap
        String aliasPrefix = VStringUtil.stringHasValue(introspectedTable.getTableConfiguration().getAlias())?introspectedTable.getTableConfiguration().getAlias()+"_":"";
        introspectedTable.getColumn(DefaultColumnNameEnum.PARENT_ID.columnName()).ifPresent(introspectedColumn -> {
            XmlElement resultMapWithChildrenCount = new XmlElement("resultMap"); //$NON-NLS-1$
            resultMapWithChildrenCount.addAttribute(new Attribute("id","ResultMapChildrenCount"));
            resultMapWithChildrenCount.addAttribute(new Attribute("extends",introspectedTable.getBaseResultMapId()));
            resultMapWithChildrenCount.addAttribute(new Attribute("type",returnType));
            context.getCommentGenerator().addComment(resultMapWithChildrenCount);
            XmlElement relationElement = new XmlElement("result");
            relationElement.addAttribute(new Attribute("column",aliasPrefix+"children_count"));
            relationElement.addAttribute(new Attribute("property","childrenCount"));
            resultMapWithChildrenCount.addElement(relationElement);
            parentElement.addElement(resultMapWithChildrenCount);
        });
    }

    private void addResultMapElements(XmlElement answer) {
        buildResultMapItems(ResultElementType.ID, introspectedTable.getPrimaryKeyColumns()).forEach(answer::addElement);

        List<IntrospectedColumn> columns;
        if (isSimple) {
            columns = introspectedTable.getNonPrimaryKeyColumns();
        } else {
            columns = introspectedTable.getBaseColumns();
        }

        buildResultMapItems(ResultElementType.RESULT, columns).forEach(answer::addElement);
    }

    private void addResultMapConstructorElements(XmlElement answer) {
        answer.addElement(buildConstructorElement(isSimple));
        /*XmlElement constructor = new XmlElement("constructor"); //$NON-NLS-1$

        for (IntrospectedColumn introspectedColumn : introspectedTable
                .getPrimaryKeyColumns()) {
            XmlElement resultElement = new XmlElement("idArg"); //$NON-NLS-1$

            resultElement.addAttribute(generateColumnAttribute(introspectedColumn));
            resultElement.addAttribute(new Attribute("jdbcType", //$NON-NLS-1$
                    introspectedColumn.getJdbcTypeName()));
            resultElement.addAttribute(new Attribute("javaType", //$NON-NLS-1$
                    introspectedColumn.getFullyQualifiedJavaType()
                            .getFullyQualifiedName()));

            if (stringHasValue(introspectedColumn.getTypeHandler())) {
                resultElement.addAttribute(new Attribute(
                        "typeHandler", introspectedColumn.getTypeHandler())); //$NON-NLS-1$
            }

            constructor.addElement(resultElement);
        }

        List<IntrospectedColumn> columns;
        if (isSimple) {
            columns = introspectedTable.getNonPrimaryKeyColumns();
        } else {
            columns = introspectedTable.getBaseColumns();
        }
        for (IntrospectedColumn introspectedColumn : columns) {
            XmlElement resultElement = new XmlElement("arg"); //$NON-NLS-1$

            resultElement.addAttribute(generateColumnAttribute(introspectedColumn));
            resultElement.addAttribute(new Attribute("jdbcType", //$NON-NLS-1$
                    introspectedColumn.getJdbcTypeName()));
            resultElement.addAttribute(new Attribute("javaType", //$NON-NLS-1$
                    introspectedColumn.getFullyQualifiedJavaType()
                            .getFullyQualifiedName()));

            if (stringHasValue(introspectedColumn.getTypeHandler())) {
                resultElement.addAttribute(new Attribute(
                        "typeHandler", introspectedColumn.getTypeHandler())); //$NON-NLS-1$
            }

            constructor.addElement(resultElement);
        }

        answer.addElement(constructor);*/
    }

    private Attribute generateColumnAttribute(IntrospectedColumn introspectedColumn) {
        return new Attribute("column", //$NON-NLS-1$
                MyBatis3FormattingUtilities.getRenamedColumnNameForResultMap(introspectedColumn));
    }
}
