package org.mybatis.generator.api;

import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinFunction;
import org.mybatis.generator.api.dom.kotlin.KotlinProperty;
import org.mybatis.generator.api.dom.kotlin.KotlinType;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GeneratedHtmlFile;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * 此接口定义了将在代码生成过程。这些方法可用于扩展或修改生成的代码。客户端可以完整地实现此接口，或者扩展 PluginAdapter（强烈推荐）。
 *
 * <p>插件有一个生命周期。一般来说，生命周期是这样的：
 *
 * <ol>
 * <li>setXXX 方法调用一次</li>
 * <li>validate 方法调用一次</li>
 * <li>为每个自省表调用初始化的方法</li>
 * <li>为每个自省表调用 clientXXX 方法</li>
 * <li>为每个自省表调用 providerXXX 方法</li>
 * <li>为每个自省表调用 modelXXX 方法</li>
 * <li>为每个自省表调用 sqlMapXXX 方法</li>
 * <li>contextGenerateAdditionalJavaFiles（IntrospectedTable） 方法是
 * 为每个内省表调用</li>
 * <li>contextGenerateAdditionalXmlFiles（IntrospectedTable） 方法调用
 * 对于每个自省表</li>
 * <li>contextGenerateAdditionalJavaFiles（） 方法调用一次</li>
 * <li>contextGenerateAdditionalXmlFiles（） 方法调用一次</li>
 * </ol>
 *
 * <p>插件与上下文相关 - 因此每个上下文都有自己的一组
 * 插件。如果在多个上下文中指定了相同 的 插件，则每个
 * context 将保存插件的唯一实例。
 * <p>
 * 插件的<p>调用和初始化顺序与指定插件的顺序相同
 * 配置。
 *
 * <p>clientXXX、modelXXX 和 sqlMapXXX 方法由代码调用
 * 发电机。如果将默认代码生成器替换为其他
 * 实现时，这些方法可能不会被调用。
 *
 * @author cen_c
 * @see PluginAdapter
 *
 */
public interface Plugin {

    /**
     * 独立主键锁定 Mapper 方法扩展点，不调用普通查询回调以免引入缓存或分页。
     *
     * @param method 锁定方法
     * @param interfaze Mapper 接口
     * @param table 内省表
     * @return 是否保留方法，默认保留以兼容已有插件
     */
    default boolean clientSelectByPrimaryKeyForUpdateMethodGenerated(Method method, Interface interfaze,
                                                                   AbstractIntrospectedTable table) {
        return true;
    }

    /**
     * 独立条件锁定 Mapper 方法扩展点。
     *
     * @param method 锁定方法
     * @param interfaze Mapper 接口
     * @param table 内省表
     * @return 是否保留方法，默认保留以兼容已有插件
     */
    default boolean clientSelectByExampleForUpdateMethodGenerated(Method method, Interface interfaze,
                                                                AbstractIntrospectedTable table) {
        return true;
    }

    /**
     * 独立主键锁定 XML 扩展点，每个目标方言执行一次。
     *
     * @param element 锁定查询元素
     * @param table 内省表
     * @return 是否保留元素，默认保留以兼容已有插件
     */
    default boolean sqlMapSelectByPrimaryKeyForUpdateElementGenerated(XmlElement element, AbstractIntrospectedTable table) {
        return true;
    }

    /**
     * 独立条件锁定 XML 扩展点，每个目标方言执行一次。
     *
     * @param element 锁定查询元素
     * @param table 内省表
     * @return 是否保留元素，默认保留以兼容已有插件
     */
    default boolean sqlMapSelectByExampleForUpdateElementGenerated(XmlElement element, AbstractIntrospectedTable table) {
        return true;
    }

    enum ModelClassType {
        /**
         * 类的类型
         */
        PRIMARY_KEY,
        BASE_RECORD,
        RECORD_WITH_BLOBS
    }

    /**
     * 设置运行此插件的上下文。
     *
     * @param context 新的上下文
     */
    void setContext(Context context);

    /**
     * 从插件配置中设置属性。
     *
     * @param properties 插件配置
     *
     */
    void setProperties(Properties properties);

    /**
     * 此方法在内省表上调用 getGeneratedXXXFiles 方法之前调用。插件
     * 可以实现此方法来覆盖任何默认属性，或更改数据库的结果
     * 在发生任何代码生成活动之前进行内省。属性作为静态字符串列出，其中
     * 前缀 ATTR_ 在 IntrospectedTable 中。
     *
     * <p>覆盖属性的一个很好的例子是用户想要更改属性的名称
     * ，更改目标包，或更改生成的 SQL 映射文件的名称。
     *
     * <p><b>警告：</b>被列为 属性的 内容都不应被其他插件之一更改
     * 方法。例如，如果要更改生成的示例类的名称，则不应简单地更改
     * <code>modelExampleClassGenerated（）</code> 方法中的 Type。如果这样做，更改将不会反映出来
     * 在其他生成的工件中。
     *
     * @param introspectedTable 内省表
     */
    default void initialized(AbstractIntrospectedTable introspectedTable) {
    }

    /**
     * 此方法在所有 setXXX 方法调用后调用，但在调用任何其他方法。这允许插件确定是否
     * 它可以运行或不运行。例如，如果插件需要某些属性要设置，并且属性没有设置，则插件无效且
     * 不会运行。
     *
     * @param warnings 如果插件配置不正确，则包含警告
     *                 例如，如果
     *                 插件无效，您应该说明原因。警告是
     *                 运行完成后向用户报告。
     *                 如果插件处于有效状态，则@return true。无效的插件不会
     *                 名叫
     */
    boolean validate(List<String> warnings);

    /**
     * 此方法可用于生成您的实现。此方法在所有其他 Java 之后调用一次文件已生成。
     *
     * @return GeneratedJavaFiles 列表 - 这些文件将被保存与此运行中的其他文件一起使用。
     */
    default List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles() {
        return Collections.emptyList();
    }

    /**
     * 此方法可用于生成可能与特定表相关的实现。此方法是为配置中的每个表调用一次。
     *
     * @param introspectedTable 包含有关表的信息的类
     *                          从数据库中内省
     * @return GeneratedJavaFiles 列表 - 这些文件将被保存
     * 与此运行中的其他文件一起使用。
     */
    default List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(AbstractIntrospectedTable introspectedTable) {
        return Collections.emptyList();
    }

