/**
* @description ${ tableRemark }类型定义
* @version: types template version 1.0.1
*/
export type ${ typeName } = {
<#list fields as field>
    ${ field.name }: ${ field.type };   // ${ field.remarks }
</#list>
}