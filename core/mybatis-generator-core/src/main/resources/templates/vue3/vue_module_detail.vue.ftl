/**
* @description ${ tableRemark }显示详情
* @version: detail template version 1.0.2
*/
<template>
    <vgo-form v-if="formConfigReady" v-model="_formData" ref="vgoFormRef" :formConfig="_formConfig"
              :viewStatus="viewStatus" :hideRequiredAsterisk="true" labelSuffix=":"></vgo-form>
</template>

<script lang="ts" setup name="${ modelName }Detail">
    import { onMounted, onUnmounted, PropType, provide, ref, unref, watch } from 'vue';
    import tool from '@/framework/utils/tool';
    import { EMPTY_OBJECT } from '@/framework/utils/constant';
    import { TFormConfig } from '@/framework/components/vgoForm/types';
    import { T${ modelName } } from '../types/T${ modelName }';
    import { ServiceApi } from '@/api/service';
    import { ElMessage } from 'element-plus';
    import { useFormConfigStore } from '@/store/formConfig';
    import { useI18n } from 'vue-i18n';
    <#if workflowEnabled >
    import { useCurrentTaskAttributesStore } from '@/framework/workflow/store/currentTaskAttributes';

    const currentTaskAttributesStore = useCurrentTaskAttributesStore();
    </#if>
    const formConfigStore = useFormConfigStore();
    const i18n = useI18n();

    const props = defineProps({
        modelValue: { type: Object as PropType<T${ modelName }>, default: EMPTY_OBJECT },
        formConfig: { type: Object as PropType<TFormConfig>, default: EMPTY_OBJECT },       //允许父级组件缓存formConfig
        viewStatus: { type: Number as PropType<number>, default: 1 },                       //是否为查看状态
        dataId: { type: String as PropType<string>, default: null },                       //数据id
    })

    const _restBasePath = '${ restBasePath }';

    const vgoFormRef = ref();

    const service = ref<ServiceApi<T${ modelName }>>(new ServiceApi<T${ modelName }>(_restBasePath));
    const _formConfig = ref<TFormConfig>(props.formConfig);
    const _formData = ref<T${ modelName }>(props.modelValue);
    const formConfigReady = ref(false);
    const dataReady = ref(false);

    const emit = defineEmits(['update:modelValue']);

    watch(() => props.modelValue, (val) => {
        _formData.value = val;
    });

    watch(() => _formData.value, (val) => {
        emit('update:modelValue', val);
    });

    const getFormConfig = async () => {
        let formKey = _restBasePath.replace(/\//g, '-');
        _formConfig.value = formConfigStore.hasFormConfig(formKey) ? formConfigStore.getFormConfig(formKey) : await formConfigStore.fetchFormConfigAsync(formKey);
        formConfigReady.value = true;
    }

    const getFormData = async () => {
        service.value.get(props.dataId).then((resp) => {
            _formData.value = tool.perOnload(
                resp.data,
                _formConfig.value?.formItems,
            );
            dataReady.value = true;
        });
    }

    <#if workflowEnabled >
    const isTaskAttributesLoaded = ref(false);
    provide('isTaskAttributesLoaded', isTaskAttributesLoaded);
    </#if>

    onMounted( <#if workflowEnabled > async </#if> () => {
        if (Object.keys(_formConfig.value).length === 0) {
            getFormConfig();
            formConfigReady.value = true;
        } else {
            formConfigReady.value = true;
        }
        if (Object.keys(_formData.value).length === 0 ) {
            if(props.dataId){
                getFormData();
            }else{
                _formData.value = {};
                dataReady.value = true;
                ElMessage.error('没有获取到${ tableRemark }数据');
            }
        } else {
            _formData.value = tool.perOnload(
                unref(_formData.value),
                _formConfig.value?.formItems,
            );
            dataReady.value = true;
        }
        <#if workflowEnabled >
        if(!!_formData.value && _formData.value['workflowEnabled']===1){
            const taskAttributes = await currentTaskAttributesStore.getCurrentTaskAttributesWithFetch(_formData.value?.id);
            _formData.value['actionList'] = taskAttributes?.taskExtAttributes?.actionList||[];
            _formData.value['taskAttributes'] = taskAttributes;
        }
        isTaskAttributesLoaded.value = true;
        </#if>
    });

    onUnmounted(() => {
        <#if workflowEnabled >
        if(_formData.value){
            currentTaskAttributesStore.deleteCurrentTaskAttributes(_formData.value?.id);
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