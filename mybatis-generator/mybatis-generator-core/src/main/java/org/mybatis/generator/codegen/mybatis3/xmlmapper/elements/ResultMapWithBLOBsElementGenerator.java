package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * @author cen_c
 */
public class ResultMapWithBLOBsElementGenerator extends AbstractXmlElementGenerator {

    public ResultMapWithBLOBsElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("resultMap");

        answer.addAttribute(new Attribute("id",
                introspectedTable.getResultMapWithBlobsId()));

        String returnType;
        if (introspectedTable.getRules().generateRecordWithBlobsClass()) {
            returnType = introspectedTable.getRecordWithBlobsType();
        } else {
            // table has BLOBs, but no BLOB class - BLOB fields must be
            // in the base class
            returnType = introspectedTable.getBaseRecordType();
        }

        answer.addAttribute(new Attribute("type", returnType));

        if (!introspectedTable.isConstructorBased()) {
            if (!introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().isEmpty()) {
                answer.addAttribute(new Attribute("extends", introspectedTable.getRelationResultMapId()));
            }else{
                answer.addAttribute(new Attribute("extends", introspectedTable.getBaseResultMapId()));
            }
        }

        context.getCommentGenerator().addComment(answer);

        if (introspectedTable.isConstructorBased()) {
            addResultMapConstructorElements(answer);
        } else {
            addResultMapElements(answer);
        }

        if (context.getPlugins().sqlMapResultMapWithBlobsElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }

    private void addResultMapElements(XmlElement answer) {
        buildResultMapItems(ResultElementType.RESULT, introspectedTable.getBlobColumns()).forEach(answer::addElement);
    }

    private void addResultMapConstructorElements(XmlElement answer) {
        answer.addElement(buildConstructorElement(true));
    }
}
