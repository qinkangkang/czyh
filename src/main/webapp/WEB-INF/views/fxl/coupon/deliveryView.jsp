<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2-bootstrap.min.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/select2.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/i18n/zh-CN.js"></script>
<script type="text/javascript">
$(document).ready(function() {
	
	

 	var deliveryId = $("#fdeliveryId").val();
	//待发放优惠券列表
    var couponTable = $("table#couponTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/coupon/getDeliveryCoupon",
 		    "type": "POST",
 		    "data": function (data) {
 		    	data["couponStartOffsetKey"] = "couponViewStartOffset";
 		    	data['deliveryId'] = deliveryId;
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
		},{
			"title" : '<center>券名称</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "150px",
			"orderable" : false
		}, {
			"title" : '<center>使用条件</center>',
			"data" : "conditions",
			"className": "text-center",
			"width" : "60px",
			"orderable" : false
		}, {
			"title" : '<center>创建人</center>',
			"data" : "operator",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>总张数</center>',
			"data" : "deliveryCount",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		},{
			"title" : '<center>发放张数</center>',
			"data" : "sendCount",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>已使用张数</center>',
			"data" : "useCount",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		},{
			"title" : '<center>适用范围说明</center>',
			"data" : "useRange",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},{
			"title" : '<center>创建时间 </center>',
			"data" : "createtime",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},{
			"title" : "发放范围",
			"data" : "useRange",
			"visible": false,
			"orderable" : false
		}, {
			"title" : "审批状态",
			"data" : "statusString",
			"visible": false,
			"orderable" : false
		}],
		"columnDefs" : []
	});
	
	
	
});
</script>
</head>
<body>
<input id="fdeliveryId" name="fdeliveryId" type="hidden" value="${fdeliveryId}">
<div class="row">
    <p class="text-right"><h3>此活动发放优惠券信息：</h3></p>
    <table id="couponTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
</div>
<div class="row">

	<p class="text-right"><h3>活动详细信息：</h3></p>
	<div>
		<label style="width: 500px;">活动审核状态： <span><c:out
					value='${tDelivery.statusString}' /></span></label>
	</div>
    <div>
		<div>
			<label style="width: 500px;">活动名称： <span><c:out value='${tDelivery.ftitle}'/></span></label>
			<label style="width: 500px;">活动ID：<span><c:out value='${deliveryId}'/></span></label>
		</div>
		
		<div>
			<div>
				<label style="width: 500px;">活动开始时间： <span><c:out value='${tDelivery.fdeliveryStartTime}'/></span></label> 
				<label>活动结束时间： <span><c:out value='${tDelivery.fdeliveryEndTime}'/></span></label>
			</div>
		</div>
		<div>
			<label style="width: 500px;">活动创建时间： <span><c:out value='${tDelivery.fdeliveryCreateTime}'/></span></label> 
			<label>创建人： <span><c:out value='${tDelivery.foperator}'/></span></label> <br>
			<label style="width: 500px;">活动审核时间： <span><c:out value='${tDelivery.fupdateTime}'/></span></label>
			<label>审核人： <span><c:out value='${tDelivery.fauditor}'/></span></label> 
		</div>
		<div><label style="width: 500px;">短信推送： <span><c:out value='${tDelivery.fisPushString}'/></span></label></div>
		<div><label style="width: 500px;">推送内容： <span><c:out value='${tDelivery.fpushContent}'/></span></label></div>
		<c:if test="${tDelivery.factivityType == 20 && tDelivery.freciveChannel == 20}">
			<div><label style="color:red;">领券连接： <span><c:out value='${tDelivery.reciveCouponUrl}'/></span></label></div>
		</c:if>
	</div>
</div>
</body>
</html>