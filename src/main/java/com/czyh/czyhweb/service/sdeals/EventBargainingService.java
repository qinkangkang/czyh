package com.czyh.czyhweb.service.sdeals;

import java.math.BigDecimal;
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
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;

import com.czyh.czyhweb.dao.CustomerBargainingDAO;
import com.czyh.czyhweb.dao.EventBargainingDAO;
import com.czyh.czyhweb.dao.EventDAO;
import com.czyh.czyhweb.dao.EventSessionDAO;
import com.czyh.czyhweb.dao.EventSpecDAO;
import com.czyh.czyhweb.dao.ImageDAO;
import com.czyh.czyhweb.dao.TimingTaskDAO;
import com.czyh.czyhweb.entity.TEvent;
import com.czyh.czyhweb.entity.TEventBargaining;
import com.czyh.czyhweb.entity.TTimingTask;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.CommonService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.DictionaryUtil;

/**
 * 砍一砍活动业务管理类
 * 
 * @author jinsey
 *
 */
@Component
@Transactional
public class EventBargainingService {

	private static final Logger logger = LoggerFactory.getLogger(EventBargainingService.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private CommonService commonService;

	@Autowired
	private EventBargainingDAO eventBargainingDAO;

	@Autowired
	private CustomerBargainingDAO customerBargainingDAO;

	@Autowired
	private ImageDAO imageDAO;

	@Autowired
	private EventDAO eventDAO;

	@Autowired
	private EventSessionDAO eventSessionDAO;

	@Autowired
	private EventSpecDAO eventSpecDAO;

	@Autowired
	private TimingTaskDAO timingTaskDAO;

	@Transactional(readOnly = true)
	public void getBargainingList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.feventId as feventId, t.ftitle as ftitle, t.fbeginTime as fbeginTime, t.fendTime as fendTime, t.fstatus as fstatus from TEventBargaining t where fstatus<999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_ftitle") && StringUtils.isNotBlank(valueMap.get("s_ftitle").toString())) {
				hql.append(" and t.ftitle like :s_ftitle ");
				hqlMap.put("s_ftitle", "%" + valueMap.get("s_ftitle").toString() + "%");
			}
			if (valueMap.containsKey("fbeginTime") && StringUtils.isNotBlank(valueMap.get("fbeginTime").toString())) {
				hql.append(" and t.fbeginTime >= :fbeginTime ");
				hqlMap.put("fbeginTime", DateUtils.parseDate(valueMap.get("fbeginTime").toString(), "yyyy-MM-dd"));
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
			if (amap.get("fbeginTime") != null && StringUtils.isNotBlank(amap.get("fbeginTime").toString())) {
				date = (Date) amap.get("fbeginTime");
				amap.put("fbeginTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
			if (amap.get("fendTime") != null && StringUtils.isNotBlank(amap.get("fendTime").toString())) {
				date = (Date) amap.get("fendTime");
				amap.put("fendTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}

			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatusString", DictionaryUtil.getString(DictionaryUtil.BonusStatus,
						(Integer) amap.get("fstatus"), shiroUser.getLanguage()));
			}

		}
	}

