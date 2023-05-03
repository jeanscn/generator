package org.mybatis.generator.config;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.List;

/**
 * This class is used to specify a renaming rule for columns in a table. This
 * renaming rule will be run against all column names before calculating the
 * corresponding property name. The most common use case is when columns in a
 * table are all prefixed by a certain value.
 *
 * <p>For example, if columns in a table are named:
 *
 * <ul>
 * <li>CUST_NAME</li>
 * <li>CUST_ADDRESS</li>
 * <li>CUST_CITY</li>
 * <li>CUST_STATE</li>
 * </ul>
 *
 * <p>it might be annoying to have the generated properties all containing the CUST
 * prefix. This class can be used to remove the prefix by specifying
 *
 * <ul>
 * <li>searchString = "^CUST"</li>
 * <li>replaceString=""</li>
 * </ul>
 *
 * <p>Note that internally, the generator uses the
 * <code>java.util.regex.Matcher.replaceAll</code> method for this function. See
 * the documentation of that method for example of the regular expression
 * language used in Java.
 *
 * @author Jeff Butler
 *
 */
public class ColumnRenamingRule {
    private String searchString;
    private String replaceString;

    public String getReplaceString() {
        return replaceString;
    }

    public void setReplaceString(String replaceString) {
        this.replaceString = replaceString;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public void validate(List<String> errors, String tableName) {
        if (!stringHasValue(searchString)) {
            errors.add(getString("ValidationError.14", tableName)); //$NON-NLS-1$
        }
    }
}
