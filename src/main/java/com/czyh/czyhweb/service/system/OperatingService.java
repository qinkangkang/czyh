package com.czyh.czyhweb.service.system;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Calendar;
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

import com.czyh.czyhweb.dao.ArticleDAO;
import com.czyh.czyhweb.dao.CommentDAO;
import com.czyh.czyhweb.dao.ConsultDAO;
import com.czyh.czyhweb.dao.CouponDAO;
import com.czyh.czyhweb.dao.CouponDeliveryDAO;
import com.czyh.czyhweb.dao.CouponObjectDAO;
import com.czyh.czyhweb.dao.CustomerDAO;
import com.czyh.czyhweb.dao.EventDAO;
import com.czyh.czyhweb.dao.ImageDAO;
import com.czyh.czyhweb.dao.SponsorDAO;
import com.czyh.czyhweb.dao.TimingTaskDAO;
import com.czyh.czyhweb.entity.TArticle;
import com.czyh.czyhweb.entity.TComment;
import com.czyh.czyhweb.entity.TConsult;
import com.czyh.czyhweb.entity.TCoupon;
import com.czyh.czyhweb.entity.TCouponDelivery;
import com.czyh.czyhweb.entity.TCouponObject;
import com.czyh.czyhweb.entity.TCustomer;
import com.czyh.czyhweb.entity.TEvent;
import com.czyh.czyhweb.entity.TTimingTask;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.CommonService;
import com.czyh.czyhweb.service.wechat.WxService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.PropertiesUtil;
import com.czyh.czyhweb.util.dingTalk.DingTalkUtil;
import com.czyh.czyhweb.util.excel.ExcelTemplate;
import com.czyh.czyhweb.util.html.StaticHtmlBean;
import com.czyh.czyhweb.util.html.StaticHtmlManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 运营模块的操作业务管理类.
 * 
 * @author jinshengzhi
 */
@Component
@Transactional
public class OperatingService {

	private static final Logger logger = LoggerFactory.getLogger(OperatingService.class);

	// private static JsonMapper mapper = new JsonMapper(Include.ALWAYS);

	@Autowired
	private CommonService commonService;

	@Autowired
	private EventDAO eventDAO;

	@Autowired
	private CouponDAO couponDAO;

	@Autowired
	private CouponDeliveryDAO couponDeliveryDAO;

	@Autowired
	private CouponObjectDAO couponObjectDAO;

	@Autowired
	private ArticleDAO articleDAO;

	@Autowired
	private ConsultDAO consultDAO;

	@Autowired
	private SponsorDAO sponsorDAO;

	@Autowired
	private ImageDAO imageDAO;

	@Autowired
	private CustomerDAO customerDAO;

	@Autowired
	private TimingTaskDAO timingTaskDAO;

	@Autowired
	private CommentDAO commentDAO;

	@Autowired
	private WxService wxService;

