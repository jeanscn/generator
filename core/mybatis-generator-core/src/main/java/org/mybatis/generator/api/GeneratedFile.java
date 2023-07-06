package org.mybatis.generator.api;

/**
 * Abstract class that holds information common to all generated files.
 *
 * @author Jeff Butler
 */
public abstract class GeneratedFile {

    protected final String targetProject;

    protected boolean isOverWriteFile = true;

    protected GeneratedFile(String targetProject) {
        this.targetProject = targetProject;
    }

    /**
     * Returns the entire contents of the generated file. Clients
     * can simply save the value returned from this method as the file contents.
     * Subclasses such as @see org.mybatis.generator.api.GeneratedJavaFile offer
     * more fine grained access to file parts, but still implement this method
     * in the event that the entire contents are desired.
     *
     * @return Returns the content.
     */
    public abstract String getFormattedContent();

    /**
     * Get the file name (without any path). Clients should use this method to
     * determine how to save the results.
     *
     * @return Returns the file name.
     */
    public abstract String getFileName();

    /**
     * Gets the target project. Clients can call this method to determine how to
     * save the results.
     *
     * @return the target project
     */
    public String getTargetProject() {
        return targetProject;
    }

    /**
     * Get the target package for the file. Clients should use this method to
     * determine how to save the results.
     *
     * @return Returns the target project.
     */
    public abstract String getTargetPackage();

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getFileName();
    }

    /**
     * Checks if is mergeable.
     *
     * @return true, if is mergeable
     */
    public abstract boolean isMergeable();

    public abstract String getFileEncoding();

    public boolean isOverWriteFile(){
        return this.isOverWriteFile;
    }

    public void setOverWriteFile(boolean isOverWriteFile){
        this.isOverWriteFile = isOverWriteFile;
    }
}
