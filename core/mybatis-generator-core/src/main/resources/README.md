更新日志：
# 2023-9-27
## 一、更新说明：
* 1、修正表单switch元素生成格式的错误。
# 2023-9-26
## 一、更新说明：
* 1、更新列表操作的生成方式，支持动态生成列表toolbar及定义回调方法。
* 2、更新列表行操作列的操作生成方式，支持动态生成列表actionBar及定义回调方法。
## 二、新增功能：
* 1、新增列表查询的动态定义及生成。
# 2023-8-31
## 一、更新说明：
* 1、打印相关调整
## 二、新增功能：
* 1、新增前端操作按钮、链接定义，及动态计算内容。
# 2023-8-9
## 一、更新说明：
* 1、htmlElementDescriptor元素增加属性：multiple，用于指定生成select的元素是否支持多选的配置。
# 2023-8-4
## 一、新增功能：
* 1、table标签新增processDefinedKey属性：当生成对象为流程类对象时，指定该属性会生成流程定义数据。
* 2、当service标签中定义事件监听时，为自动生成监听类并生成指定属性的默认重写。
* 3、如果当前生成实体的父类指定的为流程类抽象时，会生成flowable的流程监听类，默认实现流程启动及结束事件的默认重写。
# 2023-7-31
## 一、更新说明：
* 1、统一html配置的三个属性：
  ** htmlHiddenColumns 页面文件中隐藏的列，多个”,“分隔。将影响当前表的默认列表，列表字段范围和页面编辑时的表单
  ** htmlReadonlyFields 页面文件中只读的列，多个”,“分隔。将影响当前表的默认列表，列表字段范围和页面编辑时的表单
  ** htmlDisplayOnlyFields 页面文件中只显示的列，多个”,“分隔。将影响当前表的默认列表，列表字段范围和页面编辑时的表单
# 2023-7-28
## 一、新增功能：
1、 controller配置增加属性：entityEvent ，用于指定需要发布实体生命周期阶段事件的类型，用于发布实体的增删改的相关事件。
# 2023-7-20
## 一、新增功能：
* 1、innerListView元素增加listKey属性，用来唯一标识innerListView，允许一个表定义多个list配置。
        与之相对应的，htmlElementInnerList增加listKey属性，用来指定使用哪个innerListView。
* 2、htmlElementInnerList元素增加moduleKeyword属性，当引用不同模块的列表时，指定模块的关键字。

# 2023-7-15
## 一、新增功能：
* 1、新增默认生成缺省列表渲染包括：name（链接）、state启用状态、wfState（流程状态）、priority（缓急程度）。
## 二、更新说明
* 1、htmlElementDescriptor元素增加labelCss属性，可以为生成的元素label添加附加的css样式。
* 2、htmlElementDescriptor元素增加elementCss属性，可以为生成的元素添加附加的css样式。
* 3、innerListView元素增加listColumnDescriptor属性，用于指定列表的列配置参数。
## 三、修正bug：
* 1、内置列表无法正确继承context和table的html隐藏属性问题。

# 2023-7-11
## 更新说明
* 1、viewVO增加toolbar属性，用于定制列表左上的操作按钮。可选值：CREATE、EDIT、REMOVE、SEARCH、REFRESH、EXPORT、IMPORT、PRINT、COLUMN。
# 2023-7-06
## 更新说明
* 1、页面生成元素中添加approvalComment元素，用于生成审批意见组件。（一个页面可添加0或n个）
* 2、页面生成元素中添加htmlFileAttachmentList元素，用于生成文件上传列表组件。（一个页面可添加0或1个）
# 2023-7-02
## 更新说明
* 1、生成工作流相关内容调整
* 2、配置文件中generateHtml标签的overWriteFile修改为overWriteHtmlFile，用于区分是否覆盖生成的html文件。
* 3、配置文件中generateHtml标签中增加overWriteJsFile，用于定义是否覆盖生成的js文件。
* 4、配置文件中generateHtml标签中增加overWriteCssFile，用于定义是否覆盖生成的css文件。
## 修正bug：
* 1、修正列表继承隐藏字段及指定显示属性列表不正确的问题。
# 2023-6-19
## 更新说明
* 1、删除sys_cfg_module表或者修改字段名：module_tag->code_,module_name->name_。
* 2、wf_per_done|wf_per_todo| 删除字段：module_name。
## 新增功能：
* 1、当指定的父类中包含”cur_processors“、"module_id"字段时，自动生成翻译字段curProcessorsName和moduleId。
# 2023-6-16
## 修正bug：
* 1、更新生成配置中通过dataFormat指定默认类型时候，生成内容缺少属性的问题。
# 2023-6-9
## 一、新增功能：
* 1、增加新配置元素innerListView，用于页内列表生成功能，可以生成页面带编辑的列表，可以单独配置编辑器也可以继承指定页面的编辑器配置。
* 2、htmlElementDescriptor增加属性：switchText，用来指定当值为1、0的开关时对应文本。
* 3、新增实例化空对象的api接口，用于生成空对象的实例（newInstance）。
## 二、更新功能：
  * 1、htmlElementDescriptor指定dataFormat时，转换dataSource为DictEnum类型，enumClassFullName赋值对应的枚举。

