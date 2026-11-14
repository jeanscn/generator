package org.mybatis.generator.codegen.mybatis3;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.codegen.mybatis3.freemaker.vue3.GeneratedVueFile;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 无数据库内省的编辑模板分支渲染回归。
 *
 * @author vgosoft
 */
class VueEditTemplateTest {
    @Test
    void rendersWorkflowAndInnerListBranchesIndependently() {
        for (boolean workflow : new boolean[] {false, true}) {
            for (boolean innerList : new boolean[] {false, true}) {
                var file = new GeneratedVueFile("SampleEdit.vue", "", "", null,
                        "vue_module_edit.vue.ftl", Map.of(
                                "OVERRIDABLE_FLAG", "@VCF.overridable",
                                "tableRemark", "示例",
                                "modelName", "Sample",
                                "modelPath", "sample",
                                "restBasePath", "sample-impl",
                                "workflowEnabled", workflow,
                                "hasInnerList", innerList));
                String output = file.getFormattedContent();
                assertThat(output).isNotBlank()
                        .contains("@VCF.overridable", "name=\"SampleEdit\"",
                                "@update:modelValue=\"onModelUpdate\"")
                        .doesNotContain("<#if", "${ modelName }", "deep: true");
                assertThat(output.contains("useCurrentTaskAttributesStore")).isEqualTo(workflow);
                assertThat(output.contains("defaultInnerListButtonActionHandler")).isEqualTo(innerList);
            }
        }
    }
}
