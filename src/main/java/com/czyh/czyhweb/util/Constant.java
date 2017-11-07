package com.czyh.czyhweb.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springside.modules.utils.Exceptions;

import com.czyh.czyhweb.util.dingTalk.DingTalkUtil;

public class Constant {
	
	private static final Logger logger = LoggerFactory.getLogger(Constant.class);

	// 系统属性设置文件
	public static final String SYSTEM_SETTING_FILE = "classpath:/system.properties";

	// 敏感词文件
	public static final String BAD_WORD_FILE = "classpath:wordfilter.txt";

	// 项目文件根路径
	public static String RootPath = null;

	// email设置
	public static final String EmailFrom = "emailFrom";

	// SESSION的被踢出局参数
	public static final String KICKOUT = "kickout";

	// 系统默认的语言
	public static final String defaultLanguage = "zh_CN";

	// 英文语言
	public static final String englishLanguage = "en_US";

	// 水印图片对象
	public static BufferedImage watermarkFile = null;

	// 水印图片对象（小）
	public static BufferedImage watermarkSmFile = null;

	// 短信验证码缓存Key
	public static final String SmsCheckCodeCacheKey = "SmsCheckCode";

	// 上下架场次计数缓存Key
	public static final String OnOffSale = "OnOffSale";

	// 商家核销订单商家用户ID与订单ID绑定缓存Key
	public static final String VerificationOrder = "VerificationOrder";

	// 活动库存缓存Key
	public static final String EventStock = "EventStock";

	// 活动类目缓存Key
	public static final String EventCategory = "EventCategory";
	
	// 七牛用户上传图片缓存的token KEY
	public static final String QnUserUploadToken = "QnUserUploadToken";

	// 活动详情静态化模板
	public static final String EventDetilTemplate = "eventDetailHtml2.ftl";

	// 商家详情静态化模板
	public static final String MerchantDetilTemplate = "merchantDetailHtml.ftl";

	// 专题详情静态化模板
	public static final String SubjectDetilTemplate = "subjectDetailHtml.ftl";
	
	// 配置文件缓存
	public static final String Configuration = "Configuration";

	// 默认头像
	public static String defaultHeadImgUrl = null;

	// 移动端活动详情页URL
	public static String czyhServiceUrl = null;
	
	// 优惠券URL
	public static String couponInterfaceUrl = null;
	
	//微信push接口URL
	public static String weChatPushUrl = null;
	
	//微信调用链接URL
	public static String weChatUrl = null;
	
	// 微信push接口URL
	public static String czyhFscUrl = null;
	
	//微信模板消息first内容
	public static String consultingReplyFirst = null;
	
	//微信模板消息备注内容
	public static String consultingReplyRemark = null;
	
	//微信模板消息first内容
	public static String commentingReplyFirst = null;
	
	//微信模板消息备注内容
	public static String commentingReplyRemark = null;
	
	// 微信订单评价提醒模板消息first内容
	public static String orderEvaluationFirst = null;
	
	// 微信订单评价提醒模板消息备注内容
	public static String orderEvaluationRemark = null;
	
	// 微信订单评价返积分提醒模板消息first内容
	public static String orderEvaluationBonusFirst = null;
	
	// 微信订单评价反积分提醒模板消息备注内容
	public static String orderEvaluationBonusRemark = null;
	
	// 微信订单评价返现提醒模板消息first内容
	public static String orderCashBackFirst = null;
	
	// 微信订单评价返现提醒模板消息备注内容
	public static String orderCashBackRemark = null;
	
	// 积分商城兑换订单提醒模板消息first内容
	public static String bonusOrderFirst = null;
	
	// 积分商城兑换订单提醒模板消息备注内容
	public static String bonusOrderRemark = null;
	
	// 微信积分到账提醒模板消息first内容
	public static String bonusAccountFirst = null;
	
	// 微信积分到账提醒模板消息备注内容
	public static String bonusAccountRemark = null;
	
	// 客服回复提醒模板消息first内容
	public static String orderReplayFirst = null;
	
	// 客服回复提醒模板消息备注内容
	public static String orderReplayRemark = null;
	
