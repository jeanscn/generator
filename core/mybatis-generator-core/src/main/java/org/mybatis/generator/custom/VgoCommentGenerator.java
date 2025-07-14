package org.mybatis.generator.custom;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.MergeConstants;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.DefaultCommentGenerator;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author cen_c
 */
public class VgoCommentGenerator extends DefaultCommentGenerator {
    /**
     *  阻止生成注释
     */
    private boolean suppressAllComments;
    /**
     * 是否生成数据库表的注释
     */
    private boolean addRemarkComments;
    /**
     * 阻止生成时间戳
     */
    private boolean suppressDate;

    /**
     * 设置用户配置的参数
     * @param properties
     * All properties from the configuration
     */
    @Override
    public void addConfigurationProperties(Properties properties) {
        //调用父类方法保证父类方法可以正常使用
        super.addConfigurationProperties(properties);
        //获取suppressAllComments参数值
        suppressAllComments = Boolean.parseBoolean(properties.getProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_ALL_COMMENTS));
        //获取addRemarkComments参数值
        addRemarkComments = Boolean.parseBoolean(properties.getProperty(PropertyRegistry.COMMENT_GENERATOR_ADD_REMARK_COMMENTS));

        suppressDate = Boolean.parseBoolean(properties.getProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_DATE));
    }

    /**
     * 给model字段添加注释信息
     * @param field
     *            the field
     * @param introspectedTable
     *            the introspected table
     * @param introspectedColumn
     *            the introspected column
     */
    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        //文档注释开始
        field.addJavaDocLine("/**");
        //获取数据库字段的备注信息
        String remarks = introspectedColumn.getRemarks(false);
        //根据参数和备注信息判断是否添加备注信息
        if (addRemarkComments && StringUtility.stringHasValue(remarks)) {
            String[] remarkLinesStrings = remarks.split(System.lineSeparator());
            for (String remarkLine : remarkLinesStrings) {
                field.addJavaDocLine(" * " + remarkLine);
                field.addJavaDocLine(" * Column Name:" + introspectedColumn.getActualColumnName() + " Type:" + introspectedColumn.getActualTypeName() + " Remark:" + introspectedColumn.getRemarks(false));
            }
        }
        field.addJavaDocLine(" */");
    }

    @Override
    public void addFieldComment(Field field, String...comments) {
        if (comments.length == 0) {
            return;
        }
        field.addJavaDocLine("/**");
        for (String comment : comments) {
            if (StringUtility.stringHasValue(comment)) {
                field.addJavaDocLine(" * " + comment);
            }
        }
        field.addJavaDocLine(" */");
    }

    /**
     * 给model以外字段添加注释，如example类
     * @param field
     *            the field
     * @param introspectedTable
     *            the introspected table
     */
    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable) {
        if ("ignoreDeleteFlag".equals(field.getName())) {
            field.addJavaDocLine("/**");
            field.addJavaDocLine(" * 是否忽略删除标记");
            field.addJavaDocLine(" */");
        } else if ("ignorePermissionAnnotation".equals(field.getName())) {
            field.addJavaDocLine("/**");
            field.addJavaDocLine(" * 是否忽略权限注解 包括：@TableDataScope等");
            field.addJavaDocLine(" */");
        } else if ("distinct".equals(field.getName())) {
            field.addJavaDocLine("/**");
            field.addJavaDocLine(" * 是否去重");
            field.addJavaDocLine(" */");
        } else if ("oredCriteria".equals(field.getName())) {
            field.addJavaDocLine("/**");
            field.addJavaDocLine(" * 过滤条件");
            field.addJavaDocLine(" */");
        } else if ("orderByClause".equals(field.getName())) {
            field.addJavaDocLine("/**");
            field.addJavaDocLine(" * 排序条件");
            field.addJavaDocLine(" */");
        } else if ("limit".equals(field.getName())) {
            field.addJavaDocLine("/**");
            field.addJavaDocLine(" * 分页条件");
            field.addJavaDocLine(" */");
        } else if ("offset".equals(field.getName())) {
            field.addJavaDocLine("/**");
            field.addJavaDocLine(" * 偏移量条件");
            field.addJavaDocLine(" */");
        } else {
            super.addFieldComment(field, introspectedTable);
        }
    }

    /**
     * 在java文件顶端加注释
     */
    @Override
    public void addJavaFileComment(CompilationUnit compilationUnit) {
        if (suppressAllComments) {
            return;
        }
        compilationUnit.addFileCommentLine("/*");
        compilationUnit.addFileCommentLine(" * The content below is generated by vgosoft's code factory.");
        if (!suppressDate) {
            StringBuilder sb = new StringBuilder();
            String s = getDateString();
            sb.append(" * 生成时间: ").append(s);
            compilationUnit.addFileCommentLine(sb.toString());
        }
        compilationUnit.addFileCommentLine(" */");
    }

    @Override
    public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (suppressAllComments) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        topLevelClass.addJavaDocLine("/**");
        sb.append(" * 类对应的数据库表为： ");
        sb.append(introspectedTable.getFullyQualifiedTable());
        topLevelClass.addJavaDocLine(sb.toString());
        //添加表注释
        sb.setLength(0);
        sb.append(" * ");
        sb.append(introspectedTable.getRemarks(false));
        topLevelClass.addJavaDocLine(sb.toString());
        topLevelClass.addJavaDocLine(" */");
    }

    @Override
    public void addModelClassComment(CompilationUnit compilationUnit, String mainComment, boolean warn, String... comments) {
        if (suppressAllComments) {
            return;
        }
        addJavaDocLine(compilationUnit,"/**");
        addJavaDocLine(compilationUnit,VStringUtil.format(" * {0}", VStringUtil.stringHasValue(mainComment)?mainComment:"--"));
        if (comments.length>0) {
            addJavaDocLine(compilationUnit," * <p>");
            for (String comment : comments) {
                if (StringUtility.stringHasValue(comment)) {
                    addJavaDocLine(compilationUnit," * " + comment);
                }
            }
            addJavaDocLine(compilationUnit," * </p>");
        }

        if (warn) {
            addJavaDocLine(compilationUnit," * <p class=\"warning\">");
            addJavaDocLine(compilationUnit," * <strong>注意：</strong>此类由VGOSoft代码生成器自动生成，任何直接修改都可能在重新生成代码时被覆盖。");
            addJavaDocLine(compilationUnit," *   如需扩展功能，请更新继承类。");
            addJavaDocLine(compilationUnit," * </p>");
        }
        addJavaDocLine(compilationUnit," *");
        addJavaDocLine(compilationUnit," * @author @VCF");
        addJavaDocLine(compilationUnit," */");
    }

    private void addJavaDocLine(CompilationUnit topLevelClass,String comment) {
        if (topLevelClass instanceof TopLevelClass) {
            TopLevelClass topClass = (TopLevelClass) topLevelClass;
            if (StringUtility.stringHasValue(comment)) {
                topClass.addJavaDocLine(comment);
            }
        } else if (topLevelClass instanceof Interface) {
            Interface intf = (Interface) topLevelClass;
            if (StringUtility.stringHasValue(comment)) {
                intf.addJavaDocLine(comment);
            }
        } else {
            throw new IllegalArgumentException("Unsupported CompilationUnit type: " + topLevelClass.getClass().getName());
        }
    }

    @Override
    public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType record = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        if(("view"+record.getShortName()).equals(method.getName())){
            addMethodJavaDocLine(method,"根据主键获取单个业务实例",""
                    ,"@param id 可选参数，存在时查询数据；否则直接返回视图，用于打开表单。");
        }else if(("get"+record.getShortName()).equals(method.getName())){
            addMethodJavaDocLine(method,"根据主键获取单个实体");
        }else if(("list"+record.getShortName()).equals(method.getName())){
            String param = getEntityParam(introspectedTable);
            addMethodJavaDocLine(method,"获取条件实体对象列表",""
                    ,"@param "+param+" 用于接收属性同名参数");
        }else if(("create"+record.getShortName()).equals(method.getName())){
            addMethodJavaDocLine(method,"新增一条记录");
        }else if(("createBatch"+record.getShortName()).equals(method.getName())){
            addMethodJavaDocLine(method,"新增多条记录");
        }else if(("upload"+record.getShortName()).equals(method.getName())){
            addMethodJavaDocLine(method,"单个文件上传");
        }else if(("download"+record.getShortName()).equals(method.getName())){
            addMethodJavaDocLine(method,"文件下载","","@param id 路径参数，资源标识id"
                    ,"@param type 路径参数，下载方式：1-下载或另存 0-直接在浏览器中打开");
        }else if(("update"+record.getShortName()).equals(method.getName())){
            addMethodJavaDocLine(method,"根据主键更新实体对象");
        }else if(("updateBatch"+record.getShortName()).equals(method.getName())){
            addMethodJavaDocLine(method,"根据主键批量更新实体对象");
        }else if(("delete"+record.getShortName()).equals(method.getName())){
            addMethodJavaDocLine(method,"删除一条记录");
        }else if(("deleteBatch"+record.getShortName()).equals(method.getName())){
            addMethodJavaDocLine(method,"根据ids批量删除记录");
        }else if(("getDefaultViewConfig"+record.getShortName()).equals(method.getName())){
            addMethodJavaDocLine(method,"获取默认数据视图配置");
        }else if(("getDefaultView"+record.getShortName()).equals(method.getName())){
            addMethodJavaDocLine(method,"获取默认数据视图");
        }else if(("getDict"+record.getShortName()).equals(method.getName())){
            addMethodJavaDocLine(method,"获取默认缓存（字典）数据");
        }else if(("getCache"+record.getShortName()).equals(method.getName())){
            addMethodJavaDocLine(method,"获取默认缓存数据");
        }else if(VStringUtil.contains(method.getName(), "deleteByTable")){
            addMethodJavaDocLine(method,"删除中间关系表数据（取消数据关联）");
        }else if(VStringUtil.contains(method.getName(), "insertByTable")){
            addMethodJavaDocLine(method,"添加中间关系表数据（添加数据关联）");
        }else if (VStringUtil.contains(method.getName(), "option")) {
            String param = getEntityParam(introspectedTable);
            String property = VStringUtil.replace(method.getName(), "option", "").replace(record.getShortName(), "");
            addMethodJavaDocLine(method,"获取Options-"+JavaBeansUtil.getFirstCharacterLowercase(property)+"选项列表",""
                    ,"@param "+param+" 用于接收属性同名参数","@param selected 选中的值");
        }
    }

    private String getEntityParam(IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        String firstCharacterLowercase = JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName());
        String param = firstCharacterLowercase;
        if (introspectedTable.getRules().isGenerateRequestVo()) {
            param = firstCharacterLowercase + "RequestVo";
        }else if (introspectedTable.getRules().isGenerateVoModel()) {
            param = firstCharacterLowercase + "Vo";
        }
        return param;
    }

    @Override
    public void addGetterComment(Method method, IntrospectedTable introspectedTable,
                                 IntrospectedColumn introspectedColumn) {
    }

    @Override
    public void addSetterComment(Method method, IntrospectedTable introspectedTable,
                                 IntrospectedColumn introspectedColumn) {
    }

    @Override
    public void addComment(XmlElement xmlElement) {
        if (suppressAllComments) {
            return;
        }
        xmlElement.addElement(new TextElement("<!--"));
        String t = "  提示 - " + MergeConstants.NEW_ELEMENT_TAG;
        xmlElement.addElement(new TextElement(t));
        xmlElement.addElement(
                new TextElement("  这个元素通过Mybatis Generator自动生成,"
                        + " 请勿修改."));
        xmlElement.addElement(new TextElement("-->"));
    }

    /**
     * 添加注释
     * @param method 要添加注释的方法
     * @param comments 要添加的注释
     * @param singleLine 注释方式（false-多行javaDoc方式，true-单行双斜杠）
     */
    @Override
    public void addMethodJavaDocLine(Method method,boolean singleLine,String...comments){
        super.addMethodJavaDocLine(method,singleLine,comments);
    }

    @Override
    public void addMethodJavaDocLine(Method method, String...comments){
        List<String> docLine = method.getParameters().stream().map(p -> "@param " + p.getName() + " " + p.getRemark()).collect(Collectors.toList());
        method.getReturnType().ifPresent(r->{
            if (method.getReturnRemark() != null) {
                docLine.add("@return "+r.getShortName()+" "+method.getReturnRemark());
            }
        });
        method.getExceptionRemark().ifPresent(r->{
            if (!method.getExceptions().isEmpty()) {
                String collect = method.getExceptions().stream()
                        .map(FullyQualifiedJavaType::getShortName)
                        .collect(Collectors.joining(","));
                docLine.add("@throws "+collect+" "+r);
            }
        });
        docLine.add(0, "");
        for (int i = 0; i < comments.length; i++) {
            docLine.add(i, comments[i]);
        }
        docLine.add(0, "提示 - "+ConstantsUtil.GENERATED_FLAG);
        addMethodJavaDocLine(method,false,docLine.toArray(new String[0]));
    }
}
