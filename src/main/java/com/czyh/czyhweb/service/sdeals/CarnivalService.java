package com.czyh.czyhweb.service.sdeals;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.shiro.SecurityUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;

import com.czyh.czyhweb.dao.CarnivalDAO;
import com.czyh.czyhweb.dao.CarnivalDayPrizeDAO;
import com.czyh.czyhweb.dao.CarnivalPrizeDAO;
import com.czyh.czyhweb.dao.EventDAO;
import com.czyh.czyhweb.dao.EventSessionDAO;
import com.czyh.czyhweb.dao.EventSpecDAO;
import com.czyh.czyhweb.dao.ImageDAO;
import com.czyh.czyhweb.dto.MedalRuleDTO;
import com.czyh.czyhweb.dto.ResponseDTO;
import com.czyh.czyhweb.entity.TCarnival;
import com.czyh.czyhweb.entity.TCarnivalDayPrize;
import com.czyh.czyhweb.entity.TCarnivalPrize;
import com.czyh.czyhweb.entity.TEventSession;
import com.czyh.czyhweb.entity.TEventSpec;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.CommonService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.HttpClientUtil;
import com.czyh.czyhweb.util.wx.message.WxInterfaceApi;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component
@Transactional
public class CarnivalService {

	private static final Logger logger = LoggerFactory.getLogger(CarnivalService.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private CommonService commonService;

	@Autowired
	private CarnivalDAO carnivalDAO;

	@Autowired
	private CarnivalPrizeDAO carnivalPrizeDAO;

	@Autowired
	private EventSessionDAO eventSessionDAO;

	@Autowired
	private EventSpecDAO eventSpecDAO;

	@Autowired
	private EventDAO eventDAO;

	@Autowired
	private ImageDAO imageDAO;

	@Autowired
	private CarnivalDayPrizeDAO carnivalDayPrizeDAO;

	@Transactional(readOnly = true)
	public void getEventCarnivalList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId,t.ftitle as ftitle,t.fstartTime as fstartTime,t.fendTime as fendTime,t.fdayNumber as fdayNumber,t.fimage as fimage,t.fchannel as fchannel,t.flotteryNumber as flotteryNumber,t.fcredentialNumber as fcredentialNumber,t.fodds as fodds,t.frule as frule,t.fstatus as fstatus from TCarnival t where t.fstatus<999");
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
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.fstartTime desc");
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
				amap.put("fstatusString", DictionaryUtil.getString(DictionaryUtil.CarnivalStatus,
						(Integer) amap.get("fstatus"), shiroUser.getLanguage()));
			}

		}
	}

	public void addCarnival(Map<String, Object> valueMap) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		TCarnival tCarnival = new TCarnival();
		if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
			tCarnival.setFtitle((valueMap.get("ftitle").toString()));
		}
		try {
			if (valueMap.containsKey("fstartTime") && StringUtils.isNotBlank(valueMap.get("fstartTime").toString())) {
				tCarnival.setFstartTime(DateUtils.parseDate(valueMap.get("fstartTime").toString(), "yyyy-MM-dd"));
			}
			if (valueMap.containsKey("fendTime") && StringUtils.isNotBlank(valueMap.get("fendTime").toString())) {
				tCarnival.setFendTime(DateUtils.parseDate(valueMap.get("fendTime").toString(), "yyyy-MM-dd"));
			}
		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}

		if (valueMap.containsKey("fdayNumber") && StringUtils.isNotBlank(valueMap.get("fdayNumber").toString())) {
			tCarnival.setFdayNumber(Integer.valueOf(valueMap.get("fdayNumber").toString()));
		}
		if (valueMap.containsKey("fimage") && StringUtils.isNotBlank(valueMap.get("fimage").toString())) {
			tCarnival.setFimage(Long.valueOf(valueMap.get("fimage").toString()));
		}
		if (valueMap.containsKey("fchannel") && StringUtils.isNotBlank(valueMap.get("fchannel").toString())) {
			tCarnival.setFchannel(Integer.valueOf(valueMap.get("fchannel").toString()));
		}
		if (valueMap.containsKey("flotteryNumber")
				&& StringUtils.isNotBlank(valueMap.get("flotteryNumber").toString())) {
			tCarnival.setFlotteryNumber(Integer.valueOf(valueMap.get("flotteryNumber").toString()));
		}
		if (valueMap.containsKey("fcredentialNumber")
				&& StringUtils.isNotBlank(valueMap.get("fcredentialNumber").toString())) {
			tCarnival.setFcredentialNumber(Integer.valueOf(valueMap.get("fcredentialNumber").toString()));
		}
		if (valueMap.containsKey("fodds") && StringUtils.isNotBlank(valueMap.get("fodds").toString())) {
			tCarnival.setFodds(new BigDecimal(valueMap.get("fodds").toString()));
		}
		if (valueMap.containsKey("frule") && StringUtils.isNotBlank(valueMap.get("frule").toString())) {
			tCarnival.setFrule(valueMap.get("frule").toString());
		}
		tCarnival.setFstatus(30);

		// 发送二维码创建连接
		try {
			String qrCodeUrl = new StringBuilder().append(Constant.weChatUrl).append(WxInterfaceApi.QRCODEWXAPI)
					.append("/").append(WxInterfaceApi.GETQRCODECREATELASTTICKET).toString();
			Map<String, Object> paramsMap = Maps.newHashMap();
			String codeNum;
			String pointCodeNum = carnivalDAO.getMaxPointCode();
			if (pointCodeNum != null) {
				codeNum = pointCodeNum;
			} else {
				codeNum = "29000001";
			}
			paramsMap.put("scene", codeNum);
			String reuslt = HttpClientUtil.callUrlPost(qrCodeUrl, paramsMap);
			ResponseDTO responseDTO = mapper.fromJson(reuslt, ResponseDTO.class);
			tCarnival.setFsceneStr(codeNum);
			tCarnival.setFwxQrCodeUrl(responseDTO.getData().get("qrCodeUrl").toString());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
		carnivalDAO.save(tCarnival);
	}

	public void delCarnival(String fID) {
		carnivalDAO.delCarnival(999, fID);
	}

	public void editCarnival(Map<String, Object> valueMap) {

		TCarnival tCarnival = carnivalDAO.getOne(valueMap.get("id").toString());

		if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
			tCarnival.setFtitle(valueMap.get("ftitle").toString());
		} else {
			tCarnival.setFtitle(null);
		}
		try {
			if (valueMap.containsKey("fstartTime") && StringUtils.isNotBlank(valueMap.get("fstartTime").toString())) {
				tCarnival.setFstartTime(DateUtils.parseDate(valueMap.get("fstartTime").toString(), "yyyy-MM-dd"));
			} else {
				tCarnival.setFstartTime(null);
			}
			if (valueMap.containsKey("fendTime") && StringUtils.isNotBlank(valueMap.get("fendTime").toString())) {
				tCarnival.setFendTime(DateUtils.parseDate(valueMap.get("fendTime").toString(), "yyyy-MM-dd"));
			} else {
				tCarnival.setFendTime(null);
			}
		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
		if (valueMap.containsKey("fdayNumber") && StringUtils.isNotBlank(valueMap.get("fdayNumber").toString())) {
			tCarnival.setFdayNumber(Integer.valueOf(valueMap.get("fdayNumber").toString()));
		} else {
			tCarnival.setFdayNumber(null);
		}
		if (valueMap.containsKey("fimage") && StringUtils.isNotBlank(valueMap.get("fimage").toString())) {
			tCarnival.setFimage(Long.valueOf(valueMap.get("fimage").toString()));
		} else {
			tCarnival.setFimage(null);
		}
		if (valueMap.containsKey("fchannel") && StringUtils.isNotBlank(valueMap.get("fchannel").toString())) {
			tCarnival.setFchannel(Integer.valueOf(valueMap.get("fchannel").toString()));
		} else {
			tCarnival.setFchannel(null);
		}
		if (valueMap.containsKey("flotteryNumber")
				&& StringUtils.isNotBlank(valueMap.get("flotteryNumber").toString())) {
			tCarnival.setFlotteryNumber(Integer.valueOf(valueMap.get("flotteryNumber").toString()));
		} else {
			tCarnival.setFlotteryNumber(null);
		}
		if (valueMap.containsKey("fcredentialNumber")
				&& StringUtils.isNotBlank(valueMap.get("fcredentialNumber").toString())) {
			tCarnival.setFcredentialNumber(Integer.valueOf(valueMap.get("fcredentialNumber").toString()));
		} else {
			tCarnival.setFcredentialNumber(null);
		}
		if (valueMap.containsKey("fodds") && StringUtils.isNotBlank(valueMap.get("fodds").toString())) {
			tCarnival.setFodds(new BigDecimal(valueMap.get("fodds").toString()));
		} else {
			tCarnival.setFodds(null);
		}
		if (valueMap.containsKey("frule") && StringUtils.isNotBlank(valueMap.get("frule").toString())) {
			tCarnival.setFrule(valueMap.get("frule").toString());
		} else {
			tCarnival.setFrule(null);
		}
		carnivalDAO.save(tCarnival);
	}

	@Transactional(readOnly = true)
	public TCarnival getCarnivalDetail(String Id) {
		return carnivalDAO.getOne(Id);
	}

	public void onAndOffCarnival(String fID, Integer Status) {
		carnivalDAO.saveStatusCarnival(Status, fID);
	}

	@Transactional(readOnly = true)
	public void getCarnivalPrizeList(Map<String, Object> valueMap, CommonPage page, String carnivalId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId,t.fcarnivalId as fcarnivalId,t.ftitle as ftitle,t.flevel as flevel,t.fimage as fimage,t.fcount as fcount from TCarnivalPrize t where t.fcarnivalId=:fcarnivalId");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("fcarnivalId", carnivalId);
		try {
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.flevel asc");
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		for (Map<String, Object> amap : list) {

			if (amap.get("flevel") != null && StringUtils.isNotBlank(amap.get("flevel").toString())) {
				amap.put("flevel", DictionaryUtil.getString(DictionaryUtil.CarnivalPrize, (Integer) amap.get("flevel"),
						shiroUser.getLanguage()));
			}

		}
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getEventPrizeList() {
		return commonService.find("select t.id as key, t.ftitle as value from TEvent t where t.fstatus < 999");
	}

	public void addCarnivalPrize(Map<String, Object> valueMap) {

		TCarnivalPrize tCarnivalPrize1 = carnivalPrizeDAO.findRepeatPrize(valueMap.get("carnivalId").toString(),
				Integer.valueOf(valueMap.get("flevel").toString()));

		if (tCarnivalPrize1 != null) {
			if (valueMap.get("flevel").toString().equals("1")) {
				if (tCarnivalPrize1.getFlevel() == 1) {
					throw new ServiceException("当前活动一等奖已经存在,请去添加别的奖品");
				}
			} else if (valueMap.get("flevel").toString().equals("2")) {
				if (tCarnivalPrize1.getFlevel() == 2) {
					throw new ServiceException("当前活动二等奖已经存在,请去添加别的奖品");
				}
			} else if (valueMap.get("flevel").toString().equals("3")) {
				if (tCarnivalPrize1.getFlevel() == 3) {
					throw new ServiceException("当前活动三等奖已经存在,请去添加别的奖品");
				}
			}
		}

		TCarnivalPrize tCarnivalPrize = new TCarnivalPrize();
		if (valueMap.containsKey("carnivalId") && StringUtils.isNotBlank(valueMap.get("carnivalId").toString())) {
			tCarnivalPrize.setFcarnivalId(valueMap.get("carnivalId").toString());
		}
		if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
			tCarnivalPrize.setFtitle((valueMap.get("ftitle").toString()));
		}
		if (valueMap.containsKey("flevel") && StringUtils.isNotBlank(valueMap.get("flevel").toString())) {
			tCarnivalPrize.setFlevel(Integer.valueOf(valueMap.get("flevel").toString()));
		}
		if (valueMap.containsKey("fimage") && StringUtils.isNotBlank(valueMap.get("fimage").toString())) {
			tCarnivalPrize.setFimage(Long.valueOf(valueMap.get("fimage").toString()));
		}

		if (valueMap.containsKey("eventId") && StringUtils.isNotBlank(valueMap.get("eventId").toString())) {
			tCarnivalPrize.setFeventId(valueMap.get("eventId").toString());
		}
		if (valueMap.containsKey("fcount") && StringUtils.isNotBlank(valueMap.get("fcount").toString())) {
			tCarnivalPrize.setFcount(Integer.valueOf(valueMap.get("fcount").toString()));
		}
		// 读取改活动的场次
		TEventSession tEventSession = eventSessionDAO.findSaleFlag(valueMap.get("eventId").toString(), 5);
		if (tEventSession == null) {
			throw new ServiceException("该活动没有促销场次,请尽快去添加场次");
		}

		List<TEventSpec> TEventSpecList = eventSpecDAO.getTEventSpec(tEventSession.getId());
		if (TEventSpecList.isEmpty()) {
			throw new ServiceException("该促销场次下没有规格,请尽快去添加规格");
		}

		tCarnivalPrize.setFsessionId(tEventSession.getId());
		tCarnivalPrize.setFspecId(TEventSpecList.get(0).getId());

		// 兑奖规则
		MedalRuleDTO medalRuleDTO = null;

		String[] prizeNames = null;
		if (valueMap.containsKey("prizeName")
				&& org.springframework.util.ObjectUtils.isArray(valueMap.get("prizeName"))) {
			prizeNames = (String[]) valueMap.get("prizeName");
		} else {
			prizeNames = ArrayUtils.toArray(valueMap.get("prizeName").toString());
		}

		String[] prizeTypes = null;
		if (valueMap.containsKey("prizeType")
				&& org.springframework.util.ObjectUtils.isArray(valueMap.get("prizeType"))) {
			prizeTypes = (String[]) valueMap.get("prizeType");
		} else {
			prizeTypes = ArrayUtils.toArray(valueMap.get("prizeType").toString());
		}

		String[] prizeNumbers = null;
		if (valueMap.containsKey("prizeNumber")
				&& org.springframework.util.ObjectUtils.isArray(valueMap.get("prizeNumber"))) {
			prizeNumbers = (String[]) valueMap.get("prizeNumber");
		} else {
			prizeNumbers = ArrayUtils.toArray(valueMap.get("prizeNumber").toString());
		}

		List<MedalRuleDTO> list = Lists.newArrayList();
		String fruleJson = null;
		for (int i = 0; i < prizeNames.length; i++) {
			medalRuleDTO = new MedalRuleDTO();
			medalRuleDTO.setName(prizeNames[i]);
			medalRuleDTO.setType(Integer.valueOf(prizeTypes[i]));
			medalRuleDTO.setNumber(Integer.valueOf(prizeNumbers[i]));
			list.add(medalRuleDTO);
			fruleJson = mapper.toJson(list);
			tCarnivalPrize.setFrule(fruleJson);
		}

		tCarnivalPrize = carnivalPrizeDAO.save(tCarnivalPrize);

		// 保存发奖天数
		TCarnival tCarnival = carnivalDAO.getOne(tCarnivalPrize.getFcarnivalId());
		int days = Days.daysBetween(new DateTime(tCarnival.getFstartTime()), new DateTime(tCarnival.getFendTime()))
				.getDays();
		TCarnivalDayPrize tCarnivalDayPrize = null;
		for (int i = 0; i < days + 1; i++) {
			tCarnivalDayPrize = new TCarnivalDayPrize();
			System.out.println(tCarnivalPrize.getId());
			tCarnivalDayPrize.setFcarnivalId(tCarnival.getId());
			tCarnivalDayPrize.setFcarnivalDay(DateUtils.addDays(tCarnival.getFstartTime(), (i + 1 - 1)));
			tCarnivalDayPrize.setFcarnivalDaySerial(i + 1);
			tCarnivalDayPrize.setFlevel(tCarnivalPrize.getFlevel());
			tCarnivalDayPrize.setFprizeId(tCarnivalPrize.getId());
			tCarnivalDayPrize.setFacceptCount(0);
			tCarnivalDayPrize.setFreceiveCount(0);
			tCarnivalDayPrize.setFcount(0);
			tCarnivalDayPrize.setFstartTime(DateUtils.addDays(tCarnival.getFstartTime(), (i + 1 - 1)));
			carnivalDayPrizeDAO.save(tCarnivalDayPrize);
		}

		// // 单日奖品列表
		// String[] fbeginTimes = null;
		// if (valueMap.containsKey("fbeginTime")
		// &&
		// org.springframework.util.ObjectUtils.isArray(valueMap.get("fbeginTime")))
		// {
		// fbeginTimes = (String[]) valueMap.get("fbeginTime");
		// } else {
		// fbeginTimes =
		// ArrayUtils.toArray(valueMap.get("fbeginTime").toString());
		// }
		//
		// String[] ffsls = null;
		// if (valueMap.containsKey("ffsl") &&
		// org.springframework.util.ObjectUtils.isArray(valueMap.get("ffsl"))) {
		// ffsls = (String[]) valueMap.get("ffsl");
		// } else {
		// ffsls = ArrayUtils.toArray(valueMap.get("ffsl").toString());
		// }
		//
		// String[] daysPrizeIds = null;
		// if (valueMap.containsKey("daysPrizeId") &&
		// org.springframework.util.ObjectUtils.isArray(valueMap.get("daysPrizeId")))
		// {
		// daysPrizeIds = (String[]) valueMap.get("daysPrizeId");
		// } else {
		// daysPrizeIds =
		// ArrayUtils.toArray(valueMap.get("daysPrizeId").toString());
		// }
		//
		// for (int i = 0; i < fbeginTimes.length; i++) {
		// try {
		// TCarnivalDayPrize tCarnivalDayPrize =
		// carnivalDayPrizeDAO.getOne(daysPrizeIds[i]);
		// System.out.println(i + "循环了几次");
		// tCarnivalDayPrize.setFprizeId(tCarnivalPrize.getId());
		// tCarnivalDayPrize.setFlevel(tCarnivalPrize.getFlevel());
		// tCarnivalDayPrize.setFstartTime(DateUtils.parseDate(fbeginTimes[i],
		// "yyyy-MM-dd HH:mm"));
		// tCarnivalDayPrize.setFcount(Integer.valueOf(ffsls[i]));
		// tCarnivalDayPrize.setFunSendCount(Integer.valueOf(ffsls[i]));
		// tCarnivalDayPrize.setFacceptCount(0);
		// tCarnivalDayPrize.setFreceiveCount(0);
		// carnivalDayPrizeDAO.save(tCarnivalDayPrize);
		// } catch (ParseException e) {
		// logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		// }
		// }

		if (tCarnivalPrize.getFimage() != null) {
			imageDAO.saveStatusAndEntityIdAndEntityType(tCarnivalPrize.getFimage(), 2, tCarnivalPrize.getId(), 10);
		}
		eventDAO.updateSaleType(5, valueMap.get("eventId").toString());
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getDaysPrizeList(String carnivalId) {
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as key, t.fcarnivalDay as value from TCarnivalDayPrize t where t.fcarnivalId =:carnivalId");
		hqlMap.put("carnivalId", carnivalId);

		return commonService.find(hql.toString(), hqlMap);
	}

	public void delCarnivalPrize(String fID) {
		carnivalPrizeDAO.delCarnivalPrize(fID);
		// 同时删除单日奖品表中的数据
		carnivalDayPrizeDAO.delCarnivalDaysPrize(fID);
	}

	@Transactional(readOnly = true)
	public void getCarnivalPrizeDaysList(Map<String, Object> valueMap, CommonPage page, String prizeId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId,t.fcarnivalDaySerial as fcarnivalDaySerial,t.fstartTime as fstartTime,t.fcount as fcount,t.flevel as flevel from TCarnivalDayPrize t where t.fprizeId=:fprizeId");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("fprizeId", prizeId);
		try {
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.fcarnivalDaySerial asc");
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		Date date = null;
		for (Map<String, Object> amap : list) {

			if (amap.get("flevel") != null && StringUtils.isNotBlank(amap.get("flevel").toString())) {
				amap.put("flevel", DictionaryUtil.getString(DictionaryUtil.CarnivalPrize, (Integer) amap.get("flevel"),
						shiroUser.getLanguage()));
			}

			if (amap.get("fstartTime") != null && StringUtils.isNotBlank(amap.get("fstartTime").toString())) {
				date = (Date) amap.get("fstartTime");
				amap.put("fstartTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss"));
			}

		}
	}

	public void editCarnivalPrzieDays(String id, Integer number, String startTime) {
		try {
			TCarnivalDayPrize tCarnivalDayPrize = carnivalDayPrizeDAO.getOne(id);
			if (startTime != null && StringUtils.isNotBlank(startTime)) {
				tCarnivalDayPrize.setFstartTime(DateUtils.parseDate(startTime, "yyyy-MM-dd HH:mm:ss"));
			}
			tCarnivalDayPrize.setFcount(number);
			tCarnivalDayPrize.setFunSendCount(number);
			carnivalDayPrizeDAO.save(tCarnivalDayPrize);
		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
	}

	@Transactional(readOnly = true)
	public TCarnivalPrize getCarnivalPrizeDetail(String Id) {
		return carnivalPrizeDAO.getOne(Id);
	}

	public void editCarnivalPrize(Map<String, Object> valueMap) {

		TCarnivalPrize tCarnivalPrize = carnivalPrizeDAO.getOne(valueMap.get("id").toString());

		if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
			tCarnivalPrize.setFtitle(valueMap.get("ftitle").toString());
		} else {
			tCarnivalPrize.setFtitle(null);
		}
		if (valueMap.containsKey("eventId") && StringUtils.isNotBlank(valueMap.get("eventId").toString())) {
			tCarnivalPrize.setFeventId(valueMap.get("eventId").toString());
		} else {
			tCarnivalPrize.setFeventId(null);
		}
		if (valueMap.containsKey("flevel") && StringUtils.isNotBlank(valueMap.get("flevel").toString())) {
			tCarnivalPrize.setFlevel(Integer.valueOf(valueMap.get("flevel").toString()));
		} else {
			tCarnivalPrize.setFlevel(null);
		}
		if (valueMap.containsKey("fimage") && StringUtils.isNotBlank(valueMap.get("fimage").toString())) {
			tCarnivalPrize.setFimage(Long.valueOf(valueMap.get("fimage").toString()));
		} else {
			tCarnivalPrize.setFimage(null);
		}
		if (valueMap.containsKey("fcount") && StringUtils.isNotBlank(valueMap.get("fcount").toString())) {
			tCarnivalPrize.setFcount(Integer.valueOf(valueMap.get("fcount").toString()));
		} else {
			tCarnivalPrize.setFcount(null);
		}

		// 读取改活动的场次
		TEventSession tEventSession = eventSessionDAO.findSaleFlag(valueMap.get("eventId").toString(), 5);
		if (tEventSession == null) {
			throw new ServiceException("该活动没有促销场次,请尽快去添加场次");
		}

		List<TEventSpec> TEventSpecList = eventSpecDAO.getTEventSpec(tEventSession.getId());
		if (TEventSpecList.isEmpty()) {
			throw new ServiceException("该促销场次下没有规格,请尽快去添加规格");
		}

		tCarnivalPrize.setFsessionId(tEventSession.getId());
		tCarnivalPrize.setFspecId(TEventSpecList.get(0).getId());

		// 编辑碎片规则
		MedalRuleDTO medalRuleDTO = null;

		String[] prizeNames = null;
		if (valueMap.containsKey("prizeName")
				&& org.springframework.util.ObjectUtils.isArray(valueMap.get("prizeName"))) {
			prizeNames = (String[]) valueMap.get("prizeName");
		} else {
			prizeNames = ArrayUtils.toArray(valueMap.get("prizeName").toString());
		}

		String[] prizeTypes = null;
		if (valueMap.containsKey("prizeType")
				&& org.springframework.util.ObjectUtils.isArray(valueMap.get("prizeType"))) {
			prizeTypes = (String[]) valueMap.get("prizeType");
		} else {
			prizeTypes = ArrayUtils.toArray(valueMap.get("prizeType").toString());
		}

		String[] prizeNumbers = null;
		if (valueMap.containsKey("prizeNumber")
				&& org.springframework.util.ObjectUtils.isArray(valueMap.get("prizeNumber"))) {
			prizeNumbers = (String[]) valueMap.get("prizeNumber");
		} else {
			prizeNumbers = ArrayUtils.toArray(valueMap.get("prizeNumber").toString());
		}

		List<MedalRuleDTO> list = Lists.newArrayList();
		String fruleJson = null;
		for (int i = 0; i < prizeNames.length; i++) {
			medalRuleDTO = new MedalRuleDTO();
			medalRuleDTO.setName(prizeNames[i]);
			medalRuleDTO.setType(Integer.valueOf(prizeTypes[i]));
			medalRuleDTO.setNumber(Integer.valueOf(prizeNumbers[i]));
			list.add(medalRuleDTO);
			fruleJson = mapper.toJson(list);
			tCarnivalPrize.setFrule(fruleJson);
		}
		carnivalPrizeDAO.save(tCarnivalPrize);
	}

	@Transactional(readOnly = true)
	public Map<Integer, List<Map<String, Object>>> getCarnivalPrizeListDetail(String fcarnivalId) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId,t.fcarnivalId as fcarnivalId, t.flevel as flevel, t.fcarnivalDay as fcarnivalDay, t.fcarnivalDaySerial as fcarnivalDaySerial,t.fstartTime as fstartTime,t.fendTime as fendTime,t.fcount as fcount,t.funSendCount as funSendCount ,t.facceptCount as facceptCount , t.freceiveCount as freceiveCount from TCarnivalDayPrize t where t.flevel in (1,2,3) and t.fcarnivalId = :fcarnivalId order by t.fcarnivalDaySerial asc");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("fcarnivalId", fcarnivalId);

		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		List<Map<String, Object>> cPrizeOneListMap = Lists.newArrayList();
		List<Map<String, Object>> cPrizeTwoListMap = Lists.newArrayList();
		List<Map<String, Object>> cPrizeThreeListMap = Lists.newArrayList();
		int flevel = 0;
		for (Map<String, Object> map : list) {
			flevel = ((Integer) map.get("flevel")).intValue();
			switch (flevel) {
			case 1: {
				cPrizeOneListMap.add(map);
				break;
			}
			case 2: {
				cPrizeTwoListMap.add(map);
				break;
			}
			case 3: {
				cPrizeThreeListMap.add(map);
				break;
			}
			default: {
				break;
			}
			}
		}

		Map<Integer, List<Map<String, Object>>> map = Maps.newHashMap();
		map.put(1, cPrizeOneListMap);
		map.put(2, cPrizeTwoListMap);
		map.put(3, cPrizeThreeListMap);
		return map;
	}

}