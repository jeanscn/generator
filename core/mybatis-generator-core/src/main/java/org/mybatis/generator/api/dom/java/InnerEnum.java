package org.mybatis.generator.api.dom.java;

import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates the idea of an inner enum - it has methods that make
 * it easy to generate inner enum.
 *
 * @author Jeff Butler
 */
public class InnerEnum extends AbstractJavaType {

    private final List<String> enumConstants = new ArrayList<>();

    private final List<InitializationBlock> initializationBlocks = new ArrayList<>();

    public InnerEnum(FullyQualifiedJavaType type) {
        super(type);
    }

    public InnerEnum(String type) {
        super(type);
    }

    public List<String> getEnumConstants() {
        return enumConstants;
    }

    public void addEnumConstant(String enumConstant) {
        enumConstants.add(enumConstant);
    }

    public List<InitializationBlock> getInitializationBlocks() {
        return initializationBlocks;
    }

    public void addInitializationBlock(InitializationBlock initializationBlock) {
        initializationBlocks.add(initializationBlock);
    }
}
