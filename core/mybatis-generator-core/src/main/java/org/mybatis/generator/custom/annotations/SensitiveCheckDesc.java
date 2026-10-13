package org.mybatis.generator.custom.annotations;

import org.mybatis.generator.config.SensitiveCheckConfiguration;

import java.util.Objects;

/**
 * 根据敏感检测配置描述实体字段注解。
 *
 * @author vgosoft
 */
public final class SensitiveCheckDesc extends AbstractAnnotation {
    private final boolean enable;
    private final String strategy;
    private final String contentType;

    /**
     * 从已验证的敏感检测配置创建注解描述。
     *
     * @param configuration 敏感检测配置
     */
    public SensitiveCheckDesc(SensitiveCheckConfiguration configuration) {
        Objects.requireNonNull(configuration, "敏感检测配置不能为空");
        this.enable = configuration.isEnable();
        this.strategy = configuration.getStrategy();
        this.contentType = configuration.getContentType();
        addImports("com.vgosoft.core.annotation.sensitive.SensitiveCheck");
        addImports("com.vgosoft.core.annotation.sensitive.SensitiveCheckStrategy");
        addImports("com.vgosoft.core.annotation.sensitive.SensitiveContentType");
    }

    @Override
    public String toAnnotation() {
        return "@SensitiveCheck(enable = " + enable
                + ", strategy = SensitiveCheckStrategy." + strategy
                + ", contentType = SensitiveContentType." + contentType + ")";
    }
}
