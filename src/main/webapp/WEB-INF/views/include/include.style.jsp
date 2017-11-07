<%@ page language="java" pageEncoding="UTF-8" %>
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
<meta http-equiv="Cache-Control" content="no-store">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="0">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="shortcut icon" href="${ctx}/styles/fxl/images/favicon.png">
<link href="${ctx}/styles/bootstrap/css/bootstrap.min.css" rel="stylesheet">
<link href="${ctx}/styles/bootstrap/css/bootstrap-theme.min.css" rel="stylesheet">
<link href="${ctx}/styles/pace/css/pink/pace-theme-flash.css" rel="stylesheet">
<link href="${ctx}/styles/fxl/css/style.css" rel="stylesheet" type="text/css">
<!--[if lt IE 9]>
<script src="${ctx}/styles/jquery/html5shiv.min.js"></script>
<script src="${ctx}/styles/jquery/respond.min.js"></script>
<![endif]-->
<script src="${ctx}/styles/jquery/jquery2.min.js"></script>
<script src="${ctx}/styles/jquery/jquery.cookie.js"></script>
<script src="${ctx}/styles/bootstrap/js/bootstrap.min.js"></script>
<script src="${ctx}/styles/pace/js/pace.min.js"></script>
<script type="text/javascript">
var ctx = "${ctx}";
var noPicUrl = ctx + "/styles/fxl/images/nopic.png";
var pickerLocal = "zh-CN";
var datatablePageLength = 10;
var dataTableLanguage = {
	"processing":   "正在努力的加载中...",
	"lengthMenu":   "显示 _MENU_ 条记录",
	"zeroRecords":  "对不起，没有满足条件的记录！",
	"info":         "显示第 _START_ 至 _END_ 条记录，共 _TOTAL_ 条",
	"infoEmpty":    "显示第 0 至 0 条记录，共 0 条",
	"infoFiltered": "(由 _MAX_ 项结果过滤)",
	"infoPostFix":  "",
	"search":       "搜索:",
	"emptyTable":     "对不起，没有满足条件的记录！",
	"loadingRecords": "正在努力的加载中....",
	"infoThousands":  ",",
	"paginate": {
		"first":    "首页",
		"previous": "上页",
		"next":     "下页",
		"last":     "末页"
	},
	"aria": {
		"sortAscending":  ": 以升序排列此列",
		"sortDescending": ": 以降序排列此列"
	}
};
</script>