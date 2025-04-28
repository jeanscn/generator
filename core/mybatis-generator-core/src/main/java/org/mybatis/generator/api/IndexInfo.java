package org.mybatis.generator.api;

import java.util.*;

public class IndexInfo {
    private final String name;
    private final boolean unique;
    private final Map<Short, String> columns = new TreeMap<>(); // 使用TreeMap保持列顺序
    private final Map<String, String> columnSortTypes = new HashMap<>(); // 存储排序方式(ASC/DESC)
    private String comments; // 索引注释

    public IndexInfo(String name, boolean unique) {
        this.name = name;
        this.unique = unique;
    }

    /**
     * 创建索引信息
     *
     * @param name     索引名称
     * @param unique   是否唯一索引
     * @param comments 索引注释
     */
    public IndexInfo(String name, boolean unique, String comments) {
        this.name = name;
        this.unique = unique;
        this.comments = comments;
    }

    public void addColumn(String columnName, short position, String sortType) {
        columns.put(position, columnName);
        if (sortType != null) {
            columnSortTypes.put(columnName, sortType);
        }
    }

    // getter方法
    public String getName() {
        return name;
    }

    public boolean isUnique() {
        return unique;
    }

    public List<String> getColumnNames() {
        return new ArrayList<>(columns.values());
    }

    public String getSortType(String columnName) {
        return columnSortTypes.getOrDefault(columnName, "ASC");
    }


    /**
     * 获取索引注释
     */
    public String getComments() {
        return comments;
    }

    /**
     * 设置索引注释
     *
     * @param comments 索引注释
     */
    public void setComments(String comments) {
        this.comments = comments;
    }
}
