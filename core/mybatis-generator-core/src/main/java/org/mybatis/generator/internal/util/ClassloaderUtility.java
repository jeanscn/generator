package org.mybatis.generator.internal.util;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mybatis.generator.logging.Log;
import org.mybatis.generator.logging.LogFactory;

/**
 * This class holds methods useful for constructing custom classloaders.
 *
 * @author Jeff Butler
 *
 */
public class ClassloaderUtility {

    private static final Log LOG = LogFactory.getLog(ClassloaderUtility.class);

    /**
     * Utility Class - No Instances.
     */
    private ClassloaderUtility() {
    }

    public static ClassLoader getCustomClassloader(Collection<String> entries) {
        List<URL> urls = new ArrayList<>();
        File file;

        if (entries != null) {
            for (String classPathEntry : entries) {
                file = new File(classPathEntry);
                if (!file.exists()) {
                    LOG.warn(getString("Warning.31", classPathEntry)); //$NON-NLS-1$
                    continue;
                }

                try {
                    urls.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    // this shouldn't happen, but just in case...
                    throw new RuntimeException(getString(
                            "RuntimeError.9", classPathEntry)); //$NON-NLS-1$
                }
            }
        }

        ClassLoader parent = Thread.currentThread().getContextClassLoader();

        return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
    }
}
