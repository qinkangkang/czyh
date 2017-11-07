package com.czyh.czyhweb.util.sms;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;

import com.czyh.czyhweb.util.PropertiesUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Maps;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.AlibabaAliqinFcSmsNumSendRequest;
import com.taobao.api.response.AlibabaAliqinFcSmsNumSendResponse;

public class SmsUtil {
	
	private static Logger logger = LoggerFactory.getLogger(SmsUtil.class);

	public static final int CheckCodeSms = 1;

	public static final int PaySuccessSms = 2;

	public static final int VerificationSuccessSms = 3;

	public static final int TimeOutNoPaySms = 4;

	public static final int RefundSuccessSms = 5;

	public static final int EventNoticeSms = 6;

	public static final String CheckCodeSmsId = "SMS_6095610";

	public static final String PaySuccessSmsId = "SMS_6080657";

	public static final String VerificationSuccessSmsId = "SMS_6135592";

	public static final String TimeOutNoPaySmsId = "SMS_6370369";

	public static final String RefundSuccessSmsId = "SMS_6140559";

	public static final String EventNoticeSmsId = "SMS_8155605";

	public static boolean CheckCodeSwitch = false;

	public static boolean PaySuccessSwitch = false;

	public static boolean VerificationSuccessSwitch = false;

	public static boolean TimeOutNoPaySwitch = false;

	public static boolean RefundSuccessSwitch = false;

	private static JsonMapper mapper = new JsonMapper(Include.ALWAYS);

	private static final String SmsServerUrl = "http://gw.api.taobao.com/router/rest";

	private static String Appkey = null;

	private static String Appsecret = null;

	private static String ChenkCodeSignName = null;

	private static TaobaoClient client = null;

	public static void init() {
		Appkey = PropertiesUtil.getProperty("Appkey");
		Appsecret = PropertiesUtil.getProperty("Appsecret");
		ChenkCodeSignName = PropertiesUtil.getProperty("ChenkCodeSignName");

		client = new DefaultTaobaoClient(SmsServerUrl, Appkey, Appsecret);
	}

