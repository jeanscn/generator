更新日志：
# 2023-5-21
## 一、新增功能：
## 二、更新功能：
* 1、更新默认列表的字段渲染功能，有列名更新为属性名，以便于支持非数据字段的渲染，同时支持多个属性渲染为同一个效果。
## 三、修正bug：
* 1、更新父级时，如果上级为自己，会造成数据读取的死循环错误。
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

