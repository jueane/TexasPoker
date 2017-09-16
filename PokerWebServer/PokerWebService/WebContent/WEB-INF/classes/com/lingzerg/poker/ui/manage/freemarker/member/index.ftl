<#include	 "../layout.ftl">
<#macro head>
</#macro>

<#macro body>
<div id="content-header">
	<h1>会员管理</h1>
</div>
<div class="container-fluid">
	<div>
		<a class="btn btn-info btn-large" href="${basePath}/manage/member/create">添加会员</a>
		<div class="widget-box">
			<div class="widget-title">
				<span class="icon">
					<i class="icon-th"></i>
				</span>
				<h5>会员列表</h5>
			</div>
			<div class="widget-content nopadding">
				<table class="table table-bordered table-striped">
				<tr>
				
				<th><p>用户名</p></th>
				<th><p>手机</p></th>
				<th><p>注册时间</p></th>
				<th><p>状态</p></th>
				<th><p>操作</p></th>
				</tr>
				<#list pager.getList()! as entity>
				<tr>
					
					<td>${entity.getUsername()!}</td>
					<td>${entity.getPhone()!}</td>
					<td>${entity.getCreateDate()!}</td>
					<td>
						<#if entity.getStatus() = 0> 未验证
						<#elseif entity.getStatus() = 1>已验证
						<#elseif entity.getStatus() = 2>已封号
						</#if>
					</td>
					<td>
						<a href="${basePath}/manage/member/edit?id=${entity.getId()}">编辑</a>
						<a href="${basePath}/manage/member/delete?id=${entity.getId()}">删除</a>
					</td>
				</tr>
				</#list>
				</table>
			</div>
	</div>
	<#include	 "../pager.ftl">
</div>


</#macro>