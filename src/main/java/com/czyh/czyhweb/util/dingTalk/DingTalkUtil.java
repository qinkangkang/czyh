package com.czyh.czyhweb.util.dingTalk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;

import com.czyh.czyhweb.util.HttpClientUtil;
import com.czyh.czyhweb.util.PropertiesUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JavaType;

public class DingTalkUtil {
	
	private static Logger logger = LoggerFactory.getLogger(DingTalkUtil.class);

	private static JsonMapper mapper = new JsonMapper(Include.ALWAYS);

	// 钉钉推送通知总开关
	private static boolean DingTalkSwitch = false;

	// 设置支付成功同步发送钉钉通知
	private static String replyConsoleDingTalk = "461331323924,03086562258707,03086563586340,01010720367671,0541124832780672,04474136585745";

	//设置服务器宕机发送钉钉通知
	private static String goDieDingTalk = "01263719039172,01213733021644,03121031306560";
	
	// 设置用户咨询同步发送钉钉通知
	private static String consultSyncingDingTalk = "461331323924,03086563586340,03086562258707,01010720367671,0541124832780672,04474136585745,03121565096537,0439545227943042";

	public static boolean isDingTalkSwitch() {
		return DingTalkSwitch;
	}

	public static void setDingTalkSwitch(boolean dingTalkSwitch) {
		DingTalkSwitch = dingTalkSwitch;
	}

	public static String getReplyConsoleDingTalk() {
		return replyConsoleDingTalk;
	}

	public static void setReplyConsoleDingTalk(String replyConsoleDingTalk) {
		DingTalkUtil.replyConsoleDingTalk = replyConsoleDingTalk;
	}

	public static String getGoDieDingTalk() {
		return goDieDingTalk;
	}

	public static void setGoDieDingTalk(String goDieDingTalk) {
		DingTalkUtil.goDieDingTalk = goDieDingTalk;
	}

	public static String getConsultSyncingDingTalk() {
		return consultSyncingDingTalk;
	}

	public static void setConsultSyncingDingTalk(String consultSyncingDingTalk) {
		DingTalkUtil.consultSyncingDingTalk = consultSyncingDingTalk;
	}

	/**
	 * 发送钉钉公告消息
	 * 
	 * @param msg
	 *            发送的信息
	 * @param sendUsers
	 *            消息接收者，多个接收者用“,”来分割
	 *            '分隔(比如：userId1,userId2,userId2）特殊情况：指定为@all，则向该企业应用的全部成员发送
	 * @return
	 */
	public static boolean sendDingTalk(String msg, String toUsers) {
		if (!DingTalkSwitch) {
			return true;
		}
		if (StringUtils.isBlank(msg) || StringUtils.isBlank(toUsers)) {
			return false;
		}
		toUsers = toUsers.replaceAll(",", "|");
		boolean b = false;
		String result = "";
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("msg", msg));
			params.add(new BasicNameValuePair("toUsers", toUsers));
			String url = PropertiesUtil.getProperty("DingTalkProxyUrl");
			result = HttpClientUtil.callUrlPost(url, params);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
		if (StringUtils.isBlank(result)) {
			return false;
		} else {
			JavaType jt = mapper.contructMapType(HashMap.class, String.class, Boolean.class);
			Map<String, Boolean> map = mapper.fromJson(result, jt);
			b = map.get("success");
		}
		return b;
	}

}