/**
* @description DialogForm component for ${ componentName } module
*/
<template>
    <VgoDialog  v-model="showDialog" :title="pageTitle" :popSize="popSize" :draggable="popDraggable"
                @close="destroyForm" :closeOnClickModal=false :closeOnPressEscape=false>
        <${ componentName }Edit v-if="showDialog && _viewStatus === 1" ref="bizFormRef" v-model="_formData"
                         :formConfig="_formConfig" :viewStatus="_viewStatus" @form-submit="onSubmit"></${ componentName }Edit>
        <${ componentName }Detail v-if="showDialog && _viewStatus === 0" ref="bizFormRef" v-model="_formData"
                           :formConfig="_formConfig" :viewStatus="_viewStatus"></${ componentName }Detail>
        <template #footer>
            <FormButtonsBar v-model="_formData" :formConfig="_formConfig" :viewStatus="_viewStatus" popType="dialog"
                            @default-form-button-click="defaultFormButtonActionHandler($event, 'dialog')">
            </FormButtonsBar>
        </template>
    </VgoDialog>
</template>

<script lang="ts" setup name="${ componentName }Modal">
    import { onMounted, watch, PropType, ref, inject, Ref } from 'vue';
    import { TFormConfig } from '@/framework/components/vgoForm/types';
    import { IButtonProps } from '@/framework/types/core';
    import { TFormButtonActionParam } from '@/hooks/types';
    import { ServiceApi } from '@/api/service';
    import { TVgoTableInstance } from '@/framework/components/vgoTable/types';
    import { useI18n } from 'vue-i18n';
    import * as extMethod from '@/hooks/useCustomHandle';
    import { EMPTY_OBJECT } from '@/framework/utils/constant';
    import _ from 'lodash';

    import { T${ modelName } } from '../${ modelPath }/types/T${ modelName }';
    import ${ componentName }Edit from '../${ modelPath }/components/${ componentName }Edit.vue';
    import ${ componentName }Detail from '../${ modelPath }/components/${ componentName }Detail.vue';
    import { TGlobalDialog } from '@/framework/layout/components/GlobalDialog.vue';

    import FormButtonsBar from '@/framework/workflow/components/FormButtonsBar.vue';

    const emit = defineEmits(['update:modelValue', 'close']);

    const props = defineProps({
        modelValue: { type: Boolean as PropType<boolean>, default: false },
        viewStatus: { type: Number as PropType<number>, default: 1 },
        moduleId: { type: String as PropType<string>, default: '' },
        applyWorkflow: { type: Number as PropType<number>, default: 0 },
        formConfig: { type: Object as PropType<TFormConfig>, default: EMPTY_OBJECT },
        formData: { type: Object as PropType<T${ modelName }>, default: EMPTY_OBJECT },
        service: { type: Object as PropType<ServiceApi<any>>, default: EMPTY_OBJECT },
    })

    const i18n = useI18n();
    const moduleKey = "${ modelPath }";
    const _moduleId = ref<string>(props.moduleId);
    const _applyWorkflow = ref<number>(props.applyWorkflow);

    const tableRef = ref<TVgoTableInstance>();
    const buttonRef = ref<IButtonProps>();
    const bizFormRef = ref();

    const showDialog = ref(props.modelValue);

    const pageTitle = ref('');
    const popSize = ref<string>('largeX');
    const popDraggable = ref<boolean>(false);
    const _viewStatus = ref<number>(props.viewStatus);
    const _formConfig = ref<TFormConfig>(props.formConfig);

    const _formData = ref<T${ modelName }>(props.formData);
    const _service = ref<ServiceApi<T${ modelName }>>();

    _service.value = new ServiceApi<T${ modelName }>(_formConfig.value.restBasePath);

    watch(() => props.modelValue, (val) => {
        showDialog.value = val;
    },{ immediate: true });


    onMounted(() => { })

    const globalDialog:Ref<TGlobalDialog> = inject('globalDialog', ref({
        openGlobalDialog: () => {
            console.log('openGlobalDialog()不存在');
        },
        closeGlobalDialog: () => {
            console.log('closeGlobalDialog()不存在');
        },
        getComponentRef: () => {
            console.log('getComponentRef()不存在');
        },
    }));

    const defaultFormButtonActionHandler = (button: IButtonProps, popType: string) => {
        buttonRef.value = button;
        let params: TFormButtonActionParam<T${ modelName }> = {
            moduleKey: moduleKey,
            moduleId: _moduleId.value,
            applyWorkflow: _applyWorkflow.value,
            service: _service,
            button: button,
            formData: _formData,
            formConfig: _formConfig,
            dialogVisible: showDialog,
            tableRef: tableRef,
            pageTitle: pageTitle,
            popType: popType,
            bizFormRef: bizFormRef,
            i18n: i18n,
            viewStatus: _viewStatus,
            globalDialog: globalDialog,
        };
        extMethod.defaultFormButtonActionHandler<T${ modelName }>(params);
    };

    const onSubmit = (val) => {
        tableRef.value!.updateRows([val]);
    };
    const destroyForm = () => {
        showDialog.value = false;
        _formData.value = {};
        emit('update:modelValue', false);
        emit('close');
    };

</script>

<style lang="scss" scoped></style>