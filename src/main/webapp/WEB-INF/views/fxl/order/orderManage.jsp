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
	
	var fsponsorSelect = $("#s_fsponsor").select2({
		placeholder: "选择一个活动组织者",
		allowClear: true,
		language: pickerLocal
	});
	
	var searchForm = $('form#searchForm');
	
	searchForm.submit(function(e) {
		e.preventDefault();
		orderTable.ajax.reload();
	});
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		fsponsorSelect.val(null).trigger("change");
    });
	
	var orderTable = $("table#orderTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/order/getOrderList",
 		    "type": "POST",
 		    "data": function (data) {
 		    	data["s_fstatus"] = $("#s_fstatus").val();
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
			"data" : "forderNum",
			"className": "text-center operation",
			"width" : "40px",
			"orderable" : false
		}, /* {
			"title" : '<center>活动信息</center>',
			"data" : "eventInfo",
			"className": "text-left",
			"width" : "200px",
			"orderable" : false
		}, */ {
			"title" : '<center>商家信息</center>',
			"data" : "sponsorInfo",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		},  {
			"title" : '<center>客户信息</center>',
			"data" : "customerInfo",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>价格信息</center>',
			"data" : "priceInfo",
			"className": "text-left",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>订单<br/>金额</center>',
			"data" : "totalInfo",
			"className": "text-center",
			"width" : "40px",
			"orderable" : true
		}, {
			"title" : '<center>支付方式</center>',
			"data" : "fpayType",
			"className": "text-left",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>支付时间</center>',
			"data" : "fpayTime",
			"className": "text-left",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>订单售卖模式</center>',
			"data" : "fsellmodelString",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>订单状态</center>',
			"data" : "fstatusString",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "30px",
			"className": "text-center operation",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [1],
			"render" : function(data, type, full) {
				var retString = '<a id="viewOrder" href="javascript:;" mId="' + full.DT_RowId + '">' + data + '</a>';
				if(full.flockFlag == 1){
					retString += ' <span class="label label-danger label-xs"><span class="glyphicon glyphicon-lock" aria-hidden="true"></span></span>';
				}
				if(full.fmerchantRemarkFlag == 1){
					retString += ' <img src="' + ctx + '/styles/fxl/images/cs_remark.png" alt="" class="img-thumbnail" width="50">';
				}
				return retString;
			}
		}, {
			"targets" : [10],
			"render" : function(data, type, full) {
				var retString = '';
				if(data.fstatus < 60){
					retString += '<button id="cancel" mId="' + full.DT_RowId + '" type="button" class="btn btn-info btn-xs">取消</button>';
				}
				if(data.fstatus == 20){
						retString += '<button id="verification" mId="' + full.DT_RowId + '" type="button" class="btn btn-warning btn-xs">核销</button>';
					if(data.fsellModel==0){
						retString += '<button id="deliverGoods" mId="' + full.DT_RowId + '" type="button" class="btn btn-warning btn-xs">发货</button>';
					}
				}
				/* retString += '<button id="reply" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs">回复</button>';
				if(data.SystemType > 0 && data.SystemResultCode == 0){
					retString += '<button id="resend" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs">重发</button>';
				} */
				return retString;
			}
		}],
		"drawCallback": function(settings){
			$('table a[id=orderStatus]').popover();
	    }
	});
	
	var deliverGoodsModal =  $('#deliverGoodsModal');
	
	//deliverGoods发货
	$("#orderTable").delegate("button[id=deliverGoods]", "click", function(){
		var mId = $(this).attr("mId");
		$("#expressOrderId").val(mId);
		deliverGoodsModal.modal('show');
	});
	
	$("#deliverGoodsModal").delegate("button[id=saveExpress]", "click", function(){
		var orderId = $("#expressOrderId").val();
		var expressCode = $("#expressCode").val();
		var expressName = $("#expressName").val();
		var expressFrom = $("#expressFrom").val();
		var expressTo = $("#expressTo").val();
		if(expressCode==""||expressName==""||expressFrom==""||expressTo==""){
			toastr.error("请正确填写物流信息");
			return;
		}
    	$.post("${ctx}/fxl/order/saveExpress", $.param({orderId:orderId,expressCode:expressCode,expressName:expressName,expressFrom:expressFrom,expressTo:expressTo},true), function(data) {
			if(data.success){
				toastr.success(data.msg);
				deliverGoodsModal.modal('hide');
				orderTable.ajax.reload(null,false);
			}else{
				toastr.error(data.msg);
			}
		}, "json");
			
		
	});

	$("#orderTable").delegate("a[id=viewOrder]", "click", function(){
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
				$("#addRemark").text(data.addRemark);
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
				$("#insuredInfo").text(data.insuredInfo);
				$("#fchannel").text(data.fchannel);
				$("#bargain").text(data.bargain);
				$("#orderReplay").text(data.orderReplay);
				$("#verification").text(data.verification);
				$("#reward").text(data.reward);
				$("#fsellModel").text(data.fsellModel);
				$("#fCreateTime").text(data.fCreateTime);
				
				var orderGoodsHtml = '<table class="table table-hover table-striped table-bordered">';
				var orderGoods = "<tr><td>商品名称</td><td>规格</td><td>单价</td><td>数量</td></tr>";
				$.each(data.orderGoodsList, function(i, n){
					orderGoods += "<tr>";
					orderGoods += "<td>" + n.feventTitle + "</td><td>" + n.fspec + "</td><td>" + n.fprice + "</td><td>" + n.fcount + "</td>";
					orderGoods += "</tr>";
 				});
				orderGoodsHtml += orderGoods;
				orderGoodsHtml += "</table>";
				$("#orderGoodsHtml").html(orderGoodsHtml);
				
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
	
	$("#orderTable").delegate("button[id=cancel]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要取消该订单进行吗？',
		    okValue: '确定',
		    ok: function () {
		    	$.post("${ctx}/fxl/order/csCancel/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						orderTable.ajax.reload(null,false);
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
	
	$("#orderTable").delegate("button[id=resend]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要重发吗？',
		    okValue: '确定',
		    ok: function () {
		    	$.post("${ctx}/fxl/order/resend/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						orderTable.ajax.reload(null,false);
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
	
	$("#orderTable").delegate("button[id=verification]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '<div id="queryTicket-returnValue-demo">你确认核销该订单吗？</div>',
		    button: [{
			value: '确定',
	            callback: function () {
	            	$.post("${ctx}/fxl/order/csVerification/" + mId , function(data) {
						if(data.success){
							toastr.success(data.msg);
							orderTable.ajax.reload(null,false);
						}else{
							toastr.error(data.msg);
						}
					}, "json");
			    }
				},
		        {
		            value: '关闭'
		        }     
		        
		    ]
		}).showModal();
	});
	
	$("#orderTable").delegate("button[id=verificationTicket]", "click", function(){
		var mId = $(this).attr("mId");
		$.ajax({
	        url: "${ctx}/fxl/order/queryTicket/" + mId,              
	        type: "POST",                       
	        dataType: "json",
	        success: function (data) {
	        	if(data.success){
	        		$("#queryTicket-returnValue-demo").html("票务系统返回信息："+data.data+'<br/>'+"确认核销该订单吗？");
	        	}else{
	        		$("#queryTicket-returnValue-demo").html("查询票务系统失败！"+'<br/>'+"确认核销该订单吗？");
	        	}
	        },
	        fail: function (data) {
	        	$("#queryTicket-returnValue-demo").html("查询票务系统失败！"+'<br/>'+"确认核销该订单吗？");
	        }
	    });
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '<div id="queryTicket-returnValue-demo">正在查询票务系统，请稍等。。。</div>',
		    button: [{
			value: '确定',
	            callback: function () {
	            	$.post("${ctx}/fxl/order/csVerification/" + mId , function(data) {
						if(data.success){
							toastr.success(data.msg);
							orderTable.ajax.reload(null,false);
						}else{
							toastr.error(data.msg);
						}
					}, "json");
			    }
				},
				 
			 {
			value: '核销并push',
	            callback: function () {
	            	$.post("${ctx}/fxl/order/csVerificationPush/" + mId , function(data) {
						if(data.success){
							toastr.success(data.msg);
							orderTable.ajax.reload(null,false);
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
	
	$("#orderTable").delegate("button[id=reply]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '订单跟进内容',
		    content: '<textarea id="property-returnValue-demo" cols="80" rows="5"></textarea>',
		    okValue: '确认回复',
		    ok: function () {
		    	var value = $('#property-returnValue-demo').val();
		    	$.post("${ctx}/fxl/order/orderReplay/" + mId+"/"+value, function(data) {
					if(data.success){
						toastr.success(data.msg);
						orderTable.ajax.reload(null,false);
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

	$("#orderTable").delegate("tbody tr[id]", "click", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
	    var tr = $(this);
	    var row = orderTable.row(tr);
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
	
	$("#orderTable").on('click', 'td.operation', function (event) {
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
	    return false;
	});
	
	$("#orderTable").delegate("input[id^=csremark_]", "keypress", function(event){
		var csremarkInput = $(this);
		if(event.keyCode == "13" && csremarkInput.data("running") != "ok"){
			csremarkInput.data("running","ok");
			var mId = csremarkInput.attr("mId");
			if(!csremarkInput.validationEngine('validate')){
		    	$.post("${ctx}/fxl/order/saveCsRemark", $.param({id:mId,csRemark:csremarkInput.val()},true), function(data) {
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
	
	var orderDetailModal =  $('#orderDetailModal');
	
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
			    content: '您确定要导出当前订单列表的EXCEL文档吗？',
			    okValue: '导出',
			    ok: function () {
			    	exportProgressModal.modal('show');
			    	$.post("${ctx}/fxl/order/createOrderExcel" , $.param($.merge(searchForm.serializeArray(),[{name:"s_fstatus", value:$("#s_fstatus").val()}]),true), function(data) {
						if(data.success){
							exportProgressModal.modal('hide');
							if(downFlag){
								$("#downFile").attr("src","${ctx}/fxl/order/exportExcel/" + data.datePath + "/" + data.excelFileName);
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
	
	$('a[id="orderStatusBtn"]').each(function(i,n){
		$(this).click(function(e){
			e.stopPropagation();
			$("#s_fstatus").val($(this).data("status"));
			orderTable.ajax.reload();
			$(this).tab('show');
		});
	});

});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<input id="s_fstatus" name="s_fstatus" type="hidden" value="10">
<div class="row">
  <div class="col-md-10"><h3>订单管理</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>订单列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="exportExcelBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-list-alt"></span> 导出EXCEL</button>
  	<button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center">
	<form id="searchForm" action="${ctx}/fxl/order/getOrderList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_forderNum">订单编号：</label>
			<input type="text" id="s_forderNum" name="s_forderNum" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_feventTitle">商品名称：</label>
			<input type="text" id="s_feventTitle" name="s_feventTitle" class="form-control input-sm" >
		</div> 
		<%-- <div class="form-group"><label for="s_fsponsor">组织者：</label>
	        <select id="s_fsponsor" name="s_fsponsor" class="form-control input-sm" style="width: 220px;">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="sponsorItem" items="${sponsorMap}"> 
				<option value="${sponsorItem.key}">${sponsorItem.value}</option>
				</c:forEach>
			</select>
	    </div> --%>
	   <%--  <div class="form-group"><label for="s_fbdId">商家BD：</label>
			<select id="s_fbdId" name="s_fbdId" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="bd" items="${bdList}">
					<option value="${bd.key}">${bd.value}</option>
				</c:forEach>
			</select>
		</div> --%>
		<div class="form-group"><label for="s_channel">订单来源：</label>
			<select id="s_channel" name="s_channel" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<option value="1">web</option>
				<option value="2">ios</option>
				<option value="3">android</option>
			</select>
		</div>
		<div class="form-group"><label for="s_sellModel">订单售卖模式：</label>
			<select id="s_sellModel" name="s_sellModel" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<option value="0">自营</option>
				<option value="1">自提</option>
			</select>
		</div>
		<%-- <div class="form-group"><label for="s_source">促销方式：</label>
			<select id="s_source" name="s_source" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="source" items="${sourceMap}">
					<option value="${source.key}">${source.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="checkbox"><label>
			<input type="checkbox" id="s_changeAmount" name="s_changeAmount" value="1" class="form-control input-sm"> 使用优惠
			</label>
		</div> --%>
		<div class="form-group"><label for="s_fcustomerName">客户名称：</label>
			<input type="text" id="s_fcustomerName" name="s_fcustomerName" class="form-control input-sm" size="14">
		</div>
		<div class="form-group"><label for="s_fcustomerPhone">客户手机号：</label>
			<input type="text" id="s_fcustomerPhone" name="s_fcustomerPhone" class="form-control input-sm" size="14">
		</div>
		<div class="form-group"><label>下单时间：</label>
		<div class="input-daterange input-group date" id="createDateDiv" style="width:300px;">
		    <input type="text" class="form-control input-sm" name="fcreateTimeStart" style="cursor: pointer;">
		    <span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
		    <input type="text" class="form-control input-sm" name="fcreateTimeEnd" style="cursor: pointer;">
		</div></div>
		<p class="text-center"><button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<ul class="nav nav-tabs" id="orderTab">
	<li class="active"><a id="orderStatusBtn" data-status="10" data-toggle="tab">已下单</a></li>
	<li><a id="orderStatusBtn" data-status="20" data-toggle="tab">已付款</a></li>
	<li><a id="orderStatusBtn" data-status="30" data-toggle="tab">待收货</a></li>
	<li><a id="orderStatusBtn" data-status="110" data-toggle="tab">退款申请</a></li>
	<li><a id="orderStatusBtn" data-status="120" data-toggle="tab">退款成功</a></li>
	<li><a id="orderStatusBtn" data-status="60" data-toggle="tab">待评价</a></li>
	<li><a id="orderStatusBtn" data-status="70" data-toggle="tab">已完成</a></li>
	<li><a id="orderStatusBtn" data-status="100" data-toggle="tab">已取消</a></li>
	<li><a id="orderStatusBtn" data-status="0" data-toggle="tab">全&nbsp;&nbsp;部</a></li>
</ul>
<div class="alert alert-warning text-center" role="alert" style="padding: 3px; margin-bottom: 0px;"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><strong>点击订单表中的订单，即可显示该订单的客服备注信息。</strong></div>
<table id="orderTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
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
								<td align="right"><strong>下单时间</strong></td>
								<td><div id="fCreateTime"></div></td>
								<td align="right"><strong>邮费</strong></td>
								<td><div id="fpostage"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>应收金额</strong></td>
								<td><div id="freceivableTotal"></div></td>
								<td align="right"><strong>变更金额</strong></td>
								<td><div id="fchangeAmount"></div></td>
								<td align="right"><strong>实收金额</strong></td>
								<td><div id="ftotal"></div></td>
							</tr>
							<tr>
							    <td align="right"><strong>下单渠道</strong></td>
								<td><div id="fchannel"></div></td>
								<td align="right"><strong>订单模式</strong></td>
								<td><div id="fsellModel"></div></td>
								<td align="right"><strong>支付类型</strong></td>
								<td><div id="fpayType"></div></td>
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
							<!-- <tr>
								<td align="right"><strong>下单渠道</strong></td>
								<td><div id="fchannel"></div></td>
								<td align="right"><strong>核销方式</strong></td>
								<td><div id="verification"></div></td>
								<td align="right"><strong>评价激励</strong></td>
								<td><div id="reward"></div></td>
							</tr> -->
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
							<tr>
								<td align="right"><strong>购买商品</strong></td>
								<td colspan="5"><div id="orderGoodsHtml"></div></td>
							</tr>
						</table>
					</div>
					<div class="panel-footer"><p class="text-right" style="margin: 0;"><em>查找优惠</em></p></div>
				</div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span><fmt:message key="fxl.button.close" /></button>
			</div>
		</div>
	</div>
</div>
<!--订单详情结束-->
<!--录入物流详情开始-->
<div class="modal fade" id="deliverGoodsModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="myModalLabel">录入物流详情</h4>
			</div>
			<div class="modal-body">
				<div class="panel panel-info" >
					<div class="panel-heading"><strong>物流信息</strong></div>
					<div class="form-inline">
					<input id="expressOrderId" type="hidden">
						<div id="expressOrderId" style="display:none"></div>
						<div class="form-group has-error"><label for="expressCode">快递：</label>
							<select id="expressCode" name="expressCode" class="form-control input-sm">
								<c:forEach var="express" items="${ExpressCodeMap}">
									<option value="${express.key}">${express.value}</option>
								</c:forEach>
							</select>
						</div>
						<div class="form-group has-error"><label for="expressName">快递单号：</label>
							<input type="text" id="expressName" name="expressName" class="form-control input-sm" size="14">
						</div>
						<div class="form-group has-error"><label for="expressFrom">起始位置：</label>
							<input type="text" id="expressFrom" name="expressFrom" class="form-control input-sm" size="14">
						</div>
						<div class="form-group has-error"><label for="expressTo">到达位置：</label>
							<input type="text" id="expressTo" name="expressTo" class="form-control input-sm" size="14">
						</div>
					</div>
						
					<div class="panel-footer"><p class="text-right" style="margin: 0;"><em>查找优惠</em></p></div>
				</div>
			</div>
			<div class="modal-footer">
				<button class="btn btn-primary" type="submit" id ="saveExpress"><span class="glyphicon glyphicon-arrow-right"></span> 保存</button>
				<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span><fmt:message key="fxl.button.close" /></button>
			</div>
		</div>
	</div>
</div>
<!-- 录入物流详情结束 -->
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