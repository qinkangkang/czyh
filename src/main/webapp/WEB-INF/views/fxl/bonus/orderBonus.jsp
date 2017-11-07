<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2-bootstrap.min.css" rel="stylesheet" type="text/css">

<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js" charset="UTF-8"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/select2.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/i18n/zh-CN.js"></script>
<script type="text/javascript">
$(document).ready(function() {
		
	var feventSelect = $("#s_ftitle").select2({
		placeholder: "选择一个商品名称",
		allowClear: true,
		language: pickerLocal
	});
	
	$('#createDateDiv').datepicker({
	    format : "yyyy-mm-dd",
	    endDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	$('#fdateDiv').datepicker({
	    format : "yyyy-mm-dd",
	    //startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	$("#fuseDateDiv").datetimepicker({
	    format : "yyyy-mm-dd hh:ii",
	    //startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true,
	    pickerPosition: "top-left"
	});
	
	var searchForm = $('form#searchForm');
	
	searchForm.submit(function(e) {
		e.preventDefault();
		orderBonusTable.ajax.reload();
	});
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		feventSelect.val(null).trigger("change");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	
	var orderBonusTable = $("table#orderBonusTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/bonus/getOrdeBonusList",
 		    "type": "POST",
 		    "data": function (data) {
  		    	data["eventStartOffsetKey"] = "eventViewStartOffset";
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
			"title" : '<center>序号</center>',
			"data" : "rank",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		},{
			"title" : '<center>活动名称</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "150px",
			"orderable" : false
		}, {
			"title" : '<center>消耗积分</center>',
			"data" : "fbonus",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>订单金额</center>',
			"data" : "ftotal",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>微信名</center>',
			"data" : "wxname",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>用户手机号</center>',
			"data" : "phone",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		},{
			"title" : '<center>订单状态</center>',
			"data" : "fstatusString",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "20px",
			"className": "text-center operation",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [8],
			"render" : function(data, type, full) {
				var retString = '';
				if(data.fstatus == 10){
					retString =  '<button id="view" mId="' + full.DT_RowId + '" type="button" class="btn btn-success btn-xs">查看</button>'
					+ '<button id="verification" mId="' + full.DT_RowId + '" type="button" class="btn btn-warning btn-xs">兑换</button>'
					+ '<button id="cancel" mId="' + full.DT_RowId + '" type="button" class="btn btn-info btn-xs">取消</button>';
				}else{
					retString =  '<button id="view" mId="' + full.DT_RowId + '" type="button" class="btn btn-success btn-xs">查看</button>';
				}
				return retString;
			}
		}]
	});
	
	$("#orderBonusTable").delegate("button[id=verification]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '确认兑换',
		    content: '客服回复内容：<textarea id="property-returnValue-demo" value="" height = "100px"/>',
		    height:70,
		    width:200,
		    button: [
					 {
			value: '兑换',
	            callback: function () {
	            	$.post("${ctx}/fxl/bonus/verOrderBonus/" + mId , function(data) {
						if(data.success){
							toastr.success(data.msg);
							orderBonusTable.ajax.reload(null,false);
						}else{
							toastr.error(data.msg);
						}
					}, "json");
			    }
					 },
					 
				 {
			value: '兑换并push',
	            callback: function () {
	            	var value = $('#property-returnValue-demo').val();
	            	if($.trim(value)==''){
	            		toastr.warning("请填写客服回复内容");
	            		return false;
	            	}
	            	$.post("${ctx}/fxl/bonus/verOrderBonusPush/" + mId +"/"+value, function(data) {
						if(data.success){
							toastr.success(data.msg);
							orderBonusTable.ajax.reload(null,false);
						}else{
							toastr.error(data.msg);
						}
					}, "json");
			    },
		        },
		        
		        {
		            value: '关闭'
		        }     
		        
		    ]
		}).showModal();
	});
	
	$("#orderBonusTable").delegate("button[id=cancel]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要取消该兑换订单吗？',
		    okValue: '确定',
		    ok: function () {
		    	$.post("${ctx}/fxl/bonus/cancelBonusOrder/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						orderBonusTable.ajax.reload(null,false);
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
	
	$("#orderBonusTable").delegate("button[id=view]", "click", function(){
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/bonus/getBonusOrder/" + mId, function(data) {
			if(data.success){
				$("#fbonus").text(data.fbonus);
				$("#feventTitle").text(data.feventTitle);
				$("#customerName").text(data.customerName);
				$("#customerPhone").text(data.customerPhone);
				$("#fstatusString").text(data.fstatusString);
				$("#address").text(data.address);
				$("#remark").text(data.remark);
				$("#note").text(data.note);
				$("#fbonusPrice").text(data.fbonusPrice);
				
				orderDetailModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	var orderDetailModal =  $('#orderDetailModal');
    
	$("#orderBonusTable").delegate("tbody tr[id]", "click", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
	    var tr = $(this);
	    var row = orderBonusTable.row(tr);
	    if(tr.data("initOk") != "ok"){
	    	var csRemark = "";
	    	if(row.data().fcsRemark != null){
	    		csRemark = row.data().fcsRemark;
	    	}
	    	var childDiv = $("<div class='alert alert-info' role='alert' style='margin-bottom:0px;'></div>").html("<span class='glyphicon glyphicon-hand-up' aria-hidden='true'></span> <strong>客服备注：</strong><div id='csRemarkDiv_" + row.data().DT_RowId + "'>"
	    			+ csRemark
	    			+ '</div><div class="row"><div class="col-md-12"><div class="input-group"><input type="text" id="csremark_' + row.data().DT_RowId + '" mId="' + row.data().DT_RowId + '" class="form-control validate[required,maxSize[250]]" size="200"><div class="input-group-addon">回车即可上传</div></div></div></div>');
			row.child(childDiv);
			childDiv.closest("td").css("white-space","pre-line");
			tr.data("initOk","ok");
		}
	    if(row.child.isShown()){
	        row.child.hide();
	    }else{        	
	        row.child.show();
	    }
	});
	
	$("#orderBonusTable").on('click', 'td.operation', function (event) {
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
	    return false;
	});
	
	$("#orderBonusTable").delegate("input[id^=csremark_]", "keypress", function(event){
		var csremarkInput = $(this);
		if(event.keyCode == "13" && csremarkInput.data("running") != "ok"){
			csremarkInput.data("running","ok");
			var mId = csremarkInput.attr("mId");
			if(!csremarkInput.validationEngine('validate')){
		    	$.post("${ctx}/fxl/bonus/saveCsRemark", $.param({id:mId,csRemark:csremarkInput.val()},true), function(data) {
					if(data.success){
						toastr.success(data.msg);
						$('#csRemarkDiv_' + mId).text(data.newCsRemark);
						csremarkInput.val("");
					}else{
						toastr.error(data.msg);
					}
					csremarkInput.removeData("running");
				}, "json");
			}
		}
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
			    content: '您确定要导出现有积分兑换订单列表的EXCEL文档吗？',
			    okValue: '导出',
			    ok: function () {
			    	exportProgressModal.modal('show');
			    	$.post("${ctx}/fxl/bonus/createBonusOrderExcel" , searchForm.serializeArray(), function(data) {
						if(data.success){
							exportProgressModal.modal('hide');
							if(downFlag){
								$("#downFile").attr("src","${ctx}/fxl/bonus/bonusOrderExcel/" + data.datePath + "/" + data.excelFileName);
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
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-8"><h3>订单管理</h3></div>
  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>订单列表</h4></div>
  <div class="col-md-10"><p class="text-right">
  	<button id="exportExcelBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-list-alt"></span> 导出EXCEL</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button>
    </p></div>
</div>
<div id="selectDiv" class="text-center">
	<form id="searchForm" action="${ctx}/fxl/event/getEventList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_ftitle">商品名称：</label>
	        <select id="s_ftitle" name="s_ftitle" class="form-control input-sm" style="width: 220px;">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="bonusEventItem" items="${bonusEventMap}"> 
				<option value="${bonusEventItem.key}">${bonusEventItem.value}</option>
				</c:forEach>
			</select>
	    </div>
		<div class="form-group"><label for="s_name">用户名：</label>
			<input type="text" id="s_ftitle" name="s_name" class="form-control input-sm" >
		</div>
		
		<div class="form-group"><label for="s_status">订单状态：</label>
			<select id="s_status" name="s_status" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="statusItem" items="${orderBonusType}"> 
				<option value="${statusItem.key}">${statusItem.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group"><label>兑换时间：</label>
		<div class="input-daterange input-group date" id="createDateDiv" style="width:330px;">
		    <input type="text" class="form-control input-sm" name="fcreateTimeStart" style="cursor: pointer;">
		    <span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
		    <input type="text" class="form-control input-sm" name="fcreateTimeEnd" style="cursor: pointer;">
		</div></div>
		<p class="text-center"><button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="orderBonusTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>

<!--订单详情开始-->
<div class="modal fade" id="orderDetailModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="myModalLabel">订单详情</h4>
			</div>
			<div class="modal-body">
				<div class="panel panel-info">
					<div class="panel-heading"><strong>订单信息</strong></div>
					<div class="panel-body">
						<table class="table table-hover table-striped table-bordered">
							<tr>
								<td width="10%" align="right"><strong>活动名称</strong></td>
								<td width="23%"><div id="feventTitle"></div></td>
								<td width="10%" align="right"><strong>订单状态</strong></td>
								<td width="23%"><div id="fstatusString"></div></td>
								<td width="10%" align="right"><strong>消耗积分</strong></td>
								<td width="24%"><div id="fbonus"></div></td>
							</tr>
							<tr>
							    <td align="right"> <strong>金额</strong></td>
								<td><div id="fbonusPrice"></div></td>
								<td align="right"><strong>收货人</strong></td>
								<td><div id="customerName"></div></td>
								<td align="right"><strong>联系电话</strong></td>
								<td><div id="customerPhone"></div></td>
								
							</tr>
							<tr>
								<td align="right"><strong>收货地址</strong></td>
								<td colspan="5"><div id="address"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>用户备注</strong></td>
								<td colspan="5"><div id="remark"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>客服备注</strong></td>
								<td colspan="5"><div id="note"></div></td>
							</tr>
						</table>
					</div>
					<div class="panel-footer"><p class="text-right" style="margin: 0;"><em>零到壹，查找优惠</em></p></div>
				</div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span><fmt:message key="fxl.button.close" /></button>
			</div>
		</div>
	</div>
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