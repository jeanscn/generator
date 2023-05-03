package org.mybatis.generator.api;

/**
 * A slightly more verbose progress callback.
 *
 * @author Jeff Butler
 *
 */
public class VerboseProgressCallback implements ProgressCallback {

    public VerboseProgressCallback() {
        super();
    }

    @Override
    public void startTask(String taskName) {
        System.out.println(taskName);
    }
}
