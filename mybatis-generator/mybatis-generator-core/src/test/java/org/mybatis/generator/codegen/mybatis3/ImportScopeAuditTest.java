package org.mybatis.generator.codegen.mybatis3;

import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 全量 import 范围审计：所有生成 Java 文件的 import 都必须被内容短名引用。
 * <p>
 * 审计期间关闭生成器末尾的统一兜底（vgosoft.audit.skipReconciler），
 * 只保留 DisabledServiceEntries.apply 在改写方法时的登记侧自清理，
 * 从而强制孤儿 import 在登记侧不产生，而不是依赖最终被动回收。
 * 覆盖无停用基线、真实停用配置（含 CAS）以及历史上产生孤儿最多的
 * updateBatch 停用场景。
 *
 * @author vgosoft-agent
 */
public class ImportScopeAuditTest {

    private static final String ENTITY = "com.vgosoft.sensitive.entity.SensitiveRuleRevision";
    private static final String EXAMPLE = "com.vgosoft.sensitive.entity.example.SensitiveRuleRevisionExample";

    @Test
    void keepsEveryGeneratedImportReferencedWithoutGlobalFallback() throws Exception {
        System.setProperty("vgosoft.audit.skipReconciler", "true");
        try {
            assertThat(audit("baseline-no-disabled", CasConsumerBaselineTest.consumerXml(null)))
                    .as("无停用基线的全部生成类不得存在无效 import").isEmpty();
            System.setProperty("sc03.disabled", "true");
            try {
                assertThat(audit("consumer-with-disabled", CasConsumerBaselineTest.consumerXml("true")))
                        .as("真实停用配置（含 CAS）的全部生成类不得存在无效 import").isEmpty();
            } finally {
                System.clearProperty("sc03.disabled");
            }
            assertThat(audit("revision-updateBatch-disabled", revisionUpdateBatchDisabled()))
                    .as("updateBatch 停用场景的全部生成类不得存在无效 import").isEmpty();
        } finally {
            System.clearProperty("vgosoft.audit.skipReconciler");
        }
    }

    /** 历史上产生 ServiceCodeEnum、VgoException 孤儿 import 的停用组合。 */
    private static Configuration revisionUpdateBatchDisabled() throws Exception {
        return DisabledServiceEntryGenerationTest.auditConfiguration(
                DisabledServiceEntryGenerationTest.disabledEntry("deleteByExample(" + EXAMPLE + ")")
                        + DisabledServiceEntryGenerationTest.disabledEntry("deleteByPrimaryKey(java.lang.String)")
                        + DisabledServiceEntryGenerationTest.disabledEntry("insert(" + ENTITY + ")")
                        + DisabledServiceEntryGenerationTest.disabledEntry("insertSelective(" + ENTITY + ")")
                        + DisabledServiceEntryGenerationTest.disabledEntry("updateBatchByPrimaryKey(java.util.List)"));
    }

    /** 生成并汇总每个文件的孤儿 import，键为 场景:包名.文件名。 */
    private static java.util.Map<String, List<String>> audit(String label, String xml) throws Exception {
        var parser = new ConfigurationParser(new ArrayList<>());
        Configuration config = parser.parseConfiguration(new StringReader(xml));
        parser.customConfig(config);
        return audit(label, config);
    }

    /** 生成并汇总每个文件的孤儿 import，键为 场景:包名.文件名。 */
    private static java.util.Map<String, List<String>> audit(String label, Configuration config) throws Exception {
        var generator = new MyBatisGenerator(config, new DefaultShellCallback(true), new ArrayList<>());
        generator.generate(null, null, null, false);
        var orphans = new TreeMap<String, List<String>>();
        for (GeneratedJavaFile file : generator.getGeneratedJavaFiles()) {
            String content = file.getFormattedContent().lines()
                    .filter(line -> !line.startsWith("import "))
                    .collect(Collectors.joining("\n"));
            List<String> unused = file.getCompilationUnit().getImportedTypes().stream()
                    .flatMap(type -> type.getImportList().stream())
                    .filter(name -> !name.startsWith("static "))
                    .filter(name -> !Pattern.compile("(?<![A-Za-z0-9_$])" + Pattern.quote(shortName(name))
                            + "(?![A-Za-z0-9_])").matcher(content).find())
                    .distinct().sorted().toList();
            if (!unused.isEmpty()) {
                orphans.put(label + ":" + file.getTargetPackage() + "." + file.getFileName(), unused);
            }
        }
        return orphans;
    }

    private static String shortName(String importedType) {
        String name = importedType.substring(importedType.lastIndexOf('.') + 1);
        int array = name.indexOf('[');
        return array < 0 ? name : name.substring(0, array);
    }
}
