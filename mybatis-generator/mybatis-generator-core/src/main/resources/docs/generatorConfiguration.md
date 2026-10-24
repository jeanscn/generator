I'll create an updated Java class hierarchy merged property table that includes the initial values column as requested:

# Java类层次结构合并属性表

| 属性                                         | 类型 | 初始值 | AbstractGeneratorConfiguration | AbstractModelGeneratorConfiguration | AbstractTableListCommonConfiguration | 说明                    |
|--------------------------------------------|---|---|:---:|:---:|:---:|-----------------------|
| **来自TypedPropertyHolder（父类）**              ||||||
| properties                                 | Properties | null | ✓ | ✓ | ✓ | 属性集合                  |
| **来自AbstractGeneratorConfiguration**       ||||||
| baseTargetPackage                          | String | null | ✓ | ✓ | ✓ | 生成代码的基础包              |
| context                                    | Context | null | ✓ | ✓ | ✓ | 生成上下文的引用              |
| generate                                   | boolean | false | ✓ | ✓ | ✓ | 是否生成代码                |
| subTargetPackage                           | String | null | ✓ | ✓ | ✓ | 生成代码的子包               |
| targetPackage                              | String | null | ✓ | ✓ | ✓ | 生成代码的目标包              |
| targetPackageGen                           | String | null | ✓ | ✓ | ✓ | 生成代码的目标包(替代)          |
| targetProject                              | String | null | ✓ | ✓ | ✓ | 生成代码的目标项目             |
| **来自AbstractModelGeneratorConfiguration**  ||||||
| additionalPropertyConfigurations           | List\<VoAdditionalPropertyGeneratorConfiguration\> | new ArrayList<>() | | ✓ | ✓ | 附加属性配置                |
| equalsAndHashCodeColumns                   | List\<String\> | new ArrayList<>() | | ✓ | ✓ | 用于equals和hashCode方法的列 |
| excludeColumns                             | Set\<String\> | new HashSet<>() | | ✓ | ✓ | 需要排除的列                |
| fullyQualifiedJavaType                     | FullyQualifiedJavaType | null | | ✓ | ✓ | 完全限定的Java类型           |
| overridePropertyConfigurations             | List\<OverridePropertyValueGeneratorConfiguration\> | new ArrayList<>() | | ✓ | ✓ | 属性覆盖配置                |
| voColumnRenderFunGeneratorConfigurations   | List\<VoColumnRenderFunGeneratorConfiguration\> | new ArrayList<>() | | ✓ | ✓ | 列渲染函数配置               |
| voNameFragmentGeneratorConfigurations      | List\<VoNameFragmentGeneratorConfiguration\> | new ArrayList<>() | | ✓ | ✓ | Vo名称片段配置              |
| **来自AbstractTableListCommonConfiguration** ||||||
| columnActions                              | List\<String\> | new ArrayList<>() | | | ✓ | 操作列名                  |
| actionColumnFixed                          | String | null | | | ✓ | ��作列是否固定              |
| actionColumnWidth                          | String | null | | | ✓ | 操作列宽度                 |
| categoryTreeMultiple                       | boolean | false | | | ✓ | 分类树是否允许多选             |
| categoryTreeUrl                            | String | null | | | ✓ | 分类树URL                |
| defaultDisplayFields                       | List\<String\> | new ArrayList<>() | | | ✓ | 默认显示字段                |
| defaultFilterExpr                          | String | null | | | ✓ | 默认过滤表达式               |
| defaultHiddenFields                        | Set\<String\> | new HashSet<>() | | | ✓ | 默认隐藏字段                |
| defaultToolbar                             | List\<String\> | new ArrayList<>() | | | ✓ | 默认工具栏项                |
| detailFormIn                               | String | null | | | ✓ | 详情表单容器                |
| editFormIn                                 | String | null | | | ✓ | 编辑表单容器                |
| enablePager                                | boolean | true | | | ✓ | 是否启用分页                |
| filterColumns                              | List\<String\> | new ArrayList<>() | | | ✓ | 过滤列                   |
| fuzzyColumns                               | List\<String\> | new ArrayList<>() | | | ✓ | 模糊搜索列                 |
| indexColumn                                | String | null | | | ✓ | 索引列名                  |
| indexColumnFixed                           | String | null | | | ✓ | 索引列是否固定               |
| listKey                                    | String | null | | | ✓ | 列表键                   |
| parentMenuId                               | String | null | | | ✓ | 父菜单ID                 |
| queryColumns                               | List\<String\> | new ArrayList<>() | | | ✓ | 查询列                   |
| showActionColumn                           | String | "default" | | | ✓ | 如何显示操作列               |
| showRowNumber                              | boolean | true | | | ✓ | 是否显示行号                |
| size                                       | String | null | | | ✓ | 大小规格                  |
| tableType                                  | String | null | | | ✓ | 表格类型                  |
| title                                      | String | null | | | ✓ | 表格标题                  |
| toolbar                                    | List\<String\> | new ArrayList<>() | | | ✓ | 工具栏项                  |
| totalFields                                | Set\<String\> | null | | | ✓ | 需要显示汇总的字段             |
| totalRow                                   | boolean | false | | | ✓ | 是否显示汇总行               |
| totalText                                  | String | "合计" | | | ✓ | 汇总行文本                 |
| uiFrameType                                | ViewVoUiFrameEnum | null | | | ✓ | UI框架类型                |
| viewMenuElIcon                             | String | null | | | ✓ | 视图菜单的Element UI图标     |