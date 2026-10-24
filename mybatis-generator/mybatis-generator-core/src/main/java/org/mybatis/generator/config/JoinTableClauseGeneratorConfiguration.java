package org.mybatis.generator.config;

import com.vgosoft.core.constant.enums.db.JoinTableTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * @author cen_c
 */
@Setter
@Getter
public class JoinTableClauseGeneratorConfiguration extends PropertyHolder {

    private String joinType = JoinTableTypeEnum.INNER.code();
    private String joinTable;
    private String tableAlias;
    private String onSearchConditionExpr;
    private List<String> clauseColumns = new ArrayList<>();

    public JoinTableClauseGeneratorConfiguration() {
        super();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) { return false; }
        JoinTableClauseGeneratorConfiguration that = (JoinTableClauseGeneratorConfiguration) o;
        return Objects.equals(joinTable, that.joinTable) && Objects.equals(tableAlias, that.tableAlias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(joinTable, tableAlias);
    }
}
