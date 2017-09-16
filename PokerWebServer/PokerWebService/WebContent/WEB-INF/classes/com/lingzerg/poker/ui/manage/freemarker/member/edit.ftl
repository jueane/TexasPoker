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
			<form method="post" action="${basePath}/manage/member/edit" class="form-horizontal">
				<div class="control-group">
					<label class="control-label">用户名</label>
					<div class="controls">
						<input name="username" value="${entity.getUsername()!}" type="text" maxlength="200" />
					</div>
				</div>	
				<div class="control-group">
					<label class="control-label">密码</label>
					<div class="controls">
						<input value="${entity.getPassword()!}" name="password" type="text" maxlength="200" />
					</div>
				</div>
				<div class="control-group">
					<label class="control-label">邮箱</label>
					<div class="controls">
						<input name="email" value="${entity.getEmail()!}" type="text" maxlength="200" />
					</div>
				</div>
				<div class="control-group">
					<label class="control-label">手机号</label>
					<div class="controls">
						<input name="phone" value="${entity.getPhone()!}" type="text" maxlength="200" />
					</div>
				</div>
				<div class="control-group">
					<label class="control-label">金币</label>
					<div class="controls">
						<input name="gem" value="${entity.getGem()!}" type="text" maxlength="200" />
					</div>
				</div>
				<div class="control-group">
					<label class="control-label">德币</label>
					<div class="controls">
						<input name="gold" value="${entity.getGold()!}" type="text" maxlength="200" />
					</div>
				</div>
				<div class="control-group">
					<label class="control-label">性别</label>
					<div class="controls">
						<select name="male" value="${entity.isMale()?string(1, 0)}">
						  <option value ="1" ${entity.isMale()?string('selected', '')}>男</option>
						  <option value ="0" ${entity.isMale()?string('', 'selected')}>女</option>
						</select>
					</div>
				</div>
				
				<div class="control-group">
					<label class="control-label">注册时间</label>
					<label class="control-label">${entity.getCreateDate()!}</label>
					
				</div>
				
				<div class="control-group">
					<label class="control-label">状态</label>
					<div class="controls">
						<select name="status" value="${entity.getStatus()!}">  
						  <option <#if entity.getStatus() = 0>selected</#if> value ="0">未验证</option>
						  <option <#if entity.getStatus() = 1>selected</#if> value ="1">已验证</option>
						  <option <#if entity.getStatus() = 2>selected</#if> value ="2">已封号</option> 
						</select>
					</div>
				</div>
				
				
				<div class="chat-message well">
					<input name="createDate" type="hidden" value="${entity.getCreateDate()!}"/>
					<input name="id" type="hidden" value="${entity.getId()!}"> 
					<input type="submit" class="btn btn-success" value="确定" /> 
					<a class="btn" href="../member">返回列表</a>
					<font color="#F00>${notice!}</font>
				</div>
			</form>
	</div>
</div>

</#macro>