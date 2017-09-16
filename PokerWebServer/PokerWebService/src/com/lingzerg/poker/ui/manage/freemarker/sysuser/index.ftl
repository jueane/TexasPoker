<#include	 "../layout.ftl">
<#macro head>
</#macro>

<#macro body>
<div id="content-header">
	<h1>系统用户管理</h1>
</div>
<div class="container-fluid">
	<div>
		<a class="btn btn-info btn-large" href="${basePath}/manage/sysuser/create">添加系统用户</a>
	</div>
	<div class="widget-box">
	<div class="widget-title">
		<span class="icon">
			<i class="icon-th"></i>
		</span>
		<h5>系统账户列表</h5>
	</div>
	<div class="widget-content nopadding">
		<table class="table table-bordered table-striped">
			<tr>
				<th><p>id</p></th>
				<th><p>用户名</p></th>
				<th><p>上次登录时间</p></th>
				<th>操作</th>
			</tr>
			<#list pager.getList()! as entity>
				<tr>
				<td>${entity.getId()!}</td>
				<td>${entity.getAdminname()!}</td>
				<td>${entity.getLastLoginDate()!}</td>
				<td><a href="${basePath}/manage/sysuser/edit?id=${entity.getId()}">编辑</a>
				<a href="${basePath}/manage/sysuser/delete?id=${entity.getId()}">删除</a></td>
				</tr>
			</#list>			
		</table>
	</div>
	
</div>

<#include	 "../pager.ftl">
<td>${notice !}</td>

</#macro>