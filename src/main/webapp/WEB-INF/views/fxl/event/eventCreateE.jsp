<%@ page contentType="text/html;charset=UTF-8" %>
<meta name="decorator" content="/WEB-INF/decorators/decorator_no_theme.jsp">
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/select2/css/select2.min.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="${ctx}/styles/select2/js/select2.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/i18n/zh-CN.js"></script>
<script type="text/javascript"  src="${ctx}/styles/ajaxfileupload/ajaxfileupload.js"></script>
<script src="http://www.jq22.com/jquery/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript"  src="${ctx}/styles/ajaxfileupload/jquery.form.js"></script>
<script type="text/javascript">
$(document).ready(function() {
	
	//参数名称验证
/* 	$.validationEngineLanguage.allRules.checkvalueName={
			"url" : '${ctx}/fxl/event/checkValueName',
			"extraDataDynamic": ['#fgoodsId','#id'],
			"alertTextOk" : '您可以使用该参数名称！',
		    "alertText" : '您输入的参数名称已经存在，请更换其他参数名称！',
		    "alertTextLoad" : '正在验证该参数名称是否被占用……'
		}; */
	
	//下载参数导入模板;无法使用ajax,ajax无法弹出下载保存框
	$("#importTemplateBtn").click(function(e) {
		 var url = "${ctx}/fxl/event/downLoadTemplate";  
		  	url = encodeURI(url);
		   	location.href = url;
		   	
    });
	
	var uploadModal =  $('#uploadModal');
	
	$('#importBtn').on('click',function(e) {
		$('#fgoodsId').val($('#feventId').val());
		$('#resetBtn').show();
		uploadModal.modal('show');
	});
	
	/* $('#submitbtn').click(function(){
		$("#uploaderForm").ajaxSubmit(function(data){
				if(data.success){
					alert('success');
					resetForm: true ;
					createSkuModal.modal('hide');
					toastr.success(data.msg);
					$('#eventTab a:eq(5)').tab('show');
				}else{
					toastr.error(data.msg);
				}
			});
		return false;
			 
		}); */ 
	
	
		
	
  var uploaderForm =  $('#uploaderForm');
 	uploaderForm.on("submit", function(event){
 		if (!event.isDefaultPrevented()) {
			event.preventDefault();
 		}
 		var path=$("#uploadFile").val();
 		if(path==null || path==""){
 			toastr.error("上传文件为空，请重新确认");
 		}else if(path!=null){
 			var suffix = path.substring(path.lastIndexOf(".") + 1);
 			if(suffix=="xls" || suffix=="xlxs"){
 				ajaxFileUpload();
 			}else{
 				toastr.error("文件上传格式不正确，请上传excel格式的模板文件");
 			}
 			
 		}
 		});
 	
 		function ajaxFileUpload(){
  			$.ajaxFileUpload({
  				url:"${ctx}/fxl/event/import?fgoodsId="+$("#eventId").val(),
 				secureuri: false, //是否需要安全协议，一般设置为false
  	            fileElementId: 'uploadFile', //文件上传域的ID
  	            dataType: 'text', //返回值类型 一般设置为json
 	            success:function(data){
 	            		var start = data.indexOf(">");  
 	               		if(start != -1) {  
 	                	var end = data.indexOf("<", start + 1);  
 	                  	if(end != -1) {  
 	                    	data = data.substring(start + 1, end); 
 	                    	var succ=data.indexOf("true");
 	                  		var msgIndex=data.indexOf("msg");
 	                   		var splitIndex=data.indexOf(",");
 	                 		var message = data.substring(msgIndex + 6, splitIndex-1); 
 	                 		if(succ!=-1){
 	                 			
 	                 			uploadModal.modal('hide');
 	                 			toastr.success(message);
 	                 			window.location.reload();
 	                 			$('#eventTab a:eq(5)').tab('show');
 	                 		}else{
 	                 			toastr.error(message);
 	                 			uploadModal.modal('hide');
 	                 		}
 	                 		
 	                  }  
 	               }  
  	            }
  			});
  		}
 		
 		/* function tab() {
 		    var selectTab = $('#detailtypeInfo').tabs('getSelected');
 		    var url =$(selectTab.panel('options').content).attr("");
 		    $('#detailtypeInfo').tabs('update', {
 		        tab : selectTab,
 		        options : {
 		       	 	href : url
 		    	}
 			});
 		} */
	
	$("#addSpecDetail").click(function(e) {
		var model = $('.model').html();
        $('#big_div').append(model);
	});
	
	$("#topBtnA").click(function(e) {
		$('html, body').animate({scrollTop:0}, 'slow');
    });
	
	$("#topBtnB").click(function(e) {
		$('html, body').animate({scrollTop:0}, 'slow');
    });
	
	var createEventEForm = $('#createEventEForm');
	
	createEventEForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});

	$("#ftype option[value='${ftype}']").prop("selected",true);
	
	createEventEForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			$.post(form.attr("action"), $.param($.merge(form.serializeArray(),[{name:"eventId", value:$("#eventId").val()}]),true), function(data){
				console.log(form.serializeArray());
				if(data.success){
					dialog({
						fixed: true,
				        title: '操作成功',
				        content: data.msg,
				        okValue: '去维护库存规格',
				        ok: function () {
				        	window.location.href='${ctx}/fxl/event/toSs/' + $("#eventId").val();
				        },
				        cancelValue: '返回活动浏览',
				        cancel: function () {
				        	window.location.href='${ctx}/fxl/event/view';
				        }
				    }).showModal();
				}else{
					toastr.error(data.msg);
				}
				form.removeData("running");
			}, "json");
		}
	});
	
});

