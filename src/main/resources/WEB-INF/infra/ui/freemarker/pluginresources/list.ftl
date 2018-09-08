<#include "/common/header.ftl">

<table class="table table-striped">
  <tr>
    <th><@spring.message "resources.type"/></th>
    <th><@spring.message "resources.name"/></th>
    <th><@spring.message "resources.description"/></th>
    <th><@spring.message "terms.actions"/></th>
  </tr>
  <#list resourcesByType?keys as type>
    <#list resourcesByType[type] as resource>
      <tr>
        <td>${type}</td>
        <td>${resource.resourceName}</td>
        <td>${resource.resourceDescription}</td>
        <td>
          
          <#assign resourceNameArgs = [resource.resourceName]/>
          <form class="confirm form-inline" method="post" action="/pluginresources/delete" data-confirm="<@spring.messageArgs 'prompt.delete.confirm' resourceNameArgs />">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            <input type="hidden" name="resourceId" value="${resource.internalId?c}" />

            <a class="btn btn-sm btn-primary" href="/pluginresources/edit/${resource.internalId}"><@spring.message "button.edit"/></a>
        
            <button class="btn btn-sm btn-danger"><@spring.message 'button.delete'/></button>  
  
          </form>

        </td>
      </tr>
   </#list>
  </#list>
</table>

<#include "/common/footer.ftl">
