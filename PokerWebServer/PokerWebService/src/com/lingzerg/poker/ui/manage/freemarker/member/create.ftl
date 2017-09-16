<#include	 "../layout.ftl">

<#macro head>
</#macro>

<#macro body>

<div id="content-header">
	<h1>添加会员</h1>
</div>
<div class="container-fluid">
	<div class="widget-box">
		<div class="widget-title">
			<span class="icon">
				<i class="icon-th"></i>
			</span>
			<h5>会员详情</h5>
		</div>
		<div class="widget-content">
			<form method="post" action="${basePath}/manage/member/create" class="form-horizontal">
				<div class="control-group">
					<label class="control-label">用户名</label>
					<div class="controls">
						<input name="username" type="text" maxlength="200" />
					</div>
				</div>	
				<div class="control-group">
					<label class="control-label">密码</label>
					<div class="controls">
						<input  name="password" type="text" maxlength="200" />
					</div>
				</div>
				<div class="control-group">
					<label class="control-label">邮箱</label>
					<div class="controls">
						<input name="email" type="text" maxlength="200" />
					</div>
				</div>
				<div class="control-group">
					<label class="control-label">手机号</label>
					<div class="controls">
						<input name="phone" type="text" maxlength="200" />
					</div>
				</div>
				<div class="control-group">
					<label class="control-label">金币</label>
					<div class="controls">
						<input name="realname" type="text" maxlength="200" />
					</div>
				</div>
				<div class="control-group">
					<label class="control-label">德币</label>
					<div class="controls">
						<input name="balance" type="text" maxlength="200" />
					</div>
				</div>
				<div class="control-group">
					<label class="control-label">性别</label>
					<div class="controls">
						<select name="male" >
						  <option value ="1" >男</option>
						  <option value ="0">女</option>
						</select>
					</div>
				</div>
				<div class="chat-message well">
					<input name="id" type="hidden" value="3"> 
					<input type="submit" class="btn btn-success" value="确定" /> 
					<a class="btn" href="../member">返回列表</a>
					<font color="#F00>${notice!}</font>
				</div>
			</form>
	</div>
</div>

</#macro>