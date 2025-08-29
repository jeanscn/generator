package org.mybatis.generator.codegen.mybatis3.service.elements;

import com.vgosoft.core.constant.enums.data.StatTimeDimEnum;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.config.JavaServiceImplGeneratorConfiguration;
import org.mybatis.generator.config.StatisticsDateDimGeneratorConfiguration;

import java.util.stream.Collectors;

/**
 * selectByExampleWithRelation实现方法
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-23 23:57
 * @version 3.0
 */
public class StatisticsTotalElement extends AbstractServiceElementGenerator {

    public StatisticsTotalElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        JavaServiceImplGeneratorConfiguration serviceImplGeneratorConfiguration = introspectedTable.getTableConfiguration().getJavaServiceImplGeneratorConfiguration();
        if (serviceImplGeneratorConfiguration == null || !serviceImplGeneratorConfiguration.isGenerate()) {
            return;
        }
        if (serviceImplGeneratorConfiguration.getStatisticsConfigurations().isEmpty()) {
            return;
        }
        for (StatisticsDateDimGeneratorConfiguration statisticsConfiguration : serviceImplGeneratorConfiguration.getStatisticsConfigurations()) {
            Method statisticsTotalMethod = serviceMethods.getStatisticsTotalMethod(parentElement, statisticsConfiguration, false,true);
            statisticsTotalMethod.setVisibility(JavaVisibility.PUBLIC);
            statisticsTotalMethod.addAnnotation("@Override");
            StatTimeDimEnum statTimeDimEnum = StatTimeDimEnum.ofCode(statisticsConfiguration.getDateDimension());
            if (statTimeDimEnum != null) {
                statisticsTotalMethod.addBodyLine("minDimension = VStringUtil.stringHasValue(minDimension) ? minDimension : StatTimeDimEnum.{0}.code();", StatTimeDimEnum.ofCode(statisticsConfiguration.getDateDimension()).name());
                parentElement.addImportedType(new FullyQualifiedJavaType("com.vgosoft.core.constant.enums.data.StatTimeDimEnum"));
            }
            statisticsTotalMethod.addBodyLine("return mapper.{0}({1});", statisticsConfiguration.getMethodName()
                    ,statisticsTotalMethod.getParameters().stream()
                            .map(Parameter::getName)
                            .collect(Collectors.joining(",")));
            parentElement.addMethod(statisticsTotalMethod);
        }
    }
}
