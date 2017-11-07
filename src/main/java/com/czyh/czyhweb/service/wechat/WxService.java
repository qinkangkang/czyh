package com.czyh.czyhweb.service.wechat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.utils.Exceptions;

import com.czyh.czyhweb.common.dict.ResponseConfigurationDict;
import com.czyh.czyhweb.dao.UserDAO;
import com.czyh.czyhweb.dao.WechatPushDAO;
import com.czyh.czyhweb.dao.WxMenuDAO;
import com.czyh.czyhweb.dao.WxMenuItemDAO;
import com.czyh.czyhweb.entity.TComment;
import com.czyh.czyhweb.entity.TConsult;
import com.czyh.czyhweb.entity.TCustomer;
import com.czyh.czyhweb.entity.TCustomerInfo;
import com.czyh.czyhweb.entity.TEvent;
import com.czyh.czyhweb.entity.TOrder;
import com.czyh.czyhweb.entity.TOrderBonus;
import com.czyh.czyhweb.entity.TWechatPush;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.CommonService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.ConfigurationUtil;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.HttpClientUtil;
import com.czyh.czyhweb.util.wx.message.TemplateMessage;
import com.czyh.czyhweb.util.wx.message.WxInterfaceApi;
import com.google.common.collect.Maps;

/**
 * WX业务管理类.
 * 
 * @author jinshengzhi
 */
@Component
@Transactional
public class WxService {

	private static final Logger logger = LoggerFactory.getLogger(WxService.class);

	@Autowired
	private WxMenuItemDAO wxMenuItemDAO;

	@Autowired
	private WxMenuDAO wxMenuDAO;

	@Autowired
	private CommonService commonService;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private WechatPushDAO wechatPushDAO;

