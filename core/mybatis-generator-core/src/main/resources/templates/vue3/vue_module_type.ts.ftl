export interface ${ typeName } {
<#list fields as field>
    ${ field.name }: ${ field.type };
</#list>
}