<%@ page contentType="text/html;charset=UTF-8" %>
<meta name="decorator" content="/WEB-INF/decorators/decorator_no_theme.jsp">
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<script type="text/javascript">
$(document).ready(function() {
	
	var eventId = $("#eventId");
	
	var ftag = $("#ftag").select2({
		placeholder: "请选择一个或多个活动标签",
		allowClear: true,
		language: pickerLocal
	});
	
	var createEventBForm = $('#createEventBForm');
	
	createEventBForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	createEventBForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			$.post(form.attr("action"), $.param($.merge(form.serializeArray(),[{name:"eventId", value:$("#eventId").val()}]),true), function(data){
				if(data.success){
					toastr.success(data.msg);
					$('#eventTab a:eq(2)').tab('show');
				}else{
					toastr.error(data.msg);
				}
				form.removeData("running");
			}, "json");
		}
	});
	
	$("#fonSaleTimeDiv").datetimepicker({
	    format : "yyyy-mm-dd hh:mm:ss",
	    startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	   /*  ,
	    pickerPosition: "top-left"  */
	});
	
	
    var createEventInfoForm = $('#createEventInfoForm');
	
    createEventInfoForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	createEventInfoForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			$.post(form.attr("action"), $.param($.merge(form.serializeArray(),[{name:"eventId", value:$("#eventId").val()}]),true), function(data){
				if(data.success){
					toastr.success(data.msg);
					eventTable.ajax.reload(null,false);
				}else{
					toastr.error(data.msg);
				}
				form.removeData("running");
			}, "json");
		}
	});

	$("#fcity option[value='${fcity}']").prop("selected",true);
	$("#fsponsor option[value='${fsponsor}']").prop("selected",true);
	$("#fdistribution option[value='${fdistribution}']").prop("selected",true);
	$("#fbdId option[value='${fbdId}']").prop("selected",true);
	$("#fcreaterId option[value='${fcreaterId}']").prop("selected",true);
	$("#fexternalSystemType option[value='${fexternalSystemType}']").prop("selected",true);
	$("#fsellModel option[value='${fsellModel}']").prop("selected",true);
	$("#fpromotionModel option[value='${fpromotionModel}']").prop("selected",true);
	$("#fgoodsTag option[value='${fgoodsTag}']").prop("selected",true);
	$("#fsadelModel option[value='${fsadelModel}']").prop("selected",true);
	$("#fUsePreferential option[value='${fUsePreferential}']").prop("selected",true);
	

	var SpecAndPackingTable=$("table#SpecAndPackingTable").DataTable({
		"language":dataTableLanguage,
		"filter":false,
		"autoWidth":false,
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax":{
			"url": "${ctx}/fxl/event/getSpecList?fgoodsId=${eventId}",
 		    "type": "POST",
 		    "data": function (data) {
 		        return data;
 		    }
		},
		"stateSave": true,
		"deferRender": true,
		"lengthMenu": [[ 20, 30, 50,100], [20, 30, 50,100]],
		"lengthChange": false,
		"displayLength" : datatablePageLength,
		"columns" : [
			{	"title" : "ID",
				"data" : "DT_RowId",
				"visible": false,
				"orderable" : false
			},{
				"title" : '<center>规格名称</center>',
				"data" : "fspaceName",
				"className": "text-center",
				"width" : "50px",
				"orderable" : false
			},{
				"title" : '<center>规格值</center>',
				"data" : "fvalueName",
				"className": "text-center",
				"width" : "50px",
				"orderable" : false
			},{
				"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
				"data" : null,
				"width" : "50px",
				"className": "text-center",
				"orderable" : false
			}],
			"columnDefs" : [{
				"targets" : [3],
				"render" : function(data, type, full) {
					var retString = '<button id="edit" mId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs"><fmt:message key="fxl.button.edit" /></button><button id="del" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" /></button>';
					return retString;
				}
			}]
		
	})
	
	
	$("#fsponsor").select2({
		placeholder: "选择一个活动商家",
		allowClear: true,
		language: pickerLocal
	});
	
	var ftagVal = "${ftag}";
	if($.trim(ftagVal) != ""){
		ftag.val(ftagVal.split(";")).trigger("change");
	}
	
	$("#btn_center").click(function () {
    	popCenterWindow();  
    });  
        
	
	//附加属性显示表格
	var eventTable = $("table#eventTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"paging": false,
		"autoWidth" : false,
	  	//"scrollY": "350px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/event/getTEventExtInfo/" + eventId.val(),
 		    "type": "POST",
 		    "data": function (data) {
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
			"title" : '<center>属性名称</center>',
			"data" : "fname",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>文案提示</center>',
			"data" : "fprompt",
			"className": "text-center",
			"width" : "150px",
			"orderable" : false
		}, {
			"title" : '<center>是否必填项</center>',
			"data" : "fisRequired",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>是否根据人数累加</center>',
			"data" : "fisEveryone",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>活动排序</center>',
			"data" : "forder",
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
			"targets" : [6],
			"render" : function(data, type, full) {
				return '<div class="btn-group btn-group-xs" role="group" aria-label="零到壹，查找优惠"></button><button id="del" mId="' + full.DT_RowId + '" type="button" class="btn btn-success btn-xs"><fmt:message key="fxl.button.delete" /></button></div>';
			}
		}, {
			"targets" : [5],
			"render" : function(data, type, full) {
				return '<div class="input-group"><input type="text" id="eventOrder_' + full.DT_RowId + '" class="form-control validate[required,custom[integer],min[-10],max[5]] input-sm" size="1" value="' + data + '"><span class="input-group-btn"><button id="saveOrderBtn" class="btn btn-default btn-sm" type="button" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></button></span></div>';
			}
		}, {
			"targets" : [4],
			"render" : function(data, type, full) {
				return '<div class="input-group"><input type="text" id="eventIsEveryone_' + full.DT_RowId + '" class="form-control validate[required] input-sm" size="1" value="' + data + '"><span class="input-group-btn"><button id="saveOrderBtn" class="btn btn-default btn-sm" type="button" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></button></span></div>';
			}
		},  {
			"targets" : [3],
			"render" : function(data, type, full) {
				return '<div class="input-group"><input type="text" id="eventIsRequired_' + full.DT_RowId + '" class="form-control validate[required] input-sm" size="1" value="' + data + '"><span class="input-group-btn"><button id="saveOrderBtn" class="btn btn-default btn-sm" type="button" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></button></span></div>';
			}
		},  {
			"targets" : [2],
			"render" : function(data, type, full) {
				return '<div class="input-group"><input type="text" id="eventPrompt_' + full.DT_RowId + '" class="form-control validate[required] input-sm" size="90" value="' + data + '"><span class="input-group-btn"><button id="saveOrderBtn" class="btn btn-default btn-sm" type="button" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></button></span></div>';
			}
		},  {
			"targets" : [1],
			"render" : function(data, type, full) {
				return '<div class="input-group"><input type="text" id="eventName_' + full.DT_RowId + '" class="form-control validate[required] input-sm" size="10" value="' + data + '"><span class="input-group-btn"><button id="saveOrderBtn" class="btn btn-default btn-sm" type="button" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></button></span></div>';
			}
		}]
	});
	
	$("#eventTable").delegate("button[id=saveOrderBtn]", "click", function(){
		var mId = $(this).attr("mId");
		var eventOrder = $('#eventOrder_'+mId);
		var eventPrompt = $('#eventPrompt_'+mId);
		var eventName = $('#eventName_'+mId);
		var eventIsRequired = $('#eventIsRequired_'+mId);
		var eventIsEveryone = $('#eventIsEveryone_'+mId);
		if(!eventOrder.validationEngine('validate')){
	    	$.post("${ctx}/fxl/event/editEventTexInfo", $.param({id:mId,forder:eventOrder.val(),fprompt:eventPrompt.val(),fname:eventName.val(),fisRequired:eventIsRequired.val(),fisEveryone:eventIsEveryone.val()},true), function(data) {
				if(data.success){
					toastr.success(data.msg);
					eventTable.ajax.reload(null,false);
				}else{
					toastr.error(data.msg);
				}
			}, "json");
		}
	});
	
	$("#eventTable").delegate("button[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该扩展属性吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/event/deleteEventExtInfo/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						eventTable.ajax.reload(null,false);
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
	
	
	
	
});

 function saveShop(){
	 alert(1);	
	 //alert($("#fspaceName").val());
	 $.ajax({ type:'post', url:'${ctx}/fxl/event/addSpecAndPacking',
           dataType:'json',   
           data:{
	      	   'fspaceName':$("#fspaceName").val(),
	      	   'fvalueName':$("#fvalueName").val(),
	      	  // 'id':$("#id").val(),
	      	   'fgoodsId':$("#fgoodsId").val(),
      	   } ,
      	   success:function(data){
      		   console.log(data);
      		   //return data;
      	   }
      	   //$(this).resetFrom();
         });
	 /* $('#editSpecAndPackingForm').on('submit', function() {
	        var fspaceName = $('spaceName]').val();
	          var  fvalueName = $('input[name=fvalueName]').val();

	          $(this).ajaxSubmit({
 				type:'post',
 	             url:'${ctx}/fxl/event/addSpecAndPacking'
 	             dataType:'json',   
 	             data:{
 	        	   'fspaceName':fspaceName,
 	        	   'fvalueName':fvalueName
 	        	   } ,
 	        	   success:function(data){
 	        		   return data;
 	        	   }
 	        	   $(this).resetFrom();
 	           });
	          return false;
 	             
 		}); */
	 		 
} 

