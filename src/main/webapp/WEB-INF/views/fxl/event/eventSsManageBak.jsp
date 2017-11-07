<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js" charset="UTF-8"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js" charset="UTF-8"></script>
<script src="http://api.map.baidu.com/api?v=2.0&ak=8vxHpx4PyxOzXyGIjUb5GAoT" type="text/javascript"></script>
<script type="text/javascript">
$(document).ready(function() {
	
	$('#fdateDiv').datepicker({
	    format : "yyyy-mm-dd",
	    //startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	/* $("#fstartTimeDiv, #fendTimeDiv").datetimepicker({
	    format : "hh:ii",
	    startView : 1,
	    language : pickerLocal,
	    autoclose : true,
	    minuteStep : 5
	    //,pickerPosition: "bottom-left"
	}); */
	
	$("#fautoVerificationTimeDiv, #fdeadlineDiv").datetimepicker({
	    format : "yyyy-mm-dd hh:ii",
	    startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true,
	    pickerPosition: "top-left"
	});
	
	var eventSessionTable = $("table#eventSessionTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"paging": false,
		"autoWidth" : false,
	  	//"scrollY": "350px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/event/getEventSessionList/" + $("#eventId").val(),
 		    "type": "POST",
 		    "data": function (data) {
 		    	/* $.each(searchForm.serializeArray(), function(i, n){
 		    		data[n.name] = n.value;
 				}); */
 		        return data;
 		    }
		},
		"deferRender": true,
		"lengthChange": false,
		"retrieve": true,
		"columns" : [
		{	"title" : "ID",
			"data" : "DT_RowId",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center>场次名称</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		}, {
			"title" : '<center>活动地址</center>',
			"data" : "faddress",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		}, {
			"title" : '<center>活动<br/>开始日期</center>',
			"data" : "fstartDate",
			"className": "text-center",
			"width" : "60px",
			"orderable" : false
		}, {
			"title" : '<center>活动<br/>结束日期</center>',
			"data" : "fendDate",
			"className": "text-center",
			"width" : "60px",
			"orderable" : false
		}, {
			"title" : '<center>退款<br/>期限</center>',
			"data" : "frefoundPeriod",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>报名截止时间</center>',
			"data" : "fdeadline",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>是否参与促销</center>',
			"data" : "fsalesFlagString",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "40px",
			"className": "text-center",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [8],
			"render" : function(data, type, full) {
				return '<button id="edit" mId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs"><fmt:message key="fxl.button.edit" /></button><button id="create" mId="' + full.DT_RowId + '" type="button" class="btn btn-info btn-xs">规格</button><button id="copy" mId="' + full.DT_RowId + '" type="button" class="btn btn-success btn-xs">复制</button>';
			}
		}]
	});
	
	$("#eventSessionTable").delegate("button[id=create]", "click", function(){
		sessionId = $(this).attr("mId");
		$('#actionFlag').val("add");
		$('#resetBtn3').show();
		delBtn3.hide();
		createEventSpecModal3.modal('show')
	});
	
	$("#eventSessionTable").delegate("button[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$('#resetBtn').hide();
		delBtn.show();
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/event/getEventSession/" + mId, function(data) {
			if(data.success){
				$("#id").val(mId);
				$("#createEventSessionForm #ftitle").val(data.ftitle);
				$("#fgps").val(data.fgps);
				$("#faddress").val(data.faddress);
				//$("#flocation").val(data.flocation);
				$("#fstartDate").datepicker('update', data.fstartDate);
				$("#fendDate").datepicker('update', data.fendDate);
				$('#fautoVerificationTimeDiv').datetimepicker('update', data.fautoVerificationTime);
				$('#fdeadlineDiv').datetimepicker('update', data.fdeadline);
				$("#createEventSessionForm #forder").val(data.forder);
				createEventSessionModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	
	$("#eventSessionTable").delegate("button[id=copy]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要复制一个相同的活动场次吗？',
		    button: [
					 {
			value: '复制单个场次',
	            callback: function () {
	            	$.post("${ctx}/fxl/event/copyEventSession/" + mId , function(data) {
						if(data.success){
							toastr.success(data.msg);
							eventSessionTable.ajax.reload(null,false);
						}else{
							toastr.error(data.msg);
						}
					}, "json");
			    },
			    cancelValue: '<fmt:message key="fxl.button.cancel" />',
			    cancel: function (){}
				},
				{
				value: '复制场次跟规格',
	            callback: function () {
	            	$.post("${ctx}/fxl/event/copyEventSpecSessionId/" + mId , function(data) {
						if(data.success){
							toastr.success(data.msg);
							eventSessionTable.ajax.reload(null,false);
						}else{
							toastr.error(data.msg);
						}
					}, "json");
			    },
			    cancelValue: '<fmt:message key="fxl.button.cancel" />',
			    cancel: function (){
			      }
		        },
		        {
		            value: '关闭我'
		        }
		    ]
		}).showModal();
	});
	
	
	var sessionId = "";
	var selected = "";
	//活动表中的操作按钮的点击事件方法
	$("#eventSessionTable").delegate("tbody tr[id]", "click", function(e){
		e.stopPropagation();
		var tr = $(this);
		if(tr.attr("id") != sessionId){
			sessionId = tr.attr("id");
			eventSpecTable3.ajax.reload(null,false);
		}
		if($.trim(selected) != ""){
			$("#eventSessionTable tr[id=" + selected + "]").removeClass('info');
		}
		selected = this.id;
        tr.addClass('info');
	});
	
	var createEventSessionModal =  $('#createEventSessionModal');
	
	createEventSessionModal.on('hide.bs.modal', function(e){
		if(e.target.id === "createEventSessionModal"){
			createEventSessionForm.trigger("reset");
		}
	});
	
	$('#createEventSessionBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		$('#resetBtn').show();
		delBtn.hide();
		createEventSessionModal.modal('show')
	});
	
	var createEventSessionForm = $('#createEventSessionForm');
	
	createEventSessionForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	createEventSessionForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			if($('#actionFlag').val() == "add"){
				$.post(form.attr("action"), $.param($.merge(form.serializeArray(),[{name:"eventId", value:$("#eventId").val()}]),true), function(data){
					if(data.success){
						toastr.success(data.msg);
						eventSessionTable.ajax.reload(null,false);
						createEventSessionModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/event/editEventSession", form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						eventSessionTable.ajax.reload(null,false);
						createEventSessionModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
	
	createEventSessionForm.on("reset", function(event){
	/* 	if (!event.isDefaultPrevented()) {
			event.preventDefault();
		} */
		$(':input',createEventSessionForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		createEventSessionForm.validationEngine('hideAll');
	});
	
	var delBtn = $("#delBtn");
	
	delBtn.click(function(event) {
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该活动场次吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/event/delEventSession/" + $("#id").val() , function(data) {
					if(data.success){
						toastr.success(data.msg);
						eventSessionTable.ajax.reload(null,false);
						createEventSessionModal.modal("hide");
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
	
	var eventSpecTable3 = $("table#eventSpecTable3").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"paging": false,
		"autoWidth" : false,
	  	//"scrollY": "350px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/event/getEventSpecList",
 		    "type": "POST",
 		    "data": function (data) {
 		    	data['type'] = 'session';
 		    	data['eventId'] = $("#eventId").val();
 		    	data['sessionId'] = sessionId;
 		    	/* $.each(searchForm.serializeArray(), function(i, n){
 		    		data[n.name] = n.value;
 				}); */
 		        return data;
 		    }
		},
		"deferRender": true,
		"lengthChange": false,
		"retrieve": true,
		"columns" : [
		{	"title" : "ID",
			"data" : "DT_RowId",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center>规格名称</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		}, {
			"title" : '<center>原价格</center>',
			"data" : "fprice",
			"className": "text-right",
			"width" : "30px",
			"orderable" : true
		}, {
			"title" : '<center>现价格</center>',
			"data" : "fdeal",
			"className": "text-right",
			"width" : "30px",
			"orderable" : true
		}, {
			"title" : '<center>结算价格</center>',
			"data" : "fsettlementPrice",
			"className": "text-right",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>扣点比例</center>',
			"data" : "fpointsPrice",
			"className": "text-right",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>返现金额</center>',
			"data" : "fdistributionRebateAmount",
			"className": "text-right",
			"width" : "30px",
			"orderable" : false
		},{
			"title" : '<center>返现比例</center>',
			"data" : "fdistributionRebateRatio",
			"className": "text-right",
			"width" : "30px",
			"orderable" : false
		},  {
			"title" : '<center>套内<br/>成人数</center>',
			"data" : "fadult",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>套内<br/>儿童数</center>',
			"data" : "fchild",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>邮费</center>',
			"data" : "fpostage",
			"className": "text-right",
			"width" : "30px",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center>库存量</center>',
			"data" : "fstock",
			"className": "text-right",
			"width" : "30px",
			"orderable" : true
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "40px",
			"className": "text-center",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [12],
			"render" : function(data, type, full) {
				var retString = '';
				if(data.fstatus < 99){
					retString += '<button id="edit" mId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs"><fmt:message key="fxl.button.edit" /></button>';
				}
				retString += '<button id="copy" mId="' + full.DT_RowId + '" type="button" class="btn btn-success btn-xs">复制</button>';
				return retString;
			}
		}]
	});
	
	$("#eventSpecTable3").delegate("button[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$('#resetBtn3').hide();
		delBtn3.show();
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/event/getEventSpec/" + mId, function(data) {
			if(data.success){
				$("#createEventSpecForm3 #specId").val(mId);
				$("#createEventSpecForm3 #ftitle").val(data.ftitle);
				$("#createEventSpecForm3 #fdescription").val(data.fdescription);
				$("#createEventSpecForm3 #fprice").val(data.fprice);
				$("#createEventSpecForm3 #fdeal").val(data.fdeal);
				$("#createEventSpecForm3 #fsettlementPrice").val(data.fsettlementPrice);
				$("#createEventSpecForm3 #fpointsPrice").val(data.fpointsPrice);
				$("#createEventSpecForm3 #fadult").val(data.fadult);
				$("#createEventSpecForm3 #fchild").val(data.fchild);
				$("#createEventSpecForm3 #fpostage").val(data.fpostage);
				//$("#createEventSpecForm3 #ftotal").val(data.ftotal);
				$("#createEventSpecForm3 #fstock").val(data.fstock);
				$("#createEventSpecForm3 #forder").val(data.forder);
				$("#createEventSpecForm3 #fdistributionRebateAmount").val(data.fdistributionRebateAmount);
				$("#createEventSpecForm3 #fdistributionRebateRatio").val(data.fdistributionRebateRatio);
				
				$("#createEventSpecForm3 #fexternalGoodsCode").val(data.fexternalGoodsCode);
				$("#createEventSpecForm3 #frealNameType option[value='" + data.frealNameType + "']").prop("selected",true);
				createEventSpecModal3.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	$("#eventSpecTable3").delegate("button[id=copy]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要复制一个相同的活动规格吗？',
		    okValue: '复制',
		    ok: function () {
		    	$.post("${ctx}/fxl/event/copyEventSpec/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						eventSpecTable3.ajax.reload(null,false);
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
	
	var createEventSpecModal3 =  $('#createEventSpecModal3');
	
	createEventSpecModal3.on('hide.bs.modal', function(e){
		createEventSpecForm3.trigger("reset");
	});
	
	var createEventSpecForm3 = $('#createEventSpecForm3');
	
	createEventSpecForm3.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	createEventSpecForm3.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			if($('#actionFlag').val() == "add"){
				$.post(form.attr("action"), $.param($.merge(form.serializeArray(),[{name:"eventId", value:$("#eventId").val()},{name:"sessionId", value: sessionId}]),true), function(data){
					if(data.success){
						toastr.success(data.msg);
						eventSpecTable3.ajax.reload(null,false);
						createEventSpecModal3.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/event/editEventSpec", form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						eventSpecTable3.ajax.reload(null,false);
						createEventSpecModal3.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
	
	createEventSpecForm3.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createEventSpecForm3).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		createEventSpecForm3.validationEngine('hideAll');
	});
	
	var delBtn3 = $("#delBtn3");
	
	delBtn3.click(function(event) {
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该活动规格吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/event/delEventSpec/" + $("#createEventSpecForm3 #specId").val() , function(data) {
					if(data.success){
						toastr.success(data.msg);
						eventSpecTable3.ajax.reload(null,false);
						createEventSpecModal3.modal("hide");
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
	
	$('#createEventSpecBtn3').on('click',function(e) {
		$('#actionFlag').val("add");
		$('#resetBtn3').show();
		delBtn3.hide();
		createEventSpecModal3.modal('show')
	});
	
	var eventSpecTable = $("table#eventSpecTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"paging": false,
		"autoWidth" : false,
	  	//"scrollY": "350px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/event/getEventSpecList",
 		    "type": "POST",
 		    "data": function (data) {
 		    	data['type'] = 'event';
 		    	data['eventId'] = $("#eventId").val();
 		    	/* $.each(searchForm.serializeArray(), function(i, n){
 		    		data[n.name] = n.value;
 				}); */
 		        return data;
 		    }
		},
		"deferRender": true,
		"lengthChange": false,
		"retrieve": true,
		"columns" : [
		{	"title" : "ID",
			"data" : "DT_RowId",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center>规格名称</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		}, {
			"title" : '<center>原价格</center>',
			"data" : "fprice",
			"className": "text-right",
			"width" : "30px",
			"orderable" : true
		}, {
			"title" : '<center>现价格</center>',
			"data" : "fdeal",
			"className": "text-right",
			"width" : "30px",
			"orderable" : true
		}, {
			"title" : '<center>结算价格</center>',
			"data" : "fsettlementPrice",
			"className": "text-right",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>套内<br/>成人数</center>',
			"data" : "fadult",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>套内<br/>儿童数</center>',
			"data" : "fchild",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>邮费</center>',
			"data" : "fpostage",
			"className": "text-right",
			"width" : "30px",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center>库存量</center>',
			"data" : "fstock",
			"className": "text-right",
			"width" : "30px",
			"orderable" : true
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "40px",
			"className": "text-center",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [10],
			"render" : function(data, type, full) {
				var retString = '';
				if(data.fstatus < 99){
					retString += '<button id="edit" mId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs"><fmt:message key="fxl.button.edit" /></button>';
				}
				retString += '<button id="copy" mId="' + full.DT_RowId + '" type="button" class="btn btn-success btn-xs">复制</button>';
				return retString;
			}
		}]
	});
	
	$("#eventSpecTable").delegate("button[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$('#resetBtn2').hide();
		delBtn2.show();
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/event/getEventSpec/" + mId, function(data) {
			if(data.success){
				$("#createEventSpecForm #specId").val(mId);
				$("#createEventSpecForm #ftitle").val(data.ftitle);
				$("#createEventSpecForm #fdescription").val(data.fdescription);
				$("#createEventSpecForm #fprice").val(data.fprice);
				$("#createEventSpecForm #fdeal").val(data.fdeal);
				$("#createEventSpecForm #fsettlementPrice").val(data.fsettlementPrice);
				$("#createEventSpecForm #fadult").val(data.fadult);
				$("#createEventSpecForm #fchild").val(data.fchild);
				$("#createEventSpecForm #fpostage").val(data.fpostage);
				//$("#createEventSpecForm #ftotal").val(data.ftotal);
				$("#createEventSpecForm #fstock").val(data.fstock);
				$("#createEventSpecForm #forder").val(data.forder);
				createEventSpecModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	$("#eventSpecTable").delegate("button[id=copy]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要复制一个相同的活动规格吗？',
		    okValue: '复制',
		    ok: function () {
		    	$.post("${ctx}/fxl/event/copyEventSpec/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						eventSpecTable.ajax.reload(null,false);
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
	
	var createEventSpecModal =  $('#createEventSpecModal');
	
	createEventSpecModal.on('hide.bs.modal', function(e){
		createEventSpecForm.trigger("reset");
	});
	
	$('#createEventSpecBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		$('#resetBtn2').show();
		delBtn2.hide();
		createEventSpecModal.modal('show')
	});
	
	var createEventSpecForm = $('#createEventSpecForm');
	
	createEventSpecForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	createEventSpecForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			if($('#actionFlag').val() == "add"){
				$.post(form.attr("action"), $.param($.merge(form.serializeArray(),[{name:"eventId", value:$("#eventId").val()}]),true), function(data){
					if(data.success){
						toastr.success(data.msg);
						eventSpecTable.ajax.reload(null,false);
						createEventSpecModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/event/editEventSpec", form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						eventSpecTable.ajax.reload(null,false);
						createEventSpecModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
	
	createEventSpecForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createEventSpecForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		createEventSpecForm.validationEngine('hideAll');
	});
	
	var delBtn2 = $("#delBtn2");
	
	delBtn2.click(function(event) {
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该活动规格吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/event/delEventSpec/" + $("#createEventSpecForm #specId").val() , function(data) {
					if(data.success){
						toastr.success(data.msg);
						eventSpecTable.ajax.reload(null,false);
						createEventSpecModal.modal("hide");
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
	
	$('#createEventSpecBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		$('#resetBtn2').show();
		delBtn2.hide();
		createEventSpecModal.modal('show')
	});
	
	var baiduMapModal =  $('#baiduMapModal');
	
	baiduMapModal.on('hidden.bs.modal', function(e){
		baiduCoordinate.val("");
		baiduAddress.val("");
		createEventSessionModal.css("overflow-y","auto");
	});
	
	var map = new BMap.Map("fxlBaiduMap");        // 创建地图实例  
	var point = new BMap.Point(116.404, 39.915);  // 创建点坐标  
	map.centerAndZoom(point, 15);
	map.enableScrollWheelZoom();
	map.addControl(new BMap.NavigationControl());    
	map.addControl(new BMap.ScaleControl());    
	//map.addControl(new BMap.OverviewMapControl());    
	//map.addControl(new BMap.MapTypeControl());    
	map.setCurrentCity("北京"); 
	// 仅当设置城市信息时，MapTypeControl的切换功能才能可用
	
	map.addEventListener("click", function(e){
		baiduCoordinate.val(e.point.lng + "," + e.point.lat);
	});
	
	var baiduCoordinate = $('#baiduCoordinate');
	var baiduAddress = $('#baiduAddress');
	
	$('#selectBaiduOkBtn').on('click',function(e) {
		if($.trim(baiduCoordinate.val()) == ""){
			/* dialog({
				fixed: true,
		        title: '操作提示',
		        content: '请点击百度地图上的活动地点来获取坐标值！',
		        cancelValue: '关闭',
		        cancel: function () {}
		    }).showModal(); */
			toastr.warning('请点击百度地图上的活动地点来获取坐标值！');
		}else{
			$('#fgps').val(baiduCoordinate.val());
			$('#faddress').val(baiduAddress.val());
			baiduMapModal.modal('hide')
		}
	});
	
	var searchListDiv = $("#searchListDiv");
	
	var options = {
		onSearchComplete: function(results){
			if (local.getStatus() == BMAP_STATUS_SUCCESS){
				if(results.getCurrentNumPois() > 0){
					map.centerAndZoom(new BMap.Point(results.getPoi(0).point.lng, results.getPoi(0).point.lat), 15);
				}
				searchListDiv.empty();
				var liList;
				// 判断状态是否正确
				for (var i = 0; i < results.getCurrentNumPois(); i ++){
					//console.log(results.getPoi(i));
					var point = new BMap.Point(results.getPoi(i).point.lng, results.getPoi(i).point.lat);
					var marker = new BMap.Marker(point);
					map.addOverlay(marker);
					searchListDiv.append($("<a id='resultItem' href='#' class='list-group-item' data-lng='" + results.getPoi(i).point.lng + "' data-lat='" + results.getPoi(i).point.lat + "' data-title='" + results.getPoi(i).title + "' data-address='" + results.getPoi(i).address + "'>" + results.getPoi(i).title + "<br/><strong>地址：</strong>" + results.getPoi(i).address + "</a>"));
				}
			}
		}
	};
	
	searchListDiv.delegate("a[id=resultItem]", "click", function(){
		var thisa = $(this);
		baiduCoordinate.val(thisa.data("lng") + "," + thisa.data("lat"));
		baiduAddress.val(thisa.data("address"));
		
		var sContent = "<div><h4 style='margin:0 0 5px 0;padding:0.2em 0'>" + thisa.data("title") 
		+ "</h4><p style='margin:0;line-height:1.5;text-indent:2em'>" + thisa.data("address") + "</p></div>";
		var infoWindow = new BMap.InfoWindow(sContent);  // 创建信息窗口对象
		var point = new BMap.Point(thisa.data("lng"), thisa.data("lat"));
		map.centerAndZoom(point, 18);
		map.openInfoWindow(infoWindow,point);
	});
	
	var local = new BMap.LocalSearch(map, options);  
	
	$("#searchKey").bind('keypress',function(event){
        if(event.keyCode == "13"){
        	var searchKey = $.trim($("#searchKey").val());
    		if(searchKey != ""){
    			local.search(searchKey);
    		}
        }
    });
	
	$("#fsalesFlag option[value='${fsalesFlag}']").attr("selected",true);
	$("#frealNameType option[value='${frealNameType}']").attr("selected",true);
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<input id="eventId" name="eventId" type="hidden" value="${event.id}">
<div class="row">
  <div class="col-md-8"><h3>活动所属场次与规格维护</h3></div>
  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;">
  <button type="button" class="btn btn-primary btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/event/view'"><span class="glyphicon glyphicon-arrow-left"></span> 返回活动浏览</button>
  <button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="panel panel-info">
	<div class="panel-heading"><strong>活动信息</strong></div>
	<div class="panel-body">
		<table class="table table-hover table-striped table-bordered">
			<tr>
				<td width="10%" align="right"><strong>活动标题：</strong></td>
				<td width="23%">${event.ftitle}</td>
				<td width="10%" align="right"><strong>所属城市：</strong></td>
				<td width="23%">${fcity}</td>
				<td width="10%" align="right"><strong>组织者：</strong></td>
				<td width="24%">${event.TSponsor.fname}</td>
			</tr>
			<tr>
				<td align="right"><strong>一级类目：</strong></td>
				<td>${ftypeA}</td>
				<td align="right"><strong>二级类目：</strong></td>
				<td>${ftypeB}</td>
				<td align="right"><strong>适合年龄：</strong></td>
				<td>${fage}</td>
			</tr>
			<tr>
				<td align="right"><strong>活动时间：</strong></td>
				<td>${event.feventTime}</td>
				<td align="right"><strong>订单类型：</strong></td>
				<td>${forderType}</td>
				<td align="right"><strong>库存所在：</strong></td>
				<td>${fstockFlag}</td>
			</tr>
			<tr>
				<td align="right"><strong>原价信息：</strong></td>
				<td>${event.fprice}</td>
				<td align="right"><strong>现价信息：</strong></td>
				<td>${event.fdeal}</td>
				<td align="right"><strong>商家BD：</strong></td>
				<td>${bd}</td>
			</tr>
			<tr>
				<td align="right"><strong>场地/时长：</strong></td>
				<td>${siteAndDuration}</td>
				<td align="right"><strong>活动标签：</strong></td>
				<td>${event.ftag}</td>
				<td align="right"><strong>编辑人员：</strong></td>
				<td>${creater}</td>
			</tr>
			<tr>
				<td align="right"><strong>活动简介：</strong></td>
				<td colspan="5">${event.fbrief}</td>
			</tr>
			<tr>
				<td align="right"><strong>结算方式：</strong></td>
				<td colspan="2">${fsettlementType}</td>
				<td align="left"><strong>是否可用优惠卷：</strong></td>
				<td colspan="2">${fusePreferential}</td>
			</tr>
			<tr>
				<td align="right"><strong>活动亮点：</strong></td>
				<td colspan="5">${event.ffocus}</td>
			</tr>
		</table>
	</div>
	<div class="panel-footer"><p class="text-right" style="margin: 0;"><em>零到壹，查找优惠</em></p></div>
</div>
<div class="panel panel-success">
	<div class="panel-heading">
		<div class="row">
			<div class="col-md-2"><strong>活动场次</strong></div>
			<div class="col-md-10"><p class="text-right" style="margin-bottom: 0px;"><button id="createEventSessionBtn" type="button" class="btn btn-primary btn-sm"><span class="glyphicon glyphicon-plus-sign"></span> 创建【场次】</button></p></div>
		</div>
	</div>
	<div class="panel-body">
		<table id="eventSessionTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
		<div class="row">
			<div class="col-md-12"><p class="text-center">场次所属规格列表</p></div>
		</div>
		<table id="eventSpecTable3" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
	</div>
</div>
<div class="panel panel-danger">
	<div class="panel-heading">
		<div class="row">
			<div class="col-md-2"><strong>活动规格</strong></div>
			<div class="col-md-10"><p class="text-right" style="margin-bottom: 0px;"><button id="createEventSpecBtn" type="button" class="btn btn-primary btn-sm"><span class="glyphicon glyphicon-plus-sign"></span> 创建【规格】</button></p></div>
		</div>
	</div>
	<div class="panel-body">
		<table id="eventSpecTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
	</div>
</div>
<!--编辑活动场次开始-->
<div class="modal fade" id="createEventSessionModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="myModalLabel">编辑活动场次信息</h4>
			</div>
			<form id="createEventSessionForm" action="${ctx}/fxl/event/addEventSession" method="post" class="form-inline" role="form">
				<input id="id" name="id" type="hidden">
				<div class="modal-body">
					<div class="alert alert-danger text-center" role="alert" style="padding: 5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
					<div class="form-group has-error"><label for="ftitle">场次名称：</label>
					  <div class="input-group">
						<input type="text" id="ftitle" name="ftitle" class="form-control validate[required,minSize[2],maxSize[250]]" size="60"><div class="input-group-addon">输入  #FXL# 为换行符</div>
					  </div>
					</div>
					</br>
					<%-- <div class="form-group has-error"><label for="flocation">活动地点：</label>
						<input type="text" id="flocation" name="flocation" value="${flocation}" class="form-control validate[required,minSize[2],maxSize[250]]" size="50">
					</div> --%>
					<div class="form-group"><label for="location">百度地图坐标：</label>
						<div class="input-group" style="cursor: pointer;" data-toggle="modal" data-target="#baiduMapModal">
							<input type="text" id="fgps" name="fgps" value="${fgps}" class="form-control" readonly="readonly"> <span class="input-group-addon"><i class="glyphicon glyphicon-map-marker"></i></span>
						</div>
					</div>
					<c:if test="${event.fverificationType == 20}">
					<div class="form-group" style="margin-left: 30px;"><label for="fautoVerificationTime">自动核销时间：</label>
						<div id="fautoVerificationTimeDiv" class="input-group date form_datetime">
							<input id="fautoVerificationTime" name="fautoVerificationTime" type="text" class="form-control" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
						</div>
					</div>
					</c:if>
					<div class="form-group"><label for="faddress">活动地址：</label>
						<input type="text" id="faddress" name="faddress"  value="${faddress}" class="form-control validate[minSize[2],maxSize[250]]" size="85">
					</div>
					<div class="form-group has-error"><label for="fstartDate">活动日期：</label>
						<div id="fdateDiv" class="input-daterange input-group date" style="width:330px;">
							<input type="text" class="form-control validate[required]" id="fstartDate" name="fstartDate" style="cursor: pointer;"><span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
							<input type="text" class="form-control validate[required]" id="fendDate" name="fendDate" style="cursor: pointer;">
						</div>
					</div>
					<%-- <div class="form-group"><label for="flimitation">场次名额：</label>
						<div class="input-group">
							<input type="text" id="flimitation" name="flimitation" class="form-control validate[custom[integer],min[0],max[99999]]" size="5"><span class="input-group-addon"><i class="glyphicon glyphicon-education"></i></span>
							<select id="flimitationType" name="flimitationType" class="form-control validate[condRequired[flimitation]]">
								<option value=""><fmt:message key="fxl.common.select" /></option>
								<c:forEach var="limitationTypeItem" items="${limitationTypeMap}">
									<option value="${limitationTypeItem.key}">${limitationTypeItem.value}</option>
								</c:forEach>
							</select>
						</div>
					</div>
				 	<div class="form-group"><label for="fstartTime">开始时间：</label>
						<div id="fstartTimeDiv" class="input-group date form_datetime">
							<input id="fstartTime" name="fstartTime" type="text" class="form-control" style="cursor: pointer;" size="5"><span class="input-group-addon"><i class="glyphicon glyphicon-time"></i></span>
						</div>
					</div>
					<div class="form-group"><label for="fendTime">结束时间：</label>
						<div id="fendTimeDiv" class="input-group date form_datetime">
							<input id="fendTime" name="fendTime" type="text" class="form-control" style="cursor: pointer;" size="5"><span class="input-group-addon"><i class="glyphicon glyphicon-time"></i></span>
						</div>
					</div>
					<div class="form-group"><label for="frefoundPeriod">退款期限：</label>
						<div id="frefoundPeriodDiv" class="input-group date form_datetime">
							<input id="frefoundPeriod" name="frefoundPeriod" type="text" class="form-control" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
						</div>
					</div> --%>
					<div class="form-group"><label for="fdeadline">报名截止时间：</label>
						<div id="fdeadlineDiv" class="input-group date form_datetime">
							<input id="fdeadline" name="fdeadline" type="text" class="form-control validate[required]" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
						</div>
					</div>
					<div class="form-group"><label for="forder">场次排序号：</label>
						<div class="input-group">
							<input type="text" id="forder" name="forder" class="form-control validate[custom[integer],min[1],max[100]]" size="5"><div class="input-group-addon">请输入1-100之间整数，数值越大排序越靠前</div>
						</div>
					</div>
					<div class="form-group has-error"><label for="fsalesFlag" >是否参与促销：</label>
	                     <select id="fsalesFlag" name="fsalesFlag" class="validate[required] form-control">
		                  	<option value=""><fmt:message key="fxl.common.select" /></option>
		                	<c:forEach var="sale" items="${saleMap}"> 
			                <option value="${sale.key}">${sale.value}</option>
			                </c:forEach>
			             </select>
	                </div>
					<div style="clear: both;"></div>
				</div>
				<div class="modal-footer">
					<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span><fmt:message key="fxl.button.save" /></button>
					<button class="btn btn-warning" type="reset" id="resetBtn"><span class="glyphicon glyphicon-repeat"></span><fmt:message key="fxl.button.reset" /></button>
					<button class="btn btn-danger" type="button" id="delBtn"><span class="glyphicon glyphicon-trash"></span><fmt:message key="fxl.button.delete" /></button>
					<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span><fmt:message key="fxl.button.close" /></button>
				</div>
			</form>
		</div>
	</div>
</div>
<!--编辑活动场次结束-->
<!--百度地图开始-->
<div class="modal fade" id="baiduMapModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg" style="width: 1180px;">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">百度地图选点</h4>
      </div>
      <div class="modal-body">
      <div class="row">
      	<div class="col-md-3 text-right">地址搜索：</div>
		<div class="col-md-6"><input type="text" id="searchKey" name="searchKey" class="form-control" ></div>
      	<div class="col-md-3"><small>回车即可搜索</small><!-- <button id="mapSearchBtn" class="btn btn-primary" type="button"><span class="glyphicon glyphicon-search"></span> 搜索</button> --></div>
      </div>
      <p/>
      <div class="row">
      	<div class="col-md-8">
	      	<div class="well well-sm">
	  		<div id="fxlBaiduMap" style="height: 500px;width: 730px;"></div></div>
      	</div>
      	<div class="col-md-4">
			<div id="searchListDiv" class="list-group" style="max-height: 500px; overflow-y: auto;"></div>
      	</div>
      </div>
      <p/>
      <div class="row"><label for="baiduCoordinate" class="col-md-2 text-right">百度坐标值：</label>
		<div class="col-md-3"><input type="text" id="baiduCoordinate" name="baiduCoordinate" class="form-control" readonly="readonly"></div>
		<label for="baiduAddress" class="col-md-2 text-right">百度地址：</label>
		<div class="col-md-3"><input type="text" id="baiduAddress" name="baiduAddress" class="form-control" readonly="readonly"></div>
		<div class="col-md-2"></div>
	  </div>
      </div>
      <div class="modal-footer">
      	<button type="button" class="btn btn-primary" id="selectBaiduOkBtn"><span class="glyphicon glyphicon-ok"></span> <fmt:message key="fxl.button.ok" /></button>
		<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="fxl.button.close" /></button>
      </div>
    </div>
  </div>
</div>
<!--百度地图结束-->
<!--编辑规格开始-->
<div class="modal fade" id="createEventSpecModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">编辑活动规格信息</h4>
      </div>
      <form id="createEventSpecForm" action="${ctx}/fxl/event/addEventSpec" method="post" class="form-inline" role="form">
      <input id="specId" name="specId" type="hidden">
      <div class="modal-body">
      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
			<div class="form-group has-error"><label for="ftitle">规格名称：</label>
			   <div class="input-group">
				 <input type="text" id="ftitle" name="ftitle" class="form-control validate[required,minSize[2],maxSize[250]]" size="60"><div class="input-group-addon">输入  #FXL# 为换行符</div>
		       </div>
		    </div>
		    	
		    <div class="form-group"><label for="fdescription">规格描述：</label>
				<input type="text" id="fdescription" name="fdescription" class="form-control validate[minSize[2],maxSize[250]]" size="85">
			</div>
			<div class="form-group"><label for="fprice">原价格：</label>
				<div class="input-group">
		    		<input type="text" id="fprice" name="fprice" class="form-control validate[custom[number]]" size="5"><div class="input-group-addon">元</div>
		    	</div>
		    </div>
			<div class="form-group has-error"><label for="fdeal">现价格：</label>
				<div class="input-group">
		    		<input type="text" id="fdeal" name="fdeal" class="form-control validate[required,custom[number]]" size="5"><div class="input-group-addon">元</div>
		    	</div>
		    </div>
		    <c:if test="${event.fsettlementType == 30}">
			<div class="form-group has-error"><label for="fsettlementPrice">结算价格：</label>
				<div class="input-group">
		    		<input type="text" id="fsettlementPrice" name="fsettlementPrice" class="validate[required,custom[number]] form-control" size="5"><div class="input-group-addon">元</div>
		    	</div>
		    </div>
		    </c:if>
		    <div class="form-group"><label for="fadult">套内成人数：</label>
		    	<div class="input-group">
		    		<input type="text" id="fadult" name="fadult" class="validate[custom[integer],min[0],max[999]] form-control" size="5"><div class="input-group-addon">名成人</div>
		    	</div>
		    </div>
		    <div class="form-group"><label for="fchild">套内儿童数：</label>
		    	<div class="input-group">
		    		<input type="text" id="fchild" name="fchild" class="validate[custom[integer],min[0],max[999]] form-control" size="5"><div class="input-group-addon">名儿童</div>
		    	</div>
		    </div>
		    <div class="form-group"><label for="fpostage">快递费用：</label>
		    	<div class="input-group">
		    		<input type="text" id="fpostage" name="fpostage" class="validate[custom[number]] form-control" size="5"><div class="input-group-addon">元</div>
		    	</div>
		    </div>
		  	<%-- <div class="form-group"><label for="ftotal">总数量：</label>
		        <input type="text" id="ftotal" name="ftotal" class="validate[custom[integer]] form-control" size="5">
		    </div> --%>
			<div class="form-group"><label for="fstock">库存量：</label>
		        <input type="text" id="fstock" name="fstock" class="validate[required,custom[integer]] form-control" size="5">
		    </div>
<!-- 		    <div class="form-group"><label for="fstockUnit">库存单位：</label>
		        <input type="text" id="fstockUnit" name="fstockUnit" class="validate[condRequired[fstock],maxSize[30]] form-control"  size="5">
		    </div> -->
		    <div class="form-group"><label for="forder">规格排序号：</label>
		      	<div class="input-group">
		    		<input type="text" id="forder" name="forder" class="form-control validate[custom[integer],min[1],max[100]]" size="5"><div class="input-group-addon">请输入1-100之间整数，数值越大排序越靠前</div>
		    	</div>
		    </div>
		    <div style="clear:both;"></div>
      </div>
      <div class="modal-footer">
		<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span> <fmt:message key="fxl.button.save" /></button>
		<button class="btn btn-warning" type="reset" id="resetBtn2"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.reset" /></button>
		<button class="btn btn-danger" type="button" id="delBtn2"><span class="glyphicon glyphicon-trash"></span> <fmt:message key="fxl.button.delete" /></button>
		<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="fxl.button.close" /></button>
      </div>
      </form>
    </div>
  </div>
</div>
<!--编辑规格结束-->
<!--编辑场次所属规格开始-->
<div class="modal fade" id="createEventSpecModal3" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">编辑场次所属规格信息</h4>
      </div>
      <form id="createEventSpecForm3" action="${ctx}/fxl/event/addEventSpec" method="post" class="form-inline" role="form">
      <input id="specId" name="specId" type="hidden">
      <div class="modal-body">
      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>		    
		    <div class="form-group has-error"><label for="ftitle">规格名称：</label>
				<div class="input-group">
		    		<input type="text" id="ftitle" name="ftitle" class="form-control validate[required,minSize[2],maxSize[250]]" size="60"><div class="input-group-addon">输入  #FXL# 为换行符</div>
		    	</div>
		    </div>
		    <div class="form-group"><label for="fdescription">规格描述：</label>
				<input type="text" id="fdescription" name="fdescription" class="form-control validate[minSize[2],maxSize[250]]" size="85">
			</div>
			<div class="form-group has-error"><label for="fprice">原价格：</label>
				<div class="input-group">
		    		<input type="text" id="fprice" name="fprice" class="form-control validate[custom[number]]" size="5"><div class="input-group-addon">元</div>
		    	</div>
		    </div>
			<div class="form-group has-error" style="padding-left: 30px;"><label for="fdeal">现价格：</label>
				<div class="input-group">
		    		<input type="text" id="fdeal" name="fdeal" class="form-control validate[required,custom[number]]" size="5"><div class="input-group-addon">元</div>
		    	</div>
		    </div>
		    <c:if test="${event.fsettlementType == 30}">
			<div class="form-group has-error"  style="padding-left: 40px;"><label for="fsettlementPrice">结算价格：</label>
				<div class="input-group">
		    		<input type="text" id="fsettlementPrice" name="fsettlementPrice" class="validate[required,custom[number]] form-control" size="5"><div class="input-group-addon">元</div>
		    	</div>
		    </div>
		    </c:if>
		    <c:if test="${event.fsettlementType == 20}">
			<div class="form-group has-error"  style="padding-left: 40px;"><label for="fpointsPrice">扣点比例：</label>
				<div class="input-group">
		    		<input type="text" id="fpointsPrice" name="fpointsPrice" class="validate[required] form-control" size="5"><div class="input-group-addon">%</div>
		    	</div>
		    </div>
		    </c:if>
		    <div class="form-group has-error"><label for="fdistributionRebateAmount">返利金额：</label>
				<div class="input-group">
		    		<input type="text" id="fdistributionRebateAmount" name="fdistributionRebateAmount" class="form-control" size="5" ><div class="input-group-addon">元</div>
		    	</div>
		    </div>
		    <div class="form-group has-error" style="padding-left: 20px;"><label for="fdistributionRebateRatio">返利比例：</label>
				<div class="input-group">
		    		<input type="text" id="fdistributionRebateRatio" name="fdistributionRebateRatio" class="form-control" size="5"><div class="input-group-addon">%</div>
		    	</div>
		    </div>
		    <div class="form-group"><label for="fadult" style="padding-left: 20px;">套内成人数：</label>
		    	<div class="input-group">
		    		<input type="text" id="fadult" name="fadult" class="validate[custom[integer],min[0],max[999]] form-control" size="5"><div class="input-group-addon">成人</div>
		    	</div>
		    </div>
		    <div class="form-group"><label for="fchild">套内儿童数：</label>
		    	<div class="input-group">
		    		<input type="text" id="fchild" name="fchild" class="validate[custom[integer],min[0],max[999]] form-control" size="5"><div class="input-group-addon">儿童</div>
		    	</div>
		    </div>
		    <div class="form-group"><label for="fpostage">快递费用：</label>
		    	<div class="input-group">
		    		<input type="text" id="fpostage" name="fpostage" class="validate[custom[number]] form-control" size="5"><div class="input-group-addon">元</div>
		    	</div>
		    </div>
		    <%--  <div class="form-group"><label for="ftotal">总数量：</label>
		        <input type="text" id="ftotal" name="ftotal" class="validate[custom[integer]] form-control" size="5">
		    </div> --%>
		    <div class="form-group" style="padding-left: 20px;"><label for="fstock">库存量：</label>
		    	<div class="input-group">
		    		<input type="text" id="fstock" name="fstock" class="validate[required,custom[integer]] form-control" size="5"><div class="input-group-addon">个</div>
		    	</div>
		    </div>
		    
<!-- 		    <div class="form-group"><label for="fstockUnit">库存单位：</label>
		        <input type="text" id="fstockUnit" name="fstockUnit" class="validate[condRequired[fstock],maxSize[30]] form-control"  size="5">
		    </div> -->
		    <div class="form-group"><label for="forder">规格排序号：</label>
		      	<div class="input-group">
		    		<input type="text" id="forder" name="forder" class="form-control validate[custom[integer],min[1],max[100]]" size="5"><div class="input-group-addon">请输入1-100之间整数，数值越大排序越靠前</div>
		    	</div>
		    </div>
		    <div class="form-group"><label for="fexternalGoodsCode">外连系统商品号：</label>
		        <input type="text" id="fexternalGoodsCode" name="fexternalGoodsCode" class="form-control" size="50">
		    </div>
		    <div class="form-group has-error"><label for="frealNameType" >实名制类型：</label>
	                     <select id="frealNameType" name="frealNameType" class="form-control">
		                  	<option value=""><fmt:message key="fxl.common.select" /></option>
		                	<c:forEach var="frealitem" items="${frealNameTypeMap}"> 
			                <option value="${frealitem.key}">${frealitem.value}</option>
			                </c:forEach>
			             </select>
	        </div>
		    <div style="clear:both;"></div>
      </div>
      <div class="modal-footer">
		<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span> <fmt:message key="fxl.button.save" /></button>
		<button class="btn btn-warning" type="reset" ><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.reset" /></button>
		<button class="btn btn-danger" type="button" id="delBtn3"><span class="glyphicon glyphicon-trash"></span> <fmt:message key="fxl.button.delete" /></button>
		<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="fxl.button.close" /></button>
      </div>
      </form>
    </div>
  </div>
</div>
<!--编辑场次所属规格结束-->
</body>
</html>