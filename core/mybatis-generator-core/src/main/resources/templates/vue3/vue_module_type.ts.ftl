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
