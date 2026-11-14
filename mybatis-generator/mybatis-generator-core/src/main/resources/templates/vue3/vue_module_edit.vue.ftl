<!--
* ${ OVERRIDABLE_FLAG }
* @description ${ tableRemark }编辑组件
* @version: edit template version 1.0.9
-->
<template>
    <vgoForm
            v-if="formConfigReady"
            :modelValue="_formData"
            @update:modelValue="onModelUpdate"
            ref="vgoFormRef"
            :formConfig="_formConfig"
            :viewStatus="viewStatus"
            :showBlankRow="showBlankRow"
            @form-submit="onSubmit"
            @item-call-back="callHookByName"
            <#if hasInnerList>
            @row-deleted="rowDeleted"
            @inner-list-data-all-ready="innerListDataAllReady"
            @vxe-button-click="defaultInnerListButtonActionHandler"
            </#if>
    >
        <template #default = "{ data }"><slot :data = data></slot></template>
    </vgoForm>
</template>

<script lang="ts" setup name="${ modelName }Edit">
    import { PropType, <#if workflowEnabled >provide,</#if> ref, shallowRef, watch } from 'vue';
    import { EMPTY_OBJECT } from '@vgosoft/framework/utils/constant';
    import tool from '@vgosoft/framework/utils/tool';
    import { ServiceApi } from '@vgosoft/framework/api/service';
    import { loadNewInstance } from '@/hooks/useCustomHandle';
    import { T${ modelName } } from '../types/T${ modelName }';
    import { TFormConfig,TPrivateHooksParams } from '@vgosoft/framework/components/vgoForm/types';
    import { useFormConfigStore } from '@vgosoft/framework/store/formConfig';
    import * as useExtentHooks from '../PrivateUseFormHooks';
    import { useI18n } from 'vue-i18n';
    import { showError } from '@vgosoft/framework/utils/actionFeedback';
    <#if hasInnerList>
    import * as extMethod from '@/hooks/useCustomHandle';
    import { TVgoVxeTableRowDeletedParams, TVxeTableActionsParams } from '@vgosoft/framework/components/VgoVxeTable/types';
    </#if>
    <#if workflowEnabled >
    import { useCurrentTaskAttributesStore } from '@vgosoft/framework/workflow/store/currentTaskAttributes';
    import { TTaskAttributes } from '@vgosoft/framework/workflow/types';

    const currentTaskAttributesStore = useCurrentTaskAttributesStore();
    </#if>
    const i18n = useI18n();
    const formConfigStore = useFormConfigStore();

    type EditModel = T${ modelName }<#if workflowEnabled > & {
        workflowEnabled?: number;
        actionList?: TTaskAttributes['taskExtAttributes']['actionList'];
        taskAttributes?: TTaskAttributes | null;
    }</#if>;

    const props = defineProps({
        modelValue: { type: Object as PropType<EditModel>, default: EMPTY_OBJECT },
        formConfig: { type: Object as PropType<TFormConfig>, default: EMPTY_OBJECT },       //允许父级组件缓存formConfig
        viewStatus: { type: Number as PropType<number>, default: 1 },                       //是否为查看状态
        showBlankRow: { type: Boolean as PropType<boolean>, default: false },
    })

    const emit = defineEmits([
        'form-submit',
        'update:modelValue',
        <#if hasInnerList>
        'vxe-button-click',
        </#if>
    ]);

    <#if hasInnerList>
    const moduleKey = "${ modelPath }";
    </#if>
    const _restBasePath = '${ restBasePath }';

    const vgoFormRef = ref();
    const service = shallowRef(new ServiceApi<T${ modelName }>(_restBasePath));
    const _formConfig = ref<TFormConfig>(props.formConfig);
    const _formData = ref<EditModel | null>(props.modelValue);
    const formConfigReady = ref(false);

    watch(() => props.formConfig, async (config, _previous, onCleanup) => {
        let active = true;
        onCleanup(() => { active = false; });
        formConfigReady.value = false;
        _formConfig.value = {};
        const formKey = _restBasePath.replace(/\//g, '-');
        try {
            const loaded = config && Object.keys(config).length > 0
                ? config
                : formConfigStore.hasFormConfig(formKey)
                    ? formConfigStore.getFormConfig(formKey)
                    : await formConfigStore.fetchFormConfigAsync(formKey);
            if (!active) return;
            if (!loaded || Object.keys(loaded).length === 0) {
                throw new Error(i18n.t('vxeTable.actionWarning.fetchFail', [formKey]));
            }
            _formConfig.value = loaded;
        } catch (error) {
            if (active) showError(error, i18n.t('vxeTable.actionWarning.fetchFail', [formKey]));
        }
    }, { immediate: true, flush: 'sync' });

    const onModelUpdate = (val: EditModel) => {
        _formData.value = val;
        emit('update:modelValue', val);
    };

    let initializedConfig: TFormConfig | undefined;
    watch([_formConfig, () => props.modelValue], async ([config, val], _previous, onCleanup) => {
        if (config === initializedConfig && val === _formData.value) return;
        let active = true;
        onCleanup(() => { active = false; });
        formConfigReady.value = false;
        if (Object.keys(config).length === 0) {
            initializedConfig = undefined;
            return;
        }
        try {
            const data = val && Object.keys(val).length > 0
                ? tool.perOnload(val, config.formItems)
                : await loadNewInstance<T${ modelName }>(_formConfig, service, i18n);
            if (!active || !data) return;
            initializedConfig = config;
            onModelUpdate(data);
            formConfigReady.value = true;
        } catch (error) {
            if (active) showError(error, i18n.t('api.create_error'));
        }
    }, { immediate: true, flush: 'sync' });

    <#if hasInnerList>
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

    <#if workflowEnabled >
    const isTaskAttributesLoaded = ref(false);
    provide('isTaskAttributesLoaded', isTaskAttributesLoaded);
    watch([formConfigReady, _formData, () => _formData.value?.id, () => _formData.value?.workflowEnabled],
        async ([ready, data, businessKey, workflowEnabled], _previous, onCleanup) => {
            let active = true;
            onCleanup(() => { active = false; });
            isTaskAttributesLoaded.value = false;
            if (!ready || !data) return;
            data.actionList = [];
            data.taskAttributes = null;
            if (workflowEnabled !== 1 || !businessKey) {
                isTaskAttributesLoaded.value = true;
                return;
            }
            onCleanup(() => {
                active = false;
                currentTaskAttributesStore.deleteCurrentTaskAttributes(businessKey);
                data.actionList = [];
                data.taskAttributes = null;
            });
            try {
                const taskAttributes = await currentTaskAttributesStore.getCurrentTaskAttributesWithFetch(businessKey);
                if (!active) return;
                data.actionList = taskAttributes?.taskExtAttributes?.actionList || [];
                data.taskAttributes = taskAttributes;
                isTaskAttributesLoaded.value = true;
            } catch (error) {
                if (active) showError(error, i18n.t('vxeTable.actionWarning.fetchFail', [businessKey]));
            }
        }
    , { immediate: true, flush: 'sync' });
    </#if>

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