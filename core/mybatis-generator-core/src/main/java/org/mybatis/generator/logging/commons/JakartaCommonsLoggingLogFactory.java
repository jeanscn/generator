package org.mybatis.generator.logging.commons;

import org.mybatis.generator.logging.AbstractLogFactory;
import org.mybatis.generator.logging.Log;

public class JakartaCommonsLoggingLogFactory implements AbstractLogFactory {
    @Override
    public Log getLog(Class<?> clazz) {
        return new JakartaCommonsLoggingImpl(clazz);
    }
}
