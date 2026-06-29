package org.mybatis.generator.dbstrategy.introspectorstrategy;

import org.mybatis.generator.api.FullyQualifiedTable;

import java.sql.Connection;
import java.util.Collections;
import java.util.Map;

/**
 * Generic fallback strategy for databases without custom metadata queries.
 * @author cen_c
 */
public class GenericIntrospectorStrategy extends QueryBackedRemarksIntrospectorStrategy {
    @Override
    public void getIndexDetails(Connection conn, FullyQualifiedTable table,
                                Map<String, String> indexComments,
                                Map<String, String> indexTypes) {
        // fall back to JDBC metadata only
    }

    @Override
    public Map<String, String> getIndexComments(Connection conn, FullyQualifiedTable table) {
        return Collections.emptyMap();
    }

}
