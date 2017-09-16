<html>
<head>
       
        <meta http-equiv="content-type" content="text/html; charset=UTF-8">
        <meta http-equiv="pragma" content="no-cache">
        <meta http-equiv="cache-control" content="no-cache">
        <meta http-equiv="expires" content="0">
        <script src="${basePath}/resources/scripts/jquery-1.7.2.min.js"></script>
        
        <title>游戏管理平台</title>
        <link rel="stylesheet" href="${basePath}/resources/manage/css/bootstrap.min.css" />
		<link rel="stylesheet" href="${basePath}/resources/manage/css/bootstrap-responsive.min.css" />
		<link rel="stylesheet" href="${basePath}/resources/manage/css/fullcalendar.css" />	
		<link rel="stylesheet" href="${basePath}/resources/manage/css/unicorn.main.css" />
		<link rel="stylesheet" href="${basePath}/resources/manage/css/unicorn.grey.css" class="skin-color" />
        
        <link rel="shortcut icon" href="${basePath}/resources/website/img/logo.ico" />
		
		
		<script src="${basePath}/resources/manage/js/excanvas.min.js"></script>
        <script src="${basePath}/resources/manage/js/jquery.min.js"></script>
        <script src="${basePath}/resources/manage/js/jquery.ui.custom.js"></script>
        <script src="${basePath}/resources/manage/js/bootstrap.min.js"></script>
        <script src="${basePath}/resources/manage/js/jquery.flot.min.js"></script>
        <script src="${basePath}/resources/manage/js/jquery.flot.resize.min.js"></script>
        <script src="${basePath}/resources/manage/js/jquery.peity.min.js"></script>
        <script src="${basePath}/resources/manage/js/fullcalendar.min.js"></script>
        <script src="${basePath}/resources/manage/js/unicorn.js"></script>
		
        <@head />
</head>
<body>
<div id="header">
	<h1><a href="${basePath}">商城后台</a></h1>
</div>
<div id="sidebar">
	<a href="#" class="visible-phone"><i class="icon icon-home"></i> 管理后台</a>
	<ul style="display: block;">
		<li class="submenu">
			<a href="#"><i class="icon icon-th-list"></i> <span>会员</span> <span class="label">1</span></a>
			<ul>
				<li><a href="${basePath}/manage/member">会员</a></li>
				<li><a href="${basePath}/manage/report">会员反馈</a></li>
			</ul>
		</li>
		<li class="submenu">
			<a href="#"><i class="icon icon-th-list"></i> <span>销售管理</span> <span class="label">1</span></a>
			<ul>
				<li><a href="${basePath}/manage/recharge">德币记录</a></li>
			</ul>
		</li>
		<li class="submenu">
			<a href="#"><i class="icon icon-th-list"></i> <span>系统</span> <span class="label">1</span></a>
			<ul>
				<li><a href="${basePath}/manage/sysuser">系统管理员管理</a></li>
			</ul>
		</li>
		
	</ul>

</div>

<div id="content">
<@body/>
</div>
</body>
</html>