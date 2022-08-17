/*
 *    Copyright 2006-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.config;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;

public class VOViewGeneratorConfiguration extends AbstractGeneratorConfiguration {

    private List<String> includeColumns = new ArrayList<>();

    private  List<String> excludeColumns = new ArrayList<>();

    private String indexColumn = "";

    private List<String> actionColumn = new ArrayList<>();

    private List<String> queryColumns = new ArrayList<>();

    public VOViewGeneratorConfiguration() {
        super();
    }

    public VOViewGeneratorConfiguration(Context context) {
        super();
        targetProject = context.getJavaModelGeneratorConfiguration().getTargetProject();
        baseTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".");
        targetPackage = String.join(".",baseTargetPackage,"pojo");
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "VOViewGeneratorConfiguration");
    }

    public List<String> getIncludeColumns() {
        return includeColumns;
    }

    public void setIncludeColumns(List<String> includeColumns) {
        this.includeColumns = includeColumns;
    }

    public List<String> getExcludeColumns() {
        return excludeColumns;
    }

    public void setExcludeColumns(List<String> excludeColumns) {
        this.excludeColumns = excludeColumns;
    }

    public String getIndexColumn() {
        return indexColumn;
    }

    public void setIndexColumn(String indexColumn) {
        this.indexColumn = indexColumn;
    }

    public List<String> getActionColumn() {
        return actionColumn;
    }

    public void setActionColumn(List<String> actionColumn) {
        this.actionColumn = actionColumn;
    }

    public List<String> getQueryColumns() {
        return queryColumns;
    }

    public void setQueryColumns(List<String> queryColumns) {
        this.queryColumns = queryColumns;
    }
}
