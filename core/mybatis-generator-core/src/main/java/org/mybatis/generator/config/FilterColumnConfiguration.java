package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FilterColumnConfiguration extends AbstractFilterConditionConfiguration {
    private boolean repeat = false;
    private List<String> operators = new ArrayList<>();

    public FilterColumnConfiguration(TableConfiguration tc) {
        super(tc);
    }
}
