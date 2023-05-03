package org.mybatis.generator.config.xml;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class ParserErrorHandler implements ErrorHandler {

    private final List<String> warnings;

    private final List<String> errors;

    public ParserErrorHandler(List<String> warnings, List<String> errors) {
        super();
        this.warnings = warnings;
        this.errors = errors;
    }

    @Override
    public void warning(SAXParseException exception) {
        warnings.add(getString("Warning.7", //$NON-NLS-1$
                Integer.toString(exception.getLineNumber()), exception
                        .getMessage()));
    }

    @Override
    public void error(SAXParseException exception) {
        errors.add(getString("RuntimeError.4", //$NON-NLS-1$
                Integer.toString(exception.getLineNumber()), exception
                        .getMessage()));
    }

    @Override
    public void fatalError(SAXParseException exception) {
        errors.add(getString("RuntimeError.4", //$NON-NLS-1$
                Integer.toString(exception.getLineNumber()), exception
                        .getMessage()));
    }
}
