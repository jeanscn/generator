/*
 * This class was generated by MyBatis Generator Vgosoft Edition.
 * 生成时间: 2021-05-08 18:00
 */
package mbg.test.mb3.generated.annotated.hierarchical.Immutable.Model;

import com.vgosoft.core.annotation.ColumnMeta;
import com.vgosoft.core.annotation.TableMeta;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Repository;

/**
 * 类对应的数据库表为： PKFIELDSBLOBS
 */
@Repository
@TableMeta(value = "PKFieldsBlobs", descript = "", beanname = "pkfieldsblobsImpl")
@ApiModel(value = "Pkfieldsblobs", description = "")
@Setter
@Getter
public class Pkfieldsblobs extends PkfieldsblobsKey {
    private static final long serialVersionUID = 1L;

    /**
     */
    @ColumnMeta(value = "FIRSTNAME",description = "null",size =20,order = 20)
    @ApiModelProperty(value = "null",name = "FIRSTNAME")
    private String firstname;

    /**
     */
    @ColumnMeta(value = "LASTNAME",description = "null",size =20,order = 21)
    @ApiModelProperty(value = "null",name = "LASTNAME")
    private String lastname;

    public Pkfieldsblobs(Integer id1, Integer id2, String firstname, String lastname) {
        super(id1, id2);
        this.firstname = firstname;
        this.lastname = lastname;
    }
}