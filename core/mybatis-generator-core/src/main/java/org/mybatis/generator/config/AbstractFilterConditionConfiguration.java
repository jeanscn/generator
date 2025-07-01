package org.mybatis.generator.config;

import com.vgosoft.core.constant.enums.core.QueryModesEnum;
import com.vgosoft.core.constant.enums.db.FieldTypeEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementDataFormat;
import com.vgosoft.core.constant.enums.view.HtmlElementDataSourceEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mybatis.generator.api.IntrospectedColumn;

@Getter
@Setter
@NoArgsConstructor
public class AbstractFilterConditionConfiguration extends TypedPropertyHolder {
    private IntrospectedColumn introspectedColumn;
    private String tableName;
    private String tableAlias;
    private String column;
    private HtmlElementTagTypeEnum tagName;
    private String field;
    private String remark;
    private QueryModesEnum queryMode;
    private int order;
    private FieldTypeEnum fieldType;
    private HtmlElementDataFormat dataFormat;
    private HtmlElementDataSourceEnum dataSource;
    private String enumClassFullName;
    private String dictCode;
    private String switchText;
    private String dataUrl;
    private boolean multiple;
    private boolean range;
    private String listKey;

    public AbstractFilterConditionConfiguration(TableConfiguration tc) {
        super();
        this.tableName = tc.getTableName();
        this.tableAlias = tc.getAlias();
    }

}
