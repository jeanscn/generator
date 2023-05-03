package org.mybatis.generator.config;

import com.vgosoft.core.constant.enums.DefultColumnNameEnum;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-08-16 14:16
 * @version 3.0
 */
public class TreeViewCateGeneratorConfiguration extends PropertyHolder {

    private String SPeL = "'"+ DefultColumnNameEnum.PARENT_ID.columnName() +" = ''' + #this.id +''''";

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
}
