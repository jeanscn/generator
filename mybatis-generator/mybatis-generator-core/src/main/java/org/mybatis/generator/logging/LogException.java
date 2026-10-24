package org.mybatis.generator.logging;

public class LogException extends RuntimeException {

    private static final long serialVersionUID = 7522435242386492002L;

    public LogException() {
        super();
    }

    public LogException(String message) {
        super(message);
    }

    public LogException(String message, Throwable cause) {
        super(message, cause);
    }

    public LogException(Throwable cause) {
        super(cause);
    }

}
