<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<%-- <link href="${ctx}/styles/countDown/style/main.css" rel="stylesheet">
<script src="${ctx}/styles/countDown/js/jquery.lwtCountdown-1.0.js"></script>
<script src="${ctx}/styles/countDown/js/misc.js"></script> --%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js"></script>

<link href="${ctx}/styles/swiper/css/swiper.min.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/swiper/js/swiper.min.js"></script>

<script type="text/javascript">
$(document).ready(function(){
	$.cookie('fxlUsername', '<shiro:principal type="com.czyh.czyhweb.security.ShiroDbRealm$ShiroUser" property="loginName"></shiro:principal>',{expires:30});
	
});
</script>

<script type="text/javascript">

	function browserRedirect() {
		var sUserAgent = navigator.userAgent.toLowerCase();
		var bIsIpad = sUserAgent.match(/ipad/i) == "ipad";
		var bIsIphoneOs = sUserAgent.match(/iphone os/i) == "iphone os";
		var bIsMidp = sUserAgent.match(/midp/i) == "midp";
		var bIsUc7 = sUserAgent.match(/rv:1.2.3.4/i) == "rv:1.2.3.4";
		var bIsUc = sUserAgent.match(/ucweb/i) == "ucweb";
		var bIsAndroid = sUserAgent.match(/android/i) == "android";
		var bIsCE = sUserAgent.match(/windows ce/i) == "windows ce";
		var bIsWM = sUserAgent.match(/windows mobile/i) == "windows mobile";
		//document.writeln("您的浏览设备为：");
		if (bIsIpad || bIsIphoneOs || bIsMidp || bIsUc7 || bIsUc || bIsAndroid
				|| bIsCE || bIsWM) {
			//document.writeln("phone");
			window.location.href = "${ctx}/fxl/Mobile/indexPhone";
		}/* else {
		document.writeln("pc");
			window.location.href = "${ctx}/fxl/Mobile/indexPhone";
		} */
	}

	browserRedirect();
</script>

<style>
html, body {
	position: relative;
	height: 100%;
}

body {
	background: #eee;
	font-family: Helvetica Neue, Helvetica, Arial, sans-serif;
	font-size: 14px;
	color: #000;
	margin: 0;
	padding: 0;
}

.swiper-container {
	width: 80%;
	height: 100%;
	background: #000;
	left: 150px;
	top: -225px;
}

.swiper-slide {
	font-size: 18px;
	color: #fff;
	-webkit-box-sizing: border-box;
	box-sizing: border-box;
	padding: 40px 60px;
}

.parallax-bg {
	position: absolute;
	left: 0;
	top: 0;
	width: 100%;
	height: 100%;
	-webkit-background-size: 100% 100%;
	background-size: 100% 100%;
	background-position: center;
}

.swiper-slide .title {
	font-size: 41px;
	font-weight: 300;
	
}

.swiper-slide .subtitle {
	font-size: 21px;
	
}

.swiper-slide .text {
	font-size: 21px;
	max-width: 400px;
	line-height: 1.3;
	
}
</style>
</head>
<body>
<div class="jumbotron">
	<!-- <h1>距离3月26日华润五彩城活动还剩下</h1>
	<div id="countdown_dashboard">
		<div class="dash weeks_dash">
			<span class="dash_title">weeks</span>
			<div class="digit">0</div>
			<div class="digit">0</div>
		</div>
		<div class="dash days_dash">
			<span class="dash_title">days</span>
			<div class="digit">0</div>
			<div class="digit">0</div>
		</div>
		<div class="dash hours_dash">
			<span class="dash_title">hours</span>
			<div class="digit">0</div>
			<div class="digit">0</div>
		</div>
		<div class="dash minutes_dash">
			<span class="dash_title">minutes</span>
			<div class="digit">0</div>
			<div class="digit">0</div>
		</div>
		<div class="dash seconds_dash">
			<span class="dash_title">seconds</span>
			<div class="digit">0</div>
			<div class="digit">0</div>
		</div>
	</div> -->
	<p><img alt="" src="${ctx}/styles/fxl/images/logo-md.png" class="img-rounded" ></p>
	<br/>
	

	<div class="swiper-container">
	
        <div class="parallax-bg" style="background-image:url(${ctx}/styles/fxl/images/indexbg.jpg)" data-swiper-parallax="-23%"></div>
        <div class="swiper-wrapper">
         <c:forEach items="${noticeList}" var="noticeListMap">
            <div class="swiper-slide">
                <div class="title" data-swiper-parallax="-100">${noticeListMap.ftitle}</div>
                
               <!--  <c:choose>
                    <c:when test="${noticeListMap.fnoticeType==10}">
                             <div class="subtitle" data-swiper-parallax="-200">公告类型:系统公告</div>
                    </c:when>
                    <c:otherwise>
                             <div class="subtitle" data-swiper-parallax="-200">公告类型:消息公告</div>
                    </c:otherwise>
                 </c:choose> 
               -->           
                <div class="text" data-swiper-parallax="-300">
                    <p></p>${noticeListMap.fnoticeContent}
                </div> 
            </div>
           </c:forEach>     
        </div>
        <!-- Add Pagination -->
        <div class="swiper-pagination swiper-pagination-white"></div>
        <!-- Add Navigation -->
        <div class="swiper-button-prev swiper-button-white"></div>
        <div class="swiper-button-next swiper-button-white"></div>
    </div>
  
</div>


<script>
	var swiper = new Swiper('.swiper-container', {
		nextButton : '.swiper-button-next',
		prevButton : '.swiper-button-prev',
		pagination : '.swiper-pagination',
		paginationClickable : true,
		// Disable preloading of all images
		preloadImages : false,
		// Enable lazy loading
		lazyLoading : true
	});
</script>
</body>
</html>