	public void addBargaining(Map<String, Object> valueMap) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		TEventBargaining tEventBargaining = new TEventBargaining();
		if (valueMap.containsKey("eventId") && StringUtils.isNotBlank(valueMap.get("eventId").toString())) {
			tEventBargaining.setFeventId(valueMap.get("eventId").toString());
		}
		if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
			tEventBargaining.setFtitle(valueMap.get("ftitle").toString());
		}
		try {
			if (valueMap.containsKey("fbeginTime") && StringUtils.isNotBlank(valueMap.get("fbeginTime").toString())) {
				tEventBargaining
						.setFbeginTime(DateUtils.parseDate(valueMap.get("fbeginTime").toString(), "yyyy-MM-dd HH:mm"));
			}
			if (valueMap.containsKey("fendTime") && StringUtils.isNotBlank(valueMap.get("fendTime").toString())) {
				tEventBargaining
						.setFendTime(DateUtils.parseDate(valueMap.get("fendTime").toString(), "yyyy-MM-dd HH:mm"));
			}
		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}

		if (valueMap.containsKey("fimage") && StringUtils.isNotBlank(valueMap.get("fimage").toString())) {
			tEventBargaining.setFimage(Long.valueOf(valueMap.get("fimage").toString()));
		}
		if (valueMap.containsKey("ftype") && StringUtils.isNotBlank(valueMap.get("ftype").toString())) {
			tEventBargaining.setFtype(Integer.valueOf(valueMap.get("ftype").toString()));
		}
		if (valueMap.containsKey("finputText") && StringUtils.isNotBlank(valueMap.get("finputText").toString())) {
			tEventBargaining.setFinputText(valueMap.get("finputText").toString());
		}
		if (valueMap.containsKey("fpackageDesc") && StringUtils.isNotBlank(valueMap.get("fpackageDesc").toString())) {
			tEventBargaining.setFpackageDesc(valueMap.get("fpackageDesc").toString());
		}
		if (valueMap.containsKey("fstartPrice") && StringUtils.isNotBlank(valueMap.get("fstartPrice").toString())) {
			tEventBargaining.setFstartPrice(new BigDecimal(valueMap.get("fstartPrice").toString()));
		}
		if (valueMap.containsKey("fsettlementPrice")
				&& StringUtils.isNotBlank(valueMap.get("fsettlementPrice").toString())) {
			tEventBargaining.setFsettlementPrice(new BigDecimal(valueMap.get("fsettlementPrice").toString()));
		}
		if (valueMap.containsKey("ffloorPrice1") && StringUtils.isNotBlank(valueMap.get("ffloorPrice1").toString())) {
			tEventBargaining.setFfloorPrice1(new BigDecimal(valueMap.get("ffloorPrice1").toString()));
		}
		if (valueMap.containsKey("ffloorPrice2") && StringUtils.isNotBlank(valueMap.get("ffloorPrice2").toString())) {
			tEventBargaining.setFfloorPrice2(new BigDecimal(valueMap.get("ffloorPrice2").toString()));
		}
		if (valueMap.containsKey("ffloorPrice3") && StringUtils.isNotBlank(valueMap.get("ffloorPrice3").toString())) {
			tEventBargaining.setFfloorPrice3(new BigDecimal(valueMap.get("ffloorPrice3").toString()));
		}

		if (valueMap.containsKey("fstock1") && StringUtils.isNotBlank(valueMap.get("fstock1").toString())) {
			tEventBargaining.setFstock1(Integer.valueOf(valueMap.get("fstock1").toString()));
			tEventBargaining.setFremainingStock1(Integer.valueOf(valueMap.get("fstock1").toString()));
		}
		if (valueMap.containsKey("fstock2") && StringUtils.isNotBlank(valueMap.get("fstock2").toString())) {
			tEventBargaining.setFstock2(Integer.valueOf(valueMap.get("fstock2").toString()));
			tEventBargaining.setFremainingStock2(Integer.valueOf(valueMap.get("fstock2").toString()));
		}
		if (valueMap.containsKey("fstock3") && StringUtils.isNotBlank(valueMap.get("fstock3").toString())) {
			tEventBargaining.setFstock3(Integer.valueOf(valueMap.get("fstock3").toString()));
			tEventBargaining.setFremainingStock3(Integer.valueOf(valueMap.get("fstock3").toString()));
		}

		if (valueMap.containsKey("fmaxBargaining")
				&& StringUtils.isNotBlank(valueMap.get("fmaxBargaining").toString())) {
			tEventBargaining.setFmaxBargaining(new BigDecimal(valueMap.get("fmaxBargaining").toString()));
		}
		if (valueMap.containsKey("fminBargaining")
				&& StringUtils.isNotBlank(valueMap.get("fminBargaining").toString())) {
			tEventBargaining.setFminBargaining(new BigDecimal(valueMap.get("fminBargaining").toString()));
		}

		TEvent tEvent = eventDAO.getOne(valueMap.get("eventId").toString());
		tEventBargaining.setFeventTitle(tEvent.getFtitle());

		tEventBargaining.setFcreaterId(shiroUser.getId());
		tEventBargaining.setFstatus(30);
		tEventBargaining.setFcreateTime(new Date());
		tEventBargaining = eventBargainingDAO.save(tEventBargaining);

		// 修改该活动为促销活动
		eventDAO.updateSaleType(1, valueMap.get("eventId").toString());

		if (tEventBargaining.getFimage() != null) {
			imageDAO.saveStatusAndEntityIdAndEntityType(tEventBargaining.getFimage(), 2, tEventBargaining.getId(), 10);
		}
		// 创建定时器定时推送模版提醒消息
		Date date2 = DateUtils.addHours(tEventBargaining.getFendTime(), -2);

		TTimingTask tEventTimingTask = new TTimingTask();
		tEventTimingTask = new TTimingTask();
		tEventTimingTask.setEntityId(tEventBargaining.getId());
		tEventTimingTask.setTaskType(19);
		tEventTimingTask.setTaskTime(date2.getTime());
		timingTaskDAO.save(tEventTimingTask);
	}

	public void delEventBargaining(String fID) {
		eventBargainingDAO.saveStatusBargaining(999, fID, new Date());
		// delete定时器定时推送模版提醒消息
		try {
			timingTaskDAO.clearTimeTaskByEntityId(fID);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
	}

	public void onBargaining(String fID, Integer Status) {
		eventBargainingDAO.saveStatusBargaining(Status, fID, new Date());

		TEventBargaining tEventBargaining = eventBargainingDAO.getOne(fID);
		TTimingTask timingTask = timingTaskDAO.getByEntityIdAndTaskType(fID, 19);
		if (timingTask == null) {
			Date date2 = DateUtils.addHours(tEventBargaining.getFendTime(), -2);
			if (DateUtils.truncatedCompareTo(new Date(), date2, Calendar.MINUTE) < 0) {
				timingTask = new TTimingTask();
				timingTask.setEntityId(fID);
				timingTask.setTaskType(19);
				timingTask.setTaskTime(tEventBargaining.getFendTime().getTime());
				timingTaskDAO.save(timingTask);
			}
		} else {
			timingTaskDAO.saveTaskTime(tEventBargaining.getFendTime().getTime(), fID, 19);
		}
	}

	public void offBargaining(String fID, Integer Status) {
		eventBargainingDAO.saveStatusBargaining(Status, fID, new Date());
		try {
			timingTaskDAO.clearTimeTaskByEntityId(fID);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
	}

	@Transactional(readOnly = true)
	public TEventBargaining gettEventBargaining(String Id) {
		return eventBargainingDAO.getOne(Id);
	}

	public void editBargaining(Map<String, Object> valueMap) {

		TEventBargaining tEventBargaining = eventBargainingDAO.getOne(valueMap.get("id").toString());
		if (valueMap.containsKey("eventId") && StringUtils.isNotBlank(valueMap.get("eventId").toString())) {
			tEventBargaining.setFeventId(valueMap.get("eventId").toString());
		} else {
			tEventBargaining.setFeventId(null);
		}
		if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
			tEventBargaining.setFtitle(valueMap.get("ftitle").toString());
		} else {
			tEventBargaining.setFtitle(null);
		}

		if (valueMap.containsKey("fimage") && StringUtils.isNotBlank(valueMap.get("fimage").toString())) {
			tEventBargaining.setFimage(Long.valueOf(valueMap.get("fimage").toString()));
		} else {
			tEventBargaining.setFimage(null);
		}
		if (valueMap.containsKey("ftype") && StringUtils.isNotBlank(valueMap.get("ftype").toString())) {
			tEventBargaining.setFtype(Integer.valueOf(valueMap.get("ftype").toString()));
		} else {
			tEventBargaining.setFtype(null);
		}
		if (valueMap.containsKey("finputText") && StringUtils.isNotBlank(valueMap.get("finputText").toString())) {
			tEventBargaining.setFinputText(valueMap.get("finputText").toString());
		} else {
			tEventBargaining.setFinputText(null);
		}
		if (valueMap.containsKey("fpackageDesc") && StringUtils.isNotBlank(valueMap.get("fpackageDesc").toString())) {
			tEventBargaining.setFpackageDesc(valueMap.get("fpackageDesc").toString());
		} else {
			tEventBargaining.setFpackageDesc(null);
		}
		if (valueMap.containsKey("fstartPrice") && StringUtils.isNotBlank(valueMap.get("fstartPrice").toString())) {
			tEventBargaining.setFstartPrice(new BigDecimal(valueMap.get("fstartPrice").toString()));
		} else {
			tEventBargaining.setFstartPrice(null);
		}
		if (valueMap.containsKey("fsettlementPrice")
				&& StringUtils.isNotBlank(valueMap.get("fsettlementPrice").toString())) {
			tEventBargaining.setFsettlementPrice(new BigDecimal(valueMap.get("fsettlementPrice").toString()));
		} else {
			tEventBargaining.setFsettlementPrice(null);
		}
		if (valueMap.containsKey("ffloorPrice1") && StringUtils.isNotBlank(valueMap.get("ffloorPrice1").toString())) {
			tEventBargaining.setFfloorPrice1(new BigDecimal(valueMap.get("ffloorPrice1").toString()));
		} else {
			tEventBargaining.setFfloorPrice1(null);
		}
		if (valueMap.containsKey("ffloorPrice2") && StringUtils.isNotBlank(valueMap.get("ffloorPrice2").toString())) {
			tEventBargaining.setFfloorPrice2(new BigDecimal(valueMap.get("ffloorPrice2").toString()));
		} else {
			tEventBargaining.setFfloorPrice2(null);
		}
		if (valueMap.containsKey("ffloorPrice3") && StringUtils.isNotBlank(valueMap.get("ffloorPrice3").toString())) {
			tEventBargaining.setFfloorPrice3(new BigDecimal(valueMap.get("ffloorPrice3").toString()));
		} else {
			tEventBargaining.setFfloorPrice3(null);
		}

		if (valueMap.containsKey("fmaxBargaining")
				&& StringUtils.isNotBlank(valueMap.get("fmaxBargaining").toString())) {
			tEventBargaining.setFmaxBargaining(new BigDecimal(valueMap.get("fmaxBargaining").toString()));
		} else {
			tEventBargaining.setFmaxBargaining(null);
		}

		if (valueMap.containsKey("fminBargaining")
				&& StringUtils.isNotBlank(valueMap.get("fminBargaining").toString())) {
			tEventBargaining.setFminBargaining(new BigDecimal(valueMap.get("fminBargaining").toString()));
		} else {
			tEventBargaining.setFminBargaining(null);
		}

		if (valueMap.containsKey("fstock1") && StringUtils.isNotBlank(valueMap.get("fstock1").toString())) {
			tEventBargaining.setFremainingStock1(tEventBargaining.getFremainingStock1()
					+ (Integer.valueOf(valueMap.get("fstock1").toString()) - tEventBargaining.getFstock1()));
			tEventBargaining.setFstock1(Integer.valueOf(valueMap.get("fstock1").toString()));
		}
		if (valueMap.containsKey("fstock2") && StringUtils.isNotBlank(valueMap.get("fstock2").toString())) {
			tEventBargaining.setFremainingStock2(tEventBargaining.getFremainingStock2()
					+ (Integer.valueOf(valueMap.get("fstock2").toString()) - tEventBargaining.getFstock2()));
			tEventBargaining.setFstock2(Integer.valueOf(valueMap.get("fstock2").toString()));
		}
		if (valueMap.containsKey("fstock3") && StringUtils.isNotBlank(valueMap.get("fstock3").toString())) {
			tEventBargaining.setFremainingStock3(tEventBargaining.getFremainingStock3()
					+ (Integer.valueOf(valueMap.get("fstock3").toString()) - tEventBargaining.getFstock3()));
			tEventBargaining.setFstock3(Integer.valueOf(valueMap.get("fstock3").toString()));
		}

		try {
			if (valueMap.containsKey("fbeginTime") && StringUtils.isNotBlank(valueMap.get("fbeginTime").toString())) {
				tEventBargaining
						.setFbeginTime(DateUtils.parseDate(valueMap.get("fbeginTime").toString(), "yyyy-MM-dd HH:mm"));
			} else {
				tEventBargaining.setFbeginTime(null);
			}
			if (valueMap.containsKey("fendTime") && StringUtils.isNotBlank(valueMap.get("fendTime").toString())) {
				tEventBargaining
						.setFendTime(DateUtils.parseDate(valueMap.get("fendTime").toString(), "yyyy-MM-dd HH:mm"));
				// 活动还有两小时结束微信推送定时器任务
				// 只有在活动结束时间变化的时候进行定时器任务的变更
				if (DateUtils.truncatedCompareTo(
						DateUtils.parseDate(valueMap.get("fendTime").toString(), "yyyy-MM-dd HH:mm"),
						DateUtils.truncate(tEventBargaining.getFendTime(), Calendar.SECOND), Calendar.SECOND) != 0) {
					// 距离活动结束之前两小时时间
					Date date2 = DateUtils.addHours(tEventBargaining.getFendTime(), -2);
					// 如果当前时间距离活动结束时间不足两小时，则设定马上通知定时任务
					if (DateUtils.truncatedCompareTo(new Date(), date2, Calendar.MINUTE) > 0) {
						timingTaskDAO.saveTaskTime(new Date().getTime(), tEventBargaining.getId(), 19);
					} else {
						TTimingTask timingTask = timingTaskDAO.getByEntityIdAndTaskType(tEventBargaining.getId(), 19);

						if (timingTask == null) {

							timingTask = new TTimingTask();
							timingTask.setEntityId(tEventBargaining.getId());
							timingTask.setTaskType(19);

						}
						timingTask.setTaskTime(date2.getTime());
						timingTaskDAO.save(timingTask);
					}
				}
			} else {
				tEventBargaining.setFendTime(null);
			}
		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}

		eventBargainingDAO.save(tEventBargaining);
	}

	@Transactional(readOnly = true)
	public void getBargainingRank(Map<String, Object> valueMap, CommonPage page) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId,c.fname as fname, c.fweixinId as fweixinId, d.ftitle as ftitle,t.fstartPrice as fstartPrice,t.fendPrice as fendPrice,t.fbargainingCount as fbargainingCount,t.fstatus as fstatus from TCustomerBargaining t")
				.append(" inner join TCustomer c on c.id = t.fcustomerId")
				.append(" inner join TEventBargaining d on d.id = t.fbargainingId").append(" where c.fstatus = 1 ");

		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_name") && StringUtils.isNotBlank(valueMap.get("s_name").toString())) {
				hql.append(" and c.fname like :s_name ");
				hqlMap.put("s_name", "%" + valueMap.get("s_name").toString() + "%");
			}
			if (valueMap.containsKey("s_ftitle") && StringUtils.isNotBlank(valueMap.get("s_ftitle").toString())) {
				System.out.println(valueMap.get("s_ftitle").toString() + "////////");
				hql.append(" and d.ftitle like :s_ftitle ");
				hqlMap.put("s_ftitle", "%" + valueMap.get("s_ftitle").toString() + "%");
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
				hql.append(" order by t.fendPrice asc");
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		Integer rank = 1;
		for (Map<String, Object> amap : list) {
			amap.put("rank", page.getOffset() + rank++);
			BigDecimal st = new BigDecimal(amap.get("fstartPrice").toString());
			BigDecimal ed = new BigDecimal(amap.get("fendPrice").toString());
			amap.put("lowpic", st.subtract(ed).doubleValue());
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatus", DictionaryUtil.getString(DictionaryUtil.BonusStatus, (Integer) amap.get("fstatus"),
						shiroUser.getLanguage()));
			}
		}
	}

	@Transactional(readOnly = true)
	public TEventBargaining getBargaining(String bargainId) {
		return eventBargainingDAO.getOne(bargainId);
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getBargainingTitleList() {
		return commonService.find("select t.id as key, t.ftitle as value from TEventBargaining t");
	}
}