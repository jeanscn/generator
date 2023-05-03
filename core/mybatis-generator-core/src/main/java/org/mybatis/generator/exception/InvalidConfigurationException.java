package org.mybatis.generator.exception;

import java.util.List;

public class InvalidConfigurationException extends MultiMessageException {

    private static final long serialVersionUID = 4854214073644581094L;

    public InvalidConfigurationException(List<String> errors) {
        super(errors);
    }
}
