<template>
    <vgo-table v-if="dataReady" ref="tableRef" tableName="SysTenant" :apiObj="dataFetchMethod" :tableConfigProps="tableConfigProps" row-key="id" stripe remoteSort>
        <template #state="scope">
            <el-tag type="primary"> {{ scope.row.state === 1?'启用':'停用' }} </el-tag>
        </template>
        <template #icon="scope">
            <el-icon size="large">
                <component :is="scope.row.icon"></component>
            </el-icon>
        </template>
    </vgo-table>
</template>

<script lang="ts" setup name="SysTenant">
    import { provide } from 'vue';
    import tableConfig from "@/framework/config/table";
    import { ITableConfigProps } from "@/framework/config/table";
    import { onMounted, reactive, ref } from 'vue';
    import { ServiceApi } from '@/api/service';
    import { useRoute } from 'vue-router';
    import API from '@/api';

    const dataReady = ref(false);
    const tableRef = ref(null) as any;
    const tableColumns = ref([]) as any;
    const tableConfigProps = {} as ITableConfigProps;
    const route = useRoute();
    const meta = route.meta as { viewId: string };

    const dataFetchMethod = ref<Function>(() => {});
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
            tableColumns.value = tableConfigProps.tableColumns;
            console.log('columns', tableConfigProps.tableColumns);
            //删除columns中属性nam为null的列
            // for (let i = 0; i < columns.length; i++) {
            // 	if (columns[i].name === null) {
            // 		columns.splice(i, 1);
            // 		i--;
            // 	}
            // }
            Object.assign(tableConfigProps.tableColumns,tableConfig.transformViewConfig(tableConfigProps.tableColumns));
            console.log('tableColumns', tableColumns.value);
            const service = new ServiceApi(resp.data.restBasePath);
            dataFetchMethod.value = (params: any) => service.list(params);
            dataReady.value = true;
        }
    };

    const defaultDtCustomButtonAction = (buttonId:string, selectedRows:any) => {
        console.log('defaultDtCustomButtonAction', buttonId, selectedRows);
    };

    provide('defaultDtCustomButtonAction', defaultDtCustomButtonAction);

    onMounted(() => {
        loadViewConfig();
    });

    const query = () => {
        tableRef.value.reload();
    };

    const reload = () => {
        tableRef.value.refresh();
    };

    const deleteSelected = () => {
        tableRef.value.deleteSelected();
    };
</script>

<style scoped>

</style>