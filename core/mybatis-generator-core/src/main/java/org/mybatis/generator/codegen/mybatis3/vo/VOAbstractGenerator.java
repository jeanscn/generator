package org.mybatis.generator.codegen.mybatis3.vo;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.custom.ConstantsUtil;

import java.util.List;

import static org.mybatis.generator.internal.util.JavaBeansUtil.*;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-03-29 17:17
 * @version 3.0
 */
public class VOAbstractGenerator extends AbstractVOGenerator{

    public VOAbstractGenerator(IntrospectedTable introspectedTable, String project, ProgressCallback progressCallback, List<String> warnings,Interface mappingsInterface) {
        super(introspectedTable, project, progressCallback, warnings,mappingsInterface);
    }

    @Override
    TopLevelClass generate() {
        /*
         * 生成AbstractVo
         * */
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        String abstractName = "Abstract" + entityType.getShortName() + "VO";
        String abstractVoType = String.join(".", baseTargetPackage, subPackageAbs, abstractName);
        TopLevelClass abstractVo = new TopLevelClass(abstractVoType);
        abstractVo.setAbstract(true);
        abstractVo.setVisibility(JavaVisibility.PUBLIC);
        abstractVo.addSuperInterface(new FullyQualifiedJavaType(ConstantsUtil.I_BASE_DTO));
        abstractVo.addImportedType(ConstantsUtil.I_BASE_DTO);
        commentGenerator.addJavaFileComment(abstractVo);
        commentGenerator.addModelClassComment(abstractVo, introspectedTable);
        abstractVo.addAnnotation("@Data");
        abstractVo.addAnnotation("@NoArgsConstructor");
        if (voGenService.getAbstractVOColumns().size() > 0) {
            abstractVo.addAnnotation("@AllArgsConstructor");
        }
        abstractVo.addImportedType("lombok.*");
        abstractVo.addSerialVersionUID();
        //添加属性
        for (IntrospectedColumn introspectedColumn : voGenService.getAbstractVOColumns()) {
            Field field = getJavaBeansField(introspectedColumn, context, introspectedTable);
            field.setVisibility(JavaVisibility.PROTECTED);
            if (plugins.voAbstractFieldGenerated(field, abstractVo, introspectedColumn, introspectedTable)) {
                abstractVo.addField(field);
                abstractVo.addImportedType(field.getType());

                StringBuilder sb = new StringBuilder(introspectedColumn.getJavaProperty());
                if (sb.length() > 1 && Character.isUpperCase(sb.charAt(1))) {
                    Method method = getJavaBeansGetter(introspectedColumn, context, introspectedTable);
                    abstractVo.addMethod(method);
                    if (!introspectedTable.isImmutable()) {
                        method = getJavaBeansSetter(introspectedColumn, context, introspectedTable);
                        abstractVo.addMethod(method);
                    }
                }
            }
        }
        return abstractVo;
    }
}
