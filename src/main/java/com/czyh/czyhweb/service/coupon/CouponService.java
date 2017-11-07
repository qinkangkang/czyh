package com.czyh.czyhweb.service.coupon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
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

import com.czyh.czyhweb.common.dict.ResponseConfigurationDict;
import com.czyh.czyhweb.dao.AppChannelEventDAO;
import com.czyh.czyhweb.dao.AppChannelSettingDAO;
import com.czyh.czyhweb.dao.CouponActivitycouponsDAO;
import com.czyh.czyhweb.dao.CouponChannelDAO;
import com.czyh.czyhweb.dao.CouponDAO;
import com.czyh.czyhweb.dao.CouponDeliveryDAO;
import com.czyh.czyhweb.dao.CouponDeliveryHistoryDAO;
import com.czyh.czyhweb.dao.CouponInformationDAO;
import com.czyh.czyhweb.dao.CouponObjectDAO;
import com.czyh.czyhweb.dao.CustomerDAO;
import com.czyh.czyhweb.dao.DeliveryDAO;
import com.czyh.czyhweb.dao.EventCategoryDAO;
import com.czyh.czyhweb.dao.ImageDAO;
import com.czyh.czyhweb.dao.TimingTaskDAO;
import com.czyh.czyhweb.dao.UserDAO;
import com.czyh.czyhweb.entity.TAppChannelSetting;
import com.czyh.czyhweb.entity.TCouponActivitycoupons;
import com.czyh.czyhweb.entity.TCouponChannel;
import com.czyh.czyhweb.entity.TCouponDelivery;
import com.czyh.czyhweb.entity.TCouponDeliveryHistory;
import com.czyh.czyhweb.entity.TCouponInformation;
import com.czyh.czyhweb.entity.TCouponObject;
import com.czyh.czyhweb.entity.TCustomer;
import com.czyh.czyhweb.entity.TDelivery;
import com.czyh.czyhweb.entity.TTimingTask;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.CommonService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.ConfigurationUtil;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.PropertiesUtil;
import com.google.common.collect.Lists;

import net.sf.ehcache.Element;

/**
 * 优惠券业务管理类
 * 
 * @author jinshengzhi
 */
@Component
@Transactional
public class CouponService {

	private static final Logger logger = LoggerFactory.getLogger(CouponService.class);

	@Autowired
	private CommonService commonService;

	@Autowired
	private TimingTaskDAO timingTaskDAO;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private CouponObjectDAO couponObjectDAO;

	@Autowired
	private CouponInformationDAO couponInformationDAO;

	@Autowired
	private DeliveryDAO deliveryDAO;

	@Autowired
	private CouponDeliveryDAO couponDeliveryDAO;

	@Autowired
	private CouponActivitycouponsDAO couponActivitycouponsDAO;

	@Autowired
	private CustomerDAO customerDAO;

	@Autowired
	private CouponDeliveryHistoryDAO couponDeliveryHistoryDAO;

	@Autowired
	private CouponChannelDAO couponChannelDAO;

	@Autowired
	private AppChannelSettingDAO appChannelSettingDAO;

