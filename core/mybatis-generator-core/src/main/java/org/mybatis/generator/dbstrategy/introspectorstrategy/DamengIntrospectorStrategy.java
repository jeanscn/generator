package org.mybatis.generator.dbstrategy.introspectorstrategy;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.dbstrategy.DatabaseIntrospectorStrategy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author cen_c
 */
public class DamengIntrospectorStrategy implements DatabaseIntrospectorStrategy {

	@Override
	public void getIndexDetails(Connection conn, FullyQualifiedTable table,
								Map<String, String> indexComments,
								Map<String, String> indexTypes) throws SQLException {
		String schema = resolveSchema(conn, table);
		// noinspection SqlResolve
		String sql = "SELECT ai.INDEX_NAME, ai.INDEX_TYPE, aic.COMMENTS AS INDEX_COMMENT " +
				"FROM ALL_INDEXES ai " +
				"LEFT JOIN ALL_IND_COMMENTS aic ON ai.OWNER = aic.INDEX_OWNER AND ai.INDEX_NAME = aic.INDEX_NAME " +
				"WHERE ai.TABLE_OWNER = ? AND ai.TABLE_NAME = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, schema);
			ps.setString(2, normalizeIdentifier(table.getIntrospectedTableName()));

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String indexName = rs.getString("INDEX_NAME");
					String indexType = rs.getString("INDEX_TYPE");
					String indexComment = rs.getString("INDEX_COMMENT");

					if (indexName != null) {
						if (indexType != null) {
							indexTypes.put(indexName, indexType);
						}
						if (indexComment != null) {
							indexComments.put(indexName, indexComment);
						}
					}
				}
			}
		}
	}

	@Override
	public Map<String, String> getIndexComments(Connection conn, FullyQualifiedTable table) throws SQLException {
		Map<String, String> indexComments = new HashMap<>();
		getIndexDetails(conn, table, indexComments, new HashMap<>());
		return indexComments;
	}

	@Override
	public Map<String, String> getColumnRemarks(Connection conn, String tableName) throws SQLException {
		Map<String, String> remarks = new HashMap<>();
		String schema = resolveCurrentSchema(conn);
		// noinspection SqlResolve
		String sql = "SELECT COLUMN_NAME, COMMENTS FROM ALL_COL_COMMENTS WHERE OWNER = ? AND TABLE_NAME = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, schema);
			ps.setString(2, normalizeIdentifier(tableName));

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String columnName = rs.getString("COLUMN_NAME");
					String comment = rs.getString("COMMENTS");
					if (columnName != null && comment != null) {
						remarks.put(columnName, comment);
					}
				}
			}
		}
		return remarks;
	}

	@Override
	public void updateTableRemark(AbstractIntrospectedTable abstractIntrospectedTable,
								  Connection conn) throws SQLException {
		FullyQualifiedTable table = abstractIntrospectedTable.getFullyQualifiedTable();
		String schema = resolveSchema(conn, table);
		// noinspection SqlResolve
		String sql = "SELECT COMMENTS FROM ALL_TAB_COMMENTS WHERE OWNER = ? AND TABLE_NAME = ?";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, schema);
			ps.setString(2, normalizeIdentifier(table.getIntrospectedTableName()));

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String remark = rs.getString("COMMENTS");
					if (remark != null && !remark.trim().isEmpty()) {
						abstractIntrospectedTable.setRemarks(remark);
					}
				}
			}
		}
	}

	private String resolveSchema(Connection conn, FullyQualifiedTable table) throws SQLException {
		String schema = table.getIntrospectedSchema();
		if (schema == null || schema.trim().isEmpty()) {
			schema = table.getIntrospectedCatalog();
		}
		if (schema == null || schema.trim().isEmpty()) {
			schema = resolveCurrentSchema(conn);
		}
		return normalizeIdentifier(schema);
	}

	private String resolveCurrentSchema(Connection conn) throws SQLException {
		String schema = conn.getSchema();
		if (schema != null && !schema.trim().isEmpty()) {
			return normalizeIdentifier(schema);
		}

		schema = conn.getMetaData().getUserName();
		if (schema != null && !schema.trim().isEmpty()) {
			return normalizeIdentifier(schema);
		}

		throw new SQLException("Cannot resolve current schema for Dameng database");
	}

	private String normalizeIdentifier(String identifier) {
		return identifier == null ? null : identifier.trim().toUpperCase(Locale.ROOT);
	}

}
