package com.czyh.czyhweb.service.push;

import java.text.ParseException;
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
import org.springside.modules.mapper.BeanMapper;
import org.springside.modules.utils.Exceptions;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.czyh.czyhweb.dao.PushCustomerInfoDAO;
import com.czyh.czyhweb.dao.PushDAO;
import com.czyh.czyhweb.dao.TimingTaskDAO;
import com.czyh.czyhweb.dao.UserDAO;
import com.czyh.czyhweb.dto.ResponseDTO;
import com.czyh.czyhweb.entity.TPush;
import com.czyh.czyhweb.entity.TPushCustomerInfo;
import com.czyh.czyhweb.entity.TTimingTask;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.CommonService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.PropertiesUtil;
import com.czyh.czyhweb.util.push.AndroidNotification;
import com.czyh.czyhweb.util.push.PushClient;
import com.czyh.czyhweb.util.push.android.AndroidBroadcast;
import com.czyh.czyhweb.util.push.android.AndroidGroupcast;
import com.czyh.czyhweb.util.push.android.AndroidUnicast;
import com.czyh.czyhweb.util.push.ios.IOSBroadcast;
import com.czyh.czyhweb.util.push.ios.IOSGroupcast;
import com.czyh.czyhweb.util.push.ios.IOSUnicast;

/**
 * 推送业务类
 * 
 * @author jinshengzhi
 *
 */
@Component
@Transactional
public class PushService {

	private static final Logger logger = LoggerFactory.getLogger(PushService.class);

	private PushClient client = new PushClient();

	@Autowired
	private PushDAO PushDAO;

	@Autowired
	private CommonService commonService;

	@Autowired
	private TimingTaskDAO timingTaskDAO;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private PushCustomerInfoDAO pushCustomerInfoDAO;

	/**
	 * 创建推送消息
	 * 
	 * @param valueMap
	 */
	public void savePush(Map<String, Object> valueMap) {

		try {
			if (valueMap.containsKey("fpushTime") && StringUtils.isNotBlank(valueMap.get("fpushTime").toString())) {
				valueMap.put("fpushTime",
						DateUtils.parseDate(valueMap.get("fpushTime").toString(), "yyyy-MM-dd HH:mm"));
			}

			if (valueMap.containsKey("fvalidTime") && StringUtils.isNotBlank(valueMap.get("fvalidTime").toString())) {
				valueMap.put("fvalidTime",
						DateUtils.parseDate(valueMap.get("fvalidTime").toString(), "yyyy-MM-dd HH:mm"));
			}

		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException();
		}

		TPush tpush = new TPush();
		BeanMapper.copy(valueMap, tpush);

		Date now = new Date();
		tpush.setFcreateTime(now);
		tpush = PushDAO.save(tpush);
	}

