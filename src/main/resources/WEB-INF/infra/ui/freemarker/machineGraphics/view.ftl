<#include "/common/header.ftl">

<h3>${machineName}</h3>

<h4><@spring.message 'terms.cpu'/></h4>
<@graph url="/${controllerName}/graphCpu/${machineName}"/>

<h4><@spring.message 'terms.memory'/></h4>
<@graph url="/${controllerName}/graphMemory/${machineName}"/>

<h4><@spring.message 'terms.disk'/></h4>
<@graph url="/${controllerName}/graphDisk/${machineName}"/>

<h4><@spring.message 'terms.network'/></h4>
<@graph url="/${controllerName}/graphNetwork/${machineName}"/>

<#include "/common/footer.ftl">