| dataFormat | dataSource | enumClassFullName |
|:---|:---|:---|
| yes、true、是、是否 | DictEnum | com.vgosoft.core.constant.enums.core.YesNoEnum |
|exist、有、有无|DictEnum|com.vgosoft.core.constant.enums.core.ExistOrNotEnum|
|sex、性别|DictEnum|com.vgosoft.core.constant.enums.core.GenderEnum|
|state、启停、启用停用|DictEnum|com.vgosoft.core.constant.enums.core.CommonStatusEnum|
|level、级别|DictEnum|com.vgosoft.core.constant.enums.core.LevelListEnum|
|急、缓急|DictEnum|com.vgosoft.core.constant.enums.core.UrgencyEnum|
# 2023-5-26
## 一、新增功能：
* 1、htmlElementDescriptor增加callback属性，可以在生成select（弹窗选择时），同时在js中增加一个回调函数，当前用于处理关系表的操作，比如在编辑用户时，更新用户-部门关系表数据。
# 2023-5-25
## 一、新增功能：
* 1、增加生成Excel导入实体的功能，用于区分导出的实体属性。用于生成默认导入模板和导入数据的接收。
## 二、更新功能：
* 1、更新excelVO生成配置，增加importIncludeColumns，importExcludeColumns，使用方式与includeColumns，excludeColumns相同，用于导入列的处理。
* 2、基于最新vgosoft-constant包enum路径的调整，更新生成中相关enum引用的路径。
## 三、修正bug：
* 1、excelVO对象增加Dict注解的属性的getter方法，可以导出转换前进行原始值赋值。
* 2、对表名中的空格进行过滤，避免生成异常。
# 2023-5-21
## 一、新增功能：
## 二、更新功能：
* 1、更新默认列表的字段渲染功能，有列名更新为属性名，以便于支持非数据字段的渲染，同时支持多个属性渲染为同一个效果。
## 三、修正bug：
* 1、更新父级时，如果上级为自己，会造成数据读取的死循环错误（目前还是存在隔级循环的问题）。
# 2023-5-19
## 一、新增功能：
* 1、增加请求enum数据类型的支持，配置文件：增加页面元素数据为enum的配置，如下：
    <htmlElementDescriptor column="type_" tagType="dropdownlist" dataSource="DictEnum" enumClassFullName="com.vgosoft.system.enums.MenuTypeEnum"/>
* 2、增加编辑页面自动生成页面组件的功能，
    parent_id:自动生成父级id选择弹窗,
    state_: 自动生成启用-停用，开关,
    如果字段注释以“是否”开头，则默认为“是否”开关，数据源为：DictEnum，枚举类全路径为：com.vgosoft.core.constant.enums.YesNoEnum
## 二、更新功能：
* 1、在viewVo配置中，支持按照defaultDisplayFields配置的顺序显示视图列顺序。
* 2、columnOverride增加columnComment属性，用于更新字段注释，从而修改生成的label等内容。
* 3、generateTreeViewCate增加idProperty、nameProperty属性，用于指定树形结构的id、name属性。
* 4、overridePropertyValue增加属性：enumClassFullName，用于定义数据为enum常量的注解属性。
## 三、修正bug：
* 1、excelVo对象的java8日期增加类型转换，支持LocalDate、LocalDateTime、LocalTime、Instant类型。

