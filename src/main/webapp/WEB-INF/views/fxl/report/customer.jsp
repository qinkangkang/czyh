<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
	<head>
		<title>Welcome</title>
		<%@ include file="/WEB-INF/views/include/include.jsp"%>
		<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
		<link href="${ctx}/styles/select2/css/select2.min.css" rel="stylesheet" type="text/css">
		<link href="${ctx}/styles/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css">
		<script src="${ctx}/styles/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
		<script src="${ctx}/styles/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js" charset="UTF-8"></script>
		<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
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
				
				$('#create2DateDiv').datepicker({
				    format : "yyyy-mm-dd",
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
						customerTable.ajax.reload(null,false);
			
						form.removeData("running");
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
				
				var customerTable = $("table#customerTable").DataTable({
					"language": dataTableLanguage,
					"filter": false,
					"autoWidth" : false,
				  	//"scrollY": "400px",
				  	"paging": true,
					"scrollCollapse": true,
					"serverSide": true,
					"stateSave": true,
					"deferRender": true,
					//"pagingType": "full_numbers",
					//"lengthMenu": [[10, 20, 30, 50], [10, 20, 30, 50]],
					"lengthChange": false,
					"displayLength" : "20",
					"ajax": {
					    "url": "${ctx}/fxl/report/getReportCustomerList",
			 		    "type": "POST",
			 		    "data": function (data) {
			 		    	$.each(searchForm.serializeArray(), function(i, n){
			 		    		data[n.name] = n.value;
			 				});
			 		        return data;
			 		    }
					},
					"columns" : [
					{
						"title" : '<center>序号</center>',
						"className": "text-center",
						"width" : "50px",
						"orderable" : false
					}, {
						"title" : '<center>用户名称</center>',
						"data" : "name",
						"className": "text-center",
						"width" : "100px",
						"orderable" : false
					}, {
						"title" : '<center>性别</center>',
						"data" : "sex",
						"className": "text-center",
						"width" : "100px",
						"orderable" : false
					}, {
						"title" : '<center>联系电话</center>',
						"data" : "phone",
						"className": "text-center",
						"width" : "100px",
						"orderable" : false
					}, {
						"title" : '<center>注册时间</center>',
						"data" : "registerTime",
						"className": "text-center",
						"width" : "100px",
						"orderable" : false
					}, {
						"title" : '<center>注册渠道</center>',
						"data" : "registerChannel",
						"className": "text-center",
						"width" : "100px",
						"orderable" : false
					}, {
						"title" : '<center>0元单支付数量</center>',
						"data" : "zeroOrderNumber",
						"className": "text-center",
						"width" : "100px",
						"orderable" : false
					}, {
						"title" : '<center>非零元单支付数量</center>',
						"data" : "payOrderNumber",
						"className": "text-center",
						"width" : "100px",
						"orderable" : false
					}, {
						"title" : '<center>累计支付金额</center>',
						"data" : "total",
						"className": "text-center",
						"width" : "100px",
						"orderable" : false
					}] 
				});
				
				//序号
				customerTable.on( 'order.dt search.dt', function () {
					customerTable.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
			            cell.innerHTML = i+1;
			        } );
			    } ).draw();
			
			});
		</script>
	</head>
	<body>
		<div class="row">
		  <div class="col-md-10"><h3>用户报表</h3></div>
		  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
		</div>
		<div class="selectDiv">
			<form id="searchForm" action="${ctx}/fxl/report/getReportCustomerList" method="post" class="form-inline" role="form">
				<div class="form-group" >
					<label> 注册时间：</label>
					<div id="createDateDiv" class="input-daterange input-group date" style="width:250px;">
						<input id="fdeadline" name="fcreateTimeStart" type="text" class="form-control" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
					</div>
					<div id="create2DateDiv" class="input-daterange input-group date" style="width:250px;">
						<span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
						<input id="fdeadline" name="fcreateTimeEnd" type="text" class="form-control" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
					</div>
				</div>
				<div class="form-group"><label for="s_fphone">手机号码：</label>
					<input type="text" id="s_fphone" name="s_fphone" class="form-control input-sm" >
				</div>
				<div class="form-group"><label for="s_fweixinName">微信昵称：</label>
					<input type="text" id="s_fweixinName" name="s_fweixinName" class="form-control input-sm" >
				</div>
				<div class="form-group"><label for="s_ftype">用户注册渠道：</label>
					<select id="" name="registerChannel" class="form-control input-sm">
						<option value="all"><fmt:message key="fxl.common.all" /></option>
						<c:forEach var="registrationChannelItem" items="${registrationChannelMap}"> 
							<option value="${registrationChannelItem.key}">${registrationChannelItem.value}</option>
						</c:forEach>
					</select>
				</div>
				<div class="form-group"><label for="s_flevel">用户标签：</label>
					<select id="" name="tag" class="form-control input-sm">
						<option value="0"><fmt:message key="fxl.common.all" /></option>
						<c:forEach var="userTagItem" items="${userTagMap}"> 
							<option value="${userTagItem.key}">${userTagItem.value}</option>
						</c:forEach>
					</select>
				</div><br/>
				<div class="form-group"><label for="s_ftype">用户信息完整度：</label>
					<input class="form-control input-sm" type="radio" name="phone" value="0" checked = "checked">全部
				</div>
				<div class="form-group">
					<input class="form-control input-sm" type="radio" name="phone" value="1">有手机号码
				</div>
				<div class="form-group">
					<input class="form-control input-sm" type="radio" name="phone" value="2">无手机号码
				</div>
				
				<div class="form-group"><label for="s_ftype">是否下过单：</label>
					<input class="form-control input-sm" type="radio" name="order" value="0" checked = "checked">所有
				</div>
				<div class="form-group">
					<input class="form-control input-sm" type="radio" name="order" value="1">未下单
				</div>
				<div class="form-group">
					<input class="form-control input-sm" type="radio" name="order" value="2">下过单
				</div>
				<button id="select" type="submit" class="btn btn-primary">
				<span class="glyphicon glyphicon-play"></span>
				<fmt:message key="fxl.button.query" /></button>
				<button id="clear" type="button" class="btn btn-warning">
				<span class="glyphicon glyphicon-repeat"></span>
				<fmt:message key="fxl.button.clear" /></button>
			</form>
			<table id="customerTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
		</div>
	</body>
</html>