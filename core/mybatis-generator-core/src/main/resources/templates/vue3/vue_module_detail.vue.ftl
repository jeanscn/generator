<!--
* @description ${ tableRemark }显示详情
* @version: detail template version 1.0.7
-->
<template>
    <vgoForm v-if="formConfigReady" v-model="_formData" ref="vgoFormRef"
             :formConfig="_formConfig"
             :showBlankRow="showBlankRow"
             :viewStatus="viewStatus"
             :hideRequiredAsterisk="true"
             labelSuffix=":"
        <#if hasInnerList>
        @inner-list-data-all-ready="innerListDataAllReady"
        @vxe-button-click="defaultInnerListButtonActionHandler"
        </#if>
    >
        <template #default = "{ data }"><slot :data = data></slot></template>
    </vgoForm>
</template>

<script lang="ts" setup name="${ modelName }Detail">
    import { onMounted, onUnmounted, PropType, <#if workflowEnabled >provide,</#if> ref, Ref, watch } from 'vue';
    import { EMPTY_OBJECT } from '@/framework/utils/constant';
    import { TFormConfig } from '@/framework/components/vgoForm/types';
    import { T${ modelName } } from '../types/T${ modelName }';
    import { useFormConfigStore } from '@/store/formConfig';
    <#if hasInnerList>
    import * as extMethod from '@/hooks/useCustomHandle';
    import * as useExtentHooks from '../PrivateUseFormHooks';
    import { TVxeTableActionsParams } from '@/framework/components/VgoVxeTable/types';
    </#if>
    <#if workflowEnabled >
    import { useCurrentTaskAttributesStore } from '@/framework/workflow/store/currentTaskAttributes';

    const currentTaskAttributesStore = useCurrentTaskAttributesStore();
    </#if>
    const formConfigStore = useFormConfigStore();

    const props = defineProps({
        modelValue: { type: Object as PropType<T${ modelName }>, default: EMPTY_OBJECT },
        formConfig: { type: Object as PropType<TFormConfig>, default: EMPTY_OBJECT },       //允许父级组件缓存formConfig
        viewStatus: { type: Number as PropType<number>, default: 1 },                       //是否为查看状态
        dataId: { type: String as PropType<string>, default: null },                       //数据id
        showBlankRow: { type: Boolean as PropType<boolean>, default: false },
    })

    <#if hasInnerList>
    const moduleKey = "${ modelPath }";
    </#if>
    const _restBasePath = '${ restBasePath }';
    const vgoFormRef = ref();
    const _formConfig = ref<TFormConfig>(props.formConfig);
    const _formData = ref<T${ modelName }>(props.modelValue);
    const formConfigReady = ref(false);

    const emit = defineEmits([
        'update:modelValue',
    <#if hasInnerList>
        'inner-list-data-all-ready',
        'vxe-button-click',
    </#if>
    ]);

    watch(() => props.modelValue, (val) => {
        _formData.value = val;
    });

    watch(() => _formData.value, (val) => {
        emit('update:modelValue', val);
    });

    <#if hasInnerList>
    const innerListDataAllReady = (data: any, grid: any, listKey: string, formData: T${ modelName }) => {
        if (typeof useExtentHooks.default[`innerListDataAllReady`] === 'function') {
            useExtentHooks.default[`innerListDataAllReady`](data, grid, listKey, formData);
        }else{
            emit('inner-list-data-all-ready', data, grid, listKey, formData);
        }
    };
    const defaultInnerListButtonActionHandler = (params: TVxeTableActionsParams) => {
        params = {
            moduleKey: moduleKey,
            ...params,
        };
        if (extMethod.defaultInnerListButtonActionHandler && typeof extMethod.defaultInnerListButtonActionHandler === 'function') {
            extMethod.defaultInnerListButtonActionHandler(params);
        } else {
            emit('vxe-button-click', params);
        };
    };
    </#if>

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
            const taskAttributes = await currentTaskAttributesStore.getCurrentTaskAttributesWithFetch(_formData.value?.id ?? '');
            _formData.value['actionList'] = taskAttributes?.taskExtAttributes?.actionList||[];
            _formData.value['taskAttributes'] = taskAttributes;
        }
        isTaskAttributesLoaded.value = true;
        </#if>
    });

    onUnmounted(() => {
        <#if workflowEnabled >
        if(_formData.value){
            currentTaskAttributesStore.deleteCurrentTaskAttributes(_formData.value?.id ?? '');
            _formData.value['actionList'] = [];
            _formData.value['taskAttributes'] = {};
        }
        </#if>
    });

    const getFormRef = () => {
        return vgoFormRef.value;
    };

    defineExpose({
        getFormRef,
    })

</script>

<style lang="scss" scoped>
    :deep(.el-form-item) {
        --font-size: 1em;
        --el-form-label-font-size: 1em;
        margin: 7.5px 0 -1px 0;
        border-bottom: 1px solid var(--el-border-color);
        height: 100%;
    }

    :deep(.el-form-item__label) {
        min-height: 32px;
        line-height: 32px;
        height: 100%;
        font-size: var(--el-form-label-font-size);
        .main-label {
            font-weight: 500;

            &::after {
                content: '：';
                margin-right: 8px;
            }
        }
    }
    :deep(a:link) {
        text-decoration: underline !important;
        color: var(--el-color-primary-dark-3);
    }

    :deep(.vgo-editor) {
        min-height: 120px;
        img {
            max-width: 100%;
            height: auto;
        }
    }
</style>