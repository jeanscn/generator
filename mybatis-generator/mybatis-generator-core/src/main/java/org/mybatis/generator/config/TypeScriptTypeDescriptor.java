package org.mybatis.generator.config;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mybatis.generator.custom.FieldItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TypeScript 类型描述配置文件
 * @author cen_c
 */
@Data
@NoArgsConstructor
public class TypeScriptTypeDescriptor {
    /**
     * 类型名称
     */
    private String name = "UnnamedType";

    /**
     * 类型描述（interface 或 type）
     * "interface" 或 "type"
     */
    private String typeKind = "interface";

    /**
     * 类型说明
     */
    private String description;

    /**
     * 继承的类型名称（可选）
     */
    private String extendsName;

    /**
     * 属性列表
     */
    private List<FieldItem> properties = new ArrayList<>();

    public TypeScriptTypeDescriptor(String name, String typeKind, String description) {
        this.name = name;
        this.typeKind = typeKind;
        this.description = description;
    }

    /**
     * 判断 properties 是否非空，供 FreeMarker 模板直接调用
      */
    public boolean hasProperties() {
        return !properties.isEmpty();
    }

    public void addProperty(FieldItem property) {
        Optional<FieldItem> fieldItem = properties.stream()
                .filter(p -> p.getName().equals(property.getName()))
                .findFirst();
        if (!fieldItem.isPresent()) {
            properties.add(property);
        }

    }
}
