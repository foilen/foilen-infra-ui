<#include "/common/header.ftl">

<table class="table table-striped">
  <tr>
    <th><@spring.message 'terms.name'/></th>
    <th><@spring.message 'terms.cpu'/></th>
    <th><@spring.message 'terms.memory'/></th>
    <th><@spring.message 'terms.disk'/></th>
    <th><@spring.message 'terms.network'/></th>
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
