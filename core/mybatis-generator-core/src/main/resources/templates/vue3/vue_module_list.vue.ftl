/**
* @description ${ tableRemark }列表组件
*/
<template>
    <el-container>
        <el-aside width="220px" v-if="showTreePanel">
            <TreePanel :treeDataUrl="categoryTreeUrl" :treePanelProps="sideTreePanelProps"
                       @node-click="navTreeItemClick"></TreePanel>
        </el-aside>
        <el-container>
            <el-main class="nopadding">
                <vgo-table v-if="tableConfigReady" ref="tableRef" tableName="${ tableName }"  modelType="${ modelType!'default' }"
                           :apiObj="tableApiObj.fetchTableData"
                           :tableConfigProps="_tableConfigProps"
                           @view-row-button-click="defaultViewRowActionHandler"
                           @view-toolbar-button-click="defaultDtCustomButtonActionHandler"
                           row-key="id" stripe remoteSort>
                    <template v-for="column in _tableConfigProps!.tableColumns" :key="Math.random" #[column.prop]="scope">
                        <PrivateTableColumnSlots :scope="scope" :slotName="column.prop"></PrivateTableColumnSlots>
                    </template>
                </vgo-table>
                <VgoDialog v-if="showDialog" v-model="showDialog" :title="pageTitle" :popSize="popSize" :draggable="popDraggable" @close="destroyForm"
                           :closeOnClickModal=false :closeOnPressEscape=false>
                    <${ componentName }Edit v-if="showDialog && viewStatus === 1" ref="bizFormRef" v-model="formData"
                                        :formConfig="formConfig" :viewStatus="viewStatus" @form-submit="onSubmit"></${ componentName }Edit>
                    <${ componentName }Detail v-if="showDialog && viewStatus === 0" ref="bizFormRef" v-model="formData"
                                          :formConfig="formConfig" :viewStatus="viewStatus"></${ componentName }Detail>
                    <template #footer>
                        <FormButtonsBar v-model="formData" :formConfig="formConfig" :viewStatus="viewStatus"
                            popType="dialog" @default-form-button-click="defaultFormButtonActionHandler($event,'dialog')">
                        </FormButtonsBar>
                    </template>
                </VgoDialog>
                <VgoTreeDrawer v-if="showTreeDrawer" v-model="showTreeDrawer" v-model:treeSelected="treeSelected" :title="pageTitle"
                               :apiObj="drawerTreeApiObj" :treePanelProps="drawerTreePanelProps"
                               :mainRecordId="mainRecordId" :drawerOnButtonId="drawerOnButtonId" @check="treeDrawerCheckCheck">
                </VgoTreeDrawer>
                <VgoFormDrawer v-if="showFormDrawer" :visible.sync="showFormDrawer" v-model="showFormDrawer" :title="pageTitle" :formData="formData"  :close-on-click-modal=false
                               :size="drawerSize as string | undefined" :formConfig="formConfig" :viewStatus="viewStatus"
                               @form-submit="onSubmit">
                    <${ componentName }Edit v-if="viewStatus === 1" ref="bizFormRef" v-model="formData"
                                        :formConfig="formConfig" :viewStatus="viewStatus" @form-submit="onSubmit"></${ componentName }Edit>
                    <${ componentName }Detail v-if="viewStatus === 0" ref="bizFormRef" v-model="formData"
                                          :formConfig="formConfig" :viewStatus="viewStatus"></${ componentName }Detail>
                    <template #footer>
                        <FormButtonsBar v-model="formData" :formConfig="formConfig" :viewStatus="viewStatus"
                            popType="drawer" @default-form-button-click="defaultFormButtonActionHandler($event,'drawer')">
                        </FormButtonsBar>
                    </template>
                </VgoFormDrawer>
                <vgoFileImport v-model="showImportDialog" :service="service" @on-success="importSuccess"/>
            </el-main>
        </el-container>
    </el-container>

