<#include	 "../layout.ftl">

<#macro head>
</#macro>

<#macro body>

<div id="content-header">
	<h1>添加管理员</h1>
</div>
<div class="container-fluid">
	<div class="widget-box">
		<div class="widget-title">
			<span class="icon">
				<i class="icon-th"></i>
			</span>
			<h5>管理员详情</h5>
		</div>
		<div class="widget-content">
			<form method="post" action="${basePath}/manage/sysuser/create" class="form-horizontal">
				<div class="control-group">
					<label class="control-label">用户名</label>
					<div class="controls">
						<input name="adminname" type="text" maxlength="200" />
					</div>
				</div>
				<div class="control-group">
					<label class="control-label">密码</label>
					<div class="controls">
						<input name="password" type="text" maxlength="200" />
					</div>
				</div>
				<input type="submit" class="btn btn-success" value="确定" /> 
				<a class="btn" href="../sysuser">返回列表</a>
			</form>
	</div>
</div>
</#macro>