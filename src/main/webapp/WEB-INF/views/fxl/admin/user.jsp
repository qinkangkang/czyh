<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/ztree/css/zTreeStyle.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2-bootstrap.min.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="${ctx}/styles/ztree/js/jquery.ztree.all.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/select2.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/i18n/zh-CN.js"></script>
<script type="text/javascript">
$(document).ready(function() {
	
	$.validationEngineLanguage.allRules.checkUsername={
		"url" : '${ctx}/fxl/admin/user/checkUsername',
		"alertTextOk" : '您可以使用该用户登录名！',
	    "alertText" : '您输入的用户登录名已被占用，请更换其它用户登录名！',
	    "alertTextLoad" : '正在验证该用户登录名是否被占用……'
	};
	
	$.validationEngineLanguage.allRules.checkEditUsername={
		"url" : '${ctx}/fxl/admin/user/checkEditUsername',
		"extraDataDynamic": ['#id'],
		"alertTextOk" : '您可以使用该用户登录名！',
	    "alertText" : '您输入的用户登录名已被占用，请更换其它用户登录名！',
	    "alertTextLoad" : '正在验证该用户登录名是否被占用……'
	};
	
	var category = $("#category").select2({
		placeholder: "请选择一个或多个员工类别",
		allowClear: true,
		language: pickerLocal
	});
	
	var searchForm = $('#searchForm');
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#select").click(function(e) {
		userTable.ajax.reload();
		//$("#selectDiv").slideUp("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	var userTable = $("table#userTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	"scrollY": "500px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/admin/user/getUserList",
		    "type": "POST",
 		    "data": function ( data ) {
 		    	$.each(searchForm.serializeArray(), function(i, n){
 		    		data[n.name] = n.value;
 				});
 		        return data;
 		    }
		},
		"stateSave": true,
		"deferRender": true,
		//"pagingType": "full_numbers",
		"lengthMenu": [[10, 20, 30, 50], [10, 20, 30, 50]],
		"lengthChange": false,
		"displayLength" : datatablePageLength,
		"columns" : [
		{	"title" : "ID",
			"data" : "DT_RowId",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center>登录名</center>',
			"data" : "username",
			"width" : "50px",
			"className": "text-center",
			"orderable" : true
		},{
			"title" : '<center>用户姓名</center>',
			"data" : "realname",
			"width" : "50px",
			"className": "text-center",
			"orderable" : false
		}, {
			"title" : '<center>E-Mail</center>',
			"data" : "email",
			"width" : "50px",
			"className": "text-center",
			"orderable" : false
		}, {
			"title" : '<center>联系电话</center>',
			"data" : "phone",
			"width" : "40px",
			"className": "text-center",
			"orderable" : false
		}, {
			"title" : '<center>状态</center>',
			"data" : "status",
			"width" : "50px",
			"className": "text-center",
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "50px",
			"className": "text-center",
			"orderable" : false
		} ],
		"columnDefs" : [ {
			"targets" : [ 6 ],
			"render" : function(data, type, full) {
				return '<button id="edit" mId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs"><fmt:message key="fxl.button.edit" /></button>';
			}
		} ]
	});
	
	var zTreeObj;
	
	$("#userTable").delegate("button[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$("#username").removeClass("validate[required,minSize[6],maxSize[100],custom[onlyLetterNumber],ajax[checkUsername]]");
		$("#username").addClass("validate[required,minSize[6],maxSize[100],custom[onlyLetterNumber],ajax[checkEditUsername]]");
		$('#passwordDiv').hide();
		$('#resetBtn').hide();
		resetPasswordBtn.show();
		delUserBtn.show();
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/admin/user/getUser/" + mId, function(data) {
			if(data.success){
				$("#id").val(mId);
				$("#username").val(data.username);
				$("#realname").val(data.realname);
				$("#phone").val(data.phone);
				$("#email").val(data.email);
				$("#status option[value='" + data.status + "']").prop("selected",true);
				var categoryVal = data.category;
				if($.trim(categoryVal) != ""){
					category.val(categoryVal.split(";")).trigger("change");
				}
				if(zTreeObj){
					zTreeObj.destroy();
				}
				zTreeObj = $.fn.zTree.init($("#userRoleTree"), {
					check: {
						enable: true
					},
					data: {
						simpleData: {
							enable:true,
							rootPId: ""
						}
					}
				}, data.tree);
				zTreeObj.expandAll(true);
				createUserModal.modal('show')
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});

	var createUserModal =  $('#createUserModal');
	
	createUserModal.on('hide.bs.modal', function(e){
		createUserForm.trigger("reset");
		category.val(null).trigger("change");
	});

	$('#createUserBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		$("#username").removeClass("validate[required,minSize[6],maxSize[100],custom[onlyLetterNumber],ajax[checkEditUsername]]");
		$("#username").addClass("validate[required,minSize[6],maxSize[100],custom[onlyLetterNumber],ajax[checkUsername]]");
		$('#passwordDiv').show();
		$('#resetBtn').show();
		resetPasswordBtn.hide();
		delUserBtn.hide();
		$.post("${ctx}/fxl/admin/user/getRoleTree", function(data) {
			if(data.success){				
				if(zTreeObj){
					zTreeObj.destroy();
				}
				zTreeObj = $.fn.zTree.init($("#userRoleTree"), {
					check: {
						enable: true
					},
					data: {
						simpleData: {
							enable:true,
							rootPId: ""
						}
					}
				}, data.tree);
				zTreeObj.expandAll(true);
				createUserModal.modal('show')
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	var createUserForm = $('form#createUserForm');
	
	createUserForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	createUserForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			var ids = new Array();
			if(zTreeObj){
    			var nodes = zTreeObj.getCheckedNodes(true);
    			if(nodes.length == 0){
    				toastr.warning('<fmt:message key="fxl.admin.user.addUser.confirm.selectUser"/>');
    				form.removeData("running");
    				event.preventDefault();
    				return false;
    			}
    			$.each(nodes, function(i, n){
    				if(n.id != 0){
    					ids.push({name:"ids", value:n.id});
    				}
    			});
    			if($('#actionFlag').val() == "add"){
    				$.post(form.attr("action"), $.param($.merge(form.serializeArray(),ids),true), function(data){
						if(data.success){
							toastr.success(data.msg);
							userTable.ajax.reload(null,false);
							zTreeObj.cancelSelectedNode();
							createUserModal.modal('hide');
						}else{
							toastr.error(data.msg);
						}
						form.removeData("running");
					}, "json");
    			}else{
    				$.post("${ctx}/fxl/admin/user/editUser", $.param($.merge(form.serializeArray(),ids),true), function(data){
						if(data.success){
							toastr.success(data.msg);
							userTable.ajax.reload(null,false);
							createUserModal.modal('hide');
						}else{
							toastr.error(data.msg);
						}
						form.removeData("running");
					}, "json");
    			}
    		}else{
    			toastr.warning('<fmt:message key="fxl.admin.user.addUser.createTreeError" />');
    			form.removeData("running");
    		}
		}
	});
	
	createUserForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createUserForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		createUserForm.validationEngine('hideAll');
	});
	
	var resetPasswordBtn = $("#resetPasswordBtn");

	resetPasswordBtn.click(function(event) {
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '<fmt:message key="fxl.admin.user.resetPassowrd.confim" />',
		    okValue: '<fmt:message key="fxl.button.ok" />',
		    ok: function () {
		    	$.post("${ctx}/fxl/admin/user/resetMemberPassword/" + $("#id").val() , function(data) {
					if(data.success){
						toastr.success(data.msg);
						userTable.ajax.reload(null,false);
						editUserModal.modal('hide');
					}else{
						toastr.error(data.msg);
					}
				}, "json");
		    },
		    cancelValue: '<fmt:message key="fxl.button.cancel" />',
		    cancel: function (){
		    }
		}).showModal();
    });
	
	var delUserBtn = $("#delUserBtn");
	
	delUserBtn.click(function(event) {
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '<fmt:message key="fxl.admin.user.deleteUser.confim" />',
		    okValue: '<fmt:message key="fxl.button.ok" />',
		    ok: function () {
		    	$.post("${ctx}/fxl/admin/user/delUser/" + $("#id").val() , function(data) {
					if(data.success){
						toastr.success(data.msg);
						userTable.ajax.reload(null,false);
						editUserModal.modal('hide');
					}else{
						toastr.error(data.msg);
					}
				}, "json");
		    },
		    cancelValue: '<fmt:message key="fxl.button.cancel" />',
		    cancel: function (){
		    }
		}).showModal();
    });
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-10"><h3>运维人员信息维护</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>运维人员列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createUserBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> <fmt:message key="fxl.admin.user.createUserBtn" /></button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/admin/user/getUserList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_username">登录名：</label>
			<input type="text" id="s_username" name="s_username" class="form-control input-sm" ></div>
		<div class="form-group"><label for="s_realname">员工名称：</label>
			<input type="text" id="s_realname" name="s_realname" class="form-control input-sm" ></div>
		<div class="form-group"><label for="s_email">E-Mail：</label>
			<input type="text" id="s_email" name="s_email" class="form-control input-sm" ></div>
		<div class="form-group"><label for="s_status">员工状态：</label>
			<select id="s_status" name="s_status" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="statusItem" items="${statusMap}">
				<option value="${statusItem.key}">${statusItem.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group"><label for="s_category">员工类别：</label>
			<select id="s_category" name="s_category" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="categoryItem" items="${categoryMap}"> 
				<option value="${categoryItem.key}">${categoryItem.value}</option>
				</c:forEach>
			</select>
		</div>
		<p class="text-center"><button id="select" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="userTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
<!--用户信息维护modal开始-->
<div class="modal fade" id="createUserModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel"><fmt:message key="fxl.admin.user.addUser.title.a" /></h4>
      </div>
      <form id="createUserForm" action="${ctx}/fxl/admin/user/addUser" method="post" class="form-inline" role="form">
      <div class="modal-body">
      <div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
      <input id="id" name="id" type="hidden">
      	<div class="row">
			<div class="col-md-8">
				<div class="form-group has-error">
			    	<label for="username"><fmt:message key="fxl.admin.user.userList.table.username" />：</label>
			      	<input type="text" id="username" name="username" class="form-control validate[required,minSize[6],maxSize[100],custom[onlyLetterNumber],ajax[checkUsername]]" size="16">
			    </div>
				<div class="form-group has-error">			
				    <label for="realname"><fmt:message key="fxl.admin.user.userList.table.realname" />：</label>
				    <input type="text" id="realname" name="realname" class="form-control validate[required,minSize[2],maxSize[100]]" size="16">
			    </div>
			    <div id="passwordDiv">
				    <div class="form-group has-error">			
					    <label for="password"><fmt:message key="fxl.admin.user.password" />：</label>
				      	 <input type="password" id="password" name="password" class="form-control validate[required,minSize[6],maxSize[32]]" size="16">
				    </div>
					<div class="form-group has-error">
				    	<label for="password2"><fmt:message key="fxl.admin.user.password2" />：</label>
				    	<input type="password" id="password2" name="password2" class="form-control validate[required,equals[password]]" size="16">
				    </div>
			    </div>
			    <div class="form-group">
			    	<label for="phone"><fmt:message key="fxl.admin.user.userList.table.phone" />：</label>
			    	<input type="text" id="phone" name="phone" class="form-control validate[maxSize[100]]" size="16">
			    </div>
			    <div class="form-group">
			    	<label for="description">E-mail：</label>
			    	<input type="text" id="email" name="email" class="form-control validate[custom[email]]" size="16">
			    </div>
			    <div class="form-group has-error"><label for="category">员工类别：</label>
					<select id="category" name="category" class="form-control validate[required]" multiple="multiple" style="width: 420px;">
						<c:forEach var="categoryItem" items="${categoryMap}"> 
						<option value="${categoryItem.key}">${categoryItem.value}</option>
						</c:forEach>
					</select>
				</div>
			    <div class="form-group has-error">
			    	<label for="status"><fmt:message key="fxl.admin.user.userList.table.status" />：</label>
			    	<select id="status" name="status" class="form-control validate[required]">
						<option value=""><fmt:message key="fxl.common.select" /></option>
						<c:forEach var="statusItem" items="${statusMap}">
						<option value="${statusItem.key}">${statusItem.value}</option>
						</c:forEach>
					</select>
			    </div>
			</div>
			<div class="col-md-4">
				<div class="panel panel-info">
					<div class="panel-heading"><fmt:message key="fxl.admin.user.selectRole" /></div>
					<div class="panel-body">
						<ul id="userRoleTree" class="ztree"></ul>
					</div>
				</div>
			</div>
		</div>
      </div>
      <div class="modal-footer">
      	<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span> <fmt:message key="fxl.button.save" /></button>
		<button class="btn btn-warning" type="reset" id="resetBtn"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.reset" /></button>
		<button class="btn btn-warning" type="button" id="resetPasswordBtn"><span class="glyphicon glyphicon-qrcode"></span> <fmt:message key="fxl.admin.user.resetPasswordBtn" /></button>
		<button class="btn btn-danger" type="button" id="delUserBtn"><span class="glyphicon glyphicon-trash"></span> <fmt:message key="fxl.admin.user.deleteUserBtn" /></button>
        <button class="btn btn-default" type="button" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="fxl.button.close" /></button>
      </div>
      </form>
    </div>
  </div>
</div>
<!--用户信息维护Modal结束-->
</body>
</html>