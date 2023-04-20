package org.mybatis.generator.config;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-11 13:43
 * @version 3.0
 */
public class VoColumnRenderFunGeneratorConfiguration  extends TypedPropertyHolder{

    private final TableConfiguration tc;

    private final Context context;

    private String column;

    private String renderFun;


    public VoColumnRenderFunGeneratorConfiguration(Context context,TableConfiguration tc) {
        this.tc = tc;
        this.context = context;
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

    public String getRenderFun() {
        return renderFun;
    }

    public void setRenderFun(String renderFun) {
        this.renderFun = renderFun;
    }
}
