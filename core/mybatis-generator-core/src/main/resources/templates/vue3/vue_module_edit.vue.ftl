/**
* @description ${ tableRemark }编辑组件
*/
<template>
    <vgo-form v-if="formConfigReady" v-model="_formData" ref="vgoFormRef" :formConfig="formConfig"
              :viewStatus="viewStatus" @form-submit="onSubmit"></vgo-form>
</template>

<script lang="ts" setup name="${ modelName }Edit">
    import { onMounted, PropType, Ref, ref, unref, watch } from 'vue';
    import { EMPTY_OBJECT } from '@/framework/utils/constant';
    import API from '@/api';
    import tool from '@/framework/utils/tool';
    import { ServiceApi } from '@/api/service';
    import { loadNewInstance } from '@/modules/hooks/useCustomHandle';
    import { T${ modelName } } from '../${ modelPath }/types/T${ modelName }';
    import { TFormConfig } from '@/framework/components/vgoForm/types';

    const props = defineProps({
        modelValue: { type: Object as PropType<T${ modelName }>, default: EMPTY_OBJECT },
        formConfig: { type: Object as PropType<TFormConfig>, default: EMPTY_OBJECT },       //允许父级组件缓存formConfig
        viewStatus: { type: Number as PropType<number>, default: 1 },                       //是否为查看状态
    })

    const emit = defineEmits(['form-submit', 'update:modelValue']);

    const _restBasePath = '${ restBasePath }';

    const vgoFormRef = ref();
    const service = ref<ServiceApi<T${ modelName }>>(new ServiceApi<T${ modelName }>(_restBasePath));
    const _formConfig = ref<TFormConfig>(props.formConfig);
    const _formData = ref<T${ modelName } | null>(props.modelValue);
    const formConfigReady = ref(false);

    watch(() => props.modelValue, async (val) => {
        _formData.value = unref(val) || EMPTY_OBJECT;
        if (Object.keys(_formConfig.value).length === 0) {
            let formKey = _restBasePath.replace(/\//g, '-');
            const formResp = await API.common.formConfig.get(formKey);
            _formConfig.value = formResp.data;
            formConfigReady.value = true;
        } else {
            formConfigReady.value = true;
        }
        if (Object.keys(_formData.value).length === 0) {
            _formData.value = await loadNewInstance<T${ modelName }>(_formConfig, service as Ref<ServiceApi<T${ modelName }>>) as T${ modelName };
        } else {
            _formData.value = tool.perOnload(
                unref(_formData.value),
                _formConfig.value?.formItems,
            );
        }
    }, { immediate: true });

    watch(() => _formData.value, (val) => {
        emit('update:modelValue', val);
    });

    onMounted(() => { });

    const onSubmit = () => {
        emit('form-submit');
    };

    const submit = () => {
        vgoFormRef.value?.submit();
    }

    // 以下可以根据需要添表单中的各种回调与计算方法


    defineExpose({
        submit,
    })

</script>

<style lang="scss" scoped></style>