/**
* @description ${ tableRemark }列表列插槽渲染
*/
<template #[slotName]="scope">
    <el-tag v-if="slotName === 'state'"
            :type="stateText === '启用' ? 'primary' : stateText === '停用' ? 'warning' : 'info'">
        {{ stateText }}
    </el-tag>
    <#if workflowEnabled ??>
    <el-tag v-else-if="slotName === 'priority'"
            :type="scope.row.priority > 50 ? 'danger' : 'info'">
        {{ urgencyText }}
    </el-tag>
    <el-tag v-else-if="slotName === 'wfState'"
            :type="scope.row.wfState === 1 ? 'primary' : scope.row.wfState === 2 ? 'success' : 'info'">
        {{ wfStateText }}
    </el-tag>
    </#if>
    <span v-else>
        {{ rowData[slotName] }}
    </span>
</template>
<script lang="ts" setup name="PrivateTableColumnSlots">
    import { computed, ref } from 'vue';
    import textMaps from '@/framework/utils/maps';
    const props = defineProps({
        slotName: { type: String, default: '' },
        scope: { type: Object, default: () => { } }
    });
    const rowData = ref(props.scope.row);
    <#if workflowEnabled ??>
    const stateText = computed(() => textMaps.stateTextMap[rowData.value.state] || '--');
    const urgencyText = computed(() => textMaps.urgencyTextMap[rowData.value.priority] || '--');
    </#if>
    const wfStateText = computed(() => textMaps.wfStateTextMap[rowData.value.wfState] || '--');
</script>