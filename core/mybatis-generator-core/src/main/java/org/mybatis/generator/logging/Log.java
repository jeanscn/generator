package org.mybatis.generator.logging;

public interface Log {

    boolean isDebugEnabled();

    void error(String s, Throwable e);

    void error(String s);

    void debug(String s);

    void warn(String s);
}
