package org.mybatis.generator.logging.slf4j;

import org.mybatis.generator.logging.AbstractLogFactory;
import org.mybatis.generator.logging.Log;

public class Slf4jLoggingLogFactory implements AbstractLogFactory {
    @Override
    public Log getLog(Class<?> clazz) {
        return new Slf4jImpl(clazz);
    }
}
