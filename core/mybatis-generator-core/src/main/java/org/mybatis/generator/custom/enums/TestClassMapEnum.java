package org.mybatis.generator.custom.enums;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Optional;

import static org.mybatis.generator.custom.ConstantsUtil.ABSTRACT_MBG_SERVICE_INTERFACE;
import static org.mybatis.generator.custom.ConstantsUtil.TEST_ABSTRACT_MYBATIS_BG_SERVICE_TEST;

@Getter
public enum TestClassMapEnum {

    AbstractMybatisBGService(ABSTRACT_MBG_SERVICE_INTERFACE, TEST_ABSTRACT_MYBATIS_BG_SERVICE_TEST);

    private final String superClass;
    private final String testClass;

    TestClassMapEnum(final String superClass, final String testClass) {
        this.superClass = superClass;
        this.testClass = testClass;
    }

    public static Optional<TestClassMapEnum> ofSuperClass(final String superClass) {
        return EnumSet.allOf(TestClassMapEnum.class).stream()
                .filter(e -> e.superClass.equals(superClass))
                .findFirst();
    }

}
