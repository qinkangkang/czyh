<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>czyhweb移动端管理系统</title>
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

</head>

<body>
<div class="modal-dialog modal-lg">

<div class="row">
  <div class="col-md-10"><h3>czyhweb V0.3 Mobile Phone <span style="margin-left: 40px;"><a id="exit" href="javascript:;"><fmt:message key="fxl.index.menu.setting.exit" /></a></span></h3></div>
</div>
<div class="row">
  <div class="col-xs-6 col-sm-6 col-md-6"><a href="${ctx}/fxl/operating/consult/replyPhone/10"><button type="button" class="btn btn-primary" style="width:100%; height:100px; margin-top: 50px">未回复咨询</button></a></div>
  
  <div class="col-xs-6 col-sm-6 col-md-6"><a href="${ctx}/fxl/operating/consult/replyPhone/20"><button type="button" class="btn btn-success" style="width:100%; height:100px; margin-top: 50px">已回复咨询</button></a></div>
</div>

<div class="row">

  <div class="col-xs-6 col-sm-6 col-md-6"><a href="${ctx}/fxl/order/orderViewPhone"><button type="button" class="btn btn-info" style="width: 100%; height:100px; margin-top: 50px">订单查询</button></a></div>
  
  <div class="col-xs-6 col-sm-6 col-md-6"><a href="${ctx}/fxl/order/refundPhone"><button type="button" class="btn btn-warning" style="width: 100%; height:100px; margin-top: 50px">已退款列表</button></a></div>

</div>

<div class="row">

  <shiro:hasPermission name="orderAudit"><div class="col-xs-6 col-sm-6 col-md-6"><a href="${ctx}/fxl/order/orderAuditPhone"><button type="button" class="btn btn-info" style="width: 100%; height:100px; margin-top: 50px">退款审核</button></a></div></shiro:hasPermission>
    
</div>

</div>
</body>
</html>