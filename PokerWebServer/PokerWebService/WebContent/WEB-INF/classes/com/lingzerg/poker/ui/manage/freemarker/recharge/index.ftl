<#include	 "../layout.ftl">
<#macro head>
</#macro>

<#macro body>
<div id="content-header">
	<h1>德币消费记录</h1>
</div>
<div class="container-fluid">
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
				<th><p>用户</p></th>
				<th><p>数量</p></th>
				<th><p>内容</p></th>
			</tr>
			<#list pager.getList()! as entity>
				<tr>
				<td>${entity.getMember().getUsername()!}</td>
				<td>${entity.getCount()!}</td>
				<td>
					<#if entity.getSourceType() = 1>
						充值
						<#elseif entity.getSourceType() = 2>
						游戏
						<#elseif entity.getSourceType() = 3>
						活动
						<#elseif entity.getSourceType() = 4>
						苹果机
						<#elseif entity.getSourceType() = 5>
						发牌女郎
						<#elseif entity.getSourceType() = 6>
						表情
						<#elseif entity.getSourceType() = 7>
						评论奖励
						<#elseif entity.getSourceType() = 8>
						首日登陆奖励
						<#elseif entity.getSourceType() = 9>
						邀请好友
					</#if>
				</td>
				</tr>
			</#list>			
		</table>
	</div>
	
</div>

<#include	 "../pager.ftl">
<td>${notice !}</td>

</#macro>