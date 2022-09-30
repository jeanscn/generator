package org.mybatis.generator.config;

import java.util.ArrayList;
import java.util.List;

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

    public TableConfiguration getTc() {
        return tc;
    }

    public Context getContext() {
        return context;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }
}