	/**
	 * 广播推送
	 * 
	 * @param valueMap
	 */
	public void savePushAll(Map<String, Object> valueMap) {

		try {
			if (valueMap.containsKey("fpushTime") && StringUtils.isNotBlank(valueMap.get("fpushTime").toString())) {
				valueMap.put("fpushTime",
						DateUtils.parseDate(valueMap.get("fpushTime").toString(), "yyyy-MM-dd HH:mm"));
			}

		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException();
		}

		TPush tpush = new TPush();
		BeanMapper.copy(valueMap, tpush);

		Date now = new Date();
		tpush.setFcreateTime(now);
		tpush = PushDAO.save(tpush);

		String target_type = DictionaryUtil.getString(DictionaryUtil.PushLinkTargetType, tpush.getFtargetType());

		// 进行消息推送
		try {
			AndroidBroadcast broadcast = new AndroidBroadcast(PropertiesUtil.getProperty("umappkey"),
					PropertiesUtil.getProperty("appMasterSecret"));
			broadcast.setTicker("您有新的通知栏消息");
			broadcast.setTitle(tpush.getFtitle());// 消息标题
			broadcast.setText(tpush.getFcontent());// 消息内容
			broadcast.setDescription(tpush.getFdescription());// 消息描述
			broadcast.goAppAfterOpen();
			broadcast.setDisplayType(AndroidNotification.DisplayType.NOTIFICATION);

			// broadcast.setProductionMode();//生产环境 “上线后打开此注释切换生产模式”
			broadcast.setTestMode();// 测试环境
			broadcast.setExtraField("target_type", target_type);
			broadcast.setExtraField("target_id", tpush.getFtargetObject());

			client.send(broadcast);
		} catch (Exception e) {

			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}

		// IOS全局消息推送
		try {
			IOSBroadcast broadcast = new IOSBroadcast(PropertiesUtil.getProperty("IOSUMAppKey"),
					PropertiesUtil.getProperty("IOSappMasterSecret"));

			broadcast.setAlert(tpush.getFcontent());// 推送的消息
			broadcast.setBadge(1);// 设置角标
			broadcast.setSound("default");// 设置声音
			broadcast.setDescription(tpush.getFdescription());
			// broadcast.setProductionMode();//生产环境 “上线后打开此注释切换生产模式”
			broadcast.setTestMode();// 测试环境
			broadcast.setCustomizedField("target_type", target_type);
			broadcast.setCustomizedField("target_id", tpush.getFtargetObject());

			client.send(broadcast);
		} catch (Exception e) {

			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}

	}

	/**
	 * 单播推送
	 * 
	 * @param valueMap
	 */
	public void savePushUnicast(Map<String, Object> valueMap) {

		try {
			if (valueMap.containsKey("fpushTime") && StringUtils.isNotBlank(valueMap.get("fpushTime").toString())) {
				valueMap.put("fpushTime",
						DateUtils.parseDate(valueMap.get("fpushTime").toString(), "yyyy-MM-dd HH:mm"));
			}

		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException();
		}

		TPush tpush = new TPush();
		BeanMapper.copy(valueMap, tpush);

		Date now = new Date();
		tpush.setFcreateTime(now);
		tpush = PushDAO.save(tpush);

		String target_type = DictionaryUtil.getString(DictionaryUtil.PushLinkTargetType, tpush.getFtargetType());

		// 进行单播消息推送
		try {
			AndroidUnicast unicast = new AndroidUnicast(PropertiesUtil.getProperty("umappkey"),
					PropertiesUtil.getProperty("appMasterSecret"));

			unicast.setDeviceToken("");
			unicast.setTicker("您有新的通知消息");
			unicast.setTitle(tpush.getFtitle());
			unicast.setText(tpush.getFcontent());
			unicast.setDescription(tpush.getFdescription());
			unicast.goAppAfterOpen();
			unicast.setDisplayType(AndroidNotification.DisplayType.NOTIFICATION);

			// unicast.setProductionMode();//生产环境 “上线后打开此注释切换生产模式”
			unicast.setTestMode();// 测试环境

			unicast.setExtraField("target_type", target_type);
			unicast.setExtraField("target_id", tpush.getFtargetObject());

			client.send(unicast);
		} catch (Exception e) {

			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}

		// IOS单播消息推送
		try {

			IOSUnicast unicast = new IOSUnicast(PropertiesUtil.getProperty("IOSUMAppKey"),
					PropertiesUtil.getProperty("IOSappMasterSecret"));

			unicast.setDeviceToken("");
			unicast.setAlert(tpush.getFcontent());
			unicast.setBadge(1);
			unicast.setSound("default");
			unicast.setDescription(tpush.getFdescription());
			// unicast.setProductionMode();//生产环境 “上线后打开此注释切换生产模式”
			unicast.setTestMode();// 测试环境

			// System.out.println("当前字符串为空");
			unicast.setCustomizedField("target_type", target_type);
			unicast.setCustomizedField("target_id", tpush.getFtargetObject());

			client.send(unicast);
		} catch (Exception e) {

			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}

	}

