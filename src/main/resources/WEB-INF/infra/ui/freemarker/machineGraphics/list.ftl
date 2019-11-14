<#include "/common/header.ftl">

<table class="table table-striped">
  <tr>
    <th><@spring.message 'term.name'/></th>
    <th><@spring.message 'term.cpu'/></th>
    <th><@spring.message 'term.memory'/></th>
    <th><@spring.message 'term.disk'/></th>
    <th><@spring.message 'term.network'/></th>
  </tr>
  <#list machines as machine>
	  <tr>
	    <td><a href="/${controllerName}/view/${machine}">${machine}</a></td>
	    <td><@graph url="/${controllerName}/graphCpu/${machine}"/></td>
	    <td><@graph url="/${controllerName}/graphMemory/${machine}"/></td>
	    <td><@graph url="/${controllerName}/graphDisk/${machine}"/></td>
	    <td><@graph url="/${controllerName}/graphNetwork/${machine}"/></td>
	  </tr>
  </#list>
</table>


<#include "/common/footer.ftl">
