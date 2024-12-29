/**
* @description ${ tableRemark }-表单钩子函数定义
* @version: hooks template version 1.0.1
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
