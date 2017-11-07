package com.czyh.czyhweb.service.sdeals;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;

import com.czyh.czyhweb.dao.CustomerBonusDAO;
import com.czyh.czyhweb.dao.CustomerDAO;
import com.czyh.czyhweb.dao.CustomerInfoDAO;
import com.czyh.czyhweb.dao.CustomerLevelDAO;
import com.czyh.czyhweb.dao.EventBonusDAO;
import com.czyh.czyhweb.dao.ImageDAO;
import com.czyh.czyhweb.dao.IssusBonusDAO;
import com.czyh.czyhweb.dao.OrderBonusDAO;
import com.czyh.czyhweb.dao.PosterDAO;
import com.czyh.czyhweb.dao.WxPayDAO;
import com.czyh.czyhweb.entity.TCustomer;
import com.czyh.czyhweb.entity.TCustomerBonus;
import com.czyh.czyhweb.entity.TCustomerInfo;
import com.czyh.czyhweb.entity.TCustomerLevel;
import com.czyh.czyhweb.entity.TEvent;
import com.czyh.czyhweb.entity.TEventBonus;
import com.czyh.czyhweb.entity.TIssueBonus;
import com.czyh.czyhweb.entity.TOrderBonus;
import com.czyh.czyhweb.entity.TPoster;
import com.czyh.czyhweb.entity.TWxPay;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.CommonService;
import com.czyh.czyhweb.service.wechat.WxService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.PropertiesUtil;
import com.czyh.czyhweb.util.excel.ExcelTemplate;
import com.czyh.czyhweb.util.wx.WxPayUtil;
import com.czyh.czyhweb.util.wx.WxRefundResult;
import com.google.common.collect.Maps;

/**
 * 积分系统
 * 
 * @author jinshengzhi
 *
 */
@Component
@Transactional
public class BonusService {

