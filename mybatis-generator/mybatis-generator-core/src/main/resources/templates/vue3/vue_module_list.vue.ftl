<!--
* ${ OVERRIDABLE_FLAG }
* @description ${ tableRemark }列表组件
* @version: list template version 1.0.36
-->
<#-- language=Vue3 -->
<template>
    <VgoTableView :moduleKey="moduleKey" :permissionKey="permissionKey" :tableKey="tableKey">
        <template v-for="column in availableColumns" :key="column.prop"
            #[column.prop]="{ scope, column: col, selectedHref, openSelectedHrefModal }">
            <el-link v-if="selectedHref(col)" type="primary" class="table-cell-link"
             @click="openSelectedHrefModal(col, scope.row)" underline="never">
            <span class="table-cell-link-text">
                <PrivateTableColumnSlots v-memo="[scope.row[col.prop], col.prop]" :scope="scope" :column="col"
                     :slotName="col.prop" />
            </span>
            </el-link>
            <span v-else>
				<PrivateTableColumnSlots v-memo="[scope.row[col.prop], col.prop]" :scope="scope" :column="col"
                   :slotName="col.prop" />
			</span>
        </template>
        <template #module-modal="modalCtx">
            <${ componentName }Modal
                    v-if="modalCtx.showDialog"
                    :modelValue="modalCtx.showDialog"
                    :type="modalCtx.dialogType"
                    :viewStatus="modalCtx.viewStatus"
                    :moduleId="modalCtx.moduleId"
                    :applyWorkflow="modalCtx.applyWorkflow"
                    :formConfig="modalCtx.formConfig"
                    :formData="modalCtx.formData || {}"
                    :service="modalCtx.service"
                    :pageTitle="modalCtx.pageTitle"
                    :popSize="modalCtx.popSize"
                    :popDraggable="modalCtx.popDraggable"
                    :tableRef="modalCtx.tableRef"
                    :isModal="false"
                    @update:modelValue="(val) => modalCtx.updateShowDialog(val)"
                    @vxe-button-click="modalCtx.defaultInnerListButtonActionHandler"
                    @form-submit="modalCtx.onSubmit"
                    @closed="modalCtx.destroyForm" />
        </template>
    </VgoTableView>
</template>

<#-- language=TypeScript -->
<script lang="ts" setup name="${ componentName }">
import { TColumn } from '@vgosoft/framework/components/vgoTable/types';
import VgoTableView from '@vgosoft/framework/components/vgoTable/vgoTableView';
import { useTableConfigStore } from '@vgosoft/framework/store/tableConfig';
import { onMounted, provide, ref } from 'vue';
import { useRoute } from 'vue-router';
import ${ componentName }Modal from '../modals/${ componentName }Modal.vue';
import PrivateTableColumnSlots from '../${ modelPath }/PrivateTableColumnSlots.vue';
import { loadModalsProps } from '../${ modelPath }/sharedVariables';

const route = useRoute();
const meta = route.meta as { viewId: string };

const tableKey = ref<string>(meta.viewId);
const moduleKey = ref<string>("${ modelPath }");
const permissionKey = ref<string>('${ permissionKey }');

const tableConfigStore = useTableConfigStore();
// 获取可用的列配置，用于动态生成插槽
const availableColumns = ref<TColumn[]>([]);

provide('loadModalsProps', loadModalsProps);

onMounted(async () => {
    const config = await tableConfigStore.getTableConfig(tableKey.value);
    availableColumns.value = config?.columns || [];
});
</script>