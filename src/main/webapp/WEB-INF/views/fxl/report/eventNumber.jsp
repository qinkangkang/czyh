<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2.min.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="${ctx}/styles/select2/js/select2.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/i18n/zh-CN.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	$.cookie('fxlUsername', '<shiro:principal type="com.czyh.czyhweb.security.ShiroDbRealm$ShiroUser" property="loginName"></shiro:principal>',{expires:30});
	
	$.post("${ctx}/fxl/report/getEventNumber", function(data){
		if(data.success){
			var tbody = $("#dataTable").find("tbody");
			tbody.empty();
			var tdHtml = "";
			var dataList = data.list;
			for (var i = 0;i < dataList.length; i++) {
				tdHtml+= " <tr ><th>" + dataList[i].title + "</th><th>" + dataList[i].onSaleCount + "</th><th>" + 
				dataList[i].offSaleCount + "</th><th>" + dataList[i].count + "</th></tr>";
			}
			tbody.append(tdHtml);
		}else{
			toastr.error(data.msg);
		}
		/* form.removeData("running"); */
	}, "json");
	
	var searchForm = $('#searchForm');
	
	searchForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	/* $('#createDateDiv').datepicker({
	    format : "yyyy/mm/dd",
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	}); */
	 var date = new Date();
	$('#time').val(date.toLocaleDateString());
	
	$("#select").on("click" ,function(event){
		$.post("${ctx}/fxl/report/getEventNumber", function(data){
			if(data.success){
				var tbody = $("#dataTable").find("tbody");
				tbody.empty();
				var tdHtml = "";
				var dataList = data.list;
				for (var i = 0;i < dataList.length; i++) {
					tdHtml+= " <tr ><th>" + dataList[i].title + "</th><th>" + dataList[i].onSaleCount + "</th><th>" + 
					dataList[i].offSaleCount + "</th><th>" + dataList[i].count + "</th></tr>";
				}
				tbody.append(tdHtml);
			}else{
				toastr.error(data.msg);
			}
		}, "json");
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
			    content: '您确定要导出现有活动列表的EXCEL文档吗？',
			    okValue: '导出',
			    ok: function () {
			    	exportProgressModal.modal('show');
			    	$.post("${ctx}/fxl/report/createEventExcel" , searchForm.serializeArray(), function(data) {
						if(data.success){
							exportProgressModal.modal('hide');
							if(downFlag){
								$("#downFile").attr("src","${ctx}/fxl/report/exportEventExcel/" + data.datePath + "/" + data.excelFileName);
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
  <div class="col-md-10"><h3>活动数量</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p>
   <p class="text-right" style="margin:10px 0 0 0;"><button id="exportExcelBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-list-alt"></span> 导出EXCEL</button></p></div>
</div>
<div class="selectDiv">
	<form id="searchForm" action="${ctx}/fxl/report/getEventNumber" method="post" class="form-inline" role="form">
		<div class="form-group">
			<label>选择统计区间：</label>
			<div class="input-daterange input-group date" id="createDateDiv" style="width:100px;">
				<input id="time" disabled="disabled" type="text" class="form-control input-sm  form-control" name="fcreateTimeEnd" style="cursor: pointer;">
			</div>
		</div>
		<button id="select" type="button" class="btn btn-primary">
		<span class="glyphicon glyphicon-play"></span>
		<fmt:message key="fxl.button.query" /></button>
	</form>
	<table id="dataTable" class="table table-striped">
	   <thead>
	      <tr>
	         <th>类目</th>
	         <th>上架数量</th>
	         <th>下架数量</th>
	         <th>待上架数量</th>
	      </tr>
	   </thead>
	   <tbody>
	   </tbody>
	</table>
</div>
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