package mbg.test.common;

/**
 * @author Jeff Butler
 */
public class BaseClass {
    private String lastname;

    /**
     *
     */
    public BaseClass() {
        super();
    }

    // these methods are final so that an error will be generated
    // if the extended class tries to overwrite them.  This
    // will break the build as the generator should
    // see a duplicate property.
    public final String getLastname() {
        return lastname;
    }

    public final void setLastname(String lastname) {
        this.lastname = lastname;
    }
}
