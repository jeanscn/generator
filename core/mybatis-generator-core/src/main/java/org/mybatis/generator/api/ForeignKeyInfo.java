package org.mybatis.generator.api;

import com.vgosoft.core.constant.enums.db.ForeignKeyRuleType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForeignKeyInfo {
        private String name;
        private String pkTableName;
        private String pkColumnName;
        private String fkColumnName;
        private ForeignKeyRuleType updateRule;
        private ForeignKeyRuleType deleteRule;
        private short keySeq;

        public ForeignKeyInfo(String name, String pkTableName, String pkColumnName, String fkColumnName, Short updateRule, Short deleteRule, short keySeq) {
                this.name = name;
                this.pkTableName = pkTableName;
                this.pkColumnName = pkColumnName;
                this.fkColumnName = fkColumnName;
                this.updateRule = ForeignKeyRuleType.ofCode(Integer.valueOf(updateRule));
                this.deleteRule = ForeignKeyRuleType.ofCode(Integer.valueOf(deleteRule));
                this.keySeq = keySeq;
        }
}