</script>
<style type="text/css">
h3 {
	font-weight: bold;
}

.window{  
    width:400px;  
    border:1px solid #DDDDDD;
    position:absolute;  
    padding:2px;  
    margin-top:930px; 
    display:none;  
    }  
.content{  
    background-color:#FFF;  
    font-size:14px;  
    overflow:auto;  
    }  
.win_title{  
    padding:2px;  
    color:black;  
    font-size:14px;  
    }  
.win_title div{  
    float:right;  
    }
    

    
</style>
<div>
	<form id="createEventBForm" action="${ctx}/fxl/event/addEventB" method="post" class="form-inline" role="form">
	<!-- Ⅰ.活动基础信息： -->
	<blockquote><h3>Ⅰ.商品基础信息:</h3></blockquote>
	<div class="form-group has-error"><label for="fcity">所属城市：</label>
	<select id="fcity" name="fcity" class="validate[required] form-control">
		<option value=""><fmt:message key="fxl.common.select" /></option>
		<c:forEach var="cityItem" items="${cityMap}"> 
		<option value="${cityItem.key}">${cityItem.value}</option>
		</c:forEach>
	</select>
	</div>
	<div class="form-group has-error"><label for="fsponsor">商品商家：</label>
	      <select id="fsponsor" name="fsponsor" class="validate[required] form-control" style="width: 200px;">
		<option value=""><fmt:message key="fxl.common.select" /></option>
		<c:forEach var="sponsorItem" items="${sponsorMap}"> 
		<option value="${sponsorItem.key}">${sponsorItem.value}</option>
		</c:forEach>
	</select>
	</div>
	<!--  <div class="form-group"><label for="fprice">推荐分数：</label>
		<input type="text" id="fbaseScore" name="fbaseScore" class="validate[maxSize[30]] form-control" value="${fbaseScore}" size="15">
	</div>
	-->
	 <div class="form-group has-error"><label for="ftitle">商品标题：</label>
	       <input type="text" id="ftitle" name="ftitle" class="form-control validate[required,minSize[2],maxSize[50]]" value="${ftitle}" size="110">
	 </div>
	 <div class="form-group has-error"><label for="fsubTitle">副标题：</label>
	       <input type="text" id="fsubTitle" name="fsubTitle" class="form-control validate[minSize[2],maxSize[50]]" value="${fsubTitle}" size="110">
	 </div>
	 <br/>
 	<div class="form-group has-error"><label for="fsellModel">售卖模式：</label>
        <select id="fsellModel" name="fsellModel" class="validate[required] form-control" style="width: 120px;">
			<option value=""><fmt:message key="fxl.common.select" /></option>
			<c:forEach var="Item" items="${fsellModelMap}"> 
			<option value="${Item.key}">${Item.value}</option>
			</c:forEach>
		</select>
    </div>
	<%-- <div class="form-group"><label for="fspec">商品规格：</label>
		<input type="text" id="fspec" name="fspec" value="${fspec}" class="form-control validate[minSize[1],maxSize[250]]" size="20">
	</div> --%>
		<div class="form-group has-error"><label for="fsadelModel">促销模块：</label>
	        <select id="fsadelModel" name="fsadelModel" class="validate[required] form-control" style="width: 120px;">
				<c:forEach var="Item" items="${fsadelModelMap}"> 
				<option value="${Item.key}">${Item.value}</option>
				</c:forEach>
			</select>
	    </div>
	  	<div class="form-group"><label for="fUsePreferential">可否用优惠：</label>
		     <select id="fUsePreferential" name="fUsePreferential" class="form-control" style="width: 120px;">
		         <option value="1" selected='selected' >不可用</option>   
                 <option value="0">可用</option>
			</select>
		</div>
		
		<div class="form-group"><label for="fprice">商品原价：</label>
		 	<div class="input-group">
				<input type="text" id="fprice" name="fprice" class="validate[required] form-control" value="${fprice}" size="4">
			</div>
		</div>
		<div class="form-group"><label for="fpriceMoney">商品售价：</label>
		 	<div class="input-group">
				<input type="text" id="fpriceMoney" name="fpriceMoney" class=" validate[required] form-control" value="${fpriceMoney}" size="4">
			</div>
		</div><br>
		<div class="form-group"><label for="ftotal">商品总库存：</label>
		 	<div class="input-group">
				<input type="text" id="ftotal" name="ftotal" class="validate[required] form-control" value="${ftotal}" size="4">
			</div>
		</div>
		<div class="form-group"><label for="fstock">商品剩余库存：</label>
		 	<div class="input-group">
				<input type="text" id="fstock" name="fstock" class=" validate[required] form-control" value="${fstock}" size="4">
			</div>
		</div><br>
		<div class="form-group"><label for="flimitation">限购数量：</label>
		 	<div class="input-group">
					<input type="text" id="flimitation" name="flimitation" class="validate[custom[integer],min[-1],max[18]] form-control" value="${flimitation}" size="4">
				<div class="input-group-addon">-1表示该商品不限购</div>
			</div>
		</div>
		<div class="form-group has-error"><label for="fonSaleTime">自动上架时间：</label>
	        <div id="fonSaleTimeDiv" class="input-group date form_datetime">
		    	<input id="fonSaleTime" name="fonSaleTime" type="text" value="${fonSaleTime }" class="form-control" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
		    </div>
	    </div>
	 <div class="form-group" style="min-height: 110px;"><label for="fbrief">商品简介：</label>
	        <textarea id="fbrief" name="fbrief" cols="100%" rows="5" class="validate[required,maxSize[250]] form-control">${fbrief}</textarea>
	 </div>
	
		<!-- Ⅱ.活动属性信息： -->
