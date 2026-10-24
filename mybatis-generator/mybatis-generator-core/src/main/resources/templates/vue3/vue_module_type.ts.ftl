/**
* ${ OVERRIDABLE_FLAG }
* @description ${ tableRemark }类型定义
* @version: types template version 1.0.2
*/
export type ${ typeName } = {
<#list fields as field>
    ${ field.name }<#if !field.required>?</#if>: ${ field.type };   // <#if field.remarks??>${ field.remarks }<#else>-</#if>
</#list>
}

<#list tsTypeDescriptors as descriptor>
export ${ descriptor.typeKind } ${ descriptor.name } <#if descriptor.extendsName??> extends ${ descriptor.extendsName } </#if>{
  <#if descriptor.hasProperties()>
    <#list descriptor.properties as field>
    ${ field.name }<#if !field.required>?</#if>: ${ field.type };   // <#if field.remarks??>${ field.remarks }<#else>-</#if>
    </#list>
  </#if>
}
</#list>
<#if sensitiveFields?? && sensitiveFields?size gt 0>

/** 敏感字段显示映射；检测规则以服务端实体注解为准。 */
export const ${typeName}SensitiveFields = [
<#list sensitiveFields as field>
    { table: "${field.table?js_string}", entity: "${field.entity?js_string}", property: "${field.property?js_string}", label: "${field.label?js_string}", contentType: "${field.contentType?js_string}" },
</#list>
] as const;
</#if>
