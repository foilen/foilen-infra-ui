<#macro topmenu prefix items selectedTopMenuItem="" selectedLeftMenuItem="">

	<div class="navbar navbar-default" role="navigation">
	  <div class="navbar-header">
	    <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
	      <span class="sr-only">Toggle navigation</span>
	      <span class="icon-bar"></span>
	      <span class="icon-bar"></span>
	      <span class="icon-bar"></span>
	    </button>
	    <a class="navbar-brand" href="/">FoilenInfra</a>
	  </div>
	  <div class="navbar-collapse collapse">
	    <ul class="nav navbar-nav">
	
			<#list items as item>
		
				<#if item.children?size == 0>
					<@menuitem prefix item selectedTopMenuItem />
				<#else>
					<@menuitemWithChildren prefix item selectedTopMenuItem selectedLeftMenuItem />
				</#if>
			
			</#list>
	
	    </ul>
	    <ul class="nav navbar-nav navbar-right">
	      <li><a href="?lang=<@spring.message "navbar.nextlang.id"/>"><@spring.message "navbar.nextlang.name"/></a></li>
	      <li><a href="/index.html"/><@spring.message "navbar.newUi"/></a></li>
	    </ul>
	  </div>
	</div>
	
</#macro>

<#macro menuitem prefix item selectedItem="">
	<li class="${(item == selectedItem)?string('active','')}"><a href="${item.uri}"><@spring.message prefix + "." + item.name/></a></li>
</#macro>

<#macro menuitemButton prefix item selectedItem="">
	<li><a class="btn btn-${(item == selectedItem)?string('primary','default')}" href="${item.uri}"><@spring.message prefix + "." + item.name/></a></li>
</#macro>

<#macro menuitemWithChildren prefix item selectedTopMenuItem="" selectedLeftMenuItem="">
  <li class="dropdown ${(item == selectedTopMenuItem)?string('active','')}">
    <a href="#" class="dropdown-toggle" data-toggle="dropdown"><@spring.message prefix + "." + item.name/> <b class="caret"></b></a>
    <ul class="dropdown-menu">
			<#list item.children as child>
				<@menuitem prefix child selectedLeftMenuItem />
			</#list>
    </ul>
  </li>
</#macro>

<#macro leftmenu prefix selectedTopMenuItem="" selectedLeftMenuItem="">

	<#if selectedTopMenuItem?has_content>
		<div class="col-md-2">
		  <h2><@spring.message prefix + "." + selectedTopMenuItem.name/></h2>
		  <ul class="left-menu">
			<#list selectedTopMenuItem.children as item>
				<@menuitemButton prefix item selectedLeftMenuItem />
			</#list>
		  </ul>
		</div>
	</#if>

</#macro>

<#macro graph url>
  <div class="canvas-holder">
    <canvas data-url="${url}" class="line-chart" width="250" height="125"></canvas>
  </div>
  <script>
  
  </script>
</#macro>
