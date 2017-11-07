<%@ page contentType="text/html;charset=UTF-8"%>
<!doctype html>
<html>
<head>
<title>零到壹，查找优惠 | <sitemesh:write property='title' /></title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<%@ include file="/WEB-INF/views/include/include.style.jsp"%>
<link href="${ctx}/styles/datatables/css/dataTables.bootstrap.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/artDialog/css/ui-dialog.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/toastr/toastr.min.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="${ctx}/styles/datatables/js/jquery.dataTables.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/datatables/js/dataTables.bootstrap.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/artDialog/dist/dialog-min.js"></script>
<script type="text/javascript" src="${ctx}/styles/toastr/toastr.min.js"></script>
<%@ include file="/WEB-INF/views/include/include.valid.jsp"%>
<script type="text/javascript">
$(document).ready(function() {

	/* if ($.trim($.cookie("fxlLanguage")) == "en_US") {
		pickerLocal = "en";
		dataTableLanguage = {};
	} */
	
	toastr.options = {
		"closeButton": true,
		"debug": false,
		"positionClass": "toast-bottom-right",
		"onclick": null,
		"showDuration": "300",
		"hideDuration": "1000",
		"timeOut": "8000",
		"extendedTimeOut": "1000",
		"showEasing": "swing",
		"hideEasing": "linear",
		"showMethod": "fadeIn",
		"hideMethod": "fadeOut"
	};

	$("a[id=exit]").click(function(e) {
		dialog({
			fixed : true,
			title : '<fmt:message key="fxl.index.exit.title" />',
			content : '<fmt:message key="fxl.index.exit.info" />',
			okValue : '<fmt:message key="fxl.index.menu.setting.exit" />',
			ok : function() {
				window.location.href = "${ctx}/fxl/logout";
			},
			cancelValue : '<fmt:message key="fxl.button.cancel" />',
			cancel : function() {
			}
		}).showModal();
	});
	
	var updatePassword =  $('#updatePassword');
	
	updatePassword.on('hide.bs.modal', function(e){
		updatePasswordForm.trigger("reset");
	});

	var updatePasswordForm = $('#updatePasswordForm');

	updatePasswordForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});

	updatePasswordForm.on("submit", function(event) {
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if (form.validationEngine("validate")){
			$.post(form.attr("action"), form.serialize(), function(data) {
				if (data.success) {
					dialog({
						title : '操作成功',
						content : data.msg,
						okValue : '<fmt:message key="fxl.button.close" />',
						ok : function() {
						}
					}).showModal();
				} else {
					dialog({
						title : '操作失败',
						content : data.msg,
						okValue : '<fmt:message key="fxl.button.close" />',
						ok : function() {
						}
					}).showModal();
				}
			}, "json");
		}
	});

}).ajaxError(function(event, request, settings) {
	var d;
	if (request.responseText == "com.czyh.fxl.sessionTimeout") {
		d = dialog({
			title : '会话超时啦！',
			content : '由于您长时间没有进行应用操作，您的平台会话已超时，请重新返回登录页面登录应用！',
			okValue : '去登录页',
			ok : function() {
				window.location.href = "${ctx}/fxl/logout";
			}
		});
	} else {
		d = dialog({
			title : '系统出错啦！',
			content : '程序猿同学，看到这个窗口那就证明你当前操作的程序出错了，快去修改吧！请叫我雷锋，不谢！',
			okValue : '好吧，我去改BUG',
			ok : function() {
			}
		});
	}
	d.showModal();
});
</script>
<sitemesh:write property='head' />
</head>
<body role="document">
<nav class="navbar navbar-fixed-top navbar-default">
	<div class="container" style="width:Auto">
		<div class="navbar-header">
			<a class="navbar-brand" href="${ctx}/fxl/index"><span class="label label-danger">czyh</span></a>
		</div>
		<div id="navbar" class="navbar-collapse collapse">
			<ul class="nav navbar-nav">
				<c:forEach var="menuItem" items="${menu}">
					<li class="dropdown"><a href="#" class="dropdown-toggle" data-toggle="dropdown">${menuItem.name} <span class="caret"></span></a>
						<ul class="dropdown-menu" role="menu">
							<c:forEach var="subMenuItem" items="${menuItem.children}" varStatus="status">
								<c:if test="${!status.first}" >
								<li role="separator" class="divider"></li>
								</c:if>
								<li class="text-center"><a href="${ctx}${subMenuItem.url}"><i class="icon-order"></i> ${subMenuItem.name}</a></li>
							</c:forEach>
						</ul>
					</li>
				</c:forEach>
			</ul>
			<ul class="nav navbar-nav navbar-right">
				<li class="dropdown"><a href="#" class="dropdown-toggle" data-toggle="dropdown"><shiro:principal type="com.czyh.czyhweb.security.ShiroDbRealm$ShiroUser" property="name"></shiro:principal><span class="caret"></span></a>
					<ul class="dropdown-menu" role="menu">
						<li class="text-center"><a href="${ctx}/fxl/index"><i class="icon-order"></i> <fmt:message key="fxl.index.menu.my.info" /></a></li>
						<li role="separator" class="divider"></li>
						<li class="text-center"><a href="#" data-toggle="modal" data-target="#updatePassword"><i class="icon-order"></i> <fmt:message key="fxl.index.menu.my.updatePassword" /></a></li>
						<li role="separator" class="divider"></li>
						<li class="text-center"><a href="${ctx}/fxl/index"><i class="icon-order"></i> <fmt:message key="fxl.index.menu.my.help" /></a></li>
					</ul>
				</li>
				<li><a id="exit" href="javascript:;"><fmt:message key="fxl.index.menu.setting.exit" /></a></li>
			</ul>
		</div>
	</div>
