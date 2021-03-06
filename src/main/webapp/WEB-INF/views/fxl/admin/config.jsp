<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<script type="text/javascript">
$(document).ready(function () {
	
	$.validationEngineLanguage.allRules.checkCnName={
		"url" : '${ctx}/fxl/admin/config/checkCnName',
		"extraDataDynamic": ['#classId',"#id"],
		"alertTextOk" : '您可以使用该字典项中文名！',
	    "alertText" : '您输入的字典项中文名已被占用，请更换其它字典项中文名！',
	    "alertTextLoad" : '正在验证该字典项中文名是否被占用……'
	};
	
	$.validationEngineLanguage.allRules.checkEnName={
		"url" : '${ctx}/fxl/admin/config/checkEnName',
		"extraDataDynamic": ['#classId',"#id"],
		"alertTextOk" : '您可以使用该字典项英文名！',
	    "alertText" : '您输入的字典项英文名已被占用，请更换其它字典项英文名！',
	    "alertTextLoad" : '正在验证该字典项英文名是否被占用……'
	};
	
	$.validationEngineLanguage.allRules.checkValue={
		"url" : '${ctx}/fxl/admin/config/checkValue',
		"extraDataDynamic": ['#classId',"#id"],
		"alertTextOk" : '您可以使用该字典项值！',
	    "alertText" : '您输入的字典项值已被占用，请更换其它字典项值！',
	    "alertTextLoad" : '正在验证该字典项值是否被占用……'
	};

	var searchForm = $('form#searchForm');
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#select").click(function(e) {
		configTable.ajax.reload();
		//$("#selectDiv").slideUp("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	var configTable = $("table#configTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"paging": false,
		"autoWidth" : false,
	  	//"scrollY": "450px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/admin/config/getConfigList",
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
			"title" : '<center><fmt:message key="fxl.admin.config.addConfig.table.name" /></center>',
			"data" : "name",
			"width" : "200px",
			"className": "text-center",
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.admin.config.addConfig.table.code" /></center>',
			"data" : "code",
			"width" : "300px",
			"className": "text-center",
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "100px",
			"className": "text-center",
			"orderable" : false
		} ],
		"columnDefs" : [ {
			"targets" : [ 3 ],
			"render" : function(data, type, full) {
				return '<button id="edit" mId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs"><fmt:message key="fxl.admin.config.addConfig.table.edit" /></button>';
			}
		}]
	});
	
	$("#configTable").delegate("button[id=edit]", "click", function(){
		$("#classId").val($(this).attr("mId"));
		configItemTable.ajax.reload(null,false);
		editConfigModal.modal('show');
	});
	
	var editConfigModal =  $('#editConfigModal');
	
	editConfigModal.on('hide.bs.modal', function(e){
		addConfigItemForm.trigger("reset");
	});

	var addConfigItemForm = $('form#addConfigItemForm');
	
	addConfigItemForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	addConfigItemForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			$.post(form.attr("action"), form.serialize(), function(data){
				if(data.success){
					toastr.success(data.msg);
					configItemTable.ajax.reload(null,false);
					form.trigger("reset");
				}else{
					toastr.error(data.msg);
				}
				form.removeData("running");
			}, "json");
		}
	});
	
	addConfigItemForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$("#id").val("");
		$("#name").val("");
		$("#code").val("");
		$("#value").val("");
		addConfigItemForm.validationEngine('hideAll');
	});

	var configItemTable = $("table#configItemTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"paging": false,
		"autoWidth" : false,
	  	//"scrollY": "350px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/admin/config/getConfigItemList",
		    "type": "POST",
 		    "data": function (data) {
 		    	data["classId"] = $("#classId").val();
 		        return data;
 		    }
		},
		"stateSave": true,
		"deferRender": true,
		"lengthChange": false,
		"retrieve": true,
		"columns" : [
		{	"title" : "ID",
			"data" : "DT_RowId",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.admin.config.configItem.table.name" /></center>',
			"data" : "name",
			"width" : "200px",
			"className": "text-center",
			"orderable" : true
		}, {
			"title" : '<center><fmt:message key="fxl.admin.config.configItem.table.code" /></center>',
			"data" : "code",
			"width" : "200px",
			"className": "text-center",
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.admin.config.configItem.table.value" /></center>',
			"data" : "value",
			"width" : "60px",
			"className": "text-center",
			"orderable" : true
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "80px",
			"className": "text-center",
			"orderable" : false
		} ],
		"columnDefs" : [ {
			"targets" : [ 4 ],
			"render" : function(data, type, full) {
				return '<button id="edit" mId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs"><fmt:message key="fxl.button.edit" /></button>&nbsp;<button id="delete" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" /></button>';
			}
		}]/* ,
		"drawCallback": function(settings){
			config_edit_modal.modal('refresh');
	    } */
	});
	
	$("#configItemTable").delegate("button[id=edit]", "click", function(){
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/admin/config/getConfigItem/" + mId, function(data) {
			if(data.success){
				$("#id").val(mId);
				$("#name").val(data.name);
				$("#code").val(data.code);
				$("#value").val(data.value);
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	$("#configItemTable").delegate("button[id=delete]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '<fmt:message key="fxl.admin.config.configItem.delete.confim" />',
		    okValue: '<fmt:message key="fxl.button.ok" />',
		    ok: function () {
		    	$.post("${ctx}/fxl/admin/config/delConfigItem/" + mId, function(data) {
					if(data.success){
						toastr.success(data.msg);
						configItemTable.ajax.reload(null,false);
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
<div class="row">
  <div class="col-md-10"><h3>数据字典维护</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4><fmt:message key="fxl.admin.config.title.b" /></h4></div>
  <div class="col-md-10"><p class="text-right"><button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/admin/config/getConfigList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_name">数据字典名称：</label>
			<input type="text" id="s_name" name="s_name" class="form-control input-sm" ></div>
		<p class="text-center"><button id="select" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="configTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
<!--维护数据字典modal开始-->
<div class="modal fade" id="editConfigModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
	aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="myModalLabel">
					<fmt:message key="fxl.admin.config.configItem.modal.title" />
				</h4>
			</div>
			<div class="modal-body">
				<div class="alert alert-danger text-center" role="alert" style="padding: 5px;">
					<strong><fmt:message key="fxl.common.redRequired" /></strong>
				</div>
				<form id="addConfigItemForm" class="form-inline" role="form" action="${ctx}/fxl/admin/config/addConfigItem" method="post">
					<input type="hidden" id="classId" name="classId">
					<input type="hidden" id="id" name="id">
					<div class="form-group has-error"><label for="name"><fmt:message key="fxl.admin.config.configItem.table.name" />：</label>
					<input type="text" id="name" name="name" class="form-control validate[required,maxSize[100],ajax[checkCnName]]" ></div>
					<div class="form-group has-error"><label for="code"><fmt:message key="fxl.admin.config.configItem.table.code" />：</label>
					<input type="text" id="code" name="code" class="form-control  validate[required,maxSize[100],ajax[checkEnName]]" ></div>
					<div class="form-group has-error"><label for="value"><fmt:message key="fxl.admin.config.configItem.table.value" />：</label>
					<input type="text" id="value" name="value" class="form-control validate[required,custom[integer],min[1],max[999999999],ajax[checkValue]]" ></div>
					<button class="btn btn-primary" type="submit">
						<span class="glyphicon glyphicon-floppy-saved"></span>
						<fmt:message key="fxl.button.save" />
					</button>
					<button class="btn btn-warning" type="reset">
						<span class="glyphicon glyphicon-repeat"></span>
						<fmt:message key="fxl.button.reset" />
					</button>
				</form>
				<table id="configItemTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
			</div>
			<div class="modal-footer">
				<button class="btn btn-default" type="button" data-dismiss="modal">
					<span class="glyphicon glyphicon-remove"></span><fmt:message key="fxl.button.close" />
				</button>
			</div>
		</div>
	</div>
</div>
<!--维护数据字典Modal结束-->
</body>
</html>