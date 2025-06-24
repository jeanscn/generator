package org.mybatis.generator.config;

import com.vgosoft.tool.core.VStringUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.*;

@Data
@NoArgsConstructor
public class HtmlElementDescriptor  extends AbstractHtmlElementDescriptor{

    private IntrospectedColumn column;

    private HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    private String name;

    private String tagType;

    private String dataFormat;

    private String otherFieldName;

    private String dataSource;

    private String beanName;

    private String applyProperty;

    private String applyPropertyKey;

    private List<String> verify = new ArrayList<>();

    private String enumClassName;

    private String switchText;

    private String dictCode;

    private String callback;

    private String labelCss;

    private String elementCss;

    private String dataFmt;

    private boolean dateRange = false;

    private String listViewClass;

    private boolean multiple;

    private boolean remoteApiParse = false;

    private boolean remoteToTree = false;

    private String remoteValueType;

    private boolean remoteAsync = false;

    private String keyMapLabel;

    private String keyMapValue;
    //是否排除自己
    private boolean excludeSelf = false;

    private String parentFormKey;

    private String designIdField;

    private String configJsonfield = "jsonContent";

    private Set<String> watchFields = new HashSet<>();

    private String dateRangeSeparator;

    private boolean renderHref;

    private String hrefDataKeyField;

    private String tips;

    private final List<HtmlHrefElementConfiguration> htmlHrefElementConfigurations = new ArrayList<>();

    public HtmlElementDescriptor(HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HtmlElementDescriptor)) return false;
        HtmlElementDescriptor that = (HtmlElementDescriptor) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    public String getEnumClassName() {
        if (enumClassName != null) {
            return enumClassName;
        } else  if(VStringUtil.stringHasValue(this.dataFormat)){
            return Mb3GenUtil.getEnumClassNameByDataFmt(this.dataFormat);
        }
        return null;
    }

    public String getSwitchText() {
        if (VStringUtil.stringHasValue(switchText)) {
            return switchText;
        }else if(VStringUtil.stringHasValue(this.dataFormat)){
            return Mb3GenUtil.getSwitchTextByDataFmt(this.dataFormat);
        }
        return null;
    }
}
