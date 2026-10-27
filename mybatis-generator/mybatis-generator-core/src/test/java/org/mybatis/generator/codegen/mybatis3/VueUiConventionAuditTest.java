package org.mybatis.generator.codegen.mybatis3;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 生成前端产物的 UI 约定审计。
 * <p>
 * 治理对象为 mpmm-ui 设计规范（docs/frontend-ui-design-standard.md）中两条对生成产物同样生效的约定：
 * <ul>
 *     <li>§6.1：业务页面不得写死组件尺寸 <code>size</code>，一律继承 <code>ElConfigProvider</code>
 *     （即全局密度档位）；生成页面属于业务页面，模板不得输出 <code>size="small"</code> 一类固定档位。</li>
 *     <li>§2.4：间距一律以 <code>var(--vgo-space-base, 15px)</code> 形式消费密度变量，
 *     不得写死密度档位取值的 px（7.5 / 10 / 13 / 15 / 20）。</li>
 * </ul>
 * 消费项目侧按「生成产物不手工改、改模板」的口径处理，故此处对模板渲染结果做零违例断言，
 * 使历史产物中出现过的两类问题（*Detail.vue 的 <code>margin: 7.5px 0 -1px 0</code>、
 * 旧版列表模板的 <code>size="small"</code>）在模板侧不再可回归。
 * <p>
 * 装饰性小间距（8px 及以下的图标/后缀留白）按 §2.4「组件内部 5–12px 装饰间距属组件自身约定」豁免，
 * 不在本次审计范围。
 *
 * @author vgosoft-agent
 */
public class VueUiConventionAuditTest {

    /** 密度预设档位取值：出现在 margin/padding 中即视为应消费 --vgo-space-* 变量。 */
    private static final Set<String> DENSITY_SPACING_VALUES = Set.of("7.5", "10", "13", "15", "20");

    private static final Pattern SIZE_ATTRIBUTE = Pattern.compile("\\ssize=\"(small|large|default)\"");
    private static final Pattern SPACING_DECLARATION = Pattern.compile("(?:margin|padding)[a-z-]*\\s*:\\s*([^;]+);");
    private static final Pattern PX_VALUE = Pattern.compile("(-?\\d+(?:\\.\\d+)?)px");

    @Test
    void generatedVueFilesCarryNoHardCodedSizeOrDensitySpacing() throws Exception {
        assertThat(audit(CasConsumerBaselineTest.consumerXml(null)))
                .as("生成的 .vue 产物不得写死组件尺寸（规范 6.1）或密度档位间距 px（规范 2.4）")
                .isEmpty();
    }

    /**
     * 正向断言：*Detail.vue 的表单项纵向间距必须消费密度变量，且默认档仍为 7.5px。
     * 仅靠「无违例」无法区分「已改变量」与「模板未输出该声明」，故补一条内容断言。
     */
    @Test
    void detailFormItemSpacingConsumesDensityVariable() throws Exception {
        Map<String, String> contents = generateVueFiles(CasConsumerBaselineTest.consumerXml(null));
        List<String> detailFiles = contents.keySet().stream()
                .filter(name -> name.endsWith("Detail.vue"))
                .sorted()
                .toList();
        assertThat(detailFiles).as("基线配置应生成至少一个 *Detail.vue").isNotEmpty();
        for (String detailFile : detailFiles) {
            assertThat(contents.get(detailFile))
                    .as(detailFile + " 的 .el-form-item 间距应写 calc(var(--vgo-space-base, 15px) / 2)")
                    .contains("margin: calc(var(--vgo-space-base, 15px) / 2) 0 -1px 0;")
                    .doesNotContain("margin: 7.5px");
        }
    }

    /** 生成并汇总每个 .vue 产物的 UI 约定违例，键为 文件名。 */
    private static Map<String, List<String>> audit(String xml) throws Exception {
        Map<String, String> contents = generateVueFiles(xml);
        var violations = new TreeMap<String, List<String>>();
        if (contents.isEmpty()) {
            violations.put("audit-setup", List.of("未取到任何 .vue 产物：配置未开启 Vue 生成，审计结论不可信"));
            return violations;
        }
        contents.forEach((name, content) -> {
            List<String> problems = collectProblems(content);
            if (!problems.isEmpty()) {
                violations.put(name, problems);
            }
        });
        return violations;
    }

    /** 生成并返回全部 .vue 产物内容，键为 文件名。 */
    private static Map<String, String> generateVueFiles(String xml) throws Exception {
        var parser = new ConfigurationParser(new ArrayList<>());
        Configuration config = parser.parseConfiguration(new StringReader(xml));
        parser.customConfig(config);
        var generator = new MyBatisGenerator(config, new DefaultShellCallback(true), new ArrayList<>());
        generator.generate(null, null, null, false);

        var contents = new TreeMap<String, String>();
        for (GeneratedFile file : generator.getOtherGeneratedFiles()) {
            if (file.getFileName().endsWith(".vue")) {
                contents.put(file.getFileName(), file.getFormattedContent());
            }
        }
        return contents;
    }

    private static List<String> collectProblems(String content) {
        var problems = new LinkedHashSet<String>();

        Matcher sizeMatcher = SIZE_ATTRIBUTE.matcher(content);
        while (sizeMatcher.find()) {
            problems.add("写死组件尺寸 size=\"" + sizeMatcher.group(1)
                    + "\"（规范 6.1：业务页面一律继承 ElConfigProvider，不得写死 size）");
        }

        Matcher spacingMatcher = SPACING_DECLARATION.matcher(content);
        while (spacingMatcher.find()) {
            String declaration = spacingMatcher.group(0).trim();
            if (declaration.contains("var(--vgo-space")) {
                continue;
            }
            Matcher valueMatcher = PX_VALUE.matcher(spacingMatcher.group(1));
            while (valueMatcher.find()) {
                String value = valueMatcher.group(1);
                if (DENSITY_SPACING_VALUES.contains(value)) {
                    problems.add("间距写死 " + value + "px（规范 2.4：须写 var(--vgo-space-base, 15px) 形式）：" + declaration);
                }
            }
        }
        return new ArrayList<>(problems);
    }
}