    /**
     * 此方法可用于生成您的实现。此方法在所有其他 XML 之后调用一次文件已生成。
     *
     * @return GeneratedXmlFiles 列表 - 这些文件将被保存与此运行中的其他文件一起使用。
     */
    default List<GeneratedKotlinFile> contextGenerateAdditionalKotlinFiles() {
        return Collections.emptyList();
    }

    /**
     * 此方法可用于生成可能与特定表相关的实现。此方法是为配置中的每个表调用一次。
     *
     * @param introspectedTable 包含有关表的信息的类从数据库中内省
     * @return GeneratedXmlFiles 列表 - 这些文件将被保存与此运行中的其他文件一起使用。
     */
    default List<GeneratedKotlinFile> contextGenerateAdditionalKotlinFiles(AbstractIntrospectedTable introspectedTable) {
        return Collections.emptyList();
    }

    /**
     * 此方法可用于生成您的实现所需的任何附加文件。此方法在所有其他
     * 文件生成后调用一次。
     *
     * @return GeneratedFiles 列表 - 这些文件将与本次运行中的其他文件一起保存。
     */
    default List<GeneratedFile> contextGenerateAdditionalFiles() {
        return Collections.emptyList();
    }

    /**
     * 此方法可用于生成您的实现所需的附加文件，这些文件可能与特定表相关。
     * 此方法对配置中的每个表调用一次。
     *
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return GeneratedFiles 列表 - 这些文件将与本次运行中的其他文件一起保存。
     */
    default List<GeneratedFile> contextGenerateAdditionalFiles(AbstractIntrospectedTable introspectedTable) {
        return Collections.emptyList();
    }

    /**
     * 此方法可用于生成您的实现所需的任何附加 XML 文件。此方法在所有其他 XML
     * 文件生成后调用一次。
     *
     * @return GeneratedXmlFiles 列表 - 这些文件将与本次运行中的其他文件一起保存。
     */
    default List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles() {
        return Collections.emptyList();
    }

    /**
     * 此方法可用于生成您的实现所需的任何附加 HTML 文件。此方法在所有其他 HTML
     * 文件生成后调用一次。
     *
     * @return GeneratedHtmlFiles 列表 - 这些文件将与本次运行中的其他文件一起保存。
     */
    default List<GeneratedHtmlFile> contextGenerateAdditionalHtmlFiles() {
        return Collections.emptyList();
    }

    /**
     * 此方法可用于生成您的实现所需的附加 XML 文件，这些文件可能与特定表相关。
     * 此方法对配置中的每个表调用一次。
     *
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return GeneratedXmlFiles 列表 - 这些文件将与本次运行中的其他文件一起保存。
     */
    default List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles(
            AbstractIntrospectedTable introspectedTable) {
        return Collections.emptyList();
    }

    /**
     * 此方法可用于生成您的实现所需的附加 HTML 文件
     *
     * @param introspectedTable          包含从数据库内省的表信息的类
     * @param htmlGeneratorConfiguration HTML 生成配置
     * @return GeneratedHtmlFiles 列表 - 这些文件将被保存
     */
    default List<GeneratedFile> contextGenerateAdditionalWebFiles(
            AbstractIntrospectedTable introspectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        return Collections.emptyList();
    }

    /**
     * 此方法可用于生成您的实现所需的附加 Vue3 文件，这些文件可能与特定表相关。
     * 此方法对配置中的每个表调用一次。
     *
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return GeneratedFiles 列表 - 这些文件将与本次运行中的其他文件一起保存。
     */
    default List<GeneratedFile> contextGenerateVue3OtherFiles(
            AbstractIntrospectedTable introspectedTable) {
        return Collections.emptyList();
    }

