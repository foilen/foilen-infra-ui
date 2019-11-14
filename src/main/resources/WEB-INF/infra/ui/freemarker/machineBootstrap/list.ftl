<#include "/common/header.ftl">

<table class="table table-striped">
  <tr>
    <th><@spring.message 'term.name'/></th>
    <th><@spring.message 'term.ip'/></th>
  </tr>
  <#list machines as machine>
	  <tr>
	    <td><a href="/${controllerName}/view/${machine.name}">${machine.name}</a></td>
      <td>${machine.publicIp!""}</td>
	  </tr>
  </#list>
</table>


<#include "/common/footer.ftl">
