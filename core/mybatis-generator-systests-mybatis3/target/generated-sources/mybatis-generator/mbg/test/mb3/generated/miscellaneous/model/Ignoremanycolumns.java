/*
 * This class was generated by MyBatis Generator Vgosoft Edition.
 * 生成时间: 2021-05-08 18:00
 */
package mbg.test.mb3.generated.miscellaneous.model;

import com.vgosoft.core.annotation.ColumnMeta;
import com.vgosoft.core.annotation.TableMeta;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.sql.JDBCType;
import lombok.Getter;
import lombok.Setter;
import mbg.test.common.BaseClass;
import org.springframework.stereotype.Repository;

/**
 * 类对应的数据库表为： IGNOREMANYCOLUMNS
 */
@Repository
@TableMeta(value = "IgnoreManyColumns", descript = "", beanname = "ignoremanycolumnsImpl")
@ApiModel(value = "Ignoremanycolumns", description = "")
@Setter
@Getter
public class Ignoremanycolumns extends BaseClass {
    private static final long serialVersionUID = 1L;

    /**
     */
    @ColumnMeta(value = "COL01",description = "null",size =32,order = 20,type = JDBCType.INTEGER)
    @ApiModelProperty(value = "null",name = "COL01")
    private Integer col01;

    /**
     */
    @ColumnMeta(value = "COL13",description = "null",size =32,order = 21,type = JDBCType.INTEGER)
    @ApiModelProperty(value = "null",name = "COL13")
    private Integer col13;

    public Ignoremanycolumns(Integer col01, Integer col13) {
        this.col01 = col01;
        this.col13 = col13;
    }

    public Ignoremanycolumns() {
        super();
    }

    public Ignoremanycolumns withCol01(Integer col01) {
        this.setCol01(col01);
        return this;
    }

    public Ignoremanycolumns withCol13(Integer col13) {
        this.setCol13(col13);
        return this;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Ignoremanycolumns other = (Ignoremanycolumns) that;
        return (this.getCol01() == null ? other.getCol01() == null : this.getCol01().equals(other.getCol01()))
            && (this.getCol13() == null ? other.getCol13() == null : this.getCol13().equals(other.getCol13()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCol01() == null) ? 0 : getCol01().hashCode());
        result = prime * result + ((getCol13() == null) ? 0 : getCol13().hashCode());
        return result;
    }
}