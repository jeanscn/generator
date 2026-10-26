package org.mybatis.generator.config;

import javax.lang.model.SourceVersion;

/**
 * 声明显式关闭的完整服务签名及可选内部原语名称。
 *
 * @author vgosoft-agent
 */
public record DisabledServiceMethodConfiguration(String signature, String reason, String internalMethod) {
    public DisabledServiceMethodConfiguration {
        if (signature == null || !signature.matches(
                "[\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*\\((?:[\\w.$]+(?:\\[\\])*(?:,[\\w.$]+(?:\\[\\])*)*)?\\)")) {
            throw new IllegalArgumentException("disabledMethod signature 必须为无空格的完整擦除参数签名：" + signature);
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("disabledMethod reason 必须为非空停用原因：" + signature);
        }
        if (internalMethod != null && (!SourceVersion.isIdentifier(internalMethod)
                || SourceVersion.isKeyword(internalMethod))) {
            throw new IllegalArgumentException("disabledMethod internalMethod 不是合法 Java 方法名：" + internalMethod);
        }
    }
}