function deletediv(obj){
	$(obj).parent().remove();
}

</script>
<div class="row">

<div class="model" style="display:none">  
  <div>   
      <div class="alert alert-warning text-center" role="alert" style="padding: 3px; margin-top: 20px;"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><strong>添加参数</strong></div>
           <div class="form-group has-error" style="width: 150px; float: left;" ><label for="fspaceName">参数名称：</label>
			    <input type="text" id="fspaceName" name="fspaceName" class="form-control validate[required,minSize[2],maxSize[50],ajax[checkvalueName]]" value="${fspaceName}" size="20">
		  </div>
		  <div class="form-group has-error" style="width: 150px; float: left; margin-left: 10px;"><label for="fvalueName">参数值：</label>
		       <input type="text" id="fvalueName" name="fvalueName" class="form-control validate[minSize[2],maxSize[50]]" value="${fvalueName}" size="20">
		  </div>
		  
		  <button class="btn btn-danger" style="margin-top: 25px; margin-left: 10px;"  type="button" onclick="deletediv(this)"><span class="glyphicon glyphicon-remove"></span> 移除该参数</button>
  </div> 
</div>
<!-- 进入循环判断 -->
 <form id="createEventEForm" action="${ctx}/fxl/event/addEventE" method="post" role="form">
 <c:choose>
  <c:when test="${tSpecList.size()==0}">
  
       <div class="col-md-7 col-md-offset-1" role="main" id="big_div" >		
          <div>
          
          	<input type="hidden" id="fgoodsId" name="fgoodsId" value="${eventId}"/>
            <div class="alert alert-warning text-center" role="alert" style="padding: 3px; margin-bottom: 20px;"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><strong>添加参数</strong></div>
		        <div class="form-group has-error" style="width: 150px; float: left;"><label for="fspaceName">参数名称：</label>
				       <input type="text" id="fspaceName" name="fspaceName" class="form-control validate[required,minSize[2],maxSize[50],ajax[checkvalueName]]" value="${fspaceName}" size="20">
				 </div>
				 <div class="form-group has-error" style="width: 150px; float: left; margin-left: 10px;"><label for="fvalueName">参数值：</label>
				       <input type="text" id="fvalueName" name="fvalueName" class="form-control validate[minSize[2],maxSize[50]]" value="${fvalueName}" size="20">
				 </div>
        </div>
	 </div>
 <div class="col-md-4">
	 <div class="affix">
		<!--<input  class="form-control" type="text" value="#FXL#" readonly="readonly"/><br/><br/>-->
		<button id="topBtnB" class="btn btn-info" type="button"><span class="glyphicon glyphicon-arrow-up"></span> 顶部</button><br/><br/><br/>
		<button class="btn btn-success" type="button" id="addSpecDetail"><span class="glyphicon glyphicon-plus"></span> 添加商品参数</button><br/><br/><br/>
		<button class="btn btn-primary" type="submit" ><span class="glyphicon glyphicon-floppy-saved"></span> 保存并完成库存编辑</button>
     </div>
 </div>
