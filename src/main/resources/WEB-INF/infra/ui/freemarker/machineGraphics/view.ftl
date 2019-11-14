<#include "/common/header.ftl">

<h3>${machineName}</h3>

<h4><@spring.message 'term.cpu'/></h4>
<@graph url="/${controllerName}/graphCpu/${machineName}"/>

<h4><@spring.message 'term.memory'/></h4>
<@graph url="/${controllerName}/graphMemory/${machineName}"/>

<h4><@spring.message 'term.disk'/></h4>
<@graph url="/${controllerName}/graphDisk/${machineName}"/>

<h4><@spring.message 'term.network'/></h4>
<@graph url="/${controllerName}/graphNetwork/${machineName}"/>

<#include "/common/footer.ftl">
