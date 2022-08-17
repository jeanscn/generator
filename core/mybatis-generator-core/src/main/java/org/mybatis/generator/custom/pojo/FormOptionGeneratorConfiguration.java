package org.mybatis.generator.custom.pojo;

import org.mybatis.generator.config.PropertyHolder;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-08-16 14:16
 * @version 3.0
 */
public class FormOptionGeneratorConfiguration  extends PropertyHolder {

    private String idColumn;

    private String nameColumn;

    public FormOptionGeneratorConfiguration(String nameColumn) {
        this.idColumn = "ID_";
        this.nameColumn = nameColumn;
    }

    public String getNameColumn() {
        return nameColumn;
    }

    public void setNameColumn(String nameColumn) {
        this.nameColumn = nameColumn;
    }

    public void setIdColumn(String idColumn) {
        this.idColumn = idColumn;
    }

    public String getIdColumn() {
        return idColumn;
    }
}
