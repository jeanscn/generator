package org.mybatis.generator.plugins;

import java.util.List;
import java.util.StringTokenizer;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;

/**
 * This plugin can be used to specify columns that act as a primary key, even if
 * they are not strictly defined as primary keys in the database.
 *
 * <p>To use the plugin, add a property to the table configuration specifying a
 * comma delimited list of column names to use as a primary key:
 *
 * <p>&lt;property name="virtualKeyColumns" value="ID1,ID2"&gt;
 *
 * @author Jeff Butler
 *
 */
public class VirtualPrimaryKeyPlugin extends PluginAdapter {

    /* (non-Javadoc)
     * @see org.mybatis.generator.api.Plugin#validate(java.util.List)
     */
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.mybatis.generator.api.PluginAdapter#initialized(org.mybatis.generator.api.IntrospectedTable)
     */
    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        String virtualKey = introspectedTable.getTableConfiguration()
                .getProperty("virtualKeyColumns"); //$NON-NLS-1$

        if (virtualKey != null) {
            StringTokenizer st = new StringTokenizer(virtualKey, ", ", false); //$NON-NLS-1$
            while (st.hasMoreTokens()) {
                String column = st.nextToken();
                introspectedTable.addPrimaryKeyColumn(column);
            }
        }
    }
}
