package org.mybatis.generator.internal.rules;

import org.mybatis.generator.api.AbstractIntrospectedTable;

/**
 * This class encapsulates all the code generation rules for a table using the
 * flat model.
 *
 * @author Jeff Butler
 *
 */
public class FlatModelRules extends BaseRules {

    /**
     * Instantiates a new flat model rules.
     *
     * @param abstractIntrospectedTable
     *            the introspected table
     */
    public FlatModelRules(AbstractIntrospectedTable abstractIntrospectedTable) {
        super(abstractIntrospectedTable);
    }

    /**
     * We never generate a primary key in the flat model.
     *
     * @return true if the primary key should be generated
     */
    @Override
    public boolean generatePrimaryKeyClass() {
        return false;
    }

    /**
     * We always generate a base record in the flat model.
     *
     * @return true if the class should be generated
     */
    @Override
    public boolean generateBaseRecordClass() {
        return true;
    }

    /**
     * We never generate a record with BLOBs class in the flat model.
     *
     * @return true if the record with BLOBs class should be generated
     */
    @Override
    public boolean generateRecordWithBlobsClass() {
        return false;
    }
}
