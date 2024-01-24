package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;

public class CleanupInvalidRecordsElement extends AbstractServiceElementGenerator {

    public CleanupInvalidRecordsElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        Method method = serviceMethods.getCleanupInvalidRecordsMethod(parentElement, false);
        method.addAnnotation("@Override");
        method.addBodyLine("{0} example = new {0}();",exampleType.getShortName());
        method.addBodyLine("example.createCriteria().andWfStateEqualTo(4);");
        method.addBodyLine("return this.deleteByExample(example);");
        parentElement.addMethod(method);
        parentElement.addImportedType(exampleType);
    }
}
