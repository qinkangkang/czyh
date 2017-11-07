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
		    "url": "${ctx}/fxl/report/getSceneMapList",
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
			"title" : '<center>地推码</center>',
			"data" : "fsceneCode",
			"width" : "50px",
			"className": "text-center",
			"orderable" : true
		},{
			"title" : '<center>地推人员</center>',
			"data" : "fuserId",
			"width" : "50px",
			"className": "text-center",
			"orderable" : false
		}, {
			"title" : '<center>状态</center>',
			"data" : "fstatus",
			"width" : "50px",
			"className": "text-center",
			"orderable" : false
		}, {
			"title" : '<center>创建时间</center>',
			"data" : "createTime",
			"width" : "50px",
			"className": "text-center",
			"orderable" : true
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "50px",
			"className": "text-center",
			"orderable" : false
		} ],
		"columnDefs" : [ {
			"targets" : [ 5 ],
			"render" : function(data, type, full) {
				return '<button id="edit" mId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs"><fmt:message key="fxl.button.edit" /></button>';
			}
		} ]
	});
	
	
	$("#userTable").delegate("button[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/report/getSceneUserMap/" + mId, function(data) {
			if(data.success){
				$("#id").val(mId);
				$("#fSceneCode").val(data.sceneCode);
				$("#status option[value='" + data.status + "']").prop("selected",true);
				$("#user option[value='" + data.user + "']").prop("selected",true);
				createUserModal.modal('show')
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});

	var createUserModal =  $('#createUserModal');
	
	createUserModal.on('hide.bs.modal', function(e){
		createUserForm.trigger("reset");
		//category.val(null).trigger("change");
	});

	$('#createUserBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		createUserModal.modal('show');
	});
	
	var createUserForm = $('form#createUserForm');
	
	createUserForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			$.post(form.attr("action"), form.serialize() , function(data){
				if(data.success){
					$("#bid").val(data.id);
					console.log(data.id);
					toastr.success(data.msg);
					userTable.ajax.reload(null,false);
					createUserModal.modal("hide");
				}else{
					toastr.error(data.msg);
				}
				form.removeData("running");
			}, "json");
		}
	});
	
	createUserForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createUserForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		createUserForm.validationEngine('hideAll');
	});
	
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-10"><h3>地推码和人员维护</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>地推码列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createUserBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 新增</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/report/getSceneMapList" method="post" class="form-inline" role="form">
		
		<div class="form-group"><label for="s_realname">地推人员：</label>
			<select id="s_user" name="s_user" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="sceneItem" items="${sceneMap}">
				<option value="${sceneItem.key}">${sceneItem.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group"><label for="fsceneCode">地推码：</label>
			<input type="text" id="s_fsceneCode" name="s_fsceneCode" class="form-control input-sm" ></div>
		<div class="form-group"><label for="s_status">状态：</label>
			<select id="s_status" name="s_status" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<option value="1">正常</option>
				<option value="0">停用</option>
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
        <h4 class="modal-title" id="myModalLabel">新增地推人员与地推码映射</h4>
      </div>
      <form id="createUserForm" action="${ctx}/fxl/report/addScene" method="post" class="form-inline" role="form">
      <div class="modal-body">
      <div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
      <input id="id" name="id" type="hidden">
      	<div class="row">
			<div class="col-md-8">
				
				<div class="form-group has-error">			
				    <label for="fSceneCode">地推码：</label>
				    <input type="text" id="fSceneCode" name="fSceneCode" class="form-control validate[required,minSize[2],maxSize[100]]" size="16">
			    </div>
			 
		
			    <div class="form-group"><label for="fUserId">地推人员：</label>
					<select id="user" name="user" class="form-control input-sm">
						<option value=""><fmt:message key="fxl.common.select" /></option>
						<c:forEach var="sceneItem" items="${sceneMap}">
						<option value="${sceneItem.key}">${sceneItem.value}</option>
						</c:forEach>
					</select>
				</div>
			    <div class="form-group has-error">
			    	<label for="status">状态：</label>
			    	<select id="status" name="status" class="form-control validate[required]">
						<option value=""><fmt:message key="fxl.common.select" /></option>
						<option value="1">正常</option>
						<option value="0">停用</option>
					</select>
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
<!--用户信息维护Modal结束-->
</body>
</html>