package org.mybatis.generator.dbstrategy;

import org.mybatis.generator.custom.db.DatabaseDDLDialects;

import static java.util.Objects.requireNonNull;

/**
 * Combined database profile for metadata introspection and DDL dialect rendering.
 *
 * @author cen_c
 */
public record DatabaseProfile(DatabaseIntrospectorStrategy introspectorStrategy, DatabaseDDLDialects ddlDialect) {
    public DatabaseProfile(DatabaseIntrospectorStrategy introspectorStrategy, DatabaseDDLDialects ddlDialect) {
        this.introspectorStrategy = requireNonNull(introspectorStrategy, "introspectorStrategy can not be null");
        this.ddlDialect = ddlDialect;
    }

}
