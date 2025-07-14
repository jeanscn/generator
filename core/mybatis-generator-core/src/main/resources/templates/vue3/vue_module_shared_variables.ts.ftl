/**
* @description ${ tableRemark }-${ modelPath }-共享变量定义
* @version: shared variables version 1.0.5
*/
import { TLoadModalProps } from '@/modules/components/loadModals/types'
import { reactive } from 'vue'

// 动态弹窗共享参数
export const loadModalsProps = reactive<TLoadModalProps>({
    dataLoaded: false,
    modelValue: false,
    type: 'dialog' as 'drawer' | 'dialog',
    viewStatus: 0,
    popSize: 'largeX',
    formData: null,
    elDialogProps: {},
    elDrawerProps: {},
    tableRef: undefined,
    requestResult: null,
    loadModalProvideData: null,
    key: '${ componentName }-list',
});