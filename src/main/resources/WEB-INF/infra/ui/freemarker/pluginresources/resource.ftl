<#import "/spring.ftl" as spring />

<h2>${pageDefinition.pageTitle}</h2>

<#list pageDefinition.pageItems as pageItem>
  
  <#switch pageItem.class.simpleName>
  
    <#case "HiddenFieldPageItem">
      
      <div>
        <input type="hidden" name="${pageItem.fieldName}" value="${pageItem.fieldValue!''}" />
      </div>
      
      <#break>
      
    <#case "InputTextFieldPageItem">
      
      <div class="form-group" id="${pageItem.fieldName}">
        <label for="${pageItem.fieldName}">${pageItem.label}</label>
        <input type="${pageItem.password?then('password','text')}" class="form-control" name="${pageItem.fieldName}" value="${pageItem.fieldValue!''}" placeholder="${pageItem.placeholder!''}" />
        <span class="help-inline text-danger"></span>
      </div>
      
      <#break>

    <#case "LabelPageItem">
      <p>${pageItem.text}</p>
      <#break>
      
    <#case "ListPageItem">
      <p>${pageItem.title}</p>
      <ul>
        <#list pageItem.items as item>
          <li>${item}</li>
        </#list>
      </ul>
      <#break>
      
    <#case "MultilineInputTextFieldPageItem">
      
      <div class="form-group" id="${pageItem.fieldName}">
        <label for="${pageItem.fieldName}">${pageItem.label}</label>
        <textarea rows="${pageItem.rows}" class="form-control" name="${pageItem.fieldName}">${pageItem.fieldValue!''}</textarea>
        <span class="help-inline text-danger"></span>
      </div>
      
      <#break>
      
    <#case "ResourceFieldPageItem">
    
      <#if pageItem.value??>
        <div class="form-group ResourceFieldPageItem" id="${pageItem.fieldName}" 
          data-button-choose='<@spring.message "button.choose"/>'
          data-button-change='<@spring.message "button.change"/>'
          data-button-clear='<@spring.message "button.clear"/>'
          data-label='${pageItem.label}'
          data-field-name="${pageItem.fieldName}" 
          data-resource-type="${pageItem.resourceType.name}" 
          data-resource-id="${pageItem.value.internalId}" 
          data-resource-name="${pageItem.value.resourceName}" 
          data-resource-description="${pageItem.value.resourceDescription}" 
          >
        </div>
      <#else>
        <div class="form-group ResourceFieldPageItem" id="${pageItem.fieldName}" 
          data-button-choose='<@spring.message "button.choose"/>'
          data-button-change='<@spring.message "button.change"/>'
          data-button-clear='<@spring.message "button.clear"/>'
          data-label='${pageItem.label}'
          data-field-name="${pageItem.fieldName}" 
          data-resource-type="${pageItem.resourceType.name}" 
          data-resource-id="" 
          >
        </div>
      </#if>
      <#break>

    <#case "ResourcesFieldPageItem">
      <div class="form-group ResourcesFieldPageItem" id="${pageItem.fieldName}" 
        data-button-add='<@spring.message "button.add"/>'
        data-button-clear='<@spring.message "button.clearAll"/>'
        data-label='${pageItem.label}'
        data-field-name="${pageItem.fieldName}" 
        data-resource-type="${pageItem.resourceType.name}" 
        >
        <#list pageItem.values as resource>
          <div class="ResourcesFieldPageItem_resource"
            data-resource-id="${resource.internalId}" 
            data-resource-name="${resource.resourceName}" 
            data-resource-description="${resource.resourceDescription}"
            ></div>
        </#list>
      </div>
      
      <#break>
      
    <#case "SelectOptionsPageItem">
    
      <div class="form-group" id="${pageItem.fieldName}">
        <label for="${pageItem.fieldName}">${pageItem.label}</label>
        <select name="${pageItem.fieldName}" class="form-control">
          <#list pageItem.options as option>
            <#if pageItem.fieldValue??>
              <#if option = pageItem.fieldValue>
                <option value="${option}" selected="selected">${option}</option>
              <#else>
                <option value="${option}">${option}</option>
              </#if>
            <#else>
              <option value="${option}">${option}</option>
            </#if>
          </#list>
        <span class="help-inline text-danger"></span>
        </select>
      </div>
    
      <#break>

    <#default>
  
      <p class="text-danger">Unknown page item: ${pageItem.class.simpleName}</p>
  
  </#switch>
  <div>
  </div>
</#list>
