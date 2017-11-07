package com.czyh.czyhweb.service.goods;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.shiro.SecurityUtils;
import org.hibernate.query.internal.QueryImpl;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springside.modules.mapper.BeanMapper;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;

import com.czyh.czyhweb.dao.AppChannelEventDAO;
import com.czyh.czyhweb.dao.AppChannelSettingDAO;
import com.czyh.czyhweb.dao.ColumnBannerDAO;
import com.czyh.czyhweb.dao.EventBargainingDAO;
import com.czyh.czyhweb.dao.EventCategoryDAO;
import com.czyh.czyhweb.dao.EventDAO;
import com.czyh.czyhweb.dao.EventDetailDAO;
import com.czyh.czyhweb.dao.EventExtInfoDAO;
import com.czyh.czyhweb.dao.EventRelationDAO;
import com.czyh.czyhweb.dao.EventSessionDAO;
import com.czyh.czyhweb.dao.EventSpecDAO;
import com.czyh.czyhweb.dao.GoodsSkuDAO;
import com.czyh.czyhweb.dao.GoodsSpaceValueDAO;
import com.czyh.czyhweb.dao.GoodsTypeClassCategoryDAO;
import com.czyh.czyhweb.dao.GoodsTypeClassDAO;
import com.czyh.czyhweb.dao.GoodsTypeClassValueDAO;
import com.czyh.czyhweb.dao.ImageDAO;
import com.czyh.czyhweb.dao.TimingTaskDAO;
import com.czyh.czyhweb.dao.UserDAO;
import com.czyh.czyhweb.dto.EventDetailDTO;
import com.czyh.czyhweb.dto.goods.GoodsSolrDTO;
import com.czyh.czyhweb.dto.system.ObjectTimeStampDTO;
import com.czyh.czyhweb.entity.TAppChannelEvent;
import com.czyh.czyhweb.entity.TAppChannelSetting;
import com.czyh.czyhweb.entity.TColumnBanner;
import com.czyh.czyhweb.entity.TEvent;
import com.czyh.czyhweb.entity.TEventBargaining;
import com.czyh.czyhweb.entity.TEventCategory;
import com.czyh.czyhweb.entity.TEventDetail;
import com.czyh.czyhweb.entity.TEventExtInfo;
import com.czyh.czyhweb.entity.TEventRelation;
import com.czyh.czyhweb.entity.TEventSession;
import com.czyh.czyhweb.entity.TEventSpec;
import com.czyh.czyhweb.entity.TGoodsSku;
import com.czyh.czyhweb.entity.TGoodsSpaceValue;
import com.czyh.czyhweb.entity.TGoodsTypeClass;
import com.czyh.czyhweb.entity.TGoodsTypeClassCategory;
import com.czyh.czyhweb.entity.TGoodsTypeClassValue;
import com.czyh.czyhweb.entity.TImage;
import com.czyh.czyhweb.entity.TSponsor;
import com.czyh.czyhweb.entity.TTimingTask;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.CommonService;
import com.czyh.czyhweb.service.FxlService;
import com.czyh.czyhweb.service.system.AppService;
import com.czyh.czyhweb.service.system.RedisService;
import com.czyh.czyhweb.service.system.SystemService;
import com.czyh.czyhweb.util.ArrayStringUtils;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.PropertiesUtil;
import com.czyh.czyhweb.util.html.StaticHtmlBean;
import com.czyh.czyhweb.util.html.StaticHtmlManager;
import com.czyh.czyhweb.util.redis.RedisMoudel;
import com.czyh.czyhweb.util.solr.SolrUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * 商品核心类
 * 
 * @author jinshengzhi
 */
@Component
@Transactional
public class GoodsService {

	private static final Logger logger = LoggerFactory.getLogger(GoodsService.class);

	private static JsonMapper mapper = new JsonMapper(Include.ALWAYS);

	private static final int OnSaleRedis = 1;

	private static final int OffSaleRedis = 2;

	@Autowired
	private CommonService commonService;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private EventDAO eventDAO;

	@Autowired
	private EventSpecDAO eventSpecDAO;

	@Autowired
	private EventSessionDAO eventSessionDAO;

	@Autowired
	private AppChannelEventDAO appChannelEventDAO;

	@Autowired
	private EventCategoryDAO eventCategoryDAO;

	@Autowired
	private ImageDAO imageDAO;

	@Autowired
	private TimingTaskDAO timingTaskDAO;

	@Autowired
	private EventDetailDAO eventDetailDAO;

	@Autowired
	private EventExtInfoDAO eventExtInfoDAO;

	@Autowired
	private EventRelationDAO eventRelationDAO;

	// @Autowired
	// private CommentDAO commentDAO;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private ColumnBannerDAO columnBannerDAO;

	@Autowired
	private AppChannelSettingDAO appChannelSettingDAO;

	@Autowired
	private AppService appService;

	@Autowired
	private GoodsSkuDAO goodsSkuDAO;

	@Autowired
	private GoodsTypeClassDAO goodsTypeClassDAO;

	@Autowired
	private GoodsTypeClassCategoryDAO goodsTypeClassCategoryDAO;

	@Autowired
	private GoodsTypeClassValueDAO goodsTypeClassValueDAO;

	@Autowired
	private GoodsSpaceValueDAO goodsSpaceValueDAO;

	@Autowired
	private RedisService redisService;

	@Autowired
	private SystemService systemService;

	@Autowired
	private FxlService fxlService;

	@Autowired
	private EventBargainingDAO eventBargainingDAO;

