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
        if (introspectedTable.getRules().generateRecordWithBlobsClass()) {
            parameterType = introspectedTable.getRecordWithBlobsType();
        } else {
            parameterType = introspectedTable.getBaseRecordType();
        }

        XmlElement answer = buildUpdateByPrimaryKeyElement(
                introspectedTable.getUpdateByPrimaryKeyWithBlobsStatementId(),
                parameterType,
                introspectedTable.getNonPrimaryKeyColumns());

        if (context.getPlugins().sqlMapUpdateByPrimaryKeyWithBlobsElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
