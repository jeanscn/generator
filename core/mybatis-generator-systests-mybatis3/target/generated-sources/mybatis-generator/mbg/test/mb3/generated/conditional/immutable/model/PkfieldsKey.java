/*
 * This class was generated by MyBatis Generator Vgosoft Edition.
 * 生成时间: 2021-05-08 18:00
 */
package mbg.test.mb3.generated.conditional.immutable.model;

/**
 * 类对应的数据库表为： PKFIELDS
 */
public class PkfieldsKey {
    /**
     */
    private Integer id2;

    /**
     */
    private Integer id1;

    public PkfieldsKey(Integer id2, Integer id1) {
        this.id2 = id2;
        this.id1 = id1;
    }

    public PkfieldsKey() {
        super();
    }

    public Integer getId2() {
        return id2;
    }

    public void setId2(Integer id2) {
        this.id2 = id2;
    }

    public Integer getId1() {
        return id1;
    }

    public void setId1(Integer id1) {
        this.id1 = id1;
    }
}