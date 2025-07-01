package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class ResultMapWithBLOBsElementGenerator extends AbstractXmlElementGenerator {

    public ResultMapWithBLOBsElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("resultMap"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", //$NON-NLS-1$
                introspectedTable.getResultMapWithBLOBsId()));

        String returnType;
        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            returnType = introspectedTable.getRecordWithBLOBsType();
        } else {
            // table has BLOBs, but no BLOB class - BLOB fields must be
            // in the base class
            returnType = introspectedTable.getBaseRecordType();
        }

        answer.addAttribute(new Attribute("type", returnType)); //$NON-NLS-1$

        if (!introspectedTable.isConstructorBased()) {
            if (!introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().isEmpty()) {
                answer.addAttribute(new Attribute("extends",introspectedTable.getRelationResultMapId()));
            }else{
                answer.addAttribute(new Attribute("extends",introspectedTable.getBaseResultMapId()));
            }
        }

        context.getCommentGenerator().addComment(answer);

        if (introspectedTable.isConstructorBased()) {
            addResultMapConstructorElements(answer);
        } else {
            addResultMapElements(answer);
        }

        if (context.getPlugins().sqlMapResultMapWithBLOBsElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }

    private void addResultMapElements(XmlElement answer) {
        buildResultMapItems(ResultElementType.RESULT, introspectedTable.getBLOBColumns()).forEach(answer::addElement);
    }

    private void addResultMapConstructorElements(XmlElement answer) {
        answer.addElement(buildConstructorElement(true));
    }
}
