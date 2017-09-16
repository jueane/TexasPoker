<#include	 "../layout.ftl">
<#macro head>
</#macro>

<#macro body>
<div id="content-header">
	<h1>客户反馈信息</h1>
</div>
<div class="container-fluid">
	<div class="widget-box">
	<div class="widget-title">
		<span class="icon">
			<i class="icon-th"></i>
		</span>
		<h5>反馈列表</h5>
	</div>
	<div class="widget-content nopadding">
		<table class="table table-bordered table-striped">
			<tr>
				<th><p>用户名</p></th>
				<th><p>内容</p></th>
			</tr>
			<#list pager.getList()! as entity>
				<tr>
				<td>${entity.getMember().getUsername()!}</td>
				<td>${entity.getContent()!}</td>
				</tr>
			</#list>			
		</table>
	</div>
	
</div>

<#include	 "../pager.ftl">
<td>${notice !}</td>

</#macro>