	/**
	 * 发送短信验证码方法
	 */
	/**
	 * @param smsType
	 * @param phone
	 * @param smsParamMap
	 * @return
	 */
	public static SmsResult sendSms(int smsType, String phone, Map<String, String> smsParamMap) {
		SmsResult smsResult = new SmsResult();

		AlibabaAliqinFcSmsNumSendRequest request = new AlibabaAliqinFcSmsNumSendRequest();
		// 公共回传参数，在“消息返回”中会透传回该参数；举例：用户可以传入自己下级的会员ID，在消息返回时，该会员ID会包含在内，
		// 用户可以根据该会员ID识别是哪位会员使用了你的应用
		request.setExtend("czyhweb");
		// 短信类型，传入值请填写normal
		request.setSmsType("normal");
		// 短信模板变量，传参规则{"key":"value"}，key的名字须和申请模板中的变量名一致，多个变量之间以逗号隔开。
		// 示例：针对模板“验证码${code}，您正在进行${product}身份验证，打死不要告诉别人哦！”，传参时需传入{"code":"1234","product":"alidayu"}
		String paramJson = mapper.toJson(smsParamMap);
		smsResult.setContent(paramJson);
		request.setSmsParamString(paramJson);
		// req.setSmsParamString("{\"code\":\"1234\",\"product\":\"【零到壹】\",\"item\":\"阿里大鱼\"}");
		// 短信接收号码。支持单个或多个手机号码，传入号码为11位手机号码，不能加0或+86。
		// 群发短信需传入多个号码，以英文逗号分隔，一次调用最多传入200个号码。示例：18600000000,13911111111,13322222222
		request.setRecNum(phone);

		// smsType为1表示是验证码短信
		if (smsType == CheckCodeSms) {
			// 短信签名，传入的短信签名必须是在阿里大鱼“管理中心-短信签名管理”中的可用签名。如“阿里大鱼”已在短信签名管理中通过审核，
			// 则可传入”阿里大鱼“（传参时去掉引号）作为短信签名。短信效果示例：【阿里大鱼】欢迎使用阿里大鱼服务。
			request.setSmsFreeSignName(ChenkCodeSignName);
			// 短信模板ID，传入的模板必须是在阿里大鱼“管理中心-短信模板管理”中的可用模板。示例：SMS_585014
			request.setSmsTemplateCode(CheckCodeSmsId);
		} else if (smsType == PaySuccessSms) {
			// smsType为2表示是订单支付成功通知
			request.setSmsFreeSignName(ChenkCodeSignName);
			request.setSmsTemplateCode(PaySuccessSmsId);
		} else if (smsType == VerificationSuccessSms) {
			// smsType为3表示是订单核销成功通知
			request.setSmsFreeSignName(ChenkCodeSignName);
			request.setSmsTemplateCode(VerificationSuccessSmsId);
		} else if (smsType == TimeOutNoPaySms) {
			// smsType为4表示是超时未支付取消订单通知
			request.setSmsFreeSignName(ChenkCodeSignName);
			request.setSmsTemplateCode(TimeOutNoPaySmsId);
		} else if (smsType == RefundSuccessSms) {
			// smsType为5表示是退款成功通知
			request.setSmsFreeSignName(ChenkCodeSignName);
			request.setSmsTemplateCode(RefundSuccessSmsId);
		} else if (smsType == EventNoticeSms) {
			// smsType为5表示是退款成功通知
			request.setSmsFreeSignName(ChenkCodeSignName);
			request.setSmsTemplateCode(EventNoticeSmsId);
		}

		AlibabaAliqinFcSmsNumSendResponse response = null;
		try {
			response = client.execute(request);
			if (response.getResult() != null) {
				smsResult.setSuccess(response.getResult().getSuccess());
			}
			smsResult.setResponse(response.getBody());
		} catch (ApiException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
		return smsResult;
	}

	public static void main(String[] args) {
		SmsUtil.init();
		Map<String, String> smsParamMap = Maps.newHashMap();
		// smsParamMap.put("chackCode", "888888");
		// smsParamMap.put("minute", "3");
		// SmsUtil.sendSms(1, "15300197095", smsParamMap);
		smsParamMap.put("eventContent", "[独家定制]五一假期带孩子跟着“黑豹”露营山野，夜巡野猪沟，探秘野生动植物！");
		smsParamMap.put("dwz", "RqCbsJc");
		smsParamMap.put("fxl", "零到壹亲子平台");
		SmsUtil.sendSms(EventNoticeSms, "13801089087", smsParamMap);
	}

	public static boolean isCheckCodeSwitch() {
		return CheckCodeSwitch;
	}

	public static void setCheckCodeSwitch(boolean checkCodeSwitch) {
		CheckCodeSwitch = checkCodeSwitch;
	}

	public static boolean isPaySuccessSwitch() {
		return PaySuccessSwitch;
	}

	public static void setPaySuccessSwitch(boolean paySuccessSwitch) {
		PaySuccessSwitch = paySuccessSwitch;
	}

	public static boolean isVerificationSuccessSwitch() {
		return VerificationSuccessSwitch;
	}

	public static void setVerificationSuccessSwitch(boolean verificationSuccessSwitch) {
		VerificationSuccessSwitch = verificationSuccessSwitch;
	}

	public static boolean isTimeOutNoPaySwitch() {
		return TimeOutNoPaySwitch;
	}

	public static void setTimeOutNoPaySwitch(boolean timeOutNoPaySwitch) {
		TimeOutNoPaySwitch = timeOutNoPaySwitch;
	}

	public static boolean isRefundSuccessSwitch() {
		return RefundSuccessSwitch;
	}

	public static void setRefundSuccessSwitch(boolean refundSuccessSwitch) {
		RefundSuccessSwitch = refundSuccessSwitch;
	}
}