/**
* @description ${ tableRemark }列表列插槽渲染
*/
<template #[slotName]="scope">
    <el-tag v-if="slotName === 'state'"
            :type="stateText === '启用' ? 'primary' : stateText === '停用' ? 'warning' : 'info'">
        {{ stateText }}
    </el-tag>
    <span v-else>
    {{ rowData[slotName] }}
  </span>
</template>
<script lang="ts" setup name="PrivateTableColumnSlots">
    import { computed, ref } from 'vue';

    const props = defineProps({
        slotName: { type: String, default: '' },
        scope: { type: Object, default: () => { } }
    });

    const rowData = ref(props.scope.row);
    const stateText = computed(() => {
        return rowData.value.state === 1 || rowData.value.state === true ? '启用' : rowData.value.state === 0 || rowData.value.state === false ? '停用' : '--';
    });
</script>