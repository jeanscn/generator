package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;

/**
 * 生成按主键原子递增的方法声明。
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * @version 3.0
 */
public class AtomicIncrementMethodGenerator extends AbstractJavaMapperMethodGenerator {

    @Override
    public void addInterfaceElements(Interface interfaze) {
        ServiceMethods serviceMethods = new ServiceMethods(context, introspectedTable);
        Method method = serviceMethods.getAtomicIncrementMethod(interfaze, true);
        if (context.getPlugins().clientIncrementRevisionByPrimaryKeyGenerated(method, interfaze, introspectedTable)) {
            interfaze.addMethod(method);
        }
    }
}
