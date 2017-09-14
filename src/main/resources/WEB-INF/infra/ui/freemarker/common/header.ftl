<#import "/spring.ftl" as spring />
<#include "visual.ftl" />
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">

  <title>Foilen Infra UI</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">

  <!-- Favicon -->
  <link rel="icon" type="image/png" href="/images/favicon.png" />
  <link rel="shortcut icon" type="image/png" href="/images/favicon.png" />

  <link href="<@spring.url'/bundles/all.css'/>" rel="stylesheet">
  <script src="<@spring.url'/bundles/all.js'/>"></script>
  
</head>
<body>

  <div class="container-fluid">

    <!-- Top menu -->
    <@topmenu "menu" rootMenuEntry.children topMenuEntry leftMenuEntry />
    <!-- /Top menu -->

    <div class="pull-right">
      ${userDetails.email}
      <#if isAdmin>
        (ADMIN)
      </#if>
    </div>

    <!-- Main -->
    <div class="row">

      <#if leftMenuEntry??>
        <!-- Left menu -->
        <@leftmenu "menu" topMenuEntry leftMenuEntry />
        <!-- /Left menu -->
      </#if>

      <!-- Middle -->

      <#if leftMenuEntry??>
        <div class="col-md-10">
        <h2><@spring.message "menu." + topMenuEntry.name/> - <@spring.message "menu." + leftMenuEntry.name/></h2>
      <#else>
        <div class="col-md-12">
        <h2><@spring.message "menu." + topMenuEntry.name/></h2>
      </#if>
      
      <#if errorCode??>
        <p class="bg-danger"><@spring.message errorCode errorParams/></p>
      </#if>