	/**
	 * 组播推送
	 * 
	 * @param valueMap
	 */
	public void savePushGroupcast(Map<String, Object> valueMap) {

		try {
			if (valueMap.containsKey("fpushTime") && StringUtils.isNotBlank(valueMap.get("fpushTime").toString())) {
				valueMap.put("fpushTime",
						DateUtils.parseDate(valueMap.get("fpushTime").toString(), "yyyy-MM-dd HH:mm"));
			}

		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException();
		}

		TPush tpush = new TPush();
		BeanMapper.copy(valueMap, tpush);

		Date now = new Date();
		tpush.setFcreateTime(now);
		tpush = PushDAO.save(tpush);

		String target_type = DictionaryUtil.getString(DictionaryUtil.PushLinkTargetType, tpush.getFtargetType());

		String dimension = DictionaryUtil.getString(DictionaryUtil.PushDimension, 1);// 基础维度
		// tpush.getFdimension()
		if (dimension.equals("标签")) {
			dimension = "tag";
		} else if (dimension.equals("版本号")) {
			dimension = "app_version";
		} else if (dimension.equals("用户活跃度")) {
			dimension = "launch_from";
		} else if (dimension.equals("渠道")) {
			dimension = "channel";
		}

		String usertag = DictionaryUtil.getString(DictionaryUtil.PushUserTag, tpush.getFuserTag());// 用户标签

		String appVersion = DictionaryUtil.getString(DictionaryUtil.AppVersion, 1);// 用户版本号
		// tpush.getFappVersion()
		// 进行组播消息推送
		try {

			AndroidGroupcast groupcast = new AndroidGroupcast(PropertiesUtil.getProperty("umappkey"),
					PropertiesUtil.getProperty("appMasterSecret"));

			JSONObject filterJson = new JSONObject();
			JSONObject whereJson = new JSONObject();
			JSONArray tagArray = new JSONArray();
			JSONObject testTag = new JSONObject();
			JSONObject TestTag = new JSONObject();

			if (usertag.equals("")) {
				testTag.put(dimension, appVersion);
			} else {
				testTag.put(dimension, usertag);
			}

			// TestTag.put("channel", "www");
			tagArray.add(testTag);
			// tagArray.put(TestTag);

			whereJson.put("and", tagArray);
			filterJson.put("where", whereJson);

			groupcast.setFilter(filterJson);
			groupcast.setTicker("您有新的通知消息");
			groupcast.setTitle(tpush.getFtitle());
			groupcast.setText(tpush.getFcontent());
			groupcast.setDescription(tpush.getFdescription());
			groupcast.goAppAfterOpen();
			groupcast.setDisplayType(AndroidNotification.DisplayType.NOTIFICATION);
			// groupcast.setProductionMode();//生产环境 “上线后打开此注释切换生产模式”
			groupcast.setTestMode();// 测试环境

			groupcast.setExtraField("target_type", target_type);
			groupcast.setExtraField("target_id", tpush.getFtargetObject());

			client.send(groupcast);
		} catch (Exception e) {

			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}

		// IOS组播消息推送
		try {

			IOSGroupcast groupcast = new IOSGroupcast(PropertiesUtil.getProperty("IOSUMAppKey"),
					PropertiesUtil.getProperty("IOSappMasterSecret"));

			JSONObject filterJson = new JSONObject();
			JSONObject whereJson = new JSONObject();
			JSONArray tagArray = new JSONArray();
			JSONObject testTag = new JSONObject();
			testTag.put("tag", "iostest");
			tagArray.add(testTag);
			whereJson.put("and", tagArray);
			filterJson.put("where", whereJson);

			groupcast.setFilter(filterJson);
			groupcast.setAlert(tpush.getFcontent());
			groupcast.setBadge(1);
			groupcast.setSound("default");
			groupcast.setDescription(tpush.getFdescription());
			// groupcast.setProductionMode();//生产环境 “上线后打开此注释切换生产模式”
			groupcast.setTestMode();// 测试环境

			groupcast.setCustomizedField("target_type", target_type);
			groupcast.setCustomizedField("target_id", tpush.getFtargetObject());

			client.send(groupcast);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}

	}

