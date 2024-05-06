/**
* @description ${ tableRemark }列表组件
*/
<template>
    <el-container>
        <el-aside width="220px" v-if="showTreePanel">
            <TreePanel :treeDataUrl="categoryTreeUrl" :vgoTreeProps="{ showCheckbox: false, }"
                       @node-click="navTreeItemClick"></TreePanel>
        </el-aside>
        <el-container>
            <el-main class="nopadding">
                <vgo-table v-if="tableConfigReady" ref="tableRef" tableName="${ tableName }"
                           :apiObj="tableApiObj.fetchTableData" @view-row-button-click="defaultViewRowActionHandler"
                           @view-toolbar-button-click="defaultDtCustomButtonActionHandler" :tableConfigProps="tableConfigProps"
                           row-key="id" stripe remoteSort>
                    <!-- colDefsState -->
                    <template #state="scope">
                        <el-tag :type="scope.row.state == 1 ? 'primary' : scope.row.state == 0 ? 'warning' : 'info'"> {{
                            scope.row.state === 1 || scope.row.state === true ? '启用' : scope.row.state === 0 ||
                            scope.row.state === false ? '停用' : '--' }} </el-tag>
                    </template>
                </vgo-table>
                <VgoDialog v-model="showDialog" :title="pageTitle" :popSize="popSize" @close="destroyForm"
                           :closeOnClickModal=false :closeOnPressEscape=false>
                    <template #header>
                        <span class="el-dialog__title">{{ pageTitle }}</span>
                    </template>
                    <${ componentName }Edit v-if="showDialog && viewStatus === 1" ref="vgoFormRef" v-model="formData"
                                        :formConfig="formConfig" :viewStatus="viewStatus" @form-submit="onSubmit"></${ componentName }Edit>
                    <${ componentName }Detail v-if="showDialog && viewStatus === 0" ref="vgoFormRef" v-model="formData"
                                        :formConfig="formConfig" :viewStatus="viewStatus"></${ componentName }Detail>
                    <template #footer>
                        <el-button @click="showDialog = false">取 消</el-button>
                        <el-button type="primary" @click="submit">保 存</el-button>
                    </template>
                </VgoDialog>
                <VgoTreeDrawer v-if="showTreeDrawer" v-model="showTreeDrawer" v-model:treeSelected="treeSelected"
                               :vgoTreeProps="{ checkStrictly: checkStrictly }" :apiObj="drawerTreeApiObj"
                               :mainRecordId="mainRecordId" :drawerOnButtonId="drawerOnButtonId" @check="treeDrawerCheckCheck">
                </VgoTreeDrawer>
                <VgoFormDrawer v-if="showFormDrawer" v-model="showFormDrawer" :formData="formData"
                               :formConfig="formConfig" :viewStatus="viewStatus" @form-submit="onSubmit"></VgoFormDrawer>
            </el-main>
        </el-container>
    </el-container>

