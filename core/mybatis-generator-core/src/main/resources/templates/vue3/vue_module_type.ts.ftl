/**
* @description ${ tableRemark }类型定义
*/
export type ${ typeName } = {
<#list fields as field>
    ${ field.name }: ${ field.type };   // ${ field.remarks }
</#list>
}