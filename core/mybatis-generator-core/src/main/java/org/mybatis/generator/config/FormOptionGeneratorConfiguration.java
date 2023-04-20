package org.mybatis.generator.config;

import org.mybatis.generator.config.PropertyHolder;
import org.mybatis.generator.config.PropertyRegistry;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-08-16 14:16
 * @version 3.0
 */
public class FormOptionGeneratorConfiguration  extends PropertyHolder {

    private String idColumn;

    private String nameColumn;

    //options的数据类型，0-flat，1-tree
    private int dataType;

    public FormOptionGeneratorConfiguration(String nameColumn) {
        this.idColumn = PropertyRegistry.DEFAULT_PRIMARY_KEY;
        this.nameColumn = nameColumn;
        dataType = 0;
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

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }
}
