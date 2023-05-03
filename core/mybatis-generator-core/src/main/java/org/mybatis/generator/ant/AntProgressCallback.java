package org.mybatis.generator.ant;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.mybatis.generator.api.ProgressCallback;

/**
 * This callback logs progress messages with the Ant logger.
 *
 * @author Jeff Butler
 */
public class AntProgressCallback implements ProgressCallback {

    private final Task task;

    private final boolean verbose;

    /**
     * Instantiates a new ant progress callback.
     *
     * @param task
     *            the task
     * @param verbose
     *            the verbose
     */
    public AntProgressCallback(Task task, boolean verbose) {
        this.task = task;
        this.verbose = verbose;
    }

    /* (non-Javadoc)
     * @see org.mybatis.generator.internal.NullProgressCallback#startTask(java.lang.String)
     */
    @Override
    public void startTask(String subTaskName) {
        if (verbose) {
            task.log(subTaskName, Project.MSG_VERBOSE);
        }
    }
}
