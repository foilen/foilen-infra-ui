<#include "/common/header.ftl">

<h1><@spring.message "resource.rawView"/></h1>

<p id="topError" class="text-danger"></p>
<div class="fullMask"></div>
<div id="rawResource" autocomplete="off" data-resource-id="${resourceId}" data-resource-type="${resourceType}"><@spring.message "term.loading"/></div>

<#include "/common/footer.ftl">
