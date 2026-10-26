package org.mybatis.generator.codegen.mybatis3.service;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.java.render.TopLevelClassRenderer;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 按最终生成内容回收没有被任何短名引用的 import。
 * <p>
 * 生成过程分多步装配：各元素先在“方法会被保留”的假设下登记 import，
 * 随后才可能由 {@link DisabledServiceEntries} 把方法替换成单行拒绝桩。
 * 原有方法体连同其依赖一起消失后，登记过的 import 会变成孤儿，既不可编译优化，
 * 也会让产物出现编译告警。此类在全部装配和插件回调结束后，
 * 以真实渲染内容为基准回收这些孤儿 import。
 * </p>
 * <p>
 * 判定采用保守策略：只要 import 对应的短名在代码中以完整单词形式出现过
 * （无论前面是全限定名还是其他限定上下文），就保留该 import；
 * 静态 import 与通配 import 不参与回收。
 *
 * @author vgosoft
 */
public final class ServiceImportReconciler {

    private ServiceImportReconciler() {
    }

    /**
     * 回收目标编译单元中未被任何短名引用的 import。
     *
     * @param unit 已完成装配和插件回调的生成类，通常是抽象 Service 实现
     * @return 被回收的 import 短名，按字典序排列，便于回归断言与诊断
     */
    public static List<String> retainReferenced(TopLevelClass unit) {
        String content = new TopLevelClassRenderer().render(unit).lines()
                .filter(line -> !line.startsWith("import "))
                .collect(Collectors.joining("\n"));
        List<FullyQualifiedJavaType> orphans = unit.getImportedTypes().stream()
                .filter(type -> !referenced(content, type))
                .toList();
        unit.getImportedTypes().removeAll(orphans);
        return orphans.stream()
                .flatMap(type -> type.getImportList().stream())
                .map(ServiceImportReconciler::shortName)
                .distinct()
                .sorted()
                .toList();
    }

    private static boolean referenced(String content, FullyQualifiedJavaType type) {
        return type.getImportList().stream()
                .map(ServiceImportReconciler::shortName)
                .anyMatch(name -> Pattern.compile("(?<![A-Za-z0-9_$])" + Pattern.quote(name) + "(?![A-Za-z0-9_])")
                        .matcher(content).find());
    }

    private static String shortName(String importedType) {
        String name = importedType.substring(importedType.lastIndexOf('.') + 1);
        int array = name.indexOf('[');
        return array < 0 ? name : name.substring(0, array);
    }
}
