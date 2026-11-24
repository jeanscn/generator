package org.mybatis.generator.config;

import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-08-16 14:16
 * @version 3.0
 */
@Setter
@Getter
public class TreeViewCateGeneratorConfiguration extends PropertyHolder {

    private String pathKeyWord = "view-cate";

    private String idProperty = DefaultColumnNameEnum.ID.fieldName();

    private String nameProperty = DefaultColumnNameEnum.NAME.fieldName();

    private String sPeL = "'"+ DefaultColumnNameEnum.PARENT_ID.columnName() +" = ''' + #this.id +''''";

    public TreeViewCateGeneratorConfiguration() {
        super();
    }

    public TreeViewCateGeneratorConfiguration(String sPeL) {
        super();
       this.sPeL = sPeL;
    }

}
