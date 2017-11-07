<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/ztree/css/zTreeStyle.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="${ctx}/styles/ztree/js/jquery.ztree.all.min.js"></script>
<script type="text/javascript">
$(document).ready(function() {
	
	var zTreeObj;
	
	$.validationEngineLanguage.allRules.checkRolecode={
		"url" : '${ctx}/fxl/admin/role/checkRolecode',
		"alertTextOk" : '您可以使用该角色编码！',
	    "alertText" : '您输入的角色编码已被占用，请更换其它角色编码！',
	    "alertTextLoad" : '正在验证该角色编码是否被占用……'
	};
	
	$.validationEngineLanguage.allRules.checkEditRolecode={
		"url" : '${ctx}/fxl/admin/role/checkEditRolecode',
		"extraDataDynamic": ['#edit_id'],
		"alertTextOk" : '您可以使用该角色编码！',
	    "alertText" : '您输入的角色编码已被占用，请更换其它角色编码！',
	    "alertTextLoad" : '正在验证该角色编码是否被占用……'
	};
	
	var searchForm = $('#searchForm');
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#select").click(function(e) {
		roleTable.ajax.reload();
		//$("#selectDiv").slideUp("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	var createRoleModal =  $('#createRoleModal');
	
	createRoleModal.on('hide.bs.modal', function(e){
		createRoleForm.trigger("reset");
	});
	
	var createRoleForm = $('form#createRoleForm');
	
	createRoleForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	createRoleForm.on("submit", function(event){
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
				    toastr.warning('<fmt:message key="fxl.admin.role.addRole.mustSelectOnePerm"/>');
    				event.preventDefault();
    				return false;
    			}
    			$.each(nodes, function(i, n){
    				if(n.id != 0){
    					ids.push({name:"ids", value:n.id});
    				}
    			});
	    		$.post(form.attr("action"), $.param($.merge(form.serializeArray(),ids),true), function(data){
					if(data.success){
						toastr.success(data.msg);
						zTreeObj.cancelSelectedNode();
						roleTable.ajax.reload(null,false);
						createRoleModal.modal('hide');
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
    		}else{
    			toastr.warning('<fmt:message key="fxl.admin.role.addRole.createTreeError" />');
    		}
		}
	});
	
	createRoleForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createRoleForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		createRoleForm.validationEngine('hideAll');
	});

	$('#createRoleBtn').on('click',function(e) {
		$.post("${ctx}/fxl/admin/role/getModuleTree", function(data) {
			if(data.success){				
				if(zTreeObj){
					zTreeObj.destroy();
				}
				zTreeObj = $.fn.zTree.init($("#addRoleModelTree"), {
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
				createRoleModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	var roleTable = $("table#roleTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"paging": false,
		"autoWidth" : false,
	  	//"scrollY": "450px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/admin/role/getRoleList",
		    "type": "POST",
 		    "data": function (data) {
 		    	$.each(searchForm.serializeArray(), function(i, n){
 		    		data[n.name] = n.value;
 				});
 		        return data;
 		    }
		},
		"stateSave": true,
		"deferRender": true,
		"lengthChange": false,
		"columns" : [
		{	"title" : "ID",
			"data" : "DT_RowId",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.admin.role.roleList.table.name" /></center>',
			"data" : "name",
			"width" : "100px",
			"className": "text-center",
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.admin.role.roleList.table.code" /></center>',
			"data" : "code",
			"width" : "80px",
			"className": "text-center",
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.admin.role.roleList.table.description" /></center>',
			"data" : "description",
			"width" : "500px",
			"className": "text-center",
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.admin.role.roleList.table.userCount" /></center>',
			"data" : "userCount",
			"width" : "60px",
			"className": "text-center",
			"orderable" : true
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "100px",
			"className": "text-center",
			"orderable" : false
		} ],
		"columnDefs" : [ {
			"targets" : [ 5 ],
			"render" : function(data, type, full) {
				return '<button id="edit" mId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs"><fmt:message key="fxl.button.edit" /></button>';
			}
		}]
	});
	
	$("#roleTable").delegate("button[id=edit]", "click", function(){
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/admin/role/getRole/" + mId, function(data) {
			if(data.success){
				$("#edit_id").val(mId);
				$("#edit_name").val(data.name);
				$("#edit_code").val(data.code);
				$("#edit_description").val(data.description);
				if(zTreeObj){
					zTreeObj.destroy();
				}
				zTreeObj = $.fn.zTree.init($("#editRoleModelTree"), {
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
				editRoleModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	var editRoleModal =  $('#editRoleModal');
	
	editRoleModal.on('hide.bs.modal', function(e){
		editRoleForm.trigger("reset");
	});
	
	var editRoleForm = $('form#editRoleForm');
	
	editRoleForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	editRoleForm.on("submit", function(event){
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
    				toastr.warning('<fmt:message key="fxl.admin.role.addRole.mustSelectOnePerm"/>');
    				event.preventDefault();
    				return false;
    			}
    			$.each(nodes, function(i, n){
    				if(n.id != 0){
    					ids.push({name:"edit_ids", value:n.id});
    				}
    			});
    			$.post(form.attr("action"), $.param($.merge(form.serializeArray(),ids),true), function(data){
					if(data.success){
						toastr.success(data.msg);
						zTreeObj.cancelSelectedNode();
						roleTable.ajax.reload(null,false);
						editRoleModal.modal('hide');
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
    		}else{
    			toastr.warning('<fmt:message key="fxl.admin.role.addRole.createTreeError" />');
    		}
		}
	});
	
	editRoleForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',editRoleForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		editRoleForm.validationEngine('hideAll');
	});
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-10"><h3>系统角色维护</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4><fmt:message key="fxl.admin.role.title.b" /></h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createRoleBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> <fmt:message key="fxl.admin.role.createRoleBtn" /></button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/admin/role/getRoleList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_name"><fmt:message key="fxl.admin.role.roleList.table.name" />：</label>
			<input type="text" id="s_name" name="s_name" class="form-control input-sm" ></div>
		<div class="form-group"><label for="s_code"><fmt:message key="fxl.admin.role.roleList.table.code" />：</label>
			<input type="text" id="s_code" name="s_code" class="form-control input-sm" ></div>
		<p class="text-center"><button id="select" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="roleTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
<!--创建角色modal开始-->
<div class="modal fade" id="createRoleModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel"><fmt:message key="fxl.admin.role.addRole.title.a" /></h4>
      </div>
      <form id="createRoleForm" action="${ctx}/fxl/admin/role/addRole" method="post" role="form">
      <div class="modal-body">
      <div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
      	<div class="row">
			<div class="col-md-7">
				<div class="form-group has-error"><label for="name"><fmt:message key="fxl.admin.role.roleList.table.name" />：</label>
			      	<input type="text" id="name" name="name" class="form-control validate[required,maxSize[30]]">
			    </div>
			    <div class="form-group has-error"><label for="code"><fmt:message key="fxl.admin.role.roleList.table.code" />：</label>
			      	<input type="text" id="code" name="code" class="form-control validate[required,maxSize[30],custom[onlyLetterNumber],ajax[checkRolecode]]">
			      	<p class="help-block">系统内部对角色失败的唯一标识</p>
			    </div>
			    <div class="form-group">
			    	<label for="description"><fmt:message key="fxl.admin.role.roleList.table.description" />：</label>
			    	<textarea id="description" name="description" cols="60" rows="6" class="form-control validate[maxSize[200]]"></textarea>
			    </div>
			</div>
			<div class="col-md-5">
				<div class="panel panel-info">
					<div class="panel-heading"><fmt:message key="fxl.admin.role.selectModule" /></div>
					<div class="panel-body">
						<ul id="addRoleModelTree" class="ztree"></ul>
					</div>
				</div>
			</div>
		</div>
      </div>
      <div class="modal-footer">
      	<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span> <fmt:message key="fxl.button.save" /></button>
		<button class="btn btn-warning" type="reset" id="resetBtn"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.reset" /></button>
        <button class="btn btn-default" type="button" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="fxl.button.close" /></button>
      </div>
      </form>
    </div>
  </div>
</div>
<!--创建角色Modal结束-->
<!--编辑角色modal开始-->
<div class="modal fade" id="editRoleModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel"><fmt:message key="fxl.admin.role.editRole.title.a" /></h4>
      </div>
      <form id="editRoleForm" action="${ctx}/fxl/admin/role/editRole" method="post" role="form">
      <input id="edit_id" name="edit_id" type="hidden">
      <div class="modal-body">
      <div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
      	<div class="row">
			<div class="col-md-7">
				<div class="form-group has-error"><label for="edit_name"><fmt:message key="fxl.admin.role.roleList.table.name" />：</label>
			      	<input type="text" id="edit_name" name="edit_name" class="form-control validate[required,maxSize[30]">
			    </div>
			    <div class="form-group has-error"><label for="edit_code"><fmt:message key="fxl.admin.role.roleList.table.code" />：</label>
			      	<input type="text" id="edit_code" name="edit_code" class="form-control validate[required,maxSize[30],,custom[onlyLetterNumber],ajax[checkEditRolecode]]">
			    	<p class="help-block">系统内部对角色失败的唯一标识</p>
			    </div>
			    <div class="form-group">
			    	<label for="edit_description"><fmt:message key="fxl.admin.role.roleList.table.description" />：</label>
			    	<textarea id="edit_description" name="edit_description" cols="60" rows="6" class="form-control validate[maxSize[200]]"></textarea>
			    </div>
			</div>
			<div class="col-md-5">
				<div class="panel panel-info">
					<div class="panel-heading"><fmt:message key="fxl.admin.role.selectModule" /></div>
					<div class="panel-body">
						<ul id="editRoleModelTree" class="ztree"></ul>
					</div>
				</div>
			</div>
		</div>
      </div>
      <div class="modal-footer">
      	<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span> <fmt:message key="fxl.button.save" /></button>
		<button class="btn btn-default" type="button" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="fxl.button.close" /></button>
      </div>
      </form>
    </div>
  </div>
</div>
<!--编辑角色Modal结束-->
</body>
</html>