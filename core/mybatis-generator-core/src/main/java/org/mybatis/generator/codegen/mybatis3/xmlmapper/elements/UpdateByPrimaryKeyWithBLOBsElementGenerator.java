package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.XmlElement;

public class UpdateByPrimaryKeyWithBLOBsElementGenerator extends
        AbstractXmlElementGenerator {

    public UpdateByPrimaryKeyWithBLOBsElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        String parameterType;
        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            parameterType = introspectedTable.getRecordWithBLOBsType();
        } else {
            parameterType = introspectedTable.getBaseRecordType();
        }

        XmlElement answer = buildUpdateByPrimaryKeyElement(
                introspectedTable.getUpdateByPrimaryKeyWithBLOBsStatementId(),
                parameterType,
                introspectedTable.getNonPrimaryKeyColumns());

        if (context.getPlugins().sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
