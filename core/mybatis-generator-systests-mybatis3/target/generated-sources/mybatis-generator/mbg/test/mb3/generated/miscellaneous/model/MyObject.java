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
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import mbg.test.common.FirstName;
import mbg.test.common.MyTime;
import org.springframework.stereotype.Repository;

/**
 * 类对应的数据库表为： PKFIELDS
 */
@Repository
@TableMeta(value = "PKFields", descript = "", beanname = "myObjectImpl")
@ApiModel(value = "MyObject", description = "")
@Setter
@Getter
public class MyObject extends MyObjectKey {
    private static final long serialVersionUID = 1L;

    /**
     */
    @ColumnMeta(value = "FIRSTNAME",description = "null",size =20,order = 20)
    @ApiModelProperty(value = "null",name = "FIRSTNAME")
    private FirstName firstname;

    /**
     */
    @ColumnMeta(value = "DATEFIELD",description = "null",size =10,order = 21,type = JDBCType.DATE,dataFormat ="yyyy-MM-dd")
    @ApiModelProperty(value = "null",name = "DATEFIELD")
    private Date startDate;

    /**
     */
    @ColumnMeta(value = "TIMEFIELD",description = "null",size =8,order = 22,type = JDBCType.TIME,dataFormat ="HH:mm:ss")
    @ApiModelProperty(value = "null",name = "TIMEFIELD")
    private MyTime timefield;

