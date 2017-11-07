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
<script type="text/javascript">
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-8"><h3>嘉年华活动详细信息</h3></div>
  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>嘉年华信息列表</h4></div>
</div>

<!--活动详情开始-->
<!-- 	<div class="modal-dialog modal-lg"> -->
		<div class="modal-content">

			<div class="modal-body">
				<div class="panel panel-primary">
					<div class="panel-heading"><strong>嘉年华活动信息</strong></div>
					<div class="panel-body">
						<table id="carnivalDetailTable" class="table table-hover table-striped table-bordered">
							<tr>
								<td width="15%" align="center"><strong>活动名称</strong></td>
								<td width="35%" colspan="3" align="center"><div>${ftitle}</div></td>
							</tr>
							<tr>
								<td width="15%" align="center"><strong>开始时间</strong></td>
								<td width="35%" align="center"><div>${fstartTime}</div></td>
								<td width="15%" align="center"><strong>结束时间</strong></td>
								<td width="35%" align="center"><div>${fendTime}</div></td>
							</tr>
							<tr>
								<td align="center"><strong>活动状态</strong></td>
								<td align="center"><div >${fstatus}</div></td>
								<td align="center"><strong>整体中奖率</strong></td>
								<td align="center"><div >${fodds}</div></td>
							</tr>
							<tr>
								<td align="center"><strong>活动天数</strong></td>
								<td align="center"><div >${fdayNumber}</div></td>
								<td align="center"><strong>每个人抽奖次数</strong></td>
								<td align="center"><div >${flotteryNumber}</div></td>
							</tr>
							<tr>
								<td align="center"><strong>兑奖碎片数</strong></td>
								<td align="center" colspan="4"><div >${fcredentialNumber}</div></td>
							</tr>
							<tr>
								<td align="center"><strong>寻宝游戏链接</strong></td>
								<td align="center"  colspan="4"><div >${carnivalUrl}</div></td>
							</tr>
						</table>
						</div>
						</div>
						
					  <div class="panel panel-info">
					  <div class="panel-heading"><strong>奖品信息</strong></div>
					 
					  <table id="carnivalDetailTable" class="table table-hover table-striped table-bordered">
							<tr>
							    <td width="10%" align="center"><strong>奖品等级</strong></td>
								<td width="10%" align="center"><strong>发放天数</strong></td>
								<td width="10%" align="center"><strong>开始发放日期</strong></td>
								<td width="10%" align="center"><strong>计划发放库存</strong></td>
								<td width="10%" align="center"><strong>已发放</strong></td>
							</tr>
							<c:forEach items="${cPrizeOneList}" var="cPrizeOneListMap">
							 <tr>
							    <td><div></div>${cPrizeOneListMap.flevel}</td>
								<td><div></div>${cPrizeOneListMap.fcarnivalDaySerial}</td>
								<td><div></div>${cPrizeOneListMap.fcarnivalDay}</td>
								<td><div></div>${cPrizeOneListMap.fcount}</td>
								<td><div></div>${cPrizeOneListMap.facceptCount}</td>
							 </tr>
							</c:forEach>
					  </table>
					 
					  </div>
					    
					   <div class="panel panel-warning">
					     <div class="panel-heading"><strong>奖品信息</strong></div>
					     <table id="carnivalDetailTable" class="table table-hover table-striped table-bordered">
							<tr>
							    <td width="10%" align="center"><strong>奖品等级</strong></td>
								<td width="10%" align="center"><strong>发放天数</strong></td>
								<td width="10%" align="center"><strong>开始发放日期</strong></td>
								<td width="10%" align="center"><strong>计划发放库存</strong></td>
								<td width="10%" align="center"><strong>已发放</strong></td>
							</tr>
							<c:forEach items="${cPrizeTwoList}" var="cPrizeTwoListMap">
							 <tr>
							    <td><div id="getFremainingStock2"></div>${cPrizeTwoListMap.flevel}</td>
								<td><div id="getFremainingStock2"></div>${cPrizeTwoListMap.fcarnivalDaySerial}</td>
								<td><div id="getFremainingStock2"></div>${cPrizeTwoListMap.fcarnivalDay}</td>
								<td><div id="getFremainingStock2"></div>${cPrizeTwoListMap.fcount}</td>
								<td><div id="getFremainingStock2"></div>${cPrizeTwoListMap.facceptCount}</td>
							 </tr>
							</c:forEach>
						 </table>
					   </div>
					   
					  <div class="panel panel-danger">
					  <div class="panel-heading"><strong>奖品信息</strong></div>
					  <table id="carnivalDetailTable" class="table table-hover table-striped table-bordered">
							<tr>
							    <td width="10%" align="center"><strong>奖品等级</strong></td>
								<td width="10%" align="center"><strong>发放天数</strong></td>
								<td width="10%" align="center"><strong>开始发放日期</strong></td>
								<td width="10%" align="center"><strong>计划发放库存</strong></td>
								<td width="10%" align="center"><strong>已发放</strong></td>
							</tr>
						<c:forEach items="${cPrizeThreeList}" var="cPrizeThreeListMap">
							 <tr>
							    <td><div id=""></div>${cPrizeThreeListMap.flevel}</td>
								<td><div id=""></div>${cPrizeThreeListMap.fcarnivalDaySerial}</td>
								<td><div id=""></div>${cPrizeThreeListMap.fcarnivalDay}</td>
								<td><div id=""></div>${cPrizeThreeListMap.fcount}</td>
								<td><div id=""></div>${cPrizeThreeListMap.facceptCount}</td>
							 </tr>
						</c:forEach>
						</table>
					   </div>
					 <div class="panel-footer"><p class="text-right" style="margin: 0;"><em>零到壹，查找优惠</em></p></div>
			      </div>		   	
			<div class="modal-footer">
			</div>
	</div>
<!--活动详情结束-->

</body>
</html>