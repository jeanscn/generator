package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-11 13:43
 * @version 3.0
 */
@Getter
public class VoColumnRenderFunGeneratorConfiguration extends TypedPropertyHolder {

    private final TableConfiguration tc;

    private final Context context;

    @Setter
    private List<String> fieldNames = new ArrayList<>();

    @Setter
    private String renderFun;

    public VoColumnRenderFunGeneratorConfiguration(Context context, TableConfiguration tc) {
        this.tc = tc;
        this.context = context;
    }

}
