package org.mybatis.generator.codegen.mybatis3.xmlmapper;

import org.mybatis.generator.api.dom.xml.XmlElement;

public class MixedMapperGenerator extends XMLMapperGenerator {

    @Override
    protected void addSelectByPrimaryKeyElement(XmlElement parentElement) {
        // turn off this element in the mixed mapper
    }

    @Override
    protected void addDeleteByPrimaryKeyElement(XmlElement parentElement) {
        // turn off this element in the mixed mapper
    }

    @Override
    protected void addInsertElement(XmlElement parentElement) {
        // turn off this element in the mixed mapper
    }

    @Override
    protected void addUpdateByPrimaryKeyWithBLOBsElement(
            XmlElement parentElement) {
        // turn off this element in the mixed mapper
    }

    @Override
    protected void addUpdateByPrimaryKeyWithoutBLOBsElement(
            XmlElement parentElement) {
        // turn off this element in the mixed mapper
    }
}
