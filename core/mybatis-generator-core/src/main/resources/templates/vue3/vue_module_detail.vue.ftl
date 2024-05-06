/**
* @description ${ tableRemark }显示详情
*/
<template>

</template>

<script lang="ts" setup name="${ modelName }Detail">
    import { onMounted, watch, PropType } from 'vue';
    import { EMPTY_OBJECT } from '@/framework/utils/constant';
    import { TFormConfig } from '@/framework/components/vgoForm/types';
    import { T${ modelName } } from '../${ modelPath }/types/T${ modelName }';

    const props = defineProps({
        formData: { type: Object as PropType<T${ modelName }>, default: EMPTY_OBJECT },
        formConfig: { type: Object as PropType<TFormConfig>, default: EMPTY_OBJECT },       //允许父级组件缓存formConfig
        viewStatus: { type: Number as PropType<number>, default: 1 },                       //是否为查看状态
    })

    watch(() => { }, (val) => { })

    onMounted(() => { })

</script>

<style lang="scss" scoped></style>