	@Transactional(readOnly = true)
	public void getDeliveryList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.ftitle as ftitle, t.fdeliveryCreateTime as deliveryTime ,t.foperator as operator ,t.fstatus as status ,t.fdeliverType as deliveryType ,t.freciveLimit as reciveLimit,t.fdeliveryStartTime as startTime,t.fdeliveryEndTime as endTime from TDelivery t where 1=1");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_ftitle") && StringUtils.isNotBlank(valueMap.get("s_ftitle").toString())) {
				hql.append(" and t.ftitle like :s_ftitle ");
				hqlMap.put("s_ftitle", "%" + valueMap.get("s_ftitle").toString() + "%");
			}
			if (valueMap.containsKey("s_fcreaterId")
					&& StringUtils.isNotBlank(valueMap.get("s_fcreaterId").toString())) {
				hql.append(" and t.foperator = :s_fcreaterId ");
				hqlMap.put("s_fcreaterId", Long.valueOf(valueMap.get("s_fcreaterId").toString()));
			}
			if (valueMap.containsKey("s_status") && StringUtils.isNotBlank(valueMap.get("s_status").toString())) {
				hql.append(" and t.fstatus = :s_status ");
				hqlMap.put("s_status", Integer.parseInt((valueMap.get("s_status").toString())));
			}
			if (valueMap.containsKey("factivityType")
					&& StringUtils.isNotBlank(valueMap.get("factivityType").toString())) {
				hql.append(" and t.factivityType = :factivityType ");
				hqlMap.put("factivityType", Integer.parseInt((valueMap.get("factivityType").toString())));
			}
			if (valueMap.containsKey("fcreateTimeStart")
					&& StringUtils.isNotBlank(valueMap.get("fcreateTimeStart").toString())) {
				hql.append(" and t.fdeliveryCreateTime >= :fcreateTimeStart ");
				hqlMap.put("fcreateTimeStart",
						DateUtils.parseDate(valueMap.get("fcreateTimeStart").toString(), "yyyy-MM-dd"));
			}
			if (valueMap.containsKey("fcreateTimeEnd")
					&& StringUtils.isNotBlank(valueMap.get("fcreateTimeEnd").toString())) {
				hql.append(" and t.fdeliveryCreateTime < :fcreateTimeEnd ");
				// 选择的截止日期加一天，保证了查询条件的完整性，解决了时分秒的问题。
				hqlMap.put("fcreateTimeEnd", DateUtils
						.addDays(DateUtils.parseDate(valueMap.get("fcreateTimeEnd").toString(), "yyyy-MM-dd"), 1));
			}

		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		hql.append(" order by t.fcreateTime desc");
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		Date date = null;
		for (Map<String, Object> amap : list) {

			if (amap.get("ftitle") != null && StringUtils.isNotBlank(amap.get("ftitle").toString())) {
				amap.put("ftitle", amap.get("ftitle").toString());
			}
			if (amap.get("deliverType") != null && StringUtils.isNotBlank(amap.get("deliverType").toString())) {
				amap.put("fdeliverType", DictionaryUtil.getString(DictionaryUtil.CouponDeliverType,
						((Integer) amap.get("deliverType")), shiroUser.getLanguage()));
			}
			if (amap.get("status") != null && StringUtils.isNotBlank(amap.get("status").toString())) {
				amap.put("statusString", DictionaryUtil.getString(DictionaryUtil.CouponAuditStatus,
						((Integer) amap.get("status")), shiroUser.getLanguage()));
				amap.put("status", amap.get("status").toString());
			}
			if (amap.get("operator") != null && StringUtils.isNotBlank(amap.get("operator").toString())) {
				amap.put("operator", userDAO.getOne((Long) amap.get("operator")).getRealname());
			}
			if (amap.get("deliveryTime") != null && StringUtils.isNotBlank(amap.get("deliveryTime").toString())) {
				date = (Date) amap.get("deliveryTime");
				amap.put("deliveryTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
			}
			if (amap.get("startTime") != null && StringUtils.isNotBlank(amap.get("startTime").toString())) {
				date = (Date) amap.get("startTime");
				amap.put("startTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
			}
			if (amap.get("endTime") != null && StringUtils.isNotBlank(amap.get("endTime").toString())) {
				date = (Date) amap.get("endTime");
				amap.put("endTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
			}
			if (amap.get("deliveryType") != null && StringUtils.isNotBlank(amap.get("deliveryType").toString())) {
				amap.put("deliveryType", DictionaryUtil.getString(DictionaryUtil.CouponDeliverType,
						((Integer) amap.get("deliveryType")), shiroUser.getLanguage()));
			}
		}
	}

	@Transactional(readOnly = true)
	public void getCouponList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.ftitle as ftitle, t.fcouponNum as couponnum ,t.fuseRange as useRange ,t.fuseStartTime as starttime ,t.fuseEndTime as endtime,t.fvalidDays as validDays,t.famount as amount,t.flimitation as limitation,t.fdiscount as discount,t.fuseType as usetype,t.fuserPoint as usepoint,t.fcouponDesc as coupondesc,t.foperator as operator,t.fcreateTime as createtime from TCouponInformation t where 1=1");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_ftitle") && StringUtils.isNotBlank(valueMap.get("s_ftitle").toString())) {
				hql.append(" and t.ftitle like :s_ftitle ");
				hqlMap.put("s_ftitle", "%" + valueMap.get("s_ftitle").toString() + "%");
			}
			if (valueMap.containsKey("s_fcreaterId")
					&& StringUtils.isNotBlank(valueMap.get("s_fcreaterId").toString())) {
				hql.append(" and t.foperator = :s_fcreaterId ");
				hqlMap.put("s_fcreaterId", Long.valueOf(valueMap.get("s_fcreaterId").toString()));
			}
			if (valueMap.containsKey("type") && StringUtils.isNotBlank(valueMap.get("type").toString())) {
				hql.append(" and t.fcouponType = :type ");
				hqlMap.put("type", Integer.parseInt(valueMap.get("type").toString()));
			}
			if (valueMap.containsKey("status") && StringUtils.isNotBlank(valueMap.get("status").toString())) {
				hql.append(" and t.fcouponStatus = :status ");
				hqlMap.put("status", Integer.parseInt((String) valueMap.get("status")));
				// 如果是新添加的优惠券
				if (Integer.parseInt((String) valueMap.get("status")) == 0) {
					hql.append(" and t.foperator = :userId ");
					hqlMap.put("userId", shiroUser.getId());
				}
			}
			if (valueMap.containsKey("fcreateTimeStart")
					&& StringUtils.isNotBlank(valueMap.get("fcreateTimeStart").toString())) {
				hql.append(" and t.fcreateTime >= :fcreateTimeStart ");
				hqlMap.put("fcreateTimeStart",
						DateUtils.parseDate(valueMap.get("fcreateTimeStart").toString(), "yyyy-MM-dd"));
			}
			if (valueMap.containsKey("fcreateTimeEnd")
					&& StringUtils.isNotBlank(valueMap.get("fcreateTimeEnd").toString())) {
				hql.append(" and t.fcreateTime < :fcreateTimeEnd ");
				// 选择的截止日期加一天，保证了查询条件的完整性，解决了时分秒的问题。
				hqlMap.put("fcreateTimeEnd", DateUtils
						.addDays(DateUtils.parseDate(valueMap.get("fcreateTimeEnd").toString(), "yyyy-MM-dd"), 1));
			}

		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		Element ele = null;

		Date date = null;
		for (Map<String, Object> amap : list) {

			if (amap.get("ftitle") != null && StringUtils.isNotBlank(amap.get("ftitle").toString())) {
				amap.put("ftitle", amap.get("ftitle").toString());
			}
			if (amap.get("discount") != null && StringUtils.isNotBlank(amap.get("discount").toString())) {
				amap.put("conditions", amap.get("discount").toString() + "折");
			} else {
				amap.put("conditions", "满" + amap.get("limitation").toString() + "减" + amap.get("amount").toString());
			}
			if (amap.get("operator") != null && StringUtils.isNotBlank(amap.get("operator").toString())) {
				amap.put("operator", userDAO.getOne((Long) amap.get("operator")).getRealname());
			}
			if (amap.get("createtime") != null && StringUtils.isNotBlank(amap.get("createtime").toString())) {
				date = (Date) amap.get("createtime");
				amap.put("createtime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
			if (amap.get("starttime") != null && StringUtils.isNotBlank(amap.get("starttime").toString())) {
				date = (Date) amap.get("starttime");
				amap.put("starttimeS", DateFormatUtils.format(date, "yyyy-MM-dd"));
			}
			if (amap.get("endtime") != null && StringUtils.isNotBlank(amap.get("endtime").toString())) {
				date = (Date) amap.get("endtime");
				amap.put("endtimeS", DateFormatUtils.format(date, "yyyy-MM-dd"));
			}
		}
	}

	public void saveCoupon(Map<String, Object> valueMap) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		try {
			if (valueMap.containsKey("fuseStartTime")
					&& StringUtils.isNotBlank(valueMap.get("fuseStartTime").toString())) {
				valueMap.put("fuseStartTime",
						DateUtils.parseDate(valueMap.get("fuseStartTime").toString(), "yyyy-MM-dd"));
			}
			if (valueMap.containsKey("fuseEndTime") && StringUtils.isNotBlank(valueMap.get("fuseEndTime").toString())) {
				valueMap.put("fuseEndTime", DateUtils.parseDate(valueMap.get("fuseEndTime").toString() + " 23:59:59",
						"yyyy-MM-dd HH:mm:ss"));
			}
		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException();
		}
		TCouponInformation couponInformation = new TCouponInformation();
		BeanMapper.copy(valueMap, couponInformation);
		if (valueMap.containsKey("fdiscount") && StringUtils.isNotBlank(valueMap.get("fdiscount").toString())) {
			BigDecimal fdiscount = new BigDecimal(valueMap.get("fdiscount").toString()).divide(new BigDecimal(100), 2,
					RoundingMode.HALF_UP);
			couponInformation.setFdiscount(fdiscount);
		}
		Date now = new Date();
		couponInformation.setFoperator(shiroUser.getId());
		couponInformation.setFcreateTime(now);
		couponInformation.setFupdateTime(now);
		// 默认添加的状态为0
		couponInformation.setFcouponStatus(0);
		couponInformation.setFcouponType(Integer.parseInt(valueMap.get("fCouponType").toString()));
		couponInformation = couponInformationDAO.save(couponInformation);

		TCouponObject tCouponObject = new TCouponObject();
		if (couponInformation.getFuseType().intValue() == 10) {
			tCouponObject.setTCouponInformation(couponInformation);
			tCouponObject.setFuseType(couponInformation.getFuseType());
		} else if (couponInformation.getFuseType().intValue() == 30) {
			tCouponObject.setTCouponInformation(couponInformation);
			tCouponObject.setFuseType(couponInformation.getFuseType());
			tCouponObject.setFobjectId(valueMap.get("ftypeA").toString());
			TAppChannelSetting appChannelSetting = appChannelSettingDAO.findOne(valueMap.get("ftypeA").toString());
			tCouponObject.setFobjectTitle(appChannelSetting.getFtitle());

		} else {
			tCouponObject.setTCouponInformation(couponInformation);
			tCouponObject.setFuseType(couponInformation.getFuseType());
			tCouponObject.setFobjectId(valueMap.get("fentityId").toString());
		}
		couponObjectDAO.save(tCouponObject);
	}

	public void delCoupon(String couponId) {
		couponInformationDAO.delete(couponId);
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> checkPhoneList(Map<String, Object> valueMap) {

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		// 如果是自定义客户发放
		String[] phones = valueMap.get("fphone").toString().split(",");
		List<String> ponelist = new ArrayList<String>();
		Collections.addAll(ponelist, phones);
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as id from TCustomer t where t.ftype = 1 and t.fstatus = 1 and t.fphone IN(:phoneList)");

		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("phoneList", ponelist);
		list = commonService.find(hql.toString(), hqlMap);
		return list;
	}

	@SuppressWarnings("unchecked")
	public void savedelivery(Map<String, Object> valueMap) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Date now = new Date();
		// 保存定向投放活动信息
		TDelivery tDelivery = new TDelivery();
		BeanMapper.copy(valueMap, tDelivery);
		Integer fActivityType = Integer.parseInt(valueMap.get("fActivityType").toString());
		tDelivery.setFactivityType(fActivityType);// 定向投放活动
		if (fActivityType == 20) {
			tDelivery.setFreciveChannel(Integer.parseInt(valueMap.get("freciveChannel").toString()));
			tDelivery.setFreciveLimit(Integer.parseInt(valueMap.get("fReciveLimit").toString()));
			try {
				tDelivery
						.setFdeliveryStartTime(DateUtils.parseDate(valueMap.get("StartTime").toString(), "yyyy-MM-dd"));
				tDelivery.setFdeliveryEndTime(
						DateUtils.parseDate(valueMap.get("EndTime").toString() + " 23:59:59", "yyyy-MM-dd HH:mm:ss"));
			} catch (ParseException e) {
				logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			}
		} else {
			tDelivery.setFisPush(Integer.parseInt(valueMap.get("is_push").toString()));
			tDelivery.setFpushContent(valueMap.get("fpush").toString());
		}
		tDelivery.setFdeliverType(Integer.parseInt(valueMap.get("fDeliverType").toString()));
		tDelivery.setFoperator(shiroUser.getId());
		tDelivery.setFstatus(10);// 已发放
		tDelivery.setFdeliveryCreateTime(now);
		tDelivery.setFcreateTime(now);
		tDelivery = deliveryDAO.save(tDelivery);

		// 本次活动要发放的优惠券
		String infos = valueMap.get("couponinfo").toString();
		String[] couponinfoArr = infos.split(",");

		// 发放优惠券数量
		Integer count = 0;
		// 发放优惠券
		if (tDelivery.getFactivityType() == 10) {
			// 如果是主动发放类型
			if (tDelivery.getFdeliverType() == 10) {
				// 如果是全部发放 不在领取表中加入具体数据，在使用后再加入数据
				// count 为全部商户数量
				StringBuilder hql = new StringBuilder();
				hql.append("select t.id as id from TCustomer t where t.ftype = 1 and t.fstatus = 1");
				List<Map<String, Object>> list = commonService.find(hql.toString());
				count = list.size();

				List<TCouponDelivery> couponDeliveryList = Lists.newArrayList();
				TCouponDelivery tCouponDelivery = null;
				for (int i = 0; i < couponinfoArr.length; i++) {
					TCouponInformation couponInformation = couponInformationDAO.findOne(couponinfoArr[i]);
					tCouponDelivery = new TCouponDelivery();
					tCouponDelivery.setFdeliverTime(now);
					tCouponDelivery.setFuseStartTime(couponInformation.getFuseStartTime());
					tCouponDelivery.setFuseEndTime(couponInformation.getFuseEndTime());
					tCouponDelivery.setTDelivery(tDelivery);
					tCouponDelivery.setTCouponInformation(couponInformation);
					couponDeliveryList.add(tCouponDelivery);
				}
				couponDeliveryDAO.save(couponDeliveryList);
			} else {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				if (tDelivery.getFdeliverType() == 30) {
					// 如果是新客户发放

				} else if (tDelivery.getFdeliverType() == 80) {
					// 如果是自定义客户发放
					String[] phones = valueMap.get("fphone").toString().split(",");
					List<String> ponelist = new ArrayList<String>();
					Collections.addAll(ponelist, phones);
					StringBuilder hql = new StringBuilder();
					hql.append(
							"select t.id as id from TCustomer t where t.ftype = 1 and t.fstatus = 1 and t.fphone IN(:phoneList)");

					Map<String, Object> hqlMap = new HashMap<String, Object>();
					hqlMap.put("phoneList", ponelist);
					list = commonService.find(hql.toString(), hqlMap);
				}
				count = list.size();
				List<TCouponDelivery> couponDeliveryList = Lists.newArrayList();

				TCouponDelivery tCouponDelivery = null;
				String customerId = null;
				for (Map<String, Object> amap : list) {
					for (int i = 0; i < couponinfoArr.length; i++) {
						TCouponInformation couponInformation = couponInformationDAO.findOne(couponinfoArr[i]);
						customerId = amap.get("id").toString();
						tCouponDelivery = new TCouponDelivery();
						tCouponDelivery.setFdeliverTime(now);
						tCouponDelivery.setFuseStartTime(couponInformation.getFuseStartTime());
						tCouponDelivery.setFuseEndTime(couponInformation.getFuseEndTime());
						tCouponDelivery.setTDelivery(tDelivery);
						tCouponDelivery.setTCouponInformation(couponInformation);
						tCouponDelivery.setTCustomer(new TCustomer(customerId));

						couponDeliveryList.add(tCouponDelivery);
					}
				}
				couponDeliveryDAO.save(couponDeliveryList);
			}
		} else {
			count = Integer.parseInt(valueMap.get("coupon_num").toString());
		}

		// 保存定向投放活动与优惠券关联信息
		List<TCouponActivitycoupons> activitycouponsList = Lists.newArrayList();
		for (int i = 0; i < couponinfoArr.length; i++) {
			TCouponActivitycoupons activitycoupons = new TCouponActivitycoupons();
			activitycoupons.setFcreatetime(now);
			activitycoupons.setFdeliveryId(tDelivery.getId());
			activitycoupons.setFcouponId(couponinfoArr[i]);
			activitycoupons.setFdeliveryCount(count);
			activitycoupons.setFuseCount(0);
			if (tDelivery.getFactivityType() == 10) {
				activitycoupons.setFsendCount(count);
			} else {
				activitycoupons.setFsendCount(0);
			}
			activitycouponsList.add(activitycoupons);
			// 更新优惠券状态
			couponInformationDAO.updateCouponInfoStatus(10, couponinfoArr[i]);
		}
		couponActivitycouponsDAO.save(activitycouponsList);
	}

	@Transactional(readOnly = true)
	public TDelivery getDelivery(String deliveryId) {
		return deliveryDAO.getOne(deliveryId);
	}

	public Map<String, Object> getDeliveryDetail(String deliveryId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		TDelivery tTDelivery = deliveryDAO.findOne(deliveryId);
		Map<String, Object> map = new HashMap<String, Object>();

		if (StringUtils.isNotBlank(tTDelivery.getFcreateTime().toString())) {
			map.put("fdeliveryCreateTime",
					DateFormatUtils.format(tTDelivery.getFdeliveryCreateTime(), "yyyy-MM-dd HH:mm"));
		}
		if (tTDelivery.getFactivityType() == 20) {
			if (StringUtils.isNotBlank(tTDelivery.getFdeliveryStartTime().toString())) {
				map.put("fdeliveryStartTime",
						DateFormatUtils.format(tTDelivery.getFdeliveryStartTime(), "yyyy-MM-dd HH:mm"));
			}
			if (StringUtils.isNotBlank(tTDelivery.getFdeliveryEndTime().toString())) {
				map.put("fdeliveryEndTime",
						DateFormatUtils.format(tTDelivery.getFdeliveryEndTime(), "yyyy-MM-dd HH:mm"));
			}
		}
		if (tTDelivery.getFstatus() != 10) {
			if (StringUtils.isNotBlank(tTDelivery.getFupdateTime().toString())) {
				map.put("fupdateTime", DateFormatUtils.format(tTDelivery.getFupdateTime(), "yyyy-MM-dd HH:mm"));
				map.put("fauditor", userDAO.getOne((Long) tTDelivery.getFauditor()).getRealname());
			}
		}
		map.put("ftitle", tTDelivery.getFtitle());
		map.put("foperator", userDAO.getOne((Long) tTDelivery.getFoperator()).getRealname());
		map.put("fisPush", tTDelivery.getFisPush());
		map.put("fpushContent", tTDelivery.getFpushContent());
		map.put("fstatus", tTDelivery.getFstatus());
		if (tTDelivery.getFisPush() == null || tTDelivery.getFisPush() == 10) {
			map.put("fisPushString", "不需推送");
		} else if (tTDelivery.getFisPush() == 20) {
			map.put("fisPushString", "短信推送");
		} else {
			map.put("fisPushString", "push和微信推送");
		}
		map.put("statusString", DictionaryUtil.getString(DictionaryUtil.CouponAuditStatus,
				((Integer) map.get("fstatus")), shiroUser.getLanguage()));
		if (map.get("deliveryType") != null && StringUtils.isNotBlank(map.get("deliveryType").toString())) {
			map.put("deliveryType", DictionaryUtil.getString(DictionaryUtil.CouponDeliverType,
					((Integer) map.get("deliveryType")), shiroUser.getLanguage()));
		}
		StringBuilder reciveCouponUrl = new StringBuilder();
		reciveCouponUrl.append(PropertiesUtil.getProperty("reciveCouponUrl")).append(tTDelivery.getId());
		map.put("reciveCouponUrl", reciveCouponUrl.toString());
		map.put("factivityType", tTDelivery.getFactivityType());
		map.put("freciveChannel", tTDelivery.getFreciveChannel());
		return map;
	}

	@Transactional(readOnly = true)
	public void getDeliveryCoupon(Map<String, Object> valueMap, CommonPage page) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.ftitle as ftitle, t.fcouponNum as couponnum ,t.fuseRange as useRange ,t.fuseStartTime as starttime ,t.fuseEndTime as endtime,t.fvalidDays as validDays,t.famount as amount,t.flimitation as limitation,t.fdiscount as discount,t.fuseType as usetype,t.fuserPoint as usepoint,t.fcouponDesc as coupondesc,t.foperator as operator,t.fcreateTime as createtime,a.fdeliveryCount as deliveryCount,a.fsendCount as sendCount,a.fuseCount as useCount from TCouponInformation t inner join TCouponActivitycoupons a on t.id = a.fcouponId inner join TDelivery d on d.id = fdeliveryId where 1=1");

		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hql.append(" and d.id = :fdeliveryId ");
		hqlMap.put("fdeliveryId", valueMap.get("deliveryId").toString());
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> infolist = page.getResult();

		Date date = null;
		for (Map<String, Object> amap : infolist) {
			if (amap.get("ftitle") != null && StringUtils.isNotBlank(amap.get("ftitle").toString())) {
				amap.put("ftitle", amap.get("ftitle").toString());
			}
			if (amap.get("discount") != null && StringUtils.isNotBlank(amap.get("discount").toString())) {
				amap.put("conditions", amap.get("discount").toString() + "折");
			} else {
				amap.put("conditions", "满" + amap.get("limitation").toString() + "减" + amap.get("amount").toString());
			}
			if (amap.get("operator") != null && StringUtils.isNotBlank(amap.get("operator").toString())) {
				amap.put("operator", userDAO.getOne((Long) amap.get("operator")).getRealname());
			}
			if (amap.get("createtime") != null && StringUtils.isNotBlank(amap.get("createtime").toString())) {
				date = (Date) amap.get("createtime");
				amap.put("createtime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
			}
			if (amap.get("sendCount") == null || !StringUtils.isNotBlank(amap.get("sendCount").toString())) {
				amap.put("sendCount", 0);
			}
			if (amap.get("useCount") == null || !StringUtils.isNotBlank(amap.get("useCount").toString())) {
				amap.put("useCount", 0);
			}
		}
	}

	public void auditDelivery(Map<String, Object> valueMap) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Integer status = Integer.parseInt(valueMap.get("status").toString());
		long fAuditor = shiroUser.getId();
		Date now = new Date();
		String deliveryId = valueMap.get("deliveryId").toString();
		deliveryDAO.updatedeliverystatus(status, fAuditor, now, deliveryId);
		if (status == 30) {
			// TODO 如果是审核通过，则校验是否推送

			TDelivery tDelivery = deliveryDAO.findOne(deliveryId);
			// 如果是领取活动
			if (tDelivery.getFactivityType() == 20) {
				// 添加一个优惠券领取开始定时任务
				if (tDelivery.getFdeliveryStartTime() != null) {
					TTimingTask tEventTimingTask = new TTimingTask();
					tEventTimingTask.setEntityId(tDelivery.getId());
					tEventTimingTask.setTaskTime(tDelivery.getFdeliveryStartTime().getTime());
					tEventTimingTask.setTaskType(10);
					timingTaskDAO.save(tEventTimingTask);
				}
				// 会添加一个优惠券领取过期定时任务
				if (tDelivery.getFdeliveryEndTime() != null) {
					TTimingTask tEventTimingTask = new TTimingTask();
					tEventTimingTask.setEntityId(tDelivery.getId());
					tEventTimingTask.setTaskTime(tDelivery.getFdeliveryEndTime().getTime());
					tEventTimingTask.setTaskType(11);
					timingTaskDAO.save(tEventTimingTask);
				}
			}
		} else if (status == 50) {
			// 删除已经插入数据库的该活动的优惠券
			couponDeliveryDAO.deleteByDeliveryId(deliveryId);
		}
	}

	@Transactional(readOnly = true)
	public void getcustomertList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.fusername as fusername, t.fname as fname, t.fphone as fphone, t.fsex as fsex, t.fstatus as fstatus from TCustomer t where t.ftype = 1 and t.fstatus < 999 and t.id in(select DISTINCT(c.TCustomer.id) from TCouponDelivery c inner join c.TDelivery d where d.fstatus = 30)");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_fhone") && StringUtils.isNotBlank(valueMap.get("s_fhone").toString())) {
				hql.append(" and t.fusername like :s_fhone ");
				hqlMap.put("s_fhone", "%" + valueMap.get("s_fhone").toString() + "%");
			}

		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		for (Map<String, Object> amap : list) {
			if (amap.get("ftype") != null && StringUtils.isNotBlank(amap.get("ftype").toString())) {
				amap.put("ftype", DictionaryUtil.getString(DictionaryUtil.SponsorType, (Integer) amap.get("ftype"),
						shiroUser.getLanguage()));
			}
			if (amap.get("fsex") != null && StringUtils.isNotBlank(amap.get("fsex").toString())) {
				amap.put("fsex", DictionaryUtil.getString(DictionaryUtil.Sex, (Integer) amap.get("fsex"),
						shiroUser.getLanguage()));
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatusString", DictionaryUtil.getString(DictionaryUtil.UserStatus,
						(Integer) amap.get("fstatus"), shiroUser.getLanguage()));
			}
		}
	}

	@Transactional(readOnly = true)
	public void getcustomertdetail(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select d.ftitle as dtitle,f.ftitle as ftitle,f.flimitation as limit,f.famount as amount,f.fdiscount as discount from TCouponDelivery c inner join c.TDelivery d inner join c.TCouponInformation f where 1=1");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("customerId") && StringUtils.isNotBlank(valueMap.get("customerId").toString())) {
				hql.append(" and c.TCustomer.id = :customerId ");
				hqlMap.put("customerId", valueMap.get("customerId").toString());
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
				amap.put("fstatusString", DictionaryUtil.getString(DictionaryUtil.CouponStatus,
						(Integer) amap.get("fstatus"), shiroUser.getLanguage()));
			}
			if (amap.get("discount") != null && StringUtils.isNotBlank(amap.get("discount").toString())) {
				amap.put("conditions", amap.get("discount").toString() + "折");
			} else {
				amap.put("conditions", "满" + amap.get("limit").toString() + "减" + amap.get("amount").toString());
			}
			if (amap.get("usetime") != null && StringUtils.isNotBlank(amap.get("usetime").toString())) {
				date = (Date) amap.get("usetime");
				amap.put("usetime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
			}
		}
	}

	@Transactional(readOnly = true)
	public TCustomer getCustomer(String customerId) {
		return customerDAO.getOne(customerId);
	}

	// 定时器将过期优惠券迁移到历史表中
	public void updateCouponStatus() {
		// 先查询出过期优惠券
		StringBuilder hql = new StringBuilder();
		hql.append("select t.id as couponDeliveryId,t.TCouponInformation.id as couponId,t.TCustomer.id as customerId,")
				.append(" t.TDelivery.id as deliveryId,t.fdeliverTime as deliveryTime,t.fuseStartTime as startTime,")
				.append("t.fuseEndTime as endTime,t.ffromOrderId as fromOrderId from TCouponDelivery t inner join t.TDelivery d where t.fuseEndTime <:now and t.TDelivery != null and d.fstatus in(40,90,100,120)");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("now", new Date());
		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		List<TCouponDelivery> couponDeliveryList = Lists.newArrayList();
		TCouponDelivery tCouponDelivery = null;
		// 添加到历史表中
		List<TCouponDeliveryHistory> couponDeliveryHistoriesList = Lists.newArrayList();
		TCouponDeliveryHistory couponDeliveryHistory = null;
		for (Map<String, Object> cmap : list) {
			if (cmap.get("customerId") != null && StringUtils.isNotBlank(cmap.get("customerId").toString())) {
				couponDeliveryHistory = new TCouponDeliveryHistory();
				couponDeliveryHistory.setFdeliverTime((Date) cmap.get("deliveryTime"));
				couponDeliveryHistory.setFstatus(90);
				couponDeliveryHistory.setFuseEndTime((Date) cmap.get("endTime"));
				couponDeliveryHistory.setFuseStartTime((Date) cmap.get("startTime"));
				couponDeliveryHistory.setTCouponInformation(new TCouponInformation(cmap.get("couponId").toString()));
				couponDeliveryHistory.setTCustomer(new TCustomer(cmap.get("customerId").toString()));
				couponDeliveryHistory.setTDelivery(new TDelivery(cmap.get("deliveryId").toString()));
				if (cmap.get("fromOrderId") != null && StringUtils.isNotBlank(cmap.get("fromOrderId").toString())) {
					couponDeliveryHistory.setFfromOrderId(cmap.get("fromOrderId").toString());
				}
				couponDeliveryHistoriesList.add(couponDeliveryHistory);
			}

			tCouponDelivery = new TCouponDelivery();
			tCouponDelivery.setId(cmap.get("couponDeliveryId").toString());
			couponDeliveryList.add(tCouponDelivery);
		}
		couponDeliveryHistoryDAO.save(couponDeliveryHistoriesList);
		// 删除过期优惠券
		couponDeliveryDAO.deleteInBatch(couponDeliveryList);
	}

	// 将订单已使用的优惠券返回给用户
	public void backCoupon(String orderId) {
		TCouponDeliveryHistory couponDeliveryHistory = couponDeliveryHistoryDAO.getCouponbyOrder(orderId);
		if (couponDeliveryHistory != null) {
			if (couponDeliveryHistory.getFuseEndTime().compareTo(new Date()) == 1) {
				TCouponDelivery tCouponDelivery = new TCouponDelivery();
				tCouponDelivery.setFdeliverTime(couponDeliveryHistory.getFdeliverTime());
				tCouponDelivery.setFuseEndTime(couponDeliveryHistory.getFuseEndTime());
				tCouponDelivery.setFuseStartTime(couponDeliveryHistory.getFuseStartTime());
				tCouponDelivery.setTCouponInformation(couponDeliveryHistory.getTCouponInformation());
				tCouponDelivery.setTCustomer(couponDeliveryHistory.getTCustomer());
				tCouponDelivery.setTDelivery(couponDeliveryHistory.getTDelivery());
				if (couponDeliveryHistory.getFfromOrderId() != null) {
					tCouponDelivery.setFfromOrderId(couponDeliveryHistory.getFfromOrderId());
				}

				couponDeliveryDAO.save(tCouponDelivery);

				couponDeliveryHistoryDAO.delete(couponDeliveryHistory);
				// 使用后记录表中使用优惠券数量- 1
				TCouponActivitycoupons activitycoupons = couponActivitycouponsDAO
						.getTActivitycoupons(tCouponDelivery.getTCouponInformation().getId());
				activitycoupons.setFuseCount(activitycoupons.getFuseCount() - 1);
				couponActivitycouponsDAO.save(activitycoupons);
			}
		}
	}

	public void issueDelivery(Map<String, Object> valueMap) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Integer status = Integer.parseInt(valueMap.get("status").toString());
		long fAuditor = shiroUser.getId();
		Date now = new Date();
		String deliveryId = valueMap.get("deliveryId").toString();
		deliveryDAO.issuedeliverystatus(status, fAuditor, now, deliveryId);
	}

	@Transactional(readOnly = true)
	public void getCouponChannelList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId,t.fdeliveryId as fdeliveryId,t.fcouponName as fcouponName,t.fuseRange as fuseRange,t.forder as forder,t.fstatus as fstatus,t.fbeginTime as fbeginTime,t.fendTime as fendTime from TCouponChannel t where 1 = 1");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_ftitle") && StringUtils.isNotBlank(valueMap.get("s_ftitle").toString())) {
				hql.append(" and t.fcouponName like :s_ftitle ");
				hqlMap.put("s_ftitle", "%" + valueMap.get("s_ftitle").toString() + "%");
			}
			if (valueMap.containsKey("s_fstatus") && StringUtils.isNotBlank(valueMap.get("s_fstatus").toString())) {
				hql.append(" and t.fstatus = :s_fstatus");
				hqlMap.put("s_fstatus", Integer.valueOf(valueMap.get("s_fstatus").toString()));
			}
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.fcreateTime, t.fstatus");
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		for (Map<String, Object> amap : list) {
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("statusString", DictionaryUtil.getString(DictionaryUtil.CouponChannelStatus,
						(Integer) amap.get("fstatus"), shiroUser.getLanguage()));
			}
			if (amap.get("fbeginTime") != null && StringUtils.isNotBlank(amap.get("fbeginTime").toString())) {
				amap.put("fbeginTime", DateFormatUtils.format((Date) amap.get("fbeginTime"), "yyyy-MM-dd"));
			}
			if (amap.get("fendTime") != null && StringUtils.isNotBlank(amap.get("fendTime").toString())) {
				amap.put("fendTime", DateFormatUtils.format((Date) amap.get("fendTime"), "yyyy-MM-dd"));
			}
		}
	}

	public Map<String, Object> addCouponChannel(Map<String, Object> valueMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		TDelivery tDelivery = deliveryDAO.findOne(valueMap.get("fDeliveryId").toString());
		if (tDelivery == null) {
			returnMap.put("status", 0);
			returnMap.put("msg", "此活动不存在，请检查优惠活动id是否正确");
			return returnMap;
		}
		if (tDelivery.getFactivityType() == 10) {
			returnMap.put("status", 0);
			returnMap.put("msg", "此活动不是领券活动，请检查优惠活动id是否正确");
			return returnMap;
		}
		if (tDelivery.getFstatus() != 40) {
			returnMap.put("status", 0);
			returnMap.put("msg", "只有可领取优惠活动才能添加，请检查优惠活动id是否正确");
			return returnMap;
		}
		Date start = null;
		Date end = null;
		try {
			start = DateUtils.parseDate(valueMap.get("fstartDate").toString(), "yyyy-MM-dd");
			end = DateUtils.parseDate(valueMap.get("fendDate").toString() + " 23:59:59", "yyyy-MM-dd HH:mm:ss");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
		if (tDelivery.getFdeliveryStartTime().getTime() > start.getTime()
				|| tDelivery.getFdeliveryEndTime().getTime() < end.getTime()) {
			returnMap.put("status", 0);
			returnMap.put("msg", "请输入正确的活动时间");
			return returnMap;
		}
		Date now = new Date();
		TCouponChannel tCouponChannel = new TCouponChannel();
		tCouponChannel.setFcouponName(valueMap.get("fname").toString());
		tCouponChannel.setFdeliveryId(valueMap.get("fDeliveryId").toString());
		tCouponChannel.setForder(Integer.parseInt(valueMap.get("forder").toString()));
		tCouponChannel.setFstatus(30);
		tCouponChannel.setFsubtitle(valueMap.get("fSubtitle").toString());
		tCouponChannel.setFuseRange(valueMap.get("fUseRange").toString());
		tCouponChannel.setFbeginTime(start);
		tCouponChannel.setFendTime(end);
		tCouponChannel.setFcreateTime(now);
		tCouponChannel.setFupdateTime(now);
		couponChannelDAO.save(tCouponChannel);
		returnMap.put("status", 1);
		return returnMap;
	}

	public void updateChannelStatus(String id, Integer status) {
		couponChannelDAO.updateStatus(status, id);
	}

	@Transactional(readOnly = true)
	public TCouponChannel getCouponChannel(String id) {
		return couponChannelDAO.getOne(id);
	}

	public Map<String, Object> editCouponChannel(Map<String, Object> valueMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		TDelivery tDelivery = deliveryDAO.findOne(valueMap.get("fDeliveryId").toString());
		if (tDelivery == null) {
			returnMap.put("status", 0);
			returnMap.put("msg", "此活动不存在，请检查优惠活动id是否正确");
			return returnMap;
		}
		if (tDelivery.getFactivityType() == 10) {
			returnMap.put("status", 0);
			returnMap.put("msg", "此活动不是领券活动，请检查优惠活动id是否正确");
			return returnMap;
		}
		if (tDelivery.getFstatus() != 40) {
			returnMap.put("status", 0);
			returnMap.put("msg", "只有可领取优惠活动才能添加，请检查优惠活动id是否正确");
			return returnMap;
		}
		Date start = null;
		Date end = null;
		try {
			start = DateUtils.parseDate(valueMap.get("fstartDate").toString(), "yyyy-MM-dd");
			end = DateUtils.parseDate(valueMap.get("fendDate").toString() + " 23:59:59", "yyyy-MM-dd HH:mm:ss");
		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
		if (tDelivery.getFdeliveryStartTime().getTime() > start.getTime()
				|| tDelivery.getFdeliveryEndTime().getTime() < end.getTime()) {
			returnMap.put("status", 0);
			returnMap.put("msg", "请输入正确的活动时间");
			return returnMap;
		}
		Date now = new Date();
		TCouponChannel tCouponChannel = couponChannelDAO.findOne(valueMap.get("id").toString());
		tCouponChannel.setFcouponName(valueMap.get("fname").toString());
		tCouponChannel.setFdeliveryId(valueMap.get("fDeliveryId").toString());
		tCouponChannel.setForder(Integer.parseInt(valueMap.get("forder").toString()));
		tCouponChannel.setFstatus(30);
		tCouponChannel.setFsubtitle(valueMap.get("fSubtitle").toString());
		tCouponChannel.setFuseRange(valueMap.get("fUseRange").toString());
		tCouponChannel.setFbeginTime(start);
		tCouponChannel.setFendTime(end);
		tCouponChannel.setFcreateTime(now);
		tCouponChannel.setFupdateTime(now);
		couponChannelDAO.save(tCouponChannel);
		returnMap.put("status", 1);
		return returnMap;
	}

	public String updateChannelStatus() {
		String id = ConfigurationUtil.getPropertiesValue(ResponseConfigurationDict.RESPONSE_PROPERTIES_COUPONCHANNELID);
		return id;
	}

}