	@Transactional(readOnly = true)
	public void getEventList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.ftitle as ftitle, s.fname as fsponsor, t.ftypeA as ftypeA, t.forderType as forderType, t.fbdId as fbdId, t.fcreaterId as fcreaterId, t.fonSaleTime as fonSaleTime, t.foffSaleTime as foffSaleTime,t.fsdealsModel as fsdealsModel,t.fpriceMoney as fpriceMoney,t.fstatus as fstatus from TEvent t left join t.TSponsor s");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_fappChannelId")
					&& StringUtils.isNotBlank(valueMap.get("s_fappChannelId").toString())) {
				hql.append(
						" inner join t.TAppChannels a where t.fstatus < 999 and a.TAppChannelSetting.id = :s_fappChannelId ");
				hqlMap.put("s_fappChannelId", valueMap.get("s_fappChannelId").toString());
			} else {
				hql.append(" where t.fstatus < 999");
			}
			if (valueMap.containsKey("s_ftitle") && StringUtils.isNotBlank(valueMap.get("s_ftitle").toString())) {
				hql.append(" and t.ftitle like :s_ftitle ");
				hqlMap.put("s_ftitle", "%" + valueMap.get("s_ftitle").toString() + "%");
			}
			if (valueMap.containsKey("s_fbdId") && StringUtils.isNotBlank(valueMap.get("s_fbdId").toString())) {
				hql.append(" and t.fbdId = :s_fbdId ");
				hqlMap.put("s_fbdId", Long.valueOf(valueMap.get("s_fbdId").toString()));
			}
			if (valueMap.containsKey("s_fcreaterId")
					&& StringUtils.isNotBlank(valueMap.get("s_fcreaterId").toString())) {
				hql.append(" and t.fcreaterId = :s_fcreaterId ");
				hqlMap.put("s_fcreaterId", Long.valueOf(valueMap.get("s_fcreaterId").toString()));
			}
			if (valueMap.containsKey("s_fsponsor") && StringUtils.isNotBlank(valueMap.get("s_fsponsor").toString())) {
				hql.append(" and s.id = :s_fsponsor ");
				hqlMap.put("s_fsponsor", valueMap.get("s_fsponsor").toString());
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
			if (valueMap.containsKey("statusBegin") && StringUtils.isNotBlank(valueMap.get("statusBegin").toString())) {
				hql.append(" and t.fstatus >= :statusBegin ");
				hqlMap.put("statusBegin", Integer.valueOf(valueMap.get("statusBegin").toString()));
			}
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				if (valueMap.containsKey("s_fappChannelId")
						&& StringUtils.isNotBlank(valueMap.get("s_fappChannelId").toString())) {
					hql.append(" order by a.forder desc");
				} else {
					hql.append(" order by t.fcreateTime desc");
				}
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		// 获取到活动类目缓存对象
		Cache eventCategoryCache = cacheManager.getCache(Constant.EventCategory);
		Element ele = null;

		Date date = null;
		for (Map<String, Object> amap : list) {
			if (amap.get("ftypeA") != null && StringUtils.isNotBlank(amap.get("ftypeA").toString())) {
				ele = eventCategoryCache.get((Integer) amap.get("ftypeA"));
				amap.put("ftypeA", ele != null ? ele.getObjectValue().toString() : StringUtils.EMPTY);
			}
			// if (amap.get("fdeal") != null &&
			// StringUtils.isNotBlank(amap.get("fdeal").toString())) {
			// amap.put("fdeal", amap.get("fdeal").toString());
			// } else {
			// amap.put("fdeal", "免费");
			// }
			if (amap.get("forderType") != null && StringUtils.isNotBlank(amap.get("forderType").toString())) {
				amap.put("forderType", DictionaryUtil.getString(DictionaryUtil.OrderType,
						((Integer) amap.get("forderType")), shiroUser.getLanguage()));
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatusString", DictionaryUtil.getString(DictionaryUtil.EventStatus,
						((Integer) amap.get("fstatus")), shiroUser.getLanguage()));
			}
			if (amap.get("fbdId") != null && StringUtils.isNotBlank(amap.get("fbdId").toString())) {
				amap.put("fbdId", userDAO.getOne((Long) amap.get("fbdId")).getRealname());
			}
			if (amap.get("fcreaterId") != null && StringUtils.isNotBlank(amap.get("fcreaterId").toString())) {
				amap.put("fcreaterId", userDAO.getOne((Long) amap.get("fcreaterId")).getRealname());
			}
			if (amap.get("fonSaleTime") != null && StringUtils.isNotBlank(amap.get("fonSaleTime").toString())) {
				date = (Date) amap.get("fonSaleTime");
				amap.put("fonSaleTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
			}
			if (amap.get("foffSaleTime") != null && StringUtils.isNotBlank(amap.get("foffSaleTime").toString())) {
				date = (Date) amap.get("foffSaleTime");
				amap.put("foffSaleTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
				amap.put("falg", "自动上架");
			} else {
				amap.put("falg", "手动上架");
			}

		}
	}

	@Transactional(readOnly = true)
	public void getEventListByRelease(Map<String, Object> valueMap, CommonPage page) {

		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.ftitle as ftitle, s.fname as fsponsor,t.fsdealsModel as fsdealsModel, t.fstatus as fstatus from TEvent t left join t.TSponsor s");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_fappChannelId")
					&& StringUtils.isNotBlank(valueMap.get("s_fappChannelId").toString())) {
				hql.append(
						" inner join t.TAppChannels a where t.fstatus < 999 and a.TAppChannelSetting.id = :s_fappChannelId ");
				hqlMap.put("s_fappChannelId", valueMap.get("s_fappChannelId").toString());
			} else {
				hql.append(" where t.fstatus < 999 and t.fsdealsModel = 0");
			}
			if (valueMap.containsKey("s_ftitle") && StringUtils.isNotBlank(valueMap.get("s_ftitle").toString())) {
				hql.append(" and t.ftitle like :s_ftitle ");
				hqlMap.put("s_ftitle", "%" + valueMap.get("s_ftitle").toString() + "%");
			}
			if (valueMap.containsKey("s_fsponsor") && StringUtils.isNotBlank(valueMap.get("s_fsponsor").toString())) {
				hql.append(" and s.id = :s_fsponsor ");
				hqlMap.put("s_fsponsor", valueMap.get("s_fsponsor").toString());
			}
			if (valueMap.containsKey("s_status") && StringUtils.isNotBlank(valueMap.get("s_status").toString())) {
				hql.append(" and t.fstatus = :s_status ");
				hqlMap.put("s_status", Integer.valueOf(valueMap.get("s_status").toString()));
			}
			if (valueMap.containsKey("s_fbdId") && StringUtils.isNotBlank(valueMap.get("s_fbdId").toString())) {
				hql.append(" and t.fbdId = :s_fbdId ");
				hqlMap.put("s_fbdId", Long.valueOf(valueMap.get("s_fbdId").toString()));
			}
			if (valueMap.containsKey("s_fcreaterId")
					&& StringUtils.isNotBlank(valueMap.get("s_fcreaterId").toString())) {
				hql.append(" and t.fcreaterId = :s_fcreaterId ");
				hqlMap.put("s_fcreaterId", Long.valueOf(valueMap.get("s_fcreaterId").toString()));
			}
			if (valueMap.containsKey("s_fbdId") && StringUtils.isNotBlank(valueMap.get("s_fbdId").toString())) {
				hql.append(" and t.fbdId = :s_fbdId ");
				hqlMap.put("s_fbdId", Long.valueOf(valueMap.get("s_fbdId").toString()));
			}
			if (valueMap.containsKey("s_fcreaterId")
					&& StringUtils.isNotBlank(valueMap.get("s_fcreaterId").toString())) {
				hql.append(" and t.fcreaterId = :s_fcreaterId ");
				hqlMap.put("s_fcreaterId", Long.valueOf(valueMap.get("s_fcreaterId").toString()));
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
				if (valueMap.containsKey("s_fappChannelId")
						&& StringUtils.isNotBlank(valueMap.get("s_fappChannelId").toString())) {
					hql.append(" order by a.forder desc");
				} else {
					hql.append(" order by t.fcreateTime desc");
				}
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		StringBuffer infoA = new StringBuffer();
		StringBuffer infoB = new StringBuffer();
		StringBuffer infoC = new StringBuffer();
		int type = 0;
		List<Map<String, Object>> lista = null;

		for (Map<String, Object> amap : list) {
			String eventId = amap.get("DT_RowId").toString();

			hql.delete(0, hql.length());
			hql.append(
					"select c.ftitle as ftitle, c.ftype as ftype from TAppChannelEvent t inner join t.TAppChannelSetting c where t.TEvent.id = :eventId and c.ftype in (1,2,3) order by c.forder");
			hqlMap.clear();
			hqlMap.put("eventId", eventId);
			lista = commonService.find(hql.toString(), hqlMap);

			infoA.delete(0, infoA.length());
			infoB.delete(0, infoB.length());
			infoC.delete(0, infoC.length());
			for (Map<String, Object> map : lista) {
				type = ((Integer) map.get("ftype")).intValue();
				switch (type) {
				case 1: {
					infoA.append(map.get("ftitle").toString()).append("；");
					break;
				}
				case 2: {
					infoB.append(map.get("ftitle").toString()).append("；");
					break;
				}
				case 3: {
					infoC.append(map.get("ftitle").toString()).append("；");
					break;
				}
				default: {
					break;
				}
				}
			}

			if (infoA.length() > 0) {
				amap.put("releaseA", infoA.deleteCharAt(infoA.length() - 1).toString());
			} else {
				amap.put("releaseA", StringUtils.EMPTY);
			}
			if (infoB.length() > 0) {
				amap.put("releaseB", infoB.deleteCharAt(infoB.length() - 1).toString());
			} else {
				amap.put("releaseB", StringUtils.EMPTY);
			}
			if (infoC.length() > 0) {
				amap.put("releaseC", infoC.deleteCharAt(infoC.length() - 1).toString());
			} else {
				amap.put("releaseC", StringUtils.EMPTY);
			}

			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatusString",
						DictionaryUtil.getString(DictionaryUtil.EventStatus, ((Integer) amap.get("fstatus"))));
			}
		}
	}

	@Transactional(readOnly = true)
	public TEvent getEvent(String eventId) {
		return eventDAO.getOne(eventId);
	}

	public void delEvent(String eventId) {
		TEvent db = eventDAO.getOne(eventId);
		db.getTAppChannels().clear();
		db.setFstatus(999);
		db.setFupdateTime(new Date());
		eventDAO.save(db);
		timingTaskDAO.clearTimeTaskByEntityId(eventId);
	}

	public String saveEventA(Map<String, Object> valueMap) {
		Date now = new Date();
		TEvent tEvent = new TEvent();
		if (valueMap.containsKey("eventId") && StringUtils.isNotBlank(valueMap.get("eventId").toString())) {
			tEvent = eventDAO.getOne(valueMap.get("eventId").toString());
			if (valueMap.containsKey("ftypeA") && StringUtils.isNotBlank(valueMap.get("ftypeA").toString())) {
				tEvent.setFtypeA(Integer.valueOf(valueMap.get("ftypeA").toString()));
			} else {
				tEvent.setFtypeA(null);
			}
			if (valueMap.containsKey("ftypeB") && StringUtils.isNotBlank(valueMap.get("ftypeB").toString())) {
				tEvent.setFtypeB(Integer.valueOf(valueMap.get("ftypeB").toString()));
			} else {
				tEvent.setFtypeB(null);
			}
			tEvent.setFupdateTime(now);
		} else {
			tEvent = new TEvent();
			BeanMapper.copy(valueMap, tEvent);
			tEvent.setFrecommend(RandomUtils.nextLong(10L, 20L));
			tEvent.setFscore(new BigDecimal(5));
			tEvent.setFcreateTime(now);
			tEvent.setFupdateTime(now);
		}
		tEvent = eventDAO.save(tEvent);
		return tEvent.getId();
	}

	public void saveEventB(Map<String, Object> valueMap) {

		if (valueMap.containsKey("eventId") && StringUtils.isNotBlank(valueMap.get("eventId").toString())) {
			TEvent db = eventDAO.getOne(valueMap.get("eventId").toString());
			if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
				db.setFtitle(valueMap.get("ftitle").toString());
			} else {
				db.setFtitle(null);
			}
			// 添加可否用优惠
			if (valueMap.containsKey("fUsePreferential")
					&& StringUtils.isNotBlank(valueMap.get("fUsePreferential").toString())) {
				db.setFusePreferential(Integer.valueOf(valueMap.get("fUsePreferential").toString()));
			} else {
				db.setFusePreferential(0);
			}
			if (valueMap.containsKey("fsadelModel") && StringUtils.isNotBlank(valueMap.get("fsadelModel").toString())) {
				db.setFsdealsModel(Integer.valueOf(valueMap.get("fsadelModel").toString()));
			} else {
				db.setFsdealsModel(0);
			}
			if (valueMap.containsKey("fcity") && StringUtils.isNotBlank(valueMap.get("fcity").toString())) {
				db.setFcity(Integer.valueOf(valueMap.get("fcity").toString()));
			} else {
				db.setFcity(null);
			}
			if (valueMap.containsKey("fsubTitle") && StringUtils.isNotBlank(valueMap.get("fsubTitle").toString())) {
				db.setFsubTitle(valueMap.get("fsubTitle").toString());
			} else {
				db.setFsubTitle(null);
			}
			if (valueMap.containsKey("fsubTitleImg")
					&& StringUtils.isNotBlank(valueMap.get("fsubTitleImg").toString())) {
				db.setFsubTitleImg(Integer.valueOf(valueMap.get("fsubTitleImg").toString()));
			} else {
				db.setFsubTitleImg(null);
			}
			if (valueMap.containsKey("fsponsor") && StringUtils.isNotBlank(valueMap.get("fsponsor").toString())) {
				db.setTSponsor(new TSponsor(valueMap.get("fsponsor").toString()));
			} else {
				db.setTSponsor(null);
			}
			if (valueMap.containsKey("fprice") && StringUtils.isNotBlank(valueMap.get("fprice").toString())) {
				db.setFprice(new BigDecimal(valueMap.get("fprice").toString()));
			} else {
				db.setFprice(null);
			}
			if (valueMap.containsKey("forderPrice") && StringUtils.isNotBlank(valueMap.get("forderPrice").toString())) {
				db.setForderPrice(new BigDecimal(valueMap.get("forderPrice").toString()));
			} else {
				db.setForderPrice(null);
			}
			if (valueMap.containsKey("fbdId") && StringUtils.isNotBlank(valueMap.get("fbdId").toString())) {
				db.setFbdId(Long.valueOf(valueMap.get("fbdId").toString()));
			} else {
				db.setFbdId(null);
			}
			if (valueMap.containsKey("fcreaterId") && StringUtils.isNotBlank(valueMap.get("fcreaterId").toString())) {
				db.setFcreaterId(Long.valueOf(valueMap.get("fcreaterId").toString()));
			} else {
				db.setFcreaterId(null);
			}
			if (valueMap.containsKey("ffocus") && StringUtils.isNotBlank(valueMap.get("ffocus").toString())) {
				db.setFfocus(valueMap.get("ffocus").toString());
			} else {
				db.setFfocus(null);
			}
			if (valueMap.containsKey("fbrief") && StringUtils.isNotBlank(valueMap.get("fbrief").toString())) {
				db.setFbrief(valueMap.get("fbrief").toString());
			} else {
				db.setFbrief(null);
			}

			if (valueMap.containsKey("fsellModel") && StringUtils.isNotBlank(valueMap.get("fsellModel").toString())) {
				db.setFsellModel(Integer.valueOf(valueMap.get("fsellModel").toString()));
			} else {
				db.setFsellModel(null);
			}
			db.setFspecModel(0);
			if (valueMap.containsKey("fspec") && StringUtils.isNotBlank(valueMap.get("fspec").toString())) {
				db.setFspec(valueMap.get("fspec").toString());
			} else {
				db.setFspec(null);
			}
			if (valueMap.containsKey("flimitation") && StringUtils.isNotBlank(valueMap.get("flimitation").toString())) {
				db.setFlimitation(Integer.valueOf(valueMap.get("flimitation").toString()));
			} else {
				db.setFlimitation(null);
			}
			if (valueMap.containsKey("fpriceMoney") && StringUtils.isNotBlank(valueMap.get("fpriceMoney").toString())) {
				db.setFpriceMoney(new BigDecimal(valueMap.get("fpriceMoney").toString()));
			} else {
				db.setFpriceMoney(null);
			}
			if (valueMap.containsKey("ftotal") && StringUtils.isNotBlank(valueMap.get("ftotal").toString())) {
				db.setFtotal(Integer.valueOf(valueMap.get("ftotal").toString()));
				if (db.getFstock() == null) {
					db.setFstock(Integer.valueOf(valueMap.get("ftotal").toString()));
				}
			} else {
				db.setFtotal(null);
			}
			if (valueMap.containsKey("fonSaleTime") && valueMap.get("fonSaleTime") != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				try {
					db.setFonSaleTime(sdf.parse(valueMap.get("fonSaleTime").toString()));
					if (db.getId() != null) {
						TTimingTask timingTask = new TTimingTask();
						timingTask.setEntityId(db.getId());
						timingTask.setTaskTime(db.getFonSaleTime().getTime());
						timingTask.setTaskType(1);
						timingTaskDAO.save(timingTask);

					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else {
				db.setFonSaleTime(null);
			}

			// if (valueMap.containsKey("fpromotionModel")
			// &&
			// StringUtils.isNotBlank(valueMap.get("fpromotionModel").toString()))
			// {
			// db.setFpromotionModel(Integer.valueOf(valueMap.get("fpromotionModel").toString()));
			// } else {
			// db.setFpromotionModel(null);
			// }
			// if (valueMap.containsKey("fgoodsTag") &&
			// StringUtils.isNotBlank(valueMap.get("fgoodsTag").toString())) {
			// db.setFgoodsTag(Integer.valueOf(valueMap.get("fgoodsTag").toString()));
			// } else {
			// db.setFgoodsTag(null);
			// }

			if (db.getFpromotionModel() == null) {
				db.setFpromotionModel(0);
			}

			if (db.getFgoodsTag() == null) {
				db.setFgoodsTag(0);
			}

			if (db.getFsaleTotal() == null) {
				db.setFsaleTotal(0);
			}
			db.setFupdateTime(new Date());
			db.setFsalesType(0);
			eventDAO.save(db);
		} else {
			throw new ServiceException("本操作缺少活动ID信息，保存出现了错误！");
		}
	}

	public void saveEventC(Map<String, Object> valueMap) {
		if (valueMap.containsKey("eventId") && StringUtils.isNotBlank(valueMap.get("eventId").toString())) {
			TEvent db = eventDAO.getOne(valueMap.get("eventId").toString());

			if (valueMap.containsKey("fimage2") && StringUtils.isNotBlank(valueMap.get("fimage2").toString())) {
				String ids = valueMap.get("fimage2").toString();
				if (StringUtils.isNotBlank(ids)) {
					ids = ids.substring(0, ids.length() - 1);
					if (!ids.equalsIgnoreCase(db.getFimage2())) {
						String[] idArray = ids.split(";");

						for (int i = 0; i < idArray.length; i++) {
							imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(idArray[i]), 2, db.getId(), 3);
						}
						// db.setFimage1(idArray[0]);
						// db.setFimage2(ids);
						eventDAO.updateImage12(db.getId(), idArray[0], ids);
					}
				}
			}
		} else {
			throw new ServiceException("本操作缺少活动ID信息，保存出现了错误！");
		}
	}

	public void saveEventD(Map<String, Object> valueMap) {
		if (valueMap.containsKey("eventId") && StringUtils.isNotBlank(valueMap.get("eventId").toString())) {
			Date now = new Date();
			TEvent db = eventDAO.getOne(valueMap.get("eventId").toString());
			if (valueMap.containsKey("fdetail") && StringUtils.isNotBlank(valueMap.get("fdetail").toString())) {
				db.setFdetail(valueMap.get("fdetail").toString());

				String relativePath = new StringBuilder("/event/").append(DateFormatUtils.format(now, "yyyy-MM-dd"))
						.append("/").append(db.getId()).append(".html").toString();
				db.setFdetailHtmlUrl(relativePath);
				// 创建异步页面静态化Bean
				StaticHtmlBean staticHtmlBean = new StaticHtmlBean();
				staticHtmlBean.setObjectId(db.getId());
				staticHtmlBean.setTemplateName(Constant.EventDetilTemplate);
				staticHtmlBean.setRelativePath(relativePath);

				Set<TEventDetail> set = db.getTEventDetails();
				List<EventDetailDTO> eventDetailList = Lists.newArrayList();
				EventDetailDTO eventDetailDTO = null;
				for (TEventDetail tEventDetail : set) {
					eventDetailDTO = new EventDetailDTO();
					eventDetailDTO.setImageUrl(new StringBuilder().append(PropertiesUtil.getProperty("fileServerUrl"))
							.append(DictionaryUtil.getString(DictionaryUtil.EventIcon, tEventDetail.getFtypeicon()))
							.toString());
					eventDetailDTO.setTitle(
							DictionaryUtil.getString(DictionaryUtil.EventTypeDetail, tEventDetail.getFtype()));
					String[] contents = tEventDetail.getFcontent().split("#FXL#");
					String[] newContents = ArrayUtils.EMPTY_STRING_ARRAY;
					for (String content : contents) {
						if (StringUtils.isNotBlank(content)) {
							content = StringUtils.replace(content, "\r\n", "<br/>");
							newContents = ArrayUtils.add(newContents, content);
						}
					}
					eventDetailDTO.setContent(newContents);
					eventDetailList.add(eventDetailDTO);
				}

				Map<String, Object> map = Maps.newHashMap();
				if (valueMap.get("fdetail") != null) {
					// 获取到html详情
					String html = valueMap.get("fdetail").toString();
					// 将img标签的src属性替换成懒加载url
					html = StringUtils.replace(html, "<img src=",
							"<img src=\"http://file.021-sdeals.cn/image/czyh.png\" data-layzr=");

					map.put("detailHtml", html);
				}
				if (CollectionUtils.isNotEmpty(set)) {
					map.put("eventDetailList", eventDetailList);
				}
				staticHtmlBean.setContentMap(map);
				// 异步生成活动静态页面，不占用本线程的执行时间
				StaticHtmlManager.put(staticHtmlBean);
			} else {
				db.setFdetail(null);
			}

			if (CollectionUtils.isEmpty(db.getTEventSessions()) && CollectionUtils.isEmpty(db.getTEventSpecs())) {
				db.setFstatus(4);
			} else if (CollectionUtils.isEmpty(db.getTEventSessions())) {
				db.setFstatus(5);
			} else if (CollectionUtils.isEmpty(db.getTEventSpecs())) {
				db.setFstatus(6);
			}
			db.setFupdateTime(now);
			eventDAO.save(db);
		} else {
			throw new ServiceException("本操作缺少活动ID信息，保存出现了错误！");
		}
	}

	/**
	 * 添加活动规则
	 * 
	 * @param valueMap
	 */
	public void saveEvent(Map<String, Object> valueMap) {

		if (valueMap.containsKey("eventId") && StringUtils.isNotBlank(valueMap.get("eventId").toString())) {
			String eventId = valueMap.get("eventId").toString();

			eventDetailDAO.deleteByEventId(eventId);

			TEvent tevent = eventDAO.getOne(eventId);
			String[] ftypes = null;
			if (valueMap.containsKey("ftype") && org.springframework.util.ObjectUtils.isArray(valueMap.get("ftype"))) {
				ftypes = (String[]) valueMap.get("ftype");
			} else {
				ftypes = ArrayUtils.toArray(valueMap.get("ftype").toString());
			}

			String[] fcontents = null;
			if (valueMap.containsKey("fcontent")
					&& org.springframework.util.ObjectUtils.isArray(valueMap.get("fcontent"))) {
				fcontents = (String[]) valueMap.get("fcontent");
			} else {
				fcontents = ArrayUtils.toArray(valueMap.get("fcontent").toString());
			}

			List<TEventDetail> list = Lists.newArrayList();

			TEventDetail teventDetail = null;
			if (valueMap.get("fcontent") != null && StringUtils.isNotBlank(valueMap.get("fcontent").toString())) {
				int num = 1;
				for (int i = 0; i < fcontents.length; i++) {
					teventDetail = new TEventDetail();
					String count = fcontents[i];
					teventDetail.setFcontent(count);
					teventDetail.setFtype(Integer.valueOf(ftypes[i]).intValue());
					teventDetail.setTEvent(tevent);
					teventDetail.setForder(num++);
					teventDetail.setFtypeicon(Integer.valueOf(ftypes[i]).intValue());
					eventDetailDAO.save(teventDetail);
					list.add(teventDetail);
				}
			}
			// 生成活动的静态html页面任务
			String relativePath = new StringBuilder("/event/").append(DateFormatUtils.format(new Date(), "yyyy-MM-dd"))
					.append("/").append(tevent.getId()).append(".html").toString();
			tevent.setFdetailHtmlUrl(relativePath);
			// 创建异步页面静态化Bean
			StaticHtmlBean staticHtmlBean = new StaticHtmlBean();
			staticHtmlBean.setObjectId(tevent.getId());
			staticHtmlBean.setTemplateName(Constant.EventDetilTemplate);
			staticHtmlBean.setRelativePath(relativePath);

			List<EventDetailDTO> eventDetailList = Lists.newArrayList();
			EventDetailDTO eventDetailDTO = null;
			for (TEventDetail tEventDetail : list) {
				eventDetailDTO = new EventDetailDTO();
				eventDetailDTO.setImageUrl(new StringBuilder().append(PropertiesUtil.getProperty("fileServerUrl"))
						.append(DictionaryUtil.getString(DictionaryUtil.EventIcon, tEventDetail.getFtypeicon()))
						.toString());
				eventDetailDTO
						.setTitle(DictionaryUtil.getString(DictionaryUtil.EventTypeDetail, tEventDetail.getFtype()));
				String[] contents = tEventDetail.getFcontent().split("#FXL#");
				String[] newContents = ArrayUtils.EMPTY_STRING_ARRAY;
				for (String content : contents) {
					if (StringUtils.isNotBlank(content)) {
						content = StringUtils.replace(content, "\r\n", "<br/>");
						newContents = ArrayUtils.add(newContents, content);
					}
				}
				eventDetailDTO.setContent(newContents);
				eventDetailList.add(eventDetailDTO);
			}

			Map<String, Object> map = Maps.newHashMap();
			if (tevent.getFdetail() != null) {
				// 获取到html详情
				String html = tevent.getFdetail();
				// 将img标签的src属性替换成懒加载url
				html = StringUtils.replace(html, "<img src=",
						"<img src=\"http://file.021-sdeals.cn/image/czyh.png\" data-layzr=");

				map.put("detailHtml", html);
			}
			if (CollectionUtils.isNotEmpty(eventDetailList)) {
				map.put("eventDetailList", eventDetailList);
			}
			staticHtmlBean.setContentMap(map);
			// 异步生成活动静态页面，不占用本线程的执行时间
			StaticHtmlManager.put(staticHtmlBean);
		}
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getEventSpecList(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.ftitle as ftitle, t.fprice as fprice, t.fsettlementPrice as fsettlementPrice, t.fpointsPrice as fpointsPrice, t.fpostage as fpostage,")
				.append(" t.fadult as fadult, t.fchild as fchild, t.fstock as fstock, t.fdistributionRebateAmount as fdistributionRebateAmount, fdistributionRebateRatio as fdistributionRebateRatio")
				.append(" from TEventSpec t where t.TEvent.id = :eventId and t.fstatus < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("eventId", valueMap.get("eventId").toString());
		try {
			if (valueMap.containsKey("type") && valueMap.get("type").toString().equals("event")) {
				hql.append(" and t.TEventSession.id is null");
			} else {
				hql.append(" and t.TEventSession.id = :sessionId");
				hqlMap.put("sessionId", valueMap.get("sessionId").toString());
			}
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.forder desc");
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);
		int flag = 0;
		StringBuilder info = new StringBuilder();
		for (Map<String, Object> amap : list) {
			// 价格信息
			flag = 0;
			info.delete(0, info.length());
			if (amap.get("fprice") != null && StringUtils.isNotBlank(amap.get("fprice").toString())) {
				info.append("<strong>原价：</strong>").append(amap.get("fprice").toString());
				flag = flag + 1;
			}
			// if (amap.get("fdeal") != null &&
			// StringUtils.isNotBlank(amap.get("fdeal").toString())) {
			// if (flag > 0) {
			// info.append("<br/>");
			// }
			// info.append("<strong>现价：</strong>").append(amap.get("fdeal").toString());
			// flag = flag + 1;
			// }
			if (amap.get("fpostage") != null && StringUtils.isNotBlank(amap.get("fpostage").toString())) {
				if (flag > 0) {
					info.append("<br/>");
				}
				info.append("<strong>邮费：</strong>").append(amap.get("fpostage").toString());
			}
			amap.put("priceInfo", info.toString());
			// 结算信息
			flag = 0;
			info.delete(0, info.length());
			if (amap.get("fsettlementPrice") != null
					&& StringUtils.isNotBlank(amap.get("fsettlementPrice").toString())) {
				info.append("<strong>底价：</strong>").append(amap.get("fsettlementPrice").toString());
				flag = flag + 1;
			}
			if (amap.get("fpointsPrice") != null && StringUtils.isNotBlank(amap.get("fpointsPrice").toString())) {
				if (flag > 0) {
					info.append("<br/>");
				}
				info.append("<strong>扣点：</strong>").append(new BigDecimal(amap.get("fpointsPrice").toString())
						.multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)).append("％");
			}
			amap.put("settlementInfo", info.toString());
			// 规格人数
			flag = 0;
			info.delete(0, info.length());
			if (amap.get("fadult") != null && StringUtils.isNotBlank(amap.get("fadult").toString())) {
				info.append("<strong>成人：</strong>").append(amap.get("fadult").toString()).append("人");
				flag = flag + 1;
			}
			if (amap.get("fchild") != null && StringUtils.isNotBlank(amap.get("fchild").toString())) {
				if (flag > 0) {
					info.append("<br/>");
				}
				info.append("<strong>儿童：</strong>").append(amap.get("fchild").toString()).append("人");
			}
			amap.put("number", info.toString());
			// 分销奖励金
			info.delete(0, info.length());
			if (amap.get("fdistributionRebateAmount") != null
					&& StringUtils.isNotBlank(amap.get("fdistributionRebateAmount").toString())) {
				info.append("<strong>金额：</strong>").append(amap.get("fdistributionRebateAmount").toString())
						.append("元");
			}
			if (amap.get("fdistributionRebateRatio") != null
					&& StringUtils.isNotBlank(amap.get("fdistributionRebateRatio").toString())) {
				info.append("<strong>比率：</strong>")
						.append(new BigDecimal(amap.get("fdistributionRebateRatio").toString())
								.multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP))
						.append("％");
			}
			amap.put("distributionRebate", info.toString());
		}
		return list;
	}

	@Transactional(readOnly = true)
	public TEventSpec getEventSpec(String specId) {
		return eventSpecDAO.getOne(specId);
	}

	public void saveEventSpec(Map<String, Object> valueMap) {
		TEventSpec tEventSpec = new TEventSpec();
		BeanMapper.copy(valueMap, tEventSpec);

		if (valueMap.containsKey("eventId") && StringUtils.isNotBlank(valueMap.get("eventId").toString())) {
			tEventSpec.setTEvent(new TEvent(valueMap.get("eventId").toString()));
		}
		if (valueMap.containsKey("sessionId") && StringUtils.isNotBlank(valueMap.get("sessionId").toString())) {
			tEventSpec.setTEventSession(new TEventSession(valueMap.get("sessionId").toString()));
		}
		if (valueMap.containsKey("fpointsPrice") && StringUtils.isNotBlank(valueMap.get("fpointsPrice").toString())) {
			tEventSpec.setFpointsPrice(new BigDecimal(valueMap.get("fpointsPrice").toString())
					.divide(new BigDecimal(100), new MathContext(4, RoundingMode.HALF_UP)));
		}
		// if (valueMap.containsKey("fdistributionRebateAmount")
		// &&
		// StringUtils.isNotBlank(valueMap.get("fdistributionRebateAmount").toString())
		// && valueMap.containsKey("fdistributionRebateRatio")
		// &&
		// StringUtils.isNotBlank(valueMap.get("fdistributionRebateRatio").toString()))
		// {
		//
		// throw new ServiceException("返利金额与返利比例只能填写一个请想好了在填写！");
		// }
		// if (valueMap.containsKey("fdistributionRebateAmount")
		// &&
		// StringUtils.isNotBlank(valueMap.get("fdistributionRebateAmount").toString()))
		// {
		// tEventSpec.setFdistributionRebateAmount(
		// new
		// BigDecimal(Integer.parseInt(valueMap.get("fdistributionRebateAmount").toString())));
		// }
		// if (valueMap.containsKey("fdistributionRebateRatio")
		// &&
		// StringUtils.isNotBlank(valueMap.get("fdistributionRebateRatio").toString()))
		// {
		// tEventSpec.setFdistributionRebateRatio(
		// new
		// BigDecimal(Integer.parseInt(valueMap.get("fdistributionRebateRatio").toString())
		// * 0.01));
		// }

		Date now = new Date();
		tEventSpec.setFstatus(10);
		tEventSpec.setFcreateTime(now);
		tEventSpec.setFupdateTime(now);
		eventSpecDAO.save(tEventSpec);

		TEvent tEvent = eventDAO.getOne(valueMap.get("eventId").toString());
		if (CollectionUtils.isNotEmpty(tEvent.getTEventSessions())) {
			eventDAO.saveStatus(10, tEvent.getId());
		} else {
			eventDAO.saveStatus(5, tEvent.getId());
		}
	}

	public void eidtEventSpec(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		TEventSpec db = eventSpecDAO.getOne(valueMap.get("specId").toString());

		if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
			db.setFtitle(valueMap.get("ftitle").toString());
		} else {
			db.setFtitle(null);
		}
		if (valueMap.containsKey("fdescription") && StringUtils.isNotBlank(valueMap.get("fdescription").toString())) {
			db.setFdescription(valueMap.get("fdescription").toString());
		} else {
			db.setFdescription(null);
		}
		if (valueMap.containsKey("fprice") && StringUtils.isNotBlank(valueMap.get("fprice").toString())) {
			db.setFprice(new BigDecimal(valueMap.get("fprice").toString()));
		} else {
			db.setFprice(null);
		}
		// if (valueMap.containsKey("fdeal") &&
		// StringUtils.isNotBlank(valueMap.get("fdeal").toString())) {
		// db.setFdeal(new BigDecimal(valueMap.get("fdeal").toString()));
		// } else {
		// db.setFdeal(null);
		// }
		if (valueMap.containsKey("fsettlementPrice")
				&& StringUtils.isNotBlank(valueMap.get("fsettlementPrice").toString())) {
			db.setFsettlementPrice(new BigDecimal(valueMap.get("fsettlementPrice").toString()));
		} else {
			db.setFsettlementPrice(null);
		}
		if (valueMap.containsKey("fpointsPrice") && StringUtils.isNotBlank(valueMap.get("fpointsPrice").toString())) {
			db.setFpointsPrice(new BigDecimal(valueMap.get("fpointsPrice").toString()).divide(new BigDecimal(100),
					new MathContext(4, RoundingMode.HALF_UP)));
		} else {
			db.setFpointsPrice(null);
		}
		if (valueMap.containsKey("fadult") && StringUtils.isNotBlank(valueMap.get("fadult").toString())) {
			db.setFadult(Integer.valueOf(valueMap.get("fadult").toString()));
		} else {
			db.setFadult(null);
		}
		if (valueMap.containsKey("fchild") && StringUtils.isNotBlank(valueMap.get("fchild").toString())) {
			db.setFchild(Integer.valueOf(valueMap.get("fchild").toString()));
		} else {
			db.setFchild(null);
		}
		if (valueMap.containsKey("fpostage") && StringUtils.isNotBlank(valueMap.get("fpostage").toString())) {
			db.setFpostage(new BigDecimal(valueMap.get("fpostage").toString()));
		} else {
			db.setFpostage(null);
		}
		if (valueMap.containsKey("ftotal") && StringUtils.isNotBlank(valueMap.get("ftotal").toString())) {
			db.setFtotal(Integer.valueOf(valueMap.get("ftotal").toString()));
		}
		if (valueMap.containsKey("fstock") && StringUtils.isNotBlank(valueMap.get("fstock").toString())) {
			db.setFstock(Integer.valueOf(valueMap.get("fstock").toString()));
		}
		if (valueMap.containsKey("forder") && StringUtils.isNotBlank(valueMap.get("forder").toString())) {
			db.setForder(Integer.valueOf(valueMap.get("forder").toString()));
		} else {
			db.setForder(null);
		}
		// if (valueMap.containsKey("fexternalGoodsCode")
		// &&
		// StringUtils.isNotBlank(valueMap.get("fexternalGoodsCode").toString()))
		// {
		// db.setFexternalGoodsCode(valueMap.get("fexternalGoodsCode").toString());
		// } else {
		// db.setFexternalGoodsCode(null);
		// }
		// if (valueMap.containsKey("frealNameType") &&
		// StringUtils.isNotBlank(valueMap.get("frealNameType").toString())) {
		// db.setFrealNameType(Integer.valueOf(valueMap.get("frealNameType").toString()));
		// } else {
		// db.setFrealNameType(null);
		// }
		db.setFupdateTime(new Date());
		eventSpecDAO.save(db);
	}

	public void delEventSpec(String specId) {
		TEventSpec db = eventSpecDAO.getOne(specId);
		eventSpecDAO.saveStatus(999, specId);

		TEvent tEvent = db.getTEvent();
		if (CollectionUtils.isEmpty(tEvent.getTEventSessions()) && CollectionUtils.isEmpty(tEvent.getTEventSpecs())) {
			eventDAO.saveStatus(4, tEvent.getId());
		} else if (CollectionUtils.isEmpty(tEvent.getTEventSpecs())) {
			eventDAO.saveStatus(6, tEvent.getId());
		}
		// if (tEvent.getFstockFlag().equals(20)) {
		// eventDAO.updateStockFlagBySpec(tEvent.getId());
		// }
	}

	public void copyEventSpec(String specId) {
		TEventSpec tEventSpec = new TEventSpec();
		TEventSpec db = eventSpecDAO.getOne(specId);

		tEventSpec.setTEvent(db.getTEvent());
		tEventSpec.setTEventSession(db.getTEventSession());
		tEventSpec.setFadult(db.getFadult());
		tEventSpec.setFchild(db.getFchild());
		// tEventSpec.setFdeal(db.getFdeal());
		tEventSpec.setFdescription(db.getFdescription());
		tEventSpec.setForder(db.getForder());
		tEventSpec.setFpostage(db.getFpostage());
		tEventSpec.setFprice(db.getFprice());
		tEventSpec.setFsettlementPrice(db.getFsettlementPrice());
		tEventSpec.setFpointsPrice(db.getFpointsPrice());
		tEventSpec.setFstock(db.getFstock());
		// tEventSpec.setFstockUnit(db.getFstockUnit());
		tEventSpec.setFtitle(db.getFtitle() + " 复制");
		tEventSpec.setFtotal(db.getFtotal());

		tEventSpec.setFstatus(10);
		Date now = new Date();
		tEventSpec.setFcreateTime(now);
		tEventSpec.setFupdateTime(now);
		eventSpecDAO.save(tEventSpec);
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getEventSessionList(Map<String, Object> valueMap) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.ftitle as ftitle, t.faddress as faddress, t.fstartDate as fstartDate, t.fendDate as fendDate, t.fdeadline as fdeadline, t.fsalesFlag as fsalesFlag, t.fstatus as fstatus from TEventSession t where t.TEvent.id = :eventId and t.fstatus < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("eventId", valueMap.get("eventId").toString());

		try {
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.forder desc");
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}

		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);
		// StringBuilder info = new StringBuilder();
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
			if (amap.get("frefoundPeriod") != null && StringUtils.isNotBlank(amap.get("frefoundPeriod").toString())) {
				date = (Date) amap.get("frefoundPeriod");
				amap.put("frefoundPeriod", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
			if (amap.get("fdeadline") != null && StringUtils.isNotBlank(amap.get("fdeadline").toString())) {
				date = (Date) amap.get("fdeadline");
				amap.put("fdeadline", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
			// if (amap.get("flimitationType") != null &&
			// StringUtils.isNotBlank(amap.get("flimitationType").toString())) {
			// amap.put("flimitationType",
			// DictionaryUtil.getString(DictionaryUtil.SessionFlimitationType,
			// Integer.valueOf(amap.get("flimitationType").toString()),
			// shiroUser.getLanguage()));
			// }
			if (amap.get("fsalesFlag") != null && StringUtils.isNotBlank(amap.get("fsalesFlag").toString())) {
				amap.put("fsalesFlagString", DictionaryUtil.getString(DictionaryUtil.YesNo,
						Integer.valueOf(amap.get("fsalesFlag").toString()), shiroUser.getLanguage()));
			}
		}
		return list;
	}

	@Transactional(readOnly = true)
	public TEventSession getEventSession(String sessionId) {
		return eventSessionDAO.getOne(sessionId);
	}

	public void saveEventSession(Map<String, Object> valueMap) {

		TEventSession salekyk = eventSessionDAO.findSaleFlag(valueMap.get("eventId").toString(), 1);

		if (salekyk != null) {
			if (valueMap.get("fsalesFlag").toString().equals("1")) {
				if (salekyk.getFsalesFlag() == 1) {
					throw new ServiceException("砍一砍促销已经存在,请不要在添加了");
				}
			}
		}

		TEventSession salejnh = eventSessionDAO.findSaleFlag(valueMap.get("eventId").toString(), 5);
		if (salejnh != null) {
			if (valueMap.get("fsalesFlag").toString().equals("5")) {
				if (salejnh.getFsalesFlag() == 5) {
					throw new ServiceException("嘉年华促销已经存在,请不要在添加了");
				}
			}
		}

		TEventSession tEventSession = new TEventSession();
		try {
			if (valueMap.containsKey("fstartDate") && StringUtils.isNotBlank(valueMap.get("fstartDate").toString())) {
				valueMap.put("fstartDate", DateUtils.parseDate(valueMap.get("fstartDate").toString(), "yyyy-MM-dd"));
			}
			if (valueMap.containsKey("fendDate") && StringUtils.isNotBlank(valueMap.get("fendDate").toString())) {
				valueMap.put("fendDate", DateUtils.parseDate(valueMap.get("fendDate").toString(), "yyyy-MM-dd"));
			}
			if (valueMap.containsKey("frefoundPeriod")
					&& StringUtils.isNotBlank(valueMap.get("frefoundPeriod").toString())) {
				valueMap.put("frefoundPeriod",
						DateUtils.parseDate(valueMap.get("frefoundPeriod").toString(), "yyyy-MM-dd HH:mm"));
			}
			if (valueMap.containsKey("fdeadline") && StringUtils.isNotBlank(valueMap.get("fdeadline").toString())) {
				valueMap.put("fdeadline",
						DateUtils.parseDate(valueMap.get("fdeadline").toString(), "yyyy-MM-dd HH:mm"));
			}
			if (valueMap.containsKey("fautoVerificationTime")
					&& StringUtils.isNotBlank(valueMap.get("fautoVerificationTime").toString())) {
				valueMap.put("fautoVerificationTime",
						DateUtils.parseDate(valueMap.get("fautoVerificationTime").toString(), "yyyy-MM-dd HH:mm"));
			}
		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
		BeanMapper.copy(valueMap, tEventSession);
		tEventSession.setFstatus(10);
		if (valueMap.containsKey("eventId") && StringUtils.isNotBlank(valueMap.get("eventId").toString())) {
			tEventSession.setTEvent(new TEvent(valueMap.get("eventId").toString()));
		}
		Date now = new Date();
		tEventSession.setFcreateTime(now);
		tEventSession.setFupdateTime(now);
		tEventSession = eventSessionDAO.save(tEventSession);

		// 添加自动核销时间定时器
		try {
			TEvent tEvent = tEventSession.getTEvent();
			if (tEvent.getFverificationType().intValue() == 20) {
				TTimingTask timingTask = new TTimingTask();
				timingTask.setEntityId(tEventSession.getId());
				// timingTask.setTaskTime(tEventSession.getFautoVerificationTime().getTime());
				timingTask.setTaskType(20);
				timingTaskDAO.save(timingTask);
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}

		// 自动下架时间修改
		try {
			if (tEventSession.getTEvent().getFoffSaleTime() == null) {
				eventDAO.updatetEventFoffSaleTime(tEventSession.getFdeadline(), valueMap.get("eventId").toString());
				// TTimingTask timingTask = new TTimingTask();
				// timingTask.setEntityId(valueMap.get("eventId").toString());
				// timingTask.setTaskTime(tEventSession.getFdeadline().getTime());
				// timingTask.setTaskType(2);
				// timingTaskDAO.save(timingTask);
			} else if (DateUtils.truncatedCompareTo(tEventSession.getTEvent().getFoffSaleTime(),
					tEventSession.getFdeadline(), Calendar.SECOND) < 0) {
				eventDAO.updatetEventFoffSaleTime(tEventSession.getFdeadline(), valueMap.get("eventId").toString());
				// 设置定时任务自动下架
				// timingTaskDAO.saveTaskTime(tEventSession.getFdeadline().getTime(),
				// valueMap.get("eventId").toString(),
				// 2);
			}

			if (CollectionUtils.isNotEmpty(tEventSession.getTEvent().getTEventSpecs())) {
				eventDAO.saveStatus(10, tEventSession.getTEvent().getId());
			} else {
				eventDAO.saveStatus(6, tEventSession.getTEvent().getId());
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
	}

	public void eidtEventSession(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		TEventSession db = eventSessionDAO.getOne(valueMap.get("id").toString());

		if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
			db.setFtitle(valueMap.get("ftitle").toString());
		} else {
			db.setFtitle(null);
		}
		if (valueMap.containsKey("fgps") && StringUtils.isNotBlank(valueMap.get("fgps").toString())) {
			db.setFgps(valueMap.get("fgps").toString());
		} else {
			db.setFgps(null);
		}
		if (valueMap.containsKey("faddress") && StringUtils.isNotBlank(valueMap.get("faddress").toString())) {
			db.setFaddress(valueMap.get("faddress").toString());
		} else {
			db.setFaddress(null);
		}
		try {
			if (valueMap.containsKey("fstartDate") && StringUtils.isNotBlank(valueMap.get("fstartDate").toString())) {
				Date startDate = DateUtils.parseDate(valueMap.get("fstartDate").toString(), "yyyy-MM-dd");
				if (db.getFstartDate() != null
						&& !DateUtils.truncatedEquals(db.getFstartDate(), startDate, Calendar.DAY_OF_MONTH)) {
					db.setFstartDate(startDate);
					// 定时任务先取消，不添加活动场次开始、结束、报名截止定时任务
					// TTimingTask timingTask =
					// timingTaskDAO.getByEntityIdAndTaskType(db.getId(), 5);
					// if (timingTask == null) {
					// timingTask = new TTimingTask();
					// timingTask.setEntityId(db.getId());
					// timingTask.setTaskType(5);
					// }
					// timingTask.setTaskTime(db.getFstartDate().getTime());
					// timingTaskDAO.save(timingTask);
				}
			} else {
				db.setFstartDate(null);
			}
			if (valueMap.containsKey("fendDate") && StringUtils.isNotBlank(valueMap.get("fendDate").toString())) {
				Date endDate = DateUtils.parseDate(valueMap.get("fendDate").toString(), "yyyy-MM-dd");
				if (db.getFendDate() != null
						&& !DateUtils.truncatedEquals(db.getFendDate(), endDate, Calendar.DAY_OF_MONTH)) {
					db.setFendDate(endDate);
					// 定时任务先取消，不添加活动场次开始、结束、报名截止定时任务
					// TTimingTask timingTask =
					// timingTaskDAO.getByEntityIdAndTaskType(db.getId(), 6);
					// if (timingTask == null) {
					// timingTask = new TTimingTask();
					// timingTask.setEntityId(db.getId());
					// timingTask.setTaskType(6);
					// }
					// timingTask.setTaskTime(db.getFendDate().getTime());
					// timingTaskDAO.save(timingTask);
				}
			} else {
				db.setFendDate(null);
			}
			if (valueMap.containsKey("frefoundPeriod")
					&& StringUtils.isNotBlank(valueMap.get("frefoundPeriod").toString())) {
				db.setFrefoundPeriod(
						DateUtils.parseDate(valueMap.get("frefoundPeriod").toString(), "yyyy-MM-dd HH:mm"));
			} else {
				db.setFrefoundPeriod(null);
			}
			if (valueMap.containsKey("fdeadline") && StringUtils.isNotBlank(valueMap.get("fdeadline").toString())) {
				db.setFdeadline(DateUtils.parseDate(valueMap.get("fdeadline").toString(), "yyyy-MM-dd HH:mm"));
				timingTaskDAO.saveTaskTime(db.getFdeadline().getTime(), db.getId(), 3);
			} else {
				db.setFdeadline(null);
			}
			// if (valueMap.containsKey("fautoVerificationTime")
			// &&
			// StringUtils.isNotBlank(valueMap.get("fautoVerificationTime").toString()))
			// {
			// db.setFautoVerificationTime(
			// DateUtils.parseDate(valueMap.get("fautoVerificationTime").toString(),
			// "yyyy-MM-dd HH:mm"));
			// timingTaskDAO.saveTaskTime(db.getFautoVerificationTime().getTime(),
			// db.getId(), 20);
			// } else {
			// db.setFautoVerificationTime(null);
			// }
		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException();
		}
		if (valueMap.containsKey("forder") && StringUtils.isNotBlank(valueMap.get("forder").toString())) {
			db.setForder(Integer.valueOf(valueMap.get("forder").toString()));
		} else {
			db.setForder(null);
		}
		if (valueMap.containsKey("fsalesFlag") && StringUtils.isNotBlank(valueMap.get("fsalesFlag").toString())) {
			db.setFsalesFlag(Integer.valueOf(valueMap.get("fsalesFlag").toString()));
		} else {
			db.setFsalesFlag(null);
		}

		db.setFupdateTime(new Date());

		// 自动下架时间修改
		try {
			if (db.getTEvent().getFoffSaleTime() != null && db.getFdeadline() != null) {
				if (DateUtils.truncatedCompareTo(db.getTEvent().getFoffSaleTime(), db.getFdeadline(),
						Calendar.SECOND) < 0) {
					eventDAO.updatetEventFoffSaleTime(db.getFdeadline(), db.getTEvent().getId());
					// timingTaskDAO.saveTaskTime(db.getFdeadline().getTime(),
					// db.getTEvent().getId(), 2);// 设置定时任务自动下架
				} else {
					String fdeadline = eventSessionDAO.getFdeadline(db.getTEvent().getId());
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date date = sdf.parse(fdeadline);
					eventDAO.updatetEventFoffSaleTime(date, db.getTEvent().getId());
				}
			} else {
				String fdeadline = eventSessionDAO.getFdeadline(db.getTEvent().getId());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = sdf.parse(fdeadline);
				eventDAO.updatetEventFoffSaleTime(date, db.getTEvent().getId());
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}

		db = eventSessionDAO.save(db);

		// 添加自动核销时间定时器
		// try {
		// TEvent tEvent = eventDAO.findOne(db.getTEvent().getId());
		// if (tEvent.getFverificationType().intValue() == 20) {
		// TTimingTask timetask =
		// timingTaskDAO.getByEntityIdAndTaskType(db.getId(), 20);
		// if (timetask == null) {
		// TTimingTask timingTask = new TTimingTask();
		// timingTask.setEntityId(db.getId());
		// timingTask.setTaskTime(db.getFautoVerificationTime().getTime());
		// timingTask.setTaskType(20);
		// timingTaskDAO.save(timingTask);
		// } else {
		// timingTaskDAO.saveTaskTime(db.getFautoVerificationTime().getTime(),
		// db.getId(), 20);
		// }
		// }
		// } catch (Exception e) {
		// logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		// }

	}

	public void delEventSession(String sessionId) {

		TEventSession db = eventSessionDAO.getOne(sessionId);
		eventSessionDAO.saveStatus(999, sessionId);

		// 修改最新的活动下架时间
		try {
			if (db.getTEvent().getFoffSaleTime() != null && db.getFdeadline() != null) {
				// if
				// (DateUtils.truncatedCompareTo(db.getTEvent().getFoffSaleTime(),
				// db.getFdeadline(),
				// Calendar.SECOND) == 0) {
				String fdeadline = eventSessionDAO.getFdeadline(db.getTEvent().getId());
				if (fdeadline == null) {
					Date now = new Date();
					eventDAO.updatetEventFoffSaleTime(now, db.getTEvent().getId());
					// timingTaskDAO.saveTaskTime(now.getTime(),
					// db.getTEvent().getId(), 2);// 设置定时任务自动下架
				} else {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date date = sdf.parse(fdeadline);
					eventDAO.updatetEventFoffSaleTime(date, db.getTEvent().getId());
					// timingTaskDAO.saveTaskTime(date.getTime(),
					// db.getTEvent().getId(), 2);// 设置定时任务自动下架
				}
			}

		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
		// if(db.getTEvent().getFoffSaleTime()==null){
		// String fdeadline =
		// eventSessionDAO.getFdeadline(db.getTEvent().getId());
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Date date = sdf.parse(fdeadline);
		// eventDAO.updatetEventFoffSaleTime(date, db.getTEvent().getId());
		// }else
		// if(DateUtils.truncatedCompareTo(db.getTEvent().getFoffSaleTime(),
		// db.getFdeadline(), Calendar.SECOND) == 0){
		// String fdeadline =
		// eventSessionDAO.getFdeadline(db.getTEvent().getId());
		// if(fdeadline==null){
		// Date now = new Date();
		// eventDAO.updatetEventFoffSaleTime(now, db.getTEvent().getId());
		// }else{
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Date date = sdf.parse(fdeadline);
		// eventDAO.updatetEventFoffSaleTime(date, db.getTEvent().getId());
		// }
		// }

		// 定时任务先取消，不添加活动场次开始、结束、报名截止定时任务
		// timingTaskDAO.clearTimeTaskByEntityId(sessionId);

		TEvent tEvent = db.getTEvent();
		if (CollectionUtils.isEmpty(tEvent.getTEventSessions()) && CollectionUtils.isEmpty(tEvent.getTEventSpecs())) {
			eventDAO.saveStatus(4, tEvent.getId());
		} else if (CollectionUtils.isEmpty(tEvent.getTEventSessions())) {
			eventDAO.saveStatus(5, tEvent.getId());
		}
		// if (tEvent.getFstockFlag().equals(10)) {
		// eventDAO.updateStockFlagBySession(tEvent.getId());
		// }
	}

	public void copyEventSession(String sessionId) {
		TEventSession tEventSession = new TEventSession();
		TEventSession db = eventSessionDAO.getOne(sessionId);

		tEventSession.setTEvent(db.getTEvent());
		tEventSession.setFaddress(db.getFaddress());
		tEventSession.setFdeadline(db.getFdeadline());
		tEventSession.setFendDate(db.getFendDate());
		tEventSession.setFgps(db.getFgps());
		tEventSession.setForder(db.getForder());
		tEventSession.setFrefoundPeriod(db.getFrefoundPeriod());
		// tEventSession.setFautoVerificationTime(db.getFautoVerificationTime());
		tEventSession.setFstartDate(db.getFstartDate());
		tEventSession.setFtitle(db.getFtitle() + " 复制");
		tEventSession.setFstatus(10);
		Date now = new Date();
		tEventSession.setFcreateTime(now);
		tEventSession.setFupdateTime(now);
		eventSessionDAO.save(tEventSession);
	}

	public void copyEventSpecSessionId(String sessionId) {

		// 复制该场次
		TEventSession tEventSession = new TEventSession();
		TEventSession db = eventSessionDAO.getOne(sessionId);

		tEventSession.setTEvent(db.getTEvent());
		tEventSession.setFaddress(db.getFaddress());
		tEventSession.setFdeadline(db.getFdeadline());
		tEventSession.setFendDate(db.getFendDate());
		tEventSession.setFgps(db.getFgps());
		tEventSession.setForder(db.getForder());
		tEventSession.setFrefoundPeriod(db.getFrefoundPeriod());
		// tEventSession.setFautoVerificationTime(db.getFautoVerificationTime());
		tEventSession.setFstartDate(db.getFstartDate());
		tEventSession.setFtitle(db.getFtitle() + "'复制场次规格'");
		tEventSession.setFstatus(10);
		Date now = new Date();
		tEventSession.setFcreateTime(now);
		tEventSession.setFupdateTime(now);
		tEventSession = eventSessionDAO.save(tEventSession);

		// 复制该场次规格
		List<TEventSpec> teventSpecList = eventSpecDAO.getTEventSpec(sessionId);
		TEventSpec tEventSpec = null;
		for (TEventSpec amap : teventSpecList) {
			tEventSpec = new TEventSpec();

			tEventSpec.setTEvent(amap.getTEvent());
			tEventSpec.setTEventSession(new TEventSession(tEventSession.getId()));
			tEventSpec.setFtitle(amap.getFtitle());
			tEventSpec.setFdescription(amap.getFdescription());
			tEventSpec.setFprice(amap.getFprice());
			// tEventSpec.setFdeal(amap.getFdeal());
			tEventSpec.setFsettlementPrice(amap.getFsettlementPrice());
			tEventSpec.setFpointsPrice(amap.getFpointsPrice());
			tEventSpec.setFadult(amap.getFadult());
			tEventSpec.setFchild(amap.getFchild());
			tEventSpec.setForder(amap.getForder());
			tEventSpec.setFpostage(amap.getFpostage());
			tEventSpec.setFtotal(amap.getFtotal());
			tEventSpec.setFstock(amap.getFstock());
			// tEventSpec.setFexternalGoodsCode(amap.getFexternalGoodsCode());
			// tEventSpec.setFrealNameType(amap.getFrealNameType());
			// tEventSpec.setFdistributionRebateAmount(amap.getFdistributionRebateAmount());
			// tEventSpec.setFdistributionRebateRatio(amap.getFdistributionRebateRatio());
			tEventSpec.setFstatus(10);
			tEventSpec.setFcreateTime(now);
			tEventSpec.setFupdateTime(now);
			eventSpecDAO.save(tEventSpec);
		}
	}

	// public void eventStockStatistical() {
	// String hql = "select t.id as id from TEvent t where t.fstatus < 999";
	// List<Map<String, Object>> list = commonService.find(hql,
	// Maps.newHashMap());
	// for (Map<String, Object> amap : list) {
	// eventDAO.updateStockFlagBySpec(amap.get("id").toString());
	// }
	// }

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getAppChannelList(Map<String, Object> valueMap) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, acs.fcode as fcode, acs.ftitle as ftitle, acs.fcity as fcity, acs.forder as forder, acs.fisVisible as fisVisible, t.forder as forder2, t.fpublishTime as fpublishTime from TAppChannelEvent t inner join t.TAppChannelSetting acs where t.TEvent.id = :eventId");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("eventId", valueMap.get("eventId").toString());
		try {
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
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		Date date = null;
		for (Map<String, Object> amap : list) {
			if (amap.get("fcity") != null && StringUtils.isNotBlank(amap.get("fcity").toString())) {
				amap.put("fcity", DictionaryUtil.getString(DictionaryUtil.City,
						Integer.valueOf(amap.get("fcity").toString()), shiroUser.getLanguage()));
			}
			if (amap.get("fisVisible") != null && StringUtils.isNotBlank(amap.get("fisVisible").toString())) {
				amap.put("fisVisible", DictionaryUtil.getString(DictionaryUtil.YesNo,
						Integer.valueOf(amap.get("fisVisible").toString()), shiroUser.getLanguage()));
			}
			if (amap.get("fpublishTime") != null && StringUtils.isNotBlank(amap.get("fpublishTime").toString())) {
				date = (Date) amap.get("fpublishTime");
				amap.put("fpublishTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
		}
		return list;
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getAppChannelListByEventId(Map<String, Object> valueMap) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.fcode as fcode, t.ftitle as ftitle, t.fcity as fcity, t.forder as forder, t.fisVisible as fisVisible from TAppChannelSetting t ")
				.append(" where t.id not in (select distinct(a.id) from TAppChannelSetting a inner join a.TAppChannelEvents ace where ace.TEvent.id = :eventId) order by t.fcity, t.forder");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("eventId", valueMap.get("eventId").toString());
		try {
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}

		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		for (Map<String, Object> amap : list) {
			if (amap.get("fcity") != null && StringUtils.isNotBlank(amap.get("fcity").toString())) {
				amap.put("fcity", DictionaryUtil.getString(DictionaryUtil.City,
						Integer.valueOf(amap.get("fcity").toString()), shiroUser.getLanguage()));
			}
			if (amap.get("fisVisible") != null && StringUtils.isNotBlank(amap.get("fisVisible").toString())) {
				amap.put("fisVisible", DictionaryUtil.getString(DictionaryUtil.YesNo,
						Integer.valueOf(amap.get("fisVisible").toString()), shiroUser.getLanguage()));
			}
		}
		return list;
	}

	/**
	 * 保存栏目关联信息
	 * 
	 * @param valueMap
	 */
	public void eventAssociate(Map<String, Object> valueMap) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		String eventId = valueMap.get("eventId").toString();
		if (valueMap.get("channelId") != null && StringUtils.isNotBlank(valueMap.get("channelId").toString())) {
			appChannelEventDAO.deleteByEventId(eventId);
		}
		String[] channelIds = null;
		if (valueMap.containsKey("channelId") && ObjectUtils.isArray(valueMap.get("channelId"))) {
			channelIds = (String[]) valueMap.get("channelId");
		} else {
			channelIds = ArrayUtils.toArray((valueMap.get("channelId").toString()));
		}

		TAppChannelEvent tAppChannelEvent = null;
		Date now = new Date();
		TEvent tEvent = eventDAO.findOne(eventId);
		TColumnBanner tColumnBanner = columnBannerDAO.findByType(1, 1);
		for (int j = 0; j < channelIds.length; j++) {
			if (channelIds[j].equals("")) {
				throw new ServiceException("当前关联栏目的ID是空的,请F5刷新页面后在提交！");
			} else {
				/*
				 * if (tEvent.getFgoodsTag().intValue() == 0) { if
				 * (!tSeckilltime.getFchannelId().equals(channelIds[j])) { throw
				 * new ServiceException("当前商品只能发布到白菜栏目！"); } } else if
				 * (tEvent.getFgoodsTag().intValue() != 0) { if
				 * (tSeckilltime.getFchannelId().equals(channelIds[j])) { throw
				 * new ServiceException("当前商品不能发布到白菜栏目！"); } }
				 */
				tAppChannelEvent = new TAppChannelEvent();
				tAppChannelEvent.setTEvent(new TEvent(eventId));
				tAppChannelEvent.setTAppChannelSetting(new TAppChannelSetting(channelIds[j]));
				tAppChannelEvent.setForder(0);
				tAppChannelEvent.setFpublishTime(now);
				tAppChannelEvent.setFpublisher(shiroUser.getId());
				appChannelEventDAO.save(tAppChannelEvent);
			}
		}
	}

	public void delEventAssociate(String channelEventId) {
		appChannelEventDAO.delete(channelEventId);
	}

	public void saveEventOrder(String channelEventId, Integer forder) {
		appChannelEventDAO.updateForder(channelEventId, forder);
	}

	@Transactional(readOnly = true)
	public String getEventInfo(String eventId) {
		StringBuilder info = new StringBuilder();
		TEvent tEvent = eventDAO.getOne(eventId);
		info.append("<strong>活动名称：</strong>").append(tEvent.getFtitle());
		info.append("<br/>").append("<strong>商家名称：</strong>").append(tEvent.getTSponsor().getFname());
		info.append("<br/>").append("<strong>商家电话：</strong>").append(tEvent.getTSponsor().getFphone());
		return info.toString();
	}

	public void eventOnOffSale(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		String eventId = valueMap.get("eventId").toString();
		TEvent tEvent = eventDAO.getOne(eventId);

		Date fonSaleTime = null;
		Date foffSaleTime = null;
		try {
			if (valueMap.get("fonSaleTime") != null && StringUtils.isNotBlank(valueMap.get("fonSaleTime").toString())) {
				fonSaleTime = DateUtils.parseDate(valueMap.get("fonSaleTime").toString(), "yyyy-MM-dd HH:mm");
				// 避免重复提交上下架任务记录，先判断一下是否已经有了上下架任务记录
				if (tEvent.getFstatus().intValue() != 20) {
					TTimingTask tEventTimingTask = timingTaskDAO.getByEntityIdAndTaskType(eventId, 1);
					if (tEventTimingTask == null) {
						tEventTimingTask = new TTimingTask();
						tEventTimingTask.setEntityId(eventId);
						tEventTimingTask.setTaskType(1);
					}
					tEventTimingTask.setTaskTime(fonSaleTime.getTime());
					timingTaskDAO.save(tEventTimingTask);
				}
			}
			if (valueMap.get("foffSaleTime") != null
					&& StringUtils.isNotBlank(valueMap.get("foffSaleTime").toString())) {
				foffSaleTime = DateUtils.parseDate(valueMap.get("foffSaleTime").toString(), "yyyy-MM-dd HH:mm");
				// 避免重复提交上下架任务记录，先判断一下是否已经有了上下架任务记录
				if (tEvent.getFstatus().intValue() != 90) {
					TTimingTask tEventTimingTask2 = timingTaskDAO.getByEntityIdAndTaskType(eventId, 2);
					if (tEventTimingTask2 == null) {
						tEventTimingTask2 = new TTimingTask();
						tEventTimingTask2.setEntityId(eventId);
						tEventTimingTask2.setTaskType(2);
					}
					tEventTimingTask2.setTaskTime(foffSaleTime.getTime());
					timingTaskDAO.save(tEventTimingTask2);
				}
			}
			eventDAO.saveOOTime(fonSaleTime, foffSaleTime, eventId);
		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException();
		}
	}

	/**
	 * 商品上架
	 * 
	 */
	public void onSale(String eventId) {
		eventDAO.saveOnTimeAndStatus(new Date(), 20, eventId);

		// 上架商品加入缓存
		this.goodsCatch(eventId, OnSaleRedis);
		TEvent tEvent = eventDAO.getOne(eventId);

		// 参数生成图片存进商品表
		String imagePath = systemService.getGoodsSpecValue(eventId);

		eventDAO.updateGoodsSpec(eventId, imagePath);

		// 将商品的所有字段加入solr库
		GoodsSolrDTO goodsDTO = new GoodsSolrDTO();

		goodsDTO.setGoodsId(tEvent.getId());
		goodsDTO.setImageUrl(fxlService.getImageUrl(tEvent.getFimage1(), false));

		String typeB = getCategoryAName(tEvent.getFtypeB());
		goodsDTO.setType(typeB != null ? typeB : StringUtils.EMPTY);

		goodsDTO.setSpec(tEvent.getFspec());
		goodsDTO.setGoodsTitle(tEvent.getFtitle());
		goodsDTO.setOriginalPrice(tEvent.getFprice().toString());
		goodsDTO.setPresentPrice(tEvent.getFpriceMoney().toString());
		goodsDTO.setDesc(tEvent.getFbrief());
		goodsDTO.setSponsorName(tEvent.getTSponsor().getFname());
		if (tEvent.getFlimitation().intValue() > 0) {
			goodsDTO.setStockFlag(1);
		} else {
			goodsDTO.setStockFlag(0);
		}
		goodsDTO.setStatus(tEvent.getFstatus());
		goodsDTO.setStatusString(DictionaryUtil.getString(DictionaryUtil.EventStatus, tEvent.getFstatus()));
		goodsDTO.setSellModel(tEvent.getFsellModel());
		goodsDTO.setSpecModel(tEvent.getFspecModel());
		goodsDTO.setPromotionModel(tEvent.getFpromotionModel());
		goodsDTO.setLimitation(tEvent.getFlimitation());
		goodsDTO.setSdealsModel(tEvent.getFsdealsModel());
		if (tEvent.getFstock() != null) {
			if (tEvent.getFstock().intValue() <= 0) {
				goodsDTO.setIfInStock(false);
			}
		}
		goodsDTO.setPercentage(tEvent.getFstock() * 100 / tEvent.getFtotal());
		goodsDTO.setSaleTotal(tEvent.getFsaleTotal());
		if (tEvent.getFusePreferential() != null && tEvent.getFusePreferential() == 1) {
			goodsDTO.setUseCoupon(false);
		}
		goodsDTO.setCreateTime(tEvent.getFonSaleTime());
		TEventBargaining tEventBargaining = eventBargainingDAO.getByEventId(eventId);
		goodsDTO.setBargainingId(tEventBargaining == null ? "" : tEventBargaining.getId());

		SolrUtil.solrSave(goodsDTO);

		// 判断商品的参数是否设置,无,不可上架
		List<TGoodsSpaceValue> specList = goodsSpaceValueDAO.getSpecList(eventId);
		if (specList == null || specList.size() <= 0) {
			throw new ServiceException("当前商品没有设置规格和包装参数,请完善信息！");
		}
		// 判断商品是否有库存.无库存不可上架
		List<TGoodsSku> skuList = goodsSkuDAO.findGoodsListSku(eventId);
		if (skuList == null || skuList.size() <= 0) {
			throw new ServiceException("当前商品没有设置库存,请完善信息！");
		} else if (null != skuList && skuList.size() > 0) {// 查看是否有库存默认显示
			int count = 0;
			// 判断是否有默认显示的sku
			for (TGoodsSku sku : skuList) {
				Integer flag = sku.getFlag();
				if (sku.getFimage() == null) {
					throw new ServiceException("该产品有sku未上传主图,请完善信息！");
				}
				if (flag == 0) {
					count++;
				}
			}
			if (count == 0) {
				throw new ServiceException("该产品没有设置默认显示sku,请完善信息！");
			}
			if (count > 1) {
				throw new ServiceException("商品只能设置一个默认sku,该商品设置了" + count + "个默认显示信息,请重新确认信息");
			}
		}
		// 商品上架前校验 商品主图是否存在
		TEvent tEventImage = eventDAO.getImageA(eventId);
		if (tEventImage.getFimage1() == null) {
			throw new ServiceException("当前商品没有主图信息,请完善信息！");
		}

	}

	public void offSale(String eventId) {
		eventDAO.saveOffTimeAndStatus(90, eventId);// 即刻下架

		// 将商品id从缓存中删除
		this.goodsCatch(eventId, OffSaleRedis);

		// 将solr中的该商品信息删除
		SolrUtil.deleteSolrValue(eventId);

	}

	@Transactional(readOnly = true)
	public TImage getImage(Long id) {
		return imageDAO.getOne(id);
	}

	public void createEventDetailHtml() {
		StringBuilder hql = new StringBuilder();
		hql.append("select t.id as id, t.fdetail as fdetail from TEvent t where t.fstatus = 20");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		String id = null;
		for (Map<String, Object> amap : list) {
			id = amap.get("id").toString();
			String relativePath = new StringBuilder("/event/").append(DateFormatUtils.format(new Date(), "yyyy-MM-dd"))
					.append("/").append(id).append(".html").toString();

			// 创建异步页面静态化Bean
			StaticHtmlBean staticHtmlBean = new StaticHtmlBean();
			staticHtmlBean.setObjectId(id);
			staticHtmlBean.setTemplateName(Constant.EventDetilTemplate);
			staticHtmlBean.setRelativePath(relativePath);

			hql.delete(0, hql.length());
			hql.append(
					"select t.ftype as ftype, t.fcontent as fcontent, t.ftypeicon as ftypeicon from TEventDetail t where t.TEvent.id = :eventId and t.fcontent is not null and t.fcontent != '' order by t.forder asc");
			hqlMap.clear();
			hqlMap.put("eventId", id);

			List<Map<String, Object>> lista = commonService.find(hql.toString(), hqlMap);
			List<EventDetailDTO> eventDetailList = Lists.newArrayList();
			EventDetailDTO eventDetailDTO = null;
			for (Map<String, Object> bmap : lista) {
				eventDetailDTO = new EventDetailDTO();
				eventDetailDTO.setImageUrl(new StringBuilder().append(PropertiesUtil.getProperty("fileServerUrl"))
						.append(DictionaryUtil.getString(DictionaryUtil.EventIcon, (Integer) bmap.get("ftypeicon")))
						.toString());
				eventDetailDTO.setTitle(
						DictionaryUtil.getString(DictionaryUtil.EventTypeDetail, (Integer) bmap.get("ftype")));

				String[] contents = bmap.get("fcontent").toString().split("#FXL#");
				if (contents.length > 1) {
					if (StringUtils.isBlank(contents[0])) {
						contents = ArrayUtils.remove(contents, 0);
					}
				}
				eventDetailDTO.setContent(contents);
				eventDetailList.add(eventDetailDTO);
			}

			Map<String, Object> map = Maps.newHashMap();
			if (amap.get("fdetail") != null) {
				// 获取到html详情
				String html = amap.get("fdetail").toString();
				// 将img标签的src属性替换成懒加载url
				html = StringUtils.replace(html, "<img src=",
						"<img src=\"http://file.021-sdeals.cn/image/czyh.png\" data-layzr=");

				map.put("detailHtml", html);
			}
			if (CollectionUtils.isNotEmpty(lista)) {
				map.put("eventDetailList", eventDetailList);
			}
			staticHtmlBean.setContentMap(map);
			// 异步生成活动静态页面，不占用本线程的执行时间
			StaticHtmlManager.put(staticHtmlBean);
			eventDAO.updateFdetailHtmlUrl(id, relativePath);
		}
	}

	@Transactional(readOnly = true)
	public Map<Integer, String> getCategoryMapA() {
		List<TEventCategory> list = eventCategoryDAO.findByLevel(1);
		Map<Integer, String> categoryMapA = Maps.newHashMap();
		for (TEventCategory tEventCategory : list) {
			categoryMapA.put(tEventCategory.getValue(), tEventCategory.getName());
		}
		return categoryMapA;
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getCategoryMapB(long parentId) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.value as key, t.id as categoryBId,t.name as value from TEventCategory t where t.level = 2 and t.parentId = :parentId");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("parentId", parentId);
		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);
		return list;
	}

	@Transactional(readOnly = true)
	public Map<String, String> getChannelSetting() {
		List<TAppChannelSetting> list = appChannelSettingDAO.findByCode();
		Map<String, String> channelSettingMapA = Maps.newHashMap();
		for (TAppChannelSetting tAppChannelSetting : list) {
			channelSettingMapA.put(tAppChannelSetting.getId(), tAppChannelSetting.getFtitle());
		}
		return channelSettingMapA;
	}

	public int clearTempEvent() {
		return eventDAO.clearTempEvent(DateUtils.addDays(new Date(), -1));
	}

	public int clearTempImage() {
		return imageDAO.clearTempImage(1);
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getTEventDetailList(String eventId) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as id,t.ftype as ftype, t.fcontent as fcontent,t.ftypeicon as ftypeicon from TEventDetail t where t.TEvent.id = :eventId order by t.forder asc");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("eventId", eventId);
		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);
		return list;
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getTEventTyppeList(String eventId) {
		StringBuilder hql = new StringBuilder();
		hql.append("select t.ftype as ftype from TEventDetail t where t.TEvent.id = :eventId");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("eventId", eventId);
		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		return list;
	}

	@Transactional(readOnly = true)
	public Map<Integer, List<Map<String, Object>>> getTAppChannelSettingList(Integer fcity) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.ftitle as ftitle, t.fallEvent as fallEvent, t.ftype as ftype from TAppChannelSetting t where t.ftype in (1,2,3) and t.fcity = :fcity order by t.forder");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("fcity", fcity);
		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		List<Map<String, Object>> tappChannelOneList = Lists.newArrayList();
		List<Map<String, Object>> tappChannelTwoList = Lists.newArrayList();
		List<Map<String, Object>> tappChannelThreeList = Lists.newArrayList();
		int type = 0;
		for (Map<String, Object> map : list) {
			type = ((Integer) map.get("ftype")).intValue();
			switch (type) {
			case 1: {
				tappChannelOneList.add(map);
				break;
			}
			case 2: {
				tappChannelTwoList.add(map);
				break;
			}
			case 3: {
				tappChannelThreeList.add(map);
				break;
			}
			default: {
				break;
			}
			}
		}

		Map<Integer, List<Map<String, Object>>> map = Maps.newHashMap();
		map.put(1, tappChannelOneList);
		map.put(2, tappChannelTwoList);
		map.put(3, tappChannelThreeList);
		return map;
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getChannelEventList(String eventId) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId,t.TAppChannelSetting.id as fchannelID,t.TAppChannelSetting.fallEvent as fallEvent,t.forder as forder,t.fpublishTime as fpublishTime,t.fpublisher as fpublisher from TAppChannelEvent t where t.TEvent.id = :eventId");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("eventId", eventId);
		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		return list;
	}

	public void addEventTexInfo(Map<String, Object> valueMap) {

		TEventExtInfo tEventExtInfo = new TEventExtInfo();
		tEventExtInfo.setFeventId(valueMap.get("eventId").toString());
		tEventExtInfo.setFname(valueMap.get("fname").toString());
		tEventExtInfo.setFprompt(valueMap.get("fprompt").toString());
		tEventExtInfo.setFisRequired(Integer.valueOf(valueMap.get("fisRequired").toString()));
		tEventExtInfo.setFisEveryone(Integer.valueOf(valueMap.get("fisEveryone").toString()));
		tEventExtInfo.setForder(1);
		tEventExtInfo.setFstatus(10);
		eventExtInfoDAO.save(tEventExtInfo);
	}

	public void editEventTexInfo(String id, Integer forder, String fprompt, String fname, String fisRequired,
			String fisEveryone) {
		// eventExtInfoDAO.updateForder(id, forder,
		// fprompt,fname,fisRequired,fisEveryone);
		TEventExtInfo tEventExtInfo = eventExtInfoDAO.getOne(id);
		tEventExtInfo.setFname(fname);
		tEventExtInfo.setFprompt(fprompt);
		if (fisRequired.equals("是")) {
			tEventExtInfo.setFisRequired(1);
		} else {
			tEventExtInfo.setFisRequired(0);
		}
		if (fisRequired.equals("是")) {
			tEventExtInfo.setFisEveryone(1);
		} else {
			tEventExtInfo.setFisEveryone(0);
		}
		tEventExtInfo.setForder(forder);
		eventExtInfoDAO.save(tEventExtInfo);
	}

	public void getTEventExtInfo(Map<String, Object> valueMap, CommonPage page, String eventId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();

		hql.append(
				"select t.id as DT_RowId, t.fname as fname, t.fprompt as fprompt, t.fisRequired as fisRequired, t.fisEveryone as fisEveryone, t.forder as forder from TEventExtInfo t where t.feventId = :eventId and t.fstatus < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hql.append(" order by t.forder desc");
		hqlMap.put("eventId", eventId);
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		for (Map<String, Object> amap : list) {

			if (amap.get("fname") != null && StringUtils.isNotBlank(amap.get("fname").toString())) {
				amap.put("fname", amap.get("fname"));
			}
			if (amap.get("fprompt") != null && StringUtils.isNotBlank(amap.get("fprompt").toString())) {
				amap.put("fprompt", amap.get("fprompt"));
			}

			if (amap.get("fisRequired") != null && StringUtils.isNotBlank(amap.get("fisRequired").toString())) {
				amap.put("fisRequired", DictionaryUtil.getString(DictionaryUtil.YesNo,
						((Integer) amap.get("fisRequired")), shiroUser.getLanguage()));
			}
			if (amap.get("fisEveryone") != null && StringUtils.isNotBlank(amap.get("fisEveryone").toString())) {
				amap.put("fisEveryone", DictionaryUtil.getString(DictionaryUtil.YesNo,
						((Integer) amap.get("fisEveryone")), shiroUser.getLanguage()));
			}
		}
	}

	/**
	 * 获取获取个性化设置活动列表
	 * 
	 * @param valueMap
	 * @return
	 */
	@Transactional(readOnly = true)
	public void getEventPersonalizedList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.ftitle as ftitle, s.fname as fsponsor, t.ftypeA as ftypeA, t.forderType as forderType, t.fbdId as fbdId, t.fcreaterId as fcreaterId, t.fonSaleTime as fonSaleTime, t.foffSaleTime as foffSaleTime, t.fstatus as fstatus,t.ftag as ftag,t.fbaseScore as fbaseScore from TEvent t left join t.TSponsor s");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_fappChannelId")
					&& StringUtils.isNotBlank(valueMap.get("s_fappChannelId").toString())) {
				hql.append(
						" inner join t.TAppChannels a where t.fstatus < 999 and a.TAppChannelSetting.id = :s_fappChannelId ");
				hqlMap.put("s_fappChannelId", valueMap.get("s_fappChannelId").toString());
			} else {
				hql.append(" where t.fstatus < 999");
			}
			if (valueMap.containsKey("s_ftitle") && StringUtils.isNotBlank(valueMap.get("s_ftitle").toString())) {
				hql.append(" and t.ftitle like :s_ftitle ");
				hqlMap.put("s_ftitle", "%" + valueMap.get("s_ftitle").toString() + "%");
			}
			if (valueMap.containsKey("s_fbdId") && StringUtils.isNotBlank(valueMap.get("s_fbdId").toString())) {
				hql.append(" and t.fbdId = :s_fbdId ");
				hqlMap.put("s_fbdId", Long.valueOf(valueMap.get("s_fbdId").toString()));
			}
			if (valueMap.containsKey("s_fcreaterId")
					&& StringUtils.isNotBlank(valueMap.get("s_fcreaterId").toString())) {
				hql.append(" and t.fcreaterId = :s_fcreaterId ");
				hqlMap.put("s_fcreaterId", Long.valueOf(valueMap.get("s_fcreaterId").toString()));
			}
			if (valueMap.containsKey("s_fsponsor") && StringUtils.isNotBlank(valueMap.get("s_fsponsor").toString())) {
				hql.append(" and s.id = :s_fsponsor ");
				hqlMap.put("s_fsponsor", valueMap.get("s_fsponsor").toString());
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
			if (valueMap.containsKey("statusBegin") && StringUtils.isNotBlank(valueMap.get("statusBegin").toString())) {
				hql.append(" and t.fstatus >= :statusBegin ");
				hqlMap.put("statusBegin", Integer.valueOf(valueMap.get("statusBegin").toString()));
			}
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				if (valueMap.containsKey("s_fappChannelId")
						&& StringUtils.isNotBlank(valueMap.get("s_fappChannelId").toString())) {
					hql.append(" order by a.forder desc");
				} else {
					hql.append(" order by t.fcreateTime desc");
				}
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		Date date = null;
		for (Map<String, Object> amap : list) {
			if (amap.get("fdeal") != null && StringUtils.isNotBlank(amap.get("fdeal").toString())) {
				amap.put("fdeal", amap.get("fdeal").toString());
			} else {
				amap.put("fdeal", "免费");
			}
			if (amap.get("forderType") != null && StringUtils.isNotBlank(amap.get("forderType").toString())) {
				amap.put("forderType", DictionaryUtil.getString(DictionaryUtil.OrderType,
						((Integer) amap.get("forderType")), shiroUser.getLanguage()));
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatusString", DictionaryUtil.getString(DictionaryUtil.EventStatus,
						((Integer) amap.get("fstatus")), shiroUser.getLanguage()));
			}
			if (amap.get("fbdId") != null && StringUtils.isNotBlank(amap.get("fbdId").toString())) {
				amap.put("fbdId", userDAO.getOne((Long) amap.get("fbdId")).getRealname());
			}
			if (amap.get("fcreaterId") != null && StringUtils.isNotBlank(amap.get("fcreaterId").toString())) {
				amap.put("fcreaterId", userDAO.getOne((Long) amap.get("fcreaterId")).getRealname());
			}
			if (amap.get("fonSaleTime") != null && StringUtils.isNotBlank(amap.get("fonSaleTime").toString())) {
				date = (Date) amap.get("fonSaleTime");
				amap.put("fonSaleTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
			}
			if (amap.get("foffSaleTime") != null && StringUtils.isNotBlank(amap.get("foffSaleTime").toString())) {
				date = (Date) amap.get("foffSaleTime");
				amap.put("foffSaleTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
			}
		}
	}

	public void addPersonalizedTag(Map<String, Object> valueMap) {
		if (valueMap.containsKey("eventId") && StringUtils.isNotBlank(valueMap.get("eventId").toString())) {
			TEvent db = eventDAO.getOne(valueMap.get("eventId").toString());

			if (valueMap.containsKey("ftag") && StringUtils.isNotBlank(valueMap.get("ftag").toString())) {
				String[] ids = ArrayUtils.EMPTY_STRING_ARRAY;
				if (valueMap.containsKey("ftag") && ObjectUtils.isArray(valueMap.get("ftag"))) {
					ids = (String[]) valueMap.get("ftag");
				} else {
					ids = ArrayUtils.toArray((valueMap.get("ftag").toString()));
				}
				String tagInfo = ArrayStringUtils.arrayToString(ids, ArrayStringUtils.Separator);
				db.setFtag(tagInfo);
			} else {
				db.setFtag(null);
			}

			if (valueMap.containsKey("fbaseScore") && StringUtils.isNotBlank(valueMap.get("fbaseScore").toString())) {
				db.setFbaseScore(Integer.valueOf(valueMap.get("fbaseScore").toString()));
			} else {
				db.setFbaseScore(null);
			}

			db.setFupdateTime(new Date());
			eventDAO.save(db);
		} else {
			throw new ServiceException("本操作缺少活动ID信息，保存出现了错误！");
		}
	}

	public void deleteEventExtInfo(String extInfoId) {
		eventExtInfoDAO.deleteTEventExtInfo(999, extInfoId);
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getEventDetailList() {
		return commonService.find("select t.id as key, t.ftitle as value from TEvent t where t.fstatus = 20");
	}

	public void addRelationRecommend(Map<String, Object> valueMap) {

		TEventRelation db = null;

		List<TEventRelation> eventRelationList = eventRelationDAO
				.getEventRelationList(valueMap.get("eventId").toString());

		if (eventRelationList != null) {
			String[] ids = ArrayUtils.EMPTY_STRING_ARRAY;
			if (valueMap.containsKey("fbyEventId") && ObjectUtils.isArray(valueMap.get("fbyEventId"))) {
				ids = (String[]) valueMap.get("fbyEventId");
			} else {
				ids = ArrayUtils.toArray((valueMap.get("fbyEventId").toString()));
			}
			if (eventRelationList.size() >= 5) {
				for (int i = 0; i < eventRelationList.size() - (eventRelationList.size() - ids.length); i++) {

					eventRelationDAO.deleteEventRelation(eventRelationList.get(i).getId());
				}

				for (int i = 0; i < ids.length; i++) {
					db = new TEventRelation();
					db.setFeventId(valueMap.get("eventId").toString());
					db.setFbyEventId(ids[i]);
					db.setForder(i + 1);
					db.setFcreateTime(new Date());
					eventRelationDAO.save(db);
				}
			}
		} else {

			if (valueMap.containsKey("eventId") && StringUtils.isNotBlank(valueMap.get("eventId").toString())) {
				if (valueMap.containsKey("fbyEventId")
						&& StringUtils.isNotBlank(valueMap.get("fbyEventId").toString())) {

					String[] ids = ArrayUtils.EMPTY_STRING_ARRAY;
					if (valueMap.containsKey("fbyEventId") && ObjectUtils.isArray(valueMap.get("fbyEventId"))) {
						ids = (String[]) valueMap.get("fbyEventId");
					} else {
						ids = ArrayUtils.toArray((valueMap.get("fbyEventId").toString()));
					}
					for (int i = 0; i < ids.length; i++) {
						db = new TEventRelation();
						db.setFeventId(valueMap.get("eventId").toString());
						db.setFbyEventId(ids[i]);
						db.setForder(i + 1);
						db.setFcreateTime(new Date());
						eventRelationDAO.save(db);
					}

					TEvent tevent = eventDAO.findOne(valueMap.get("eventId").toString());

					StringBuilder hql = new StringBuilder();
					Map<String, Object> hqlMap = Maps.newHashMap();
					hql.append(
							"select t.id as DT_RowId,t.ftitle as ftitle,t.ftypeA as ftypeA,t.forderPrice as forderPrice,t.frecommend as frecommend, (select COUNT(r.id) from TOrder r where TO_DAYS(NOW()) - TO_DAYS(r.fcreateTime) <= 30 and r.fstatus in (20, 60, 70) and r.TEvent.id = t.id) as orderCount")
							.append(" from TEvent t where t.ftypeA = :ftypeA and t.fstatus=20")
							.append(" order by orderCount desc, t.forderPrice asc,t.frecommend desc");

					hqlMap.put("ftypeA", tevent.getFtypeA());
					Query q = commonService.createQuery(hql.toString(), hqlMap);
					q.unwrap(QueryImpl.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

					int recommendNum = ids.length;
					switch (recommendNum) {
					case 1: {
						// 1.相同的一级类目2.30天销量最好的货3.相同销量以价格低者靠前4.相同价格以赞多者靠前
						int i = 2;
						q.setFirstResult(1).setMaxResults(4);
						List<Map<String, Object>> list2 = q.getResultList();
						for (Map<String, Object> amap : list2) {
							db = new TEventRelation();
							db.setFeventId(valueMap.get("eventId").toString());
							db.setFbyEventId(amap.get("DT_RowId").toString());
							db.setForder(i++);
							db.setFcreateTime(new Date());
							eventRelationDAO.save(db);
						}
						break;
					}
					case 2: {
						// 2.30天销量最好的货3.相同销量以价格低者靠前4.相同价格以赞多者靠前
						int i = 3;
						q.setFirstResult(1).setMaxResults(3);
						List<Map<String, Object>> list2 = q.getResultList();
						Map<String, Object> bmap = CollectionUtils.isNotEmpty(list2) ? list2.get(0) : null;
						for (Map<String, Object> amap : list2) {
							db = new TEventRelation();
							db.setFeventId(valueMap.get("eventId").toString());
							db.setFbyEventId(amap.get("DT_RowId").toString());
							db.setForder(i++);
							db.setFcreateTime(new Date());
							eventRelationDAO.save(db);
						}
						break;
					}
					case 3: {
						// 3.相同销量以价格低者靠前4.相同价格以赞多者靠前
						int i = 4;
						q.setFirstResult(1).setMaxResults(2);
						List<Map<String, Object>> list2 = q.getResultList();
						Map<String, Object> bmap = CollectionUtils.isNotEmpty(list2) ? list2.get(0) : null;

						for (Map<String, Object> amap : list2) {
							db = new TEventRelation();
							db.setFeventId(valueMap.get("eventId").toString());
							db.setFbyEventId(amap.get("DT_RowId").toString());
							db.setForder(i++);
							db.setFcreateTime(new Date());
							eventRelationDAO.save(db);
						}
						break;
					}
					case 4: {
						// 4.相同价格以赞多者靠前
						q.setFirstResult(1).setMaxResults(1);
						List<Map<String, Object>> list2 = q.getResultList();
						Map<String, Object> bmap = CollectionUtils.isNotEmpty(list2) ? list2.get(0) : null;
						for (Map<String, Object> amap : list2) {
							db = new TEventRelation();
							db.setFeventId(valueMap.get("eventId").toString());
							db.setFbyEventId(amap.get("DT_RowId").toString());
							db.setForder(5);
							db.setFcreateTime(new Date());
							eventRelationDAO.save(db);
						}
						break;
					}
					default: {
						break;
					}
					}

				} else {
					throw new ServiceException("本操作缺少推荐ID信息，保存失败！");
				}

			} else {
				throw new ServiceException("本操作缺少活动ID信息，保存出现了错误！");
			}
		}
	}

	// 获得商品分类列表
	@Transactional(readOnly = true)
	public void getCategoryList(Map<String, Object> valueMap, CommonPage page) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.name as name, t.code as code, t.level as level, t.value as value, t.parentId as parentId from TEventCategory t  order by t.level ASC");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		commonService.findPage(hql.toString(), page, hqlMap);

	}

	public String saveEventCategory(Map<String, Object> valueMap) {

		TEventCategory eventCategory = new TEventCategory();
		BeanMapper.copy(valueMap, eventCategory);
		// 先查找下category的集合,确认下是否有分类存在,如果不存在,value从1开始
		List<TEventCategory> list = eventCategoryDAO.findAll();
		if (list == null || list.size() == 0) {
			eventCategory.setValue(1);
		} else if (list != null && list.size() > 0) {
			// 如果存在,按照value的从大到小排序,找出最大值,最大值+1
			// 找出最大的value值
			int value = eventCategoryDAO.findMaxValue();
			value++;
			eventCategory.setValue(value);
		}
		eventCategoryDAO.save(eventCategory);
		if (eventCategory.getImageA() != null) {
			imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(eventCategory.getImageA()), 2,
					eventCategory.getId().toString(), 14);
		}

		if (eventCategory.getLevel() == 2) {// 如果是二级分类
			// 向类别属性中间表中插入数据
			if (valueMap.containsKey("typeClass1") && StringUtils.isNotBlank(valueMap.get("typeClass1").toString())) {
				// 向类别属性中间表中插入数据
				TGoodsTypeClassCategory typeClassCategory1 = new TGoodsTypeClassCategory();
				typeClassCategory1.setFcategoryId(eventCategory.getId());
				typeClassCategory1.setFtypeId(Long.parseLong(valueMap.get("typeClass1").toString()));
				goodsTypeClassCategoryDAO.save(typeClassCategory1);
			}
			if (valueMap.containsKey("typeClass2") && StringUtils.isNotBlank(valueMap.get("typeClass2").toString())) {
				// 向类别属性中间表中插入数据
				TGoodsTypeClassCategory typeClassCategory2 = new TGoodsTypeClassCategory();
				typeClassCategory2.setFcategoryId(eventCategory.getId());
				typeClassCategory2.setFtypeId(Long.parseLong(valueMap.get("typeClass2").toString()));
				goodsTypeClassCategoryDAO.save(typeClassCategory2);
			}
		}

		// 修改时间戳时间
		ObjectTimeStampDTO objectTimeStampDTO = new ObjectTimeStampDTO();
		objectTimeStampDTO.setFsubObject(2);
		objectTimeStampDTO.setFsubUpdateTime(new Date());
		appService.updateObjectTimeStamp(objectTimeStampDTO);

		return eventCategory.getId().toString();
	}

	@Transactional(readOnly = true)
	public TEventCategory geteventCategory(Long categoryId) {
		return eventCategoryDAO.findOne(categoryId);
	}

	/**
	 * 修改商品分类方法
	 * 
	 * @param map
	 */
	public void editCategory(Map<String, Object> valueMap) {

		String id = valueMap.get("id").toString();
		TEventCategory eventCategory = eventCategoryDAO.getOne(Long.valueOf(id));
		// 将此分类关联的属性删除,重新设定保存
		goodsTypeClassCategoryDAO.deleteByCategoryId(Long.valueOf(id));
		if (valueMap.containsKey("typeClass1") && StringUtils.isNotBlank(valueMap.get("typeClass1").toString())) {
			TGoodsTypeClassCategory tGoodsTypeClassCategory = new TGoodsTypeClassCategory();
			tGoodsTypeClassCategory.setFtypeId(Long.valueOf(valueMap.get("typeClass1").toString()));
			tGoodsTypeClassCategory.setFcategoryId(Long.valueOf(id));
			goodsTypeClassCategoryDAO.save(tGoodsTypeClassCategory);// 添加
		}

		if (valueMap.containsKey("typeClass2") && StringUtils.isNotBlank(valueMap.get("typeClass2").toString())) {
			TGoodsTypeClassCategory tGoodsTypeClassCategory = new TGoodsTypeClassCategory();
			tGoodsTypeClassCategory.setFtypeId(Long.valueOf(valueMap.get("typeClass2").toString()));
			tGoodsTypeClassCategory.setFcategoryId(Long.valueOf(id));
			goodsTypeClassCategoryDAO.save(tGoodsTypeClassCategory);// 添加
		}

		if (valueMap.containsKey("name") && StringUtils.isNotBlank(valueMap.get("name").toString())) {
			eventCategory.setName(valueMap.get("name").toString());
		} else {
			eventCategory.setName(null);
		}

		/*
		 * if (valueMap.containsKey("value") &&
		 * StringUtils.isNotBlank(valueMap.get("value").toString())) {
		 * eventCategory.setValue(Integer.valueOf(valueMap.get("value").toString
		 * ())); } else { eventCategory.setValue(null); }
		 */

		if (valueMap.containsKey("parentId") && StringUtils.isNotBlank(valueMap.get("parentId").toString())) {
			eventCategory.setParentId(Long.valueOf(valueMap.get("parentId").toString()));
		} else {
			eventCategory.setParentId(null);
		}
		if (valueMap.containsKey("level") && StringUtils.isNotBlank(valueMap.get("level").toString())) {
			eventCategory.setLevel(Integer.valueOf(valueMap.get("level").toString()));
		} else {
			eventCategory.setLevel(null);
		}
		if (valueMap.containsKey("name") && StringUtils.isNotBlank(valueMap.get("name").toString())) {
			eventCategory.setName(valueMap.get("name").toString());
		} else {
			eventCategory.setName(null);
		}
		if (valueMap.containsKey("imageA") && StringUtils.isNotBlank(valueMap.get("imageA").toString())) {
			if (!valueMap.get("imageA").toString().equals(eventCategory.getImageA())) {
				eventCategory.setImageA(valueMap.get("imageA").toString());
				if (eventCategory.getImageA() != null) {
					imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(eventCategory.getImageA()), 2,
							eventCategory.getId().toString(), 14);
				}
			}
		} else {
			eventCategory.setImageA(null);
		}
		if (valueMap.containsKey("imageB") && StringUtils.isNotBlank(valueMap.get("imageB").toString())) {
			if (!valueMap.get("imageB").toString().equals(eventCategory.getImageB())) {
				eventCategory.setImageB(valueMap.get("imageB").toString());
				if (eventCategory.getImageB() != null) {
					imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(eventCategory.getImageB()), 2,
							eventCategory.getId().toString(), 14);
				}
			}
		} else {
			eventCategory.setImageB(null);
		}
		eventCategoryDAO.save(eventCategory);

		// 修改时间戳时间
		ObjectTimeStampDTO objectTimeStampDTO = new ObjectTimeStampDTO();
		objectTimeStampDTO.setFsubObject(2);
		objectTimeStampDTO.setFsubUpdateTime(new Date());
		appService.updateObjectTimeStamp(objectTimeStampDTO);
	}

	/**
	 * 根据分类ID删除
	 * 
	 * @param categoryId
	 */
	public void delCategory(String categoryId) {
		eventCategoryDAO.delete(Long.valueOf(categoryId));

		// 修改时间戳时间
		ObjectTimeStampDTO objectTimeStampDTO = new ObjectTimeStampDTO();
		objectTimeStampDTO.setFsubObject(2);
		objectTimeStampDTO.setFsubUpdateTime(new Date());
		appService.updateObjectTimeStamp(objectTimeStampDTO);
	}

	/**
	 * 根据分类级别获取分类列表
	 * 
	 * @param level
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<TEventCategory> getCategoryList(Integer level) {
		return eventCategoryDAO.findByLevel(level);
	}

	/**
	 * 查找所有的属性规格
	 * 
	 * @return
	 */
	public List<TGoodsTypeClass> getAllTypeClassList() {

		return goodsTypeClassDAO.findAll();
	}

	/**
	 * 获得商品属性列表
	 * 
	 * @author maxiao
	 * @param map
	 * @param page
	 */
	@Transactional(readOnly = true)
	public void getTypeClassList(Map<String, Object> valueMap, CommonPage page) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.fclassName as fclassName, t.fsort as fsort from TGoodsTypeClass t  order by t.fsort ASC");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		commonService.findPage(hql.toString(), page, hqlMap);
	}

	/**
	 * 保存商品属性
	 * 
	 * @author maxiao
	 * @param map
	 * @return
	 */
	public String saveTypeClass(Map<String, Object> valueMap) {
		TGoodsTypeClass typeClass = new TGoodsTypeClass();
		BeanMapper.copy(valueMap, typeClass);
		goodsTypeClassDAO.save(typeClass);
		return typeClass.getId().toString();
	}

	/**
	 * 修改商品属性信息
	 * 
	 * @param map
	 */

	public void editTypeClass(Map<String, Object> valueMap) {
		String id = valueMap.get("id").toString();// 获取编辑属性的id
		TGoodsTypeClass typeClass = goodsTypeClassDAO.getOne(Long.parseLong(id));
		if (valueMap.containsKey("fclassName") && StringUtils.isNoneBlank(valueMap.get("fclassName").toString())) {
			typeClass.setFclassName(valueMap.get("fclassName").toString());
		} else {
			typeClass.setFclassName(null);
		}
		if (valueMap.containsKey("fsort") && StringUtils.isNotBlank(valueMap.get("fsort").toString())) {
			typeClass.setFsort(Integer.valueOf(valueMap.get("fsort").toString()));
		} else {
			typeClass.setFsort(null);
		}
		goodsTypeClassDAO.save(typeClass);
	}

	public TGoodsTypeClass getTypeClassInfo(Long typeClassId) {
		return goodsTypeClassDAO.findOne(typeClassId);
	}

	/**
	 * 根据ID删除商品属性
	 * 
	 * @param id
	 */
	public void delTypeClass(Long id) {
		goodsTypeClassDAO.delete(Long.valueOf(id));
	}

	/**
	 * 根据属性ID查找对应的所有的属性值
	 * 
	 * @author maxiao
	 * @param valueMap
	 * @param page
	 * @param page
	 */

	public void getTypeValueList(Map<String, Object> valueMap, CommonPage page) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.fvalue as fvalue,t.fsort as fsort from TGoodsTypeClassValue t where t.fextendClassId=:fextendClassId order by t.fsort asc");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("fextendClassId", Long.valueOf(valueMap.get("fextendClassId").toString()));
		commonService.findPage(hql.toString(), page, hqlMap);

	}

	public TGoodsTypeClassValue getTypeValueById(Long itemId) {

		return goodsTypeClassValueDAO.getOne(itemId);
	}

	public void addTypeValue(TGoodsTypeClassValue goodsTypeValue) {
		goodsTypeClassValueDAO.save(goodsTypeValue);
	}

	public void updateTypeValue(TGoodsTypeClassValue goodsTypeValue) {
		TGoodsTypeClassValue value = goodsTypeClassValueDAO.getOne(goodsTypeValue.getId());
		value.setFvalue(goodsTypeValue.getFvalue());
		value.setFsort(goodsTypeValue.getFsort());
		goodsTypeClassValueDAO.save(value);
	}

	public void deleteTypeValue(Long id) {
		goodsTypeClassValueDAO.delete(id);
	}

	public Long countTypeValueByNameAndClassId(String name, Long fextendClassId, Long id) {
		if (id == null || id.equals(0L)) {// 新增时
			return goodsTypeClassValueDAO.countByNameAndClassId(name, fextendClassId);
		} else {// 更新时
			return goodsTypeClassValueDAO.countByNameAndClassIdNotId(name, fextendClassId, id);
		}

	}

	public Long countTypeValueBySortAndClassId(Integer sort, Long fextendClassId, Long id) {
		if (id == null || id.equals(0L)) {// 新增时
			return goodsTypeClassValueDAO.countBySortAndClassId(sort, fextendClassId);
		} else {// 更新时
			return goodsTypeClassValueDAO.countBySortAndClassIdNotId(sort, fextendClassId, id);
		}
	}

	public List<TGoodsTypeClassCategory> getTypeCategory(Long categoryId) {
		return goodsTypeClassCategoryDAO.getTypeCategory(categoryId);
	}

	public List<TGoodsTypeClassValue> getTypeValueListByTypeId(Long typeClassId) {
		return goodsTypeClassValueDAO.getTypeValueListByTypeId(typeClassId);
	}

	public List<Map<String, Object>> getSkuList(Map<String, Object> valueMap) {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> hqlMap = Maps.newHashMap();

		hql.append(
				"select t.id as DT_RowId, t.fgoodsNO as fgoodsNO,t.fprice as price, t.fpriceMoney as fpriceMoney, t.ftotal as ftotal,t.fstock as fstock,t.flimitation as flimitation,t.fhavingImage as fHavingImage,t.flag as flag, ")
				.append("c.fvalue as cfvalue,c.fextendClassName as cfextendClassName,")
				.append("f.fvalue as tfvalue,f.fextendClassName as ffextendClassName ")
				.append("from TGoodsSku t left join TGoodsTypeClassValue c ")
				.append("on t.fclassTypeValue1 = c.id left join TGoodsTypeClassValue f on t.fclassTypeValue2 = f.id");

		if (StringUtils.isNotBlank(valueMap.get("eventId").toString())) {
			hql.append(" where  t.fgoodsId = :eventId");
			hqlMap.put("eventId", valueMap.get("eventId").toString());
		}

		try {
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.id asc");
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询QL语句时出错！");
		}

		return commonService.find(hql.toString(), hqlMap);
	}

	public TEventCategory getCategoryByValue(Integer value) {
		return eventCategoryDAO.getCategoryByValue(value);
	}

	public String addSku(Map<String, Object> valueMap) {
		TGoodsSku sku = new TGoodsSku();
		BeanMapper.copy(valueMap, sku);
		if (StringUtils.isNotBlank(valueMap.get("value").toString())) {
			if (StringUtils.isNotBlank(valueMap.get("fgoodsNO").toString())) {
				String fgoodsNO = valueMap.get("value").toString() + valueMap.get("fgoodsNO").toString();
				sku.setFgoodsNO(fgoodsNO);

			}
		}
		// 先判断是否为第一次添加,如果是第一次添加,将sku设置为默认
		List<TGoodsSku> skuList = goodsSkuDAO.findGoodsListSku(valueMap.get("fgoodsId").toString());
		if (null != skuList && skuList.size() > 0) {
			// 判断属性选择是否已经存在,是否与默认显示一致
			// 确认下默认显示的的属性
			StringBuilder builder1 = new StringBuilder();
			StringBuilder builder2 = new StringBuilder();
			for (TGoodsSku tGoodsSku : skuList) {
				if (tGoodsSku.getFlag() == 0) {
					if (null != tGoodsSku.getFclassTypeValue1()) {
						TGoodsTypeClassValue class1 = goodsTypeClassValueDAO.getOne(tGoodsSku.getFclassTypeValue1());
						builder1.append(class1.getFextendClassName());
					}
					if (null != tGoodsSku.getFclassTypeValue2() && tGoodsSku.getFclassTypeValue2() != 0) {
						TGoodsTypeClassValue class2 = goodsTypeClassValueDAO.getOne(tGoodsSku.getFclassTypeValue2());
						builder1.append(class2.getFextendClassName());
					}
				}
			}

			if (valueMap.containsKey("fclassTypeValue1") && StringUtils.isNotBlank(valueMap.get("fclassTypeValue1").toString())) {
				// 根据属性值的id确认下属性
				String valueId = valueMap.get("fclassTypeValue1").toString();
				TGoodsTypeClassValue classValue = goodsTypeClassValueDAO.getOne(Long.valueOf(valueId));
				builder2.append(classValue.getFextendClassName());
			}
			if (valueMap.containsKey("fclassTypeValue2") && StringUtils.isNotBlank(valueMap.get("fclassTypeValue2").toString()) ) {
				String valueId2 = valueMap.get("fclassTypeValue2").toString();
				TGoodsTypeClassValue classValue2 = goodsTypeClassValueDAO.getOne(Long.valueOf(valueId2));
				builder2.append(classValue2.getFextendClassName());
			}
			if (!builder1.toString().equals(builder2.toString())) {
				return "1";//代表此库存的属性设置与默认库存属性设置不一致,请重新确认！
				//throw new ServiceException("");
			}

		} else {
			sku.setFlag(0);
		}

		goodsSkuDAO.save(sku);
		if (sku.getFimage() != null) {
			imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(sku.getFimage()), 2, sku.getId().toString(), 14);
		}
		return sku.getId().toString();

	}

	public TGoodsSku getSkuById(String skuId) {
		return goodsSkuDAO.getOne(skuId);
	}

	/**
	 * goodsId 商品id type 1 上架加入缓存 2 下架从缓存删除
	 */
	public void goodsCatch(String goodsId, int type) {
		List<TGoodsSku> goodsSkus = goodsSkuDAO.findGoodsListSku(goodsId);
		for (TGoodsSku sku : goodsSkus) {
			String goodsSkuCache = mapper.toJson(sku);
			if (type == 1) {
				redisService.putCache(RedisMoudel.goodsSku, sku.getId(), goodsSkuCache);
			} else if (type == 2) {
				redisService.removeCache(RedisMoudel.goodsSku, sku.getId());
			}
		}
	}

	public void deleteSku(String skuId) {
		TGoodsSku goodsSku = goodsSkuDAO.getOne(skuId);
		if (goodsSku.getFlag() != 0) {
			goodsSkuDAO.delete(skuId);
		} else {
			throw new ServiceException("此为商品默认显示规格,请勿删除");
		}

	}

	public TGoodsTypeClass getTypeClassById(Long typeClass1Id) {
		return goodsTypeClassDAO.findOne(typeClass1Id);
	}

	public List<Map<String, Object>> getSpecList(Map<String, Object> valueMap) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.fspaceName as fspaceName,t.fvalueName as fvalueName from TGoodsSpaceValue t where t.fgoodsId=:fgoodsId ");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("fgoodsId", valueMap.get("fgoodsId").toString());
		try {
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.id asc");
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询QL语句时出错！");
		}

		return commonService.find(hql.toString(), hqlMap);

	}

	public void addSpecAndPacking(TGoodsSpaceValue goodsSpaceValue) {
		goodsSpaceValueDAO.save(goodsSpaceValue);

	}

	public void updateSpecAndPacking(TGoodsSpaceValue goodsSpaceValue) {
		TGoodsSpaceValue value = goodsSpaceValueDAO.getOne(goodsSpaceValue.getId());
		value.setFspaceName(goodsSpaceValue.getFspaceName());
		value.setFvalueName(goodsSpaceValue.getFvalueName());
		goodsSpaceValueDAO.save(value);

	}

	public void editSku(Map<String, Object> valueMap) {
		String id = valueMap.get("id").toString();// 获取编辑属性的id
		TGoodsSku sku = goodsSkuDAO.getOne(id);

		if (valueMap.containsKey("fclassTypeValue1")
				&& StringUtils.isNoneBlank(valueMap.get("fclassTypeValue1").toString())) {
			sku.setFclassTypeValue1(Long.valueOf(valueMap.get("fclassTypeValue1").toString()));
		} else {
			sku.setFclassTypeValue1(null);
		}

		if (valueMap.containsKey("fclassTypeValue2")
				&& StringUtils.isNoneBlank(valueMap.get("fclassTypeValue2").toString())) {
			sku.setFclassTypeValue2(Long.valueOf(valueMap.get("fclassTypeValue2").toString()));
		} else {
			sku.setFclassTypeValue2(null);
		}

		if (valueMap.containsKey("fprice") && StringUtils.isNotBlank(valueMap.get("fprice").toString())) {
			BigDecimal bdBigDecimal = new BigDecimal(valueMap.get("fprice").toString());
			sku.setFprice(bdBigDecimal);
		} else {
			sku.setFprice(null);
		}

		if (valueMap.containsKey("fpriceMoney") && StringUtils.isNotBlank(valueMap.get("fpriceMoney").toString())) {
			BigDecimal bdBigDecimal2 = new BigDecimal(valueMap.get("fpriceMoney").toString());
			sku.setFpriceMoney(bdBigDecimal2);
		} else {
			sku.setFpriceMoney(null);
		}

		if (valueMap.containsKey("ftotal") && StringUtils.isNotBlank(valueMap.get("ftotal").toString())) {
			sku.setFtotal(Integer.valueOf(valueMap.get("ftotal").toString()));
		} else {
			sku.setFtotal(null);
		}

		if (valueMap.containsKey("fstock") && StringUtils.isNotBlank(valueMap.get("fstock").toString())) {
			sku.setFstock(Integer.valueOf(valueMap.get("fstock").toString()));
		} else {
			sku.setFstock(null);
		}

		if (valueMap.containsKey("flimitation") && StringUtils.isNotBlank(valueMap.get("flimitation").toString())) {
			sku.setFlimitation(Integer.valueOf(valueMap.get("flimitation").toString()));
		} else {
			sku.setFlimitation(null);
		}

		if (valueMap.containsKey("flag") && StringUtils.isNotBlank(valueMap.get("flag").toString())) {
			sku.setFlag(Integer.valueOf(valueMap.get("flag").toString()));
		} else {
			sku.setFlag(null);
		}
		if (valueMap.containsKey("fhavingImage") && StringUtils.isNotBlank(valueMap.get("fhavingImage").toString())) {
			sku.setFhavingImage(Integer.valueOf(valueMap.get("fhavingImage").toString()));
		} else {
			sku.setFhavingImage(null);
		}

		if (valueMap.containsKey("value") && StringUtils.isNotBlank(valueMap.get("value").toString())
				&& StringUtils.isNotBlank(valueMap.get("fgoodsNO").toString()) && valueMap.containsKey("fgoodsNO")) {
			int leng = valueMap.get("value").toString().length();
			String temp = valueMap.get("value").toString();
			if (leng < 3) {
				for (int i = 0; i < 3 - leng; i++) {
					temp = "0" + temp;
				}

			}
			sku.setFgoodsNO(temp + valueMap.get("fgoodsNO").toString());
		}
		if (valueMap.containsKey("fimage") && StringUtils.isNotBlank(valueMap.get("fimage").toString())) {
			if (!valueMap.get("fimage").toString().equals(sku.getFimage())) {
				sku.setFimage(valueMap.get("fimage").toString());
				if (sku.getFimage() != null) {
					imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(sku.getFimage()), 2,
							sku.getId().toString(), 14);
				}
			}
		} else {
			sku.setFimage(null);
		}

		goodsSkuDAO.save(sku);

	}

	public TGoodsTypeClassValue getTypeValueInfo(Long typeValueId) {
		return goodsTypeClassValueDAO.getOne(typeValueId);
	}

	public TEvent getGoodsById(String eventId) {
		// TODO Auto-generated method stub
		return eventDAO.getOne(eventId);
	}

	public void updateGoods(String eventId, String imagePath) {
		TEvent event = eventDAO.getOne(eventId);
		event.setFgoodsSpecImage(imagePath);
		eventDAO.save(event);

	}

	public void saveEventE(Map<String, Object> valueMap) {

		if (valueMap.containsKey("eventId") && StringUtils.isNotBlank(valueMap.get("eventId").toString())) {
			String eventId = valueMap.get("eventId").toString();

			goodsSpaceValueDAO.deleteByEventId(eventId);

			// TEvent tevent = eventDAO.getOne(eventId);
			String[] fspaceNames = null;
			if (valueMap.containsKey("fspaceName")
					&& org.springframework.util.ObjectUtils.isArray(valueMap.get("fspaceName"))) {
				fspaceNames = (String[]) valueMap.get("fspaceName");
			} else {
				fspaceNames = ArrayUtils.toArray(valueMap.get("fspaceName").toString());
			}

			String[] fvalueNameS = null;
			if (valueMap.containsKey("fvalueName")
					&& org.springframework.util.ObjectUtils.isArray(valueMap.get("fvalueName"))) {
				fvalueNameS = (String[]) valueMap.get("fvalueName");
			} else {
				fvalueNameS = ArrayUtils.toArray(valueMap.get("fvalueName").toString());
			}

			List<TGoodsSpaceValue> list = Lists.newArrayList();

			TGoodsSpaceValue tgoodsSpaceValue = null;
			if (valueMap.get("fvalueName") != null && StringUtils.isNotBlank(valueMap.get("fvalueName").toString())) {
				for (int i = 0; i < fvalueNameS.length; i++) {
					tgoodsSpaceValue = new TGoodsSpaceValue();
					tgoodsSpaceValue.setFgoodsId(eventId);
					String count = fvalueNameS[i];
					tgoodsSpaceValue.setFvalueName(count);
					tgoodsSpaceValue.setFspaceName(fspaceNames[i]);
					goodsSpaceValueDAO.save(tgoodsSpaceValue);
					list.add(tgoodsSpaceValue);
				}
			}
		}
	}

	public List<TGoodsSpaceValue> getSpecList(String eventId) {

		return goodsSpaceValueDAO.getSpecList(eventId);
	}

	public String getCategoryAName(Integer ftypeA) {
		return eventCategoryDAO.getCategoryAName(ftypeA);
	}

	public List<TGoodsSku> getSkuListById(String eventId) {
		return goodsSkuDAO.findGoodsListSku(eventId);
	}

	public List<TEventCategory> findAllCategoryList() {
		return eventCategoryDAO.findAll();
	}

	public int findMaxValue() {
		// TODO Auto-generated method stub
		return eventCategoryDAO.findMaxValue();
	}

	public Long countGoodsByNameAndClassId(String fgoodsno, String value, String fgoodsId, String id) {
		if (id == null || id.equals(0L)) {// 新增时
			String fgoodsNO = value + fgoodsno;
			return goodsSkuDAO.countByGoodsNOAndClassId(fgoodsNO, fgoodsId);
		} else {// 更新时
			String fgoodsNO = value + fgoodsno;
			return goodsSkuDAO.countByGoodsNOAndClassIdNotId(fgoodsNO, fgoodsId, id);
		}

	}

	public Long countByCategoryName(String categoryNameValue, Long id) {
		if(id==null || id.equals(0L)){//新增时
			return  eventCategoryDAO.countByCategoryName(categoryNameValue);
		}else{
		
			return eventCategoryDAO.countByCategoryNameAndNotId(categoryNameValue,id);	
		}
		
	
		
	}

	public Long countByTypeClassName(String typeClassName, Long id) {
		if(id==null|| id.equals(0L)){
			return goodsTypeClassDAO.countByTypeClassName(typeClassName);
		}else{
			return goodsTypeClassDAO.countByTypeClassNameAndNotId(typeClassName,id);
		}
		
		
	}

	public Long countByTypeClassSort(Integer value, Long id) {
		if(id==null|| id.equals(0L)){//新增时
			return goodsTypeClassDAO.countByTypeClassSort(value);
		}else{//更新时
			return goodsTypeClassDAO.countByTypeClassSorAndNotId(value,id);
		}
	}

	public void saveGoodsSpec(List<TGoodsSpaceValue> list) {
		for (TGoodsSpaceValue tGoodsSpaceValue : list) {
			goodsSpaceValueDAO.save(tGoodsSpaceValue);
		}
		
	}

	public Long countBySpaceName(String fspaceName, String fgoodsId) {
		
		return goodsSpaceValueDAO.countBySpaceName(fspaceName,fgoodsId);
	}

	public Long countByGoodsIdAndValueName(String value, String fgoodsId, Long id) {
		if(id==null|| id.equals(0L)){//新增时
			return goodsSpaceValueDAO.countBySpaceName(value,fgoodsId);
		}else{//更新时
			return goodsSpaceValueDAO.countByGoodsIdAndValueName(value,id,fgoodsId);
		}
	}

}