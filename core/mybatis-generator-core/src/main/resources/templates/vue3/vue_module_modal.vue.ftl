/**
* @description DialogForm component for ${ componentName } module
* @version: modal template version 1.0.1
*/
<template>
    <div class="form-modal-container">
        <VgoDialog v-if="showDialog && type === 'dialog'" v-model="showDialog"
                   :title="_pageTitle"
                   :popSize="_popSize"
                   :draggable="_popDraggable"
                   :elDialogProps="_elDialogProps"
                   @close="destroyForm">
            <${ componentName }Edit v-if="showDialog && _viewStatus === 1" ref="bizFormRef" v-model="_formData"
                           :formConfig="_formConfig" :viewStatus="_viewStatus" @form-submit="onSubmit" />
            <${ componentName }Detail v-if="showDialog && _viewStatus === 0" ref="bizFormRef" v-model="_formData"
                             :formConfig="_formConfig" :viewStatus="_viewStatus" />
            <template #footer>
                <FormButtonsBar v-model="_formData" :formConfig="_formConfig" :viewStatus="_viewStatus" popType="dialog"
                                @default-form-button-click="defaultFormButtonActionHandler($event, 'dialog')">
                </FormButtonsBar>
            </template>
        </VgoDialog>
        <VgoFormDrawer v-if="showDialog && type === 'drawer'" v-model="showDialog"
                       :title="_pageTitle"
                       :size="_popSize"
                       :elDrawerProps="_elDrawerProps"
                       @close="destroyForm">
            <${ componentName }Edit v-if="_viewStatus === 1" ref="bizFormRef" v-model="_formData"
                           :formConfig="_formConfig"
                           :viewStatus="_viewStatus"
                           @form-submit="onSubmit"/>
            <${ componentName }Detail v-if="_viewStatus === 0" ref="bizFormRef" v-model="_formData"
                             :formConfig="_formConfig"
                             :viewStatus="_viewStatus" />
            <template #footer>
                <FormButtonsBar v-model="_formData" :formConfig="_formConfig" :viewStatus="_viewStatus" popType="drawer"
                                @default-form-button-click="defaultFormButtonActionHandler($event, 'drawer')">
                </FormButtonsBar>
            </template>
        </VgoFormDrawer>
    </div>
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
    import { TElDialogProps } from '@/framework/components/vgoDialog/types';
    import VgoFormDrawer from '@/framework/components/vgoDrawer/VgoFormDrawer.vue';
    import { TElDrawerProps } from '@/framework/components/vgoDrawer/types';

    const emit = defineEmits(['update:modelValue', 'close']);

    const props = defineProps({
        modelValue: { type: Boolean as PropType<boolean>, default: false },
        type: { type: String as PropType<'drawer' | 'dialog'>, default: 'dialog' },
        viewStatus: { type: Number as PropType<number>, default: 1 },
        moduleId: { type: String as PropType<string>, default: '${ moduleId }' },
        applyWorkflow: { type: Number as PropType<number>, default: ${workflowEnabled } },
        formConfig: { type: Object as PropType<TFormConfig>, default: EMPTY_OBJECT },
        formData: { type: Object as PropType<T${ modelName }>, default: EMPTY_OBJECT },
        pageTitle: { type: String as PropType<string>, default: '${ tableRemark }' },
        popSize: { type: String as PropType<string>, default: null },
        popDraggable: { type: Boolean as PropType<boolean>, default: null },
        elDialogProps: { type: Object as PropType<TElDialogProps>, default: EMPTY_OBJECT },
        elDrawerProps: { type: Object as PropType<TElDrawerProps>, default: EMPTY_OBJECT },
    })

    const i18n = useI18n();
    const moduleKey = "${ modelPath }";
    const _moduleId = ref<string>(props.moduleId);
    const _applyWorkflow = ref<number>(props.applyWorkflow);

    const tableRef = ref<TVgoTableInstance>();
    const buttonRef = ref<IButtonProps>();
    const bizFormRef = ref();

    const showDialog = ref(props.modelValue);
    const dialogType = ref<'dialog' | 'drawer'>(props.type);

    const defaultElDialogProps: TElDialogProps = {
        destroyOnClose: true,
        showClose: false,
        width: '70%',
        showFullscreen: true,
        fullscreen: false,
        draggable: true,
        closeOnPressEscape: false,
        closeOnClickModal: false,
    };
    const _elDialogProps = ref<TElDialogProps>(_.merge(defaultElDialogProps, props.elDialogProps));
    const defaultElDrawerProps = {
        title: '${ tableRemark }',
        showFullscreen: true,
        fullscreen: false,
        closeOnClickModal: false,
        destroyOnClose: true,
        size: '70%',
    };
    const _elDrawerProps = ref<TElDrawerProps>(_.merge(defaultElDrawerProps, props.elDrawerProps));

    const _pageTitle = ref(props.pageTitle != null ? props.pageTitle : _elDialogProps.value.title || '');
    const _popSize = ref<string>(props.popSize != null ? props.popSize : '70%');
    const _popDraggable = ref<boolean>(props.popDraggable != null ? props.popDraggable : _elDialogProps.value.draggable || true);
    const _viewStatus = ref<number>(props.viewStatus);
    const _formConfig = ref<TFormConfig>(props.formConfig);
    const _formData = ref<T${ modelName }>(props.formData);
    const _service = ref<ServiceApi<T${ modelName }>>();

    _service.value = new ServiceApi<T${ modelName }>(_formConfig.value.restBasePath);

    watch(() => props.modelValue, (val) => {
        showDialog.value = val;
    });

    watch(() => showDialog.value, (val) => {
        emit('update:modelValue', val);
    });


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
            dialogType: dialogType,
            tableRef: tableRef,
            pageTitle: _pageTitle,
            popType: popType,
            bizFormRef: bizFormRef,
            i18n: i18n,
            viewStatus: _viewStatus,
            globalDialog: globalDialog,
        };
        extMethod.defaultFormButtonActionHandler<T${ modelName }>(params);
    };

    const onSubmit = (val) => {
        tableRef.value && tableRef.value.updateRows([val]);
    };
    const destroyForm = () => {
        showDialog.value = false;
        _formData.value = {};
        emit('close');
    };
</script>

<style lang="scss" scoped>
    .form-modal-container {
        display: flex;
        flex-direction: column;
    }
</style>