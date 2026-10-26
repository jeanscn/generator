package org.mybatis.generator.plugins.vue3;

import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.TypeScriptTypeDescriptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 为同一模块内继承声明引用的实体类型生成类型导入，排除本文件已声明的类型。
 *
 * @author vgosoft
 */
public final class Vue3TypeImports {
    private Vue3TypeImports() {
    }

    /**
     * 从最终继承声明解析依赖，使用配置中的实体名定位兄弟模块类型文件。
     *
     * @param context 当前模块配置，不要求连接或内省数据库
     * @param descriptors 本文件实际输出的类型声明
     * @return 按表配置顺序排列且去重的类型导入
     */
    public static List<Map<String, String>> resolve(
            Context context, List<TypeScriptTypeDescriptor> descriptors) {
        Set<String> declared = new HashSet<>();
        StringBuilder inheritedTypes = new StringBuilder();
        for (TypeScriptTypeDescriptor descriptor : descriptors) {
            declared.add(descriptor.getName());
            if (descriptor.getExtendsName() != null) {
                inheritedTypes.append(' ').append(descriptor.getExtendsName());
            }
        }
        List<Map<String, String>> imports = new ArrayList<>();
        Set<String> imported = new HashSet<>();
        for (TableConfiguration table : context.getTableConfigurations()) {
            String name = table.getDomainObjectName();
            if (name == null || name.isBlank() || declared.contains(name) || imported.contains(name)) {
                continue;
            }
            if (Pattern.compile("(?<![\\w$])" + Pattern.quote(name) + "(?![\\w$])")
                    .matcher(inheritedTypes).find()) {
                imports.add(Map.of("name", name,
                        "path", "../../" + name.toLowerCase(Locale.ROOT) + "/types/T" + name));
                imported.add(name);
            }
        }
        return imports;
    }
}
