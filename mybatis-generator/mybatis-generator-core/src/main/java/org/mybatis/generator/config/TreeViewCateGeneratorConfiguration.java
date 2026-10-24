package org.mybatis.generator.config;

import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-08-16 14:16
 * @version 3.0
 */
public class TreeViewCateGeneratorConfiguration extends PropertyHolder {

    private String pathKeyWord = "view-cate";

    private String idProperty = DefaultColumnNameEnum.ID.fieldName();

    private String nameProperty = DefaultColumnNameEnum.NAME.fieldName();

    private String SPeL = "'"+ DefaultColumnNameEnum.PARENT_ID.columnName() +" = ''' + #this.id +''''";

    public TreeViewCateGeneratorConfiguration() {
        super();
    }

    public TreeViewCateGeneratorConfiguration(String SPeL) {
        super();
       this.SPeL = SPeL;
    }

    public String getSPeL() {
        return SPeL;
    }

    public void setSPeL(String SPeL) {
        this.SPeL = SPeL;
    }

    public String getPathKeyWord() {
        return pathKeyWord;
    }

    public void setPathKeyWord(String pathKeyWord) {
        this.pathKeyWord = pathKeyWord;
    }

    public String getIdProperty() {
        return idProperty;
    }

    public void setIdProperty(String idProperty) {
        this.idProperty = idProperty;
    }

    public String getNameProperty() {
        return nameProperty;
    }

    public void setNameProperty(String nameProperty) {
        this.nameProperty = nameProperty;
    }
}