    /**
     * 在生成整个客户端时调用此方法。
     * 实现此方法可向生成的客户端接口或实现类添加其他方法或字段。
     *
     * @param interfaze         生成的接口（如果存在），可能为 null
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该接口则返回 true，如果应忽略生成的接口则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientGenerated(Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在生成整个客户端时调用此方法。
     *
     * @param interfaze         生成的接口（如果存在），可能为 null
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该接口则返回 true
     */
    default boolean subClientGenerated(Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 此方法已不再被调用。
     * <p>
     * 为 mapper 接口生成 count 方法时调用此方法。
     * 此方法仅在 MyBatis3DynamicSql 运行时中被调用。
     *
     * @param method            生成的 count 方法
     * @param interfaze         部分生成的 mapper 接口
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     * @deprecated 此方法已不再被调用
     */
    @Deprecated
    default boolean clientBasicCountMethodGenerated(Method method, Interface interfaze,
                                                    AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 此方法已不再被调用。
     *
     * @param kotlinFunction    生成的函数
     * @param kotlinFile        部分生成的文件
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     * @deprecated 此方法已不再被调用
     */
    @Deprecated
    default boolean clientBasicCountMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                    AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 此方法已不再被调用。
     * <p>
     * 为 mapper 接口生成 delete 方法时调用此方法。
     * 此方法仅在 MyBatis3DynamicSql 运行时中被调用。
     *
     * @param method            生成的 delete 方法
     * @param interfaze         部分生成的 mapper 接口
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     * @deprecated 不再被调用
     */
    @Deprecated
    default boolean clientBasicDeleteMethodGenerated(Method method, Interface interfaze,
                                                     AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 此方法已不再被调用。
     *
     * @param kotlinFunction    生成的 delete 函数
     * @param kotlinFile        部分生成的文件
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     * @deprecated 不再被调用
     */
    @Deprecated
    default boolean clientBasicDeleteMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                     AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 为 mapper 接口生成 insert 方法时调用此方法。
     * 此方法仅在 MyBatis3DynamicSql 运行时中被调用。
     * <p>
     * 此方法仅在 MyBatis3DynamicSql 运行时中被调用。仅当表包含生成的主键时，才会调用此方法。
     *
     * @param method            生成的 insert 方法
     * @param interfaze         部分生成的 mapper 接口
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientBasicInsertMethodGenerated(Method method, Interface interfaze,
                                                     AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 为 mapper 接口生成 insert 函数时调用此方法。
     * 此方法仅在 MyBatis3Kotlin 运行时中被调用。仅当表包含生成的主键时，才会调用此方法。
     *
     * @param kotlinFunction    生成的 insert 函数
     * @param kotlinFile        部分生成的文件
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该函数则返回 true，如果应忽略生成的函数则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientBasicInsertMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                     AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 为 mapper 接口生成 insert multiple 方法时调用此方法。
     * 此方法仅在 MyBatis3DynamicSql 运行时中被调用。
     * <p>
     * 此方法仅在 MyBatis3DynamicSql 运行时中被调用。仅当表包含生成的主键时，才会调用此方法。
     *
     * @param method            生成的 insert 方法
     * @param interfaze         部分生成的 mapper 接口
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientBasicInsertMultipleMethodGenerated(Method method, Interface interfaze,
                                                             AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 为 mapper 接口生成 insert multiple 方法时调用此方法。
     * 此方法仅在 MyBatis3DynamicSql 运行时中被调用。仅当表包含生成的主键时，才会调用此方法。
     *
     * @param kotlinFunction    生成的 insert 函数
     * @param kotlinFile        部分生成的文件
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该函数则返回 true，如果应忽略生成的函数则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientBasicInsertMultipleMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                             AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 为 mapper 接口生成 insert multiple 方法 helper 时调用此方法。
     * 仅当多行插入包含生成的主键时才会创建 helper 方法。
     * 此方法仅在 MyBatis3DynamicSql 运行时中被调用。
     * <p>
     * 此方法已不再被调用。
     *
     * @param method            生成的 insert 方法
     * @param interfaze         部分生成的 mapper 接口
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     * @deprecated 此方法已不再被调用
     */
    @Deprecated
    default boolean clientBasicInsertMultipleHelperMethodGenerated(Method method, Interface interfaze,
                                                                   AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 未使用的遗留方法。
     *
     * @param kotlinFunction    生成的函数
     * @param kotlinFile        生成的文件
     * @param introspectedTable 内省表
     * @return true
     * @deprecated 此方法已不再被调用
     */
    @Deprecated
    default boolean clientBasicInsertMultipleHelperMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                                   AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 为 mapper 接口生成 selectMany 方法时调用此方法。
     * 此方法仅在 MyBatis3DynamicSql 运行时中被调用。
     *
     * @param method            生成的 selectMany 方法
     * @param interfaze         部分生成的 mapper 接口
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientBasicSelectManyMethodGenerated(Method method, Interface interfaze,
                                                         AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 为 mapper 接口生成 selectMany 方法时调用此方法。
     * 此方法仅在 MyBatis3Kotlin 运行时中被调用。
     *
     * @param kotlinFunction    生成的 selectMany 方法
     * @param kotlinFile        部分生成的 mapper 文件
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientBasicSelectManyMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                         AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 为 mapper 接口生成 selectOne 方法时调用此方法。
     * 此方法仅在 MyBatis3DynamicSql 运行时中被调用。
     *
     * @param method            生成的 selectOne 方法
     * @param interfaze         部分生成的 mapper 接口
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientBasicSelectOneMethodGenerated(Method method, Interface interfaze,
                                                        AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 为 mapper 接口生成 selectOne 方法时调用此方法。
     * 此方法仅在 MyBatis3Kotlin 运行时中被调用。
     *
     * @param kotlinFunction    生成的 selectOne 方法
     * @param kotlinFile        部分生成的 mapper 文件
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientBasicSelectOneMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                        AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 此方法已不再被调用。
     *
     * @param method            生成的 update 方法
     * @param interfaze         部分生成的 mapper 接口
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     * @deprecated 不再被调用
     */
    @Deprecated
    default boolean clientBasicUpdateMethodGenerated(Method method, Interface interfaze,
                                                     AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 此方法已不再被调用。
     *
     * @param kotlinFunction    生成的 update 函数
     * @param kotlinFile        部分生成的文件
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     * @deprecated 不再被调用
     */
    @Deprecated
    default boolean clientBasicUpdateMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                     AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 countByExample 方法时调用此方法。
     *
     * @param method            生成的 countByExample 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientCountByExampleMethodGenerated(Method method,
                                                        Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 deleteByExample 方法时调用此方法。
     *
     * @param method            生成的 deleteByExample 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientDeleteByExampleMethodGenerated(Method method,
                                                         Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 deleteByPrimaryKey 方法时调用此方法。
     *
     * @param method            生成的 deleteByPrimaryKey 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientDeleteByPrimaryKeyMethodGenerated(Method method,
                                                            Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 countByExample 方法时调用此方法。
     *
     * @param kotlinFunction    生成的 countByExample 方法
     * @param kotlinFile        部分实现的客户端文件。如有必要，您可以向该文件添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientDeleteByPrimaryKeyMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                            AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 insertOrDeleteByTable 方法时调用此方法。
     * 此方法仅在 MyBatis3DynamicSql 运行时中被调用。
     *
     * @param method            生成的 insertOrDeleteByTable 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientInsertOrDeleteByTableMethodGenerated(Method method,
                                                               Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成通用 count 方法时调用。在 MyBatis Dynamic SQL V2 运行时中，用于替代 countByExample。
     *
     * @param method            生成的通用 count 方法
     * @param interfaze         部分生成的 mapper 接口
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true
     */
    default boolean clientGeneralCountMethodGenerated(Method method, Interface interfaze,
                                                      AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成通用 count 方法时调用。在 MyBatis Dynamic SQL V2 运行时中，用于替代 countByExample。
     *
     * @param kotlinFunction    生成的通用 count 方法
     * @param kotlinFile        部分生成的 mapper 文件
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true
     */
    default boolean clientGeneralCountMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                      AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成通用 delete 方法时调用。在 MyBatis Dynamic SQL V2 运行时中，用于替代 deleteByExample。
     *
     * @param method            生成的通用 delete 方法
     * @param interfaze         部分生成的 mapper 接口
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true
     */
    default boolean clientGeneralDeleteMethodGenerated(Method method, Interface interfaze,
                                                       AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成通用 delete 方法时调用。在 MyBatis Dynamic SQL V2 运行时中，用于替代 deleteByExample。
     *
     * @param kotlinFunction    生成的通用 delete 方法
     * @param kotlinFile        部分生成的 mapper 文件
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true
     */
    default boolean clientGeneralDeleteMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                       AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成通用 select distinct 方法时调用。在 MyBatis Dynamic SQL V2 运行时中，用于替代
     * selectDistinctByExample。
     *
     * @param method            生成的通用 select distinct 方法
     * @param interfaze         部分生成的 mapper 接口
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true
     */
    default boolean clientGeneralSelectDistinctMethodGenerated(Method method, Interface interfaze,
                                                               AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成通用 select distinct 方法时调用。在 MyBatis Dynamic SQL V2 运行时中，用于替代
     * selectDistinctByExample。
     *
     * @param kotlinFunction    生成的通用 select distinct 方法
     * @param kotlinFile        部分生成的 mapper 文件
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true
     */
    default boolean clientGeneralSelectDistinctMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                               AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成通用 select 方法时调用。在 MyBatis Dynamic SQL V2 运行时中，用于替代
     * selectByExample。
     *
     * @param method            生成的通用 select 方法
     * @param interfaze         部分生成的 mapper 接口
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true
     */
    default boolean clientGeneralSelectMethodGenerated(Method method, Interface interfaze,
                                                       AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成通用 select 方法时调用。在 MyBatis Dynamic SQL V2 运行时中，用于替代
     * selectByExample。
     *
     * @param kotlinFunction    生成的通用 select 方法
     * @param kotlinFile        部分生成的 mapper 文件
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true
     */
    default boolean clientGeneralSelectMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                       AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成通用 update 方法时调用。在 MyBatis Dynamic SQL V2 运行时中，用于替代
     * updateByExample。
     *
     * @param method            生成的通用 update 方法
     * @param interfaze         部分生成的 mapper 接口
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true
     */
    default boolean clientGeneralUpdateMethodGenerated(Method method, Interface interfaze,
                                                       AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成通用 update 方法时调用。在 MyBatis Dynamic SQL V2 运行时中，用于替代
     * updateByExample。
     *
     * @param kotlinFunction    生成的通用 update 方法
     * @param kotlinFile        部分生成的 mapper 文件
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true
     */
    default boolean clientGeneralUpdateMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                       AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 insert 方法时调用此方法。
     *
     * @param method            生成的 insert 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientInsertMethodGenerated(Method method, Interface interfaze,
                                                AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 insertOrUpdate 方法时调用此方法。
     * 此方法仅在 MyBatis3DynamicSql 运行时中被调用。
     *
     * @param method            生成的 insertOrUpdate 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientInsertOrUpdateMethodGenerated(Method method, Interface interfaze,
                                                        AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 insert 方法时调用此方法。
     *
     * @param kotlinFunction    生成的 insert 方法
     * @param kotlinFile        部分实现的客户端文件。如有必要，您可以向该文件添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientInsertMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 insert multiple 方法时调用此方法。
     * 此方法仅在 MyBatis3DynamicSql 运行时中被调用。
     *
     * @param method            生成的 insert multiple 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientInsertMultipleMethodGenerated(Method method, Interface interfaze,
                                                        AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 insert multiple 方法时调用此方法。
     * 此方法仅在 MyBatis3DynamicSql 运行时中被调用。
     *
     * @param kotlinFunction    生成的 insert multiple 方法
     * @param kotlinFile        部分实现的客户端文件。如有必要，您可以向该文件添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientInsertMultipleMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                        AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 insert selective 方法时调用此方法。
     *
     * @param method            生成的 insert 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientInsertSelectiveMethodGenerated(Method method,
                                                         Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 insert batch 方法时调用此方法。
     *
     * @param method            生成的 insert batch 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientInsertBatchMethodGenerated(Method method,
                                                     Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 insert selective 方法时调用此方法。
     *
     * @param kotlinFunction    生成的 insert 方法
     * @param kotlinFile        部分实现的客户端文件。如有必要，您可以向该文件添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientInsertSelectiveMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                         AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 selectByExampleWithBLOBs 方法时调用此方法。
     *
     * @param method            生成的 selectByExampleWithBLOBs 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientSelectByExampleWithBlobsMethodGenerated(Method method,
                                                                  Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 selectByExampleWithoutBLOBs 方法时调用此方法。
     *
     * @param method            生成的 selectByExampleWithoutBLOBs 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientSelectByExampleWithoutBlobsMethodGenerated(Method method,
                                                                     Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 selectByPrimaryKey 方法时调用此方法。
     *
     * @param method            生成的 selectByPrimaryKey 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientSelectByPrimaryKeyMethodGenerated(Method method,
                                                            Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 selectByKeysDic 方法时调用此方法。
     *
     * @param method            生成的 selectByKeysDic 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientSelectByKeysDicMethodGenerated(Method method,
                                                         Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 selectByPrimaryKey 方法时调用此方法。
     *
     * @param kotlinFunction    生成的 selectByPrimaryKey 方法
     * @param kotlinFile        部分实现的客户端文件。如有必要，您可以向该文件添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientSelectByPrimaryKeyMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                            AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在 MyBatis Dynamic SQL V2 运行时中生成 selectList 字段时调用。
     *
     * @param field             生成的 selectList 字段
     * @param interfaze         部分生成的 mapper 接口
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该字段则返回 true
     */
    default boolean clientSelectListFieldGenerated(Field field, Interface interfaze,
                                                   AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 selectOne 方法时调用。这是 MyBatis Dynamic SQL V2 运行时中的新方法。
     *
     * @param method            生成的 selectOne 方法
     * @param interfaze         部分生成的 mapper 接口
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true
     */
    default boolean clientSelectOneMethodGenerated(Method method, Interface interfaze,
                                                   AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 selectOne 方法时调用。这是 MyBatis Dynamic SQL V2 运行时中的新方法。
     *
     * @param kotlinFunction    生成的 selectOne 方法
     * @param kotlinFile        部分生成的文件
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true
     */
    default boolean clientSelectOneMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                   AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 updateByExampleSelective 方法时调用此方法。
     *
     * @param method            生成的 updateByExampleSelective 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientUpdateByExampleSelectiveMethodGenerated(Method method,
                                                                  Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 updateAllColumns 方法时调用。生成的方法可与通用
     * update 方法结合使用，以模拟旧版 updateByExample 方法的功能。
     *
     * @param method            生成的 updateAllColumns 方法
     * @param interfaze         部分生成的 mapper 接口
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true
     */
    default boolean clientUpdateAllColumnsMethodGenerated(Method method, Interface interfaze,
                                                          AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 updateAllColumns 方法时调用。生成的方法可与通用
     * update 方法结合使用，以模拟旧版 updateByExample 方法的功能。
     *
     * @param kotlinFunction    生成的 updateAllColumns 方法
     * @param kotlinFile        部分生成的文件
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true
     */
    default boolean clientUpdateAllColumnsMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                          AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 updateSelectiveColumns 方法时调用。生成的方法可与通用
     * update 方法结合使用，以模拟旧版 updateByExampleSelective 方法的功能。
     *
     * @param method            生成的 updateSelectiveColumns 方法
     * @param interfaze         部分生成的 mapper 接口
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true
     */
    default boolean clientUpdateSelectiveColumnsMethodGenerated(Method method, Interface interfaze,
                                                                AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 updateSelectiveColumns 方法时调用。生成的方法可与通用
     * update 方法结合使用，以模拟旧版 updateByExampleSelective 方法的功能。
     *
     * @param kotlinFunction    生成的 updateSelectiveColumns 方法
     * @param kotlinFile        部分生成的文件
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true
     */
    default boolean clientUpdateSelectiveColumnsMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                                AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 updateByExampleWithBLOBs 方法时调用此方法。
     *
     * @param method            生成的 updateByExampleWithBLOBs 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientUpdateByExampleWithBlobsMethodGenerated(Method method,
                                                                  Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 updateByExampleWithoutBLOBs 方法时调用此方法。
     *
     * @param method            生成的 updateByExampleWithoutBLOBs 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientUpdateByExampleWithoutBlobsMethodGenerated(Method method,
                                                                     Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 updateByPrimaryKeySelective 方法时调用此方法。
     *
     * @param method            生成的 updateByPrimaryKeySelective 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method,
                                                                     Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 updateByPrimaryKeySelective 方法时调用此方法。
     *
     * @param method            生成的 updateByPrimaryKeySelective 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientUpdateBatchMethodGenerated(Method method,
                                                     Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 updateByPrimaryKeySelective 方法时调用此方法。
     *
     * @param kotlinFunction    生成的 updateByPrimaryKeySelective 函数
     * @param kotlinFile        部分实现的客户端文件。如有必要，您可以向该文件添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该函数则返回 true，如果应忽略生成的函数则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(KotlinFunction kotlinFunction,
                                                                     KotlinFile kotlinFile, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 updateByPrimaryKeyWithBLOBs 方法时调用此方法。
     *
     * @param method            生成的 updateByPrimaryKeyWithBLOBs 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientUpdateByPrimaryKeyWithBlobsMethodGenerated(Method method,
                                                                     Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 updateByPrimaryKeyWithoutBLOBs 方法时调用此方法。
     *
     * @param method            生成的 updateByPrimaryKeyWithoutBLOBs 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientUpdateByPrimaryKeyWithoutBlobsMethodGenerated(Method method,
                                                                        Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 incrementRevisionByPrimaryKey 方法时调用此方法。
     * @param method           生成的 incrementRevisionByPrimaryKey 方法
     * @param interfaze        部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientIncrementRevisionByPrimaryKeyGenerated(Method method,
                                                                        Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在客户端接口中生成 selectAll 方法时调用此方法。此方法仅由
     * simple 运行时生成。
     *
     * @param method            生成的 selectAll 方法
     * @param interfaze         部分实现的客户端接口。如有必要，您可以向该接口添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean clientSelectAllMethodGenerated(Method method,
                                                   Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在为表中的特定列生成字段后调用此方法。
     *
     * @param field              为指定列生成的字段
     * @param topLevelClass      部分实现的模型类。如有必要，您可以向该实现类添加额外的导入类。
     * @param introspectedColumn 包含从数据库内省的与此字段相关的列信息的类
     * @param introspectedTable  包含从数据库内省的表信息的类
     * @param modelClassType     生成该字段的类的类型
     * @return 如果应生成该字段则返回 true，如果应忽略生成的字段则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass,
                                        IntrospectedColumn introspectedColumn,
                                        AbstractIntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return true;
    }

    /**
     * voAbstract类字段生成后的回调方法
     *
     * @param field              字段
     * @param topLevelClass      类
     * @param introspectedColumn 字段对应的数据库列
     * @param introspectedTable  表信息
     * @return true 生成字段，false 不生成
     */
    default boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass,
                                             IntrospectedColumn introspectedColumn,
                                             AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * voModel类字段生成后的回调方法
     *
     * @param field              字段
     * @param topLevelClass      类
     * @param introspectedColumn 字段对应的数据库列
     * @param introspectedTable  表信息
     * @return true 生成字段，false 不生成
     */
    default boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass,
                                          IntrospectedColumn introspectedColumn,
                                          AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * voUpdate类字段生成后的回调方法
     *
     * @param field              字段
     * @param topLevelClass      类
     * @param introspectedColumn 字段对应的数据库列
     * @param introspectedTable  表信息
     * @return true 生成字段，false 不生成
     */
    default boolean voUpdateFieldGenerated(Field field, TopLevelClass topLevelClass,
                                           IntrospectedColumn introspectedColumn,
                                           AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * voCreate类字段生成后的回调方法
     *
     * @param field              字段
     * @param topLevelClass      类
     * @param introspectedColumn 字段对应的数据库列
     * @param introspectedTable  表信息
     * @return true 生成字段，false 不生成
     */
    default boolean voCreateFieldGenerated(Field field, TopLevelClass topLevelClass,
                                           IntrospectedColumn introspectedColumn,
                                           AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * voExcel类字段生成后的回调方法
     *
     * @param field              字段
     * @param topLevelClass      类
     * @param introspectedColumn 字段对应的数据库列
     * @param introspectedTable  表信息
     * @param index              字段索引
     * @return true 生成字段，false 不生成
     */
    default boolean voExcelFieldGenerated(Field field, TopLevelClass topLevelClass,
                                          IntrospectedColumn introspectedColumn,
                                          AbstractIntrospectedTable introspectedTable, int index) {
        return true;
    }

    /**
     * voExcelImport类字段生成后的回调方法
     *
     * @param field              字段
     * @param topLevelClass      类
     * @param introspectedColumn 字段对应的数据库列
     * @param introspectedTable  表信息
     * @param index              字段索引
     * @return true 生成字段，false 不生成
     */
    default boolean voExcelImportFieldGenerated(Field field, TopLevelClass topLevelClass,
                                                IntrospectedColumn introspectedColumn,
                                                AbstractIntrospectedTable introspectedTable, int index) {
        return true;
    }

    /**
     * voModel类getter方法生成后的回调方法
     *
     * @param method             getter方法
     * @param topLevelClass      类
     * @param introspectedColumn 字段对应的数据库列
     * @param introspectedTable  表信息
     * @return true 生成getter方法，false 不生成
     */
    default boolean voModelGetterMethodGenerated(Method method,
                                                 TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                                 AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * voCreate类getter方法生成后的回调方法
     *
     * @param method             getter方法
     * @param topLevelClass      类
     * @param introspectedColumn 字段对应的数据库列
     * @param introspectedTable  表信息
     * @return true 生成getter方法，false 不生成
     */
    default boolean voCreateGetterMethodGenerated(Method method,
                                                  TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                                  AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * voUpdate类getter方法生成后的回调方法
     *
     * @param method             getter方法
     * @param topLevelClass      类
     * @param introspectedColumn 字段对应的数据库列
     * @param introspectedTable  表信息
     * @return true 生成getter方法，false 不生成
     */
    default boolean voUpdateGetterMethodGenerated(Method method,
                                                  TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                                  AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * voView类getter方法生成后的回调方法
     *
     * @param field              字段
     * @param topLevelClass      类
     * @param introspectedColumn 字段对应的数据库列
     * @param introspectedTable  表信息
     * @return true 生成getter方法，false 不生成
     */
    default boolean voViewFieldGenerated(Field field, TopLevelClass topLevelClass,
                                         IntrospectedColumn introspectedColumn,
                                         AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * voRequest类字段生成后的回调方法
     *
     * @param field              字段
     * @param topLevelClass      类
     * @param introspectedColumn 字段对应的数据库列
     * @param introspectedTable  表信息
     * @return true 生成字段，false 不生成
     */
    default boolean voRequestFieldGenerated(Field field, TopLevelClass topLevelClass,
                                            IntrospectedColumn introspectedColumn,
                                            AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在为表中的特定列生成 getter（或访问器）方法后调用此方法。
     *
     * @param method             为指定列生成的 getter（或访问器）方法
     * @param topLevelClass      部分实现的模型类。如有必要，您可以向该实现类添加额外的导入类。
     * @param introspectedColumn 包含从数据库内省的与此字段相关的列信息的类
     * @param introspectedTable  包含从数据库内省的表信息的类
     * @param modelClassType     生成该字段的类的类型
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean modelGetterMethodGenerated(Method method,
                                               TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                               AbstractIntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return true;
    }

    /**
     * 在为表中的特定列生成 setter（或修改器）方法后调用此方法。
     *
     * @param method             为指定列生成的 setter（或修改器）方法
     * @param topLevelClass      部分实现的模型类。如有必要，您可以向该实现类添加额外的导入类。
     * @param introspectedColumn 包含从数据库内省的与此字段相关的列信息的类
     * @param introspectedTable  包含从数据库内省的表信息的类
     * @param modelClassType     生成该字段的类的类型
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean modelSetterMethodGenerated(Method method,
                                               TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                               AbstractIntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return true;
    }

    /**
     * 在 JavaModelGenerator 生成主键类后调用此方法。仅当表规则要求生成主键类时才会调用此方法。
     * <br><br>
     * 仅保证 Java 模型生成器会调用此方法。其他用户提供的生成器可能会或可能不会调用此方法。
     *
     * @param topLevelClass     生成的主键类
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该类则返回 true，如果应忽略生成的类则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass,
                                                  AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在 JavaModelGenerator 生成基础记录类后调用此方法。仅当表规则要求生成基础记录类时才会调用此方法。
     * <br><br>
     * 仅保证默认的 Java 模型生成器会调用此方法。其他用户提供的生成器可能会或可能不会调用此方法。
     *
     * @param topLevelClass     生成的基础记录类
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该类则返回 true，如果应忽略生成的类则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
                                                  AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成vo抽象类后的回调方法
     *
     * @param topLevelClass     vo抽象类
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean voModelAbstractClassGenerated(TopLevelClass topLevelClass,
                                                  AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成vo类后的回调方法
     *
     * @param topLevelClass     vo类
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean voModelRecordClassGenerated(TopLevelClass topLevelClass,
                                                AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成CreateVo类后的回调方法
     *
     * @param topLevelClass     CreateVo类
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean voModelCreateClassGenerated(TopLevelClass topLevelClass,
                                                AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成UpdateVo类后的回调方法
     *
     * @param topLevelClass     UpdateVo类
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean voModelUpdateClassGenerated(TopLevelClass topLevelClass,
                                                AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成ViewVo类后的回调方法
     *
     * @param topLevelClass     ViewVo类
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean voModelViewClassGenerated(TopLevelClass topLevelClass,
                                              AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成ExcelVo类后的回调方法
     *
     * @param topLevelClass     ExcelVo类
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean voModelExcelClassGenerated(TopLevelClass topLevelClass,
                                               AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成ExcelImportVo类后的回调方法
     *
     * @param topLevelClass     ExcelImportVo类
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean voModelExcelImportClassGenerated(TopLevelClass topLevelClass,
                                                     AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成RequestVo类后的回调方法
     *
     * @param topLevelClass     RequestVo类
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean voModelRequestClassGenerated(TopLevelClass topLevelClass,
                                                 AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成缓存实体对象类后的回调方法
     *
     * @param topLevelClass     缓存实体对象类
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean voModelCacheClassGenerated(TopLevelClass topLevelClass,
                                               AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在 JavaModelGenerator 生成包含 BLOB 的记录类后调用此方法。仅当表规则要求生成包含 BLOB 的记录类时才会调用此方法。
     * <br><br>
     * 仅保证默认的 Java 模型生成器会调用此方法。其他用户提供的生成器可能会或可能不会调用此方法。
     *
     * @param topLevelClass     生成的包含 BLOB 的记录类
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该类则返回 true，如果应忽略生成的类则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean modelRecordWithBlobsClassGenerated(TopLevelClass topLevelClass,
                                                       AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在 JavaModelGenerator 生成 Example 类后调用此方法。仅当表规则要求生成 Example 类时才会调用此方法。
     * <br><br>
     * 仅保证默认的 Java 模型生成器会调用此方法。其他用户提供的生成器可能会或可能不会调用此方法。
     *
     * @param topLevelClass     生成的 Example 类
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该类则返回 true，如果应忽略生成的类则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
                                               AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 SqlMap 文件时调用此方法。
     *
     * @param sqlMap            生成的文件（包含文件名、包名和项目名）
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该 sqlMap 则返回 true，如果应忽略生成的 sqlMap 则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapGenerated(GeneratedXmlFile sqlMap,
                                    AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成html文件后的回调方法
     *
     * @param htmlMap                    html文件
     * @param introspectedTable          表信息
     * @param htmlGeneratorConfiguration html文件生成配置
     * @return true生成html文件，false不生成html文件
     */
    default boolean htmlMapGenerated(GeneratedHtmlFile htmlMap,
                                     AbstractIntrospectedTable introspectedTable,
                                     HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        return true;
    }

    /**
     * 生成 SqlMap document 时调用此方法。此方法可用于向生成的 document 添加额外的 XML 元素。
     *
     * @param document          生成的 document（请注意，这是 MyBatis Generator 内部的 Document 类 - 而不是 w3c XML Document 类）
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该 document 则返回 true，如果应忽略生成的 document 则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。此外，如果有任何插件返回 false，
     * 则不会调用 <code>sqlMapGenerated</code> 方法。
     */
    default boolean sqlMapDocumentGenerated(Document document,
                                            AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成html文件后的回调方法
     *
     * @param document                   html文件
     * @param introspectedTable          表信息
     * @param htmlGeneratorConfiguration html文件生成配置
     * @return true生成html文件，false不生成html文件
     */
    default boolean htmlMapDocumentGenerated(org.mybatis.generator.api.dom.html.Document document, AbstractIntrospectedTable introspectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        return true;
    }

    /**
     * 生成基础 resultMap 时调用此方法。
     *
     * @param element           生成的 &lt;resultMap&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapResultMapWithoutBlobsElementGenerated(XmlElement element,
                                                                AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 countByExample 元素时调用此方法。
     *
     * @param element           生成的 &lt;select&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapCountByExampleElementGenerated(XmlElement element,
                                                         AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 deleteByExample 元素时调用此方法。
     *
     * @param element           生成的 &lt;delete&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapDeleteByExampleElementGenerated(XmlElement element,
                                                          AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 deleteByPrimaryKey 元素时调用此方法。
     *
     * @param element           生成的 &lt;delete&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element,
                                                             AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 exampleWhereClause 元素时调用此方法。
     *
     * @param element           生成的 &lt;sql&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element,
                                                             AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 baseColumnList 元素时调用此方法。
     *
     * @param element           生成的 &lt;sql&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapBaseColumnListElementGenerated(XmlElement element,
                                                         AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 blobColumnList 元素时调用此方法。
     *
     * @param element           生成的 &lt;sql&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapBlobColumnListElementGenerated(XmlElement element,
                                                         AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 insert 元素时调用此方法。
     *
     * @param element           生成的 &lt;insert&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapInsertElementGenerated(XmlElement element,
                                                 AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 insert selective 元素时调用此方法。
     *
     * @param element           生成的 &lt;insert&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapInsertSelectiveElementGenerated(XmlElement element,
                                                          AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 insertOrUpdate 元素时调用此方法。
     *
     * @param element           生成的 &lt;insert&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成则返回 false
     */
    default boolean sqlMapInsertOrUpdateSelectiveElementGenerated(XmlElement element,
                                                                  AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成包含 BLOB 的 resultMap 元素时调用此方法 - 此 resultMap 将继承基础 resultMap。
     *
     * @param element           生成的 &lt;resultMap&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapResultMapWithBlobsElementGenerated(XmlElement element,
                                                             AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 selectAll 元素时调用此方法。
     *
     * @param element           生成的 &lt;select&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapSelectAllElementGenerated(XmlElement element,
                                                    AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成xml文件的自定义工具栏…方法后的回调方法
     *
     * @param element           自定义工具栏…方法的xml元素
     * @param introspectedTable 表的元数据
     * @return 生成xml文件有效，false生成xml文件无效
     */
    default boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element,
                                                             AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成xml文件的selectByKeysDict方法后的回调方法
     *
     * @param element           selectByKeysDict方法的xml元素
     * @param introspectedTable 表的元数据
     * @return true生成xml文件的selectByKeysDict方法，false不生成xml文件的selectByKeysDict方法
     */
    default boolean sqlMapSelectByKeysDictElementGenerated(XmlElement element,
                                                           AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 selectByExample 元素时调用此方法。
     *
     * @param element           生成的 &lt;select&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapSelectByExampleWithoutBlobsElementGenerated(
            XmlElement element, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 selectByExampleWithBLOBs 元素时调用此方法。
     *
     * @param element           生成的 &lt;select&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapSelectByExampleWithBlobsElementGenerated(XmlElement element,
                                                                   AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 updateByExampleSelective 元素时调用此方法。
     *
     * @param element           生成的 &lt;update&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element,
                                                                   AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 updateByExampleWithBLOBs 元素时调用此方法。
     *
     * @param element           生成的 &lt;update&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapUpdateByExampleWithBlobsElementGenerated(XmlElement element,
                                                                   AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 updateByExampleWithoutBLOBs 元素时调用此方法。
     *
     * @param element           生成的 &lt;update&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapUpdateByExampleWithoutBlobsElementGenerated(
            XmlElement element, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 updateByPrimaryKeySelective 元素时调用此方法。
     *
     * @param element           生成的 &lt;update&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(
            XmlElement element, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 updateBatch 元素时调用此方法。
     *
     * @param element           xml元素
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean sqlMapUpdateBatchElementGenerated(
            XmlElement element, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 updateByPrimaryKeyWithBLOBs 元素时调用此方法。
     *
     * @param element           生成的 &lt;update&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapUpdateByPrimaryKeyWithBlobsElementGenerated(
            XmlElement element, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 updateByPrimaryKeyWithoutBLOBs 元素时调用此方法。
     *
     * @param element           生成的 &lt;update&gt; 元素
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该元素则返回 true，如果应忽略生成的元素则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean sqlMapUpdateByPrimaryKeyWithoutBlobsElementGenerated(
            XmlElement element, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 SQL provider 时调用此方法。
     * 实现此方法可向生成的 SQL provider 添加其他方法或字段。
     *
     * @param topLevelClass     生成的 provider
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该 provider 则返回 true，如果应忽略生成的 provider 则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean providerGenerated(TopLevelClass topLevelClass,
                                      AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在 SQL provider 中生成 applyWhere 方法时调用此方法。
     *
     * @param method            生成的 applyWhere 方法
     * @param topLevelClass     部分生成的 provider 类。如有必要，您可以向该类添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean providerApplyWhereMethodGenerated(Method method,
                                                      TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在 SQL provider 中生成 countByExample 方法时调用此方法。
     *
     * @param method            生成的 countByExample 方法
     * @param topLevelClass     部分生成的 provider 类。如有必要，您可以向该类添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean providerCountByExampleMethodGenerated(Method method,
                                                          TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在 SQL provider 中生成 deleteByExample 方法时调用此方法。
     *
     * @param method            生成的 deleteByExample 方法
     * @param topLevelClass     部分生成的 provider 类。如有必要，您可以向该类添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean providerDeleteByExampleMethodGenerated(Method method,
                                                           TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在 SQL provider 中生成 insertSelective 方法时调用此方法。
     *
     * @param method            生成的 insertSelective 方法
     * @param topLevelClass     部分生成的 provider 类。如有必要，您可以向该类添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean providerInsertSelectiveMethodGenerated(Method method,
                                                           TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在 SQL provider 中生成 selectByExampleWithBLOBs 方法时调用此方法。
     *
     * @param method            生成的 selectByExampleWithBLOBs 方法
     * @param topLevelClass     部分生成的 provider 类。如有必要，您可以向该类添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean providerSelectByExampleWithBlobsMethodGenerated(Method method,
                                                                    TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在 SQL provider 中生成 selectByExampleWithoutBLOBs 方法时调用此方法。
     *
     * @param method            生成的 selectByExampleWithoutBLOBs 方法
     * @param topLevelClass     部分生成的 provider 类。如有必要，您可以向该类添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean providerSelectByExampleWithoutBlobsMethodGenerated(Method method,
                                                                       TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在 SQL provider 中生成 updateByExampleSelective 方法时调用此方法。
     *
     * @param method            生成的 updateByExampleSelective 方法
     * @param topLevelClass     部分生成的 provider 类。如有必要，您可以向该类添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean providerUpdateByExampleSelectiveMethodGenerated(Method method,
                                                                    TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在 SQL provider 中生成 updateByExampleWithBLOBs 方法时调用此方法。
     *
     * @param method            生成的 updateByExampleWithBLOBs 方法
     * @param topLevelClass     部分生成的 provider 类。如有必要，您可以向该类添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean providerUpdateByExampleWithBlobsMethodGenerated(Method method,
                                                                    TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在 SQL provider 中生成 updateByExampleWithoutBLOBs 方法时调用此方法。
     *
     * @param method            生成的 updateByExampleWithoutBLOBs 方法
     * @param topLevelClass     部分生成的 provider 类。如有必要，您可以向该类添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean providerUpdateByExampleWithoutBlobsMethodGenerated(Method method,
                                                                       TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在 SQL provider 中生成 updateByPrimaryKeySelective 方法时调用此方法。
     *
     * @param method            生成的 updateByPrimaryKeySelective 方法
     * @param topLevelClass     部分生成的 provider 类。如有必要，您可以向该类添加额外的导入类。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该方法则返回 true，如果应忽略生成的方法则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean providerUpdateByPrimaryKeySelectiveMethodGenerated(Method method,
                                                                       TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 在 MyBatis Dynamic SQL 运行时中生成 MyBatis Dynamic SQL 支持类时调用此方法。
     *
     * @param supportClass      生成的 MyBatis Dynamic SQL 支持类。如有必要，您可以向生成的类添加额外的项。
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该类则返回 true，如果应忽略生成的类则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     */
    default boolean dynamicSqlSupportGenerated(TopLevelClass supportClass, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成 MyBatis Dynamic SQL 支持对象时调用此方法。该类的结构为一个外部对象（outer object）包含一个内部类（inner class）。
     * 内部类包含表和列的定义。外部（单例）对象包含对内部类实例的引用，以及引用该实例各列的快捷属性。
     *
     * @param kotlinFile         生成的包含外部对象和内部类的 Kotlin 文件
     * @param outerSupportObject 文件中外部对象的引用
     * @param innerSupportClass  内部类的引用
     * @param introspectedTable  包含从数据库内省的表信息的类
     * @return 如果应保留生成的文件则返回 true
     */
    default boolean dynamicSqlSupportGenerated(KotlinFile kotlinFile, KotlinType outerSupportObject,
                                               KotlinType innerSupportClass, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 此方法已不再被调用。
     *
     * @param extensionsFile    部分生成的文件
     * @param introspectedTable 包含从数据库内省的表信息的类
     * @return 如果应生成该文件则返回 true，如果应忽略生成的文件则返回 false。
     * 在配置了多个插件的情况下，第一个返回 false 的插件将阻止调用后续插件。
     * @deprecated 此方法已不再被调用
     */
    @Deprecated
    default boolean mapperExtensionsGenerated(KotlinFile extensionsFile, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * Kotlin生成mapper类后的回调方法
     *
     * @param mapperFile        生成的mapper类
     * @param mapper            mapper类的类型
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean mapperGenerated(KotlinFile mapperFile, KotlinType mapper, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * kotlin生成data class类后的回调方法
     *
     * @param kotlinFile        生成的data class文件
     * @param dataClass         生成的data class类
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean kotlinDataClassGenerated(KotlinFile kotlinFile, KotlinType dataClass,
                                             AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * kotlin生成dao类ColumnListProperty后的回调方法
     *
     * @param kotlinProperty    生成的dao类ColumnListProperty
     * @param kotlinFile        生成的dao文件
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean clientColumnListPropertyGenerated(KotlinProperty kotlinProperty, KotlinFile kotlinFile,
                                                      AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * kotlin生成dao类中insertMultipleVararg方法后的回调方法
     *
     * @param kotlinFunction    生成的dao-insert方法
     * @param kotlinFile        生成的dao文件
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean clientInsertMultipleVarargMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                              AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * kotlin生成dao类中updateByPrimaryKey方法后的回调方法
     *
     * @param kotlinFunction    生成的dao-updateByPrimaryKey方法
     * @param kotlinFile        生成的dao文件
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean clientUpdateByPrimaryKeyMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
                                                            AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成service类后的回调方法
     *
     * @param interfaze         生成的service类
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean serviceGenerated(Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成子service类(用于个性化service的类)后的回调方法
     *
     * @param interfaze         生成的子service类
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean subServiceGenerated(Interface interfaze, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成serviceImpl类后的回调方法
     *
     * @param topLevelClass     生成的serviceImpl类
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean serviceImplGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成子serviceImpl类(用于个性化serviceImpl的类)后的回调方法
     *
     * @param topLevelClass     生成的子serviceImpl类
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean subServiceImplGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成controller类后的回调方法
     *
     * @param topLevelClass     生成的controller类
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean controllerGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成子controller类(用于个性化controller的类)后的回调方法
     *
     * @param topLevelClass     生成的子controller类
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean subControllerGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成service单元测试类后的回调方法
     *
     * @param topLevelClass     生成的service单元测试类
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean serviceUnitTestGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable) {
        return true;
    }
}
