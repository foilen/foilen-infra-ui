<#include "/common/header.ftl">

<form class="form-group" method="get" action="/pluginresources/list" autocomplete="off">
  <input type="text" class="form-control" name="s" value="${search!''}" placeholder="<@spring.message 'term.search'/>" />
</form>

<table class="table table-striped">
  <tr>
    <th><@spring.message "resources.type"/></th>
    <th><@spring.message "resources.name"/></th>
    <th><@spring.message "resources.description"/></th>
    <th><@spring.message "term.actions"/></th>
  </tr>
  <#list resourcesTypeAndDetails as resourceTypeAndDetails>
    <tr>
      <td>${resourceTypeAndDetails.type}</td>
      <td>${resourceTypeAndDetails.resource.resourceName}</td>
      <td>${resourceTypeAndDetails.resource.resourceDescription!}</td>
      <td>
        
        <#assign resourceNameArgs = [resourceTypeAndDetails.resource.resourceName]/>
        <form class="confirm form-inline" method="post" action="/pluginresources/delete" data-confirm="<@spring.messageArgs 'prompt.delete.confirm' resourceNameArgs />">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
          <input type="hidden" name="resourceId" value="${resourceTypeAndDetails.resource.internalId?c}" />

          <a class="btn btn-sm btn-primary" href="/pluginresources/edit/${resourceTypeAndDetails.resource.internalId?c}"><@spring.message "button.edit"/></a>
      
          <button class="btn btn-sm btn-danger"><@spring.message 'button.delete'/></button>  

        </form>

      </td>
    </tr>
  </#list>
</table>

<#include "/common/footer.ftl">
