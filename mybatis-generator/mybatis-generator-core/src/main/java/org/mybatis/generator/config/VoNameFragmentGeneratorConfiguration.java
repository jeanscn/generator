package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoNameFragmentGeneratorConfiguration extends TypedPropertyHolder {

    private final TableConfiguration tc;

    private final Context context;

    private String column;

    private String fragment = "EqualTo";


    public VoNameFragmentGeneratorConfiguration(Context context, TableConfiguration tc) {
        super();
        this.context = context;
        this.tc = tc;
    }

}