	/**
	 * 发送订单评价提醒模板消息
	 * 
	 * @param tOrder
	 *            订单对象
	 * @param openId
	 *            用户的openId
	 */
	public void pushOrderEvaluationMsg(TOrder tOrder, String openId) {
		try {
			if (StringUtils.isNotBlank(openId)) {
				String orderDetailUrl = new StringBuilder().append(Constant.orderDetail).append(tOrder.getId())
						.toString();
				String postUrl = new StringBuilder().append(Constant.weChatPushUrl).append("/")
						.append(TemplateMessage.TEMPLATEMSG_TYPE_COMPLIMENTARYPOINTS).toString();
				Map<String, Object> paramsMap = Maps.newHashMap();
				paramsMap.put("openid", openId);
				paramsMap.put("url", orderDetailUrl);
				paramsMap.put("first", Constant.orderEvaluationFirst);
				paramsMap.put("remark", Constant.orderEvaluationRemark);
				paramsMap.put("keyword1", tOrder.getForderNum());
				paramsMap.put("keyword2", DateFormatUtils.format(new Date(), "yyyy年MM月dd日 HH:mm"));
				HttpClientUtil.callUrlPost(postUrl, paramsMap);
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			logger.error("发送订单评价提醒模板消息时，订单评价提醒模板消息接口出错。");
		}
	}

	/**
	 * 回复评论提醒模板消息
	 * 
	 * @param tOrder
	 *            订单对象
	 * @param openId
	 *            用户的openId
	 */
	public void pushCommentReplyMsg(TEvent event, TCustomer tCustomer, TComment db) {
		// 同步发送微信通知
		try {
			String PushWeiXin = ConfigurationUtil
					.getPropertiesValue(ResponseConfigurationDict.RESPONSE_PROPERTIES_COMMENTREPLY);
			if (StringUtils.isNotBlank(PushWeiXin) && PushWeiXin.equals("1")) {
				if (StringUtils.isNotBlank(tCustomer.getFweixinId())) {
					String fxlEventUrl = new StringBuilder().append(Constant.consultUrl).append(event.getId())
							.toString();
					String postUrl = new StringBuilder().append(Constant.weChatPushUrl).append("/")
							.append(TemplateMessage.TEMPLATEMSG_TYPE_COMMENTREPLY).toString();
					Map<String, Object> paramsMap = Maps.newHashMap();
					paramsMap.put("openid", tCustomer.getFweixinId());
					paramsMap.put("url", fxlEventUrl);
					paramsMap.put("first", Constant.commentingReplyFirst);
					paramsMap.put("remark", Constant.commentingReplyRemark);
					paramsMap.put("keyword1", db.getFuserName());
					paramsMap.put("keyword2", new Date());
					paramsMap.put("keyword3", db.getFreply());
					HttpClientUtil.callUrlPost(postUrl, paramsMap);
				}
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			logger.error("微信push消息出错。");
		}
	}

	/**
	 * 回复评价提醒模板消息
	 * 
	 * @param tOrder
	 *            订单对象
	 * @param openId
	 *            用户的openId
	 */
	public void pushConsultReplyMsg(TEvent event, TCustomer tCustomer, TConsult db) {
		// 同步发送微信通知
		try {
			String PushWeiXin = ConfigurationUtil
					.getPropertiesValue(ResponseConfigurationDict.RESPONSE_PROPERTIES_CONSULTINGREPLY);
			if (StringUtils.isNotBlank(PushWeiXin) && PushWeiXin.equals("1")) {
				if (StringUtils.isNotBlank(tCustomer.getFweixinId())) {
					String fxlEventUrl = new StringBuilder().append(Constant.consultUrl).append(event.getId())
							.toString();
					String postUrl = new StringBuilder().append(Constant.weChatPushUrl).append("/")
							.append(TemplateMessage.TEMPLATEMSG_TYPE_CONSULTINGREPLY).toString();
					Map<String, Object> paramsMap = Maps.newHashMap();
					paramsMap.put("openid", tCustomer.getFweixinId());
					paramsMap.put("url", fxlEventUrl);
					paramsMap.put("first", Constant.consultingReplyFirst);
					paramsMap.put("remark", Constant.consultingReplyRemark);
					paramsMap.put("keyword1", event.getFtitle());
					paramsMap.put("keyword2", db.getFreply());
					HttpClientUtil.callUrlPost(postUrl, paramsMap);
				}
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			logger.error("微信push消息出错。");
		}
	}

	public void saveUpdateMenu(Map<String, Object> valueMap) {

		try {
			// 第一级菜单修改
			if (valueMap.containsKey("oneMenu") && StringUtils.isNotBlank(valueMap.get("oneMenu").toString())) {
				wxMenuDAO.updateMenu(Long.valueOf(valueMap.get("oneMenuId").toString()).longValue(),
						valueMap.get("oneMenu").toString(), valueMap.get("oneMenuUrl").toString());
			}

			// 第一级子菜单修改
			if (valueMap.containsKey("fiveAMenuItemName")
					&& StringUtils.isNotBlank(valueMap.get("fiveAMenuItemName").toString())) {
				wxMenuItemDAO.updateMenuItem(Long.valueOf(valueMap.get("fiveAMenuItemId").toString()).longValue(),
						valueMap.get("fiveAMenuItemName").toString(), valueMap.get("fiveAMenuItemCount").toString(), "",
						Integer.parseInt(valueMap.get("fiveAMenuItemOrder").toString()));
			}

			if (valueMap.containsKey("fourAMenuItemName")
					&& StringUtils.isNotBlank(valueMap.get("fourAMenuItemName").toString())) {
				wxMenuItemDAO.updateMenuItem(Long.valueOf(valueMap.get("fourAMenuItemId").toString()).longValue(),
						valueMap.get("fourAMenuItemName").toString(), valueMap.get("fourAMenuItemCount").toString(), "",
						Integer.parseInt(valueMap.get("fourAMenuItemOrder").toString()));
			}

			if (valueMap.containsKey("threeAMenuItemName")
					&& StringUtils.isNotBlank(valueMap.get("threeAMenuItemName").toString())) {
				wxMenuItemDAO.updateMenuItem(Long.valueOf(valueMap.get("threeAMenuItemId").toString()).longValue(),
						valueMap.get("threeAMenuItemName").toString(), valueMap.get("threeAMenuItemCount").toString(),
						"", Integer.parseInt(valueMap.get("threeAMenuItemOrder").toString()));
			}

			if (valueMap.containsKey("towAMenuItemName")
					&& StringUtils.isNotBlank(valueMap.get("towAMenuItemName").toString())) {
				wxMenuItemDAO.updateMenuItem(Long.valueOf(valueMap.get("towAMenuItemId").toString()).longValue(),
						valueMap.get("towAMenuItemName").toString(), "", valueMap.get("towAMenuItemCK").toString(),
						Integer.parseInt(valueMap.get("towAMenuItemOrder").toString()));
			}

			if (valueMap.containsKey("oneAMenuItemName")
					&& StringUtils.isNotBlank(valueMap.get("oneAMenuItemName").toString())) {
				wxMenuItemDAO.updateMenuItem(Long.valueOf(valueMap.get("oneAMenuItemId").toString()).longValue(),
						valueMap.get("oneAMenuItemName").toString(), "", valueMap.get("oneAMenuItemCK").toString(),
						Integer.parseInt(valueMap.get("oneAMenuItemOrder").toString()));
			}

			// 第二级菜单修改
			if (valueMap.containsKey("towMenu") && StringUtils.isNotBlank(valueMap.get("towMenu").toString())) {
				wxMenuDAO.updateMenu(Long.valueOf(valueMap.get("towMenuId").toString()).longValue(),
						valueMap.get("towMenu").toString(), valueMap.get("towMenuUrl").toString());
			}
			// 第二级子菜单修改
			if (valueMap.containsKey("fiveBMenuItemName")
					&& StringUtils.isNotBlank(valueMap.get("fiveBMenuItemName").toString())) {
				wxMenuItemDAO.updateMenuItem(Long.valueOf(valueMap.get("fiveBMenuItemId").toString()).longValue(),
						valueMap.get("fiveBMenuItemName").toString(), valueMap.get("fiveBMenuItemCount").toString(), "",
						Integer.parseInt(valueMap.get("fiveBMenuItemOrder").toString()));
			}

			if (valueMap.containsKey("fourBMenuItemName")
					&& StringUtils.isNotBlank(valueMap.get("fourBMenuItemName").toString())) {
				wxMenuItemDAO.updateMenuItem(Long.valueOf(valueMap.get("fourBMenuItemId").toString()).longValue(),
						valueMap.get("fourBMenuItemName").toString(), valueMap.get("fourBMenuItemCount").toString(), "",
						Integer.parseInt(valueMap.get("fourBMenuItemOrder").toString()));
			}

			if (valueMap.containsKey("threeBMenuItemName")
					&& StringUtils.isNotBlank(valueMap.get("threeBMenuItemName").toString())) {
				wxMenuItemDAO.updateMenuItem(Long.valueOf(valueMap.get("threeBMenuItemId").toString()).longValue(),
						valueMap.get("threeBMenuItemName").toString(), valueMap.get("threeBMenuItemCount").toString(),
						"", Integer.parseInt(valueMap.get("threeBMenuItemOrder").toString()));
			}

			if (valueMap.containsKey("threeBMenuItemName")
					&& StringUtils.isNotBlank(valueMap.get("threeBMenuItemName").toString())) {
				wxMenuItemDAO.updateMenuItem(Long.valueOf(valueMap.get("threeBMenuItemId").toString()).longValue(),
						valueMap.get("threeBMenuItemName").toString(), valueMap.get("threeBMenuItemCount").toString(),
						"", Integer.parseInt(valueMap.get("threeBMenuItemOrder").toString()));
			}

			if (valueMap.containsKey("towBMenuItemName")
					&& StringUtils.isNotBlank(valueMap.get("towBMenuItemName").toString())) {
				wxMenuItemDAO.updateMenuItem(Long.valueOf(valueMap.get("towBMenuItemId").toString()).longValue(),
						valueMap.get("towBMenuItemName").toString(), "", valueMap.get("towBMenuItemCK").toString(),
						Integer.parseInt(valueMap.get("towBMenuItemOrder").toString()));
			}

			if (valueMap.containsKey("oneBMenuItemName")
					&& StringUtils.isNotBlank(valueMap.get("oneBMenuItemName").toString())) {
				wxMenuItemDAO.updateMenuItem(Long.valueOf(valueMap.get("oneBMenuItemId").toString()).longValue(),
						valueMap.get("oneBMenuItemName").toString(), "", valueMap.get("oneBMenuItemCK").toString(),
						Integer.parseInt(valueMap.get("oneBMenuItemOrder").toString()));
			}

			// 第三级菜单修改
			if (valueMap.containsKey("threeMenu") && StringUtils.isNotBlank(valueMap.get("threeMenu").toString())) {
				wxMenuDAO.updateMenu(Long.valueOf(valueMap.get("threeMenuId").toString()).longValue(),
						valueMap.get("threeMenu").toString(), null);
			}

			// 第三级子菜单修改
			if (valueMap.containsKey("fiveMenuItem")
					&& StringUtils.isNotBlank(valueMap.get("fiveMenuItem").toString())) {
				wxMenuItemDAO.updateMenuItem(Long.valueOf(valueMap.get("fiveMenuItemId").toString()).longValue(),
						valueMap.get("fiveMenuItem").toString(), valueMap.get("fiveMenuItemContent").toString(), "",
						Integer.parseInt(valueMap.get("fiveMenuItemOrder").toString()));
			}

			if (valueMap.containsKey("fourHMenuItem")
					&& StringUtils.isNotBlank(valueMap.get("fourHMenuItem").toString())) {
				wxMenuItemDAO.updateMenuItem(Long.valueOf(valueMap.get("fourHMenuItemId").toString()).longValue(),
						valueMap.get("fourHMenuItem").toString(), valueMap.get("fourHMenuItemCount").toString(), "",
						Integer.parseInt(valueMap.get("fourHMenuItemOrder").toString()));
			}

			if (valueMap.containsKey("threeMenuItem")
					&& StringUtils.isNotBlank(valueMap.get("threeMenuItem").toString())) {
				wxMenuItemDAO.updateMenuItem(Long.valueOf(valueMap.get("threeMenuItemId").toString()).longValue(),
						valueMap.get("threeMenuItem").toString(), valueMap.get("threeMenuItemCount").toString(), "",
						Integer.parseInt(valueMap.get("threeMenuItemOrder").toString()));
			}

			if (valueMap.containsKey("towMenuItem") && StringUtils.isNotBlank(valueMap.get("towMenuItem").toString())) {
				wxMenuItemDAO.updateMenuItem(Long.valueOf(valueMap.get("towMenuItemId").toString()).longValue(),
						valueMap.get("towMenuItem").toString(), "", valueMap.get("towMenuItemCK").toString(),
						Integer.parseInt(valueMap.get("towMenuItemOrder").toString()));
			}

			if (valueMap.containsKey("oneMenuItem") && StringUtils.isNotBlank(valueMap.get("oneMenuItem").toString())) {
				wxMenuItemDAO.updateMenuItem(Long.valueOf(valueMap.get("oneMenuItemId").toString()).longValue(),
						valueMap.get("oneMenuItem").toString(), "", valueMap.get("oneMenuItemCK").toString(),
						Integer.parseInt(valueMap.get("oneMenuItemOrder").toString()));
			}

		} catch (NumberFormatException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));

		}

		// 每次先删除后build
		try {
			String deleteMenuUrl = new StringBuilder().append(Constant.weChatUrl).append(WxInterfaceApi.MENUWXAPI)
					.append("/").append(WxInterfaceApi.DELETEMENU).toString();
			Map<String, Object> deleteMenuMap = Maps.newHashMap();
			HttpClientUtil.callUrlPost(deleteMenuUrl, deleteMenuMap);

			Thread.sleep(5000);
			String postUrl = new StringBuilder().append(Constant.weChatUrl).append(WxInterfaceApi.MENUWXAPI).append("/")
					.append(WxInterfaceApi.CREATEMENU).toString();
			Map<String, Object> paramsMap = Maps.newHashMap();
			HttpClientUtil.callUrlPost(postUrl, paramsMap);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}

	}

	/**
	 * 积分页面兑换商品时增加消息推送功能
	 * 
	 * @param tOrder
	 *            订单对象
	 * @param openId
	 *            用户的openId
	 */
	public void pushPrizeExchangeTemplate(TOrderBonus tOrderBonus, Integer bonus, String openId) {
		try {
			if (StringUtils.isNotBlank(openId)) {
				String BonusUrl = new StringBuilder().append(Constant.bonusUrl).toString();
				String postUrl = new StringBuilder().append(Constant.weChatPushUrl).append("/")
						.append(TemplateMessage.TEMPLATEMSG_TYPE_ORDERBONUS).toString();
				Map<String, Object> paramsMap = Maps.newHashMap();
				paramsMap.put("openid", openId);
				paramsMap.put("url", BonusUrl);
				paramsMap.put("first", Constant.bonusOrderFirst);
				paramsMap.put("remark", Constant.bonusOrderRemark);
				paramsMap.put("keyword1", tOrderBonus.getTEventBonus().getTEvent().getFtitle());
				paramsMap.put("keyword2", tOrderBonus.getTEventBonus().getFbonus() + "积分");
				paramsMap.put("keyword3", bonus + "积分");
				paramsMap.put("keyword4", DateFormatUtils.format(tOrderBonus.getFcreateTime(), "yyyy年MM月dd日 HH:mm")
						+ "\n客服备注：" + tOrderBonus.getFreply());
				HttpClientUtil.callUrlPost(postUrl, paramsMap);
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			logger.error("发送积分页面兑换商品提醒模板消息时，积分页面兑换商品提醒模板消息接口出错。");
		}
	}

	/**
	 * 客服回复提醒
	 * 
	 * @param tOrder
	 *            订单对象
	 * @param openId
	 *            用户的openId
	 */
	public void pushOrderReplayTemplate(String date, String name, TOrder order, String replay) {
		try {
			if (StringUtils.isNotBlank(order.getTCustomer().getFweixinId())) {
				String orderDetailUrl = new StringBuilder().append(Constant.orderDetail).append(order.getId())
						.toString();
				String postUrl = new StringBuilder().append(Constant.weChatPushUrl).append("/")
						.append(TemplateMessage.TEMPLATEMSG_TYPE_ORDERREPLAY).toString();
				Map<String, Object> paramsMap = Maps.newHashMap();
				paramsMap.put("openid", order.getTCustomer().getFweixinId());
				paramsMap.put("url", orderDetailUrl);
				paramsMap.put("first", Constant.orderReplayFirst);
				paramsMap.put("remark", Constant.orderReplayRemark);
				paramsMap.put("keyword1", name);
				paramsMap.put("keyword2", date);
				paramsMap.put("keyword3", replay);
				HttpClientUtil.callUrlPost(postUrl, paramsMap);
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			logger.error("发送客服回复提醒模板消息时，客服回复提醒模板消息接口出错。");
		}
	}

	/**
	 * 发送订单评价返积分提醒模板消息
	 * 
	 * @param tOrder
	 *            订单对象
	 * @param openId
	 *            用户的openId
	 */
	public void pushOrderEvaluationBonusMsg(TOrder tOrder, String openId) {
		try {
			if (StringUtils.isNotBlank(openId)) {
				String orderDetailUrl = new StringBuilder().append(Constant.orderDetail).append(tOrder.getId())
						.toString();
				String postUrl = new StringBuilder().append(Constant.weChatPushUrl).append("/")
						.append(TemplateMessage.TEMPLATEMSG_TYPE_COMPLIMENTARYPOINTS).toString();
				Map<String, Object> paramsMap = Maps.newHashMap();
				paramsMap.put("openid", openId);
				paramsMap.put("url", orderDetailUrl);
				paramsMap.put("first", Constant.orderEvaluationBonusFirst);
				paramsMap.put("remark", Constant.orderEvaluationBonusRemark);
				paramsMap.put("keyword1", tOrder.getForderNum());
				paramsMap.put("keyword2",
						new StringBuilder().append(DateFormatUtils.format(tOrder.getFcreateTime(), "yyyy年MM月dd日 HH:mm"))
								.append("\n").append(tOrder.getTEvent().getFtitle()));
				HttpClientUtil.callUrlPost(postUrl, paramsMap);
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			logger.error("发送订单评价返积分提醒模板消息时，订单评价返积分提醒模板消息接口出错。");
		}
	}

	/**
	 * 发送订单评价返先提醒模板消息
	 * 
	 * @param tOrder
	 *            订单对象
	 * @param openId
	 *            用户的openId
	 */
	public void pushOrderCashBackMsg(TOrder tOrder, String openId) {
		try {
			if (StringUtils.isNotBlank(openId)) {
				String orderDetailUrl = new StringBuilder().append(Constant.orderDetail).append(tOrder.getId())
						.toString();
				String postUrl = new StringBuilder().append(Constant.weChatPushUrl).append("/")
						.append(TemplateMessage.TEMPLATEMSG_TYPE_COMPLIMENTARYPOINTS).toString();
				Map<String, Object> paramsMap = Maps.newHashMap();
				paramsMap.put("openid", openId);
				paramsMap.put("url", orderDetailUrl);
				paramsMap.put("first", Constant.orderCashBackFirst);
				paramsMap.put("remark", Constant.orderCashBackRemark);
				paramsMap.put("keyword1", tOrder.getForderNum());
				paramsMap.put("keyword2",
						new StringBuilder().append(DateFormatUtils.format(tOrder.getFcreateTime(), "yyyy年MM月dd日 HH:mm"))
								.append("\n").append(tOrder.getTEvent().getFtitle()));
				HttpClientUtil.callUrlPost(postUrl, paramsMap);
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			logger.error("发送订单评价返现金提醒模板消息时，订单评价返现金提醒模板消息接口出错。");
		}
	}

	/**
	 * 积分到账提醒
	 * 
	 * @param tOrder
	 *            订单对象
	 * @param openId
	 *            用户的openId
	 */
	public void pushBonusAccountMsg(TCustomer tCustomer, TCustomerInfo tCustomerInfo, Integer bonusType,
			Integer bonus) {
		try {
			if (StringUtils.isNotBlank(tCustomer.getFweixinId())) {
				String BonusUrl = new StringBuilder().append(Constant.bonusUrl).toString();
				String postUrl = new StringBuilder().append(Constant.weChatPushUrl).append("/")
						.append(TemplateMessage.TEMPLATEMSG_TYPE_BONUSACCOUNT).toString();
				Map<String, Object> paramsMap = Maps.newHashMap();
				paramsMap.put("openid", tCustomer.getFweixinId());
				paramsMap.put("url", BonusUrl);
				paramsMap.put("first", Constant.bonusAccountRemark);
				paramsMap.put("remark", Constant.bonusAccountRemark);
				paramsMap.put("keyword1", tCustomer.getFweixinName());
				paramsMap.put("keyword2",
						new StringBuilder().append(DateFormatUtils.format(new Date(), "yyyy年MM月dd日 HH:mm")));
				paramsMap.put("keyword3", DictionaryUtil.getString(DictionaryUtil.BonusType, bonusType));
				paramsMap.put("keyword4", bonus);
				paramsMap.put("keyword5", tCustomerInfo.getFpoint());
				HttpClientUtil.callUrlPost(postUrl, paramsMap);
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			logger.error("发送积分到账提醒模板消息时，积分到账提醒模板消息接口出错。");
		}
	}

	@Transactional(readOnly = true)
	public void getWechatPushList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();

		hql.append(
				"select t.id as DT_RowId,t.ftitle as ftitle,t.fbeginTitle as fbeginTitle,t.fendTitle as fendTitle,t.femailTitle as femailTitle,t.fsender as fsender,t.furl as furl,t.fsendList as fsendList,t.fplanNum as fplanNum,t.fdeliveryNum as fdeliveryNum,t.fstartTime as fstartTime,t.fendTime as fendTime,t.fstatus as fstatus,t.foperator as foperator,t.fcreateTime as fcreateTime  from TWechatPush t where t.fstatus>999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		try {
			if (valueMap.containsKey("s_ftitle") && StringUtils.isNotBlank(valueMap.get("s_ftitle").toString())) {
				hql.append(" and t.ftitle like :s_ftitle ");
				hqlMap.put("s_ftitle", "%" + valueMap.get("s_ftitle").toString() + "%");
			}
			if (valueMap.containsKey("fstartTime") && StringUtils.isNotBlank(valueMap.get("fstartTime").toString())) {
				hql.append(" and t.fstartTime >= :fstartTime ");
				hqlMap.put("fstartTime", DateUtils.parseDate(valueMap.get("fstartTime").toString(), "yyyy-MM-dd"));
			}
			if (valueMap.containsKey("fendTime") && StringUtils.isNotBlank(valueMap.get("fendTime").toString())) {
				hql.append(" and t.fendTime < :fendTime ");
				// 选择的截止日期加一天，保证了查询条件的完整性，解决了时分秒的问题。
				hqlMap.put("fendTime",
						DateUtils.addDays(DateUtils.parseDate(valueMap.get("fendTime").toString(), "yyyy-MM-dd"), 1));
			}
			if (valueMap.containsKey("s_status") && StringUtils.isNotBlank(valueMap.get("s_status").toString())) {
				hql.append(" and t.fstatus >= :s_status ");
				hqlMap.put("s_status", Integer.valueOf(valueMap.get("s_status").toString()));
			}

			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.fcreateTime");
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		Date date = null;
		for (Map<String, Object> amap : list) {
			if (amap.get("fstartTime") != null && StringUtils.isNotBlank(amap.get("fstartTime").toString())) {
				date = (Date) amap.get("fstartTime");
				amap.put("fstartTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
			if (amap.get("fendTime") != null && StringUtils.isNotBlank(amap.get("fendTime").toString())) {
				date = (Date) amap.get("fendTime");
				amap.put("fendTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
			if (amap.get("fcreateTime") != null && StringUtils.isNotBlank(amap.get("fcreateTime").toString())) {
				date = (Date) amap.get("fcreateTime");
				amap.put("fcreateTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
			// if (amap.get("fstatus") != null &&
			// StringUtils.isNotBlank(amap.get("fstatus").toString())) {
			// amap.put("fstatusString",
			// DictionaryUtil.getString(DictionaryUtil.WechatPushStatus,
			// (Integer) amap.get("fstatus"), shiroUser.getLanguage()));
			// }
			if (amap.get("foperator") != null && StringUtils.isNotBlank(amap.get("foperator").toString())) {
				amap.put("foperator", userDAO.getOne((Long) amap.get("foperator")).getRealname());
			}
		}
	}

	public void addWechatPush(Map<String, Object> valueMap) {

		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		TWechatPush twechatPush = new TWechatPush();

		if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
			twechatPush.setFtitle(valueMap.get("ftitle").toString());
		}
		if (valueMap.containsKey("fbeginTitle") && StringUtils.isNotBlank(valueMap.get("fbeginTitle").toString())) {
			twechatPush.setFbeginTitle(valueMap.get("fbeginTitle").toString());
		}
		if (valueMap.containsKey("femailTitle") && StringUtils.isNotBlank(valueMap.get("femailTitle").toString())) {
			twechatPush.setFemailTitle(valueMap.get("femailTitle").toString());
		}
		if (valueMap.containsKey("fsender") && StringUtils.isNotBlank(valueMap.get("fsender").toString())) {
			twechatPush.setFsender(valueMap.get("fsender").toString());
		}
		if (valueMap.containsKey("fsender") && StringUtils.isNotBlank(valueMap.get("fsender").toString())) {
			twechatPush.setFsender(valueMap.get("fsender").toString());
		}
		if (valueMap.containsKey("furl") && StringUtils.isNotBlank(valueMap.get("furl").toString())) {
			twechatPush.setFurl(valueMap.get("furl").toString());
		}
		if (valueMap.containsKey("fendTitle") && StringUtils.isNotBlank(valueMap.get("fendTitle").toString())) {
			twechatPush.setFurl(valueMap.get("fendTitle").toString());
		}
		// 获取文本信息

		twechatPush.setFsendList("");
		twechatPush.setFplanNum(0);
		twechatPush.setFdeliveryNum(0);
		twechatPush.setFstatus(10);
		twechatPush.setFoperator(shiroUser.getId());
		twechatPush.setFcreateTime(new Date());

		wechatPushDAO.save(twechatPush);

	}

	public void editWechatPush(Map<String, Object> valueMap) {

		// TWechatPush twechatPush =
		// wechatPushDAO.getOne(valueMap.get("id").toString());
		//
		// if (valueMap.containsKey("fbonus") &&
		// StringUtils.isNotBlank(valueMap.get("fbonus").toString())) {
		// twechatPush.setFbonus(Integer.valueOf(valueMap.get("fbonus").toString()));
		// } else {
		// twechatPush.setFbonus(null);
		// }
		// wechatPushDAO.save(twechatPush);
	}

	public void delWechatPush(String fID) {
		wechatPushDAO.updateStatus(999, fID);
	}

	public void stopWechatPush(String fID, Integer Status) {
		// eventBargainingDAO.saveStatusBargaining(Status, fID, new Date());
	}

	public void pushMessage(String fID) {

	}

	public void messageTxtUtil() {

		try {
			String encoding = "GBK"; // 字符编码(可解决中文乱码问题 )

			File file = new File("D:/errlog.txt");

			if (file.isFile() && file.exists()) {

				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);

				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTXT = null;
				while ((lineTXT = bufferedReader.readLine()) != null) {

					// 将文本读取出来的字符去掉"," 因为读取出来的字符是用","来分隔的
					String text = lineTXT.replaceAll(",", "");

					// 用字符分隔成数组
					String[] text1 = text.split("-");

					for (int i = 0; i < text1.length; i++) {

						String[] test2 = text1[i].split(":");

						for (int j = 0; j < test2.length; j++) {

							System.out.println(test2[j]);
						}
					}
				}
				read.close();
			} else {
				System.out.println("找不到指定的文件！");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容操作出错");
			e.printStackTrace();
		}
	}

}