	@Transactional(readOnly = true)
	public void getApplyList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.ftitle as ftitle, t.fcouponNum as fcouponNum, t.fdeliverStartTime as fdeliverStartTime, t.fdeliverEndTime as fdeliverEndTime, t.fuseStartTime as fuseStartTime, t.fuseEndTime as fuseEndTime, t.famount as famount, t.fdiscount as fdiscount, t.flimitation as flimitation, t.fcount as fcount, t.fsendCount as fsendCount, t.fuseType as fuseType, t.fdeliverType as fdeliverType, t.fstatus as fstatus from TCoupon t where t.fstatus < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_fcouponNum")
					&& StringUtils.isNotBlank(valueMap.get("s_fcouponNum").toString())) {
				hql.append(" and t.fcouponNum like :s_fcouponNum ");
				hqlMap.put("s_fcouponNum", "%" + valueMap.get("s_fcouponNum").toString() + "%");
			}
			if (valueMap.containsKey("s_ftitle") && StringUtils.isNotBlank(valueMap.get("s_ftitle").toString())) {
				hql.append(" and t.ftitle like :s_ftitle ");
				hqlMap.put("s_ftitle", "%" + valueMap.get("s_ftitle").toString() + "%");
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
			if (valueMap.containsKey("s_fstatus") && StringUtils.isNotBlank(valueMap.get("s_fstatus").toString())) {
				int status = Integer.valueOf(valueMap.get("s_fstatus").toString());
				hql.append(" and t.fstatus = :status");
				hqlMap.put("status", status);
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

		StringBuilder info = new StringBuilder();
		Date date = null;
		for (Map<String, Object> amap : list) {
			// <dl class="dl-horizontal">
			// <dt>...</dt>
			// <dd>...</dd>
			// </dl>
			info.delete(0, info.length());
			if (amap.get("fdeliverStartTime") != null
					&& StringUtils.isNotBlank(amap.get("fdeliverStartTime").toString())) {
				date = (Date) amap.get("fdeliverStartTime");
				info.append("<strong>发放起时间：</strong>").append(DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
			if (amap.get("fdeliverEndTime") != null && StringUtils.isNotBlank(amap.get("fdeliverEndTime").toString())) {
				date = (Date) amap.get("fdeliverEndTime");
				info.append("<br/>").append("<strong>发放止时间：</strong>")
						.append(DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
			amap.put("deliverInfo", info.toString());

			info.delete(0, info.length());
			if (amap.get("fuseStartTime") != null && StringUtils.isNotBlank(amap.get("fuseStartTime").toString())) {
				date = (Date) amap.get("fuseStartTime");
				info.append("<strong>可用起日期：</strong><br/>").append(DateFormatUtils.format(date, "yyyy-MM-dd"));
			}
			if (amap.get("fuseEndTime") != null && StringUtils.isNotBlank(amap.get("fuseEndTime").toString())) {
				date = (Date) amap.get("fuseEndTime");
				info.append("<br/>").append("<strong>可用止日期：</strong><br/>")
						.append(DateFormatUtils.format(date, "yyyy-MM-dd"));
			}
			amap.put("useInfo", info.toString());

			info.delete(0, info.length());
			if (amap.get("flimitation") != null && StringUtils.isNotBlank(amap.get("flimitation").toString())) {
				info.append("<strong>购物满：</strong>").append(amap.get("flimitation").toString()).append("元");
			}
			if (amap.get("famount") != null && StringUtils.isNotBlank(amap.get("famount").toString())) {
				info.append("<br/><strong>优惠：</strong>").append(amap.get("famount").toString()).append("元");
			}
			if (amap.get("fdiscount") != null && StringUtils.isNotBlank(amap.get("fdiscount").toString())) {
				BigDecimal discount = (BigDecimal) amap.get("fdiscount");
				discount = discount.multiply(new BigDecimal(100));
				info.append("<br/><strong>折扣：</strong>").append(discount).append("％");
			}
			amap.put("amountInfo", info.toString());

			if (amap.get("fuseType") != null && StringUtils.isNotBlank(amap.get("fuseType").toString())) {
				amap.put("fuseType", DictionaryUtil.getString(DictionaryUtil.CouponUseType,
						((Integer) amap.get("fuseType")), shiroUser.getLanguage()));
			}
			if (amap.get("fdeliverType") != null && StringUtils.isNotBlank(amap.get("fdeliverType").toString())) {
				amap.put("fdeliverType", DictionaryUtil.getString(DictionaryUtil.CouponDeliverType,
						((Integer) amap.get("fdeliverType")), shiroUser.getLanguage()));
			}
		}
	}

	@Transactional(readOnly = true)
	public TCoupon getCoupon(String couponId) {
		return couponDAO.getOne(couponId);
	}

	public String saveCoupon(Map<String, Object> valueMap) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		try {
			if (valueMap.containsKey("fdeliverStartTime")
					&& StringUtils.isNotBlank(valueMap.get("fdeliverStartTime").toString())) {
				valueMap.put("fdeliverStartTime",
						DateUtils.parseDate(valueMap.get("fdeliverStartTime").toString(), "yyyy-MM-dd HH:mm"));
			}
			if (valueMap.containsKey("fdeliverEndTime")
					&& StringUtils.isNotBlank(valueMap.get("fdeliverEndTime").toString())) {
				valueMap.put("fdeliverEndTime",
						DateUtils.parseDate(valueMap.get("fdeliverEndTime").toString(), "yyyy-MM-dd HH:mm"));
			}
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
		TCoupon tCoupon = new TCoupon();
		BeanMapper.copy(valueMap, tCoupon);
		if (valueMap.containsKey("fdiscount") && StringUtils.isNotBlank(valueMap.get("fdiscount").toString())) {
			BigDecimal fdiscount = new BigDecimal(valueMap.get("fdiscount").toString()).divide(new BigDecimal(100), 2,
					RoundingMode.HALF_UP);
			tCoupon.setFdiscount(fdiscount);
		}
		Date now = new Date();
		// tCoupon.setFstatus(10);
		// 如果是用户自助领取的优惠券，则优惠券状态为40（可领取），否则是30（审核通过状态）
		if (tCoupon.getFdeliverType().intValue() == 110
				&& StringUtils.isBlank(valueMap.get("fdeliverStartTime").toString())) {
			tCoupon.setFstatus(40);
		} else {
			tCoupon.setFstatus(30);
		}
		tCoupon.setFsendCount(0);
		tCoupon.setFoperator(shiroUser.getId());
		tCoupon.setFcreateTime(now);
		tCoupon.setFupdateTime(now);
		tCoupon = couponDAO.save(tCoupon);
		// 优惠券审核后，会添加一个优惠券领取有效定时任务
		if (tCoupon.getFdeliverStartTime() != null) {
			TTimingTask tEventTimingTask = new TTimingTask();
			tEventTimingTask.setEntityId(tCoupon.getId());
			tEventTimingTask.setTaskTime(tCoupon.getFdeliverStartTime().getTime());
			tEventTimingTask.setTaskType(10);
			timingTaskDAO.save(tEventTimingTask);
		}
		// 优惠券审核后，会添加一个优惠券领取过期定时任务
		if (tCoupon.getFdeliverEndTime() != null) {
			TTimingTask tEventTimingTask = new TTimingTask();
			tEventTimingTask.setEntityId(tCoupon.getId());
			tEventTimingTask.setTaskTime(tCoupon.getFdeliverEndTime().getTime());
			tEventTimingTask.setTaskType(11);
			timingTaskDAO.save(tEventTimingTask);
		}
		// 优惠券审核后，如果优惠券适用有效期起大于当前时间，会添加一个优惠券使用有效期开始任务定时任务
		if (DateUtils.truncatedCompareTo(now, tCoupon.getFuseStartTime(), Calendar.SECOND) < 0) {
			TTimingTask tEventTimingTask = new TTimingTask();
			tEventTimingTask.setEntityId(tCoupon.getId());
			tEventTimingTask.setTaskTime(tCoupon.getFuseStartTime().getTime());
			tEventTimingTask.setTaskType(14);
			timingTaskDAO.save(tEventTimingTask);
		}
		// 优惠券审核后，如果优惠券适用有效期大于当前时间，会添加一个优惠券使用有效期结束任务定时任务
		if (DateUtils.truncatedCompareTo(now, tCoupon.getFuseEndTime(), Calendar.SECOND) < 0) {
			TTimingTask tEventTimingTask = new TTimingTask();
			tEventTimingTask.setEntityId(tCoupon.getId());
			tEventTimingTask.setTaskTime(tCoupon.getFuseEndTime().getTime());
			tEventTimingTask.setTaskType(15);
			timingTaskDAO.save(tEventTimingTask);
		}

		TCouponObject tCouponObject = new TCouponObject();
		if (tCoupon.getFuseType().intValue() == 10) {
			// tCouponObject.setTCoupon(tCoupon);
			tCouponObject.setFuseType(tCoupon.getFuseType());
		} else if (tCoupon.getFuseType().intValue() == 30) {
			// tCouponObject.setTCoupon(tCoupon);
			tCouponObject.setFuseType(tCoupon.getFuseType());
			tCouponObject.setFobjectId(valueMap.get("ftypeA").toString());
		} else {
			// tCouponObject.setTCoupon(tCoupon);
			tCouponObject.setFuseType(tCoupon.getFuseType());
			tCouponObject.setFobjectId(valueMap.get("fentityId").toString());
		}
		couponObjectDAO.save(tCouponObject);
		return tCoupon.getId();
	}

	public void editCoupon(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		Date now = new Date();
		TCoupon db = couponDAO.getOne(valueMap.get("id").toString());

		if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
			db.setFtitle(valueMap.get("ftitle").toString());
		} else {
			db.setFtitle(null);
		}
		if (valueMap.containsKey("fcouponNum") && StringUtils.isNotBlank(valueMap.get("fcouponNum").toString())) {
			db.setFcouponNum(valueMap.get("fcouponNum").toString());
		} else {
			db.setFcouponNum(null);
		}
		if (valueMap.containsKey("fcity") && StringUtils.isNotBlank(valueMap.get("fcity").toString())) {
			db.setFcity(Integer.valueOf(valueMap.get("fcity").toString()));
		} else {
			db.setFcity(null);
		}
		if (valueMap.containsKey("fdescription") && StringUtils.isNotBlank(valueMap.get("fdescription").toString())) {
			db.setFdescription(valueMap.get("fdescription").toString());
		} else {
			db.setFdescription(null);
		}
		if (valueMap.containsKey("fnotice") && StringUtils.isNotBlank(valueMap.get("fnotice").toString())) {
			db.setFnotice(valueMap.get("fnotice").toString());
		} else {
			db.setFnotice(null);
		}
		if (valueMap.containsKey("fuseRange") && StringUtils.isNotBlank(valueMap.get("fuseRange").toString())) {
			db.setFuseRange(valueMap.get("fuseRange").toString());
		} else {
			db.setFuseRange(null);
		}
		try {
			if (valueMap.containsKey("fdeliverStartTime")
					&& StringUtils.isNotBlank(valueMap.get("fdeliverStartTime").toString())) {
				db.setFdeliverStartTime(
						DateUtils.parseDate(valueMap.get("fdeliverStartTime").toString(), "yyyy-MM-dd HH:mm"));
			} else {
				db.setFdeliverStartTime(null);
			}
			if (valueMap.containsKey("fdeliverEndTime")
					&& StringUtils.isNotBlank(valueMap.get("fdeliverEndTime").toString())) {
				db.setFdeliverEndTime(
						DateUtils.parseDate(valueMap.get("fdeliverEndTime").toString(), "yyyy-MM-dd HH:mm"));
			} else {
				db.setFdeliverEndTime(null);
			}
			if (valueMap.containsKey("fuseStartTime")
					&& StringUtils.isNotBlank(valueMap.get("fuseStartTime").toString())) {
				db.setFuseStartTime(DateUtils.parseDate(valueMap.get("fuseStartTime").toString(), "yyyy-MM-dd"));
			} else {
				db.setFuseStartTime(null);
			}
			if (valueMap.containsKey("fuseEndTime") && StringUtils.isNotBlank(valueMap.get("fuseEndTime").toString())) {
				db.setFuseEndTime(DateUtils.parseDate(valueMap.get("fuseEndTime").toString(), "yyyy-MM-dd"));
			} else {
				db.setFuseEndTime(null);
			}
		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException();
		}

		if (valueMap.containsKey("famount") && StringUtils.isNotBlank(valueMap.get("famount").toString())) {
			db.setFamount(new BigDecimal(valueMap.get("famount").toString()));
		} else {
			db.setFamount(null);
		}
		if (valueMap.containsKey("fdiscount") && StringUtils.isNotBlank(valueMap.get("fdiscount").toString())) {
			BigDecimal fdiscount = new BigDecimal(valueMap.get("fdiscount").toString()).divide(new BigDecimal(100), 2,
					RoundingMode.HALF_UP);
			db.setFdiscount(fdiscount);
		} else {
			db.setFdiscount(null);
		}
		if (valueMap.containsKey("flimitation") && StringUtils.isNotBlank(valueMap.get("flimitation").toString())) {
			db.setFlimitation(new BigDecimal(valueMap.get("flimitation").toString()));
		} else {
			db.setFlimitation(null);
		}
		if (valueMap.containsKey("fcount") && StringUtils.isNotBlank(valueMap.get("fcount").toString())) {
			db.setFcount(Integer.valueOf(valueMap.get("fcount").toString()));
		} else {
			db.setFcount(null);
		}
		if (valueMap.containsKey("fuseType") && StringUtils.isNotBlank(valueMap.get("fuseType").toString())) {
			db.setFuseType(Integer.valueOf(valueMap.get("fuseType").toString()));
		} else {
			db.setFuseType(null);
		}
		if (valueMap.containsKey("fdeliverType") && StringUtils.isNotBlank(valueMap.get("fdeliverType").toString())) {
			db.setFdeliverType(Integer.valueOf(valueMap.get("fdeliverType").toString()));
		} else {
			db.setFdeliverType(null);
		}

		db.setFupdateTime(now);
		couponDAO.save(db);
	}

	public void delCoupon(String couponId) {
		couponDAO.saveStatus(999, couponId);
	}

	public void issueCoupon(String couponId) {
		// 自动发放优惠券给全体用户
		TCoupon tCoupon = couponDAO.getOne(couponId);
		StringBuilder hql = new StringBuilder();
		hql.append("select t.id as id from TCustomer t where t.ftype = 1 and t.fstatus = 1");

		List<Map<String, Object>> list = commonService.find(hql.toString());
		List<TCouponDelivery> couponDeliveryList = Lists.newArrayList();

		Date now = new Date();
		TCouponDelivery tCouponDelivery = null;
		String customerId = null;
		for (Map<String, Object> amap : list) {
			customerId = amap.get("id").toString();
			tCouponDelivery = new TCouponDelivery();
			tCouponDelivery.setFdeliverTime(now);
			// tCouponDelivery.setFstatus(10);
			// tCouponDelivery.setTCoupon(tCoupon);
			tCouponDelivery.setTCustomer(new TCustomer(customerId));

			couponDeliveryList.add(tCouponDelivery);
		}
		couponDeliveryDAO.save(couponDeliveryList);
		couponDAO.saveSendCountAndStatus(couponDeliveryList.size(), 90, couponId);
	}

	@Transactional(readOnly = true)
	public void getArticleList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.ftitle as ftitle, t.ftype as ftype, t.forder as forder, t.frecommend as frecommend, t.fcomment as fcomment, t.fstatus as fstatus from TArticle t");
		hql.append(" where t.ftype = 1 and t.fstatus < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_fcouponNum")
					&& StringUtils.isNotBlank(valueMap.get("s_fcouponNum").toString())) {
				hql.append(" and t.fcouponNum like :s_fcouponNum ");
				hqlMap.put("s_fcouponNum", "%" + valueMap.get("s_fcouponNum").toString() + "%");
			}
			if (valueMap.containsKey("s_ftitle") && StringUtils.isNotBlank(valueMap.get("s_ftitle").toString())) {
				hql.append(" and t.ftitle like :s_ftitle ");
				hqlMap.put("s_ftitle", "%" + valueMap.get("s_ftitle").toString() + "%");
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
			if (valueMap.containsKey("s_fstatus") && StringUtils.isNotBlank(valueMap.get("s_fstatus").toString())) {
				int status = Integer.valueOf(valueMap.get("s_fstatus").toString());
				hql.append(" and t.fstatus = :status");
				hqlMap.put("status", status);
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

		// Date date = null;
		for (Map<String, Object> amap : list) {
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatusString", DictionaryUtil.getString(DictionaryUtil.ArticleStatus,
						((Integer) amap.get("fstatus")), shiroUser.getLanguage()));
			}
		}
	}

	@Transactional(readOnly = true)
	public TArticle getArticle(String articleId) {
		return articleDAO.getOne(articleId);
	}

	public String saveArticle(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		Date now = new Date();

		TArticle tArticle = new TArticle();
		BeanMapper.copy(valueMap, tArticle);
		if (tArticle.getForder() == null) {
			tArticle.setForder(0);
		}
		tArticle.setFrecommend(0L);
		tArticle.setFcomment(0L);
		tArticle.setFstatus(10);
		tArticle.setFcreateTime(now);
		tArticle.setFupdateTime(now);
		tArticle = articleDAO.save(tArticle);

		if (tArticle.getFimage() != null) {
			imageDAO.saveStatusAndEntityIdAndEntityType(tArticle.getFimage(), 2, tArticle.getId(), 8);
		}
		if (valueMap.containsKey("fartCity") && StringUtils.isNotBlank(valueMap.get("fartCity").toString())) {
			tArticle.setFartCity(valueMap.get("fartCity").toString());
		}
		if (valueMap.containsKey("fartType") && StringUtils.isNotBlank(valueMap.get("fartType").toString())) {
			tArticle.setFartType(Integer.valueOf(valueMap.get("fartType").toString()));
		}

		if (valueMap.containsKey("fdetail") && StringUtils.isNotBlank(valueMap.get("fdetail").toString())) {
			String relativePath = new StringBuilder("/article/").append(DateFormatUtils.format(now, "yyyy-MM-dd"))
					.append("/").append(tArticle.getId()).append(".html").toString();
			tArticle.setFdetailHtmlUrl(relativePath);
			// 创建异步页面静态化Bean
			StaticHtmlBean staticHtmlBean = new StaticHtmlBean();
			staticHtmlBean.setObjectId(tArticle.getId());
			staticHtmlBean.setTemplateName(Constant.EventDetilTemplate);
			staticHtmlBean.setRelativePath(relativePath);

			Map<String, Object> map = Maps.newHashMap();
			map.put("detailHtml", valueMap.get("fdetail").toString());
			staticHtmlBean.setContentMap(map);
			// 异步生成活动静态页面，不占用本线程的执行时间
			StaticHtmlManager.put(staticHtmlBean);
		}
		return tArticle.getId();
	}

	public void editArticle(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		Date now = new Date();
		TArticle db = articleDAO.getOne(valueMap.get("id").toString());

		if (valueMap.containsKey("fcityId") && StringUtils.isNotBlank(valueMap.get("fcityId").toString())) {
			db.setFcityId(Integer.valueOf(valueMap.get("fcityId").toString()));
		} else {
			db.setFcityId(null);
		}
		if (valueMap.containsKey("fimage") && StringUtils.isNotBlank(valueMap.get("fimage").toString())) {
			if (!valueMap.get("fimage").toString().equals(db.getFimage().toString())) {
				db.setFimage(Long.valueOf(valueMap.get("fimage").toString()));
				if (db.getFimage() != null) {
					imageDAO.saveStatusAndEntityIdAndEntityType(db.getFimage(), 2, db.getId(), 8);
				}
			}
		} else {
			db.setFimage(null);
		}
		if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
			db.setFtitle(valueMap.get("ftitle").toString());
		} else {
			db.setFtitle(null);
		}
		if (valueMap.containsKey("ftype") && StringUtils.isNotBlank(valueMap.get("ftype").toString())) {
			db.setFtype(1);
		}
		if (valueMap.containsKey("fbrief") && StringUtils.isNotBlank(valueMap.get("fbrief").toString())) {
			db.setFbrief(valueMap.get("fbrief").toString());
		} else {
			db.setFbrief(null);
		}
		if (valueMap.containsKey("forder") && StringUtils.isNotBlank(valueMap.get("forder").toString())) {
			db.setForder(Integer.valueOf(valueMap.get("forder").toString()));
		} else {
			db.setForder(null);
		}
		if (valueMap.containsKey("fartCity") && StringUtils.isNotBlank(valueMap.get("fartCity").toString())) {
			db.setFartCity(valueMap.get("fartCity").toString());
		} else {
			db.setFartCity(null);
		}
		if (valueMap.containsKey("fartType") && StringUtils.isNotBlank(valueMap.get("fartType").toString())) {
			db.setFartType(Integer.valueOf(valueMap.get("fartType").toString()));
		} else {
			db.setFartType(null);
		}
		if (valueMap.containsKey("fdetail") && StringUtils.isNotBlank(valueMap.get("fdetail").toString())) {
			db.setFdetail(valueMap.get("fdetail").toString());

			String relativePath = new StringBuilder("/article/").append(DateFormatUtils.format(now, "yyyy-MM-dd"))
					.append("/").append(db.getId()).append(".html").toString();
			db.setFdetailHtmlUrl(relativePath);
			// 创建异步页面静态化Bean
			StaticHtmlBean staticHtmlBean = new StaticHtmlBean();
			staticHtmlBean.setObjectId(db.getId());
			staticHtmlBean.setTemplateName(Constant.EventDetilTemplate);
			staticHtmlBean.setRelativePath(relativePath);

			Map<String, Object> map = Maps.newHashMap();
			map.put("detailHtml", valueMap.get("fdetail").toString());
			staticHtmlBean.setContentMap(map);
			// 异步生成活动静态页面，不占用本线程的执行时间
			StaticHtmlManager.put(staticHtmlBean);
		} else {
			db.setFdetail(null);
		}

		db.setFupdateTime(now);
		articleDAO.save(db);
	}

	public void delArticle(String articleId) {
		articleDAO.saveUpdateTimeAndStatus(999, articleId);
	}

	public void onSaleArticle(String articleId) {
		articleDAO.saveUpdateTimeAndStatus(20, articleId);
	}

	public void offSaleArticle(String articleId) {
		articleDAO.saveUpdateTimeAndStatus(90, articleId);

		TTimingTask tEventTimingTask = timingTaskDAO.getByEntityIdAndTaskType(articleId, 12);
		if (tEventTimingTask == null) {
			tEventTimingTask = new TTimingTask();
			tEventTimingTask.setEntityId(articleId);
			tEventTimingTask.setTaskType(12);
		}
		tEventTimingTask.setTaskTime(new Date().getTime());
		timingTaskDAO.save(tEventTimingTask);
	}

	@Transactional(readOnly = true)
	public void getConsultList(Map<String, Object> valueMap, CommonPage page) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, e.ftitle as eventInfo, s.fname as fsponsorName, s.fphone as fsponsorPhone, t.fobjectId as fobjectId, t.fcustomerName as fcustomerName, t.fcreateTime as fcreateTime, t.fcontent as fcontent, t.fuserName as fuserName, t.freplyTime as freplyTime,t.fstatus as fstatus from TConsult t inner join TEvent e on t.fobjectId = e.id left join TSponsor s on e.TSponsor.id = s.id where t.ftype = :type");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("type", 1);
		try {
			if (valueMap.containsKey("s_fcustomerName")
					&& StringUtils.isNotBlank(valueMap.get("s_fcustomerName").toString())) {
				hql.append(" and t.fcustomerName like :s_fcustomerName ");
				hqlMap.put("s_fcustomerName", "%" + valueMap.get("s_fcustomerName").toString() + "%");
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
			if (valueMap.containsKey("s_fstatus") && StringUtils.isNotBlank(valueMap.get("s_fstatus").toString())) {
				int status = Integer.valueOf(valueMap.get("s_fstatus").toString());
				hql.append(" and t.fstatus = :status");
				hqlMap.put("status", status);
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
		StringBuilder info = new StringBuilder();
		for (Map<String, Object> amap : list) {
			info.delete(0, info.length());
			if (amap.get("fsponsorName") != null && StringUtils.isNotBlank(amap.get("fsponsorName").toString())) {
				info.append(amap.get("fsponsorName").toString());
			}
			if (amap.get("fsponsorPhone") != null && StringUtils.isNotBlank(amap.get("fsponsorPhone").toString())) {
				info.append("<br/>").append(amap.get("fsponsorPhone").toString());
			}
			amap.put("sponsorInfo", info.toString());
			if (amap.get("fcreateTime") != null && StringUtils.isNotBlank(amap.get("fcreateTime").toString())) {
				date = (Date) amap.get("fcreateTime");
				amap.put("fcreateTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
			if (amap.get("freplyTime") != null && StringUtils.isNotBlank(amap.get("freplyTime").toString())) {
				date = (Date) amap.get("freplyTime");
				amap.put("freplyTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
		}
	}

	@Transactional(readOnly = true)
	public void createConsultExcel(Map<String, Object> valueMap, String datePath, String excelFileName) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, e.ftitle as ftitle, s.fname as fsponsorName, s.fphone as fsponsorPhone, t.fobjectId as fobjectId, t.fcustomerName as fcustomerName,c.fphone as fphone, t.fcreateTime as fcreateTime, t.fcontent as fcontent, t.fuserName as fuserName, t.freplyTime as freplyTime, t.freply as freply from TConsult t inner join TEvent e on t.fobjectId = e.id left join TSponsor s on e.TSponsor.id = s.id inner join TCustomer c on c.id = t.fcustomerId where t.ftype = :type");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("type", 1);
		try {
			if (valueMap.containsKey("s_fcustomerName")
					&& StringUtils.isNotBlank(valueMap.get("s_fcustomerName").toString())) {
				hql.append(" and t.fcustomerName like :s_fcustomerName ");
				hqlMap.put("s_fcustomerName", "%" + valueMap.get("s_fcustomerName").toString() + "%");
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
			if (valueMap.containsKey("s_fstatus") && StringUtils.isNotBlank(valueMap.get("s_fstatus").toString())) {
				int status = Integer.valueOf(valueMap.get("s_fstatus").toString());
				hql.append(" and t.fstatus = :status");
				hqlMap.put("status", status);
			}

			hql.append(" order by t.fcreateTime desc");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}

		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		ExcelTemplate excel = ExcelTemplate.getInstance().readTemplateClassPath("/template/excel/consultTemp.xlsx");

		// 定义全路径，为了以后将相对路径和文件名添加进去
		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("excelConsultPath")).append("/").append(datePath).append("/");

		for (Map<String, Object> amap : list) {
			excel.creatNewRow();

			if (amap.get("ftitle") != null && StringUtils.isNotBlank(amap.get("ftitle").toString())) {
				excel.createNewCol(amap.get("ftitle").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fsponsorName") != null && StringUtils.isNotBlank(amap.get("fsponsorName").toString())) {
				excel.createNewCol(amap.get("fsponsorName").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fsponsorPhone") != null && StringUtils.isNotBlank(amap.get("fsponsorPhone").toString())) {
				excel.createNewCol(amap.get("fsponsorPhone").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fcustomerName") != null && StringUtils.isNotBlank(amap.get("fcustomerName").toString())) {
				excel.createNewCol(amap.get("fcustomerName").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fphone") != null && StringUtils.isNotBlank(amap.get("fphone").toString())) {
				excel.createNewCol(amap.get("fphone").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fcreateTime") != null && StringUtils.isNotBlank(amap.get("fcreateTime").toString())) {
				excel.createNewCol(DateFormatUtils.format((Date) amap.get("fcreateTime"), "yyyy-MM-dd HH:mm"));
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fcontent") != null && StringUtils.isNotBlank(amap.get("fcontent").toString())) {
				excel.createNewCol(amap.get("fcontent").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fuserName") != null && StringUtils.isNotBlank(amap.get("fuserName").toString())) {
				excel.createNewCol(amap.get("fuserName").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("freplyTime") != null && StringUtils.isNotBlank(amap.get("freplyTime").toString())) {
				excel.createNewCol(DateFormatUtils.format((Date) amap.get("freplyTime"), "yyyy-MM-dd HH:mm"));
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("freply") != null && StringUtils.isNotBlank(amap.get("freply").toString())) {
				excel.createNewCol(amap.get("freply").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}

		}
		excel.insertSer();
		File destDir = new File(rootPath.toString());
		if (!destDir.exists()) {
			destDir.mkdirs();
		}

		String newFile = rootPath.append(excelFileName).append(".xlsx").toString();
		excel.writeToFile(newFile);
	}

	@Transactional(readOnly = true)
	public TConsult getConsult(String consultId) {
		return consultDAO.getOne(consultId);
	}

	public void saveConsult(Map<String, Object> valueMap) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Date now = new Date();
		TConsult db = consultDAO.getOne(valueMap.get("id").toString());
		db.setFuserId(shiroUser.getId());
		db.setFuserName(shiroUser.getName());
		db.setFreplyTime(now);
		db.setFstatus(20);

		if (valueMap.containsKey("freply") && StringUtils.isNotBlank(valueMap.get("freply").toString())) {
			db.setFreply(valueMap.get("freply").toString());
		} else {
			db.setFreply(null);
		}
		consultDAO.save(db);

		// 同步发送钉钉通知
		try {
			TEvent event = eventDAO.getOne(db.getFobjectId());
			if (StringUtils.isNotBlank(DingTalkUtil.getReplyConsoleDingTalk())) {
				String msg = new StringBuilder().append("客服回复用户咨询提醒：客服[").append(shiroUser.getName()).append("]回复了用户[")
						.append(db.getFcustomerName()).append("]的咨询。活动名称【").append(event.getFtitle()).append("】咨询内容【")
						.append(db.getFcontent()).append("】回复内容【").append(db.getFreply()).append("】").toString();
				DingTalkUtil.sendDingTalk(msg, DingTalkUtil.getReplyConsoleDingTalk());
			}

			// 同步发送微信push
			TCustomer tCustomer = customerDAO.getOne(db.getFcustomerId());
			wxService.pushConsultReplyMsg(event, tCustomer, db);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			logger.error("客服回复用户咨询时调用钉钉通知时，钉钉通知接口出错。");
			logger.error("微信push消息出错。");
		}
	}

	public void saveComment(Map<String, Object> valueMap) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Date now = new Date();
		TComment db = commentDAO.getOne(valueMap.get("id").toString());
		db.setFuserId(shiroUser.getId());
		db.setFuserName(shiroUser.getName());
		db.setFreplyTime(now);
		db.setFstatus(40);

		if (valueMap.containsKey("freply") && StringUtils.isNotBlank(valueMap.get("freply").toString())) {
			db.setFreply(valueMap.get("freply").toString());
		} else {
			db.setFreply(null);
		}
		commentDAO.save(db);

		TEvent event = eventDAO.getOne(db.getFobjectId());
		TCustomer tCustomer = customerDAO.getOne(db.getFcustomerId());
		// 同步发送微信通知
		wxService.pushCommentReplyMsg(event, tCustomer, db);
	}

	public void delConsult(String id) {
		consultDAO.updateStatusConsult(999, id);
	}

	public void sendDingdingForConsult() {
		Long count = consultDAO.getCountByStatus(10);
		if (count.intValue() > 0) {
			String msg = new StringBuilder().append("截止目前，系统中还有").append(count).append("条活动的咨询未回复，辛苦尽快去处理。").toString();
			DingTalkUtil.sendDingTalk(msg, DingTalkUtil.getConsultSyncingDingTalk());
		}
	}

	public void restoreConsult(String id) {
		TConsult tConsult = consultDAO.findOne(id);
		if (StringUtils.isNotBlank(tConsult.getFreply())) {
			consultDAO.updateStatusConsult(20, id);
		} else {
			consultDAO.updateStatusConsult(10, id);
		}
	}

}