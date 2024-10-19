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
 * 插件。如果在多个上下文中指定了相同的插件，则每个
 * context 将保存插件的唯一实例。
 *
 * 插件的<p>调用和初始化顺序与指定插件的顺序相同
 * 配置。
 *
 * <p>clientXXX、modelXXX 和 sqlMapXXX 方法由代码调用
 *发电机。如果将默认代码生成器替换为其他
 * 实现时，这些方法可能不会被调用。
 *
 * @see PluginAdapter
 *
 */
public interface Plugin {

    enum ModelClassType {
        PRIMARY_KEY,
        BASE_RECORD,
        RECORD_WITH_BLOBS
    }

    /**
     *设置运行此插件的上下文。
     *
     * @param context the new context
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
     * <p><b>警告：</b>任何被列为属性的内容都不应被其他插件之一更改
     *方法。例如，如果要更改生成的示例类的名称，则不应简单地更改
     * <code>modelExampleClassGenerated（）</code> 方法中的 Type。如果这样做，更改将不会反映出来
     * 在其他生成的工件中。
     *
     * @param introspectedTable
     * 内省表
     */
    default void initialized(IntrospectedTable introspectedTable) {}

    /**
     * 此方法在所有 setXXX 方法调用后调用，但在调用任何其他方法。这允许插件确定是否
     * 它可以运行或不运行。例如，如果插件需要某些属性要设置，并且属性没有设置，则插件无效且
     * 不会运行。
     *
     * @param warnings 如果插件配置不正确，则包含警告
     * 例如，如果
     * 插件无效，您应该说明原因。警告是
     * 运行完成后向用户报告。
     * 如果插件处于有效状态，则@return true。无效的插件不会
     *名叫
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
     * @param introspectedTable
     * 包含有关表的信息的类
     * 从数据库中内省
     * @return GeneratedJavaFiles 列表 - 这些文件将被保存
     * 与此运行中的其他文件一起使用。
     */
    default List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        return Collections.emptyList();
    }

    /**
     * 此方法可用于生成您的实现。此方法在所有其他 XML 之后调用一次文件已生成。
     * @return GeneratedXmlFiles 列表 - 这些文件将被保存与此运行中的其他文件一起使用。
     */
    default List<GeneratedKotlinFile> contextGenerateAdditionalKotlinFiles() {
        return Collections.emptyList();
    }

    /**
     * 此方法可用于生成可能与特定表相关的实现。此方法是为配置中的每个表调用一次。
     * @param introspectedTable 包含有关表的信息的类从数据库中内省
     * @return GeneratedXmlFiles 列表 - 这些文件将被保存与此运行中的其他文件一起使用。
     */
    default List<GeneratedKotlinFile> contextGenerateAdditionalKotlinFiles(IntrospectedTable introspectedTable) {
        return Collections.emptyList();
    }

    default List<GeneratedFile> contextGenerateAdditionalFiles() {
        return Collections.emptyList();
    }

    default List<GeneratedFile> contextGenerateAdditionalFiles(IntrospectedTable introspectedTable) {
        return Collections.emptyList();
    }

    /**
     * This method can be used to generate any additional XML file needed by
     * your implementation. This method is called once, after all other XML
     * files have been generated.
     *
     * @return a List of GeneratedXmlFiles - these files will be saved
     *         with the other files from this run.
     */
    default List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles() {
        return Collections.emptyList();
    }
    default List<GeneratedHtmlFile> contextGenerateAdditionalHtmlFiles(){
        return Collections.emptyList();
    }

    /**
     * This method can be used to generate additional XML files needed by your
     * implementation that might be related to a specific table. This method is
     * called once for every table in the configuration.
     *
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return a List of GeneratedXmlFiles - these files will be saved
     *         with the other files from this run.
     */
    default List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles(
            IntrospectedTable introspectedTable){
        return Collections.emptyList();
    };
    default List<GeneratedFile> contextGenerateAdditionalWebFiles(
            IntrospectedTable introspectedTable,HtmlGeneratorConfiguration htmlGeneratorConfiguration){
        return Collections.emptyList();
    }

    /**
     * This method is called when the entire client has been generated.
     * Implement this method to add additional methods or fields to a generated
     * client interface or implementation.
     *
     * @param interfaze
     *            the generated interface if any, may be null
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the interface should be generated, false if the generated
     *         interface should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean subClientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is no longer called.
     *
     * This method is called when the count method has been generated for the mapper interface.
     * This method is only called in the MyBatis3DynamicSql runtime.
     *
     * @param method
     *     the generated count method
     * @param interfaze
     *     the partially generated mapper interfaces
     * @param introspectedTable
     *     The class containing information about the table as introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     * @deprecated this method is no longer called
     */
    @Deprecated
    default boolean clientBasicCountMethodGenerated(Method method, Interface interfaze,
                                                    IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is no longer called.
     *
     * @param kotlinFunction
     *     the generated function
     * @param kotlinFile
     *     the partially generated file
     * @param introspectedTable
     *     The class containing information about the table as introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     * @deprecated this method is no longer called
     */
    @Deprecated
    default boolean clientBasicCountMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is no longer called.
     *
     * This method is called when the delete method has been generated for the mapper interface.
     * This method is only called in the MyBatis3DynamicSql runtime.
     *
     * @param method
     *     the generated delete method
     * @param interfaze
     *     the partially generated mapper interfaces
     * @param introspectedTable
     *     The class containing information about the table as introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     * @deprecated No longer called
     */
    @Deprecated
    default boolean clientBasicDeleteMethodGenerated(Method method, Interface interfaze,
                                                     IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is no longer called.
     *
     * @param kotlinFunction
     *     the generated delete function
     * @param kotlinFile
     *     the partially generated file
     * @param introspectedTable
     *     The class containing information about the table as introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     * @deprecated No longer called
     */
    @Deprecated
    default boolean clientBasicDeleteMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the insert method has been generated for the mapper interface.
     * This method is only called in the MyBatis3DynamicSql runtime.
     *
     * This method is only called in the MyBatis3DynamicSql runtime. This method is only
     * called if the table has generated keys.
     *
     * @param method
     *     the generated insert method
     * @param interfaze
     *     the partially generated mapper interfaces
     * @param introspectedTable
     *     The class containing information about the table as introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientBasicInsertMethodGenerated(Method method, Interface interfaze,
                                                     IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the insert function has been generated for the mapper interface.
     * This method is only called in the MyBatis3Kotlin runtime. This method is only
     * called if the table has generated keys.
     *
     * @param kotlinFunction
     *     the generated insert function
     * @param kotlinFile
     *     the partially generated file
     * @param introspectedTable
     *     The class containing information about the table as introspected from the database
     * @return true if the function should be generated, false if the generated
     *         function should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientBasicInsertMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the insert multiple method has been generated for the mapper interface.
     * This method is only called in the MyBatis3DynamicSql runtime.
     *
     * This method is only called in the MyBatis3DynamicSql runtime. This method is only
     * called if the table has generated keys.
     *
     * @param method
     *     the generated insert method
     * @param interfaze
     *     the partially generated mapper interfaces
     * @param introspectedTable
     *     The class containing information about the table as introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientBasicInsertMultipleMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the insert multiple method has been generated for the mapper interface.
     * This method is only called in the MyBatis3DynamicSql runtime. This method is only
     * called if the table has generated keys.
     *
     * @param kotlinFunction
     *     the generated insert function
     * @param kotlinFile
     *     the partially generated file
     * @param introspectedTable
     *     The class containing information about the table as introspected from the database
     * @return true if the method should be generated, false if the generated
     *         function should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientBasicInsertMultipleMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the insert multiple method helper has been generated for the mapper interface.
     * The helper method is only created when a multiple row insert has a generated key.
     * This method is only called in the MyBatis3DynamicSql runtime.
     *
     * This method is no longer called.
     *
     * @param method
     *     the generated insert method
     * @param interfaze
     *     the partially generated mapper interfaces
     * @param introspectedTable
     *     The class containing information about the table as introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     * @deprecated this method is no longer called
     */
    @Deprecated
    default boolean clientBasicInsertMultipleHelperMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * Unused legacy method.
     *
     * @param kotlinFunction generated function
     * @param kotlinFile generated file
     * @param introspectedTable introspected table
     * @return true
     * @deprecated this method is no longer called
     */
    @Deprecated
    default boolean clientBasicInsertMultipleHelperMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the selectMany method has been generated for the mapper interface.
     * This method is only called in the MyBatis3DynamicSql runtime.
     *
     * @param method
     *     the generated selectMany method
     * @param interfaze
     *     the partially generated mapper interfaces
     * @param introspectedTable
     *     The class containing information about the table as introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientBasicSelectManyMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientBasicSelectManyMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the selectOne method has been generated for the mapper interface.
     * This method is only called in the MyBatis3DynamicSql runtime.
     *
     * @param method
     *     the generated selectOne method
     * @param interfaze
     *     the partially generated mapper interfaces
     * @param introspectedTable
     *     The class containing information about the table as introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientBasicSelectOneMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientBasicSelectOneMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is no longer called.
     *
     * @param method
     *     the generated update method
     * @param interfaze
     *     the partially generated mapper interfaces
     * @param introspectedTable
     *     The class containing information about the table as introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     * @deprecated no longer called
     */
    @Deprecated
    default boolean clientBasicUpdateMethodGenerated(Method method, Interface interfaze,
                                                     IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is no longer called.
     *
     * @param kotlinFunction
     *     the generated update function
     * @param kotlinFile
     *     the partially generated file
     * @param introspectedTable
     *     The class containing information about the table as introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     * @deprecated no longer called
     */
    @Deprecated
    default boolean clientBasicUpdateMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the countByExample method has been generated
     * in the client interface.
     *
     * @param method
     *            the generated countByExample method
     * @param interfaze
     *            the partially implemented client interface. You can add
     *            additional imported classes to the interface if
     *            necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientCountByExampleMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the deleteByExample method has been generated
     * in the client interface.
     *
     * @param method
     *            the generated deleteByExample method
     * @param interfaze
     *            the partially implemented client interface. You can add
     *            additional imported classes to the interface if
     *            necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientDeleteByExampleMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the deleteByPrimaryKey method has been
     * generated in the client interface.
     *
     * @param method
     *            the generated deleteByPrimaryKey method
     * @param interfaze
     *            the partially implemented client interface. You can add
     *            additional imported classes to the interface if
     *            necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientDeleteByPrimaryKeyMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientDeleteByPrimaryKeyMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientInsertOrDeleteByTableMethodGenerated(Method method,
                                                               Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * Called when the general count method has been generated. This is the replacement for countByExample
     * in the MyBatis Dynamic SQL V2 runtime.
     *
     * @param method
     *     the generated general count method
     * @param interfaze
     *     the partially generated mapper interfaces
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated
     */
    default boolean clientGeneralCountMethodGenerated(Method method, Interface interfaze,
                                                      IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientGeneralCountMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * Called when the general delete method has been generated. This is the replacement for deleteByExample
     * in the MyBatis Dynamic SQL V2 runtime.
     *
     * @param method
     *     the generated general delete method
     * @param interfaze
     *     the partially generated mapper interfaces
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated
     */
    default boolean clientGeneralDeleteMethodGenerated(Method method, Interface interfaze,
                                                       IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientGeneralDeleteMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * Called when the general select distinct method has been generated. This is the replacement for
     * selectDistinctByExample in the MyBatis Dynamic SQL V2 runtime.
     *
     * @param method
     *     the generated general select distinct method
     * @param interfaze
     *     the partially generated mapper interfaces
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated
     */
    default boolean clientGeneralSelectDistinctMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientGeneralSelectDistinctMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * Called when the general select method has been generated. This is the replacement for
     * selectByExample in the MyBatis Dynamic SQL V2 runtime.
     *
     * @param method
     *     the generated general select method
     * @param interfaze
     *     the partially generated mapper interfaces
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated
     */
    default boolean clientGeneralSelectMethodGenerated(Method method, Interface interfaze,
                                                       IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientGeneralSelectMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * Called when the general update method has been generated. This is the replacement for
     * updateByExample in the MyBatis Dynamic SQL V2 runtime.
     *
     * @param method
     *     the generated general update method
     * @param interfaze
     *     the partially generated mapper interfaces
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated
     */
    default boolean clientGeneralUpdateMethodGenerated(Method method, Interface interfaze,
                                                       IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientGeneralUpdateMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the insert method has been generated in the
     * client interface.
     *
     * @param method
     *            the generated insert method
     * @param interfaze
     *            the partially implemented client interface. You can add
     *            additional imported classes to the interface if
     *            necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientInsertMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientInsertOrUpdateMethodGenerated(Method method, Interface interfaze,
                                                IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientInsertMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the insert multiple method has been generated in the
     * client interface.
     * This method is only called in the MyBatis3DynamicSql runtime.
     *
     * @param method
     *            the generated insert multiple method
     * @param interfaze
     *            the partially implemented client interface. You can add
     *            additional imported classes to the interface if
     *            necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientInsertMultipleMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientInsertMultipleMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the insert selective method has been generated
     * in the client interface.
     *
     * @param method
     *            the generated insert method
     * @param interfaze
     *            the partially implemented client interface. You can add
     *            additional imported classes to the interface if
     *            necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientInsertSelectiveMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientInsertBatchMethodGenerated(Method method,
                                                         Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientInsertSelectiveMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the selectByExampleWithBLOBs method has been
     * generated in the client interface.
     *
     * @param method
     *            the generated selectByExampleWithBLOBs method
     * @param interfaze
     *            the partially implemented client interface. You can add
     *            additional imported classes to the interface if
     *            necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the selectByExampleWithoutBLOBs method has
     * been generated in the client interface.
     *
     * @param method
     *            the generated selectByExampleWithoutBLOBs method
     * @param interfaze
     *            the partially implemented client interface. You can add
     *            additional imported classes to the interface if
     *            necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the selectByPrimaryKey method has been
     * generated in the client interface.
     *
     * @param method
     *            the generated selectByPrimaryKey method
     * @param interfaze
     *            the partially implemented client interface. You can add
     *            additional imported classes to the interface if
     *            necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientSelectByPrimaryKeyMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientSelectByKeysDicMethodGenerated(Method method,
                                                            Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientSelectByPrimaryKeyMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * Called when the selectList field is generated in a MyBatis Dynamic SQL V2 runtime.
     *
     * @param field the generated selectList field
     * @param interfaze
     *     the partially generated mapper interfaces
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the field should be generated
     */
    default boolean clientSelectListFieldGenerated(Field field, Interface interfaze,
                                                   IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * Called when the selectOne method is generated. This is a new method in the MyBatis Dynamic SQL V2 runtime.
     *
     * @param method
     *     the generated selectOne method
     * @param interfaze
     *     the partially generated mapper interfaces
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated
     */
    default boolean clientSelectOneMethodGenerated(Method method, Interface interfaze,
                                                   IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientSelectOneMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the updateByExampleSelective method has been
     * generated in the client interface.
     *
     * @param method
     *            the generated updateByExampleSelective method
     * @param interfaze
     *            the partially implemented client interface. You can add
     *            additional imported classes to the interface if
     *            necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientUpdateByExampleSelectiveMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * Called when the updateAllColumns method is generated. The generated method can be used with the general
     * update method to mimic the function of the old updateByExample method.
     *
     * @param method
     *     the generated updateAllColumns method
     * @param interfaze
     *     the partially generated mapper interfaces
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated
     */
    default boolean clientUpdateAllColumnsMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientUpdateAllColumnsMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * Called when the updateSelectiveColumns method is generated. The generated method can be used with the general
     * update method to mimic the function of the old updateByExampleSelective method.
     *
     * @param method
     *     the generated updateSelectiveColumns method
     * @param interfaze
     *     the partially generated mapper interfaces
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated
     */
    default boolean clientUpdateSelectiveColumnsMethodGenerated(Method method, Interface interfaze,
            IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientUpdateSelectiveColumnsMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the updateByExampleWithBLOBs method has been
     * generated in the client interface.
     *
     * @param method
     *            the generated updateByExampleWithBLOBs method
     * @param interfaze
     *            the partially implemented client interface. You can add
     *            additional imported classes to the interface if
     *            necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the updateByExampleWithoutBLOBs method has
     * been generated in the client interface.
     *
     * @param method
     *            the generated updateByExampleWithoutBLOBs method
     * @param interfaze
     *            the partially implemented client interface. You can add
     *            additional imported classes to the interface if
     *            necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the updateByPrimaryKeySelective method has
     * been generated in the client interface.
     *
     * @param method
     *            the generated updateByPrimaryKeySelective method
     * @param interfaze
     *            the partially implemented client interface. You can add
     *            additional imported classes to the interface if
     *            necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientUpdateBatchMethodGenerated(Method method,
                                                     Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(KotlinFunction kotlinFunction,
            KotlinFile kotlinFile, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the updateByPrimaryKeyWithBLOBs method has
     * been generated in the client interface.
     *
     * @param method
     *            the generated updateByPrimaryKeyWithBLOBs method
     * @param interfaze
     *            the partially implemented client interface. You can add
     *            additional imported classes to the interface if
     *            necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the updateByPrimaryKeyWithoutBLOBs method has
     * been generated in the client interface.
     *
     * @param method
     *            the generated updateByPrimaryKeyWithoutBLOBs method
     * @param interfaze
     *            the partially implemented client interface. You can add
     *            additional imported classes to the interface if
     *            necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the selectAll method has been
     * generated in the client interface.  This method is only generated by
     * the simple runtime.
     *
     * @param method
     *            the generated selectAll method
     * @param interfaze
     *            the partially implemented client interface. You can add
     *            additional imported classes to the interface if
     *            necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean clientSelectAllMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called after the field is generated for a specific column
     * in a table.
     *
     * @param field
     *            the field generated for the specified column
     * @param topLevelClass
     *            the partially implemented model class. You can add additional
     *            imported classes to the implementation class if necessary.
     * @param introspectedColumn
     *            The class containing information about the column related
     *            to this field as introspected from the database
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @param modelClassType
     *            the type of class that the field is generated for
     * @return true if the field should be generated, false if the generated
     *         field should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass,
            IntrospectedColumn introspectedColumn,
            IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return true;
    }

    default boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass,
                                             IntrospectedColumn introspectedColumn,
                                             IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass,
                                             IntrospectedColumn introspectedColumn,
                                             IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean voUpdateFieldGenerated(Field field, TopLevelClass topLevelClass,
                                          IntrospectedColumn introspectedColumn,
                                          IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean voCreateFieldGenerated(Field field, TopLevelClass topLevelClass,
                                           IntrospectedColumn introspectedColumn,
                                           IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean voExcelFieldGenerated(Field field, TopLevelClass topLevelClass,
                                          IntrospectedColumn introspectedColumn,
                                          IntrospectedTable introspectedTable,int index) {
        return true;
    }

    default boolean voExcelImportFieldGenerated(Field field, TopLevelClass topLevelClass,
                                          IntrospectedColumn introspectedColumn,
                                          IntrospectedTable introspectedTable,int index) {
        return true;
    }

    default boolean voModelGetterMethodGenerated(Method method,
                                               TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                               IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean voCreateGetterMethodGenerated(Method method,
                                                 TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                                 IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean voUpdateGetterMethodGenerated(Method method,
                                                  TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                                  IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean voViewFieldGenerated(Field field, TopLevelClass topLevelClass,
                                          IntrospectedColumn introspectedColumn,
                                          IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean voRequestFieldGenerated(Field field, TopLevelClass topLevelClass,
                                          IntrospectedColumn introspectedColumn,
                                          IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called after the getter, or accessor, method is generated
     * for a specific column in a table.
     *
     * @param method
     *            the getter, or accessor, method generated for the specified
     *            column
     * @param topLevelClass
     *            the partially implemented model class. You can add additional
     *            imported classes to the implementation class if necessary.
     * @param introspectedColumn
     *            The class containing information about the column related
     *            to this field as introspected from the database
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @param modelClassType
     *            the type of class that the field is generated for
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean modelGetterMethodGenerated(Method method,
            TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
            IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return true;
    }

    /**
     * This method is called after the setter, or mutator, method is generated
     * for a specific column in a table.
     *
     * @param method
     *            the setter, or mutator, method generated for the specified
     *            column
     * @param topLevelClass
     *            the partially implemented model class. You can add additional
     *            imported classes to the implementation class if necessary.
     * @param introspectedColumn
     *            The class containing information about the column related
     *            to this field as introspected from the database
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @param modelClassType
     *            the type of class that the field is generated for
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean modelSetterMethodGenerated(Method method,
            TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
            IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return true;
    }

    /**
     * This method is called after the primary key class is generated by the
     * JavaModelGenerator. This method will only be called if
     * the table rules call for generation of a primary key class.
     * <br><br>
     * This method is only guaranteed to be called by the Java
     * model generators. Other user supplied generators may, or may not, call
     * this method.
     *
     * @param topLevelClass
     *            the generated primary key class
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the class should be generated, false if the generated
     *         class should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called after the base record class is generated by the
     * JavaModelGenerator. This method will only be called if
     * the table rules call for generation of a base record class.
     * <br><br>
     * This method is only guaranteed to be called by the default Java
     * model generators. Other user supplied generators may, or may not, call
     * this method.
     *
     * @param topLevelClass
     *            the generated base record class
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the class should be generated, false if the generated
     *         class should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成vo抽象类后的回调方法
     * @param topLevelClass vo抽象类
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean voModelAbstractClassGenerated(TopLevelClass topLevelClass,
                                                  IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成vo类后的回调方法
     * @param topLevelClass vo类
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean voModelRecordClassGenerated(TopLevelClass topLevelClass,
                                                  IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成CreateVo类后的回调方法
     * @param topLevelClass CreateVo类
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean voModelCreateClassGenerated(TopLevelClass topLevelClass,
                                                IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成UpdateVo类后的回调方法
     * @param topLevelClass UpdateVo类
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean voModelUpdateClassGenerated(TopLevelClass topLevelClass,
                                                IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成ViewVo类后的回调方法
     * @param topLevelClass ViewVo类
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean voModelViewClassGenerated(TopLevelClass topLevelClass,
                                                IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成ExcelVo类后的回调方法
     * @param topLevelClass ExcelVo类
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean voModelExcelClassGenerated(TopLevelClass topLevelClass,
                                              IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成ExcelImportVo类后的回调方法
     * @param topLevelClass ExcelImportVo类
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean voModelExcelImportClassGenerated(TopLevelClass topLevelClass,
                                               IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成RequestVo类后的回调方法
     * @param topLevelClass RequestVo类
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean voModelRequestClassGenerated(TopLevelClass topLevelClass,
                                               IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成缓存实体对象类后的回调方法
     * @param topLevelClass 缓存实体对象类
     * @param introspectedTable 表信息
     * @return true生成，false不生成
     */
    default boolean voModelCacheClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called after the record with BLOBs class is generated by
     * the JavaModelGenerator. This method will only be called
     * if the table rules call for generation of a record with BLOBs class.
     * <br><br>
     * This method is only guaranteed to be called by the default Java
     * model generators. Other user supplied generators may, or may not, call
     * this method.
     *
     * @param topLevelClass
     *            the generated record with BLOBs class
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the class should be generated, false if the generated
     *         class should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called after the example class is generated by the
     * JavaModelGenerator. This method will only be called if the table
     * rules call for generation of an example class.
     * <br><br>
     * This method is only guaranteed to be called by the default Java
     * model generators. Other user supplied generators may, or may not, call
     * this method.
     *
     * @param topLevelClass
     *            the generated example class
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the class should be generated, false if the generated
     *         class should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the SqlMap file has been generated.
     *
     * @param sqlMap
     *            the generated file (containing the file name, package name,
     *            and project name)
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the sqlMap should be generated, false if the generated
     *         sqlMap should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapGenerated(GeneratedXmlFile sqlMap,
            IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean htmlMapGenerated(GeneratedHtmlFile htmlMap,
                             IntrospectedTable introspectedTable,
                                     HtmlGeneratorConfiguration htmlGeneratorConfiguration){
        return true;
    }

    /**
     * This method is called when the SqlMap document has been generated. This
     * method can be used to add additional XML elements the the generated
     * document.
     *
     * @param document
     *            the generated document (note that this is the MyBatis generator's internal
     *            Document class - not the w3c XML Document class)
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the document should be generated, false if the generated
     *         document should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins. Also, if any plugin returns false, then the
     *         <code>sqlMapGenerated</code> method will not be called.
     */
    default boolean sqlMapDocumentGenerated(Document document,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成html文件后的回调方法
     * @param document html文件
     * @param introspectedTable 表信息
     * @param htmlGeneratorConfiguration html文件生成配置
     * @return true生成html文件，false不生成html文件
     */
    default boolean htmlMapDocumentGenerated(org.mybatis.generator.api.dom.html.Document document, IntrospectedTable introspectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration){
        return true;
    }

    /**
     * This method is called when the base resultMap is generated.
     *
     * @param element
     *            the generated &lt;resultMap&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapResultMapWithoutBLOBsElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the countByExample element is generated.
     *
     * @param element
     *            the generated &lt;select&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapCountByExampleElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the deleteByExample element is generated.
     *
     * @param element
     *            the generated &lt;delete&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapDeleteByExampleElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the deleteByPrimaryKey element is generated.
     *
     * @param element
     *            the generated &lt;delete&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the exampleWhereClause element is generated.
     *
     * @param element
     *            the generated &lt;sql&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the baseColumnList element is generated.
     *
     * @param element
     *            the generated &lt;sql&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapBaseColumnListElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the blobColumnList element is generated.
     *
     * @param element
     *            the generated &lt;sql&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapBlobColumnListElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the insert element is generated.
     *
     * @param element
     *            the generated &lt;insert&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapInsertElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the insert selective element is generated.
     *
     * @param element
     *            the generated &lt;insert&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapInsertSelectiveElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean sqlMapInsertOrUpdateSelectiveElementGenerated(XmlElement element,
                                                          IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the resultMap with BLOBs element is generated
     * - this resultMap will extend the base resultMap.
     *
     * @param element
     *            the generated &lt;resultMap&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapResultMapWithBLOBsElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the selectAll element is generated.
     *
     * @param element
     *            the generated &lt;select&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapSelectAllElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成xml文件的自定义工具栏…方法后的回调方法
     * @param element 自定义工具栏…方法的xml元素
     * @param introspectedTable 表的元数据
     * @return 生成xml文件有效，false生成xml文件无效
     */
    default boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成xml文件的selectByKeysDict方法后的回调方法
     * @param element selectByKeysDict方法的xml元素
     * @param introspectedTable 表的元数据
     * @return true生成xml文件的selectByKeysDict方法，false不生成xml文件的selectByKeysDict方法
     */
    default boolean sqlMapSelectByKeysDictElementGenerated(XmlElement element,
                                                             IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the selectByExample element is generated.
     *
     * @param element
     *            the generated &lt;select&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(
            XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the selectByExampleWithBLOBs element is
     * generated.
     *
     * @param element
     *            the generated &lt;select&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the updateByExampleSelective element is
     * generated.
     *
     * @param element
     *            the generated &lt;update&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the updateByExampleWithBLOBs element is
     * generated.
     *
     * @param element
     *            the generated &lt;update&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the updateByExampleWithourBLOBs element is
     * generated.
     *
     * @param element
     *            the generated &lt;update&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(
            XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the updateByPrimaryKeySelective element is
     * generated.
     *
     * @param element
     *            the generated &lt;update&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(
            XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean sqlMapUpdateBatchElementGenerated(
            XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the updateByPrimaryKeyWithBLOBs element is
     * generated.
     *
     * @param element
     *            the generated &lt;update&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(
            XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the updateByPrimaryKeyWithoutBLOBs element is
     * generated.
     *
     * @param element
     *            the generated &lt;update&gt; element
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the element should be generated, false if the generated
     *         element should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(
            XmlElement element, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the SQL provider has been generated.
     * Implement this method to add additional methods or fields to a generated
     * SQL provider.
     *
     * @param topLevelClass
     *            the generated provider
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the provider should be generated, false if the generated
     *         provider should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean providerGenerated(TopLevelClass topLevelClass,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the applyWhere method has
     * been generated in the SQL provider.
     *
     * @param method
     *            the generated applyWhere method
     * @param topLevelClass
     *            the partially generated provider class
     *            You can add additional imported classes to the class
     *            if necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean providerApplyWhereMethodGenerated(Method method,
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the countByExample method has
     * been generated in the SQL provider.
     *
     * @param method
     *            the generated countByExample method
     * @param topLevelClass
     *            the partially generated provider class
     *            You can add additional imported classes to the class
     *            if necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean providerCountByExampleMethodGenerated(Method method,
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the deleteByExample method has
     * been generated in the SQL provider.
     *
     * @param method
     *            the generated deleteByExample method
     * @param topLevelClass
     *            the partially generated provider class
     *            You can add additional imported classes to the class
     *            if necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean providerDeleteByExampleMethodGenerated(Method method,
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the insertSelective method has
     * been generated in the SQL provider.
     *
     * @param method
     *            the generated insertSelective method
     * @param topLevelClass
     *            the partially generated provider class
     *            You can add additional imported classes to the class
     *            if necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean providerInsertSelectiveMethodGenerated(Method method,
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the selectByExampleWithBLOBs method has
     * been generated in the SQL provider.
     *
     * @param method
     *            the generated selectByExampleWithBLOBs method
     * @param topLevelClass
     *            the partially generated provider class
     *            You can add additional imported classes to the class
     *            if necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean providerSelectByExampleWithBLOBsMethodGenerated(Method method,
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the selectByExampleWithoutBLOBs method has
     * been generated in the SQL provider.
     *
     * @param method
     *            the generated selectByExampleWithoutBLOBs method
     * @param topLevelClass
     *            the partially generated provider class
     *            You can add additional imported classes to the class
     *            if necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean providerSelectByExampleWithoutBLOBsMethodGenerated(Method method,
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the updateByExampleSelective method has
     * been generated in the SQL provider.
     *
     * @param method
     *            the generated updateByExampleSelective method
     * @param topLevelClass
     *            the partially generated provider class
     *            You can add additional imported classes to the class
     *            if necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean providerUpdateByExampleSelectiveMethodGenerated(Method method,
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the updateByExampleWithBLOBs method has
     * been generated in the SQL provider.
     *
     * @param method
     *            the generated updateByExampleWithBLOBs method
     * @param topLevelClass
     *            the partially generated provider class
     *            You can add additional imported classes to the class
     *            if necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean providerUpdateByExampleWithBLOBsMethodGenerated(Method method,
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the updateByExampleWithoutBLOBs method has
     * been generated in the SQL provider.
     *
     * @param method
     *            the generated updateByExampleWithoutBLOBs method
     * @param topLevelClass
     *            the partially generated provider class
     *            You can add additional imported classes to the class
     *            if necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean providerUpdateByExampleWithoutBLOBsMethodGenerated(Method method,
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the updateByPrimaryKeySelective method has
     * been generated in the SQL provider.
     *
     * @param method
     *            the generated updateByPrimaryKeySelective method
     * @param topLevelClass
     *            the partially generated provider class
     *            You can add additional imported classes to the class
     *            if necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the method should be generated, false if the generated
     *         method should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean providerUpdateByPrimaryKeySelectiveMethodGenerated(Method method,
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the MyBatis Dynamic SQL support class has
     * been generated in the MyBatis Dynamic SQL runtime.
     *
     * @param supportClass
     *            the generated MyBatis Dynamic SQL support class
     *            You can add additional items to the generated class
     *            if necessary.
     * @param introspectedTable
     *            The class containing information about the table as
     *            introspected from the database
     * @return true if the class should be generated, false if the generated
     *         class should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     */
    default boolean dynamicSqlSupportGenerated(TopLevelClass supportClass, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is called when the MyBatis Dynamic SQL support object has been generated. The format of the class
     * is an outer object with an inner class. The inner class contains the table and column definitions. The outer
     * (singleton) object contains a reference to an instance of the inner class, and shortcut properties that
     * reference the columns of that instance.
     *
     * @param kotlinFile the generated Kotlin file containing the outer object and inner class
     * @param outerSupportObject a reference to the outer object in the file
     * @param innerSupportClass a reference to the inner class
     * @param introspectedTable the class containing information about the table as
     *                          introspected from the database
     * @return true if the generated file should be kept
     */
    default boolean dynamicSqlSupportGenerated(KotlinFile kotlinFile, KotlinType outerSupportObject,
                                               KotlinType innerSupportClass, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * This method is no longer called.
     *
     * @param extensionsFile
     *     the partially generated file
     * @param introspectedTable
     *     The class containing information about the table as introspected from the database
     * @return true if the file should be generated, false if the generated
     *         file should be ignored. In the case of multiple plugins, the
     *         first plugin returning false will disable the calling of further
     *         plugins.
     *
     * @deprecated this method is no longer called
     */
    @Deprecated
    default boolean mapperExtensionsGenerated(KotlinFile extensionsFile, IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * Kotlin生成mapper类后的回调方法
     * @param mapperFile 生成的mapper类
     * @param mapper mapper类的类型
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean mapperGenerated(KotlinFile mapperFile, KotlinType mapper, IntrospectedTable introspectedTable) {
        return true;
    }

    default boolean kotlinDataClassGenerated(KotlinFile kotlinFile, KotlinType dataClass,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * kotlin生成dao类ColumnListProperty后的回调方法
     * @param kotlinProperty 生成的dao类ColumnListProperty
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean clientColumnListPropertyGenerated(KotlinProperty kotlinProperty, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * kotlin生成dao类中insertMultipleVararg方法后的回调方法
     * @param kotlinFunction 生成的dao-insert方法
     * @param kotlinFile 生成的dao文件
     * @param introspectedTable  表信息
     * @return true 生成 false 不生成
     */
    default boolean clientInsertMultipleVarargMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * kotlin生成dao类中updateByPrimaryKey方法后的回调方法
     * @param kotlinFunction 生成的dao-updateByPrimaryKey方法
     * @param kotlinFile 生成的dao文件
     * @param introspectedTable  表信息
     * @return true 生成 false 不生成
     */
    default boolean clientUpdateByPrimaryKeyMethodGenerated(KotlinFunction kotlinFunction, KotlinFile kotlinFile,
            IntrospectedTable introspectedTable) {
        return true;
    }

    /**
     * 生成service类后的回调方法
     * @param interfaze 生成的service类
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean serviceGenerated(Interface interfaze,IntrospectedTable introspectedTable){
        return true;
    }

    /**
     * 生成子service类(用于个性化service的类)后的回调方法
     * @param interfaze 生成的子service类
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean subServiceGenerated(Interface interfaze,IntrospectedTable introspectedTable){
        return true;
    }

    /**
     * 生成serviceImpl类后的回调方法
     * @param topLevelClass 生成的serviceImpl类
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean serviceImplGenerated(TopLevelClass topLevelClass,IntrospectedTable introspectedTable){
        return true;
    }

    /**
     * 生成子serviceImpl类(用于个性化serviceImpl的类)后的回调方法
     * @param topLevelClass 生成的子serviceImpl类
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean subServiceImplGenerated(TopLevelClass topLevelClass,IntrospectedTable introspectedTable){
        return true;
    }

    /**
     * 生成controller类后的回调方法
     * @param topLevelClass 生成的controller类
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean controllerGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable){
        return true;
    }

    /**
     * 生成子controller类(用于个性化controller的类)后的回调方法
     * @param topLevelClass 生成的子controller类
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean subControllerGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable){
        return true;
    }

    /**
     * 生成service单元测试类后的回调方法
     * @param topLevelClass 生成的service单元测试类
     * @param introspectedTable 表信息
     * @return true 生成 false 不生成
     */
    default boolean serviceUnitTestGenerated(TopLevelClass topLevelClass,IntrospectedTable introspectedTable){
        return true;
    }
}
