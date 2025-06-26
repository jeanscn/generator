package org.mybatis.generator.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FilterColumnConfiguration extends AbstractFilterConditionConfiguration {
    private boolean repeat = false;
    private List<String> operators = new ArrayList<>();

    public FilterColumnConfiguration(TableConfiguration tc) {
        super(tc);
    }
}
