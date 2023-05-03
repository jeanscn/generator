package mbg.test.common;

/**
 * @author Jeff Butler
 *
 */
public class MyTime {
    private int hours;
    private int minutes;
    private int seconds;

    /**
     *
     */
    public MyTime() {
        super();
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 == null) {
            return false;
        }

        MyTime other = (MyTime) arg0;

        return this.hours == other.hours
            && this.minutes == other.minutes
            && this.seconds == other.seconds;
    }

    @Override
    public int hashCode() {
        return hours + minutes + seconds;
    }
}
