/**
* @description ${ tableRemark }列表组件
* @version: list template version 1.0.16
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
                    <template v-for="(column,index) in _tableConfigProps!.tableColumns" :key="`column_`+index" #[column.prop]="scope">
                        <span>
							<el-link
                                    v-if="selectedHref(column)"
                                    type="primary"
                                    class="table-cell-link"
                                    @click="openSelectedHrefModal(column, scope.row)"
                                    :underline="false"
                            >
								<span class="table-cell-link-text">
									<PrivateTableColumnSlots v-memo="[scope.row[column.prop], column.prop]"
                                                             :scope="scope" :column="column" :slotName="column.prop"></PrivateTableColumnSlots>
								</span>
							</el-link>
							<span v-else>
								<PrivateTableColumnSlots v-memo="[scope.row[column.prop], column.prop]"
                                                         :scope="scope" :column="column" :slotName="column.prop"></PrivateTableColumnSlots>
							</span>
						</span>
                    </template>
                </vgo-table>
                <LoadModals v-if="loadModalsProps.dataLoaded"
                            v-model="loadModalsProps.modelValue"
                            :type="loadModalsProps.type"
                            :viewStatus="loadModalsProps.viewStatus"
                            :popSize="loadModalsProps.popSize"
                            :formData="loadModalsProps.formData"
                            :elDialogProps="loadModalsProps.elDialogProps"
                            :elDrawerProps="loadModalsProps.elDrawerProps"
                            :loadModalProvideData="loadModalsProps.loadModalProvideData"
                            @vxe-button-click="defaultInnerListButtonActionHandler"
                            @close="destroyComponent">
                </LoadModals>
                <${ componentName }Modal v-if="showDialog" v-model="showDialog"
                                :type="dialogType"
                                :viewStatus="viewStatus"
                                :moduleId="moduleId"
                                :applyWorkflow="applyWorkflow"
                                :formConfig="formConfig"
                                :formData="formData"
                                :service="service"
                                :pageTitle="pageTitle"
                                :popSize="popSize"
                                :popDraggable="popDraggable"
                                :closeOnClickModal="false"
                                :closeOnPressEscape="false"
                                :tableRef="tableRef"
                                :isModeal="false"
                                @vxe-button-click="defaultInnerListButtonActionHandler"
                                @form-submit="onSubmit"
                                @close="destroyForm">
                </${ componentName }Modal>
                <VgoTreeDrawer v-if="showTreeDrawer" v-model="showTreeDrawer" v-model:treeSelected="treeSelected" :title="pageTitle"
                               :apiObj="drawerTreeApiObj" :treePanelProps="drawerTreePanelProps"
                               :mainRecordId="mainRecordId" :drawerOnButtonId="drawerOnButtonId" @check="treeDrawerCheckCheck">
                </VgoTreeDrawer>
                <vgoFileImport v-model="showImportDialog" :service="service" @on-success="importSuccess"></vgoFileImport>
            </el-main>
        </el-container>
    </el-container>

</template>
<script lang="ts" setup name="${ componentName }">

    import { Ref, watch, onMounted, ref, inject, provide, nextTick } from 'vue';
    import { useRoute } from 'vue-router';
    import { TTableConfigProps, TTableApiObj, TVgoTableInstance, ICustomColumnProps } from "@/framework/components/vgoTable/types";
    import API from '@/api';
    import { ServiceApi } from '@/api/service';
    import { isEmpty, isNullOrUnDef } from '@/framework/utils/is';
    import * as extMethod from '@/hooks/useCustomHandle';
    import TreePanel from '@/framework/application/components/TreePanel.vue';
    import { TTreeApiObj, TTreePanelButtons, TTreePanelProps} from '@/framework/application/types';
    import { TFormConfig } from '@/framework/components/vgoForm/types';
    import { T${ componentName } } from '../${ modelPath }/types/T${ componentName }';
    import VgoTreeDrawer from '@/framework/components/vgoDrawer/VgoTreeDrawer.vue';
    import PrivateTableColumnSlots from '../${ modelPath }/PrivateTableColumnSlots.vue';
    import { useTableConfigStore } from '@/store/tableConfig';
    import { useFormConfigStore } from '@/store/formConfig';
    import { TDtCustomButtonActionParam, TViewRowActionParam, } from '@/hooks/types';
    import { TTreeCheckDataProps, TVgoTreeProps } from '@/framework/components/vgoTree/types';
    import { TApi } from '@/api/types';
    import { useI18n } from 'vue-i18n';
    import { TGlobalDialog } from '@/framework/layout/components/GlobalDialog.vue';
    import { TGlobalDrawer } from '@/framework/layout/components/GlobalDrawer.vue';
    import { IButtonProps } from '@/framework/types/core';
    import vgoFileImport from '@/framework/components/vgoFileImport/index.vue';
    import ${ componentName }Modal from '../modals/${ componentName }Modal.vue';
    import LoadModals from '@/modules/components/loadModals/index.vue';
    import { loadModalsProps } from '../${ modelPath }/sharedVariables';
    import { TVxeTableActionsParams } from '@/framework/components/VgoVxeTable/types';

    const i18n = useI18n();

    const moduleKey = "${ modelPath }";
    const permissionKey = '${ permissionKey }';

    const tableConfigStore = useTableConfigStore();
    const formConfigStore = useFormConfigStore();

    const showImportDialog = ref(false);

    const route = useRoute();
    const meta = route.meta as { viewId: string };
    const tableRef = ref<TVgoTableInstance>();
    const tableConfigReady = ref<boolean>(false);
    const showTreePanel = ref(false);
    const tableColumns = ref([]) as any;
    const _tableConfigProps = ref<TTableConfigProps | null>(null);
    const pageTitle = ref('');
    const showDialog = ref(false);
    const dialogType = ref<'dialog' | 'drawer'>('dialog');
    const formConfig = ref<TFormConfig>();
    const formData = ref<T${ componentName }|undefined>();
    const popSize = ref<string>('default');
    const popDraggable = ref<boolean>(false);
    const bizFormRef = ref();
    const restBasePath = ref<any>('');
    const showTreeDrawer = ref<boolean>(false);
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
    provide('tableRef', tableRef);
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
                disabled: () => {
                    // 入参可以是data,node,node.data
                    return false;
                },
            },
        } as TVgoTreeProps,
        treePanelWidth: '400px',
    });
    const viewStatus = ref<number>(1);
    const applyWorkflow = ref<number>(0);
    const moduleId = ref<string>('');
    onMounted(() => {
        loadViewConfig();
    });

    const selectedHref = (column: ICustomColumnProps) => {
        if (!column) return false;
        if (column.renderFunction && column.renderFunction.includes('colDefsAsLink')) {
            return true;
        }
        return false;
    }
    const openSelectedHrefModal = async (column: ICustomColumnProps, rowData: any) => {
        loadModalsProps.formData = {};
        loadModalsProps.dataLoaded = false;
        loadModalsProps.modelValue = false;
        // 检查是否可以打开抽屉
        if (selectedHref(column)) {
            // 更新数据
            Object.assign(loadModalsProps.formData, rowData);
            nextTick(() => {
                loadModalsProps.dataLoaded = true;
                loadModalsProps.modelValue = true;
            });
        } else {
            // 如果不能打开，输出调试信息
            console.warn('无法打开抽屉，column 不符合条件', column);
        }
    }

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

    const loadViewConfig = async () => {
        const tableKey = meta.viewId;
        _tableConfigProps.value = tableConfigStore.hasTableConfig(tableKey) ? tableConfigStore.getTableConfig(tableKey) : await tableConfigStore.fetchTableConfigAsync(tableKey);
        _tableConfigProps.value!.moduleKey = moduleKey;
        _tableConfigProps.value!.permissionId = permissionKey;
        pageTitle.value = _tableConfigProps.value!.listName || '';
        tableColumns.value = _tableConfigProps.value!.tableColumns;
        restBasePath.value = _tableConfigProps.value!.restBasePath;
        applyWorkflow.value = _tableConfigProps.value!.applyWorkflow ?? 0;
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
        popDraggable.value = formConfig.value!.popDraggable ?? false;
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
            dialogType: dialogType,
            service: service,
            tableRef: tableRef,
            drawerTreeApiObj: drawerTreeApiObj,
            drawerTreePanelProps: drawerTreePanelProps,
            showTreeDrawer: showTreeDrawer,
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
            dialogType: dialogType,
            service: service,
            showTreeDrawer: showTreeDrawer,
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

    const defaultInnerListButtonActionHandler = (params: TVxeTableActionsParams) => {
        params = {
            moduleKey: moduleKey,
            ...params,
        };
        extMethod.defaultInnerListButtonActionHandler(params);
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
    const destroyComponent = () => {
        loadModalsProps.modelValue = false;
        loadModalsProps.formData = {};
        loadModalsProps.dataLoaded = false;
    };
</script>

<style lang="scss" scoped>
    :deep(.table-cell-link) {
        display: contents;
        width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
    :deep(.table-cell-link-text) {
        display: block;
        width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
    :deep(.el-link__inner) {
        display: flex;
    }
</style>