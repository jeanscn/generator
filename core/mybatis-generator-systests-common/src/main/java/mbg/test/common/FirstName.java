package mbg.test.common;

/**
 * @author Jeff Butler
 *
 */
public class FirstName {

    private String value;

    /**
     *
     */
    public FirstName() {
        super();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 == null) {
            return false;
        }

        return value.equals(((FirstName)arg0).getValue());
    }

    @Override
    public int hashCode() {
        return value == null ? 0 : value.hashCode();
    }

}