	//前端个人用户订单状态页面
	public static String orderStatusUrl = null;
	
	//前端咨询页面地址
	public static String consultUrl = null;
	
	//前端咨询页面地址
	public static String bonusUrl = null;
	
	// 订单详情URL
	public static String orderDetail = null;

	// APP栏目MAP
	public static Map<String, String> appChannelMap = null;

	public static void init() {
		if (SystemUtils.IS_OS_WINDOWS) {
			RootPath = PropertiesUtil.getProperty("windowsRootPath");
		} else if (SystemUtils.IS_OS_MAC_OSX) {
			RootPath = PropertiesUtil.getProperty("macRootPath");
		} else {
			RootPath = PropertiesUtil.getProperty("linuxRootPath");
		}

		if (PropertiesUtil.getProperty("DingTalkSwitch").toString().equals("1")) {
			DingTalkUtil.setDingTalkSwitch(true);
		} else {
			DingTalkUtil.setDingTalkSwitch(false);
		}
		
		consultUrl = PropertiesUtil.getProperty("consultUrl");
		
		orderEvaluationFirst = PropertiesUtil.getProperty("orderEvaluationFirst");

		orderEvaluationRemark = PropertiesUtil.getProperty("orderEvaluationRemark");
		
		orderStatusUrl = PropertiesUtil.getProperty("orderStatusUrl");
		
		orderDetail = PropertiesUtil.getProperty("orderDetail");
		
		bonusUrl = PropertiesUtil.getProperty("bonusUrl");

		czyhServiceUrl = PropertiesUtil.getProperty("czyhServiceUrl");
		
		couponInterfaceUrl = PropertiesUtil.getProperty("couponInterfaceUrl");
		
		weChatPushUrl = PropertiesUtil.getProperty("weChatPushUrl");
		
		weChatUrl = PropertiesUtil.getProperty("weChatUrl");
		
		czyhFscUrl = PropertiesUtil.getProperty("czyhFscUrl");
		
		consultingReplyRemark = PropertiesUtil.getProperty("consultingReplyRemark");
		
		consultingReplyFirst = PropertiesUtil.getProperty("consultingReplyFirst");
		
		commentingReplyFirst = PropertiesUtil.getProperty("commentingReplyFirst");
		
		commentingReplyRemark = PropertiesUtil.getProperty("commentingReplyRemark");
		
		bonusOrderFirst = PropertiesUtil.getProperty("bonusOrderFirst");
		
		bonusOrderRemark = PropertiesUtil.getProperty("bonusOrderRemark");
		
		bonusAccountFirst = PropertiesUtil.getProperty("bonusAccountFirst");
		
		bonusAccountRemark = PropertiesUtil.getProperty("bonusAccountRemark");
		
		orderReplayFirst = PropertiesUtil.getProperty("orderReplayFirst");
		
		orderReplayRemark = PropertiesUtil.getProperty("orderReplayRemark");
		
		orderEvaluationBonusFirst = PropertiesUtil.getProperty("orderEvaluationBonusFirst");
		
		orderEvaluationBonusRemark = PropertiesUtil.getProperty("orderEvaluationBonusRemark");
		
		orderCashBackFirst = PropertiesUtil.getProperty("orderCashBackFirst");
		
		orderCashBackRemark = PropertiesUtil.getProperty("orderCashBackRemark");
		
		defaultHeadImgUrl = new StringBuilder(PropertiesUtil.getProperty("fileServerUrl"))
				.append(PropertiesUtil.getProperty("imageRootPath")).append("/czyhweb/customerHeadimg.jpg").toString();
	}

	public static void loadWatermarkFile(String systemPath) {
		try {
			Constant.watermarkFile = ImageIO.read(
					new File(new StringBuilder(systemPath).append("/styles/fxl/images/watermark.png").toString()));
			Constant.watermarkSmFile = ImageIO.read(
					new File(new StringBuilder(systemPath).append("/styles/fxl/images/watermark-sm.png").toString()));
		} catch (IOException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
	}

	public static String getH5EventUrl() {
		return new StringBuilder().append(czyhServiceUrl).append("/api/system/share/event/").toString();
	}

}