package org.mybatis.generator.custom.annotations;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.config.SensitiveCheckConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 验证敏感检测注解描述的文本、导入项和重复渲染行为。
 *
 * @author vgosoft
 */
class SensitiveCheckDescTest {

    @Test
    void extendsAbstractAnnotationAndRendersStableAnnotationAndImports() {
        SensitiveCheckDesc description = new SensitiveCheckDesc(
                new SensitiveCheckConfiguration("content_", null, null, null));

        assertThat(SensitiveCheckDesc.class.getSuperclass()).isEqualTo(AbstractAnnotation.class);
        assertThat(description.toAnnotation()).isEqualTo(
                "@SensitiveCheck(enable = true, strategy = SensitiveCheckStrategy.INHERIT, contentType = SensitiveContentType.PLAIN_TEXT)");
        assertThat(description.getImports()).containsExactlyInAnyOrder(
                "com.vgosoft.core.annotation.sensitive.SensitiveCheck",
                "com.vgosoft.core.annotation.sensitive.SensitiveCheckStrategy",
                "com.vgosoft.core.annotation.sensitive.SensitiveContentType");

        String rendered = description.toAnnotation();
        assertThat(description.toAnnotation()).isEqualTo(rendered);
        assertThat(description.toAnnotation()).isEqualTo(rendered);
        assertThat(description.getItems()).isEmpty();
    }

    @Test
    void rendersDisabledAndExplicitStrategyConfiguration() {
        SensitiveCheckDesc description = new SensitiveCheckDesc(
                new SensitiveCheckConfiguration("content_", "false", "WARN", null));

        assertThat(description.toAnnotation()).isEqualTo(
                "@SensitiveCheck(enable = false, strategy = SensitiveCheckStrategy.WARN, contentType = SensitiveContentType.PLAIN_TEXT)");
        assertThat(description.toAnnotation()).isEqualTo(description.toAnnotation());
        assertThat(description.getItems()).isEmpty();
    }

    @Test
    void rejectsNullAndInvalidConfigurationInputs() {
        assertThatThrownBy(() -> new SensitiveCheckDesc(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("敏感检测配置不能为空");
        assertThatThrownBy(() -> new SensitiveCheckConfiguration("content_", null, "INVALID", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strategy");
        assertThatThrownBy(() -> new SensitiveCheckConfiguration("content_", null, null, "HTML"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("contentType");
    }
}