</template>
<script lang="ts" setup name="${ componentName }">

    import { Ref, watch, onMounted, reactive, ref } from 'vue';
    import { useRoute } from 'vue-router';
    import tableConfig from "@/framework/config/table";
    import { TTableConfigProps, TTableApiObj } from "@/framework/components/vgoTable/types";
    import API from '@/api';
    import { ServiceApi } from '@/api/service';
    import { isEmpty, isNullOrUnDef } from '@/framework/utils/is';
    import * as extMethod from '@/modules/hooks/useCustomHandle';
    import TreePanel from '@/framework/application/components/TreePanel.vue';
    import { TTreeApiObj } from '@/framework/application/types';
    import { TFormConfig } from '@/framework/components/vgoForm/types';
    import { T${ componentName } } from '../${ modelPath }/types/T${ componentName }';
    import VgoTreeDrawer from '@/framework/components/vgoDrawer/VgoTreeDrawer.vue';
    import VgoFormDrawer from '@/framework/components/vgoDrawer/VgoFormDrawer.vue';
    import ${ componentName }Edit from '../${ modelPath }/${ componentName }Edit.vue';
    import ${ componentName }Detail from '../${ modelPath }/${ componentName }Detail.vue';

    const route = useRoute();
    const meta = route.meta as { viewId: string };
    const tableRef = ref() as any;
    const tableConfigReady = ref(false);
    const showTreePanel = ref(false);
    const tableColumns = ref([]) as any;
    const tableConfigProps = {} as TTableConfigProps;
    const pageTitle = ref('');
    const showDialog = ref(false);
    const formConfig = ref();
    const formData = ref<T${ componentName }>({});
    const popSize = ref<String>('default');
    const vgoFormRef = ref();
    const restBasePath = ref<any>('');
    const showTreeDrawer = ref<boolean>(false);
    const showFormDrawer = ref<boolean>(false);
    const drawerTreeApiObj = ref<TTreeApiObj>();
    const treeSelected = ref<String[]>([]);
    const checkStrictly = ref(false);
    const drawerOnButtonId = ref('');
    const mainRecordId = ref('');
    const categoryTreeUrl = ref('');
    const service = ref<ServiceApi<any>>();
    const tableApiObj = ref<TTableApiObj>({
        fetchTableConfig: async (...params: any) => API.common.viewConfig.post(params),
        fetchTableData: async (params: any) => service.value!.listPost(params),
        fetchFormConfig: async (...params: any) => API.common.formConfig.get(params),
    });
    const viewStatus = ref<number>(1);

    onMounted(() => {
        loadViewConfig();
    });

    watch(() => showDialog.value, (val) => {
        if (!val) {
            vgoFormRef.value = null;
        }
    });

    const loadViewConfig = async () => {
        const resp = await tableApiObj.value.fetchTableConfig(meta.viewId);
        if (resp.status === 0) {
            Object.assign(tableConfigProps, {
                indexColumns: resp.data.indexColumns,
                actionsColumns: resp.data.actionsColumns,
                toolbarActions: resp.data.toolbarActions,
                columnActions: resp.data.columnActions,
                tableColumns: reactive<any[]>(resp.data.columns),
                queryColumns: resp.data.queryColumns,
                fuzzyColumns: resp.data.fuzzyColumns,
            });
            pageTitle.value = resp.data.listName;
            tableColumns.value = tableConfigProps.tableColumns;
            Object.assign(tableConfigProps.tableColumns, tableConfig.transformViewConfig(tableConfigProps.tableColumns));
            restBasePath.value = resp.data.restBasePath;
            service.value = new ServiceApi(restBasePath.value);
            tableConfigReady.value = true;

            //树数据请求对象
            if (resp.data.categoryTreeUrl && !isEmpty(resp.data.categoryTreeUrl) && !isNullOrUnDef(resp.data.categoryTreeUrl)) {
                categoryTreeUrl.value = resp.data.categoryTreeUrl;
                showTreePanel.value = true;
            }

            //form配置
            let formKey = restBasePath.value.replace(/\//g, '-');
            const formResp = await tableApiObj.value.fetchFormConfig(formKey);
            formConfig.value = formResp.data;
            popSize.value = formResp.data.popSize;
        }
    };

    const defaultViewRowActionHandler = (rowData: any, buttonId: string) => {
        drawerOnButtonId.value = buttonId;
        let params = {
            rowData: rowData as T${ componentName },
            buttonId: buttonId as String,
            formData: formData as T${ componentName },
            formConfig: formConfig as TFormConfig,
            dialogVisible: showDialog as Ref<Boolean>,
            service: service as Ref<ServiceApi<T${ componentName }>>,
            tableRef: tableRef as Ref<any>,
            drawerTreeApiObj: drawerTreeApiObj as Ref<TTreeApiObj>,
            checkStrictly: checkStrictly as Ref<Boolean>,
            showTreeDrawer: showTreeDrawer as Ref<Boolean>,
            showFormDrawer: showFormDrawer as Ref<Boolean>,
            mainRecordId: mainRecordId as Ref<String>,
            viewStatus: viewStatus as Ref<number>,
        };
        extMethod.defaultViewRowActionHandler(params);
    };

    const defaultDtCustomButtonActionHandler = (selectedRows: any, buttonId: string) => {
        drawerOnButtonId.value = buttonId;
        let params = {
            selectedRows: selectedRows as T${ componentName }[],
            buttonId: buttonId as String,
            dialogVisible: showDialog as Ref<Boolean>,
            formData: formData as T${ componentName },
            formConfig: formConfig as TFormConfig,
            service: service as Ref<ServiceApi<T${ componentName }>>,
            tableRef: tableRef as Ref<any>,
            pageTitle: pageTitle as Ref<String>,
            showTreeDrawer: showTreeDrawer as Ref<Boolean>,
            showFormDrawer: showFormDrawer as Ref<Boolean>,
            viewStatus: viewStatus as Ref<number>,
        };
        extMethod.defaultDtCustomButtonActionHandler(params);
    };

    const treeDrawerCheckCheck = (params: any, btnId: String, recordId: String) => {
        extMethod.treeDrawerCheckCheck(
            params,
            btnId,
            recordId,
            service as Ref<ServiceApi<any>>,
        )
    };

    const navTreeItemClick = (...args: any) => {
        const params = {
            anyWhereCondition: args[0].searchExpr
        };
        tableRef.value.reload(params);
    };

    const submit = () => {
        vgoFormRef.value.submit();
    };

    const onSubmit = () => {
        tableRef.value.refresh();
        //dialogVisible.value = false;
    };

    const destroyForm = () => {
        showDialog.value = false;
    };
</script>

<style scoped></style>