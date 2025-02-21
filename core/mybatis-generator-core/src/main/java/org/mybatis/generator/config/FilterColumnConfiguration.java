package org.mybatis.generator.config;

import org.mybatis.generator.api.IntrospectedColumn;

import java.util.ArrayList;
import java.util.List;

public class FilterColumnConfiguration extends AbstractFilterConditionConfiguration {
    private boolean repeat = false;
    private List<String> operators = new ArrayList<>();

    public FilterColumnConfiguration(TableConfiguration tc) {
        super(tc);
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public List<String> getOperators() {
        return operators;
    }

    public void setOperators(List<String> operators) {
        this.operators = operators;
    }

}