<!-- 	    <blockquote><h3>Ⅱ.商品规格及包装信息：</h3></blockquote> -->
		<!--
		<div class="form-group"><label for="fpromotionModel">促销模式：</label>
		      <select id="fpromotionModel" name="fpromotionModel" class="form-control" style="width: 120px;">
			     <option value=""><fmt:message key="fxl.common.select" /></option>
			         <c:forEach var="Item" items="${promotionModelMap}"> 
			     <option value="${Item.key}">${Item.value}</option>
			   </c:forEach>
		</select>
		</div>
		 
		<div class="form-group"><label for="fgoodsTag">商品标签：</label>
		      <select id="fgoodsTag" name="fgoodsTag" class="form-control" style="width: 120px;">
			      <option value=""><fmt:message key="fxl.common.select" /></option>
			      <c:forEach var="Item" items="${goodsModelMap}"> 
			      <option value="${Item.key}">${Item.value}</option>
			      </c:forEach>
		</select>
		</div>
		 -->
			
		<!-- Ⅲ.运营信息： -->
		<blockquote><h3>Ⅱ.运营信息：</h3></blockquote>
	   	<div class="form-group has-error"><label for="fbdId">商家BD：</label>
	        <select id="fbdId" name="fbdId" class="validate[required] form-control">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="bd" items="${bdList}">
					<option value="${bd.key}">${bd.value}</option>
				</c:forEach>
			</select>
	    </div>
	    <div class="form-group has-error"><label for="fcreaterId">编辑人员：</label>
	        <select id="fcreaterId" name="fcreaterId" class="validate[required] form-control">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="editor" items="${editorList}">
					<option value="${editor.key}">${editor.value}</option>
				</c:forEach>
			</select>
	    </div>
	    <div class="form-group"><label for="fprompt" style="color: red;">注意事项：</label>
	         <input type="text" id="fprompt" name="fprompt" class="validate[maxSize[250]] form-control" value="${fprompt}" size="50">
	    </div>
	    <!--<div class="form-group has-error"><label for="fdistribution">是否分销：</label>
	        <select id="fdistribution" name="fdistribution" class="validate[required] form-control" style="width: 120px;">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="yesNoItem" items="${yesNoMap}"> 
				<option value="${yesNoItem.key}">${yesNoItem.value}</option>
				</c:forEach>
			</select>
	    </div>
		<div class="form-group has-error"><label for="fcommentRewardType" >评价激励：</label>
	        <select id="fcommentRewardType" name="fcommentRewardType" class="validate[required] form-control">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="commentRewardItem" items="${commentRewardTypeMap}"> 
				<option value="${commentRewardItem.key}">${commentRewardItem.value}</option>
				</c:forEach>
			</select>
	    </div>-->
	    <br/>
	    <!-- <div class="form-group"><label for="fcommentRewardAmount">返现金额：</label>
			<input type="text" id="fcommentRewardAmount" name="fcommentRewardAmount" class="validate[custom[number]] form-control" value="${fcommentRewardAmount}" size="15">
	    </div>
	     -->


		<!-- Ⅳ.订单和结算信息： 
		<blockquote><h3>Ⅳ.订单和结算信息：</h3></blockquote>
	   <div class="form-group has-error" ><label for="forderType">订单类型：</label>
	        <select id="forderType" name="forderType" class="validate[required] form-control" style="width: 120px;">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="orderTypeItem" items="${orderTypeMap}"> 
				<option value="${orderTypeItem.key}">${orderTypeItem.value}</option>
				</c:forEach>
			</select>
		</div>
	   <input id="fstockFlag" name="fstockFlag" value="20" type="hidden"/>
	   <div class="form-group has-error"><label for="fsettlementType">结算类型：</label>
	        <select id="fsettlementType" name="fsettlementType" class="validate[required] form-control" style="width: 120px;">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="settlementTypeItem" items="${settlementTypeMap}"> 
				<option value="${settlementTypeItem.key}">${settlementTypeItem.value}</option>
				</c:forEach>
			</select>
		</div>
	    <div class="form-group has-error"><label for="fusePreferential" >可否用优惠：</label>
	        <select id="fusePreferential" name="fusePreferential" class="validate[required] form-control">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="usePreferential" items="${usePreferentialMap}"> 
				<option value="${usePreferential.key}">${usePreferential.value}</option>
				</c:forEach>
			</select>
	    </div>
	    <br/>
		<div class="form-group has-error"><label for="freturn">可否退款：</label>
			<select id="freturn" name="freturn" class="validate[required] form-control" style="width: 120px;">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="yesNoItem" items="${yesNoMap}"> 
				<option value="${yesNoItem.key}">${yesNoItem.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group has-error"><label for="fverificationType">核销类型：</label>
			<select id="fverificationType" name="fverificationType" class="validate[required] form-control">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="vt" items="${verificationTypeMap}"> 
				<option value="${vt.key}">${vt.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group has-error"><label for="fexternalSystemType">外连系统：</label>
			<select id="fexternalSystemType" name="fexternalSystemType" class="validate[required] form-control">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="es" items="${externalSystemMap}"> 
				<option value="${es.key}">${es.value}</option>
				</c:forEach>
			</select>
		</div>
	    -->
<!-- 扩展属性 -->
		<!-- <button type="button" class="btn btn-success" id="btn_center" style="margin-left:50px;" ><span class="glyphicon glyphicon-plus"></span>添加扩展属性</button>
		<table id="eventTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table> -->
		<p class="text-center">
			<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-arrow-right"></span> 保存并下一步</button>
		</p>
		<div style="height: 20px;"></div>
	</form>
<!-- 扩展属性值 
  <div class="window" id="gos">  
     <form id="createEventInfoForm" action="${ctx}/fxl/event/addEventTexInfo" method="post" class="form-inline" role="form"> --> 
        <!-- <div id="title" class="win_title"><div class="close"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></div></div>  
        <div class="content">
            <div class="form-group"><label for="fname">属性名称：</label>
               <input type="text" id="fname" name="fname" class="form-control validate[required,minSize[2],maxSize[250]]" value="${eventExtInfoListMap.fname}" size="25">
            </div>  
        </div>
        <div class="content">
            <div class="form-group"><label for="fprompt">文案提示：</label>
               <input type="text" id="fprompt" name="fprompt" class="form-control validate[minSize[2],maxSize[250]]" value="" size="25">
            </div>  
        </div>  
        
        <div class="content">
            <div class="form-group"><label for="fisRequired">是否必填项：</label>
               <select id="fisRequired" name="fisRequired" class="validate[required] form-control" style="width: 120px;">
					<option value=""><fmt:message key="fxl.common.select" /></option>
					<c:forEach var="yesNoItem" items="${yesNoMap}"> 
					<option value="${yesNoItem.key}">${yesNoItem.value}</option>
					</c:forEach>
		    </select>
            </div>  
        </div>  
        
        <div class="content">
            <div class="form-group has-error"><label for="fisEveryone">是否根据人数累加：</label>
                  <select id="fisEveryone" name="fisEveryone" class="validate[required] form-control" style="width: 120px;">
					<option value=""><fmt:message key="fxl.common.select" /></option>
				    <c:forEach var="yesNoItem" items="${yesNoMap}"> 
				    <option value="${yesNoItem.key}">${yesNoItem.value}</option>
				    </c:forEach>
		          </select>
            </div>     
        </div>  
        <p class="text-center">
	    <button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-arrow-right"></span> 保存</button>
	    </p>
        </form>
    </div> --> 
<!-- 扩展熟悉值结束 -->
</div>

<script type="text/javascript">
	//获取窗口的高度  
	var windowHeight;
	//获取窗口的宽度  
	var windowWidth;
	//获取弹窗的宽度  
	var popWidth;
	//获取弹窗高度  
	var popHeight;
	function init() {
		windowHeight = $(window).height();
		windowWidth = $(window).width();
		popHeight = $(".window").height();
		popWidth = $(".window").width();
	}

	//关闭窗口的方法  
	function closeWindow() {
		$(".win_title div").click(function() {
			$(this).parent().parent().hide("normal");
		});
	}

	function popCenterWindow() {
	
		init();
		//计算弹出窗口的左上角Y的偏移量  
		var popY = (windowHeight - popHeight) / 2; //垂直方向偏移量  
		var popX = (windowWidth - popWidth) / 2; //水平方向偏移量  
		
		//设定窗口的位置  
		$("#gos").css("top", popY-270).css("left", popX+330).slideToggle("normal");
		closeWindow();
	}
</script>