    /**
     */
    @ColumnMeta(value = "TIMESTAMPFIELD",description = "null",size =26,order = 23,type = JDBCType.TIMESTAMP,dataFormat ="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "null",name = "TIMESTAMPFIELD")
    private Date timestampfield;

    /**
     */
    @ColumnMeta(value = "DECIMAL60FIELD",description = "null",size =6,order = 24,type = JDBCType.DECIMAL)
    @ApiModelProperty(value = "null",name = "DECIMAL60FIELD")
    private int decimal60field;

    /**
     */
    @ColumnMeta(value = "DECIMAL100FIELD",description = "null",size =10,order = 25,type = JDBCType.DECIMAL)
    @ApiModelProperty(value = "null",name = "DECIMAL100FIELD")
    private Long decimal100field;

    /**
     */
    @ColumnMeta(value = "DECIMAL155FIELD",description = "null",size =15,order = 26,type = JDBCType.DECIMAL)
    @ApiModelProperty(value = "null",name = "DECIMAL155FIELD")
    private Double decimal155field;

    /**
     */
    @ColumnMeta(value = "wierd$Field",description = "null",size =32,order = 27,type = JDBCType.INTEGER)
    @ApiModelProperty(value = "null",name = "wierd$Field")
    private Integer wierdField;

    /**
     */
    @ColumnMeta(value = "birth date",description = "null",size =10,order = 28,type = JDBCType.DATE,dataFormat ="yyyy-MM-dd")
    @ApiModelProperty(value = "null",name = "birth date")
    private Date birthDate;

    /**
     */
    @ColumnMeta(value = "STRINGBOOLEAN",description = "null",size =1,order = 29,type = JDBCType.CHAR)
    @ApiModelProperty(value = "null",name = "STRINGBOOLEAN")
    private String stringboolean;

    public MyObject(Integer id2, Integer id1, FirstName firstname, String lastname, Date startDate, MyTime timefield, Date timestampfield, int decimal60field, Long decimal100field, Double decimal155field, Integer wierdField, Date birthDate, String stringboolean) {
        super(id2, id1);
        this.firstname = firstname;
        this.lastname = lastname;
        this.startDate = startDate;
        this.timefield = timefield;
        this.timestampfield = timestampfield;
        this.decimal60field = decimal60field;
        this.decimal100field = decimal100field;
        this.decimal155field = decimal155field;
        this.wierdField = wierdField;
        this.birthDate = birthDate;
        this.stringboolean = stringboolean;
    }

    public MyObject() {
        super();
    }

    public MyObject withFirstname(FirstName firstname) {
        this.setFirstname(firstname);
        return this;
    }

    public MyObject withStartDate(Date startDate) {
        this.setStartDate(startDate);
        return this;
    }

    public MyObject withTimefield(MyTime timefield) {
        this.setTimefield(timefield);
        return this;
    }

    public MyObject withTimestampfield(Date timestampfield) {
        this.setTimestampfield(timestampfield);
        return this;
    }

    public MyObject withDecimal60field(int decimal60field) {
        this.setDecimal60field(decimal60field);
        return this;
    }

    public MyObject withDecimal100field(Long decimal100field) {
        this.setDecimal100field(decimal100field);
        return this;
    }

    public MyObject withDecimal155field(Double decimal155field) {
        this.setDecimal155field(decimal155field);
        return this;
    }

    public MyObject withWierdField(Integer wierdField) {
        this.setWierdField(wierdField);
        return this;
    }

    public MyObject withBirthDate(Date birthDate) {
        this.setBirthDate(birthDate);
        return this;
    }

    public MyObject withStringboolean(String stringboolean) {
        this.setStringboolean(stringboolean);
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
        MyObject other = (MyObject) that;
        return (this.getId2() == null ? other.getId2() == null : this.getId2().equals(other.getId2()))
            && (this.getId1() == null ? other.getId1() == null : this.getId1().equals(other.getId1()))
            && (this.getFirstname() == null ? other.getFirstname() == null : this.getFirstname().equals(other.getFirstname()))
            && (this.getLastname() == null ? other.getLastname() == null : this.getLastname().equals(other.getLastname()))
            && (this.getStartDate() == null ? other.getStartDate() == null : this.getStartDate().equals(other.getStartDate()))
            && (this.getTimefield() == null ? other.getTimefield() == null : this.getTimefield().equals(other.getTimefield()))
            && (this.getTimestampfield() == null ? other.getTimestampfield() == null : this.getTimestampfield().equals(other.getTimestampfield()))
            && (this.getDecimal60field() == other.getDecimal60field())
            && (this.getDecimal100field() == null ? other.getDecimal100field() == null : this.getDecimal100field().equals(other.getDecimal100field()))
            && (this.getDecimal155field() == null ? other.getDecimal155field() == null : this.getDecimal155field().equals(other.getDecimal155field()))
            && (this.getWierdField() == null ? other.getWierdField() == null : this.getWierdField().equals(other.getWierdField()))
            && (this.getBirthDate() == null ? other.getBirthDate() == null : this.getBirthDate().equals(other.getBirthDate()))
            && (this.getStringboolean() == null ? other.getStringboolean() == null : this.getStringboolean().equals(other.getStringboolean()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId2() == null) ? 0 : getId2().hashCode());
        result = prime * result + ((getId1() == null) ? 0 : getId1().hashCode());
        result = prime * result + ((getFirstname() == null) ? 0 : getFirstname().hashCode());
        result = prime * result + ((getLastname() == null) ? 0 : getLastname().hashCode());
        result = prime * result + ((getStartDate() == null) ? 0 : getStartDate().hashCode());
        result = prime * result + ((getTimefield() == null) ? 0 : getTimefield().hashCode());
        result = prime * result + ((getTimestampfield() == null) ? 0 : getTimestampfield().hashCode());
        result = prime * result + getDecimal60field();
        result = prime * result + ((getDecimal100field() == null) ? 0 : getDecimal100field().hashCode());
        result = prime * result + ((getDecimal155field() == null) ? 0 : getDecimal155field().hashCode());
        result = prime * result + ((getWierdField() == null) ? 0 : getWierdField().hashCode());
        result = prime * result + ((getBirthDate() == null) ? 0 : getBirthDate().hashCode());
        result = prime * result + ((getStringboolean() == null) ? 0 : getStringboolean().hashCode());
        return result;
    }
}