	private static final Logger logger = LoggerFactory.getLogger(BonusService.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private CommonService commonService;

	@Autowired
	private EventBonusDAO eventBonusDAO;

	@Autowired
	private IssusBonusDAO issusBonusDAO;

	@Autowired
	private PosterDAO posterDAO;

	@Autowired
	private ImageDAO imageDAO;

	@Autowired
	private OrderBonusDAO orderBonusDAO;

	@Autowired
	private CustomerBonusDAO customerBonusDAO;

	@Autowired
	private CustomerInfoDAO customerInfoDAO;

	@Autowired
	private CustomerDAO customerDAO;

	@Autowired
	private WxService wxService;

	@Autowired
	private WxPayDAO wxPayDAO;

	@Autowired
	private CustomerLevelDAO customerLevelDAO;

	@Transactional(readOnly = true)
	public void getEventBonusList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId,t.TEvent.ftitle as ftitle,t.TEvent.id as eventId,t.fstartDate as fstartDate,t.fendDate as fendDate,t.fstorage as fstorage,t.fstock as fstock,t.fusePerson as fusePerson,t.fstatus as fstatus,t.fbonus as fbonus from TEventBonus t where t.fstatus<999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_ftitle") && StringUtils.isNotBlank(valueMap.get("s_ftitle").toString())) {
				hql.append(" and t.TEvent.ftitle like :s_ftitle ");
				hqlMap.put("s_ftitle", "%" + valueMap.get("s_ftitle").toString() + "%");
			}
			if (valueMap.containsKey("s_status") && StringUtils.isNotBlank(valueMap.get("s_status").toString())) {
				hql.append(" and t.fstatus = :s_status ");
				hqlMap.put("s_status", Integer.valueOf(valueMap.get("s_status").toString()));
			}
			if (valueMap.containsKey("fcreateTimeStart")
					&& StringUtils.isNotBlank(valueMap.get("fcreateTimeStart").toString())) {
				hql.append(" and t.fstartDate >= :fcreateTimeStart ");
				hqlMap.put("fcreateTimeStart",
						DateUtils.parseDate(valueMap.get("fcreateTimeStart").toString(), "yyyy-MM-dd"));
			}
			if (valueMap.containsKey("fcreateTimeEnd")
					&& StringUtils.isNotBlank(valueMap.get("fcreateTimeEnd").toString())) {
				hql.append(" and t.fendDate < :fcreateTimeEnd ");
				// 选择的截止日期加一天，保证了查询条件的完整性，解决了时分秒的问题。
				hqlMap.put("fcreateTimeEnd", DateUtils
						.addDays(DateUtils.parseDate(valueMap.get("fcreateTimeEnd").toString(), "yyyy-MM-dd"), 1));
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
			if (amap.get("fstartDate") != null && StringUtils.isNotBlank(amap.get("fstartDate").toString())) {
				date = (Date) amap.get("fstartDate");
				amap.put("fstartDate", DateFormatUtils.format(date, "yyyy-MM-dd"));
			}
			if (amap.get("fendDate") != null && StringUtils.isNotBlank(amap.get("fendDate").toString())) {
				date = (Date) amap.get("fendDate");
				amap.put("fendDate", DateFormatUtils.format(date, "yyyy-MM-dd"));
			}

			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatusString", DictionaryUtil.getString(DictionaryUtil.BonusStatus,
						(Integer) amap.get("fstatus"), shiroUser.getLanguage()));
			}

		}
	}

	public void delEventBonus(String fID) {
		TEventBonus tEventBonus = eventBonusDAO.getOne(fID);
		tEventBonus.setFstatus(999);
		tEventBonus.setFcreateTime(new Date());
		eventBonusDAO.save(tEventBonus);
		// timingTaskDAO.clearTimeTaskByEntityId(tEventBonus.getTEvent().getId());
	}

	public void onAndOffBonus(String fID, Integer Status) {
		eventBonusDAO.saveStatusBonus(Status, fID);
		// 定时任务
		/*
		 * TTimingTask timetask =
		 * timingTaskDAO.getByEntityIdAndTaskType(tEvent.getId(), 2); if
		 * (timetask == null) { TTimingTask timingTask = new TTimingTask();
		 * timingTask.setEntityId(tEvent.getId());
		 * timingTask.setTaskTime(tEvent.getFoffSaleTime().getTime());
		 * timingTask.setTaskType(2); timingTaskDAO.save(timingTask); } else {
		 * timingTaskDAO.saveTaskTime(tEvent.getFoffSaleTime().getTime(),
		 * tEvent.getId(), 2); }
		 */

	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getEventBonusList() {
		return commonService.find(
				"select t.id as key, t.ftitle as value from TEvent t where t.fsdealsModel = 2 and t.fstatus < 999");
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getDeliveryList() {
		return commonService.find(
				"select t.id as key, t.ftitle as value from TDelivery t where t.fstatus = 40 and t.freciveChannel = 30 and t.fdeliveryStartTime < now() and t.fdeliveryEndTime > now()");
	}

	public void addBonus(Map<String, Object> valueMap) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		TEventBonus tEventBonus = new TEventBonus();
		if (valueMap.containsKey("eventId") && StringUtils.isNotBlank(valueMap.get("eventId").toString())) {
			tEventBonus.setTEvent(new TEvent(valueMap.get("eventId").toString()));
		}
		if (valueMap.containsKey("deliveyId") && StringUtils.isNotBlank(valueMap.get("deliveyId").toString())) {
			tEventBonus.setFdeliveryId(valueMap.get("deliveyId").toString());
		}
		if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
			tEventBonus.setFtitle(valueMap.get("ftitle").toString());
		}
		if (valueMap.containsKey("fcustomerLevel")
				&& StringUtils.isNotBlank(valueMap.get("fcustomerLevel").toString())) {
			tEventBonus.setFlevel(Integer.valueOf(valueMap.get("fcustomerLevel").toString()));
		}
		if (valueMap.containsKey("fprompt") && StringUtils.isNotBlank(valueMap.get("fprompt").toString())) {
			tEventBonus.setFprompt(valueMap.get("fprompt").toString());
		}

		try {
			if (valueMap.containsKey("fstartDate") && StringUtils.isNotBlank(valueMap.get("fstartDate").toString())) {
				tEventBonus.setFstartDate(DateUtils.parseDate(valueMap.get("fstartDate").toString(), "yyyy-MM-dd"));
			}
			if (valueMap.containsKey("fendDate") && StringUtils.isNotBlank(valueMap.get("fendDate").toString())) {
				tEventBonus.setFendDate(DateUtils.parseDate(valueMap.get("fendDate").toString(), "yyyy-MM-dd"));
			}
			if (valueMap.containsKey("fuseDate") && StringUtils.isNotBlank(valueMap.get("fuseDate").toString())) {
				tEventBonus.setFuseDate(DateUtils.parseDate(valueMap.get("fuseDate").toString(), "yyyy-MM-dd HH:mm"));
			}
		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
		if (valueMap.containsKey("fprice") && StringUtils.isNotBlank(valueMap.get("fprice").toString())) {
			tEventBonus.setFprice(new BigDecimal(valueMap.get("fprice").toString()));
		}
		if (valueMap.containsKey("fbonus") && StringUtils.isNotBlank(valueMap.get("fbonus").toString())) {
			tEventBonus.setFbonus(Integer.valueOf(valueMap.get("fbonus").toString()));
		}
		if (valueMap.containsKey("fusePerson") && StringUtils.isNotBlank(valueMap.get("fusePerson").toString())) {
			tEventBonus.setFusePerson(valueMap.get("fusePerson").toString());
		}
		if (valueMap.containsKey("fstorage") && StringUtils.isNotBlank(valueMap.get("fstorage").toString())) {
			tEventBonus.setFstorage(Integer.valueOf(valueMap.get("fstorage").toString()));
			tEventBonus.setFstock(Integer.valueOf(valueMap.get("fstorage").toString()));
		}
		if (valueMap.containsKey("funit") && StringUtils.isNotBlank(valueMap.get("funit").toString())) {
			tEventBonus.setFunit(valueMap.get("funit").toString());
		}
		if (valueMap.containsKey("faddress") && StringUtils.isNotBlank(valueMap.get("faddress").toString())) {
			tEventBonus.setFaddress(valueMap.get("faddress").toString());
		}
		if (valueMap.containsKey("fuseType") && StringUtils.isNotBlank(valueMap.get("fuseType").toString())) {
			tEventBonus.setFuseType(valueMap.get("fuseType").toString());
		}
		if (valueMap.containsKey("fuseNote") && StringUtils.isNotBlank(valueMap.get("fuseNote").toString())) {
			tEventBonus.setFuseNote(valueMap.get("fuseNote").toString());
		}
		if (valueMap.containsKey("ftype") && StringUtils.isNotBlank(valueMap.get("ftype").toString())) {
			tEventBonus.setFtype(Integer.valueOf(valueMap.get("ftype").toString()));
		}
		if (valueMap.containsKey("flimitation") && StringUtils.isNotBlank(valueMap.get("flimitation").toString())) {
			tEventBonus.setFlimitation(Integer.valueOf(valueMap.get("flimitation").toString()));
		}
		if (valueMap.containsKey("fdeal") && StringUtils.isNotBlank(valueMap.get("fdeal").toString())) {
			tEventBonus.setFdeal(new BigDecimal(valueMap.get("fdeal").toString()));
		}
		if (valueMap.containsKey("forder") && StringUtils.isNotBlank(valueMap.get("forder").toString())) {
			tEventBonus.setForder(Integer.valueOf(valueMap.get("forder").toString()));
		} else {
			tEventBonus.setForder(0);
		}

		tEventBonus.setFcreaterId(shiroUser.getId());
		tEventBonus.setFstatus(30);
		tEventBonus.setFcreateTime(new Date());
		eventBonusDAO.save(tEventBonus);
	}

	@Transactional(readOnly = true)
	public TEventBonus getTEventBonus(String Id) {
		return eventBonusDAO.getOne(Id);
	}

	public void editBonus(Map<String, Object> valueMap) {

		TEventBonus tEventBonus = eventBonusDAO.getOne(valueMap.get("id").toString());

		if (valueMap.containsKey("eventId") && StringUtils.isNotBlank(valueMap.get("eventId").toString())) {
			tEventBonus.setTEvent(new TEvent(valueMap.get("eventId").toString()));
		}
		if (valueMap.containsKey("fprompt") && StringUtils.isNotBlank(valueMap.get("fprompt").toString())) {
			tEventBonus.setFprompt(valueMap.get("fprompt").toString());
		}
		if (valueMap.containsKey("fcustomerLevel")
				&& StringUtils.isNotBlank(valueMap.get("fcustomerLevel").toString())) {
			tEventBonus.setFlevel(Integer.valueOf(valueMap.get("fcustomerLevel").toString()));
		}
		if (valueMap.containsKey("deliveyId") && StringUtils.isNotBlank(valueMap.get("deliveyId").toString())) {
			tEventBonus.setFdeliveryId(valueMap.get("deliveyId").toString());
		}

		if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
			tEventBonus.setFtitle(valueMap.get("ftitle").toString());
		}
		try {
			if (valueMap.containsKey("fstartDate") && StringUtils.isNotBlank(valueMap.get("fstartDate").toString())) {
				tEventBonus.setFstartDate(DateUtils.parseDate(valueMap.get("fstartDate").toString(), "yyyy-MM-dd"));
			} else {
				tEventBonus.setFstartDate(null);
			}
			if (valueMap.containsKey("fendDate") && StringUtils.isNotBlank(valueMap.get("fendDate").toString())) {
				tEventBonus.setFendDate(DateUtils.parseDate(valueMap.get("fendDate").toString(), "yyyy-MM-dd"));
			} else {
				tEventBonus.setFendDate(null);
			}
			if (valueMap.containsKey("fuseDate") && StringUtils.isNotBlank(valueMap.get("fuseDate").toString())) {
				tEventBonus.setFuseDate(DateUtils.parseDate(valueMap.get("fuseDate").toString(), "yyyy-MM-dd HH:mm"));
			} else {
				tEventBonus.setFuseDate(null);
			}
		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
		if (valueMap.containsKey("fprice") && StringUtils.isNotBlank(valueMap.get("fprice").toString())) {
			tEventBonus.setFprice(new BigDecimal(valueMap.get("fprice").toString()));
		} else {
			tEventBonus.setFprice(null);
		}
		if (valueMap.containsKey("fbonus") && StringUtils.isNotBlank(valueMap.get("fbonus").toString())) {
			tEventBonus.setFbonus(Integer.valueOf(valueMap.get("fbonus").toString()));
		} else {
			tEventBonus.setFbonus(null);
		}
		if (valueMap.containsKey("fusePerson") && StringUtils.isNotBlank(valueMap.get("fusePerson").toString())) {
			tEventBonus.setFusePerson(valueMap.get("fusePerson").toString());
		} else {
			tEventBonus.setFusePerson(null);
		}
		if (valueMap.containsKey("fstorage") && StringUtils.isNotBlank(valueMap.get("fstorage").toString())) {

			tEventBonus.setFstock(tEventBonus.getFstock()
					+ (Integer.valueOf(valueMap.get("fstorage").toString()).intValue() - tEventBonus.getFstorage()));
			tEventBonus.setFstorage(Integer.valueOf(valueMap.get("fstorage").toString()));
		} else {
			tEventBonus.setFstorage(null);
		}
		if (valueMap.containsKey("funit") && StringUtils.isNotBlank(valueMap.get("funit").toString())) {
			tEventBonus.setFunit(valueMap.get("funit").toString());
		} else {
			tEventBonus.setFunit(null);
		}
		if (valueMap.containsKey("faddress") && StringUtils.isNotBlank(valueMap.get("faddress").toString())) {
			tEventBonus.setFaddress(valueMap.get("faddress").toString());
		} else {
			tEventBonus.setFaddress(null);
		}
		if (valueMap.containsKey("fuseType") && StringUtils.isNotBlank(valueMap.get("fuseType").toString())) {
			tEventBonus.setFuseType(valueMap.get("fuseType").toString());
		} else {
			tEventBonus.setFuseType(null);
		}
		if (valueMap.containsKey("fuseNote") && StringUtils.isNotBlank(valueMap.get("fuseNote").toString())) {
			tEventBonus.setFuseNote(valueMap.get("fuseNote").toString());
		} else {
			tEventBonus.setFuseNote(null);
		}
		if (valueMap.containsKey("ftype") && StringUtils.isNotBlank(valueMap.get("ftype").toString())) {
			tEventBonus.setFtype(Integer.valueOf(valueMap.get("ftype").toString()));
		} else {
			tEventBonus.setFtype(null);
		}
		if (valueMap.containsKey("flimitation") && StringUtils.isNotBlank(valueMap.get("flimitation").toString())) {
			tEventBonus.setFlimitation(Integer.valueOf(valueMap.get("flimitation").toString()));
		} else {
			tEventBonus.setFlimitation(null);
		}
		if (valueMap.containsKey("fdeal") && StringUtils.isNotBlank(valueMap.get("fdeal").toString())) {
			tEventBonus.setFdeal(new BigDecimal(valueMap.get("fdeal").toString()));
		} else {
			tEventBonus.setFdeal(null);
		}
		if (valueMap.containsKey("forder") && StringUtils.isNotBlank(valueMap.get("forder").toString())) {
			tEventBonus.setForder(Integer.valueOf(valueMap.get("forder").toString()));
		} else {
			tEventBonus.setForder(0);
		}

		eventBonusDAO.save(tEventBonus);
	}

	@Transactional(readOnly = true)
	public void getPosterList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.ftitle as ftitle, t.fsubTitle as fsubTitle, t.fstartTime as fstartTime, t.fendTime as fendTime,t.fdescription as fdescription,t.fremark as fremark,t.fstatus as fstatus,t.fcreaterId as fcreaterId from TPoster t where t.fstatus<999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {

			if (valueMap.containsKey("s_ftitle") && StringUtils.isNotBlank(valueMap.get("s_ftitle").toString())) {
				hql.append(" and t.ftitle like :s_ftitle ");
				hqlMap.put("s_ftitle", "%" + valueMap.get("s_ftitle").toString() + "%");
			}
			if (valueMap.containsKey("s_status") && StringUtils.isNotBlank(valueMap.get("s_status").toString())) {
				hql.append(" and t.fstatus = :s_status ");
				hqlMap.put("s_status", Integer.valueOf(valueMap.get("s_status").toString()));
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

			if (amap.get("fstartTime") != null && StringUtils.isNotBlank(amap.get("fstartTime").toString())) {
				date = (Date) amap.get("fstartTime");
				amap.put("fstartTime", DateFormatUtils.format(date, "yyyy-MM-dd"));
			}
			if (amap.get("fendTime") != null && StringUtils.isNotBlank(amap.get("fendTime").toString())) {
				date = (Date) amap.get("fendTime");
				amap.put("fendTime", DateFormatUtils.format(date, "yyyy-MM-dd"));
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatusString", DictionaryUtil.getString(DictionaryUtil.PosterStatus,
						(Integer) amap.get("fstatus"), shiroUser.getLanguage()));
			}
		}
	}

	public void savePoster(Map<String, Object> valueMap) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		TPoster tPoster = new TPoster();
		if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
			tPoster.setFtitle(valueMap.get("ftitle").toString());
		}
		if (valueMap.containsKey("fremark") && StringUtils.isNotBlank(valueMap.get("fremark").toString())) {
			tPoster.setFremark(valueMap.get("fremark").toString());
		}
		if (valueMap.containsKey("fimage") && StringUtils.isNotBlank(valueMap.get("fimage").toString())) {
			tPoster.setFimage(valueMap.get("fimage").toString());
		}
		if (valueMap.containsKey("fimageWidth") && StringUtils.isNotBlank(valueMap.get("fimageWidth").toString())) {
			tPoster.setFimageWidth(Integer.valueOf(valueMap.get("fimageWidth").toString()));
		}
		if (valueMap.containsKey("fimageHeight") && StringUtils.isNotBlank(valueMap.get("fimageHeight").toString())) {
			tPoster.setFimageHeight(Integer.valueOf(valueMap.get("fimageHeight").toString()));
		}
		if (valueMap.containsKey("fwaterMark") && StringUtils.isNotBlank(valueMap.get("fwaterMark").toString())) {
			tPoster.setFwaterMark(Integer.valueOf(valueMap.get("fwaterMark").toString()));
		}
		if (valueMap.containsKey("fqrcodeWh") && StringUtils.isNotBlank(valueMap.get("fqrcodeWh").toString())) {
			tPoster.setFqrcodeWh(valueMap.get("fqrcodeWh").toString());
		}

		if (valueMap.containsKey("fqrcodeX") && StringUtils.isNotBlank(valueMap.get("fqrcodeX").toString())) {
			StringBuilder fqxy = new StringBuilder();
			fqxy.append(valueMap.get("fqrcodeX").toString()).append(";").append(valueMap.get("fqrcodeY").toString());
			tPoster.setFqrcodeXy(fqxy.toString());
		}
		// if (valueMap.containsKey("fqrcodeXy") &&
		// StringUtils.isNotBlank(valueMap.get("fqrcodeXy").toString())) {
		// String[] ids = ArrayUtils.EMPTY_STRING_ARRAY;
		// if (valueMap.containsKey("fqrcodeXy") &&
		// ObjectUtils.isArray(valueMap.get("fqrcodeXy"))) {
		// ids = (String[]) valueMap.get("fqrcodeXy");
		// } else {
		// ids = ArrayUtils.toArray((valueMap.get("fqrcodeXy").toString()));
		// }
		// String tagInfo = ArrayStringUtils.arrayToString(ids,
		// ArrayStringUtils.Separator);
		// tPoster.setFqrcodeXy(tagInfo);
		// }
		try {
			if (valueMap.containsKey("fstartTime") && StringUtils.isNotBlank(valueMap.get("fstartTime").toString())) {
				tPoster.setFstartTime(DateUtils.parseDate(valueMap.get("fstartTime").toString(), "yyyy-MM-dd"));
			}

			if (valueMap.containsKey("fendTime") && StringUtils.isNotBlank(valueMap.get("fendTime").toString())) {
				tPoster.setFendTime(DateUtils.parseDate(valueMap.get("fendTime").toString(), "yyyy-MM-dd"));
			}

		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException();
		}

		tPoster.setFstatus(10);
		tPoster.setFcreateTime(new Date());
		tPoster.setFcreaterId(shiroUser.getId());

		tPoster = posterDAO.save(tPoster);
		if (StringUtils.isNotBlank(tPoster.getFimage())) {
			imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(tPoster.getFimage()), 2, tPoster.getId(), 7);
		}

	}

	@Transactional(readOnly = true)
	public TPoster getPoster(String posterId) {
		return posterDAO.getOne(posterId);
	}

	public void editorPoster(Map<String, Object> valueMap) {
		String id = valueMap.get("id").toString();
		TPoster db = posterDAO.getOne(id);

		if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
			db.setFtitle(valueMap.get("ftitle").toString());
		} else {
			db.setFtitle(null);
		}
		if (valueMap.containsKey("fremark") && StringUtils.isNotBlank(valueMap.get("fremark").toString())) {
			db.setFremark(valueMap.get("fremark").toString());
		} else {
			db.setFremark(null);
		}
		if (valueMap.containsKey("fimage") && StringUtils.isNotBlank(valueMap.get("fimage").toString())) {
			db.setFimage(valueMap.get("fimage").toString());
		} else {
			db.setFimage(null);
		}
		if (valueMap.containsKey("fimageWidth") && StringUtils.isNotBlank(valueMap.get("fimageWidth").toString())) {
			db.setFimageWidth(Integer.valueOf(valueMap.get("fimageWidth").toString()));
		} else {
			db.setFimageWidth(null);
		}
		if (valueMap.containsKey("fimageHeight") && StringUtils.isNotBlank(valueMap.get("fimageHeight").toString())) {
			db.setFimageHeight(Integer.valueOf(valueMap.get("fimageHeight").toString()));
		} else {
			db.setFimageHeight(null);
		}
		if (valueMap.containsKey("fwaterMark") && StringUtils.isNotBlank(valueMap.get("fwaterMark").toString())) {
			db.setFwaterMark(Integer.valueOf(valueMap.get("fwaterMark").toString()));
		} else {
			db.setFwaterMark(null);
		}
		if (valueMap.containsKey("fqrcodeWh") && StringUtils.isNotBlank(valueMap.get("fqrcodeWh").toString())) {
			db.setFqrcodeWh(valueMap.get("fqrcodeWh").toString());
		} else {
			db.setFqrcodeWh(null);
		}
		if (valueMap.containsKey("fqrcodeX") && StringUtils.isNotBlank(valueMap.get("fqrcodeX").toString())) {
			StringBuilder fqxy = new StringBuilder();
			fqxy.append(valueMap.get("fqrcodeX").toString()).append(";").append(valueMap.get("fqrcodeY").toString());
			db.setFqrcodeXy(fqxy.toString());
		} else {
			db.setFqrcodeXy(null);
		}
		// if (valueMap.containsKey("fqrcodeXy") &&
		// StringUtils.isNotBlank(valueMap.get("fqrcodeXy").toString())) {
		// String[] ids = ArrayUtils.EMPTY_STRING_ARRAY;
		// if (valueMap.containsKey("fqrcodeXy") &&
		// ObjectUtils.isArray(valueMap.get("fqrcodeXy"))) {
		// ids = (String[]) valueMap.get("fqrcodeXy");
		// } else {
		// ids = ArrayUtils.toArray((valueMap.get("fqrcodeXy").toString()));
		// }
		// String tagInfo = ArrayStringUtils.arrayToString(ids,
		// ArrayStringUtils.Separator);
		// db.setFqrcodeXy(tagInfo);
		// } else {
		// db.setFqrcodeXy(null);
		// }

		try {
			if (valueMap.containsKey("fstartTime") && StringUtils.isNotBlank(valueMap.get("fstartTime").toString())) {
				db.setFstartTime(DateUtils.parseDate(valueMap.get("fstartTime").toString(), "yyyy-MM-dd"));
			} else {
				db.setFstartTime(null);
			}

			if (valueMap.containsKey("fendTime") && StringUtils.isNotBlank(valueMap.get("fendTime").toString())) {
				db.setFendTime(DateUtils.parseDate(valueMap.get("fendTime").toString(), "yyyy-MM-dd"));
			} else {
				db.setFendTime(null);
			}

		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException();
		}

		if (valueMap.containsKey("fimage") && StringUtils.isNotBlank(valueMap.get("fimage").toString())) {
			String icon = valueMap.get("fimage").toString();
			if (StringUtils.isBlank(db.getFimage()) || !db.getFimage().equals(icon)) {
				db.setFimage(icon);
				imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(icon), 2, id, 7);
			}
		} else {
			db.setFimage(null);
		}
		posterDAO.save(db);
	}

	public void onPoster(String fID, Integer Status) {

		TPoster tPoster = posterDAO.findPosterStatus();
		if (tPoster == null) {
			posterDAO.saveStatusPoster(Status, fID);
		} else {
			throw new ServiceException("海报活动只能发布一个,请下下架其他海报在进行发布！");
		}
		// 定时任务
		/*
		 * TTimingTask timetask =
		 * timingTaskDAO.getByEntityIdAndTaskType(tEvent.getId(), 2); if
		 * (timetask == null) { TTimingTask timingTask = new TTimingTask();
		 * timingTask.setEntityId(tEvent.getId());
		 * timingTask.setTaskTime(tEvent.getFoffSaleTime().getTime());
		 * timingTask.setTaskType(2); timingTaskDAO.save(timingTask); } else {
		 * timingTaskDAO.saveTaskTime(tEvent.getFoffSaleTime().getTime(),
		 * tEvent.getId(), 2); }
		 */
	}

	public void offPoster(String fID, Integer Status) {
		posterDAO.saveStatusPoster(Status, fID);
	}

	public void delPoster(String fID) {
		posterDAO.saveStatusPoster(999, fID);
		// timingTaskDAO.clearTimeTaskByEntityId(tEventBonus.getTEvent().getId());
	}

	@Transactional(readOnly = true)
	public void getOrdeBonusList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId,t.TEventBonus.TEvent.ftitle as ftitle,t.fcustomerId as fcustomerId,t.TEventBonus.fbonus as fbonus,t.fcustomerName as name,t.fcustomerPhone as phone,t.fstatus as fstatus,t.ftotal as ftotal from TOrderBonus t where t.fstatus<999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_ftitle") && StringUtils.isNotBlank(valueMap.get("s_ftitle").toString())) {
				hql.append(" and t.TEventBonus.id = :s_ftitle ");
				hqlMap.put("s_ftitle", valueMap.get("s_ftitle").toString());
			}
			if (valueMap.containsKey("s_name") && StringUtils.isNotBlank(valueMap.get("s_name").toString())) {
				hql.append(" and t.fcustomerName like :s_name ");
				hqlMap.put("s_name", "%" + valueMap.get("s_name").toString() + "%");
			}
			if (valueMap.containsKey("s_status") && StringUtils.isNotBlank(valueMap.get("s_status").toString())) {
				hql.append(" and t.fstatus = :s_status ");
				hqlMap.put("s_status", Integer.valueOf(valueMap.get("s_status").toString()));
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

		Integer rank = 1;
		Date date = null;
		for (Map<String, Object> amap : list) {
			amap.put("rank", page.getOffset() + rank++);
			if (amap.get("fcustomerId") != null && StringUtils.isNotBlank(amap.get("fcustomerId").toString())) {
				amap.put("wxname", customerDAO.findOne(amap.get("fcustomerId").toString()).getFname());
			}
			if (amap.get("fstartDate") != null && StringUtils.isNotBlank(amap.get("fstartDate").toString())) {
				date = (Date) amap.get("fstartDate");
				amap.put("fstartDate", DateFormatUtils.format(date, "yyyy-MM-dd"));
			}
			if (amap.get("fendDate") != null && StringUtils.isNotBlank(amap.get("fendDate").toString())) {
				date = (Date) amap.get("fendDate");
				amap.put("fendDate", DateFormatUtils.format(date, "yyyy-MM-dd"));
			}

			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatusString", DictionaryUtil.getString(DictionaryUtil.OrderBonusType,
						(Integer) amap.get("fstatus"), shiroUser.getLanguage()));
			}

		}
	}

	public void verOrderBonus(String fID, String pushContent) {
		orderBonusDAO.updateOrderStatus(20, fID);
		if (StringUtils.isNotBlank(pushContent)) {
			ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
			StringBuilder remark = new StringBuilder();
			remark.append("＜－－－－－－－－－－ ").append(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm"))
					.append(" －－－－－－－－－－ ").append(StringUtils.SPACE).append(StringUtils.SPACE).append("填写人：")
					.append(shiroUser.getName()).append(" －－－－－－－－－－＞").append(StringUtils.LF).append(pushContent);
			TOrderBonus tOrderBonus = orderBonusDAO.findOne(fID);
			if (StringUtils.isNotBlank(tOrderBonus.getFnote())) {
				remark.insert(0, new StringBuilder().append(tOrderBonus.getFnote()).append(StringUtils.LF));
			}
			tOrderBonus.setFnote(remark.toString());
			tOrderBonus.setFreply(pushContent);
			tOrderBonus.setFreplyTime(new Date());
			orderBonusDAO.save(tOrderBonus);
			TCustomer customer = customerDAO.findOne(tOrderBonus.getFcustomerId());
			TCustomerInfo customerInfo = customerInfoDAO.getByCustomerId(customer.getId());
			wxService.pushPrizeExchangeTemplate(tOrderBonus, customerInfo.getFpoint(), customer.getFweixinId());
		}
	}

	public void cancelBonusOrder(String fID) {
		TOrderBonus tOrderBonus = orderBonusDAO.findOne(fID);
		Date now = new Date();

		// 取消訂單同步退款
		if (tOrderBonus.getFtotal() != null) {
			if (tOrderBonus.getFpayType() == null) {
				throw new ServiceException("本订单无法执行退款！原因：该订单中没有支付类型信息");
			}
			String msg = null;
			boolean flag = false;
			BigDecimal refundAmount = null;

			// 先判断订单的支付方式，根据不同的支付方式调用相应的支付接口
			if (tOrderBonus.getFpayType().intValue() == 10) {
				// 现金支付的订单进行退款处理
			} else if (tOrderBonus.getFpayType().intValue() == 20) {
				// 微信支付的订单进行退款处理
				TWxPay tWxPay = wxPayDAO.getByOrderIdAndInOutAndStatus(tOrderBonus.getId(), 1, 30);
				if (tWxPay == null) {
					throw new ServiceException("本订单无法执行退款！原因：该订单没有成功支付记录");
				}
				WxRefundResult wxRefundResult = null;
				if (tWxPay.getFclientType().intValue() == 1) {
					wxRefundResult = WxPayUtil.wxRefund(tOrderBonus.getForderNum(), tWxPay.getFtransactionId());
				} else {
					wxRefundResult = WxPayUtil.wxAppRefund(tOrderBonus.getForderNum(), tWxPay.getFtransactionId());
				}
				msg = wxRefundResult.getMsg();
				flag = wxRefundResult.isSuccess();
				if (wxRefundResult.getCashRefundFee() == 0) {
					refundAmount = BigDecimal.ZERO;
				} else {
					refundAmount = new BigDecimal(wxRefundResult.getCashRefundFee()).divide(new BigDecimal(100), 2,
							RoundingMode.HALF_UP);
				}

				TWxPay tWxRefundPay = new TWxPay();
				tWxRefundPay.setFinOut(2);
				tWxRefundPay.setFclientType(tWxPay.getFclientType());
				tWxRefundPay.setFcpRequestInfo(wxRefundResult.getRequest());
				tWxRefundPay.setFcpResponseInfo(wxRefundResult.getResponse());
				tWxRefundPay.setFpayAmount(refundAmount);
				tWxRefundPay.setFcreateTime(now);
				tWxRefundPay.setFupdateTime(now);
				tWxRefundPay.setFcurrencyType(tWxPay.getFcurrencyType());
				if (wxRefundResult.isSuccess()) {
					tWxRefundPay.setFstatus(40);
				} else {
					tWxRefundPay.setFstatus(91);
				}
				tWxRefundPay.setFtransactionId(wxRefundResult.getRefundId());
				tWxRefundPay.setTCustomer(tWxPay.getTCustomer());
				tWxRefundPay.setForderId(tWxPay.getForderId());

				wxPayDAO.save(tWxRefundPay);
				wxPayDAO.saveStatus(40, tWxPay.getId());
			} else if (tOrderBonus.getFpayType().intValue() == 30) {
				// 支付宝支付的订单进行退款处理
			} else if (tOrderBonus.getFpayType().intValue() == 40) {
				// 银联支付的订单进行退款处理
			} else if (tOrderBonus.getFpayType().intValue() == 50) {
				// 苹果支付的订单进行退款处理
			}
			if (flag) {
				// 更改兑换订单状态
				orderBonusDAO.updateOrderStatus(100, fID);
				// 修改积分商城商品库存
				eventBonusDAO.updatetStock(1, tOrderBonus.getTEventBonus().getId());
				// 添加退还积分记录
				TCustomerBonus tCustomerBonus = new TCustomerBonus();
				tCustomerBonus.setFcreateTime(now);
				tCustomerBonus.setFbonus(tOrderBonus.getTEventBonus().getFbonus());
				tCustomerBonus.setFcustermerId(tOrderBonus.getFcustomerId());
				tCustomerBonus.setFobject(tOrderBonus.getId());
				tCustomerBonus.setFtype(60);
				customerBonusDAO.save(tCustomerBonus);
				// 更改用户总积分
				customerInfoDAO.updatePointAndUsePoint(tOrderBonus.getFcustomerId(),
						tOrderBonus.getTEventBonus().getFbonus(), -tOrderBonus.getTEventBonus().getFbonus());
			}
		} else {
			// 更改兑换订单状态
			orderBonusDAO.updateOrderStatus(100, fID);
			// 修改积分商城商品库存
			eventBonusDAO.updatetStock(1, tOrderBonus.getTEventBonus().getId());
			// 添加退还积分记录
			TCustomerBonus tCustomerBonus = new TCustomerBonus();
			tCustomerBonus.setFcreateTime(now);
			tCustomerBonus.setFbonus(tOrderBonus.getTEventBonus().getFbonus());
			tCustomerBonus.setFcustermerId(tOrderBonus.getFcustomerId());
			tCustomerBonus.setFobject(tOrderBonus.getId());
			tCustomerBonus.setFtype(60);
			customerBonusDAO.save(tCustomerBonus);
			// 更改用户总积分
			customerInfoDAO.updatePointAndUsePoint(tOrderBonus.getFcustomerId(),
					tOrderBonus.getTEventBonus().getFbonus(), -tOrderBonus.getTEventBonus().getFbonus());
		}
	}

	public String saveCsRemark(String orderId, String csRemark) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder remark = new StringBuilder();
		remark.append("＜－－－－－－－－－－ ").append(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm"))
				.append(" －－－－－－－－－－ ").append(StringUtils.SPACE).append(StringUtils.SPACE).append("填写人：")
				.append(shiroUser.getName()).append(" －－－－－－－－－－＞").append(StringUtils.LF).append(csRemark);
		TOrderBonus tOrderBonus = orderBonusDAO.findOne(orderId);
		if (StringUtils.isNotBlank(tOrderBonus.getFnote())) {
			remark.insert(0, new StringBuilder().append(tOrderBonus.getFnote()).append(StringUtils.LF));
		}
		orderBonusDAO.updateCsRemark(orderId, remark.toString());
		// order.setFcsRemark(remark.toString());
		// orderDAO.save(order);
		return remark.toString();
	}

	@Transactional(readOnly = true)
	public void getBonusRank(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId,c.fname as fname, t.fpoint as fpoint,t.fusedPoint as fusedPoint from TCustomerInfo t inner join TCustomer c on c.id = t.fcustomerId where c.fstatus = 1 ");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_name") && StringUtils.isNotBlank(valueMap.get("s_name").toString())) {
				hql.append(" and c.fname like :s_name ");
				hqlMap.put("s_name", "%" + valueMap.get("s_name").toString() + "%");
			}
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.fpoint desc");
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		Integer rank = 1;
		Date date = null;
		for (Map<String, Object> amap : list) {
			amap.put("rank", page.getOffset() + rank++);
		}
	}

	// 定时器将过期优惠券迁移到历史表中
	public void updateHistoryBonus() {
		StringBuilder hql = new StringBuilder();
		hql.append("select c.id as id,t.fpoint as point ")
				.append(" from TCustomerInfo t inner join TCustomer c on c.id = t.fcustomerId where c.fstatus = 1");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		Integer point = 0;
		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);
		for (Map<String, Object> amap : list) {
			// 查询出过期之前的历史可用积分
			String usableBonus = customerBonusDAO.findUsable(amap.get("id").toString(), new Date());
			if (StringUtils.isNotBlank(usableBonus)) {
				// 查询出用户过期之内消耗的积分
				String usedBonus = customerBonusDAO.findUsedBonus(amap.get("id").toString(), new Date());
				if (StringUtils.isNotBlank(usedBonus)) {
					point = Integer.parseInt(usableBonus) - Math.abs(Integer.parseInt(usableBonus));
				} else {
					point = Integer.parseInt(usableBonus);
				}
				if (point.intValue() > 0) {
					// 修改用户的可用积分
					customerInfoDAO.updatePoint(amap.get("id").toString(), point);
					// 添加一条积分使用记录
					TCustomerBonus tCustomerBonus = new TCustomerBonus();
					tCustomerBonus.setFcreateTime(new Date());
					tCustomerBonus.setFbonus(point);
					tCustomerBonus.setFcustermerId(amap.get("id").toString());
					tCustomerBonus.setFobject(null);
					tCustomerBonus.setFtype(666);
					customerBonusDAO.save(tCustomerBonus);
				}
			}
		}

	}

	@Transactional
	public void createBonusOrderExcel(Map<String, Object> map, String datePath, String excelFileName,
			String sessionid) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId,t.TEventBonus.TEvent.ftitle as ftitle,t.TEventBonus.fbonus as fbonus,t.ftotal as ftotal,t.fcustomerName as name,t.fcustomerPhone as phone,t.fexpress as fexpress,t.fremark as fremark,t.fstatus as fstatus from TOrderBonus t where t.fstatus<999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (map.containsKey("s_ftitle") && StringUtils.isNotBlank(map.get("s_ftitle").toString())) {
				hql.append(" and t.TEventBonus.TEvent.ftitle like :s_ftitle ");
				hqlMap.put("s_ftitle", "%" + map.get("s_ftitle").toString() + "%");
			}
			if (map.containsKey("s_name") && StringUtils.isNotBlank(map.get("s_name").toString())) {
				hql.append(" and t.fcustomerName like :s_name ");
				hqlMap.put("s_name", "%" + map.get("s_name").toString() + "%");
			}
			if (map.containsKey("s_status") && StringUtils.isNotBlank(map.get("s_status").toString())) {
				hql.append(" and t.fstatus = :s_status ");
				hqlMap.put("s_status", Integer.valueOf(map.get("s_status").toString()));
			}
			if (map.containsKey("fcreateTimeStart") && StringUtils.isNotBlank(map.get("fcreateTimeStart").toString())) {
				hql.append(" and t.fcreateTime >= :fcreateTimeStart ");
				hqlMap.put("fcreateTimeStart",
						DateUtils.parseDate(map.get("fcreateTimeStart").toString(), "yyyy-MM-dd"));
			}
			if (map.containsKey("fcreateTimeEnd") && StringUtils.isNotBlank(map.get("fcreateTimeEnd").toString())) {
				hql.append(" and t.fcreateTime < :fcreateTimeEnd ");
				// 选择的截止日期加一天，保证了查询条件的完整性，解决了时分秒的问题。
				hqlMap.put("fcreateTimeEnd",
						DateUtils.addDays(DateUtils.parseDate(map.get("fcreateTimeEnd").toString(), "yyyy-MM-dd"), 1));
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		ExcelTemplate excel = ExcelTemplate.getInstance().readTemplateClassPath("/template/excel/orderBonusTemp.xlsx");
		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("bonusOrderExcel")).append("/").append(datePath).append("/");
		for (Map<String, Object> amap : list) {
			excel.creatNewRow();
			if (amap.get("ftitle") != null && StringUtils.isNotBlank(amap.get("ftitle").toString())) {
				excel.createNewCol(amap.get("ftitle").toString());
			}
			if (amap.get("fbonus") != null && StringUtils.isNotBlank(amap.get("fbonus").toString())) {
				excel.createNewCol(amap.get("fbonus").toString());
			}
			if (amap.get("ftotal") != null && StringUtils.isNotBlank(amap.get("ftotal").toString())) {
				excel.createNewCol(amap.get("ftotal").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("name") != null && StringUtils.isNotBlank(amap.get("name").toString())) {
				excel.createNewCol(amap.get("name").toString());
			}
			if (amap.get("phone") != null && StringUtils.isNotBlank(amap.get("phone").toString())) {
				excel.createNewCol(amap.get("phone").toString());
			}
			if (amap.get("fexpress") != null && StringUtils.isNotBlank(amap.get("fexpress").toString())) {
				excel.createNewCol(amap.get("fexpress").toString());
			}
			if (amap.get("fremark") != null && StringUtils.isNotBlank(amap.get("fremark").toString())) {
				excel.createNewCol(amap.get("fremark").toString());
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				excel.createNewCol(DictionaryUtil.getString(DictionaryUtil.OrderBonusType,
						((Integer) amap.get("fstatus")), shiroUser.getLanguage()));
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
	public List<Map<String, Object>> getbonusEventMapList() {
		return commonService
				.find("select t.id as key, t.TEvent.ftitle as value from TEventBonus t where t.fstatus < 999");
	}

	@Transactional(readOnly = true)
	public void getIssueBonusList(Map<String, Object> valueMap, CommonPage page) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId,t.ftitle as ftitle,t.fdec as fdec,t.fbonus as fbonus,t.ftype as ftype,t.fcreateTime as fcreateTime,t.fcount as fcount from TIssueBonus t");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
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
			if (amap.get("fcreateTime") != null && StringUtils.isNotBlank(amap.get("fcreateTime").toString())) {
				date = (Date) amap.get("fcreateTime");
				amap.put("fcreateTime", DateFormatUtils.format(date, "yyyy-MM-dd"));
			}
			if (amap.get("ftype") != null && StringUtils.isNotBlank(amap.get("ftype").toString())) {
				amap.put("ftype", "筛选用户发放");
			}
		}
	}

	public void addIssueBonus(Map<String, Object> valueMap) {

		Date now = new Date();
		TIssueBonus tIssueBonus = new TIssueBonus();
		tIssueBonus.setFtype(1);
		tIssueBonus.setFcreateTime(now);
		if (valueMap.containsKey("fbonus") && StringUtils.isNotBlank(valueMap.get("fbonus").toString())) {
			tIssueBonus.setFbonus(Integer.parseInt(valueMap.get("fbonus").toString()));
		}
		if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
			tIssueBonus.setFtitle(valueMap.get("ftitle").toString());
		}
		Date begin = null;
		Date end = null;
		try {
			begin = DateUtils.parseDate(valueMap.get("fstartDate").toString(), "yyyy-MM-dd");
			end = DateUtils.parseDate(valueMap.get("fendDate").toString() + " 23:59:59", "yyyy-MM-dd HH:mm:ss");
			if (valueMap.containsKey("fstartDate") && StringUtils.isNotBlank(valueMap.get("fstartDate").toString())) {
				tIssueBonus.setFbeginTime(DateUtils.parseDate(valueMap.get("fstartDate").toString(), "yyyy-MM-dd"));
			}
			if (valueMap.containsKey("fendDate") && StringUtils.isNotBlank(valueMap.get("fendDate").toString())) {
				tIssueBonus.setFendTime(
						DateUtils.parseDate(valueMap.get("fendDate").toString() + " 23:59:59", "yyyy-MM-dd HH:mm:ss"));
			}
		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
		if (valueMap.containsKey("fdesc") && StringUtils.isNotBlank(valueMap.get("fdesc").toString())) {
			tIssueBonus.setFdec(valueMap.get("fdesc").toString());
		}
		List<String> tCustomers = customerDAO.getByTime(begin, end);
		tIssueBonus.setFcount(tCustomers.size());
		tIssueBonus = issusBonusDAO.save(tIssueBonus);

		List<TCustomerBonus> tCustomerBonus = new ArrayList<TCustomerBonus>();
		TCustomerBonus customerBonus = null;
		for (String id : tCustomers) {
			customerBonus = new TCustomerBonus();
			customerBonus.setFbonus(Integer.parseInt(valueMap.get("fbonus").toString()));
			customerBonus.setFcreateTime(now);
			customerBonus.setFcustermerId(id);
			customerBonus.setFobject(tIssueBonus.getId());
			customerBonus.setFtype(70);
			tCustomerBonus.add(customerBonus);
		}
		customerBonusDAO.save(tCustomerBonus);
		StringBuilder hql = new StringBuilder();
		hql.append("update TCustomerInfo i set i.fpoint = i.fpoint + :fbonus where i.fcustomerId in (").append(
				"select t.id from TCustomer t where t.fcreateTime between :begin and :end and t.ftype = 1 and t.fstatus = 1 and t.fweixinId is not null)");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("fbonus", Integer.parseInt(valueMap.get("fbonus").toString()));
		hqlMap.put("begin", begin);
		hqlMap.put("end", end);
		Query query = commonService.createQuery(hql.toString(), hqlMap);
		query.executeUpdate();

		// List<String> openIds = customerDAO.getOpenIdByTime(begin,end);
	}

	@Transactional(readOnly = true)
	public Map<Integer, String> getcustomerLevelMap() {
		List<TCustomerLevel> list = customerLevelDAO.findAll();
		Map<Integer, String> customerLevelMap = Maps.newHashMap();
		for (TCustomerLevel tCustomerLevel : list) {
			customerLevelMap.put(tCustomerLevel.getFlevel(), tCustomerLevel.getFtitle());
		}
		return customerLevelMap;
	}

}