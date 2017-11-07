<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<script type="text/javascript">
$(document).ready(function () {
	
	$('#updateDictionaryBtn').on('click',function(e) {
		$.post("${ctx}/fxl/admin/system/updateDictionary", function(data) {
			if(data.success){
				toastr.success(data.msg);
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	$('#updateEventCategoryBtn').on('click',function(e) {
		$.post("${ctx}/fxl/admin/system/updateEventCategory", function(data) {
			if(data.success){
				toastr.success(data.msg);
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	$('#eventDetailHtmlBtn').on('click',function(e) {
		$.post("${ctx}/fxl/admin/system/eventDetailHtml", function(data) {
			if(data.success){
				toastr.success(data.msg);
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	$('#merchantDetailHtmlBtn').on('click',function(e) {
		$.post("${ctx}/fxl/admin/system/merchantDetailHtml", function(data) {
			if(data.success){
				toastr.success(data.msg);
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});

	$('#merchantNumberBtn').on('click',function(e) {
		$.post("${ctx}/fxl/admin/system/merchantNumber", function(data) {
			if(data.success){
				toastr.success(data.msg);
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	$('#eventStockBtn').on('click',function(e) {
		$.post("${ctx}/fxl/admin/system/eventStockStatistical", function(data) {
			if(data.success){
				toastr.success(data.msg);
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	$('#czyhServiceUrlBtn').on('click',function(e) {
		$.post("${ctx}/fxl/admin/system/setczyhServiceUrl", $.param({url:$('#czyhServiceUrl').val()},true), function(data) {
			if(data.success){
				toastr.success(data.msg);
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	$('#updateConfigurationBtn').on('click',function(e) {
		$.post("${ctx}/fxl/admin/system/updateConfiguration", function(data) {
			if(data.success){
				toastr.success(data.msg);
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
});
</script>
</head>
<body>
<div class="row">
  <div class="col-md-10"><h3>系统维护</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<h4>系统维护项目</h4>
<table id="systemTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered dataTable no-footer" role="grid">
	<thead>
		<tr role="row">
			<th class="text-center sorting_disabled" rowspan="1" colspan="1" aria-label="选择" style="width: 400px;"><center>系统维护功能项</center></th>
			<th class="text-center sorting_disabled" rowspan="1" colspan="1" aria-label="栏目编码" style="width: 60px;"><center>操作</center></th>
		</tr>
	</thead>
	<tbody>
	    <tr role="row" class="odd">
			<td class="text-center">更新系统配置表缓存</td>
			<td class="text-center">
			<button id="updateConfigurationBtn" class="btn btn-success" type="button" data-dismiss="modal">
				<span class="glyphicon glyphicon-refresh"></span> 更新</button></td>
		</tr>
		<tr role="row" class="odd">
			<td class="text-center">更新系统数据字典缓存</td>
			<td class="text-center">
			<button id="updateDictionaryBtn" class="btn btn-success" type="button" data-dismiss="modal">
				<span class="glyphicon glyphicon-refresh"></span> 更新</button></td>
		</tr>
		<tr role="row" class="even">
			<td class="text-center">更新活动类目缓存</td>
			<td class=" text-center">
			<button id="updateEventCategoryBtn" class="btn btn-success" type="button" data-dismiss="modal">
				<span class="glyphicon glyphicon-refresh"></span> 更新</button>
			</td>
		</tr>
		<tr role="row" class="odd">
			<td class="text-center">活动详情介绍页面静态化</td>
			<td class="text-center">
			<button id="eventDetailHtmlBtn" class="btn btn-success" type="button" data-dismiss="modal">
				<span class="glyphicon glyphicon-inbox"></span> 后台生成</button></td>
		</tr>
		<tr role="row" class="even">
			<td class="text-center">商家详情介绍页面静态化</td>
			<td class=" text-center">
			<button id="merchantDetailHtmlBtn" class="btn btn-success" type="button" data-dismiss="modal">
				<span class="glyphicon glyphicon-inbox"></span> 后台生成</button>
			</td>
		</tr>
		<tr role="row" class="odd">
			<td class="text-center">重新生成商家编码</td>
			<td class=" text-center">
			<button id="merchantNumberBtn" class="btn btn-success" type="button" data-dismiss="modal">
				<span class="glyphicon glyphicon-inbox"></span> 生成</button>
			</td>
		</tr>
		<tr role="row" class="even">
			<td class="text-center">重新统计活动表中的库存信息</td>
			<td class=" text-center">
			<button id="eventStockBtn" class="btn btn-success" type="button" data-dismiss="modal">
				<span class="glyphicon glyphicon-inbox"></span> 统计</button>
			</td>
		</tr>
		<tr role="row" class="odd">
			<td class="text-center">更新零到壹服务接口地址信息
			<input type="text" id="czyhServiceUrl" name="czyhServiceUrl" class="form-control" value="${czyhServiceUrl}"></td>
			<td class=" text-center">
			<button id="czyhServiceUrlBtn" class="btn btn-success" type="button" data-dismiss="modal">
				<span class="glyphicon glyphicon-refresh"></span> 更新</button>
			</td>
		</tr>
	</tbody>
</table>
<!--系统维护modal开始-->
<div class="modal fade" id="systemModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
	aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="myModalLabel">系统维护提示</h4>
			</div>
			<div class="modal-body">
			</div>
			<div class="modal-footer">
				<button class="btn btn-default" type="button" data-dismiss="modal">
					<span class="glyphicon glyphicon-remove"></span><fmt:message key="fxl.button.close" />
				</button>
			</div>
		</div>
	</div>
</div>
<!--系统维护Modal结束-->
</body>
</html>