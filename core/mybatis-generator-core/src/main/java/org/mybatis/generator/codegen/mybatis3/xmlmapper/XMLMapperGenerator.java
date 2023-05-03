package org.mybatis.generator.codegen.mybatis3.xmlmapper;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.AbstractXmlGenerator;
import org.mybatis.generator.codegen.XmlConstants;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.*;
import org.mybatis.generator.config.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.config.SelectBySqlMethodGeneratorConfiguration;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;

public class XMLMapperGenerator extends AbstractXmlGenerator {

    public XMLMapperGenerator() {
        super();
    }

    protected XmlElement getSqlMapElement() {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.12", table.toString())); //$NON-NLS-1$
        XmlElement answer = new XmlElement("mapper"); //$NON-NLS-1$
        String namespace = introspectedTable.getMyBatis3SqlMapNamespace();
        answer.addAttribute(new Attribute("namespace", namespace)); //$NON-NLS-1$

        context.getCommentGenerator().addRootComment(answer);

        //默认添加
        addResultMapWithoutBLOBsElement(answer);
        addResultMapWithBLOBsElement(answer);
        addExampleWhereClauseElement(answer);
        addMyBatis3UpdateByExampleWhereClauseElement(answer);
        addBaseColumnListElement(answer);
        addBlobColumnListElement(answer);
        addSelectByExampleWithBLOBsElement(answer);
        addSelectByExampleWithoutBLOBsElement(answer);
        addSelectByPrimaryKeyElement(answer);
        addDeleteByPrimaryKeyElement(answer);
        addDeleteByExampleElement(answer);
        addInsertElement(answer);
        addInsertSelectiveElement(answer);
        addCountByExampleElement(answer);
        addUpdateByExampleSelectiveElement(answer);
        addUpdateByExampleWithBLOBsElement(answer);
        addUpdateByExampleWithoutBLOBsElement(answer);
        addUpdateByPrimaryKeySelectiveElement(answer);
        addUpdateByPrimaryKeyWithBLOBsElement(answer);
        addUpdateByPrimaryKeyWithoutBLOBsElement(answer);

        //定制追加
        addUpdateBatchByPrimaryKeyElement(answer);
        addInsertOrUpdateSelectiveElement(answer);
        addInsertBatchElement(answer);
        addBaseBySqlElement(answer);
        addSelectBySqlElement(answer);
        addSelectMapBySqlElement(answer);
        addInsertBySqlElement(answer);
        addUpdateBySqlElement(answer);
        addCountBySqlElement(answer);
        addListBySqlElement(answer);
        addSelectBySqlConditionElement(answer,false);
        addSelectBySqlConditionElement(answer,true);

        addSelectByExampleWithRelationElement(answer);

        addSelectByForeignKeyElement(answer);
        addDeleteByColumnElement(answer);
        addSelectBySqlMethodElement(answer);
        addSelectByTableElement(answer);
        addSelectByKeysDictElement(answer);
        addDeleteByTableElement(answer);
        addInsertByTableElement(answer);

        return answer;
    }

