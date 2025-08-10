package org.mybatis.generator.codegen.mybatis3.model;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getGetterMethodName;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class ExampleGenerator extends AbstractJavaGenerator {

    public ExampleGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        List<CompilationUnit> answer = new ArrayList<>();
        if (introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration() == null
                || !introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration().isGenerate()) {
            return answer;
        }
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.6", table.toString()));
        CommentGenerator commentGenerator = context.getCommentGenerator();

        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        TopLevelClass topLevelClass = new TopLevelClass(type);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(topLevelClass,false);

        // add default constructor
        Method method = new Method(type.getShortName());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setConstructor(true);
        method.addBodyLine("oredCriteria = new ArrayList<>();");

        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);

        // add field, getter, setter for orderByClause
        Field orderByClause = new Field("orderByClause", FullyQualifiedJavaType.getStringInstance());
        orderByClause.setVisibility(JavaVisibility.PROTECTED);
        commentGenerator.addFieldComment(orderByClause, introspectedTable);
        topLevelClass.addField(orderByClause);

        method = new Method("setOrderByClause");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "orderByClause"));
        method.addBodyLine("this.orderByClause = orderByClause;");
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);

        method = new Method("getOrderByClause");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getStringInstance());
        method.addBodyLine("return orderByClause;");
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);

        // add field, getter, setter for distinct
        Field distinct = new Field("distinct", FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        distinct.setVisibility(JavaVisibility.PROTECTED);
        commentGenerator.addFieldComment(distinct, introspectedTable);
        topLevelClass.addField(distinct);

        method = new Method("setDistinct");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(
                new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "distinct"));
        method.addBodyLine("this.distinct = distinct;");
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);

        method = new Method("isDistinct");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        method.addBodyLine("return distinct;");
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);

        /*
         * add field, getter, setter for ignoreDeleteFlag
         * protected boolean ignoreDeleteFlag;
         */
        Field ignoreDeleteFlag = new Field("ignoreDeleteFlag", FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        ignoreDeleteFlag.setVisibility(JavaVisibility.PROTECTED);
        commentGenerator.addFieldComment(ignoreDeleteFlag, introspectedTable);
        topLevelClass.addField(ignoreDeleteFlag);
        method = new Method("setIgnoreDeleteFlag");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "ignoreDeleteFlag"));
        method.addBodyLine("this.ignoreDeleteFlag = ignoreDeleteFlag;");
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);
        method = new Method("isIgnoreDeleteFlag");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        method.addBodyLine("return ignoreDeleteFlag;");
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);

        /*
         * add field, getter, setter for ignorePermissionAnnotation
         * protected boolean ignorePermissionAnnotation;
         */
        Field ignorePermissionAnnotation = new Field("ignorePermissionAnnotation", FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        ignorePermissionAnnotation.setVisibility(JavaVisibility.PROTECTED);
        commentGenerator.addFieldComment(ignorePermissionAnnotation, introspectedTable);
        topLevelClass.addField(ignorePermissionAnnotation);
        method = new Method("setIgnorePermissionAnnotation");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "ignorePermissionAnnotation"));
        method.addBodyLine("this.ignorePermissionAnnotation = ignorePermissionAnnotation;");
        commentGenerator.addMethodJavaDocLine(method,"ignorePermissionAnnotation的getter方法");
        topLevelClass.addMethod(method);
        method = new Method("isIgnorePermissionAnnotation");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        method.addBodyLine("return ignorePermissionAnnotation;");
        commentGenerator.addMethodJavaDocLine(method,"ignorePermissionAnnotation的setter方法");
        topLevelClass.addMethod(method);

        // add field and methods for the list of ored criteria
        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType("java.util.List<Criteria>");
        Field oredCriteria = new Field("oredCriteria", fqjt);
        oredCriteria.setVisibility(JavaVisibility.PROTECTED);

        commentGenerator.addFieldComment(oredCriteria, introspectedTable);
        topLevelClass.addField(oredCriteria);

        method = new Method("getOredCriteria");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(fqjt);
        method.addBodyLine("return oredCriteria;");
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);

        method = new Method("or");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(FullyQualifiedJavaType.getCriteriaInstance(), "criteria"));
        method.addBodyLine("oredCriteria.add(criteria);");
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);

        method = new Method("or");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
        method.addBodyLine("Criteria criteria = createCriteriaInternal();");
        method.addBodyLine("oredCriteria.add(criteria);");
        method.addBodyLine("return criteria;");
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);

        method = new Method("createCriteria");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
        method.addBodyLine("Criteria criteria = createCriteriaInternal();");
        method.addBodyLine("if (oredCriteria.isEmpty()) {");
        method.addBodyLine("oredCriteria.add(criteria);");
        method.addBodyLine("}");
        method.addBodyLine("return criteria;");
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);

        method = new Method("createCriteriaInternal");
        method.setVisibility(JavaVisibility.PROTECTED);
        method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
        method.addBodyLine("Criteria criteria = new Criteria();");
        method.addBodyLine("return criteria;");
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);

        method = new Method("clear");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addBodyLine("oredCriteria.clear();");
        method.addBodyLine("orderByClause = null;");
        method.addBodyLine("distinct = false;");
        method.addBodyLine("ignoreDeleteFlag = false;");
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);

        // now generate the inner class that holds the AND conditions
        topLevelClass.addInnerClass(getGeneratedCriteriaInnerClass(topLevelClass));

        topLevelClass.addInnerClass(getCriteriaInnerClass());

        topLevelClass.addInnerClass(getCriterionInnerClass());

        if (context.getPlugins().modelExampleClassGenerated(topLevelClass, introspectedTable)) {
            answer.add(topLevelClass);
        }
        return answer;
    }

    private InnerClass getCriterionInnerClass() {
        InnerClass answer = new InnerClass(new FullyQualifiedJavaType("Criterion"));
        answer.setVisibility(JavaVisibility.PUBLIC);
        answer.setStatic(true);
        context.getCommentGenerator().addClassComment(answer, introspectedTable);

        Field field = new Field("condition", FullyQualifiedJavaType.getStringInstance());
        field.setVisibility(JavaVisibility.PRIVATE);
        answer.addField(field);
        answer.addMethod(getGetter(field));

        field = new Field("value", FullyQualifiedJavaType.getObjectInstance());
        field.setVisibility(JavaVisibility.PRIVATE);
        answer.addField(field);
        answer.addMethod(getGetter(field));

        field = new Field("secondValue", FullyQualifiedJavaType.getObjectInstance());
        field.setVisibility(JavaVisibility.PRIVATE);
        answer.addField(field);
        answer.addMethod(getGetter(field));

        field = new Field("noValue", FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        field.setVisibility(JavaVisibility.PRIVATE);
        answer.addField(field);
        answer.addMethod(getGetter(field));

        field = new Field("singleValue", FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        field.setVisibility(JavaVisibility.PRIVATE);
        answer.addField(field);
        answer.addMethod(getGetter(field));

        field = new Field("betweenValue", FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        field.setVisibility(JavaVisibility.PRIVATE);
        answer.addField(field);
        answer.addMethod(getGetter(field));

        field = new Field("listValue", FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        field.setVisibility(JavaVisibility.PRIVATE);
        answer.addField(field);
        answer.addMethod(getGetter(field));

        field = new Field("typeHandler", FullyQualifiedJavaType.getStringInstance());
        field.setVisibility(JavaVisibility.PRIVATE);
        answer.addField(field);
        answer.addMethod(getGetter(field));

        Method method = new Method("Criterion");
        method.setVisibility(JavaVisibility.PROTECTED);
        method.setConstructor(true);
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "condition"));
        method.addBodyLine("super();");
        method.addBodyLine("this.condition = condition;");
        method.addBodyLine("this.typeHandler = null;");
        method.addBodyLine("this.noValue = true;");
        answer.addMethod(method);

        method = new Method("Criterion");
        method.setVisibility(JavaVisibility.PROTECTED);
        method.setConstructor(true);
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "condition"));
        method.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(), "value"));
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "typeHandler"));
        method.addBodyLine("super();");
        method.addBodyLine("this.condition = condition;");
        method.addBodyLine("this.value = value;");
        method.addBodyLine("this.typeHandler = typeHandler;");
        method.addBodyLine("if (value instanceof List<?>) {");
        method.addBodyLine("this.listValue = true;");
        method.addBodyLine("} else {");
        method.addBodyLine("this.singleValue = true;");
        method.addBodyLine("}");
        answer.addMethod(method);

        method = createCriterionConstructor();
        method.addBodyLine("this(condition, value, null);");
        answer.addMethod(method);

        method = createCriterionConstructor();
        method.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(), "secondValue"));
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "typeHandler"));
        method.addBodyLine("super();");
        method.addBodyLine("this.condition = condition;");
        method.addBodyLine("this.value = value;");
        method.addBodyLine("this.secondValue = secondValue;");
        method.addBodyLine("this.typeHandler = typeHandler;");
        method.addBodyLine("this.betweenValue = true;");
        answer.addMethod(method);

        method = createCriterionConstructor();
        method.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(), "secondValue"));
        method.addBodyLine("this(condition, value, secondValue, null);");
        answer.addMethod(method);

        return answer;
    }

    private Method createCriterionConstructor() {
        Method method = new Method("Criterion");
        method.setVisibility(JavaVisibility.PROTECTED);
        method.setConstructor(true);
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "condition"));
        method.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(), "value"));
        return method;
    }

    private InnerClass getCriteriaInnerClass() {
        InnerClass answer = new InnerClass(FullyQualifiedJavaType.getCriteriaInstance());

        answer.setVisibility(JavaVisibility.PUBLIC);
        answer.setStatic(true);
        answer.setSuperClass(FullyQualifiedJavaType.getGeneratedCriteriaInstance());

        context.getCommentGenerator().addClassComment(answer, introspectedTable, true);

        Method method = new Method("Criteria");
        method.setVisibility(JavaVisibility.PROTECTED);
        method.setConstructor(true);
        method.addBodyLine("super();");
        answer.addMethod(method);

        /*
         * 增加一个通用的equalTo方法
         * public void andEqualTo(String fieldName, Object value) {
            addCriterion(new StringBuilder(fieldName).append(" = ").append(value).toString());
            }
         */
        method = new Method("andEqualTo");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "fieldName"));
        method.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(), "value"));
        if (stringHasValue(introspectedTable.getTableConfiguration().getAlias())) {
            method.addBodyLine("StringBuilder sb = new StringBuilder(\"{0}\").append(\".\").append(fieldName);", introspectedTable.getTableConfiguration().getAlias());
        } else {
            method.addBodyLine("StringBuilder sb = new StringBuilder(fieldName);");
        }
        method.addBodyLine("addCriterion(sb.append(\" = \").append(value).toString());");
        answer.addMethod(method);
        return answer;
    }

    private InnerClass getGeneratedCriteriaInnerClass(TopLevelClass topLevelClass) {
        Field field;

        InnerClass answer = new InnerClass(FullyQualifiedJavaType.getGeneratedCriteriaInstance());

        answer.setVisibility(JavaVisibility.PROTECTED);
        answer.setStatic(true);
        answer.setAbstract(true);
        context.getCommentGenerator().addClassComment(answer, introspectedTable);

        Method method = new Method("GeneratedCriteria");
        method.setVisibility(JavaVisibility.PROTECTED);
        method.setConstructor(true);
        method.addBodyLine("super();");
        method.addBodyLine("criteria = new ArrayList<>();");
        answer.addMethod(method);

        List<String> criteriaLists = new ArrayList<>();
        criteriaLists.add("criteria");

        for (IntrospectedColumn introspectedColumn : introspectedTable.getNonBLOBColumns()) {
            if (stringHasValue(introspectedColumn.getTypeHandler())) {
                String name = addTypeHandledObjectsAndMethods(introspectedColumn, method, answer);
                criteriaLists.add(name);
            }
        }

        // now generate the isValid method
        method = new Method("isValid");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        StringBuilder sb = new StringBuilder();
        Iterator<String> strIter = criteriaLists.iterator();
        sb.append("return ");
        sb.append(strIter.next());
        sb.append(".size() > 0");
        if (!strIter.hasNext()) {
            sb.append(';');
        }
        method.addBodyLine(sb.toString());
        while (strIter.hasNext()) {
            sb.setLength(0);
            OutputUtilities.javaIndent(sb, 1);
            sb.append("|| ");
            sb.append(strIter.next());
            sb.append(".size() > 0");
            if (!strIter.hasNext()) {
                sb.append(';');
            }
            method.addBodyLine(sb.toString());
        }
        answer.addMethod(method);

        // now generate the getAllCriteria method
        if (criteriaLists.size() > 1) {
            field = new Field("allCriteria",
                    new FullyQualifiedJavaType("List<Criterion>"));
            field.setVisibility(JavaVisibility.PROTECTED);
            answer.addField(field);
        }

        method = new Method("getAllCriteria");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("List<Criterion>"));
        if (criteriaLists.size() < 2) {
            method.addBodyLine("return criteria;");
        } else {
            method.addBodyLine("if (allCriteria == null) {");
            method.addBodyLine("allCriteria = new ArrayList<>();");

            strIter = criteriaLists.iterator();
            while (strIter.hasNext()) {
                method.addBodyLine(String.format("allCriteria.addAll(%s);", strIter.next()));
            }

            method.addBodyLine("}");
            method.addBodyLine("return allCriteria;");
        }
        answer.addMethod(method);

        // now we need to generate the methods that will be used in the SqlMap
        // to generate the dynamic where clause
        topLevelClass.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        topLevelClass.addImportedType(FullyQualifiedJavaType.getNewArrayListInstance());

        FullyQualifiedJavaType listOfCriterion =
                new FullyQualifiedJavaType("java.util.List<Criterion>");
        field = new Field("criteria", listOfCriterion);
        field.setVisibility(JavaVisibility.PROTECTED);
        answer.addField(field);

        method = new Method(getGetterMethodName(field.getName(), field.getType()));
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(field.getType());
        method.addBodyLine("return criteria;");
        answer.addMethod(method);

        // now add the methods for simplifying the individual field set methods
        method = new Method("addCriterion");
        method.setVisibility(JavaVisibility.PROTECTED);
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "condition"));
        method.addBodyLine("if (condition == null) {");
        method.addBodyLine("throw new RuntimeException(\"Value for condition cannot be null\");");
        method.addBodyLine("}");
        method.addBodyLine("criteria.add(new Criterion(condition));");
        if (criteriaLists.size() > 1) {
            method.addBodyLine("allCriteria = null;");
        }
        answer.addMethod(method);

        method = new Method("addCriterion");
        method.setVisibility(JavaVisibility.PROTECTED);
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "condition"));
        method.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(), "value"));
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "property"));
        method.addBodyLine("if (value == null) {");
        method.addBodyLine(
                "throw new RuntimeException(\"Value for \" + property + \" cannot be null\");");
        method.addBodyLine("}");
        method.addBodyLine("criteria.add(new Criterion(condition, value));");
        if (criteriaLists.size() > 1) {
            method.addBodyLine("allCriteria = null;");
        }
        answer.addMethod(method);

        method = new Method("addCriterion");
        method.setVisibility(JavaVisibility.PROTECTED);
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "condition"));
        method.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(), "value1"));
        method.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(), "value2"));
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "property"));
        method.addBodyLine("if (value1 == null || value2 == null) {");
        method.addBodyLine(
                "throw new RuntimeException(\"Between values for \" + property + \" cannot be null\");");
        method.addBodyLine("}");
        method.addBodyLine("criteria.add(new Criterion(condition, value1, value2));");
        if (criteriaLists.size() > 1) {
            method.addBodyLine("allCriteria = null;");
        }
        answer.addMethod(method);

        FullyQualifiedJavaType listOfDates =
                new FullyQualifiedJavaType("java.util.List<java.util.Date>");

        if (introspectedTable.hasJDBCDateColumns()) {
            topLevelClass.addImportedType(FullyQualifiedJavaType.getDateInstance());
            topLevelClass.addImportedType(FullyQualifiedJavaType.getNewIteratorInstance());
            method = new Method("addCriterionForJDBCDate");
            method.setVisibility(JavaVisibility.PROTECTED);
            method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "condition"));
            method.addParameter(new Parameter(FullyQualifiedJavaType.getDateInstance(), "value"));
            method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "property"));
            method.addBodyLine("if (value == null) {");
            method.addBodyLine(
                    "throw new RuntimeException(\"Value for \" + property + \" cannot be null\");");
            method.addBodyLine("}");
            method.addBodyLine(
                    "addCriterion(condition, new java.sql.Date(value.getTime()), property);");
            answer.addMethod(method);

            method = new Method("addCriterionForJDBCDate");
            method.setVisibility(JavaVisibility.PROTECTED);
            method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "condition"));
            method.addParameter(new Parameter(listOfDates, "values"));
            method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "property"));
            method.addBodyLine("if (values == null || values.size() == 0) {");
            method.addBodyLine(
                    "throw new RuntimeException(\"Value list for \" + property + \""
                            + " cannot be null or empty\");");
            method.addBodyLine("}");
            method.addBodyLine("List<java.sql.Date> dateList = new ArrayList<>();");
            method.addBodyLine("Iterator<Date> iter = values.iterator();");
            method.addBodyLine("while (iter.hasNext()) {");
            method.addBodyLine("dateList.add(new java.sql.Date(iter.next().getTime()));");
            method.addBodyLine("}");
            method.addBodyLine("addCriterion(condition, dateList, property);");
            answer.addMethod(method);

            method = new Method("addCriterionForJDBCDate");
            method.setVisibility(JavaVisibility.PROTECTED);
            method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "condition"));
            method.addParameter(new Parameter(FullyQualifiedJavaType.getDateInstance(), "value1"));
            method.addParameter(new Parameter(FullyQualifiedJavaType.getDateInstance(), "value2"));
            method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "property"));
            method.addBodyLine("if (value1 == null || value2 == null) {");
            method.addBodyLine(
                    "throw new RuntimeException(\"Between values for \" + property + \""
                            + " cannot be null\");");
            method.addBodyLine("}");
            method.addBodyLine(
                    "addCriterion(condition, new java.sql.Date(value1.getTime()),"
                            + " new java.sql.Date(value2.getTime()), property);");
            answer.addMethod(method);
        }

        if (introspectedTable.hasJDBCTimeColumns()) {
            topLevelClass.addImportedType(FullyQualifiedJavaType.getDateInstance());
            topLevelClass.addImportedType(FullyQualifiedJavaType.getNewIteratorInstance());
            method = new Method("addCriterionForJDBCTime");
            method.setVisibility(JavaVisibility.PROTECTED);
            method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "condition"));
            method.addParameter(new Parameter(FullyQualifiedJavaType.getDateInstance(), "value"));
            method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "property"));
            method.addBodyLine("if (value == null) {");
            method.addBodyLine(
                    "throw new RuntimeException(\"Value for \" + property + \" cannot be null\");");
            method.addBodyLine("}");
            method.addBodyLine(
                    "addCriterion(condition, new java.sql.Time(value.getTime()), property);");
            answer.addMethod(method);

            method = new Method("addCriterionForJDBCTime");
            method.setVisibility(JavaVisibility.PROTECTED);
            method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "condition"));
            method.addParameter(new Parameter(listOfDates, "values"));
            method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "property"));
            method.addBodyLine("if (values == null || values.size() == 0) {");
            method.addBodyLine(
                    "throw new RuntimeException(\"Value list for \" + property + \""
                            + " cannot be null or empty\");");
            method.addBodyLine("}");
            method.addBodyLine("List<java.sql.Time> timeList = new ArrayList<>();");
            method.addBodyLine("Iterator<Date> iter = values.iterator();");
            method.addBodyLine("while (iter.hasNext()) {");
            method.addBodyLine("timeList.add(new java.sql.Time(iter.next().getTime()));");
            method.addBodyLine("}");
            method.addBodyLine("addCriterion(condition, timeList, property);");
            answer.addMethod(method);

            method = new Method("addCriterionForJDBCTime");
            method.setVisibility(JavaVisibility.PROTECTED);
            method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "condition"));
            method.addParameter(new Parameter(FullyQualifiedJavaType.getDateInstance(), "value1"));
            method.addParameter(new Parameter(FullyQualifiedJavaType.getDateInstance(), "value2"));
            method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "property"));
            method.addBodyLine("if (value1 == null || value2 == null) {");
            method.addBodyLine(
                    "throw new RuntimeException(\"Between values for \" + property + \""
                            + " cannot be null\");");
            method.addBodyLine("}");
            method.addBodyLine(
                    "addCriterion(condition, new java.sql.Time(value1.getTime()),"
                            + " new java.sql.Time(value2.getTime()), property);");
            answer.addMethod(method);
        }

        for (IntrospectedColumn introspectedColumn : introspectedTable.getNonBLOBColumns()) {
            topLevelClass.addImportedType(introspectedColumn.getFullyQualifiedJavaType());

            // here we need to add the individual methods for setting the
            // conditions for a field
            answer.addMethod(getSetNullMethod(introspectedColumn));
            answer.addMethod(getSetNotNullMethod(introspectedColumn));
            answer.addMethod(getSetEqualMethod(introspectedColumn));
            answer.addMethod(getSetNotEqualMethod(introspectedColumn));
            answer.addMethod(getSetGreaterThanMethod(introspectedColumn));
            answer.addMethod(getSetGreaterThenOrEqualMethod(introspectedColumn));
            answer.addMethod(getSetLessThanMethod(introspectedColumn));
            answer.addMethod(getSetLessThanOrEqualMethod(introspectedColumn));

            if (introspectedColumn.isJdbcCharacterColumn()) {
                answer.addMethod(getSetLikeMethod(introspectedColumn));
                answer.addMethod(getSetNotLikeMethod(introspectedColumn));
                answer.addMethod(getSetNotLikeAnyMethod(introspectedColumn));
                answer.addMethod(getSetLikeAnyMethod(introspectedColumn));
                answer.addMethod(getSetLikeLeftMethod(introspectedColumn));
                answer.addMethod(getSetLikeRightMethod(introspectedColumn));
                answer.addMethod(getSetEmptyMethod(introspectedColumn));
                answer.addMethod(getSetNotEmptyMethod(introspectedColumn));
                answer.addMethod(getSetEqualUpperCaseMethod(introspectedColumn));
                answer.addMethod(getSetNotEqualUpperCaseMethod(introspectedColumn));
            }

            answer.addMethod(getSetInOrNotInMethod(introspectedColumn, true));
            answer.addMethod(getSetInOrNotInMethod(introspectedColumn, false));
            answer.addMethod(getSetBetweenOrNotBetweenMethod(introspectedColumn, true));
            answer.addMethod(getSetBetweenOrNotBetweenMethod(introspectedColumn, false));
            answer.addMethod(getAnyConditionMethod(introspectedColumn));
        }
        answer.addMethod(getAnyAnyConditionMethod());
        return answer;
    }

    private Method getSetNullMethod(IntrospectedColumn introspectedColumn) {
        return getNoValueMethod(introspectedColumn, "IsNull", "is null");
    }

    private Method getSetNotNullMethod(IntrospectedColumn introspectedColumn) {
        return getNoValueMethod(introspectedColumn, "IsNotNull", "is not null");
    }

    private Method getSetEqualUpperCaseMethod(IntrospectedColumn introspectedColumn) {
        return getSingleValueMethod(introspectedColumn, "EqualUpperCase", "=");
    }

    private Method getSetNotEqualUpperCaseMethod(IntrospectedColumn introspectedColumn) {
        return getSingleValueMethod(introspectedColumn, "NotEqualUpperCase", "<>");
    }

    private Method getSetEqualMethod(IntrospectedColumn introspectedColumn) {
        return getSingleValueMethod(introspectedColumn, "EqualTo", "=");
    }

    private Method getSetNotEqualMethod(IntrospectedColumn introspectedColumn) {
        return getSingleValueMethod(introspectedColumn, "NotEqualTo", "<>");
    }

    private Method getSetGreaterThanMethod(IntrospectedColumn introspectedColumn) {
        return getSingleValueMethod(introspectedColumn, "GreaterThan", ">");
    }

    private Method getSetGreaterThenOrEqualMethod(IntrospectedColumn introspectedColumn) {
        return getSingleValueMethod(introspectedColumn, "GreaterThanOrEqualTo", ">=");
    }

    private Method getSetLessThanMethod(IntrospectedColumn introspectedColumn) {
        return getSingleValueMethod(introspectedColumn, "LessThan", "<");
    }

    private Method getSetLessThanOrEqualMethod(IntrospectedColumn introspectedColumn) {
        return getSingleValueMethod(introspectedColumn, "LessThanOrEqualTo", "<=");
    }

    private Method getSetLikeMethod(IntrospectedColumn introspectedColumn) {
        return getSingleValueMethod(introspectedColumn, "Like", "like");
    }

    private Method getSetLikeAnyMethod(IntrospectedColumn introspectedColumn) {
        return getSingleValueMethod(introspectedColumn, "LikeAny", "like");
    }

    private Method getSetLikeLeftMethod(IntrospectedColumn introspectedColumn) {
        return getSingleValueMethod(introspectedColumn, "LikeLeft", "like");
    }

    private Method getSetLikeRightMethod(IntrospectedColumn introspectedColumn) {
        return getSingleValueMethod(introspectedColumn, "LikeRight", "like");
    }

    private Method getSetNotLikeMethod(IntrospectedColumn introspectedColumn) {
        return getSingleValueMethod(introspectedColumn, "NotLike", "not like");
    }

    private Method getSetNotLikeAnyMethod(IntrospectedColumn introspectedColumn) {
        return getSingleValueMethod(introspectedColumn, "NotLikeAny", "not like");
    }

    private Method getSetNotEmptyMethod(IntrospectedColumn introspectedColumn) {
        return getNotEmptyMethod(introspectedColumn, "NotEmpty", "!= ''");
    }

    private Method getSetEmptyMethod(IntrospectedColumn introspectedColumn) {
        return getEmptyMethod(introspectedColumn, "IsEmpty", "= ''");
    }

    private Method getSingleValueMethod(IntrospectedColumn introspectedColumn, String nameFragment, String operator) {

        StringBuilder sb = new StringBuilder();
        sb.append(initializeAndMethodName(introspectedColumn));
        sb.append(nameFragment);

        Method method = new Method(sb.toString());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(), "value"));
        method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());

        sb.setLength(0);
        sb.append(initializeAddLine(introspectedColumn, nameFragment));
        sb.append(' ');
        sb.append(operator);
        sb.append("\", ");
        switch (nameFragment) {
            case "LikeAny":
            case "NotLikeAny":
                sb.append("'%'+value+'%'");
                break;
            case "LikeRight":
                sb.append("'%'+value");
                break;
            case "LikeLeft":
                sb.append("value+'%'");
                break;
            default:
                sb.append("value");
        }
        sb.append(", \"");
        sb.append(introspectedColumn.getJavaProperty());
        sb.append("\");");
        method.addBodyLine(sb.toString());
        method.addBodyLine("return (Criteria) this;");

        return method;
    }

    private String initializeAndMethodName(IntrospectedColumn introspectedColumn) {
        StringBuilder sb = new StringBuilder();
        sb.append(introspectedColumn.getJavaProperty());
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        sb.insert(0, "and");
        return sb.toString();
    }

    private String initializeAddLine(IntrospectedColumn introspectedColumn, String nameFragment) {
        StringBuilder sb = new StringBuilder();
        if (introspectedColumn.isJDBCDateColumn()) {
            sb.append("addCriterionForJDBCDate(\"");
        } else if (introspectedColumn.isJDBCTimeColumn()) {
            sb.append("addCriterionForJDBCTime(\"");
        } else if (stringHasValue(introspectedColumn.getTypeHandler())) {
            sb.append("add");
            sb.append(introspectedColumn.getJavaProperty());
            sb.setCharAt(3, Character.toUpperCase(sb.charAt(3)));
            if (nameFragment.contains("UpperCase")) {
                sb.append("Criterion(UPPER(\"");
            }else {
                if (nameFragment.contains("UpperCase")) {
                    sb.append("Criterion(\"UPPER(");
                } else {
                    sb.append("Criterion(\"");
                }
            }
        } else {
            if (nameFragment.contains("UpperCase")) {
                sb.append("addCriterion(\"UPPER(");
            } else {
                sb.append("addCriterion(\"");
            }
        }
        sb.append(MyBatis3FormattingUtilities.getAliasedActualColumnName(introspectedColumn));
        if (nameFragment.contains("UpperCase")) {
            sb.append(")");
        }
        return sb.toString();
    }

    /**
     * Generates methods that set between and not between conditions.
     *
     * @param introspectedColumn the introspected column
     * @param betweenMethod      true if between, else not between
     * @return a generated method for the between or not between method
     */
    private Method getSetBetweenOrNotBetweenMethod(IntrospectedColumn introspectedColumn, boolean betweenMethod) {

        StringBuilder sb = new StringBuilder();
        sb.append(initializeAndMethodName(introspectedColumn));
        if (betweenMethod) {
            sb.append("Between");
        } else {
            sb.append("NotBetween");
        }
        Method method = new Method(sb.toString());
        method.setVisibility(JavaVisibility.PUBLIC);
        FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();

        method.addParameter(new Parameter(type, "value1"));
        method.addParameter(new Parameter(type, "value2"));
        method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());

        sb.setLength(0);
        sb.append(initializeAddLine(introspectedColumn, "Between"));
        if (betweenMethod) {
            sb.append(" between");
        } else {
            sb.append(" not between");
        }
        sb.append("\", ");
        sb.append("value1, value2");
        sb.append(", \"");
        sb.append(introspectedColumn.getJavaProperty());
        sb.append("\");");
        method.addBodyLine(sb.toString());
        method.addBodyLine("return (Criteria) this;");

        return method;
    }

    /**
     * Generates an In or NotIn method.
     *
     * @param introspectedColumn the introspected column
     * @param inMethod           if true generates an "in" method, else generates a "not in"
     *                           method
     * @return a generated method for the in or not in method
     */
    private Method getSetInOrNotInMethod(IntrospectedColumn introspectedColumn, boolean inMethod) {
        StringBuilder sb = new StringBuilder();
        sb.append(initializeAndMethodName(introspectedColumn));
        if (inMethod) {
            sb.append("In");
        } else {
            sb.append("NotIn");
        }
        Method method = new Method(sb.toString());
        method.setVisibility(JavaVisibility.PUBLIC);
        FullyQualifiedJavaType type = FullyQualifiedJavaType.getNewListInstance();
        if (introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
            type.addTypeArgument(introspectedColumn.getFullyQualifiedJavaType().getPrimitiveTypeWrapper());
        } else {
            type.addTypeArgument(introspectedColumn.getFullyQualifiedJavaType());
        }

        method.addParameter(new Parameter(type, "values"));
        method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());

        sb.setLength(0);
        sb.append(initializeAddLine(introspectedColumn, inMethod ? "In" : "NotIn"));
        if (inMethod) {
            sb.append(" in");
        } else {
            sb.append(" not in");
        }
        sb.append("\", values, \"");
        sb.append(introspectedColumn.getJavaProperty());
        sb.append("\");");
        method.addBodyLine(sb.toString());
        method.addBodyLine("return (Criteria) this;");

        return method;
    }

    private Method getNoValueMethod(IntrospectedColumn introspectedColumn, String nameFragment, String operator) {
        StringBuilder sb = new StringBuilder();
        sb.append(initializeAndMethodName(introspectedColumn));
        sb.append(nameFragment);
        Method method = new Method(sb.toString());

        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());

        sb.setLength(0);
        sb.append("addCriterion(\"");
        sb.append(MyBatis3FormattingUtilities.getAliasedActualColumnName(introspectedColumn));
        sb.append(' ');
        sb.append(operator);
        sb.append("\");");
        method.addBodyLine(sb.toString());
        method.addBodyLine("return (Criteria) this;");

        return method;
    }

    private Method getAnyConditionMethod(IntrospectedColumn introspectedColumn) {
        StringBuilder sb = new StringBuilder();
        sb.append(initializeAndMethodName(introspectedColumn));
        sb.append("AnyCondition");
        Method method = new Method(sb.toString());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "expression"));
        method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());

        sb.setLength(0);
        sb.append(initializeAddLine(introspectedColumn,"AnyCondition"));
        sb.append(' ');
        sb.append("\"+expression);");
        method.addBodyLine(sb.toString());
        method.addBodyLine("return (Criteria) this;");
        return method;
    }

    private Method getAnyAnyConditionMethod() {
        Method method = new Method("andAnyCondition");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "expression"));
        method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
        method.addBodyLine("addCriterion(expression);");
        method.addBodyLine("return (Criteria) this;");
        return method;
    }

    private Method getEmptyMethod(IntrospectedColumn introspectedColumn, String nameFragment, String operator) {
        StringBuilder sb = new StringBuilder();
        sb.append(initializeAndMethodName(introspectedColumn));
        sb.append(nameFragment);
        Method method = new Method(sb.toString());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());

        sb.setLength(0);
        sb.append("addCriterion(\"(");
        sb.append(MyBatis3FormattingUtilities.getAliasedActualColumnName(introspectedColumn));
        sb.append(" ");
        sb.append("is null");
        sb.append(" OR ");
        sb.append("trim(");
        sb.append(MyBatis3FormattingUtilities.getAliasedActualColumnName(introspectedColumn));
        sb.append(")");
        sb.append(' ');
        sb.append(operator);
        sb.append(")\");");
        method.addBodyLine(sb.toString());
        method.addBodyLine("return (Criteria) this;");

        return method;
    }

    private Method getNotEmptyMethod(IntrospectedColumn introspectedColumn, String nameFragment, String operator) {
        StringBuilder sb = new StringBuilder();
        sb.append(initializeAndMethodName(introspectedColumn));
        sb.append(nameFragment);
        Method method = new Method(sb.toString());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());

        sb.setLength(0);
        sb.append("addCriterion(\"(");
        sb.append(MyBatis3FormattingUtilities.getAliasedActualColumnName(introspectedColumn));
        sb.append(" ");
        sb.append("is not null");
        sb.append(" AND ");
        sb.append("trim(");
        sb.append(MyBatis3FormattingUtilities.getAliasedActualColumnName(introspectedColumn));
        sb.append(")");
        sb.append(' ');
        sb.append(operator);
        sb.append(")\");");
        method.addBodyLine(sb.toString());
        method.addBodyLine("return (Criteria) this;");

        return method;
    }

    /**
     * This method adds all the extra methods and fields required to support a
     * user defined type handler on some column.
     *
     * @param introspectedColumn the introspected column
     * @param constructor        the constructor
     * @param innerClass         the enclosing class
     * @return the name of the List added to the class by this method
     */
    private String addTypeHandledObjectsAndMethods(IntrospectedColumn introspectedColumn, Method constructor,
                                                   InnerClass innerClass) {
        StringBuilder sb = new StringBuilder();

        // add new private field and public accessor in the class
        sb.setLength(0);
        sb.append(introspectedColumn.getJavaProperty());
        sb.append("Criteria");
        String answer = sb.toString();

        Field field = new Field(answer, new FullyQualifiedJavaType("java.util.List<Criterion>"));
        field.setVisibility(JavaVisibility.PROTECTED);
        innerClass.addField(field);

        Method method = new Method(getGetterMethodName(field.getName(), field.getType()));
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(field.getType());
        sb.insert(0, "return ");
        sb.append(';');
        method.addBodyLine(sb.toString());
        innerClass.addMethod(method);

        // add constructor initialization
        sb.setLength(0);
        sb.append(field.getName());
        sb.append(" = new ArrayList<>();");
        constructor.addBodyLine(sb.toString());

        // now add the methods for simplifying the individual field set methods
        sb.setLength(0);
        sb.append("add");
        sb.append(introspectedColumn.getJavaProperty());
        sb.setCharAt(3, Character.toUpperCase(sb.charAt(3)));
        sb.append("Criterion");
        method = new Method(sb.toString());
        method.setVisibility(JavaVisibility.PROTECTED);
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "condition"));
        method.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(), "value"));
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "property"));
        method.addBodyLine("if (value == null) {");
        method.addBodyLine(
                "throw new RuntimeException(\"Value for \" + property + \" cannot be null\");");
        method.addBodyLine("}");

        method.addBodyLine(
                String.format("%s.add(new Criterion(condition, value, \"%s\"));",
                        field.getName(), introspectedColumn.getTypeHandler()));
        method.addBodyLine("allCriteria = null;");
        innerClass.addMethod(method);

        sb.setLength(0);
        sb.append("add");
        sb.append(introspectedColumn.getJavaProperty());
        sb.setCharAt(3, Character.toUpperCase(sb.charAt(3)));
        sb.append("Criterion");

        method = new Method(sb.toString());
        method.setVisibility(JavaVisibility.PROTECTED);
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "condition"));
        method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(), "value1"));
        method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(), "value2"));
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "property"));
        if (!introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
            method.addBodyLine("if (value1 == null || value2 == null) {");
            method.addBodyLine(
                    "throw new RuntimeException(\"Between values for \" + property + \""
                            + " cannot be null\");");
            method.addBodyLine("}");
        }

        method.addBodyLine(
                String.format("%s.add(new Criterion(condition, value1, value2, \"%s\"));",
                        field.getName(), introspectedColumn.getTypeHandler()));

        method.addBodyLine("allCriteria = null;");
        innerClass.addMethod(method);

        return answer;
    }
}
