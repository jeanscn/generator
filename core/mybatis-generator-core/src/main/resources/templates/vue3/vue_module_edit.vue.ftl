/**
* @description ${ tableRemark }编辑组件
* @version: edit template version 1.0.4
*/
<template>
    <vgoForm
            v-if="formConfigReady"
            v-model="_formData"
            ref="vgoFormRef"
            :formConfig="_formConfig"
            :viewStatus="viewStatus"
            @form-submit="onSubmit"
            @item-call-back="callHookByName"
            <#if hasInnerList>
            @row-deleted="rowDeleted"
            @inner-list-data-all-ready="innerListDataAllReady"
            @vxe-toolbar-button-click="(params: TVxeTableActionsParams) => $emit('vxe-toolbar-button-click', params)"
            @vxe-row-button-click="(params: TVxeTableActionsParams) => $emit('vxe-row-button-click', params)"
            </#if>
    ></vgoForm>
</template>

<script lang="ts" setup name="${ modelName }Edit">
    import { onMounted, onUnmounted, PropType, Ref, provide, ref, unref, watch } from 'vue';
    import { EMPTY_OBJECT } from '@/framework/utils/constant';
    import tool from '@/framework/utils/tool';
    import { ServiceApi } from '@/api/service';
    import { loadNewInstance } from '@/hooks/useCustomHandle';
    import { T${ modelName } } from '../types/T${ modelName }';
    import { TFormConfig,TPrivateHooksParams } from '@/framework/components/vgoForm/types';
    import { useFormConfigStore } from '@/store/formConfig';
    import * as useExtentHooks from '../PrivateUseFormHooks';
    import { useI18n } from 'vue-i18n';
    <#if hasInnerList>
    import { TVgoVxeTableRowDeletedParams, TVxeTableActionsParams } from '@/framework/components/VgoVxeTable/types';
    </#if>
    <#if workflowEnabled >
    import { useCurrentTaskAttributesStore } from '@/framework/workflow/store/currentTaskAttributes';

    const currentTaskAttributesStore = useCurrentTaskAttributesStore();
    </#if>
    const i18n = useI18n();
    const formConfigStore = useFormConfigStore();

    const props = defineProps({
        modelValue: { type: Object as PropType<T${ modelName }>, default: EMPTY_OBJECT },
        formConfig: { type: Object as PropType<TFormConfig>, default: EMPTY_OBJECT },       //允许父级组件缓存formConfig
        viewStatus: { type: Number as PropType<number>, default: 1 },                       //是否为查看状态
    })

    const emit = defineEmits([
        'form-submit',
        'update:modelValue',
        <#if hasInnerList>
        'vxe-toolbar-button-click',
        'vxe-row-button-click',
        </#if>
    ]);

    const _restBasePath = '${ restBasePath }';

    const vgoFormRef = ref();
    const service = ref<ServiceApi<T${ modelName }>>(new ServiceApi<T${ modelName }>(_restBasePath));
    const _formConfig = ref<TFormConfig>(props.formConfig);
    const _formData = ref<T${ modelName } | null>(props.modelValue);
    const formConfigReady = ref(false);

    watch(() => props.modelValue, async (val) => {
        if(Object.keys(_formConfig.value).length === 0) return;
        _formData.value = unref(val) || EMPTY_OBJECT;
        if (Object.keys(_formData.value).length === 0) {
            _formData.value = await loadNewInstance<T${ modelName }>(_formConfig, service as Ref<ServiceApi<T${ modelName }>>,i18n) as T${ modelName };
        } else {
            _formData.value = tool.perOnload(
                unref(_formData.value),
                _formConfig.value?.formItems,
            );
        }
    }, { deep: true, immediate: true });

    watch(() => _formData.value, (val) => {
        emit('update:modelValue', val);
    });

    const fetchFormConfigAsync = async (_formConfig:Ref<TFormConfig>) => {
        if (Object.keys(_formConfig.value).length === 0) {
            let formKey = _restBasePath.replace(/\//g, '-');
            _formConfig.value = formConfigStore.hasFormConfig(formKey) ? formConfigStore.getFormConfig(formKey) : await formConfigStore.fetchFormConfigAsync(formKey);
            formConfigReady.value = true;
        } else {
            formConfigReady.value = true;
        }
    };

    <#if workflowEnabled >
    const isTaskAttributesLoaded = ref(false);
    provide('isTaskAttributesLoaded', isTaskAttributesLoaded);
    </#if>

    onMounted( <#if workflowEnabled > async </#if> () => {
        fetchFormConfigAsync(_formConfig);
        <#if workflowEnabled >
        if(!!_formData.value && _formData.value['workflowEnabled']===1){
            const taskAttributes = await currentTaskAttributesStore.getCurrentTaskAttributesWithFetch(_formData.value?.id || 'undefined');
            _formData.value['actionList'] = taskAttributes?.taskExtAttributes?.actionList||[];
            _formData.value['taskAttributes'] = taskAttributes;
        }
        isTaskAttributesLoaded.value = true;
        </#if>
    });

    onUnmounted(() => {
        <#if workflowEnabled >
        if(_formData.value){
            currentTaskAttributesStore.deleteCurrentTaskAttributes(_formData.value?.id || 'undefined');
            _formData.value['actionList'] = [];
            _formData.value['taskAttributes'] = {};
        }
        </#if>
    });

    const onSubmit = (val) => {
        emit('form-submit',val);
    };

    const submit = () => {
        return vgoFormRef.value?.submit();
    };

    const validate = () => {
        return vgoFormRef.value?.validate();
    };

    // 以下处理组件的回调
    const callHookByName = (params: TPrivateHooksParams) => {
        let { hookName } = params;
        if (typeof useExtentHooks.default[hookName] === 'function') {
            useExtentHooks.default[hookName](params);
        } else {
            console.log('Hook ['+ hookName + '] does not exist');
        }
    };

    <#if hasInnerList>
    const rowDeleted = (params: TVgoVxeTableRowDeletedParams) => {
        if (typeof useExtentHooks.default[`rowDeleted`] === 'function') {
            useExtentHooks.default[`rowDeleted`](params)
        }
    };
    const innerListDataAllReady = (data: any, grid: any, listKey: string, formData: T${ modelName }) => {
        if (typeof useExtentHooks.default[`innerListDataAllReady`] === 'function') {
            useExtentHooks.default[`innerListDataAllReady`](data, grid, listKey, formData);
        }
    };
    </#if>

    const getFormRef = () => {
        return vgoFormRef.value;
    };

    defineExpose({
        submit,
        validate,
        getFormRef,
    })

</script>

<style lang="scss" scoped></style>