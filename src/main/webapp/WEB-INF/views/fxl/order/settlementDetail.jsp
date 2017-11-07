<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2.min.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/select2.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/i18n/zh-CN.js"></script>
<script type="text/javascript">
$(document).ready(function() {
	
	$('#createDateDiv').datepicker({
	    format : "yyyy-mm-dd",
	    endDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	$("#s_fsponsor").select2({
		placeholder: "选择一个商家",
		allowClear: true,
		language: pickerLocal
	});
	
	var searchForm = $('form#searchForm');
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#select").click(function(e) {
		orderTable.ajax.reload();
		//$("#selectDiv").slideUp("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
    });
	
	var settlementDetailTable = $("table#settlementDetailTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/order/getSettlementDetailList/" + $("#settlementId").val(),
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
			"title" : '<center>订单编号</center>',
			"data" : "orderNum",
			"className": "text-center",
			"width" : "60px",
			"orderable" : false
		}, {
			"title" : '<center>活动名称</center>',
			"data" : "feventTitle",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>核销时间</center>',
			"data" : "fverificationTime",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>核销人</center>',
			"data" : "fverificationname",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>核销方式</center>',
			"data" : "fclientOperate",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>实收金额</center>',
			"data" : "foriginalAmount",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>优惠金额</center>',
			"data" : "forderChangelAmount",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>结算单金额</center>',
			"data" : "ftotal",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>结算单类型</center>',
			"data" : "typeString",
			"className": "text-center",
			"width" : "60px",
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "30px",
			"className": "text-center operation",
			"orderable" : false
		}] ,
		"columnDefs" : [{
			"targets" : [10],
			"render" : function(data, type, full) {
				var retString = '';
				retString =  '<button id="view" mId="' + full.orderId + '" type="button" class="btn btn-success btn-xs">查看</button>';
				
				return retString;
			}
		}] 
	});
	
	$("#settlementDetailTable").delegate("button[id=view]", "click", function(){
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/order/getOrder/" + mId, function(data) {
			if(data.success){
				$("#forderNum").text(data.forderNum);
				$("#feventTitle").text(data.feventTitle);
				$("#fsessionTitle").text(data.fsessionTitle);
				$("#fspecTitle").text(data.fspecTitle);
				$("#fprice").text(data.fprice);
				$("#fcount").text(data.fcount);
				$("#fpostage").text(data.fpostage);
				$("#freceivableTotal").text(data.freceivableTotal);
				$("#fchangeAmount").text(data.fchangeAmount);
				$("#fchangeAmountInstruction").text(data.fchangeAmountInstruction);
				$("#ftotal").text(data.ftotal);
				$("#fremark").text(data.fremark);
				$("#fcsRemark").text(data.fcsRemark);
				$("#forderType").text(data.forderType);
				$("#freturn").text(data.freturn);
				$("#fpayType").text(data.fpayType);
				$("#flockFlag").text(data.flockFlag);
				$("#recipient").text(data.recipient);
				$("#phone").text(data.phone);
				$("#address").text(data.address);
				$("#fsponsorName").text(data.fsponsorName);
				$("#fsponsorFullName").text(data.fsponsorFullName);
				$("#fsponsorNumber").text(data.fsponsorNumber);
				$("#fsponsorPhone").text(data.fsponsorPhone);
				$("#fcustomerName").text(data.fcustomerName);
				$("#fcustomerSex").text(data.fcustomerSex);
				$("#fcustomerPhone").text(data.fcustomerPhone);
				var orderStatusHtml = "<ul>";
				$.each(data.orderStatusList, function(i, n){
					orderStatusHtml += "<li>" + n.statusString + "&nbsp;&nbsp;&nbsp;" + n.fcreateTime + "</li>";
 				});
				orderStatusHtml += "</ul>";
				$("#orderStatusList").html(orderStatusHtml);
				orderDetailModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	var orderDetailModal =  $('#orderDetailModal');
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<input id="s_fstatus" name="s_fstatus" type="hidden" value="10">
<input id="settlementId" name="settlementId" type="hidden" value="${statement.id}">
<div class="row">
  <div class="col-md-10"><h3>商家结算单明细</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="panel panel-info">
	<div class="panel-heading">结算单信息</div>
	<div class="panel-body">
		<table class="table table-hover table-striped table-bordered">
			<tr>
				<td align="right"><strong>商家名称：</strong></td>
				<td colspan="5">${statement.TSponsor.ffullName}</td>
			</tr>
			<tr>
				<td width="10%" align="right"><strong>结算单编号：</strong></td>
				<td width="23%">${statement.fstatementNum}</td>
				<td width="10%" align="right"><strong>结算起时间：</strong></td>
				<td width="23%"><fmt:formatDate value="${statement.fbeginTime}" type="date" pattern="yyyy-MM-dd" /></td>
				<td width="10%" align="right"><strong>结算止时间：</strong></td>
				<td width="24%"><fmt:formatDate value="${statement.fendTime}" type="date" pattern="yyyy-MM-dd" /></td>
			</tr>
			<tr>
				<td align="right"><strong>实收总金额：</strong></td>
				<td><fmt:formatNumber value="${statement.fpadinAmount}" pattern="#.##" />元</td>
				<td align="right"><strong>优惠总金额：</strong></td>
				<td><fmt:formatNumber value="${statement.forderChangelAmount}" pattern="#.##" />元</td>
				<td align="right"><strong>结算总金额：</strong></td>
				<td><fmt:formatNumber value="${statement.famount}" pattern="#.##" />元</td>
			</tr>
			<tr>
				<td align="right"><strong>结算操作人：</strong></td>
				<td>${operator}</td>
				<td align="right"><strong>操作时间：</strong></td>
				<td><fmt:formatDate value="${statement.ftime}" type="date" pattern="yyyy-MM-dd HH:mm" /></td>
			</tr>
			<tr>
				<td align="right"><strong>备注：</strong></td>
				<td colspan="5">${statement.fremark}</td>
			</tr>
		</table>
	</div>
	<div class="panel-footer"><p class="text-right" style="margin: 0;"><em>零到壹，查找优惠</em></p></div>
</div>
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/event/getEventList" method="post" class="form-inline" role="form">
		<div class="form-group"><label>核销时间：</label>
		<div class="input-daterange input-group date" id="createDateDiv" style="width:330px;">
		    <input type="text" class="form-control input-sm" name="fcreateTimeStart" style="cursor: pointer;">
		    <span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
		    <input type="text" class="form-control input-sm" name="fcreateTimeEnd" style="cursor: pointer;">
		</div></div>
		<p class="text-center"><button id="select" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="settlementDetailTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-bordered"></table>
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
								<td width="10%" align="right"><strong>订单编号</strong></td>
								<td width="23%"><div id="forderNum"></div></td>
								<td width="10%" align="right"><strong>活动名称</strong></td>
								<td width="23%"><div id="feventTitle"></div></td>
								<td width="10%" align="right"><strong>场次名称</strong></td>
								<td width="24%"><div id="fsessionTitle"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>规格名称</strong></td>
								<td><div id="fspecTitle"></div></td>
								<td align="right"><strong>单价</strong></td>
								<td><div id="fprice"></div></td>
								<td align="right"><strong>购买数量</strong></td>
								<td><div id="fcount"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>邮费</strong></td>
								<td><div id="fpostage"></div></td>
								<td align="right"><strong>数量单位</strong></td>
								<td><div id="funits"></div></td>
								<td align="right"><strong>应收金额</strong></td>
								<td><div id="freceivableTotal"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>变更金额</strong></td>
								<td><div id="fchangeAmount"></div></td>
								<td align="right"><strong>变更说明</strong></td>
								<td><div id="fchangeAmountInstruction"></div></td>
								<td align="right"><strong>实收金额</strong></td>
								<td><div id="ftotal"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>订单类型</strong></td>
								<td><div id="forderType"></div></td>
								<td align="right"><strong>是否预约</strong></td>
								<td><div id="fappointment"></div></td>
								<td align="right"><strong>可否退换</strong></td>
								<td><div id="freturn"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>支付类型</strong></td>
								<td><div id="fpayType"></div></td>
								<td align="right"><strong>是否锁定</strong></td>
								<td><div id="flockFlag"></div></td>
								<td align="right"><strong>商家名称</strong></td>
								<td><div id="fsponsorName"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>商家全称</strong></td>
								<td><div id="fsponsorFullName"></div></td>
								<td align="right"><strong>商家编号</strong></td>
								<td><div id="fsponsorNumber"></div></td>
								<td align="right"><strong>商家电话</strong></td>
								<td><div id="fsponsorPhone"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>用户名称</strong></td>
								<td><div id="fcustomerName"></div></td>
								<td align="right"><strong>用户性别</strong></td>
								<td><div id="fcustomerSex"></div></td>
								<td align="right"><strong>用户电话</strong></td>
								<td><div id="fcustomerPhone"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>收货人</strong></td>
								<td><div id="recipient"></div></td>
								<td align="right"><strong>联系电话</strong></td>
								<td><div id="phone"></div></td>
								<td align="right"><strong>收货地址</strong></td>
								<td><div id="address"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>投保人信息</strong></td>
								<td colspan="5"><div id="insuredInfo"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>用户备注</strong></td>
								<td colspan="5"><div id="fremark"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>客服备注</strong></td>
								<td colspan="5"><div id="fcsRemark"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>订单历史</strong></td>
								<td colspan="5"><div id="orderStatusList"></div></td>
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
</body>
</html>