<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <title>gen_demo_table_inner_list_fragments</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<body>
<th:block th:fragment="inner_list_fragments">
    <!-- 操作模板 -->
    <script type="text/html" id="TPL-inner-list-header-toolbar">
        <div class="layui-btn-container">
            <button type="button" class="layui-btn layui-btn-sm layui-btn-primary" lay-event="add">
                <i class="layui-icon layui-icon-addition"></i></button>
            <button type="button" class="layui-btn layui-btn-sm layui-btn-primary" lay-event="delete">
                <i class="layui-icon layui-icon-delete"></i></button>
        </div>
    </script>
    <!--日期编辑模板-->
    <#list date as item>
        <script type="text/html" id="TPL-inner-${item.fieldName}">
            <input class="layui-input table-inner-input"
                   data-field="${item.fieldName}"
                   lay-date="${item.fieldType}"
                   id="inner-${item.fieldName}{{d.LAY_NUM}}"
                   value="{{=d.${item.fieldName}||''}}"
                   readonly="readonly"/>
        </script>
    </#list>
    <!--下拉选择模板-->
    <#list dropdownlist as item>
        <script type="text/html" id="TPL-inner-${item.fieldName}">
            <select class="table-inner-select"
                    <#if item.dataUrl?? && item.dataUrl!="" >data-url="${item.dataUrl}"</#if>
                    data-field="${item.fieldName}"
                    data-other-field="${item.otherFieldName}"
                    data-value="{{=d.${item.fieldName}||''}}"
                    id="inner-${item.fieldName}{{d.LAY_NUM}}"
                    data-cache-key="inner-${item.fieldName}"
                    lay-ignore>
                <option value="">
                    请选择
                </option>
            </select>
        </script>
    </#list>
    <!--开关模板-->
    <#list switch as item>
        <script type="text/html" id="TPL-inner-${item.fieldName}">
            <input class="table-inner-switch"
                   data-field="${item.fieldName}"
                   id="inner-${item.fieldName}{{d.LAY_NUM}}"
                   title=<#if item.switchText?? && item.switchText!="" >"${item.switchText}"<#else>"启用|禁用"</#if>
            {{=d.${item.fieldName}==1?"checked":""}}
            type="checkbox"
            lay-skin="switch"
            value="{{=d.${item.fieldName}}}"/>
        </script>
    </#list>
    <!--弹窗选择模板-->
    <#list select as item>
        <script type="text/html" id="TPL-inner-${item.fieldName}">
            <input class="layui-input table-inner-input"
                   data-field="${item.thisFieldName}"
                   data-other-field="${item.otherFieldName}"
                   <#if item.dataUrl?? && item.dataUrl!="" >data-url="${item.dataUrl}"</#if>
                    <#if item.title?? && item.title!="" >data-title="${item.title}"</#if>
                   id="inner-${item.fieldName}{{d.LAY_NUM}}"
                   lay-event="${item.dataType}"
                   readonly="readonly" type="text"
                   value="{{=d.${item.fieldName}||''}}"/>
        </script>
    </#list>
    <!--radio模板-->
    <#list radio as item>
        <script type="text/html" id="TPL-inner-${item.fieldName}">
            <div class="table-inner-radio"
                 data-field="${item.fieldName}"
                 data-other-field="${item.otherFieldName}"
                 data-radio-group="${item.fieldName}-Gp{{d.LAY_NUM}}"
                 <#if item.dataUrl?? && item.dataUrl!="" >data-url="${item.dataUrl}"</#if>
                 data-value="{{=d.${item.fieldName}||''}}"
                 id="inner-${item.fieldName}{{d.LAY_NUM}}">
            </div>
        </script>
    </#list>
    <!--checkbox模板-->
    <#list checkbox as item>
        <script type="text/html" id="TPL-inner-${item.fieldName}">
            <div class="table-inner-checkbox"
                 data-field="${item.fieldName}"
                 data-other-field="${item.otherFieldName}"
                 <#if item.dataUrl?? && item.dataUrl!="" >data-url="${item.dataUrl}"</#if>
                 data-value="{{=d.${item.fieldName}||''}}"
                 id="inner-${item.fieldName}{{d.LAY_NUM}}">
            </div>
        </script>
    </#list>
</th:block>
</body>
</html>