</nav>
<!--head区结束-->
<div style="height: 60px; width: 0px;"></div>
<!--主内容区-->
<div role="main" class="container">
<sitemesh:write property='body' />
</div>
<!--主内容区结束-->
<!--修改密码信息开始-->
<div class="modal fade" id="updatePassword" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
	aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="myModalLabel">
					<fmt:message key="fxl.index.password.titile" />
				</h4>
			</div>
			<form id="updatePasswordForm" action="${ctx}/fxl/updatePassword" method="post" role="form">
				<div class="modal-body">
					<div class="form-group col-md-8 col-md-offset-2">
						<label for="oldPassword"><fmt:message key="fxl.index.password.oldPasswrod" />：</label> <input type="password"
							id="oldPassword" name="oldPassword" placeholder="<fmt:message key="fxl.index.password.oldPasswrodAlt" />"
							class="validate[required,minSize[6],maxSize[32]] form-control input-sm">
					</div>
					<div class="form-group col-md-8 col-md-offset-2">
						<label for="password"><fmt:message key="fxl.index.password.newPasswrod" />：</label> <input type="password"
							id="password" name="password" placeholder="<fmt:message key="fxl.index.password.newPasswrodAlt" />"
							class="validate[required,minSize[6],maxSize[32]] form-control input-sm">
					</div>
					<div class="form-group col-md-8 col-md-offset-2">
						<label for="password2"><fmt:message key="fxl.index.password.newPasswrod2" />：</label> <input type="password"
							id="password2" name="password2" placeholder="<fmt:message key="fxl.index.password.newPasswrod2Alt" />"
							class="validate[required,equals[password]] form-control input-sm">
					</div>
					<div style="clear: both;"></div>
				</div>
				<div class="modal-footer">
					<button class="btn btn-primary " type="submit">
						<span class="glyphicon glyphicon-floppy-saved"></span>
						<fmt:message key="fxl.button.save" />
					</button>
					<button class="btn btn-warning" type="reset" onclick="javascript:$(this).closest('form').validationEngine('hideAll');">
						<span class="glyphicon glyphicon-repeat"></span>
						<fmt:message key="fxl.button.reset" />
					</button>
					<button type="button" class="btn btn-default" data-dismiss="modal">
						<span class="glyphicon glyphicon-remove"></span>
						<fmt:message key="fxl.button.close" />
					</button>
				</div>
			</form>
		</div>
	</div>
</div>
<!--修改密码信息结束-->
<!--为了保证页脚不能阻挡内容信息添加的空DIV-->
<%-- <div style="height: 60px;"></div>
<%@ include file="/WEB-INF/views/include/footer.jsp"%> --%>
</body>
</html>