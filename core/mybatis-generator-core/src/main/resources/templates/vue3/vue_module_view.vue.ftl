<template>
    <el-container>
        <el-aside width="200px" v-if="showTree">
            <el-container>
                <el-container>
                    <el-header>
                        <el-input v-model="treeFilterText" placeholder="输入关键字进行过滤"	clearable></el-input>
                    </el-header>
                    <el-main class="nopadding">
                        <el-tree ref="treeRef" node-key="id" :props="defaultProps" :data="treeData"
                                 :current-node-key="''" :highlight-current="true"
                                 :filter-node-method="treeFilterNode" @node-click="treeItemClick">
                        </el-tree>
                    </el-main>
                </el-container>
            </el-container>
        </el-aside>
        <el-container>
            <el-main class="nopadding">
                <vgo-table v-if="dataReady" ref="tableRef" tableName="${ tableName }" :apiObj="dataFetchMethod"  @defaultViewRowAction="defaultViewRowAction" @defaultDtCustomButtonAction="defaultDtCustomButtonAction"
                           :tableConfigProps="tableConfigProps" row-key="id" stripe remoteSort>
                    <template #state="scope">
                        <el-tag type="primary"> {{ scope.row.state === 1 ? '启用' : '停用' }} </el-tag>
                    </template>
                </vgo-table>
                <vgo-dialog v-model="dialogVisible" :title="pageTitle"  :popSize="popSize"
                            @close="destroyForm"
                            :closeOnClickModal=false
                            :closeOnPressEscape=false>
                    <template #header>
                        <span class="el-dialog__title">{{ pageTitle }}</span>
                    </template>
                    <vgo-form v-if="dialogVisible" ref="vgoFormRef" :formConfig="formConfig" v-model="formData" :loading="loading"
                              @form-submit="onSubmit" :callbacks="callbacks"></vgo-form>
                    <template #footer>
                        <el-button @click="dialogVisible = false">取 消</el-button>
                        <el-button type="primary" @click="submit">保 存</el-button>
                    </template>
                </vgo-dialog>
            </el-main>
        </el-container>
    </el-container>