	/**
	 * 获取推送列表信息
	 * 
	 * @param valueMap
	 * @param page
	 */
	@Transactional(readOnly = true)
	public void getPushList(Map<String, Object> valueMap, CommonPage page) {

		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.ftitle as ftitle, t.fcontent as fcontent, t.fimage as fimage, t.ftype as ftype, t.ftargetObject as ftargetObject, t.ftargetType as ftargetType, t.fpushTime as fpushTime, t.fdescription as fdescription, t.fstatus as fstatus, t.fcreateTime as fcreateTime, t.fauditStatus as fauditStatus,t.fvalidTime as fvalidTime,t.fauditMessage as fauditMessage,t.fauditTime as fauditTime,t.foperator as foperator from TPush t where t.fstatus < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {

			if (valueMap.containsKey("s_fdescription")
					&& StringUtils.isNotBlank(valueMap.get("s_fdescription").toString())) {
				hql.append(" and t.fdescription like :s_fdescription");
				hqlMap.put("s_fdescription", "%" + valueMap.get("s_fdescription").toString() + "%");
			}

			if (valueMap.containsKey("s_ftitle") && StringUtils.isNotBlank(valueMap.get("s_ftitle").toString())) {
				hql.append(" and t.ftitle like :s_ftitle ");
				hqlMap.put("s_ftitle", "%" + valueMap.get("s_ftitle").toString() + "%");
			}

			if (valueMap.containsKey("fcreateTimeStart")
					&& StringUtils.isNotBlank(valueMap.get("fcreateTimeStart").toString())) {
				hql.append(" and t.fpushTime >= :fcreateTimeStart ");
				hqlMap.put("fcreateTimeStart",
						DateUtils.parseDate(valueMap.get("fcreateTimeStart").toString(), "yyyy-MM-dd"));
			}

			if (valueMap.containsKey("fcreateTimeEnd")
					&& StringUtils.isNotBlank(valueMap.get("fcreateTimeEnd").toString())) {
				hql.append(" and t.fpushTime < :fcreateTimeEnd ");
				// 选择的截止日期加一天，保证了查询条件的完整性，解决了时分秒的问题。
				hqlMap.put("fcreateTimeEnd", DateUtils
						.addDays(DateUtils.parseDate(valueMap.get("fcreateTimeEnd").toString(), "yyyy-MM-dd"), 1));
			}

			if (valueMap.containsKey("s_ftargetType")
					&& StringUtils.isNotBlank(valueMap.get("s_ftargetType").toString())) {
				hql.append(" and t.ftargetType = :s_ftargetType ");
				hqlMap.put("s_ftargetType", Integer.valueOf(valueMap.get("s_ftargetType").toString()));
			}

			if (valueMap.containsKey("s_fstatus") && StringUtils.isNotBlank(valueMap.get("s_fstatus").toString())) {
				int status = Integer.valueOf(valueMap.get("s_fstatus").toString());
				hql.append(" and t.fstatus = :status");
				hqlMap.put("status", status);
			}
			if (valueMap.containsKey("s_fauditStatus")
					&& StringUtils.isNotBlank(valueMap.get("s_fauditStatus").toString())) {
				int fauditStatus = Integer.valueOf(valueMap.get("s_fauditStatus").toString());
				hql.append(" and t.fauditStatus = :fauditStatus");
				hqlMap.put("fauditStatus", fauditStatus);
			}

			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.fcreateTime desc");
			}

		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}

		commonService.findPage(hql.toString(), page, hqlMap);

