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
public class FormOptionGeneratorConfiguration  extends PropertyHolder {

    private String idColumn;

    private String nameColumn;

    //options的数据类型，0-flat，1-tree
    private int dataType;

    public FormOptionGeneratorConfiguration(String nameColumn) {
        this.idColumn = DefaultColumnNameEnum.ID.columnName();
        this.nameColumn = nameColumn;
        dataType = 0;
    }

}
