<html>
<head>
       
        <meta http-equiv="content-type" content="text/html; charset=UTF-8">
        <meta http-equiv="pragma" content="no-cache">
        <meta http-equiv="cache-control" content="no-cache">
        <meta http-equiv="expires" content="0">
        
        <link rel="shortcut icon" href="${basePath}/resources/website/img/logo.ico" />
        <link rel="stylesheet" href="${basePath}/resources/manage/css/bootstrap.min.css" />
		<link rel="stylesheet" href="${basePath}/resources/manage/css/bootstrap-responsive.min.css" />
		<link rel="stylesheet" href="${basePath}/resources/manage/css/fullcalendar.css" />	
		<link rel="stylesheet" href="${basePath}/resources/manage/css/unicorn.login.css" />
		<link rel="stylesheet" href="${basePath}/resources/manage/css/unicorn.grey.css" class="skin-color" />
        
		<script src="${basePath}/resources/scripts/jquery-1.7.2.min.js"></script>
		<script src="${basePath}/resources/manage/js/jquery.min.js"></script>  
        <script src="${basePath}/resources/manage/js/unicorn.login.js"></script> 
        <style>
        	.{
        		font-family:黑体;
        	}
        </style>
</head>
<body>
<div id="logo">
            <img src="${basePath}/resources/manage/img/logo.png" alt="" />
        </div>
        <div id="loginbox">            
            <form id="loginform" action="${basePath}/manage/login" method="post">
				<p>请输入管理员的用户名和密码.</p>
                <div class="control-group">
                    <div class="controls">
                        <div class="input-prepend">
                            <span class="add-on"><i class="icon-user"></i></span><input name="adminname" type="text" />
                        </div>
                    </div>
                </div>
                <div class="control-group">
                    <div class="controls">
                        <div class="input-prepend">
                            <span class="add-on"><i class="icon-lock"></i></span><input name="password" type="text" />
                        </div>
                    </div>
                </div>
                <div class="form-actions">
                    <span class="pull-left"><a href="#" class="flip-link" id="to-recover">忘记密码?</a></span>
                    <span class="pull-right"><input type="submit" class="btn btn-inverse" value="登陆" /></span>
                </div>
            </form>
            <form id="recoverform" action="#" class="form-vertical" />
				<p>请输入邮箱.</p>
				<div class="control-group">
                    <div class="controls">
                        <div class="input-prepend">
                            <span class="add-on"><i class="icon-envelope"></i></span><input type="text" placeholder="E-mail address" />
                        </div>
                    </div>
                </div>
                <div class="form-actions">
                    <span class="pull-left"><a href="#" class="flip-link" id="to-login">返回登陆</a></span>
                    <span class="pull-right"><input type="submit" class="btn btn-inverse" value="找回" /></span>
                </div>
            </form>
        </div>
</body>
</html>