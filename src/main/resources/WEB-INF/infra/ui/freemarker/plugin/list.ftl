<#include "/common/header.ftl">

<h3><@spring.message "plugin.available"/></h3>
<table class="table table-striped">
  <tr>
    <th><@spring.message "plugin.name"/></th>
    <th><@spring.message "plugin.vendor"/></th>
    <th><@spring.message "plugin.description"/></th>
    <th><@spring.message "plugin.version"/></th>
    <th><@spring.message "plugin.resources"/></th>
    <th><@spring.message "plugin.resourceEditors"/></th>
    <th><@spring.message "plugin.timers"/></th>
  </tr>
  <#list availables as item>
    <tr>
      <td>${item.pluginName}</td>
      <td>${item.pluginVendor}</td>
      <td>${item.pluginDescription}</td>
      <td>${item.pluginVersion}</td>
      <td>
        <ul>
          <#list item.customResources as customResource>
            <li><strong>${customResource.resourceType}</strong> (${customResource.resourceClass.name})</li>
          </#list>            
        </ul>
      </td>
      <td>
        <ul>
          <#list item.resourceEditors as resourceEditor>
            <li><a href="/pluginresources/create/${resourceEditor.editorName}">${resourceEditor.editorName}</a> for ${resourceEditor.editor.forResourceType.name}</li>
          </#list>            
        </ul>
      </td>
      <td>
        <ul>
          <#list item.timers as timer>
            <li>${timer.timerName} | ${timer.deltaTime} ${timer.calendarUnitInText}</li>
          </#list>            
        </ul>
      </td>
    </tr>
  </#list>
</table>

<h3><@spring.message "plugin.broken"/></h3>
<table class="table table-striped">
  <tr>
    <th><@spring.message "plugin.class"/></th>
    <th><@spring.message "plugin.vendor"/></th>
    <th><@spring.message "plugin.name"/></th>
    <th><@spring.message "plugin.description"/></th>
    <th><@spring.message "plugin.version"/></th>
    <th><@spring.message "plugin.error"/></th>
  </tr>
  <#list brokens as item>
    <tr>
      <td>${item.a.name}</td>
      <#if item.b??>
        <td>${item.b.pluginVendor}</td>
        <td>${item.b.pluginName}</td>
        <td>${item.b.pluginDescription}</td>
        <td>${item.b.pluginVersion}</td>
      <#else>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
      </#if>
      <td>${item.c}</td>
    </tr>
  </#list>
</table>


<#include "/common/footer.ftl">