		List<Map<String, Object>> list = page.getResult();
		Date date = null;
		for (Map<String, Object> amap : list) {

			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatusString", DictionaryUtil.getString(DictionaryUtil.PushStatus,
						((Integer) amap.get("fstatus")), shiroUser.getLanguage()));
			}

			if (amap.get("fpushTime") != null && StringUtils.isNotBlank(amap.get("fpushTime").toString())) {
				date = (Date) amap.get("fpushTime");
				amap.put("fPushTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}

			if (amap.get("fcreateTime") != null && StringUtils.isNotBlank(amap.get("fcreateTime").toString())) {
				date = (Date) amap.get("fcreateTime");
				amap.put("fcreateTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}

			if (amap.get("fvalidTime") != null && StringUtils.isNotBlank(amap.get("fvalidTime").toString())) {
				date = (Date) amap.get("fvalidTime");
				amap.put("fvalidTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}

			if (amap.get("fauditTime") != null && StringUtils.isNotBlank(amap.get("fauditTime").toString())) {
				date = (Date) amap.get("fauditTime");
				amap.put("fauditTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}

			if (amap.get("ftargetType") != null && StringUtils.isNotBlank(amap.get("ftargetType").toString())) {
				amap.put("ftargetType", DictionaryUtil.getString(DictionaryUtil.PushLinkTargetType,
						((Integer) amap.get("ftargetType")), shiroUser.getLanguage()));
			}

			if (amap.get("fauditStatus") != null && StringUtils.isNotBlank(amap.get("fauditStatus").toString())) {
				amap.put("fauditStatusString", DictionaryUtil.getString(DictionaryUtil.PushfauditStatus,
						((Integer) amap.get("fauditStatus")), shiroUser.getLanguage()));
			}

			if (amap.get("foperator") != null && StringUtils.isNotBlank(amap.get("foperator").toString())) {
				amap.put("foperator", userDAO.getOne((Long) amap.get("foperator")).getRealname());
			}

		}
	}

	public List<TPush> getPushList() {
		return PushDAO.findAllPushList();
	}

	public void delPush(String pid) {
		PushDAO.saveUpdatedel(999, pid);
	}

	/**
	 * 推送成功并添加定时任务
	 * 
	 * @param pushId
	 * @param times
	 */
	public void saveUpdateSuccess(String pushId, String times) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Date date = new Date();
		times = date.toString();
		Long timelong = date.getTime();

		TTimingTask timingTask = new TTimingTask();
		timingTask.setEntityId(pushId);
		timingTask.setTaskTime(timelong);
		timingTask.setTaskType(16);

		timingTaskDAO.save(timingTask);

		PushDAO.saveUpdatePush(20, 40, pushId, shiroUser.getId());
	}

	public void saveUpdatedefeate(String pushId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		PushDAO.saveUpdatePush(30, 30, pushId, shiroUser.getId());
	}

	public void removePush(String pushId) {
		PushDAO.saveUpdatedel(50, pushId);
		timingTaskDAO.clearTimeTaskByEntityId(pushId);
	}

	/**
	 * 审核备注
	 * 
	 * @param orderId
	 */
	public String saveAuditMessage(String pushId, String auditMessage) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder message = new StringBuilder();
		message.append(StringUtils.LF).append("＜－－－－－－－－－－ ")
				.append(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm")).append(" －－－－－－－－－－ ")
				.append(StringUtils.SPACE).append(StringUtils.SPACE).append("填写人：").append(shiroUser.getName())
				.append(" －－－－－－－－－－＞").append(StringUtils.LF).append(auditMessage);

		TPush push = PushDAO.getOne(pushId);
		if (StringUtils.isNotBlank(push.getFauditMessage())) {
			message.insert(0, push.getFauditMessage());
		}
		PushDAO.updateAuditMessage(pushId, message.toString());
		return message.toString();
	}

	@Transactional(readOnly = true)
	public TPush getDetails(String pushId) {
		return PushDAO.getOne(pushId);
	}

	/**
	 * 添加 AndroidTag
	 * 
	 * @param tag
	 * @param DeviceToken
	 * @return
	 * @throws Exception
	 */
	@Transactional(readOnly = true)
	public ResponseDTO tagAndroidAdd(String tag, String device_tokens) throws Exception {
		ResponseDTO res = client.AddTags(PropertiesUtil.getProperty("umappkey"),
				PropertiesUtil.getProperty("appMasterSecret"), tag, device_tokens);
		return res;
	}

	/**
	 * 添加 IOSTag
	 * 
	 * @param tag
	 * @param DeviceToken
	 * @return
	 * @throws Exception
	 */
	@Transactional(readOnly = true)
	public ResponseDTO tagIOSAdd(String tag, String device_tokens) throws Exception {
		ResponseDTO res = client.AddTags(PropertiesUtil.getProperty("IOSUMAppKey"),
				PropertiesUtil.getProperty("IOSappMasterSecret"), tag, device_tokens);
		return res;
	}

	/**
	 * 移除AndroidTag
	 * 
	 * @param tag
	 * @param DeviceToken
	 * @return
	 * @throws Exception
	 */
	@Transactional(readOnly = true)
	public ResponseDTO tagAndroidDel(String tag, String device_tokens) throws Exception {
		ResponseDTO res = client.DelTag(PropertiesUtil.getProperty("umappkey"),
				PropertiesUtil.getProperty("appMasterSecret"), tag, device_tokens);
		return res;
	}

	/**
	 * 移除IOSTag
	 * 
	 * @param tag
	 * @param DeviceToken
	 * @return
	 * @throws Exception
	 */
	@Transactional(readOnly = true)
	public ResponseDTO tagIOSDel(String tag, String device_tokens) throws Exception {
		ResponseDTO res = client.DelTag(PropertiesUtil.getProperty("IOSUMAppKey"),
				PropertiesUtil.getProperty("IOSappMasterSecret"), tag, device_tokens);
		return res;
	}

	public void savePushCustomerInfo(String fcustomerId, String ftitle, String fcontent, String fimage, String ftype,
			Integer ftargetType, String ftargetObject, Date fpushTime, String fdescription, String fpageTitle) {

		try {
			TPushCustomerInfo tPushCustomerInfo = new TPushCustomerInfo();
			tPushCustomerInfo.setFcustomerId(fcustomerId);
			tPushCustomerInfo.setFtitle(ftitle);
			tPushCustomerInfo.setFcontent(fcontent);
			tPushCustomerInfo.setFimage(fimage);
			tPushCustomerInfo.setFtype(ftype);
			tPushCustomerInfo.setFtargetType(ftargetType);
			tPushCustomerInfo.setFtargetObject(ftargetObject);
			tPushCustomerInfo.setFpushTime(fpushTime);
			tPushCustomerInfo.setFdescription(fdescription);
			tPushCustomerInfo.setFpageTitle(fpageTitle);
			tPushCustomerInfo.setFunread(0);
			tPushCustomerInfo.setFstatus(20);

			pushCustomerInfoDAO.save(tPushCustomerInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 系统推送单播用
	 * 
	 * @param valueMap
	 */
	public void pushMessage(Integer ftargetType, String ftitle, String fcontent, String fdescription,
			String ftargetObject, String fdeviceToken, String fcustomerId, String fimage, String ftype,
			String fpageTitle) {

		String target_type = DictionaryUtil.getString(DictionaryUtil.PushLinkTargetType, ftargetType);

		Date date = new Date();
		try {
			AndroidUnicast unicast = new AndroidUnicast(PropertiesUtil.getProperty("umappkey"),
					PropertiesUtil.getProperty("appMasterSecret"));

			unicast.setDeviceToken(fdeviceToken);
			unicast.setTicker("您有新的通知消息");
			unicast.setTitle(ftitle);
			unicast.setText(fcontent);
			unicast.setDescription(fdescription);
			unicast.goAppAfterOpen();
			unicast.setDisplayType(AndroidNotification.DisplayType.NOTIFICATION);

			unicast.setProductionMode();// 生产环境 “上线后打开此注释切换生产模式”
			// unicast.setTestMode();// 测试环境
			unicast.setExtraField("target_type", target_type);
			unicast.setExtraField("target_id", ftargetObject);

			client.send(unicast);

		} catch (Exception e) {

			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}

		try {

			IOSUnicast unicast = new IOSUnicast(PropertiesUtil.getProperty("IOSUMAppKey"),
					PropertiesUtil.getProperty("IOSappMasterSecret"));

			unicast.setDeviceToken(fdeviceToken);
			unicast.setAlert(fcontent);
			unicast.setBadge(1);
			unicast.setSound("default");
			unicast.setDescription(fdescription);
			unicast.setProductionMode();// 生产环境 “上线后打开此注释切换生产模式”
			// unicast.setTestMode();// 测试环境

			unicast.setCustomizedField("target_type", target_type);
			unicast.setCustomizedField("target_id", ftargetObject);

			client.send(unicast);

		} catch (Exception e) {

			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
		savePushCustomerInfo(fcustomerId, ftitle, fcontent, fimage, ftype, ftargetType, ftargetObject, date,
				fdescription, fpageTitle);
	}

	/*
	 * 订单待付款通知
	 */
	public void toPaid(String orderNum, String orderId, String fdeviceToken, String fcustomerId) {
		this.pushMessage(10, "订单未付款通知", "您的订单:" + orderNum + "暂未付款，系统将在20分钟后自动取消订单，请在“我-待付款”中及时付款。。", "您有一个订单尚未付款",
				orderId, fdeviceToken, fcustomerId, "", "1", "");
	}

	/*
	 * 订单支付成功推送
	 */
	public void successPayment(String orderNum, String orderId, String fdeviceToken, String fcustomerId) {
		this.pushMessage(10, "订单支付成功通知", "您好，您的订单编号为:" + orderNum + "}付款已成功！请在“我-待发货”中查看物流动向。", "您有一个订单支付成功", orderId,
				fdeviceToken, fcustomerId, "", "1", "");
	}

	/*
	 * 物流发货推送
	 */
	public void confirmGoods(String orderNum, String express, String orderId, String fdeviceToken, String fcustomerId) {
		this.pushMessage(10, "订单物流发货通知", "您好，您的订单编号为：" + orderNum + "正在向您飞奔！请在“我-待收货”中查看物流状态。", "您的订单已发货", orderId,
				fdeviceToken, fcustomerId, "", "1", "");
	}

	/*
	 * 退款发起通知
	 */
	public void refund(String orderNum, String orderId, String fdeviceToken, String fcustomerId) {
		this.pushMessage(10, "订单退款审核成功通知", "您的订单编号为：" + orderNum + "退款已通过审核，退款金额将在0-7个工作日返回原支付账户，请注意查收~", "您的退款订单已审核通过",
				orderId, fdeviceToken, fcustomerId, "", "1", "");
	}

	/*
	 * 优惠券过期通知
	 */
	public void couponOverdue(String money, String fdeviceToken, String fcustomerId, String customerName) {
		this.pushMessage(8, "优惠券即将过期通知", "亲爱的" + customerName + "您有1张优惠券即将过期，快来查找优惠放肆买买买吧！", "您有一张优惠券即将过期", "mecoupon",
				fdeviceToken, fcustomerId, "", "1", "");
	}

	/*
	 * 积分到账通知
	 */
	public void bonusAccount(String bonus, String fdeviceToken, String fcustomerId, String customerName) {
		this.pushMessage(9, "积分到账通知", "亲爱的" + customerName + "，您的U币已到账，请在“我-U币社”中兑换你心水的商品哦~", "您有积分入账", "welfare",
				fdeviceToken, fcustomerId, "", "1", "");
	}

	/*
	 * 积分消耗通知
	 */
	public void bonusConsumption(String bonus, String goodsTitle, String fdeviceToken, String fcustomerId,
			String customerName) {
		this.pushMessage(9, "消耗积分通知", "恭喜您兑换商品" + goodsTitle + "成功！本次消耗" + bonus + "U币，邀请好友得更多U币！详情请【点击这里】", "您消耗了积分",
				"welfare", fdeviceToken, fcustomerId, "", "1", "");
	}

}