<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<%-- <link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css"> --%>
<link href="${ctx}/styles/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2.min.css" rel="stylesheet" type="text/css">
<%-- <script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js" charset="UTF-8"></script> --%>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js" charset="UTF-8"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/select2.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/i18n/zh-CN.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	$.cookie('fxlUsername', '<shiro:principal type="com.czyh.czyhweb.security.ShiroDbRealm$ShiroUser" property="loginName"></shiro:principal>',{expires:30});
	
	var searchForm = $('#searchForm');
	
	searchForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	$('#createDateDiv').datetimepicker({
	    format : "yyyy-mm-dd hh:ii",
	    endDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	$('#create2DateDiv').datetimepicker({
	    format : "yyyy-mm-dd hh:ii",
	    endDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
    });
	
	 searchForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			sceneUserTable.ajax.reload(null,false);

			form.removeData("running");
		}
	}); 
	 
	 var sceneUserTable = $("table#sceneUserTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/report/getReportSceneUserGatherList",
 		    "type": "POST",
 		    "data": function (data) {
 		    	$.each(searchForm.serializeArray(), function(i, n){
 		    		data[n.name] = n.value;
 				});
 		        return data;
 		    }
		},
		"stateSave": false,
		"deferRender": true,
		//"pagingType": "full_numbers",
		"lengthMenu": [[10, 20, 30, 50], [10, 20, 30, 50]],
		"lengthChange": false,
		"displayLength" : datatablePageLength,
		"columns" : [
   		{
   			"title" : '<center>渠道码</center>',
   			"data" : "sceneStr",
   			"className": "text-center",
   			"width" : "50px",
   			"orderable" : false
   		},{
   			"title" : '<center>公众号关注数</center>',
   			"data" : "subscribeNum",
   			"className": "text-center",
   			"width" : "50px",
   			"orderable" : false
   		}, {
   			"title" : '<center>授权地理信息数</center>',
   			"data" : "gpsNum",
   			"className": "text-center",
   			"width" : "100px",
   			"orderable" : false
   		}, {
   			"title" : '<center>领奖数</center>',
   			"data" : "registerNum",
   			"className": "text-center",
   			"width" : "100px",
   			"orderable" : false
   		}, {
   			"title" : '<center>当日取消数</center>',
   			"data" : "todayUnRegisterNum",
   			"className": "text-center",
   			"width" : "100px",
   			"orderable" : false
   		}, {
   			"title" : '<center>当日留存</center>',
   			"data" : "todaysubscribe",
   			"className": "text-center",
   			"width" : "100px",
   			"orderable" : false
   		}, {
   			"title" : '<center>取消关注数</center>',
   			"data" : "unRegisterNum",
   			"className": "text-center",
   			"width" : "100px",
   			"orderable" : false
   		}, {
   			"title" : '<center>历史留存</center>',
   			"data" : "hRegisterNum",
   			"className": "text-center",
   			"width" : "100px",
   			"orderable" : false
   		}, {
   			"title" : '<center>扫码总数</center>',
   			"data" : "sweepNum",
   			"className": "text-center",
   			"width" : "100px",
   			"orderable" : false
   		} ] ,
   		"columnDefs" : []
	});
	
	var exportProgressModal =  $('#exportProgressModal');
	
	var downFlag = false;
	$("#exportExcelBtn").click(function(e){
		e.stopPropagation();
		var exportExcelBtn = $(this);
		if(exportExcelBtn.data("running") != "ok"){
			exportExcelBtn.data("running","ok");
			downFlag = true;
			var mId = $(this).attr("mId");
			dialog({
				fixed: true,
			    title: '操作提示',
			    content: '您确定要导出当前地推汇总列表的EXCEL文档吗？',
			    okValue: '导出',
			    ok: function () {
			    	exportProgressModal.modal('show');
			    	$.post("${ctx}/fxl/report/createSceneGatherExcel" , searchForm.serializeArray(), function(data) {
						if(data.success){
							exportProgressModal.modal('hide');
							if(downFlag){
								$("#downFile").attr("src","${ctx}/fxl/report/exportGatherExcel/" + data.datePath + "/" + data.excelFileName);
							}
						}else{
							toastr.error(data.msg);
						}
						exportExcelBtn.removeData("running");
					}, "json");
			    },
			    cancelValue: '<fmt:message key="fxl.button.cancel" />',
			    cancel: function (){
			    }
			}).showModal();
		}
	});
	
	$("#exportStopBtn").click(function(e){
		e.stopPropagation();
		downFlag = false;
		exportProgressModal.modal('hide');
	});
	

});
</script>
</head>
<body>
<div class="row">
  <div class="col-md-10"><h3>地推报表</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;">
  <button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p>
  <p class="text-right" style="margin:10px 0 0 0;"><button id="exportExcelBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-list-alt"></span> 导出EXCEL</button></p></div>
</div>
<div id="selectDiv" class="text-center">
	<form id="searchForm" action="${ctx}/fxl/report/getReportSceneUserGatherList" method="post" class="form-inline" role="form">
		<div class="form-group">
			<label for="fdeadline">选择统计区间：</label>
			<div id="createDateDiv" class="input-group date form_datetime">
				<input id="fdeadline" name="fcreateTimeStart" type="text" class="form-control" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
			</div>
			<div id="create2DateDiv" class="input-group date form_datetime">
				<span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
				<input id="fdeadline" name="fcreateTimeEnd" type="text" class="form-control" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
			</div>
		</div>
		<div class="form-group"><label for="s_realname">地推人员：</label>
			<select id="user" name="user" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="sceneItem" items="${sceneMap}">
				<option value="${sceneItem.key}">${sceneItem.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group"><label for="s_ftype">渠道码：</label>
			<select id="" name="sceneStr" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="sceneStr" items="${sceneStrList}" > 
					<option value="${sceneStr }">${sceneStr }</option>
				</c:forEach>
			</select>
		</div>
		<p class="text-center"><button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
	<table id="sceneUserTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
</div>
<!--订单详情结束-->
<!--图片上传进度条开始-->
<div class="modal fade" id="exportProgressModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <h4 class="modal-title" id="myModalLabel">请稍等，后台正在生成EXCEL文档……</h4>
      </div>
      <div class="modal-body">
      	<div class="progress">
			<div id="exportProgress" class="progress-bar progress-bar-info progress-bar-striped active" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%"></div>
		</div>
      </div>
      <div class="modal-footer">
		<button id="exportStopBtn" type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> 取消生成</button>
      </div>
      </form>
    </div>
  </div>
</div>
<!--图片上传进度条结束-->
<iframe id="downFile" height="0" width="0" style="display: none;"></iframe>
</body>
</html>