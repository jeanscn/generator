<!--
* @description ${ tableRemark }列表列插槽渲染
* @version: slots template version 1.0.4
-->
<template #[slotName]="scope">
    <el-tag v-if="slotName === 'state'"
            :type="stateText === '启用' ? 'primary' : stateText === '停用' ? 'warning' : 'info'">
        {{ stateText }}
    </el-tag>
    <#if workflowEnabled >
    <el-tag v-else-if="slotName === 'priority'"
            :type="scope.row.priority > 50 ? 'danger' : 'info'">
        {{ urgencyText }}
    </el-tag>
    <el-tag v-else-if="slotName === 'wfState'"
            :type="scope.row.wfState === 1 ? 'primary' : scope.row.wfState === 2 ? 'success' : 'info'">
        {{ wfStateText }}
    </el-tag>
    </#if>
    <span v-else>{{ scope.row[slotName] }}</span>
</template>
<script lang="ts" setup name="PrivateTableColumnSlots">
    import { computed, PropType, toRef } from 'vue'
    import textMaps from '@/framework/utils/maps'
    import { EMPTY_OBJECT } from '@/framework/utils/constant'
    import { TColumn } from '@/framework/components/vgoTable/types'

    const props = defineProps({
        slotName: { type: String as PropType<string>, default: '' },
        scope: { type: Object, default: EMPTY_OBJECT },
        column: { type: Object as PropType<TColumn>, default: EMPTY_OBJECT },
    })

    // 使用 toRef 确保 rowData 能响应 props.scope.row 的变化
    const rowData = toRef(() => props.scope.row)
    const stateText = computed(() => textMaps.stateTextMap[rowData.value.state] || '--')
    <#if workflowEnabled >
    const urgencyText = computed(() => textMaps.urgencyTextMap[rowData.value.priority] || '--')
    const wfStateText = computed(() => textMaps.wfStateTextMap[rowData.value.wfState] || '--')
    </#if>
</script>