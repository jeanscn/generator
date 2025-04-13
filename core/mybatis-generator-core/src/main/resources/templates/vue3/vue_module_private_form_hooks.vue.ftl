/**
 * @description ${ tableRemark }-表单钩子函数定义
 * @version: hooks template version 1.0.4
 * 个性化的钩子函数集合。
 * 固定的函数名称包括：
 * 1.	innerListDataAllReady - 子表数据加载完成的钩子
 * 		innerListDataAllReady: (data: any, grid: any, listKey: string, formData: T${ modelName }) => {})
 * 		参数：data: 子表数据，grid: 子表实例，listKey: 子表键，formData: 主表数据
 * 2.	rowDeleted - 子表行删除后的钩子
 * 		rowDeleted: (params: TVgoVxeTableRowDeletedParams) => {})
 * 3.	defaultInnerListButtonActionHandler - 子表按钮操作的钩子
 * 		defaultInnerListButtonActionHandler: (params: TVxeTableActionsParams) => {})
 * 		参数：params: 子表按钮操作的参数
 */

import { Ref } from "vue";
import { T${ modelName } } from "./types/T${ modelName }";
import { TPrivateHooksParams } from "@/framework/components/vgoForm/types";

export default {
    /*
     * 钩子函数Demo
     * @param formData 表单数据, 通过Ref包装，可以实现数据的双向绑定。注意使用时，需要使用formData.value获取或设置数据
     * @param params 方法的参数（多值数组）
     */
    fooHook: (formData: Ref<T${ modelName }>, params: TPrivateHooksParams) => {
        console.log(formData.value, params);
    },
 };