</template>
<script lang="ts" setup name="${ componentName }">

    import { Ref, watch, onMounted, ref, computed, inject } from 'vue';
    import { useRoute } from 'vue-router';
    import { TTableConfigProps, TTableApiObj, TVgoTableInstance } from "@/framework/components/vgoTable/types";
    import API from '@/api';
    import { ServiceApi } from '@/api/service';
    import { isEmpty, isNullOrUnDef } from '@/framework/utils/is';
    import * as extMethod from '@/hooks/useCustomHandle';
    import TreePanel from '@/framework/application/components/TreePanel.vue';
    import { TTreeApiObj, TTreePanelButtons, TTreePanelProps} from '@/framework/application/types';
    import { TFormConfig } from '@/framework/components/vgoForm/types';
    import { T${ componentName } } from '../${ modelPath }/types/T${ componentName }';
    import VgoTreeDrawer from '@/framework/components/vgoDrawer/VgoTreeDrawer.vue';
    import VgoFormDrawer from '@/framework/components/vgoDrawer/VgoFormDrawer.vue';
    import ${ componentName }Edit from '../${ modelPath }/components/${ componentName }Edit.vue';
    import ${ componentName }Detail from '../${ modelPath }/components/${ componentName }Detail.vue';
    import PrivateTableColumnSlots from '../${ modelPath }/PrivateTableColumnSlots.vue';
    import { useTableConfigStore } from '@/store/tableConfig';
    import { useFormConfigStore } from '@/store/formConfig';
    import { TDtCustomButtonActionParam, TFormButtonActionParam, TViewRowActionParam, } from '@/hooks/types';
    import { TTreeCheckDataProps, TVgoTreeProps } from '@/framework/components/vgoTree/types';
    import FormButtonsBar from '@/framework/workflow/components/FormButtonsBar.vue';
    import { TApi } from '@/api/types';
    import { useI18n } from 'vue-i18n';
    import { TGlobalDialog } from '@/framework/layout/components/GlobalDialog.vue';
    import { TGlobalDrawer } from '@/framework/layout/components/GlobalDrawer.vue';
    import { IButtonProps } from '@/framework/types/core';
    import vgoFileImport from '@/framework/components/vgoFileImport/index.vue';

    const i18n = useI18n();

    const moduleKey = "${ modelPath }";
    const permissionKey = '${ permissionKey }';
    const permissionActions = ref<string[]>([]);

    const tableConfigStore = useTableConfigStore();
    const formConfigStore = useFormConfigStore();

    const showImportDialog = ref(false);

    const route = useRoute();
    const meta = route.meta as { viewId: string };
    const tableRef = ref<TVgoTableInstance>();
    const tableConfigReady = ref<boolean>(false);
    const permissionReady = ref<boolean>(false);
    const showTreePanel = ref(false);
    const tableColumns = ref([]) as any;
    const _tableConfigProps = ref<TTableConfigProps | null>(null);
    const pageTitle = ref('');
    const showDialog = ref(false);
    const formConfig = ref<TFormConfig>();
    const formData = ref<T${ componentName }>({});
    const popSize = ref<string>('default');
    const popDraggable = ref<boolean>(false);
    const bizFormRef = ref();
    const restBasePath = ref<any>('');
    const showTreeDrawer = ref<boolean>(false);
    const showFormDrawer = ref<boolean>(false);
    const drawerTreeApiObj = ref<TTreeApiObj>();
    const treeSelected = ref<string[]>([]);
    const drawerOnButtonId = ref('');
    const buttonRef = ref<IButtonProps>();
    const mainRecordId = ref('');
    const categoryTreeUrl = ref('');
    const service = ref<ServiceApi<any>>();
    const tableApiObj = ref<TTableApiObj>({
        fetchTableConfig: async (...params: any) => (API as TApi).common.viewConfig.post(params),
        fetchTableData: async (params: any) => service.value!.listPost(params),
        fetchFormConfig: async (...params: any) =>  (API as TApi).common.formConfig.get(params),
    });
    const sideTreePanelProps = ref<TTreePanelProps>({
        treePanelButtons: {
            showAll: true,
            collapseAll: true,
            expandAll: true,
            add: false,
            edit: false,
            del: false,
        } as TTreePanelButtons,
        vgoTreeProps: {
            showCheckbox: false,
        } as TVgoTreeProps,
    });
    const drawerTreePanelProps = ref<TTreePanelProps>({
        treePanelButtons: {
            showAll: false,
            collapseAll: true,
            expandAll: true,
            add: false,
            edit: false,
            del: false,
        } as TTreePanelButtons,
        vgoTreeProps: {
            showCheckbox: true,
            checkStrictly: true,
            defaultExpandAll: true,
            props: {
                children: 'children',
                label: 'name',
                value: 'id',
                disabled: (data,node)=>{
                    return false;
                },
            },
        } as TVgoTreeProps,
        treePanelWidth: '400px',
    });
    const viewStatus = ref<number>(1);
    const drawerSize = computed(() => {
        return popSize.value === 'small' ? '40%' : popSize.value === 'large' ? '70%'  : popSize.value === 'largeX' ? '90%' : '50%';
    });
    const applyWorkflow = ref<number>(0);
    const moduleId = ref<string>('');
    onMounted(() => {
        loadViewConfig();
        getActionPermission();
    });

    watch(() => showDialog.value, (val) => {
        if (!val) {
            bizFormRef.value = null;
        }
    });

    const importSuccess = () => {
        tableRef.value!.refresh();
    };

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

    const globalDrawer:Ref<TGlobalDrawer> = inject('globalDrawer', ref({
        openGlobalDrawer: () => {
            console.log('openGlobalDialog()不存在');
        },
        closeGlobalDrawer: () => {
            console.log('openGlobalDrawer()不存在');
        },
        getComponentRef: () => {
            console.log('getComponentRef()不存在');
        },
    }));

    const getActionPermission = async () => {
        //获取对当前用户的操作权限
        let permissionResp = await new ServiceApi('system/sys-permission-action-impl').get('get-current-user-permission-action',{permissionParentId: permissionKey});
        if(permissionResp.data !== null){
            permissionActions.value =  permissionResp.data.map((item: string) => item.substring(item.lastIndexOf(':') + 1));
        }
        permissionReady.value = true;
    }

    const loadViewConfig = async () => {
        const tableKey = meta.viewId;
        _tableConfigProps.value = tableConfigStore.hasTableConfig(tableKey) ? tableConfigStore.getTableConfig(tableKey) : await tableConfigStore.fetchTableConfigAsync(tableKey);
        pageTitle.value = _tableConfigProps.value!.listName || '';
        tableColumns.value = _tableConfigProps.value!.tableColumns;
        restBasePath.value = _tableConfigProps.value!.restBasePath;
        applyWorkflow.value = _tableConfigProps.value!.applyWorkflow || 0;
        moduleId.value = _tableConfigProps.value!.moduleId || '';
        service.value = new ServiceApi(restBasePath.value);
        tableConfigReady.value = true;
        let cateUrl = _tableConfigProps.value!.categoryTreeUrl;
        if (cateUrl && !isEmpty(cateUrl) && !isNullOrUnDef(cateUrl)) {
            categoryTreeUrl.value = cateUrl;
            showTreePanel.value = true;
        }
        //form配置
        let formKey = restBasePath.value.replace(/\//g, '-');
        formConfig.value = formConfigStore.hasFormConfig(formKey) ? formConfigStore.getFormConfig(formKey) : await formConfigStore.fetchFormConfigAsync(formKey);
        popSize.value = formConfig.value!.popSize||'default';
        popDraggable.value = formConfig.value!.popDraggable||false;
    };

    const defaultViewRowActionHandler = (rowData: any, button: IButtonProps) => {
        drawerOnButtonId.value = button.id;
        formData.value = {...rowData};
        buttonRef.value = button;
        let params: TViewRowActionParam<T${ componentName }> = {
            moduleKey: moduleKey,
            moduleId: moduleId.value,
            applyWorkflow: applyWorkflow.value,
            rowData: rowData,
            button: button,
            formData: formData,
            formConfig: formConfig,
            dialogVisible: showDialog,
            service: service,
            tableRef: tableRef,
            drawerTreeApiObj: drawerTreeApiObj,
            drawerTreePanelProps: drawerTreePanelProps,
            showTreeDrawer: showTreeDrawer,
            showFormDrawer: showFormDrawer,
            pageTitle: pageTitle,
            mainRecordId: mainRecordId,
            viewStatus: viewStatus,
            globalDialog: globalDialog,
            globalDrawer: globalDrawer,
            i18n: i18n,
            tableConfigProps: _tableConfigProps,
        };
        pageTitle.value = _tableConfigProps.value!.listName || '';
        extMethod.defaultViewRowActionHandler<T${ componentName }>(params);
    };

    const defaultDtCustomButtonActionHandler = (selectedRows: T${ componentName }[], button: IButtonProps) => {
        drawerOnButtonId.value = button.id;
        buttonRef.value = button;
        let params: TDtCustomButtonActionParam<T${ componentName }> = {
            moduleKey: moduleKey,
            moduleId: moduleId.value,
            applyWorkflow: applyWorkflow.value,
            selectedRows: selectedRows,
            button: button,
            formData: formData,
            formConfig: formConfig,
            dialogVisible: showDialog,
            service: service,
            showTreeDrawer: showTreeDrawer,
            showFormDrawer: showFormDrawer,
            tableRef: tableRef as Ref<any>,
            pageTitle: pageTitle,
            viewStatus: viewStatus,
            globalDialog: globalDialog,
            globalDrawer: globalDrawer,
            i18n: i18n,
            tableConfigProps: _tableConfigProps,
        };
        pageTitle.value = _tableConfigProps.value!.listName || '';
        switch (button.id) {
            case 'top-custom-import':
                showImportDialog.value = true;
                break;
            default:
                extMethod.defaultDtCustomButtonActionHandler<T${ componentName }>(params);
                break;
        }
    };

    const defaultFormButtonActionHandler = (button: IButtonProps,popType:string) => {
        buttonRef.value = button;
        let params: TFormButtonActionParam<T${ componentName }> = {
            moduleKey: moduleKey,
            moduleId: moduleId.value,
            applyWorkflow: applyWorkflow.value,
            service: service,
            button: button,
            formData: formData,
            formConfig: formConfig,
            dialogVisible: showDialog,
            tableRef: tableRef,
            showTreeDrawer: showTreeDrawer,
            showFormDrawer: showFormDrawer,
            pageTitle: pageTitle,
            popType: popType,
            bizFormRef: bizFormRef,
            globalDialog: globalDialog,
            globalDrawer: globalDrawer,
            i18n: i18n,
            viewStatus: viewStatus,
        };
        pageTitle.value = _tableConfigProps.value!.listName || '';
        extMethod.defaultFormButtonActionHandler<T${ componentName }>(params);
    };

    const treeDrawerCheckCheck = (params: TTreeCheckDataProps) => {
        params = {
            moduleKey: moduleKey,
            service: service,
            ...params,
        };
        extMethod.treeDrawerCheckCheck(params);
    };

    const navTreeItemClick = (args: any) => {
        let param = (args[0] && Object.keys(args[0]).length > 0 && args[0].searchExpr) || {};
        tableRef.value!.reload({anyWhereCondition: param});
    };

    const onSubmit = (val) => {
        tableRef.value!.updateRows([val]);
    };

    const destroyForm = () => {
        showDialog.value = false;
    };
</script>

<style scoped></style>