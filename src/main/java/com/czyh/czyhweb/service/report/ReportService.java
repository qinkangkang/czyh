package com.czyh.czyhweb.service.report;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

import com.czyh.czyhweb.dao.EventDAO;
import com.czyh.czyhweb.dao.OrderDAO;
import com.czyh.czyhweb.dao.SceneUserDAO;
import com.czyh.czyhweb.dao.SceneUserMapDAO;
import com.czyh.czyhweb.dao.TempSceneDataDAO;
import com.czyh.czyhweb.dao.UserDAO;
import com.czyh.czyhweb.dto.CountOrderDTO;
import com.czyh.czyhweb.dto.EventNumberDTO;
import com.czyh.czyhweb.entity.TSceneUserMap;
import com.czyh.czyhweb.entity.TempSceneData;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.CommonService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.HttpClientUtil;
import com.czyh.czyhweb.util.PropertiesUtil;
import com.czyh.czyhweb.util.bmap.BmapGpsLocationBean;
import com.czyh.czyhweb.util.bmap.LbsCloud;
import com.czyh.czyhweb.util.excel.ExcelTemplate;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 系统报表业务类
 * 
 * @author jinshengzhi
 *
 */
@Component
@Transactional
public class ReportService {

	private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

	private static JsonMapper mapper = new JsonMapper(Include.ALWAYS);

	@Autowired
	private CommonService commonService;

	@Autowired
	private EventDAO eventDAO;

	@Autowired
	private OrderDAO orderDAO;

	@Autowired
	private SceneUserDAO sceneUserDAO;

	@Autowired
	private SceneUserMapDAO sceneUserMapDAO;

	@Autowired
	private TempSceneDataDAO tempSceneDataDAO;

	@PersistenceContext()
	protected EntityManager em;

	@Autowired
	private UserDAO userDAO;