    private void addInsertByTableElement(XmlElement parentElement) {
        if (introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream().anyMatch(SelectByTableGeneratorConfiguration::isEnableUnion)) {
            AbstractXmlElementGenerator elementGenerator = new InsertByTableElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addDeleteByTableElement(XmlElement parentElement) {
        if (introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream().anyMatch(SelectByTableGeneratorConfiguration::isEnableSplit)) {
            AbstractXmlElementGenerator elementGenerator = new DeleteByTableElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addSelectByKeysDictElement(XmlElement parentElement) {
        if (introspectedTable.getRules().isGenerateCachePO()) {
            AbstractXmlElementGenerator elementGenerator = new SelectByKeysDictElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addBaseBySqlElement(XmlElement parentElement) {
        AbstractXmlElementGenerator elementGenerator = new BaseBySqlElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addSelectBySqlConditionElement(XmlElement parentElement, boolean b) {
        AbstractXmlElementGenerator elementGenerator = new SelectBySqlConditionElementGenerator(b);
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addListBySqlElement(XmlElement parentElement) {
        AbstractXmlElementGenerator elementGenerator = new ListBySqlElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addCountBySqlElement(XmlElement parentElement) {
        AbstractXmlElementGenerator elementGenerator = new CountBySqlElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addUpdateBySqlElement(XmlElement parentElement) {
        AbstractXmlElementGenerator elementGenerator = new UpdateBySqlElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addInsertBySqlElement(XmlElement parentElement) {
        AbstractXmlElementGenerator elementGenerator = new InsertBySqlElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addSelectBySqlElement(XmlElement parentElement) {
        AbstractXmlElementGenerator elementGenerator = new SelectBySqlElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addSelectMapBySqlElement(XmlElement parentElement) {
        AbstractXmlElementGenerator elementGenerator = new SelectMapBySqlElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }



    protected void addResultMapWithoutBLOBsElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateBaseResultMap()) {
            AbstractXmlElementGenerator elementGenerator = new ResultMapWithoutBLOBsElementGenerator(false);
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addResultMapWithBLOBsElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateResultMapWithBLOBs()) {
            AbstractXmlElementGenerator elementGenerator = new ResultMapWithBLOBsElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addExampleWhereClauseElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateSQLExampleWhereClause()) {
            AbstractXmlElementGenerator elementGenerator = new ExampleWhereClauseElementGenerator(false);
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addMyBatis3UpdateByExampleWhereClauseElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateMyBatis3UpdateByExampleWhereClause()) {
            AbstractXmlElementGenerator elementGenerator = new ExampleWhereClauseElementGenerator(true);
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addBaseColumnListElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateBaseColumnList()) {
            AbstractXmlElementGenerator elementGenerator = new BaseColumnListElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addBlobColumnListElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateBlobColumnList()) {
            AbstractXmlElementGenerator elementGenerator = new BlobColumnListElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addSelectByExampleWithoutBLOBsElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateSelectByExampleWithoutBLOBs()) {
            AbstractXmlElementGenerator elementGenerator = new SelectByExampleWithoutBLOBsElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addSelectByExampleWithBLOBsElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateSelectByExampleWithBLOBs()) {
            AbstractXmlElementGenerator elementGenerator = new SelectByExampleWithBLOBsElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addSelectByPrimaryKeyElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateSelectByPrimaryKey()) {
            AbstractXmlElementGenerator elementGenerator = new SelectByPrimaryKeyElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addDeleteByExampleElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateDeleteByExample()) {
            AbstractXmlElementGenerator elementGenerator = new DeleteByExampleElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addDeleteByPrimaryKeyElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateDeleteByPrimaryKey()) {
            AbstractXmlElementGenerator elementGenerator = new DeleteByPrimaryKeyElementGenerator(false);
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addInsertElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateInsert()) {
            AbstractXmlElementGenerator elementGenerator = new InsertElementGenerator(false);
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addInsertBatchElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateInsertBatch()) {
            AbstractXmlElementGenerator elementGenerator = new InsertBatchElementGenerator(false);
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addInsertOrUpdateSelectiveElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateInsertOrUpdate()) {
            AbstractXmlElementGenerator elementGenerator = new InsertOrUpdateSelectiveElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addInsertSelectiveElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateInsertSelective()) {
            AbstractXmlElementGenerator elementGenerator = new InsertSelectiveElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addCountByExampleElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateCountByExample()) {
            AbstractXmlElementGenerator elementGenerator = new CountByExampleElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addUpdateByExampleSelectiveElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateUpdateByExampleSelective()) {
            AbstractXmlElementGenerator elementGenerator = new UpdateByExampleSelectiveElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addUpdateByExampleWithBLOBsElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateUpdateByExampleWithBLOBs()) {
            AbstractXmlElementGenerator elementGenerator = new UpdateByExampleWithBLOBsElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addUpdateByExampleWithoutBLOBsElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateUpdateByExampleWithoutBLOBs()) {
            AbstractXmlElementGenerator elementGenerator = new UpdateByExampleWithoutBLOBsElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addUpdateByPrimaryKeySelectiveElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateUpdateByPrimaryKeySelective()) {
            AbstractXmlElementGenerator elementGenerator = new UpdateByPrimaryKeySelectiveElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addUpdateBatchByPrimaryKeyElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateUpdateBatch()) {
            AbstractXmlElementGenerator elementGenerator = new UpdateBatchByPrimaryKeySelectiveElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addUpdateByPrimaryKeyWithBLOBsElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateUpdateByPrimaryKeyWithBLOBs()) {
            AbstractXmlElementGenerator elementGenerator = new UpdateByPrimaryKeyWithBLOBsElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addUpdateByPrimaryKeyWithoutBLOBsElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateUpdateByPrimaryKeyWithoutBLOBs()) {
            AbstractXmlElementGenerator elementGenerator = new UpdateByPrimaryKeyWithoutBLOBsElementGenerator(false);
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addSelectByExampleWithRelationElement(XmlElement parentElement){
        if (introspectedTable.getRules().generateRelationWithSubSelected()) {
            AbstractXmlElementGenerator elementGenerator = new SelectByExampleWithRelationElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addSelectByForeignKeyElement(XmlElement parentElement){
        if (introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().size() > 0) {
            AbstractXmlElementGenerator elementGenerator = new SelectByColumnElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addDeleteByColumnElement(XmlElement parentElement){
        introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().stream()
                .filter(SelectByColumnGeneratorConfiguration::isEnableDelete)
                .forEach(c->{
                    AbstractXmlElementGenerator elementGenerator = new DeleteByColumnElementGenerator(c);
                    initializeAndExecuteGenerator(elementGenerator, parentElement);
                });
    }

    protected void addSelectByTableElement(XmlElement parentElement){
        if (introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().size()>0) {
            SelectByTableElementGenerator elementGenerator = new SelectByTableElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);

        }
    }

    protected void addSelectBySqlMethodElement(XmlElement parentElement) {
        for (SelectBySqlMethodGeneratorConfiguration configuration : introspectedTable.getTableConfiguration().getSelectBySqlMethodGeneratorConfigurations()) {
            AbstractXmlElementGenerator elementGenerator = new SelectBySqlMethodElementGenerator(configuration);
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void initializeAndExecuteGenerator(AbstractXmlElementGenerator elementGenerator,
                                                 XmlElement parentElement) {
        elementGenerator.setContext(context);
        elementGenerator.setIntrospectedTable(introspectedTable);
        elementGenerator.setProgressCallback(progressCallback);
        elementGenerator.setWarnings(warnings);
        elementGenerator.addElements(parentElement);
    }

    @Override
    public Document getDocument() {
        Document document = new Document(XmlConstants.MYBATIS3_MAPPER_PUBLIC_ID,
                XmlConstants.MYBATIS3_MAPPER_SYSTEM_ID);
        document.setRootElement(getSqlMapElement());

        if (!context.getPlugins().sqlMapDocumentGenerated(document, introspectedTable)) {
            document = null;
        }

        return document;
    }
}