</template>
<script lang="ts" setup name="${ componentName }">
    import { watch } from 'vue';
    import tableConfig from "@/framework/config/table";
    import { ITableConfigProps } from "@/framework/config/table";
    import { onMounted, reactive, ref } from 'vue';
    import { ServiceApi } from '@/api/service';
    import { useRoute } from 'vue-router';
    import API from '@/api';
    import { ElMessageBox,ElMessage } from 'element-plus';
    import tool from '@/framework/utils/tool';
    import config from '@/framework/config/tableSelect';
    import { isEmpty,isNullOrUnDef } from '@/framework/utils/is';

    const tableRef = ref(null) as any;
    const treeRef = ref() as any;
    const dataReady = ref(false);
    const showTree = ref(false);
    const treeData = ref<any[]>([]);
    const tableColumns = ref([]) as any;
    const tableConfigProps = {} as ITableConfigProps;
    const route = useRoute();
    const meta = route.meta as { viewId: string };
    const pageTitle = ref('');
    const dialogVisible = ref(false);
    const loading = ref(false);
    const formConfig = ref();
    const formData = ref({});
    const vgoFormRef = ref();
    const restBasePath = ref<any>('');
    const service = ref<ServiceApi<any>>();
    const dataFetchMethod = ref<Function>(() => { });
    const treeFilterText = ref("");
    const popSize = ref<String>("default");

    const selectUserSelected = (selected: any) => {
    };

    const jobTitleSelected = (selected: any) => {
    };
    const switchChange = (value: any) => {
    };

    //所有回调函数
    const callbacks = {
        selectUserSelected: selectUserSelected,
        jobTitleSelected: jobTitleSelected,
        switchChange: switchChange,
    };

    const defaultProps = reactive({
        label: config.props.label,
        value: config.props.value,
        page: config.request.page,
        pageSize: config.request.pageSize,
    });

    const submit = () => {
        vgoFormRef.value.submit();
    };

    const onSubmit = () => {
        tableRef.value.refresh();
        dialogVisible.value = false;
    };

    const destroyForm = () => {
        dialogVisible.value = false;
    };

    const loadViewConfig = async () => {
        const resp = await API.common.viewConfig.post(meta.viewId);
        if (resp.status === 0) {
            Object.assign(tableConfigProps, {
                indexColumns: resp.data.indexColumns,
                actionsColumns: resp.data.actionsColumns,
                toolbarActions: resp.data.toolbarActions,
                columnActions: resp.data.columnActions,
                tableColumns: reactive<any[]>(resp.data.columns)
            });
            pageTitle.value = resp.data.listName;
            tableColumns.value = tableConfigProps.tableColumns;
            Object.assign(tableConfigProps.tableColumns, tableConfig.transformViewConfig(tableConfigProps.tableColumns));
            restBasePath.value = resp.data.restBasePath;
            service.value = new ServiceApi(restBasePath.value);
            dataFetchMethod.value = (params: any) => service.value!.list(params);
            dataReady.value = true;
            //form配置
            let formKey = restBasePath.value.replace(/\//g, '-');
            const formResp = await API.common.formConfig.get(formKey);
            formConfig.value = formResp.data;
            popSize.value = formResp.data.popSize;
            //树数据
            if(resp.data.categoryTreeUrl && !isEmpty(resp.data.categoryTreeUrl) && !isNullOrUnDef(resp.data.categoryTreeUrl)){
                showTree.value = true;
                const treeResp = await service.value.getAny(resp.data.categoryTreeUrl);
                if (treeResp.status === 0) {
                    treeData.value = tool.listToTree(treeResp.data);
                }
            }
        }
    };

    const loadNewInstance = async () => {
        const resp = await service.value!.get('new-instance');
        if (resp.status === 0) {
            const newInstance = tool.perOnload(resp.data, formConfig.value.formItems);
            formData.value = newInstance;
        }
    };

    const defaultDtCustomButtonAction = (selectedRows: any,buttonId: string) => {
        if (buttonId === 'top-custom-create') {
            loadNewInstance();
            dialogVisible.value = true;
        } else if (buttonId === 'top-custom-edit') {
            if (selectedRows.length !== 1) {
                ElMessageBox.alert('请选择一条数据进行编辑', '提示', {
                    type: 'warning'
                });
                return;
            }
            formData.value = tool.perOnload(selectedRows[0], formConfig.value.formItems);
            dialogVisible.value = true;
        } else if(buttonId == 'top-custom-remove'){
            if (selectedRows.length === 0) {
                ElMessage.warning('请选择数据进行删除');
                return;
            }
            ElMessageBox.confirm('确定删除选中的数据吗?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                const ids = selectedRows.map((row: any) => row.id);
                service.value!.delete('',ids).then(() => {
                    tableRef.value.refresh();
                });
            }).catch(() => {
            });
        } else if(buttonId == 'top-custom-export'){
            if(selectedRows.length === 0){
                ElMessage.warning('请选择数据进行导出');
                return;
            }
            const ids = selectedRows.map((row: any) => row.id);
            service.value!.exportSelect(ids).then((resp: any) => {
                const blob = new Blob([resp], { type: 'application/vnd.ms-excel' });
                const url = URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = url;
                link.download = pageTitle.value+'.xlsx';
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            });
        }
    };

    const defaultViewRowAction = (rowData:any,btnId:string) => {
        if (btnId === 'view-row-edit') {
            formData.value = tool.perOnload(rowData, formConfig.value.formItems);
            dialogVisible.value = true;
        }else if (btnId === 'view-row-remove') {
            ElMessageBox.confirm('确定删除该条数据吗?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                service.value!.delete(rowData.id).then(() => {
                    tableRef.value.refresh();
                });
            }).catch(() => {
            });
        }else if (btnId === 'view-row-view') {
            console.log('查看：'+rowData.id);
        }else if (btnId === 'row-table-drop-data') {
            ElMessageBox.confirm('确定清空模块的数据吗?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                tableRef.value.refresh();
            }).catch(() => {
            });
        }
    };

    onMounted(() => {
        loadViewConfig();
    });

    watch(() => dialogVisible.value, (val) => {
        if (!val) {
            vgoFormRef.value = null;
        }
    });

    const treeFilterNode = (value: string, data: any) => {
        if (!value) return true;
        return data.name.indexOf(value) !== -1;
    };

    watch(treeFilterText, (val) => {
        treeRef.value!.filter(val);
    });

    const treeItemClick = (data: any) => {
        const params = {
            anyWhereCondition: data.searchExpr
        };
        tableRef.value.reload(params);
    };

</script>

<style scoped>
    * {
        --el-header-height: 50px;
    }

    .el-tree :deep(.el-tree-node__content) {
        position: relative;
    }

    .el-tree :deep(.el-tree-node:last-child > .el-tree-node__content::before) {
        content: "";
        width: 1px;
        border: 0;
        border-left: 1px dashed #aaa;
        height: 10000px;
        position: absolute;
        margin-left: -9px;
        bottom: 18px;
    }

    .el-tree :deep(.el-tree-node__children .el-tree-node__content::after) {
        content: "";
        width: 10px;
        height: 1px;
        border: 0;
        border-top: 1px dashed #aaa;
        position: absolute;
        margin-left: -9px;
    }

    .el-tree :deep(.el-tree-node__content > .el-tree-node__expand-icon) {
        padding: 6px 3px;
    }
</style>