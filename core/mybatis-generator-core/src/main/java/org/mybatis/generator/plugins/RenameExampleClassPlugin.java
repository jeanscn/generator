package org.mybatis.generator.plugins;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;

/**
 * This plugin demonstrates overriding the initialized() method to rename the
 * generated example classes. Instead of xxxExample, the classes will be named
 * xxxCriteria.
 *
 * <p>This plugin accepts two properties:
 *
 * <ul>
 * <li><code>searchString</code> (required) the regular expression of the name
 * search.</li>
 * <li><code>replaceString</code> (required) the replacement String.</li>
 * </ul>
 *
 * <p>For example, to change the name of the generated Example classes from
 * xxxExample to xxxCriteria, specify the following:
 *
 * <dl>
 * <dt>searchString</dt>
 * <dd>Example$</dd>
 * <dt>replaceString</dt>
 * <dd>Criteria</dd>
 * </dl>
 *
 *
 * @author Jeff Butler
 *
 */
public class RenameExampleClassPlugin extends PluginAdapter {
    private String replaceString;
    private Pattern pattern;

    @Override
    public boolean validate(List<String> warnings) {

        String searchString = properties.getProperty("searchString");
        replaceString = properties.getProperty("replaceString");

        boolean valid = stringHasValue(searchString)
                && stringHasValue(replaceString);

        if (valid) {
            pattern = Pattern.compile(searchString);
        } else {
            if (!stringHasValue(searchString)) {
                warnings.add(getString("ValidationError.18",
                        "RenameExampleClassPlugin",
                        "searchString"));
            }
            if (!stringHasValue(replaceString)) {
                warnings.add(getString("ValidationError.18",
                        "RenameExampleClassPlugin",
                        "replaceString"));
            }
        }

        return valid;
    }

    @Override
    public void initialized(AbstractIntrospectedTable introspectedTable) {
        String oldType = introspectedTable.getExampleType();
        Matcher matcher = pattern.matcher(oldType);
        oldType = matcher.replaceAll(replaceString);

        introspectedTable.setExampleType(oldType);
    }
}