</c:when>
          
<c:otherwise>
  <div class="col-md-7 col-md-offset-1" role="main" id="big_div">
      <c:forEach items="${tSpecList}" var="tGoodsSpaceValue">
		  <div>
				<input id="id" name="id" type="hidden" value="${tGoodsSpaceValue.id}">
				<input id="fgoodsId" name="fgoodsId" type="hidden" value="${tGoodsSpaceValue.fgoodsId}">
				<div class="alert alert-warning text-center" role="alert" style="padding: 3px; margin-bottom: 20px;"><button type="button" class="close" data-dismiss="alert" ><span aria-hidden="true">&times;</span></button><strong>添加参数</strong></div>
		        <div class="form-group has-error" style="width: 150px; float: left;"><label for="fspaceName">参数名称：</label>
				       <input type="text" id="fspaceName" name="fspaceName" class="form-control validate[required,minSize[2],maxSize[50]]" value="${tGoodsSpaceValue.fspaceName}" size="20">
				 </div>
				 <div class="form-group has-error" style="width: 150px;float: left; margin-left: 10px;"><label for="fvalueName">参数值：</label>
				       <input type="text" id="fvalueName" name="fvalueName" class="form-control validate[minSize[2],maxSize[50]]" value="${tGoodsSpaceValue.fvalueName}" size="20">
				 </div>

				<button class="btn btn-danger" style="margin-top: 25px; margin-left: 10px;" type="button" onclick="deletediv(this)"> <span class="glyphicon glyphicon-remove"></span> 移除该参数</button>
			  
			   <div style="margin-top: 30px;"></div>
		  </div>
	  </c:forEach>
  </div>
	  <div class="col-md-4">
		 <div class="affix">
		     <button id="topBtnA" class="btn btn-info" type="button"><span class="glyphicon glyphicon-arrow-up"></span> 顶部</button><br/><br/><br/>
		     <button id="importTemplateBtn" class="btn btn-danger" type="button"><span class="glyphicon glyphicon-arrow-down"></span> 下载参数导入模板</button><br/><br/><br/>
		    <button id="importBtn" class="btn btn-info" type="button"><span class="glyphicon glyphicon-arrow-down"> </span>导入参数</button><br/><br/><br/>
		     <!-- <input  class="form-control" type="text" value="#FXL#" readonly="readonly"/><br/><br/> -->
		     <button class="btn btn-success" type="button" id="addSpecDetail"><span class="glyphicon glyphicon-plus"></span> 添加参数</button><br/><br/><br/>
		     <button class="btn btn-primary" type="submit" ><span class="glyphicon glyphicon-floppy-saved"></span> 保存并完成库存编辑</button>
         </div>
	  </div>
	  </c:otherwise>
	</c:choose> 
 </form>
 </div>

<!-- 上传 -->
<div class="modal fade" id="uploadModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">上传参数excel表</h4>
      </div>
      <form id="uploaderForm" action="${ctx}/fxl/event/import" method="post"  class="form-inline" role="form" enctype="multipart/form-data" >
	      <div class="modal-body">
	      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
	      		<input id="fgoodsId" name="fgoodsId"  value=${eventId}  type="hidden" />
	      		<input id="uploadFile" type="file" name="uploadFile" />

	      	</div>

	     <div class="modal-footer">
	      	<button class="btn btn-primary"  id="import" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span>上传</button>
			<button class="btn btn-warning" type="reset" id="resetBtn"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.reset" /></button>
	        <button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="fxl.button.close" /></button>
	     </div>
	  </form>
	</div>
 </div>    		
</div>	