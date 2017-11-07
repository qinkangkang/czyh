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
$(document).ready(function(){
	$.cookie('fxlUsername', '<shiro:principal type="com.czyh.czyhweb.security.ShiroDbRealm$ShiroUser" property="loginName"></shiro:principal>',{expires:30});
	
	var searchForm = $('#searchForm');
	
	searchForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	$('#createDateDiv').datepicker({
	    format : "yyyy-mm-dd",
	    endDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	$('#createDateDiv').datepicker({
	   'update':"2016-05-26",
	});
	
	$("#select").click(function(e) {
		eventTable.ajax.reload();
		/* if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			
			form.removeData("running");
		} */
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
			$.post(form.attr("action"), form.serialize(), function(data){
				if(data.success){
					customerTable.clear();
					customerTable.rows.add(data.list).draw();
					//var dataList = data.list;
					/* orderTable.clear();
					orderTable.rows.add(data.list).draw(); */
					 /* for (var i = 0;i < data.list.length; i++) {
						 orderTable.row.add(
									data.list[i]
							    ).draw();
						 var status = dataList[i].orderStatus;
						if (status === 10) {
							dataList[i].title = "代付款";
						}else if (status === 20) {
							dataList[i].title = "已支付";
						} else if (status === 60) {
							dataList[i].title = "已核销未评价";
						}else if (status === 70) {
							dataList[i].title = "完成";
						} else if (status === 100) {
							dataList[i].title = "主动取消";
						} else if (status === 109) {
							dataList[i].title = "订单过期取消";
						} else if (status === 110) {
							dataList[i].title = "退款申请中";
						} else if (status === 120) {
							dataList[i].title = "退款成功";
						} else {
							dataList[i].title = "合计";
						} 
					} */
					
				}else{
					toastr.error(data.msg);
				}
				form.removeData("running");
			}, "json");
		}
	});
	/* $('#countdown_dashboard').countDown({
		targetDate: {
			'day': 26,
			'month': 3,
			'year': 2016,
			'hour': 0,
			'min': 0,
			'sec': 0
		}
	}) */
	
	/* $("#s_fsponsor").select2({
		placeholder: "选择一个用户标签",
		allowClear: true,
		language: pickerLocal
	}); */
	
	var eventTable = $("table#eventTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
		"paging": false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"deferRender": true,
		"lengthChange": false,
		"retrieve": true,
		"ajax": {
		    "url": "${ctx}/fxl/report/getReportEventStatusList",
 		    "type": "POST",
 		    "data": function (data) {
 		    	$.each(searchForm.serializeArray(), function(i, n){
 		    		data[n.name] = n.value;
 				});
 		        return data;
 		    }
		},
		//"deferRender": true,
		//"pagingType": "full_numbers",
		//"lengthChange": false,
		"columns" : [
		{
			"title" : '<center>序号</center>',
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>活动名称</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		}, {
			"title" : '<center>操作</center>',
			"data" : "operate",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		}, {
			"title" : '<center>现在状态</center>',
			"data" : "name",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},] 
	});
	
	//序号
	eventTable.on( 'order.dt search.dt', function () {
		eventTable.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
            cell.innerHTML = i+1;
        } );
    } ).draw();

});
</script>
</head>
<body>
<div class="row">
  <div class="col-md-10"><h3>活动状态报表</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="selectDiv">
	<form id="searchForm" action="${ctx}/fxl/report/getReportEventStatusList" method="post" class="form-inline" role="form">
		<div class="form-group" >
			<label>查询时间：</label>
			<div class="input-daterange input-group date" id="createDateDiv" style="width:130px;">
			    <input type="text" class="form-control input-sm validate[required] form-control" name="fcreateTimeStart" style="cursor: pointer;">
			</div>
		</div>
		
		<div class="form-group">
			<input class="form-control input-sm" type="radio" name="status" value="0" checked = "checked">所有
		</div>
		<div class="form-group">
			<input class="form-control input-sm" type="radio" name="status" value="1">上架
		</div>
		<div class="form-group">
			<input class="form-control input-sm" type="radio" name="status" value="2">下架
		</div>
		<div class="form-group">
			<input class="form-control input-sm" type="radio" name="status" value="3">录入
		</div>
		
		<button id="select" type="button" class="btn btn-primary">
		<span class="glyphicon glyphicon-play"></span>
		<fmt:message key="fxl.button.query" /></button>
		<button id="clear" type="button" class="btn btn-warning">
		<span class="glyphicon glyphicon-repeat"></span>
		<fmt:message key="fxl.button.clear" /></button>
	</form>
	<table id="eventTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
</div>
</body>
</html>