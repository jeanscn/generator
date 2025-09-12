package org.mybatis.generator.api;

import lombok.Getter;

/**
 * Abstract class that holds information common to all generated files.
 *
 * @author Jeff Butler
 */
@Getter
public abstract class GeneratedFile {

    /**
     * -- GETTER --
     *  Gets the target project. Clients can call this method to determine how to
     *  save the results.
     *
     */
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
     * Get the target package for the file. Clients should use this method to
     * determine how to save the results.
     *
     * @return Returns the target project.
     */
    public abstract String getTargetPackage();

    /**
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getFileName();
    }

    /**
     * Checks if is mergeable. This method is used to determine if the file can be merged
     *
     * @return true, if is mergeable
     */
    public abstract boolean isMergeable();

    /**
     * Returns the file encoding for the generated file.
     * This is used to determine how to write the file.
     *
     * @return the file encoding
     */
    public abstract String getFileEncoding();

    public void setOverWriteFile(boolean isOverWriteFile){
        this.isOverWriteFile = isOverWriteFile;
    }
}
