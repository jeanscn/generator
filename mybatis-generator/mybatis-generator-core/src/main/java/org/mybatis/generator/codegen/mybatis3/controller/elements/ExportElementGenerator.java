package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;
import org.mybatis.generator.custom.annotations.swagger3.OperationDesc;
import org.mybatis.generator.custom.enums.ReturnTypeEnum;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities.getAliasedEscapedColumnName;
import static org.mybatis.generator.internal.util.StringUtility.escapeStringForJava;

import static org.mybatis.generator.custom.ConstantsUtil.ANNOTATION_REQUEST_PARAM_SPLIT;

/**
 * @author cen_c
 */
public class ExportElementGenerator extends AbstractControllerElementGenerator {

    public ExportElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        if (introspectedTable.getPrimaryKeyColumns().size() != 1
                || !FullyQualifiedJavaType.getStringInstance().equals(
                introspectedTable.getPrimaryKeyColumns().get(0).getFullyQualifiedJavaType())) {
            if (warnings != null) {
                warnings.add("Export endpoints skipped for " + introspectedTable.getBaseRecordType()
                        + ": export scopes require a single String primary key.");
            }
            return;
        }
        IntrospectedColumn primaryKey = introspectedTable.getPrimaryKeyColumns().get(0);
        parentElement.addImportedType("jakarta.servlet.http.HttpServletResponse");
        parentElement.addImportedType("com.vgosoft.plugins.excel.service.VgoFesodSheet");
        parentElement.addImportedType("java.io.IOException");
        parentElement.addImportedType(entityExcelVoType);
        parentElement.addImportedType(entityType);
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());

        final String methodPrefix = "export";
        Method method = createMethod(methodPrefix);

        FullyQualifiedJavaType response = new FullyQualifiedJavaType("jakarta.servlet.http.HttpServletResponse");
        Parameter parameter = new Parameter(response, "response");
        parameter.setRemark("http响应");
        method.addParameter(parameter);
        FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
        listInstance.addTypeArgument(FullyQualifiedJavaType.getStringInstance());
        Parameter idsParam = new Parameter(listInstance, "ids");
        idsParam.setRemark("需要导出的数据id列表");
        idsParam.addAnnotation("@RequestParam");
        idsParam.addAnnotation("@RequestParamSplit");
        parentElement.addImportedType(ANNOTATION_REQUEST_PARAM_SPLIT);
        method.addParameter(idsParam);
        method.addException(new FullyQualifiedJavaType("java.io.IOException"));
        method.setExceptionRemark("IO读写异常");

        method.addAnnotation(new SystemLogDesc("数据导出", introspectedTable), parentElement);
        method.addAnnotation(new RequestMappingDesc("export", RequestMethodEnum.GET), parentElement);
        addSecurityPreAuthorize(method, methodPrefix, "导出");
        method.addAnnotation(new OperationDesc("Excel数据导出", "Excel数据导出接口"), parentElement);

        commentGenerator.addMethodJavaDocLine(method, "Excel数据导出");

        method.addBodyLine("{0} example = new {0}();", exampleType.getShortName());
        method.addBodyLine("example.createCriteria().and{0}In(ids);", capitalize(primaryKey.getJavaProperty()));
        method.addBodyLine("ServiceResult<List<{0}>> result = {1}.selectByExample(example);",
                entityType.getShortName(), serviceBeanName);
        method.addBodyLine("if (result == null || !result.isSuccess()) {");
        method.addBodyLine("throw new IllegalStateException(result == null ? \"Export query failed\" : result.getMessage());");
        method.addBodyLine("}");
        method.addBodyLine("List<{0}> list = mappings.to{0}s(result.getResult()).stream()",
                entityExcelVoType.getShortName());
        method.addBodyLine("        .map(JsonUtil::serializeObject)\n" +
                "                .collect(Collectors.toList());");
        method.addBodyLine("VgoFesodSheet.write(response, \"{1}数据\", \"{1}\", {0}.class, list, getDefaultColumnWidthStyleStrategy(\"export\"));",entityExcelVoType.getShortName(), introspectedTable.getRemarks(true));
        parentElement.addMethod(method);
        parentElement.addImportedType("java.util.stream.Collectors");
        parentElement.addImportedType("com.vgosoft.tool.core.JsonUtil");
        addPersonalizedExport(parentElement, primaryKey);
    }

    private void addPersonalizedExport(TopLevelClass parent, IntrospectedColumn primaryKey) {
        FullyQualifiedJavaType queryType = getMethodParameterVoType("list");
        for (String type : List.of("com.vgosoft.plugins.excel.export.ExportSupport",
                "com.vgosoft.plugins.excel.pojo.vo.VgoExportRequestVo",
                "com.vgosoft.plugins.excel.pojo.vo.VgoExportOptionsVo",
                "com.vgosoft.plugins.excel.pojo.vo.VgoExportOptionsViewVo",
                "com.github.pagehelper.PageHelper", "java.util.Map", "java.util.LinkedHashMap",
                "java.util.Iterator", "java.util.NoSuchElementException", "java.util.Collections",
                "java.util.HashSet", "org.springframework.web.bind.annotation.RequestBody",
                "org.springframework.validation.annotation.Validated",
                "jakarta.validation.groups.Default",
                "com.vgosoft.core.valid.ValidateQuery")) {
            parent.addImportedType(type);
        }
        parent.addImportedType(queryType);
        parent.addImportedType(exampleType);
        Set<String> unsupportedColumns = unsupportedExportColumns();
        Method allowedColumns = createMethod("exportAllowedColumns");
        allowedColumns.setReturnType(new FullyQualifiedJavaType("java.util.Set<java.lang.String>"));
        parent.addImportedType("java.util.Set");
        allowedColumns.addBodyLine("Set<String> unsupported = Set.of("
                + unsupportedColumns.stream().map(name -> "\"" + escapeStringForJava(name) + "\"")
                .collect(java.util.stream.Collectors.joining(", ")) + ");");
        allowedColumns.addBodyLine("return ExportSupport.schema(" + entityExcelVoType.getShortName()
                + ".class).stream().map(column -> column.field()).filter(field -> !unsupported.contains(field))"
                + ".collect(Collectors.toSet());");
        parent.addMethod(allowedColumns);
        String allowedCall = allowedColumns.getName() + "()";

        Method columns = createMethod("exportSortColumns");
        columns.setReturnType(new FullyQualifiedJavaType("java.util.Map<java.lang.String, java.lang.String>"));
        columns.addBodyLine("Map<String, String> columns = new LinkedHashMap<>();");
        introspectedTable.getAllColumns().stream().filter(column -> !column.isBlobColumn())
                .forEach(column -> columns.addBodyLine("columns.put(\"" + escapeStringForJava(column.getJavaProperty())
                        + "\", \"" + escapeStringForJava(getAliasedEscapedColumnName(column)) + "\");"));
        columns.addBodyLine("return Collections.unmodifiableMap(columns);");
        parent.addMethod(columns);
        String columnsCall = columns.getName() + "()";

        Method metadata = createMethod("exportOptions");
        metadata.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_MODEL,
                new FullyQualifiedJavaType("com.vgosoft.plugins.excel.pojo.vo.VgoExportOptionsViewVo"), parent));
        exportAnnotations(metadata, parent, "export/options", RequestMethodEnum.GET, "导出选项");
        metadata.addBodyLine("VgoExportOptionsViewVo options = ExportSupport.metadata("
                + entityExcelVoType.getShortName() + ".class, " + columnsCall + ");");
        metadata.addBodyLine("options.setMaxScopeIds(500);");
        metadata.addBodyLine("Set<String> allowed = " + allowedCall + ";");
        metadata.addBodyLine("options.setColumns(options.getColumns().stream()"
                + ".filter(column -> allowed.contains(column.field())).collect(Collectors.toList()));");
        metadata.addBodyLine("return ResponseResult.success(options);");
        parent.addMethod(metadata);

        Method export = createMethod("exportPost");
        FullyQualifiedJavaType request = new FullyQualifiedJavaType(
                "com.vgosoft.plugins.excel.pojo.vo.VgoExportRequestVo");
        request.addTypeArgument(queryType);
        Parameter body = new Parameter(request, "request");
        body.addAnnotation("@RequestBody");
        body.addAnnotation("@Validated({Default.class, ValidateQuery.class})");
        export.addParameter(body);
        export.addParameter(new Parameter(new FullyQualifiedJavaType("jakarta.servlet.http.HttpServletResponse"), "response"));
        export.addException(new FullyQualifiedJavaType("java.io.IOException"));
        exportAnnotations(export, parent, "export", RequestMethodEnum.POST, "个性化数据导出");
        export.addBodyLine("if (request == null || request.getQuery() == null || request.getOptions() == null) {");
        export.addBodyLine("throw new IllegalArgumentException(\"Export query and options are required\");");
        export.addBodyLine("}");
        export.addBodyLine(queryType.getShortName() + " query = request.getQuery();");
        export.addBodyLine("VgoExportOptionsVo options = request.getOptions();");
        export.addBodyLine("Map<String, String> sortColumns = " + columnsCall + ";");
        export.addBodyLine("ExportSupport.validate(options, " + entityExcelVoType.getShortName() + ".class, sortColumns);");
        export.addBodyLine("if (!" + allowedCall + ".containsAll(options.getColumns())) {");
        export.addBodyLine("throw new IllegalArgumentException(\"Export columns include fields not populated by the query\");");
        export.addBodyLine("}");
        export.addBodyLine("int formatCap = \"pdf\".equals(options.getFormat()) ? 5000 : 50000;");
        export.addBodyLine("if (options.getMaxRows() < 1 || options.getMaxRows() > formatCap) {");
        export.addBodyLine("throw new IllegalArgumentException(\"Export maxRows exceeds the format limit\");");
        export.addBodyLine("}");
        export.addBodyLine("List<String> ids = ExportSupport.ids(options);");
        export.addBodyLine("boolean idScope = !\"all\".equals(options.getScope());");
        export.addBodyLine("if (idScope && (ids.isEmpty() || ids.size() > 500 || new HashSet<>(ids).size() != ids.size())) {");
        export.addBodyLine("throw new IllegalArgumentException(\"Page/selected export requires 1 to 500 distinct IDs\");");
        export.addBodyLine("}");
        addQuerySanitization(export, queryType);
        export.addBodyLine(exampleType.getShortName() + " example = buildExample(query);");
        export.addBodyLine("example.setIgnorePermissionAnnotation(false);");
        export.addBodyLine("example.setIgnoreDeleteFlag(false);");
        export.addBodyLine("if (example.getOredCriteria().isEmpty()) { example.createCriteria(); }");
        if (queryHasField(queryType, "filterParam")) {
            parent.addImportedType("com.vgosoft.web.utils.ParameterUtil");
            parent.addImportedType("com.vgosoft.core.adapter.web.FilterParam");
            parent.addImportedType("com.vgosoft.tool.core.VStringUtil");
            export.addBodyLine("FilterParam filterParam = query.getFilterParam();");
            export.addBodyLine("if (filterParam != null) {");
            export.addBodyLine("String filterSql = ParameterUtil.parseFilterMap(filterParam, \""
                    + escapeStringForJava(introspectedTable.getTableConfiguration().getAlias() == null
                    ? "" : introspectedTable.getTableConfiguration().getAlias()) + "\");");
            export.addBodyLine("if (VStringUtil.stringHasValue(filterSql)) {");
            export.addBodyLine("example.getOredCriteria().forEach(criteria -> criteria.andAnyCondition(filterSql));");
            export.addBodyLine("}");
            export.addBodyLine("}");
        }
        export.addBodyLine("if (idScope) {");
        export.addBodyLine("if ((long) ids.size() * example.getOredCriteria().size() > 1000) {");
        export.addBodyLine("throw new IllegalArgumentException(\"Export IDs across query OR branches exceed the portable parameter budget\");");
        export.addBodyLine("}");
        export.addBodyLine("example.getOredCriteria().forEach(criteria -> criteria.and"
                + capitalize(primaryKey.getJavaProperty()) + "In(ids));");
        export.addBodyLine("}");
        export.addBodyLine("example.setOrderByClause(ExportSupport.orderBy(options, sortColumns, \""
                + escapeStringForJava(defaultOrder()) + "\", \""
                + escapeStringForJava(getAliasedEscapedColumnName(primaryKey)) + "\"));");
        String entity = entityType.getShortName();
        String excel = entityExcelVoType.getShortName();
        String batchCall = createBatchQuery(parent).getName();
        export.addBodyLine("try {");
        export.addBodyLine("if (idScope) {");
        export.addBodyLine("List<" + entity + "> records = " + batchCall + "(example, 0, ids.size());");
        export.addBodyLine("if (records.size() != ids.size() || !records.stream().map("
                + entity + "::get" + capitalize(primaryKey.getJavaProperty())
                + ").collect(Collectors.toSet()).equals(new HashSet<>(ids))) {");
        export.addBodyLine("throw new IllegalArgumentException(\"Some requested records are missing, deleted or unauthorized\");");
        export.addBodyLine("}");
        export.addBodyLine("if (options.getSorts() == null || options.getSorts().isEmpty()) {");
        export.addBodyLine("records = ExportSupport.restoreOrder(records, ids, " + entity
                + "::get" + capitalize(primaryKey.getJavaProperty()) + ");");
        export.addBodyLine("}");
        export.addBodyLine("records = records.subList(0, Math.min(records.size(), options.getMaxRows()));");
        export.addBodyLine("List<" + excel + "> rows = mappings.to" + excel
                + "s(records).stream().map(JsonUtil::serializeObject).collect(Collectors.toList());");
        export.addBodyLine("VgoFesodSheet.export(response, options, " + excel + ".class, Collections.singletonList(rows));");
        export.addBodyLine("return;");
        export.addBodyLine("}");
        export.addBodyLine("List<" + entity + "> firstBatch = " + batchCall
                + "(example, 0, Math.min(500, options.getMaxRows()));");
        export.addBodyLines("""
                Iterable<List<%s>> batches = () -> new Iterator<List<%s>>() {
                private List<%s> pending = firstBatch;
                private int offset;
                private boolean finished;
                @Override
                public boolean hasNext() {
                if (finished) { return false; }
                if (pending == null) {
                pending = %s(example, offset, Math.min(500, options.getMaxRows() - offset));
                }
                if (pending.isEmpty()) { finished = true; return false; }
                return true;
                }
                @Override
                public List<%s> next() {
                if (!hasNext()) { throw new NoSuchElementException(); }
                int requested = Math.min(500, options.getMaxRows() - offset);
                List<%s> records = pending;
                pending = null;
                offset += records.size();
                finished = records.size() < requested || offset >= options.getMaxRows();
                return mappings.to%ss(records).stream().map(JsonUtil::serializeObject).collect(Collectors.toList());
                }
                };
                VgoFesodSheet.export(response, options, %s.class, batches);
                } finally {
                PageHelper.clearPage();
                }
                """.formatted(excel, excel, entity, batchCall, excel, entity, excel, excel).lines().toList());
        parent.addMethod(export);
    }

    private Method createBatchQuery(TopLevelClass parent) {
        Method batch = createMethod("exportBatch");
        batch.setReturnType(new FullyQualifiedJavaType("java.util.List<" + entityType.getFullyQualifiedName() + ">"));
        batch.addParameter(new Parameter(exampleType, "example"));
        batch.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(), "offset"));
        batch.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(), "size"));
        batch.addBodyLine("try {");
        batch.addBodyLine("PageHelper.offsetPage(offset, size, false).setReasonable(false);");
        batch.addBodyLine("ServiceResult<List<" + entityType.getShortName() + ">> result = "
                + serviceBeanName + ".selectByExample(example);");
        batch.addBodyLine("if (result == null || !result.isSuccess() || result.getResult() == null) {");
        batch.addBodyLine("throw new IllegalStateException(result == null ? \"Export query failed\" : result.getMessage());");
        batch.addBodyLine("}");
        batch.addBodyLine("return result.getResult();");
        batch.addBodyLine("} finally {");
        batch.addBodyLine("PageHelper.clearPage();");
        batch.addBodyLine("}");
        parent.addMethod(batch);
        return batch;
    }

    private void addQuerySanitization(Method method, FullyQualifiedJavaType queryType) {
        for (String field : List.of("ignorePermissionAnnotation", "ignoreDeleteFlag", "anyWhereCondition", "orderByClause")) {
            if (queryHasField(queryType, field)) {
                method.addBodyLine("query.set" + capitalize(field) + "("
                        + (field.startsWith("ignore") ? "false" : "null") + ");");
            }

        }
    }

    private Set<String> unsupportedExportColumns() {
        Set<String> unsupported = introspectedTable.getBlobColumns().stream()
                .map(IntrospectedColumn::getJavaProperty)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        var vo = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration();
        if (vo != null && vo.getVoExcelConfiguration() != null) {
            for (var mapping : vo.getVoExcelConfiguration().getOverridePropertyConfigurations()) {
                boolean blobSource = introspectedTable.getBlobColumns().stream()
                        .anyMatch(column -> column.getActualColumnName()
                                .equalsIgnoreCase(mapping.getSourceColumnName()));
                if (blobSource) {
                    if (mapping.getTargetPropertyName() != null) {
                        unsupported.add(mapping.getTargetPropertyName());
                    }
                    if (mapping.getTargetColumnName() != null) {
                        introspectedTable.getColumn(mapping.getTargetColumnName())
                                .ifPresent(column -> unsupported.add(column.getJavaProperty()));
                    }
                }
            }
        }
        return unsupported;
    }

    private boolean queryHasField(FullyQualifiedJavaType type, String field) {
        return introspectedTable.getTopLevelClassExampleFields()
                .getOrDefault(type.getShortName(), List.of()).contains(field);
    }

    private String defaultOrder() {
        return introspectedTable.getAllColumns().stream()
                .filter(column -> List.of(DefaultColumnNameEnum.SORT.columnName(),
                                DefaultColumnNameEnum.CREATE_TIME.columnName(),
                                DefaultColumnNameEnum.UPDATE_TIME.columnName()).stream()
                        .anyMatch(name -> name.equalsIgnoreCase(column.getActualColumnName())))
                .map(column -> getAliasedEscapedColumnName(column)
                        + (DefaultColumnNameEnum.SORT.columnName()
                        .equalsIgnoreCase(column.getActualColumnName()) ? " asc" : " desc"))
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private void exportAnnotations(Method method, TopLevelClass parent, String path,
                                   RequestMethodEnum verb, String summary) {
        method.addAnnotation(new SystemLogDesc(summary, introspectedTable), parent);
        method.addAnnotation(new RequestMappingDesc(path, verb), parent);
        addSecurityPreAuthorize(method, "export", "导出");
        method.addAnnotation(new OperationDesc(summary, summary), parent);
    }

    private static String capitalize(String property) {
        return Character.toUpperCase(property.charAt(0)) + property.substring(1);
    }

}
