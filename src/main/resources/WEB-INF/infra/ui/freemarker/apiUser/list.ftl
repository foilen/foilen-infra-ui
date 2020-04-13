<#include "/common/header.ftl">

<div class="pull-right">
    <form class="form-inline" method="post" action="create">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
      <button class="btn btn-success"><@spring.message 'button.create'/></button>
    </form>
</div>

<#if createdUser??>
  <p class="bg-success"><@spring.message 'userApi.created'/>${createdUser.a} / ${createdUser.b}</p>
</#if>

<table class="table table-striped">
  <tr>
    <th><@spring.message 'term.userId'/></th>
    <th><@spring.message 'term.description'/></th>
    <th><@spring.message 'term.isAdmin'/></th>
    <th><@spring.message 'term.createdOn'/></th>
    <th><@spring.message 'term.expireOn'/></th>
    <th><@spring.message "term.actions"/></th>
  </tr>
  <#list apiUsers as apiUser>
	  <tr>
	    <td>${apiUser.userId}</a></td>
	    <td>${apiUser.description}</a></td>
	    <td>${apiUser.admin?c}</a></td>
	    <td>${apiUser.createdOnText!''}</a></td>
	    <td>${apiUser.expireOnText!''}</a></td>
      <td>
        
        <#assign userIdArgs = [apiUser.userId]/>
        <form class="confirm form-inline" method="post" action="delete" data-confirm="<@spring.messageArgs 'prompt.delete.confirm' userIdArgs />">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
          <input type="hidden" name="userId" value="${apiUser.userId}" />

          <button class="btn btn-sm btn-danger"><@spring.message 'button.delete'/></button>  

        </form>

      </td>
	  </tr>
  </#list>
</table>


<#include "/common/footer.ftl">