	@Transactional(readOnly = true)
	public List<CountOrderDTO> countOrder(Map<String, Object> map) {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		// 查询出按底价结算订单数据
		hql.append(
				"SELECT count(t.id) as countNum, sum(COALESCE(t.ftotal,0)) as total, sum(COALESCE(t.fchangeAmount,0)) as changeAmount,SUM(COALESCE(t.freceivableTotal, 0)* COALESCE(t.TEventSpec.fpointsPrice, 0) - COALESCE(t.fchangeAmount, 0)) as profit,t.fstatus as status")
				.append(" FROM TOrder t WHERE t.fcreateTime BETWEEN :fcreateTimeStart AND :fcreateTimeEnd and t.TEvent.fsettlementType = 20");
		try {
			if (map.containsKey("fcreateTimeStart") && StringUtils.isNotBlank(map.get("fcreateTimeStart").toString())) {
				hqlMap.put("fcreateTimeStart",
						DateUtils.parseDate(map.get("fcreateTimeStart").toString(), "yyyy-MM-dd"));
			}
			if (map.containsKey("fcreateTimeEnd") && StringUtils.isNotBlank(map.get("fcreateTimeEnd").toString())) {
				// 选择的截止日期加一天，保证了查询条件的完整性，解决了时分秒的问题。
				hqlMap.put("fcreateTimeEnd",
						DateUtils.addDays(DateUtils.parseDate(map.get("fcreateTimeEnd").toString(), "yyyy-MM-dd"), 1));
			}
			if (map.containsKey("s_source") && StringUtils.isNotBlank(map.get("s_source").toString())) {
				hql.append(" and t.fsource = :s_source ");
				hqlMap.put("s_source", Integer.valueOf(map.get("s_source").toString()));
			}
			if (map.containsKey("s_changeAmount") && StringUtils.isNotBlank(map.get("s_changeAmount").toString())) {
				if (Integer.valueOf(map.get("s_changeAmount").toString()) == 1) {
					hql.append(" and t.fchangeAmount is not null ");
				}
			}
			if (map.containsKey("status") && StringUtils.isNotBlank(map.get("status").toString())) {
				Integer status = Integer.valueOf(map.get("status").toString());
				if (status.intValue() == 1) {
					hql.append(" AND t.ftotal = 0 ");
				} else if (status.intValue() == 2) {
					hql.append(" AND t.ftotal > 0 ");
				}
			}
			hql.append(" GROUP BY t.fstatus ");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		List<Map<String, Object>> listPrice = commonService.find(hql.toString(), hqlMap);
		// 查询出按扣点结算订单数据
		hql.delete(0, hql.length() - 1);
		hqlMap.clear();
		hql.append(
				"SELECT count(t.id) as countNum, sum(COALESCE(t.ftotal,0)) as total, sum(COALESCE(t.fchangeAmount,0)) as changeAmount,SUM(COALESCE(t.freceivableTotal, 0)-t.fcount*COALESCE(t.TEventSpec.fsettlementPrice, 0) - COALESCE(t.fchangeAmount, 0)) as profit,t.fstatus as status")
				.append(" FROM TOrder t WHERE t.fcreateTime BETWEEN :fcreateTimeStart AND :fcreateTimeEnd and t.TEvent.fsettlementType = 30");
		try {
			if (map.containsKey("fcreateTimeStart") && StringUtils.isNotBlank(map.get("fcreateTimeStart").toString())) {
				hqlMap.put("fcreateTimeStart",
						DateUtils.parseDate(map.get("fcreateTimeStart").toString(), "yyyy-MM-dd"));
			}
			if (map.containsKey("fcreateTimeEnd") && StringUtils.isNotBlank(map.get("fcreateTimeEnd").toString())) {
				// 选择的截止日期加一天，保证了查询条件的完整性，解决了时分秒的问题。
				hqlMap.put("fcreateTimeEnd",
						DateUtils.addDays(DateUtils.parseDate(map.get("fcreateTimeEnd").toString(), "yyyy-MM-dd"), 1));
			}
			if (map.containsKey("s_source") && StringUtils.isNotBlank(map.get("s_source").toString())) {
				hql.append(" and t.fsource = :s_source ");
				hqlMap.put("s_source", Integer.valueOf(map.get("s_source").toString()));
			}
			if (map.containsKey("s_changeAmount") && StringUtils.isNotBlank(map.get("s_changeAmount").toString())) {
				if (Integer.valueOf(map.get("s_changeAmount").toString()) == 1) {
					hql.append(" and t.fchangeAmount is not null ");
				}
			}
			if (map.containsKey("status") && StringUtils.isNotBlank(map.get("status").toString())) {
				Integer status = Integer.valueOf(map.get("status").toString());
				if (status.intValue() == 1) {
					hql.append(" AND t.ftotal = 0 ");
				} else if (status.intValue() == 2) {
					hql.append(" AND t.ftotal > 0 ");
				}
			}
			hql.append(" GROUP BY t.fstatus ");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		List<Map<String, Object>> listPoint = commonService.find(hql.toString(), hqlMap);

		CountOrderDTO countOrderDTO = null;
		Map<Integer, CountOrderDTO> mapData = new HashMap<Integer, CountOrderDTO>();
		for (Map<String, Object> amap : listPrice) {
			countOrderDTO = new CountOrderDTO();
			if (amap.get("status") != null && StringUtils.isNotBlank(amap.get("status").toString())) {
				countOrderDTO.setCount(amap.get("countNum").toString());
				countOrderDTO.setOrderStatus((Integer) amap.get("status"));
				countOrderDTO.setTotal((BigDecimal) amap.get("total"));
				countOrderDTO.setChangeAmount((BigDecimal) amap.get("changeAmount"));
				countOrderDTO.setProfit((BigDecimal) amap.get("profit"));
				mapData.put((Integer) amap.get("status"), countOrderDTO);
			}
		}
		for (Map<String, Object> amap : listPoint) {
			countOrderDTO = new CountOrderDTO();
			if (amap.get("status") != null && StringUtils.isNotBlank(amap.get("status").toString())) {
				if (mapData.containsKey((Integer) amap.get("status"))) {
					Integer statuscount = Integer.parseInt(mapData.get((Integer) amap.get("status")).getCount())
							+ Integer.parseInt(amap.get("countNum").toString());
					countOrderDTO.setCount(statuscount.toString());
					countOrderDTO.setOrderStatus((Integer) amap.get("status"));
					countOrderDTO.setTotal(
							mapData.get((Integer) amap.get("status")).getTotal().add((BigDecimal) amap.get("total")));
					countOrderDTO.setProfit(
							mapData.get((Integer) amap.get("status")).getProfit().add((BigDecimal) amap.get("profit")));
					countOrderDTO.setChangeAmount(mapData.get((Integer) amap.get("status")).getChangeAmount()
							.add((BigDecimal) amap.get("changeAmount")));
					mapData.put((Integer) amap.get("status"), countOrderDTO);
				} else {
					countOrderDTO.setCount(amap.get("countNum").toString());
					countOrderDTO.setOrderStatus((Integer) amap.get("status"));
					countOrderDTO.setTotal((BigDecimal) amap.get("total"));
					countOrderDTO.setChangeAmount((BigDecimal) amap.get("changeAmount"));
					countOrderDTO.setProfit((BigDecimal) amap.get("profit"));
					mapData.put((Integer) amap.get("status"), countOrderDTO);
				}
			}
		}
		// 将111状态加到110
		if (mapData.containsKey(111)) {
			countOrderDTO = new CountOrderDTO();
			if (mapData.containsKey(110)) {
				Integer viewCount = Integer.parseInt(mapData.get(111).getCount())
						+ Integer.parseInt(mapData.get(110).getCount());
				countOrderDTO.setCount(viewCount.toString());
				countOrderDTO.setOrderStatus(110);
				countOrderDTO.setTotal(mapData.get(111).getTotal().add(mapData.get(110).getTotal()));
				countOrderDTO.setProfit(mapData.get(111).getProfit().add(mapData.get(110).getProfit()));
				countOrderDTO
						.setChangeAmount(mapData.get(111).getChangeAmount().add(mapData.get(110).getChangeAmount()));
				mapData.put(110, countOrderDTO);
			} else {
				countOrderDTO.setCount(mapData.get(111).getCount());
				countOrderDTO.setOrderStatus(110);
				countOrderDTO.setTotal(mapData.get(111).getTotal());
				countOrderDTO.setProfit(mapData.get(111).getProfit());
				countOrderDTO.setChangeAmount(mapData.get(111).getChangeAmount());
				mapData.put(110, countOrderDTO);
			}
		}
		// 将115状态变为120
		if (mapData.containsKey(115)) {
			countOrderDTO = new CountOrderDTO();
			if (mapData.containsKey(120)) {
				Integer viewCount = Integer.parseInt(mapData.get(115).getCount())
						+ Integer.parseInt(mapData.get(120).getCount());
				countOrderDTO.setCount(viewCount.toString());
				countOrderDTO.setOrderStatus(120);
				countOrderDTO.setTotal(mapData.get(115).getTotal().add(mapData.get(120).getTotal()));
				countOrderDTO.setProfit(mapData.get(115).getProfit().add(mapData.get(120).getProfit()));
				countOrderDTO
						.setChangeAmount(mapData.get(115).getChangeAmount().add(mapData.get(110).getChangeAmount()));
				mapData.put(120, countOrderDTO);
			} else {
				countOrderDTO.setCount(mapData.get(115).getCount());
				countOrderDTO.setOrderStatus(120);
				countOrderDTO.setTotal(mapData.get(115).getTotal());
				countOrderDTO.setProfit(mapData.get(115).getProfit());
				countOrderDTO.setChangeAmount(mapData.get(115).getChangeAmount());
				mapData.put(120, countOrderDTO);
			}
		}
		List<CountOrderDTO> dataList = Lists.newArrayList();
		BigDecimal allTotal = new BigDecimal("0");
		Long allCount = new Long(0);
		BigDecimal allChangeAmount = new BigDecimal("0");
		BigDecimal allProfit = new BigDecimal("0");
		Set<Integer> statusList = new HashSet<Integer>();
		for (Map.Entry<Integer, CountOrderDTO> entry : mapData.entrySet()) {
			Integer status = entry.getKey();
			if (status == 10 || status == 20 || status == 70 || status == 60 || status == 100 || status == 101
					|| status == 109 || status == 110 || status == 120) {
				statusList.add(status);
				dataList.add(entry.getValue());
			}
			if (status == 20 || status == 60 || status == 70) {
				allTotal = allTotal.add(mapData.get(status).getTotal());
				allCount = allCount + Integer.parseInt(mapData.get(status).getCount());
				allChangeAmount = allChangeAmount.add(mapData.get(status).getChangeAmount());
				allProfit = allProfit.add(mapData.get(status).getProfit());
			}
		}
		if (!statusList.contains(10)) {
			CountOrderDTO countOrderDTOs = new CountOrderDTO();
			countOrderDTOs.setCount("0");
			countOrderDTOs.setTotal(new BigDecimal("0"));
			countOrderDTOs.setChangeAmount(new BigDecimal("0"));
			countOrderDTOs.setProfit(new BigDecimal("0"));
			countOrderDTOs.setOrderStatus(10);
			dataList.add(countOrderDTOs);
		}
		if (!statusList.contains(20)) {
			CountOrderDTO countOrderDTOs = new CountOrderDTO();
			countOrderDTOs.setCount("0");
			countOrderDTOs.setTotal(new BigDecimal("0"));
			countOrderDTOs.setChangeAmount(new BigDecimal("0"));
			countOrderDTOs.setProfit(new BigDecimal("0"));
			countOrderDTOs.setOrderStatus(20);
			dataList.add(countOrderDTOs);
		}
		if (!statusList.contains(60)) {
			CountOrderDTO countOrderDTOs = new CountOrderDTO();
			countOrderDTOs.setCount("0");
			countOrderDTOs.setTotal(new BigDecimal("0"));
			countOrderDTOs.setChangeAmount(new BigDecimal("0"));
			countOrderDTOs.setProfit(new BigDecimal("0"));
			countOrderDTOs.setOrderStatus(60);
			dataList.add(countOrderDTOs);
		}
		if (!statusList.contains(70)) {
			CountOrderDTO countOrderDTOs = new CountOrderDTO();
			countOrderDTOs.setCount("0");
			countOrderDTOs.setTotal(new BigDecimal("0"));
			countOrderDTOs.setChangeAmount(new BigDecimal("0"));
			countOrderDTOs.setProfit(new BigDecimal("0"));
			countOrderDTOs.setOrderStatus(70);
			dataList.add(countOrderDTOs);
		}
		if (!statusList.contains(100)) {
			CountOrderDTO countOrderDTOs = new CountOrderDTO();
			countOrderDTOs.setCount("0");
			countOrderDTOs.setTotal(new BigDecimal("0"));
			countOrderDTOs.setChangeAmount(new BigDecimal("0"));
			countOrderDTOs.setProfit(new BigDecimal("0"));
			countOrderDTOs.setOrderStatus(100);
			dataList.add(countOrderDTOs);
		}
		if (!statusList.contains(109)) {
			CountOrderDTO countOrderDTOs = new CountOrderDTO();
			countOrderDTOs.setCount("0");
			countOrderDTOs.setTotal(new BigDecimal("0"));
			countOrderDTOs.setChangeAmount(new BigDecimal("0"));
			countOrderDTOs.setProfit(new BigDecimal("0"));
			countOrderDTOs.setOrderStatus(109);
			dataList.add(countOrderDTOs);
		}
		if (!statusList.contains(110)) {
			CountOrderDTO countOrderDTOs = new CountOrderDTO();
			countOrderDTOs.setCount("0");
			countOrderDTOs.setTotal(new BigDecimal("0"));
			countOrderDTOs.setChangeAmount(new BigDecimal("0"));
			countOrderDTOs.setProfit(new BigDecimal("0"));
			countOrderDTOs.setOrderStatus(110);
			dataList.add(countOrderDTOs);
		}
		if (!statusList.contains(120)) {
			CountOrderDTO countOrderDTOs = new CountOrderDTO();
			countOrderDTOs.setCount("0");
			countOrderDTOs.setTotal(new BigDecimal("0"));
			countOrderDTOs.setChangeAmount(new BigDecimal("0"));
			countOrderDTOs.setProfit(new BigDecimal("0"));
			countOrderDTOs.setOrderStatus(120);
			dataList.add(countOrderDTOs);
		}

		CountOrderDTO countOrderDTO2 = new CountOrderDTO();
		countOrderDTO2.setTotal(allTotal);
		countOrderDTO2.setCount(allCount.toString());
		countOrderDTO2.setChangeAmount(allChangeAmount);
		countOrderDTO2.setProfit(allProfit);
		countOrderDTO2.setOrderStatus(888);
		dataList.add(countOrderDTO2);
		return dataList;
	}

	@Transactional(readOnly = true)
	public void getReportCustomerList(Map<String, Object> map, CommonPage page) {

		/*
		 * commonService.findPage(hql.toString(), page, hqlMap);
		 * 
		 * List<Map<String, Object>> list = page.getResult();
		 */
		StringBuilder hql = new StringBuilder();
		hql.append(
				"SELECT DISTINCT(t.id) AS id, t.fname AS name,t.fphone AS fphone,c.fregisterChannel AS fregisterChannel,t.fsex AS fsex,t.fcreateTime as fregisterTime")
				.append(" FROM TCustomer t LEFT JOIN TCustomerInfo c ON t.id = c.fcustomerId LEFT JOIN TCustomerTag g ON ")
				.append(" g.fcustomerId = t.id  LEFT JOIN TDictionary d ON d.TDictionaryClass.id = :classid WHERE 1=1 AND t.ftype = :userType AND t.fstatus = :userStatus ");
		StringBuilder pagehql = new StringBuilder();
		pagehql.append("SELECT count(DISTINCT t.id) as total ")
				.append(" FROM TCustomer t LEFT JOIN TCustomerInfo c ON t.id = c.fcustomerId LEFT JOIN TCustomerTag g ON ")
				.append(" g.fcustomerId = t.id  LEFT JOIN TDictionary d ON d.TDictionaryClass.id = :classid WHERE 1=1 AND t.ftype = :userType AND t.fstatus = :userStatus ");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("classid", DictionaryUtil.PushUserTag);
		hqlMap.put("userType", 1);
		hqlMap.put("userStatus", 1);
		try {
			if (map.containsKey("fcreateTimeStart") && StringUtils.isNotBlank(map.get("fcreateTimeStart").toString())) {
				pagehql.append(" AND t.fcreateTime >= :fcreateTimeStart ");
				hql.append(" AND t.fcreateTime >= :fcreateTimeStart ");
				hqlMap.put("fcreateTimeStart",
						DateUtils.parseDate(map.get("fcreateTimeStart").toString(), "yyyy-MM-dd"));
			}
			if (map.containsKey("fcreateTimeEnd") && StringUtils.isNotBlank(map.get("fcreateTimeEnd").toString())) {

				pagehql.append(" AND t.fcreateTime <= :fcreateTimeEnd ");
				hql.append(" AND t.fcreateTime <= :fcreateTimeEnd ");
				hqlMap.put("fcreateTimeEnd",
						DateUtils.addDays(DateUtils.parseDate(map.get("fcreateTimeEnd").toString(), "yyyy-MM-dd"), 1));
			}
			if (map.containsKey("registerChannel") && StringUtils.isNotBlank(map.get("registerChannel").toString())) {
				if (!map.get("registerChannel").toString().equals("all")) {
					pagehql.append(" AND c.fregisterChannel = :fregisterChannel ");
					hql.append(" AND c.fregisterChannel = :fregisterChannel ");
					hqlMap.put("fregisterChannel", map.get("registerChannel").toString());
				}
			}
			if (map.containsKey("s_fweixinName") && StringUtils.isNotBlank(map.get("s_fweixinName").toString())) {
				pagehql.append(" AND t.fweixinName = :s_fweixinName ");
				hql.append(" and t.fweixinName like :s_fweixinName");
				hqlMap.put("s_fweixinName", "%" + map.get("s_fweixinName").toString() + "%");
			}
			if (map.containsKey("s_fphone") && StringUtils.isNotBlank(map.get("s_fphone").toString())) {
				pagehql.append(" AND t.fphone = :s_fphone ");
				hql.append(" and t.fphone = :s_fphone");
				hqlMap.put("s_fphone", map.get("s_fphone").toString());
			}
			if (map.containsKey("tag") && StringUtils.isNotBlank(map.get("tag").toString())) {
				Integer tag = Integer.valueOf(map.get("tag").toString());
				if (tag.intValue() != 0) {
					pagehql.append(" AND g.ftag = :ftag ");
					hql.append(" AND g.ftag = :ftag ");
					hqlMap.put("ftag", tag);
				}
			}
			if (map.containsKey("phone") && StringUtils.isNotBlank(map.get("phone").toString())) {
				Integer phone = Integer.valueOf(map.get("phone").toString());
				if (phone.intValue() == 1) {
					pagehql.append(" AND t.fphone IS NOT NULL ");
					hql.append(" AND t.fphone IS NOT NULL ");
				} else if (phone.intValue() == 2) {
					pagehql.append(" AND t.fphone IS NULL ");
					hql.append(" AND t.fphone IS NULL ");
				}
			}
			if (map.containsKey("order") && StringUtils.isNotBlank(map.get("order").toString())) {
				Integer order = Integer.valueOf(map.get("order").toString());
				if (order.intValue() == 1) {
					pagehql.append(" AND c.fpayOrderNumber = 0 ");
					hql.append(" AND c.fpayOrderNumber = 0 ");
				} else if (order.intValue() == 2) {
					pagehql.append(" AND c.fpayOrderNumber > 0 ");
					hql.append(" AND c.fpayOrderNumber > 0 ");
				}
			}
			hql.append(" order by t.fcreateTime desc");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> pageList = commonService.find(pagehql.toString(), hqlMap);

		List<Map<String, Object>> list = page.getResult();
		if (pageList != null) {
			if (pageList.get(0).get("total") != null
					&& StringUtils.isNotBlank(pageList.get(0).get("total").toString())) {
				page.setTotalCount((long) pageList.get(0).get("total"));
			}
		}

		for (Map<String, Object> amap : list) {
			if (amap.get("fsex") != null && StringUtils.isNotBlank(amap.get("fsex").toString())) {
				amap.put("sex", ((Integer) amap.get("fsex") == 1 ? "男" : "女"));
			} else {
				amap.put("sex", "");
			}
			if (amap.get("fphone") != null && StringUtils.isNotBlank(amap.get("fphone").toString())) {
				amap.put("phone", amap.get("fphone").toString());
			} else {
				amap.put("phone", "");
			}
			if (amap.get("fregisterTime") != null && StringUtils.isNotBlank(amap.get("fregisterTime").toString())) {
				Date date = (Date) amap.get("fregisterTime");
				amap.put("registerTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
			if (amap.get("fregisterChannel") != null
					&& StringUtils.isNotBlank(amap.get("fregisterChannel").toString())) {
				amap.put("registerChannel", DictionaryUtil.getName(DictionaryUtil.RegistrationChannel,
						amap.get("fregisterChannel").toString()));
			}

			if (amap.get("id") != null && StringUtils.isNotBlank(amap.get("id").toString())) {
				Integer zeroOrderNumber = orderDAO.countZeroOrderNumber(amap.get("id").toString(), 20, 60, 70);
				Integer payOrderNumber = orderDAO.countPayOrderNumber(amap.get("id").toString(), 20, 60, 70);
				BigDecimal total = orderDAO.countTotalOrder(amap.get("id").toString(), 20, 60, 70);
				amap.put("zeroOrderNumber", zeroOrderNumber);
				amap.put("payOrderNumber", payOrderNumber);
				amap.put("total", total);
			}

		}
	}

	@Transactional(readOnly = true)
	public List<EventNumberDTO> getEventNumber() {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hql.append("SELECT t.value as cValue,t.name as cName FROM TEventCategory t WHERE t.level = :level");
		hqlMap.put("level", 1);
		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);
		List<EventNumberDTO> dataList = Lists.newArrayList();
		EventNumberDTO eventNumberDTO = null;
		Long allCount = new Long(0);
		Long onCount = new Long(0);
		Long offCount = new Long(0);
		for (Map<String, Object> amap : list) {
			eventNumberDTO = new EventNumberDTO();
			if (amap.get("cValue") != null && StringUtils.isNotBlank(amap.get("cValue").toString())
					&& amap.get("cName") != null && StringUtils.isNotBlank(amap.get("cName").toString())) {
				eventNumberDTO.setTitle(amap.get("cName").toString());
				Long n = eventDAO.getCountByStatusAndType((Integer) amap.get("cValue"), 20);
				Long f = eventDAO.getCountByStatusAndType((Integer) amap.get("cValue"), 90);
				Long c = eventDAO.getCountByStatusAndType((Integer) amap.get("cValue"), 10);
				eventNumberDTO.setCount(c);
				eventNumberDTO.setOnSaleCount(n);
				eventNumberDTO.setOffSaleCount(f);
				allCount = allCount + c;
				onCount = onCount + n;
				offCount = offCount + f;
			}
			dataList.add(eventNumberDTO);
		}
		EventNumberDTO eventNumberDTO2 = new EventNumberDTO();
		eventNumberDTO2.setTitle("合计");
		eventNumberDTO2.setOffSaleCount(offCount);
		eventNumberDTO2.setCount(allCount);
		eventNumberDTO2.setOnSaleCount(onCount);
		dataList.add(eventNumberDTO2);
		return dataList;
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getReportEventStatusList(Map<String, Object> map) {

		List<Map<String, Object>> dataList = Lists.newArrayList();
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		if (map.containsKey("status") && StringUtils.isNotBlank(map.get("status").toString())
				&& map.containsKey("fcreateTimeStart")
				&& StringUtils.isNotBlank(map.get("fcreateTimeStart").toString())) {
			Integer status = Integer.valueOf(map.get("status").toString());
			boolean all = (status.intValue() == 0 ? true : false);
			try {
				hqlMap.put("fcreateTimeStart",
						DateUtils.parseDate(map.get("fcreateTimeStart").toString(), "yyyy-MM-dd"));
				hqlMap.put("classId", DictionaryUtil.EventStatus);
			} catch (ParseException e) {
				logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			}
			if (status.intValue() == 1 || all) {
				StringBuilder hql = new StringBuilder();
				hql.append(
						" SELECT t1.ftitle AS ftitle,'上架操作' as operate,t2.name as name FROM TEvent t1 LEFT JOIN TDictionary t2 ON t2.value = t1.fstatus WHERE t2.TDictionaryClass.id = :classId AND Date(t1.fonSaleTime) = :fcreateTimeStart ");

				/*
				 * if (map.containsKey("fcreateTimeStart") &&
				 * StringUtils.isNotBlank(map.get("fcreateTimeStart").toString()
				 * )) { q.setParameter("fcreateTimeStart",
				 * DateUtils.parseDate(map.get("fcreateTimeStart").toString(),
				 * "yyyy-MM-dd")); }
				 */

				List<Map<String, Object>> list1 = commonService.find(hql.toString(), hqlMap);
				if (all) {
					dataList.addAll(list1);
				} else {
					dataList = list1;
				}
			}
			if (status.intValue() == 2 || all) {
				StringBuilder hql = new StringBuilder();
				// hql.append("SELECT t3.fTitle as title,'上架操作' as
				// operate,t4.name as name FROM t_event t3 LEFT JOIN
				// t_dictionary t4 ON t4.value=t3.fStatus WHERE t4.class_id =4
				// AND Date(t3.fOnSaleTime) = " +
				// map.get("fcreateTimeStart").toString());
				hql.append(
						" SELECT t3.ftitle AS ftitle,'下架操作' as operate,t4.name as name FROM TEvent t3 LEFT JOIN TDictionary t4 ON t4.value = t3.fstatus WHERE t4.TDictionaryClass.id = :classId AND Date(t3.foffSaleTime) = :fcreateTimeStart ");
				/*
				 * if (map.containsKey("fcreateTimeStart") &&
				 * StringUtils.isNotBlank(map.get("fcreateTimeStart").toString()
				 * )) { q.setParameter("fcreateTimeStart2",
				 * DateUtils.parseDate(map.get("fcreateTimeStart").toString(),
				 * "yyyy-MM-dd")); }
				 */
				/* q.setParameter("classId2",DictionaryUtil.EventStatus); */
				List<Map<String, Object>> list2 = commonService.find(hql.toString(), hqlMap);
				if (all) {
					dataList.addAll(list2);
				} else {
					dataList = list2;
				}
			}
			if (status.intValue() == 3 || all) {
				StringBuilder hql = new StringBuilder();
				// hql.append(" SELECT t5.fTitle as title,'录入操作' as
				// operate,t6.name as name FROM t_event t5 LEFT JOIN
				// t_dictionary t6 ON t6.value=t5.fStatus WHERE t6.class_id =4
				// AND Date(t5.fCreateTime) = " +
				// map.get("fcreateTimeStart").toString());
				hql.append(
						" SELECT t5.ftitle AS ftitle,'录入操作' as operate,t6.name as name FROM TEvent t5 LEFT JOIN TDictionary t6 ON t6.value = t5.fstatus WHERE t6.TDictionaryClass.id = :classId AND Date(t5.fcreateTime) = :fcreateTimeStart ");
				/*
				 * if (map.containsKey("fcreateTimeStart") &&
				 * StringUtils.isNotBlank(map.get("fcreateTimeStart").toString()
				 * )) { q.setParameter("fcreateTimeStart3",
				 * DateUtils.parseDate(map.get("fcreateTimeStart").toString(),
				 * "yyyy-MM-dd")); }
				 */
				List<Map<String, Object>> list3 = commonService.find(hql.toString(), hqlMap);
				if (all) {
					dataList.addAll(list3);
				} else {
					dataList = list3;
				}
			}
		}

		return dataList;
	}

	@Transactional(readOnly = true)
	public void getReportSceneUserList(CommonPage page, Map<String, Object> map) {
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT s.fsceneStr as sceneStr, s.fsceneGps as sceneGps, s.fopenId as openId,")
				.append("s.fsubscribe as fsubscribe, s.fsubscribeTime as fsubscribeTime, s.funSubscribe as funSubscribe, ")
				.append("s.funSubscribeTime as funSubscribeTime, s.fregister as fregister, s.fregisterTime as fregisterTime, ")
				.append("s.fdelivery as fdelivery, s.fdeliveryTime as fdeliveryTime, c.fname as fname, c.fphone as phone ")
				.append(" FROM TSceneUser s LEFT JOIN TCustomer c ON s.fopenId = c.fweixinId WHERE 1=1 ");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		try {

			if (map.containsKey("fcreateTimeStart") && StringUtils.isNotBlank(map.get("fcreateTimeStart").toString())
					&& map.containsKey("fcreateTimeEnd")
					&& StringUtils.isNotBlank(map.get("fcreateTimeEnd").toString())) {
				hql.append(" AND s.fsubscribeTime BETWEEN :fcreateTimeStart AND :fcreateTimeEnd ");
				hqlMap.put("fcreateTimeStart",
						DateUtils.parseDate(map.get("fcreateTimeStart").toString(), "yyyy-MM-dd HH:mm"));
				// hqlMap.put("fcreateTimeEnd",
				// DateUtils.addDays(DateUtils.parseDate(map.get("fcreateTimeEnd").toString(),
				// "yyyy-MM-dd"), 1));
				hqlMap.put("fcreateTimeEnd",
						DateUtils.parseDate(map.get("fcreateTimeEnd").toString(), "yyyy-MM-dd HH:mm"));
			}

			if (map.containsKey("sceneStr") && StringUtils.isNotBlank(map.get("sceneStr").toString())) {
				if (!"all".equals(map.get("sceneStr").toString())) {
					hql.append(" AND s.fsceneStr = :sceneStr ");
					hqlMap.put("sceneStr", map.get("sceneStr").toString());
				}
			}
			if (map.containsKey("register") && StringUtils.isNotBlank(map.get("register").toString())) {
				Integer register = Integer.valueOf(map.get("register").toString());
				if (register > 0) {
					hql.append(" AND s.fregister > :register ");
				} else {
					hql.append(" AND s.fregister = :register ");
				}
				hqlMap.put("register", 0);
			}
			if (map.containsKey("subscribe") && StringUtils.isNotBlank(map.get("subscribe").toString())) {
				Integer subscribe = Integer.valueOf(map.get("subscribe").toString());
				hql.append(" AND s.fsubscribe = :subscribe ");
				hqlMap.put("subscribe", subscribe);
			}
			if (map.containsKey("registerType") && StringUtils.isNotBlank(map.get("registerType").toString())) {
				Integer registerType = Integer.valueOf(map.get("registerType").toString());
				hql.append(" AND s.fregister = :registerType ");
				hqlMap.put("registerType", registerType);
			}

		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		Date date = null;
		for (Map<String, Object> amap : list) {
			if (amap.get("fsubscribeTime") != null && StringUtils.isNotBlank(amap.get("fsubscribeTime").toString())) {
				date = (Date) amap.get("fsubscribeTime");
				amap.put("subscribeTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
			if (amap.get("funSubscribeTime") != null
					&& StringUtils.isNotBlank(amap.get("funSubscribeTime").toString())) {
				date = (Date) amap.get("funSubscribeTime");
				amap.put("unSubscribeTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
			if (amap.get("fdeliveryTime") != null && StringUtils.isNotBlank(amap.get("fdeliveryTime").toString())) {
				date = (Date) amap.get("fdeliveryTime");
				amap.put("deliveryTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
			if (amap.get("fregisterTime") != null && StringUtils.isNotBlank(amap.get("fregisterTime").toString())) {
				date = (Date) amap.get("fregisterTime");
				amap.put("registerTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
			if (amap.get("sceneStr") != null && StringUtils.isNotBlank(amap.get("sceneStr").toString())) {

				amap.put("scene", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
			if (amap.get("fname") != null && StringUtils.isNotBlank(amap.get("fname").toString())) {

				amap.put("name", amap.get("fname").toString());
			} else {
				amap.put("name", "");
			}
			if (amap.get("sceneGps") != null && StringUtils.isNotBlank(amap.get("sceneGps").toString())) {

				String city = "";
				LinkedHashMap<String, Object> paramsMap = Maps.newLinkedHashMap();
				try {
					paramsMap.put("ak", LbsCloud.AK);
					// paramsMap.put("callback", "renderReverse");
					String[] gpsa = StringUtils.split(amap.get("sceneGps").toString(), ',');
					paramsMap.put("location",
							new StringBuilder().append(gpsa[1]).append(",").append(gpsa[0]).toString());
					paramsMap.put("output", "json");
					paramsMap.put("pois", "0");

					String sn = LbsCloud.getSn(LbsCloud.LOCATION_URI_GPS, paramsMap);
					paramsMap.put("sn", sn);

					String json = HttpClientUtil.callUrlGet(LbsCloud.LBS_URL + LbsCloud.LOCATION_URI_GPS, paramsMap);
					BmapGpsLocationBean bmapGpsLocationBean = mapper.fromJson(json, BmapGpsLocationBean.class);
					// 返回状态码如果为0，表示获取活动距离成功
					if (bmapGpsLocationBean.getStatus() == 0) {
						city = bmapGpsLocationBean.getResult().getAddressComponent().getCity();
					}
				} catch (Exception e) {
					logger.error("去百度LBS云坐标信息获取城市码时出错");
				}
				amap.put("sceneGps", city);
			}

			if (amap.get("fregister") != null && StringUtils.isNotBlank(amap.get("fregister").toString())) {
				int fregister = Integer.valueOf(amap.get("fregister").toString()).intValue();
				// String register
				if (fregister == 0) {
					amap.put("register", "否");
					amap.put("oldUser", "否");
				} else if (fregister == 1) {
					amap.put("register", "是");
					amap.put("oldUser", "否");
				} else if (fregister == 2) {
					amap.put("register", "是");
					amap.put("oldUser", "是");
				}
			}
			if (amap.get("fdelivery") != null && StringUtils.isNotBlank(amap.get("fdelivery").toString())) {
				int fdelivery = Integer.valueOf(amap.get("fdelivery").toString()).intValue();
				if (fdelivery == 1) {
					amap.put("delivery", "是");
				} else {
					amap.put("delivery", "否");
				}
			}
			if (amap.get("fsubscribe") != null && StringUtils.isNotBlank(amap.get("fsubscribe").toString())) {
				int fdelivery = Integer.valueOf(amap.get("fsubscribe").toString()).intValue();
				if (fdelivery == 1) {
					amap.put("subscribe", "是");
				} else {
					amap.put("subscribe", "否");
				}
			}
			if (amap.get("funSubscribe") != null && StringUtils.isNotBlank(amap.get("funSubscribe").toString())) {
				int fdelivery = Integer.valueOf(amap.get("funSubscribe").toString()).intValue();
				if (fdelivery == 1) {
					amap.put("unSubscribe", "是");
				} else {
					amap.put("unSubscribe", "否");
				}
			}

			if (amap.get("sceneStr") != null && StringUtils.isNotBlank(amap.get("sceneStr").toString())) {
				String str = amap.get("sceneStr").toString().substring(0, 1);
				if ("1".equals(str)) {
					amap.put("sceneId", "地推渠道");
				} else if ("2".equals(str)) {
					amap.put("sceneId", "物料渠道");
				} else if ("3".equals(str)) {
					amap.put("sceneId", "待定渠道");
				} else if ("5".equals(str)) {
					amap.put("sceneId", "砍一砍渠道");
				} else if ("9".equals(str)) {
					amap.put("sceneId", "积分渠道");
				} else {
					amap.put("sceneId", "自然关注渠道");
				}
			}
		}
	}

	@Transactional(readOnly = true)
	public List<String> getSceneStr() {
		return sceneUserDAO.getSceneStrList();
	}

	@Transactional(readOnly = true)
	public void createSceneExcel(Map<String, Object> map, String datePath, String excelFileName) {
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT s.fsceneStr as sceneStr, s.fsceneGps as sceneGps, s.fopenId as openId,")
				.append("s.fsubscribe as fsubscribe, s.fsubscribeTime as fsubscribeTime, s.funSubscribe as funSubscribe, ")
				.append("s.funSubscribeTime as funSubscribeTime, s.fregister as fregister, s.fregisterTime as fregisterTime, ")
				.append("s.fdelivery as fdelivery, s.fdeliveryTime as fdeliveryTime, c.fname as fname, c.fphone as phone ,s.fcity as fcity ")
				.append(" FROM TSceneUser s LEFT JOIN TCustomer c ON s.fopenId = c.fweixinId WHERE 1=1 ");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		try {

			if (map.containsKey("fcreateTimeStart") && StringUtils.isNotBlank(map.get("fcreateTimeStart").toString())
					&& map.containsKey("fcreateTimeEnd")
					&& StringUtils.isNotBlank(map.get("fcreateTimeEnd").toString())) {
				hql.append(" AND s.fsubscribeTime BETWEEN :fcreateTimeStart AND :fcreateTimeEnd ");
				hqlMap.put("fcreateTimeStart",
						DateUtils.parseDate(map.get("fcreateTimeStart").toString(), "yyyy-MM-dd HH:mm"));
				hqlMap.put("fcreateTimeEnd",
						DateUtils.parseDate(map.get("fcreateTimeEnd").toString(), "yyyy-MM-dd HH:mm"));
			}

			if (map.containsKey("sceneStr") && StringUtils.isNotBlank(map.get("sceneStr").toString())) {
				if (!"all".equals(map.get("sceneStr").toString())) {
					hql.append(" AND s.fsceneStr = :sceneStr ");
					hqlMap.put("sceneStr", map.get("sceneStr").toString());
				}
			}
			if (map.containsKey("register") && StringUtils.isNotBlank(map.get("register").toString())) {
				Integer register = Integer.valueOf(map.get("register").toString());
				if (register > 0) {
					hql.append(" AND s.fregister > :register ");
				} else {
					hql.append(" AND s.fregister = :register ");
				}
				hqlMap.put("register", 0);
			}
			if (map.containsKey("subscribe") && StringUtils.isNotBlank(map.get("subscribe").toString())) {
				Integer subscribe = Integer.valueOf(map.get("subscribe").toString());
				hql.append(" AND s.fsubscribe = :subscribe ");
				hqlMap.put("subscribe", subscribe);
			}
			if (map.containsKey("fregisterTime") && StringUtils.isNotBlank(map.get("fregisterTime").toString())) {
				Integer registerType = Integer.valueOf(map.get("registerType").toString());
				hql.append(" AND s.fregister = :registerType ");
				hqlMap.put("registerType", registerType);
			}

		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}

		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);
		ExcelTemplate excel = ExcelTemplate.getInstance().readTemplateClassPath("/template/excel/sceneTemp.xlsx");
		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("excelScenePath")).append("/").append(datePath).append("/");
		Date date = null;
		for (Map<String, Object> amap : list) {
			excel.creatNewRow();
			if (amap.get("sceneStr") != null && StringUtils.isNotBlank(amap.get("sceneStr").toString())) {
				excel.createNewCol(amap.get("sceneStr").toString());
				String str = amap.get("sceneStr").toString().substring(0, 1);
				if ("1".equals(str)) {
					excel.createNewCol("地推渠道");
				} else if ("2".equals(str)) {
					excel.createNewCol("物料渠道");
				} else if ("3".equals(str)) {
					excel.createNewCol("待定渠道");
				} else if ("5".equals(str)) {
					excel.createNewCol("砍一砍渠道");
				} else if ("9".equals(str)) {
					excel.createNewCol("积分渠道");
				} else {
					excel.createNewCol("自然关注渠道");
					// excel.createNewCol(StringUtils.EMPTY);
				}
			} else {
				excel.createNewCol(StringUtils.EMPTY);
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fname") != null && StringUtils.isNotBlank(amap.get("fname").toString())) {
				excel.createNewCol(amap.get("fname").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("openId") != null && StringUtils.isNotBlank(amap.get("openId").toString())) {
				excel.createNewCol(amap.get("openId").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("phone") != null && StringUtils.isNotBlank(amap.get("phone").toString())) {
				excel.createNewCol(amap.get("phone").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fregister") != null && StringUtils.isNotBlank(amap.get("fregister").toString())) {
				int fregister = Integer.valueOf(amap.get("fregister").toString()).intValue();
				if (fregister == 0) {
					excel.createNewCol("否");
					excel.createNewCol("否");
				} else if (fregister == 1) {
					excel.createNewCol("是");
					excel.createNewCol("否");
				} else if (fregister == 2) {
					excel.createNewCol("是");
					excel.createNewCol("是");
				}
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fregisterTime") != null && StringUtils.isNotBlank(amap.get("fregisterTime").toString())) {
				date = (Date) amap.get("fregisterTime");
				excel.createNewCol(DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fcity") != null && StringUtils.isNotBlank(amap.get("fcity").toString())) {
				excel.createNewCol(amap.get("fcity").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("sceneGps") != null && StringUtils.isNotBlank(amap.get("sceneGps").toString())) {
				excel.createNewCol(amap.get("sceneGps").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fsubscribe") != null && StringUtils.isNotBlank(amap.get("fsubscribe").toString())) {
				int fsubscribe = Integer.valueOf(amap.get("fsubscribe").toString()).intValue();
				if (fsubscribe == 1) {
					excel.createNewCol("是");
				} else {
					excel.createNewCol("否");
				}
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fsubscribeTime") != null && StringUtils.isNotBlank(amap.get("fsubscribeTime").toString())) {
				date = (Date) amap.get("fsubscribeTime");
				excel.createNewCol(DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("funSubscribe") != null && StringUtils.isNotBlank(amap.get("funSubscribe").toString())) {
				int fdelivery = Integer.valueOf(amap.get("funSubscribe").toString()).intValue();
				if (fdelivery == 1) {
					excel.createNewCol("是");
				} else {
					excel.createNewCol("否");
				}
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("funSubscribeTime") != null
					&& StringUtils.isNotBlank(amap.get("funSubscribeTime").toString())) {
				date = (Date) amap.get("funSubscribeTime");
				excel.createNewCol(DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			/*
			 * if (amap.get("fdelivery") != null &&
			 * StringUtils.isNotBlank(amap.get("fdelivery").toString())) { int
			 * fdelivery =
			 * Integer.valueOf(amap.get("fdelivery").toString()).intValue(); if
			 * (fdelivery == 1) { excel.createNewCol("是"); } else {
			 * excel.createNewCol("否"); } } else {
			 * excel.createNewCol(StringUtils.EMPTY); } if
			 * (amap.get("fdeliveryTime") != null &&
			 * StringUtils.isNotBlank(amap.get("fdeliveryTime").toString())) {
			 * date = (Date) amap.get("fdeliveryTime");
			 * excel.createNewCol(DateFormatUtils.format(date,
			 * "yyyy-MM-dd HH:mm")); } else {
			 * excel.createNewCol(StringUtils.EMPTY); }
			 */
		}

		excel.insertSer();
		File destDir = new File(rootPath.toString());
		if (!destDir.exists()) {
			destDir.mkdirs();
		}

		String newFile = rootPath.append(excelFileName).append(".xlsx").toString();
		excel.writeToFile(newFile);
	}

	public void getReportSceneUserGatherList(CommonPage page, Map<String, Object> map, String sessionid) {
		StringBuilder hql = new StringBuilder();
		hql.append("select t.id as DT_RowId, t.fsceneCode as sceneStr,t.fuserId as fuserId,t.fstatus as fstatus")
				.append(" ,t.fcreateTime as createTime from TSceneUserMap t where t.fstatus = 1");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		Date fcreateTimeStart = null;
		Date fcreateTimeEnd = null;
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			if (map.containsKey("fcreateTimeStart") && StringUtils.isNotBlank(map.get("fcreateTimeStart").toString())) {
				fcreateTimeStart = DateUtils.parseDate(map.get("fcreateTimeStart").toString(), "yyyy-MM-dd HH:mm");
			} else {
				fcreateTimeStart = DateUtils.parseDate(
						simpleDateFormat.format(DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH)),
						"yyyy-MM-dd HH:mm");
			}
			if (map.containsKey("fcreateTimeEnd") && StringUtils.isNotBlank(map.get("fcreateTimeEnd").toString())) {
				fcreateTimeEnd = DateUtils.parseDate(map.get("fcreateTimeEnd").toString(), "yyyy-MM-dd HH:mm");
			} else {
				fcreateTimeEnd = DateUtils.parseDate(simpleDateFormat.format(new Date()), "yyyy-MM-dd HH:mm");
			}
			if (map.containsKey("sceneStr") && StringUtils.isNotBlank(map.get("sceneStr").toString())) {
				hql.append(" AND t.fsceneCode = :sceneStr ");
				hqlMap.put("sceneStr", map.get("sceneStr").toString());
			}
			if (map.containsKey("user") && StringUtils.isNotBlank(map.get("user").toString())) {
				hql.append(" and t.fuserId = :fuserId");
				hqlMap.put("fuserId", Integer.parseInt(map.get("user").toString()));
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		// 执行存储过程
		StringBuilder sceneStrs = new StringBuilder();
		for (Map<String, Object> maps : list) {
			if (maps.get("sceneStr") != null && StringUtils.isNotBlank(maps.get("sceneStr").toString())) {
				sceneStrs.append(maps.get("sceneStr").toString()).append(",");
			}
		}
		sceneStrs.setLength(sceneStrs.length() - 1);
		StringBuilder sql = new StringBuilder();
		Query q = null;
		sql.append("{call proc_scene_for_report(?,?,?,?)}");
		q = em.createNativeQuery(sql.toString());
		q.setParameter(1, fcreateTimeStart);
		q.setParameter(2, fcreateTimeEnd);
		q.setParameter(3, sceneStrs.toString());
		q.setParameter(4, sessionid);
		q.executeUpdate();
		q.getResultList();
		// 查询临时表得到报表数据

		// 公众号关注数//授权地理信息//注册数//当日取消数//取消关注数
		List<TempSceneData> tempSceneDatas = tempSceneDataDAO.findDataBySessionId(sessionid);
		try {
			for (Map<String, Object> amap : list) {
				if (amap.get("sceneStr") != null && StringUtils.isNotBlank(amap.get("sceneStr").toString())) {
					String sceneStr = (String) amap.get("sceneStr");
					for (TempSceneData tempSceneData : tempSceneDatas) {
						if (sceneStr.equals(tempSceneData.getFsceneStr())) {
							amap.put("subscribeNum", tempSceneData.getFsubscribeNum());
							amap.put("gpsNum", tempSceneData.getFgpsNum());
							amap.put("registerNum", tempSceneData.getFdeliveryNum());
							amap.put("unRegisterNum", tempSceneData.getFunSubscribeNum());
							amap.put("todayUnRegisterNum", tempSceneData.getFtodayUnRegisterNum());
							amap.put("todaysubscribe", tempSceneData.getFtodaysubscribe());
							amap.put("hRegisterNum", tempSceneData.getFhregisterNum());
							amap.put("sweepNum", tempSceneData.getFsweepCount());
						}
					}
				}
			}
		} catch (Exception e) {
		}
		// 删除该临时数据
		tempSceneDataDAO.deleteBySessionId(sessionid);
	}

	@Transactional
	public void createSceneGatherExcel(Map<String, Object> map, String datePath, String excelFileName,
			String sessionid) {
		StringBuilder hql = new StringBuilder();
		hql.append("select t.id as DT_RowId, t.fsceneCode as sceneStr,t.fuserId as fuserId,t.fstatus as fstatus")
				.append(" ,t.fcreateTime as createTime from TSceneUserMap t where t.fstatus = 1");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		Date fcreateTimeStart = null;
		Date fcreateTimeEnd = null;
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			if (map.containsKey("fcreateTimeStart") && StringUtils.isNotBlank(map.get("fcreateTimeStart").toString())) {
				fcreateTimeStart = DateUtils.parseDate(map.get("fcreateTimeStart").toString(), "yyyy-MM-dd HH:mm");
			} else {
				fcreateTimeStart = DateUtils.parseDate(
						simpleDateFormat.format(DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH)),
						"yyyy-MM-dd HH:mm");
			}
			if (map.containsKey("fcreateTimeEnd") && StringUtils.isNotBlank(map.get("fcreateTimeEnd").toString())) {
				fcreateTimeEnd = DateUtils.parseDate(map.get("fcreateTimeEnd").toString(), "yyyy-MM-dd HH:mm");
			} else {
				fcreateTimeEnd = DateUtils.parseDate(simpleDateFormat.format(new Date()), "yyyy-MM-dd HH:mm");
			}
			if (map.containsKey("sceneStr") && StringUtils.isNotBlank(map.get("sceneStr").toString())) {
				hql.append(" AND t.fsceneCode = :sceneStr ");
				hqlMap.put("sceneStr", map.get("sceneStr").toString());
			}
			if (map.containsKey("user") && StringUtils.isNotBlank(map.get("user").toString())) {
				hql.append(" and t.fuserId = :fuserId");
				hqlMap.put("fuserId", Integer.parseInt(map.get("user").toString()));
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}

		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);
		// 执行存储过程
		StringBuilder sceneStrs = new StringBuilder();
		for (Map<String, Object> maps : list) {
			if (maps.get("sceneStr") != null && StringUtils.isNotBlank(maps.get("sceneStr").toString())) {
				sceneStrs.append(maps.get("sceneStr").toString()).append(",");
			}
		}
		sceneStrs.setLength(sceneStrs.length() - 1);
		StringBuilder sql = new StringBuilder();
		Query q = null;
		sql.append("{call proc_scene_for_report(?,?,?,?)}");
		q = em.createNativeQuery(sql.toString());
		q.setParameter(1, fcreateTimeStart);
		q.setParameter(2, fcreateTimeEnd);
		q.setParameter(3, sceneStrs.toString());
		q.setParameter(4, sessionid);
		q.executeUpdate();

		ExcelTemplate excel = ExcelTemplate.getInstance().readTemplateClassPath("/template/excel/sceneGatherTemp.xlsx");
		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("excelSceneGatherPath")).append("/").append(datePath).append("/");

		List<TempSceneData> tempSceneDatas = tempSceneDataDAO.findDataBySessionId(sessionid);
		for (Map<String, Object> amap : list) {
			if (amap.get("sceneStr") != null && StringUtils.isNotBlank(amap.get("sceneStr").toString())) {
				String sceneStr = (String) amap.get("sceneStr");
				for (TempSceneData tempSceneData : tempSceneDatas) {
					if (sceneStr.equals(tempSceneData.getFsceneStr())) {
						excel.creatNewRow();
						excel.createNewCol(sceneStr);
						excel.createNewCol(tempSceneData.getFsubscribeNum());
						excel.createNewCol(tempSceneData.getFgpsNum());
						excel.createNewCol(tempSceneData.getFdeliveryNum());
						excel.createNewCol(tempSceneData.getFtodayUnRegisterNum());
						excel.createNewCol(tempSceneData.getFtodaysubscribe());
						excel.createNewCol(tempSceneData.getFunSubscribeNum());
						excel.createNewCol(tempSceneData.getFhregisterNum());
						excel.createNewCol(tempSceneData.getFsweepCount());
					}
				}
			}
		}
		excel.insertSer();
		File destDir = new File(rootPath.toString());
		if (!destDir.exists()) {
			destDir.mkdirs();
		}

		String newFile = rootPath.append(excelFileName).append(".xlsx").toString();
		excel.writeToFile(newFile);
		// 删除该临时数据
		tempSceneDataDAO.deleteBySessionId(sessionid);
	}

	@Transactional(readOnly = true)
	public void getSceneMapList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append("select t.id as DT_RowId, t.fsceneCode as fsceneCode,t.fuserId as fuserId,t.fstatus as fstatus")
				.append(" ,t.fcreateTime as createTime from TSceneUserMap t where t.fstatus < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_user") && StringUtils.isNotBlank(valueMap.get("s_user").toString())) {
				hql.append(" and t.fuserId = :fuserId");
				hqlMap.put("fuserId", Integer.parseInt(valueMap.get("s_user").toString()));
			}
			if (valueMap.containsKey("s_fsceneCode")
					&& StringUtils.isNotBlank(valueMap.get("s_fsceneCode").toString())) {
				hql.append(" and t.fsceneCode = :fsceneCode");
				hqlMap.put("fsceneCode", valueMap.get("s_fsceneCode").toString());
			}
			if (valueMap.containsKey("s_status") && StringUtils.isNotBlank(valueMap.get("s_status").toString())) {
				hql.append(" and t.fstatus = :s_status");
				hqlMap.put("s_status", Integer.valueOf(valueMap.get("s_status").toString()));
			}
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.fcreateTime desc ");
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询QL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		Date date = null;
		for (Map<String, Object> amap : list) {
			if (amap.get("createTime") != null && StringUtils.isNotBlank(amap.get("createTime").toString())) {
				date = (Date) amap.get("createTime");
				amap.put("createTime", DateFormatUtils.format(date, "yyyy-MM-dd"));
			}
			if (amap.get("fuserId") != null && StringUtils.isNotBlank(amap.get("fuserId").toString())) {
				amap.put("fuserId", DictionaryUtil.getString(DictionaryUtil.SceneMap, (Integer) amap.get("fuserId"),
						shiroUser.getLanguage()));
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				if ((Integer) amap.get("fstatus") == 0) {
					amap.put("fstatus", "禁用");
				} else if ((Integer) amap.get("fstatus") == 1) {
					amap.put("fstatus", "正常");
				}
			}
		}
	}

	public void addScene(Map<String, Object> valueMap) {

		TSceneUserMap tSceneUserMap = null;
		if (valueMap.containsKey("id") && StringUtils.isNotBlank(valueMap.get("id").toString())) {
			tSceneUserMap = sceneUserMapDAO.findOne(valueMap.get("id").toString());
		} else {
			tSceneUserMap = new TSceneUserMap();
		}
		if (valueMap.containsKey("user") && StringUtils.isNotBlank(valueMap.get("user").toString())) {
			tSceneUserMap.setFuserId(Integer.parseInt(valueMap.get("user").toString()));
		} else {
			tSceneUserMap.setFuserId(null);
		}
		tSceneUserMap.setFcreateTime(new Date());
		tSceneUserMap.setFsceneCode(valueMap.get("fSceneCode").toString());
		tSceneUserMap.setFstatus(Integer.parseInt(valueMap.get("status").toString()));
		sceneUserMapDAO.save(tSceneUserMap);

	}

	public Integer findBySceneCode(Map<String, Object> valueMap) {
		List<TSceneUserMap> list = sceneUserMapDAO.findBySceneCode(valueMap.get("fSceneCode").toString());
		return list.size();
	}

	@Transactional(readOnly = true)
	public TSceneUserMap getTSceneUserMap(String Id) {
		return sceneUserMapDAO.getOne(Id);
	}

	@Transactional
	public void createEventExcel(Map<String, Object> map, String datePath, String excelFileName, String sessionid) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as id,t.ftitle as ftitle,t.fbdId as fbdId,t.fstatus as fstatus from TEvent t where t.fstatus < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		ExcelTemplate excel = ExcelTemplate.getInstance().readTemplateClassPath("/template/excel/eventTemp.xlsx");
		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("excelEventPath")).append("/").append(datePath).append("/");

		for (Map<String, Object> amap : list) {
			excel.creatNewRow();
			if (amap.get("ftitle") != null && StringUtils.isNotBlank(amap.get("ftitle").toString())) {
				excel.createNewCol(amap.get("ftitle").toString());
			}
			if (amap.get("id") != null && StringUtils.isNotBlank(amap.get("id").toString())) {
				excel.createNewCol(amap.get("id").toString());
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				excel.createNewCol(DictionaryUtil.getString(DictionaryUtil.EventStatus, ((Integer) amap.get("fstatus")),
						shiroUser.getLanguage()));
			}
			if (amap.get("fbdId") != null && StringUtils.isNotBlank(amap.get("fbdId").toString())) {
				excel.createNewCol(userDAO.getOne((Long) amap.get("fbdId")).getRealname());
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
	public void getFinanceReconList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT o.fstatus as fstatus, g.fcount as fcount, o.ftotal as ftotal,")
				.append(" o.fchangeAmount as fchangeAmount, g.ftotalPrice as ftotalPrice")
				.append(" FROM TOrder o LEFT JOIN TOrderGoods g ON o.id = g.forderId")
				.append(" WHERE o.fstatus < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		
		try {
			if (valueMap.containsKey("o_fpayType") && StringUtils.isNotBlank(valueMap.get("o_fpayType").toString())) {
				hql.append(" and o.fpayType = :o_fpayType ");
				hqlMap.put("o_fpayType", Integer.valueOf(valueMap.get("o_fpayType").toString()));
			}
			if (valueMap.containsKey("o_fsponsorFullName")
					&& StringUtils.isNotBlank(valueMap.get("o_fsponsorFullName").toString())) {
				hql.append(" and o.fsponsorFullName = :o_fsponsorFullName ");
				hqlMap.put("o_fsponsorFullName", valueMap.get("o_fsponsorFullName").toString());
			}
			if (valueMap.containsKey("fpayTimeStart")
					&& StringUtils.isNotBlank(valueMap.get("fpayTimeStart").toString())) {
				hql.append(" and o.fpayTime >= :fpayTimeStart ");
				hqlMap.put("fpayTimeStart",
						DateUtils.parseDate(valueMap.get("fpayTimeStart").toString(), "yyyy-MM-dd"));
			}
			if (valueMap.containsKey("fpayTimeEnd") && StringUtils.isNotBlank(valueMap.get("fpayTimeEnd").toString())) {
				hql.append(" and o.fpayTime < :fpayTimeEnd ");
				// 选择的截止日期加一天，保证了查询条件的完整性，解决了时分秒的问题。
				hqlMap.put("fpayTimeEnd", DateUtils
						.addDays(DateUtils.parseDate(valueMap.get("fpayTimeEnd").toString(), "yyyy-MM-dd"), 1));
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}

		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();
		for (Map<String, Object> amap : list) {
			amap.put("rate", "0.28%");
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatus", DictionaryUtil.getString(DictionaryUtil.OrderStatus,
						((Integer) amap.get("fstatus")), shiroUser.getLanguage()));
			}
			if (amap.get("ftotalPrice") != null && StringUtils.isNotBlank(amap.get("ftotalPrice").toString())) {
				BigDecimal rate = (BigDecimal) amap.get("ftotalPrice");
				BigDecimal multiply = rate.multiply(BigDecimal.valueOf(0.0028D));
				BigDecimal ftotalPrice = rate.subtract(multiply).setScale(2, BigDecimal.ROUND_HALF_UP);
				amap.put("ftotalPrice", ftotalPrice);
			}
		}
	}

	@Transactional(readOnly = true)
	public void createFinanceReconExcel(Map<String, Object> valueMap, String datePath, String excelFileName) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT o.fstatus as fstatus, g.fcount as fcount, o.ftotal as ftotal,")
				.append(" o.fchangeAmount as fchangeAmount, g.ftotalPrice as ftotalPrice")
				.append(" FROM TOrder o LEFT JOIN TOrderGoods g ON o.id = g.forderId")
				.append(" WHERE o.fstatus < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		
		try {
			if (valueMap.containsKey("o_fpayType") && StringUtils.isNotBlank(valueMap.get("o_fpayType").toString())) {
				hql.append(" and o.fpayType = :o_fpayType ");
				hqlMap.put("o_fpayType", Integer.valueOf(valueMap.get("o_fpayType").toString()));
			}
			if (valueMap.containsKey("o_fsponsorFullName")
					&& StringUtils.isNotBlank(valueMap.get("o_fsponsorFullName").toString())) {
				hql.append(" and o.fsponsorFullName = :o_fsponsorFullName ");
				hqlMap.put("o_fsponsorFullName", valueMap.get("o_fsponsorFullName").toString());
			}
			if (valueMap.containsKey("fpayTimeStart")
					&& StringUtils.isNotBlank(valueMap.get("fpayTimeStart").toString())) {
				hql.append(" and o.fpayTime >= :fpayTimeStart ");
				hqlMap.put("fpayTimeStart",
						DateUtils.parseDate(valueMap.get("fpayTimeStart").toString(), "yyyy-MM-dd"));
			}
			if (valueMap.containsKey("fpayTimeEnd") && StringUtils.isNotBlank(valueMap.get("fpayTimeEnd").toString())) {
				hql.append(" and o.fpayTime < :fpayTimeEnd ");
				// 选择的截止日期加一天，保证了查询条件的完整性，解决了时分秒的问题。
				hqlMap.put("fpayTimeEnd", DateUtils
						.addDays(DateUtils.parseDate(valueMap.get("fpayTimeEnd").toString(), "yyyy-MM-dd"), 1));
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);
		ExcelTemplate excel = ExcelTemplate.getInstance()
				.readTemplateClassPath("/template/excel/financeReconTemp.xlsx");
		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("excelfinanceReconPath")).append("/").append(datePath).append("/");
		
		for (Map<String, Object> amap : list) {
			excel.creatNewRow();
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				excel.createNewCol(DictionaryUtil.getString(DictionaryUtil.OrderStatus, ((Integer) amap.get("fstatus")),
						shiroUser.getLanguage()));
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("forderNum") != null && StringUtils.isNotBlank(amap.get("forderNum").toString())) {
				excel.createNewCol(amap.get("forderNum").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("ftotal") != null && StringUtils.isNotBlank(amap.get("ftotal").toString())) {
				excel.createNewCol(amap.get("ftotal").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fchangeAmount") != null && StringUtils.isNotBlank(amap.get("fchangeAmount").toString())) {
				excel.createNewCol(amap.get("fchangeAmount").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			excel.createNewCol("0.28%");
			if (amap.get("ftotalPrice") != null && StringUtils.isNotBlank(amap.get("ftotalPrice").toString())) {
				BigDecimal rate = (BigDecimal) amap.get("ftotalPrice");
				BigDecimal multiply = rate.multiply(BigDecimal.valueOf(0.0028D));
				BigDecimal ftotalPrice = rate.subtract(multiply).setScale(2, BigDecimal.ROUND_HALF_UP);
				excel.createNewCol(ftotalPrice.toString());
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
	
	

}
