<#include "/common/header.ftl">

<h1><@spring.message "resource.edit"/></h1>

<p id="topError" class="text-danger"></p>
<div class="fullMask"></div>
<div class="pull-right">
  <select id="editorName">
    <#list editorNames as item>
      <option value="${item}" ${(editorName == item)?then('selected="selected"','')}>${item}</option>
    </#list>
  </select>
</div>
<form id="mainResource" data-resource-id="${resourceId}" data-editor-name="${editorName}"><@spring.message "term.loading"/></form>

<hr/>

<#assign resourceNameArgs = [resourceName]/>
<form class="confirm" method="post" action="/pluginresources/delete" data-confirm="<@spring.messageArgs 'prompt.delete.confirm' resourceNameArgs/>">
  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
  <input type="hidden" name="resourceId" value="${resourceId}" />

  <button class="btn btn-danger pull-right"><@spring.message 'button.delete'/></button>  
</form>

<button class="pull-right btn btn-success resourceUpdate"><@spring.message "button.edit"/></button>

<#include "/common/footer.ftl">
