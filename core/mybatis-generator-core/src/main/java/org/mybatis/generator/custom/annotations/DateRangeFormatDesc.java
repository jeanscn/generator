package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.DateRangeFormat;
import com.vgosoft.tool.core.VStringUtil;

public class DateRangeFormatDesc  extends AbstractAnnotation{

        public static final String ANNOTATION_NAME = "@"+DateRangeFormat.class.getSimpleName();

        private String source;

        private String pattern;

        private String separator;

        private String startPlaceholder;

        private String endPlaceholder;

        public static DateRangeFormatDesc create(){
            return new DateRangeFormatDesc();
        }

        public DateRangeFormatDesc() {
            super();
            this.addImports(DateRangeFormat.class.getCanonicalName());
        }

        public DateRangeFormatDesc(String source) {
            this();
            this.source = source;
        }

        public DateRangeFormatDesc(String source,String pattern) {
            this(source);
            this.pattern = pattern;
        }

        public DateRangeFormatDesc(String source,String pattern,String separator) {
            this(source,pattern);
            this.separator = separator;
        }

        @Override
        public String toAnnotation() {
            items.clear();
            if (VStringUtil.isNotBlank(source)) {
                this.items.add(VStringUtil.format("source = \"{0}\"", this.source));
            }
            if (VStringUtil.isNotBlank(pattern) && !"yyyy年MM月dd日".equals(pattern)) {
                this.items.add(VStringUtil.format("pattern = \"{0}\"", this.pattern));
            }
            if (VStringUtil.isNotBlank(separator) && !"-".equals(separator)) {
                this.items.add(VStringUtil.format("separator = \"{0}\"", this.separator));
            }
            if (VStringUtil.isNotBlank(startPlaceholder)) {
                this.items.add(VStringUtil.format("startPlaceholder = \"{0}\"", this.startPlaceholder));
            }
            if (VStringUtil.isNotBlank(endPlaceholder)) {
                this.items.add(VStringUtil.format("endPlaceholder = \"{0}\"", this.endPlaceholder));
            }
            if (!this.items.isEmpty()) return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
            else return ANNOTATION_NAME;
        }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getStartPlaceholder() {
        return startPlaceholder;
    }

    public void setStartPlaceholder(String startPlaceholder) {
        this.startPlaceholder = startPlaceholder;
    }

    public String getEndPlaceholder() {
        return endPlaceholder;
    }

    public void setEndPlaceholder(String endPlaceholder) {
        this.endPlaceholder = endPlaceholder;
    }
}
