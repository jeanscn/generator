package org.mybatis.generator.logging;

/**
 * Defines the interface for creating Log implementations.
 *
 * @author Jeff Butler
 *
 */
public interface AbstractLogFactory {
    Log getLog(Class<?> targetClass);
}
