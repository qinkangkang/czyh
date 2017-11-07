package com.czyh.czyhweb.service.finance;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
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

import com.czyh.czyhweb.dao.OrderVerificationDAO;
import com.czyh.czyhweb.dao.SponsorBalanceDAO;
import com.czyh.czyhweb.dao.SponsorDAO;
import com.czyh.czyhweb.dao.SponsorStatementDAO;
import com.czyh.czyhweb.dao.UserDAO;
import com.czyh.czyhweb.entity.TOrderVerification;
import com.czyh.czyhweb.entity.TSponsor;
import com.czyh.czyhweb.entity.TSponsorBalance;
import com.czyh.czyhweb.entity.TSponsorStatement;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.CommonService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.PropertiesUtil;
import com.czyh.czyhweb.util.excel.ExcelTemplate;

/**
 * 结算管理类
 * 
 * @author jinshengzhi
 */
@Component
@Transactional
public class SettlementService {
	private static final Logger logger = LoggerFactory.getLogger(SettlementService.class);

	@Autowired
	private CommonService commonService;

	@Autowired
	private OrderVerificationDAO orderVerificationDAO;

	@Autowired
	private SponsorDAO sponsorDAO;

	@Autowired
	private SponsorBalanceDAO sponsorBalanceDAO;

	@Autowired
	private SponsorStatementDAO sponsorStatementDAO;

	@Autowired
	private UserDAO userDAO;

	/**
	 * 定时自动结算方法，根据商家ID，将一个时间区间内的核销单记录
	 * 
	 * @param merchantId
	 */
	public void autoSettlement(String merchantId, Date startDate, Date endDate) {
		Date now = new Date();
		TSponsor tSponsor = sponsorDAO.getOne(merchantId);

		List<TOrderVerification> list = orderVerificationDAO.findByUnSettlement(merchantId, startDate, endDate);
		if (CollectionUtils.isNotEmpty(list)) {

			TSponsorStatement sponsorStatement = new TSponsorStatement();
			sponsorStatement.setFstatementNum(tSponsor.getFnumber() + DateFormatUtils.format(endDate, "yyyyMMdd"));
			sponsorStatement.setFbeginTime(startDate);
			sponsorStatement.setFendTime(endDate);
			sponsorStatement.setFoperator(0L);
			sponsorStatement.setFremark("系统自动结算生成。");
			sponsorStatement.setFtime(now);
			sponsorStatement.setTSponsor(tSponsor);
			sponsorStatement.setFstatus(10);
			sponsorStatement = sponsorStatementDAO.save(sponsorStatement);
			BigDecimal total = BigDecimal.ZERO;
			TSponsorBalance tSponsorBalance = null;

			for (TOrderVerification tOrderVerification : list) {

				String eventTitle = tOrderVerification.getTOrder().getFeventTitle();
				// 生成原价订单结算明细
				tSponsorBalance = new TSponsorBalance();
				tSponsorBalance.setFobjectId(tOrderVerification.getId());
				tSponsorBalance.setFobjectNum(eventTitle);
				tSponsorBalance.setFamount(tOrderVerification.getForderOriginalAmount());
				tSponsorBalance.setForiginalAmount(tOrderVerification.getForderOriginalAmount());
				tSponsorBalance.setFtype(10);
				tSponsorBalance.setFstatus(10);
				tSponsorBalance.setFoperator(0L);
				tSponsorBalance.setFtime(now);
				tSponsorBalance.setTSponsorStatement(sponsorStatement);
				tSponsorBalance.setTSponsor(tSponsor);
				sponsorBalanceDAO.save(tSponsorBalance);

				// 零元订单不生产扣费结算明细
				if (tOrderVerification.getForderOriginalAmount().compareTo(BigDecimal.ZERO) != 0) {
					// 生成服务费结算明细
					tSponsorBalance = new TSponsorBalance();
					tSponsorBalance.setFobjectId(tOrderVerification.getId());
					tSponsorBalance.setFobjectNum(eventTitle);
					tSponsorBalance.setFamount(tOrderVerification.getForderOriginalAmount()
							.subtract(tOrderVerification.getForderAmount()));
					tSponsorBalance.setForiginalAmount(tOrderVerification.getForderOriginalAmount());
					tSponsorBalance.setFtype(20);
					tSponsorBalance.setFstatus(10);
					tSponsorBalance.setFoperator(0L);
					tSponsorBalance.setFtime(now);
					tSponsorBalance.setTSponsorStatement(sponsorStatement);
					tSponsorBalance.setTSponsor(tSponsor);
					sponsorBalanceDAO.save(tSponsorBalance);
				}

				total = total.add(tOrderVerification.getForderAmount());

				orderVerificationDAO.updateStatus(tOrderVerification.getId(), 20);
			}
			// 将结算的总额回写到结算单总
			sponsorStatement.setFamount(total);
			sponsorStatementDAO.save(sponsorStatement);

			sponsorDAO.updateBalance(merchantId, total);
		} else {
			TSponsorStatement sponsorStatement = new TSponsorStatement();
			sponsorStatement.setFstatementNum(tSponsor.getFnumber() + DateFormatUtils.format(endDate, "yyyyMMdd"));
			sponsorStatement.setFamount(BigDecimal.ZERO);
			sponsorStatement.setFbeginTime(startDate);
			sponsorStatement.setFendTime(endDate);
			sponsorStatement.setFoperator(0L);
			sponsorStatement.setFremark("系统自动结算生成。");
			sponsorStatement.setFtime(now);
			sponsorStatement.setTSponsor(new TSponsor(merchantId));
			sponsorStatement.setFstatus(10);
			sponsorStatementDAO.save(sponsorStatement);
		}
	}

	@Transactional(readOnly = true)
	public void getSettlementList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.fstatementNum as fstatementNum, t.TSponsor.id as sponsorId ,t.TSponsor.fname as fname,t.TSponsor.fbdId as fbdId,")
				.append(" t.fbeginTime as fbeginTime, t.fendTime as fendTime, t.famount as famount, t.foperator as foperator, ")
				.append("t.fstatus as fstatus,t.forderChangelAmount as forderChangelAmount,t.fpadinAmount as fpadinAmount from TSponsorStatement t where t.fstatus < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_forderNum") && StringUtils.isNotBlank(valueMap.get("s_forderNum").toString())) {
				hql.append(" and t.fstatementNum = :s_forderNum ");
				hqlMap.put("s_forderNum", valueMap.get("s_forderNum").toString());
			}
			if (valueMap.containsKey("s_fsponsor") && StringUtils.isNotBlank(valueMap.get("s_fsponsor").toString())) {
				hql.append(" and t.TSponsor.id = :s_fsponsor ");
				hqlMap.put("s_fsponsor", valueMap.get("s_fsponsor").toString());
			}
			if (valueMap.containsKey("fcreateTimeStart")
					&& StringUtils.isNotBlank(valueMap.get("fcreateTimeStart").toString())) {
				hql.append(" and t.fendTime >= :fcreateTimeStart ");
				hqlMap.put("fcreateTimeStart",
						DateUtils.parseDate(valueMap.get("fcreateTimeStart").toString(), "yyyy-MM-dd"));
			}
			if (valueMap.containsKey("fcreateTimeEnd")
					&& StringUtils.isNotBlank(valueMap.get("fcreateTimeEnd").toString())) {
				hql.append(" and t.fendTime < :fcreateTimeEnd ");
				// 选择的截止日期加一天，保证了查询条件的完整性，解决了时分秒的问题。
				hqlMap.put("fcreateTimeEnd", DateUtils
						.addDays(DateUtils.parseDate(valueMap.get("fcreateTimeEnd").toString(), "yyyy-MM-dd"), 1));
			}
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.ftime desc");
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
			if (amap.get("fbeginTime") != null && StringUtils.isNotBlank(amap.get("fbeginTime").toString())) {
				date = (Date) amap.get("fbeginTime");
				amap.put("fbeginTime", DateFormatUtils.format(date, "yyyy-MM-dd"));
			}
			if (amap.get("fendTime") != null && StringUtils.isNotBlank(amap.get("fendTime").toString())) {
				date = (Date) amap.get("fendTime");
				amap.put("fendTime", DateFormatUtils.format(date, "yyyy-MM-dd"));
			}
			if (amap.get("fverificationTime") != null
					&& StringUtils.isNotBlank(amap.get("fverificationTime").toString())) {
				date = (Date) amap.get("fverificationTime");
				amap.put("fverificationTime",
						DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
			}
			if (amap.get("famount") != null && StringUtils.isNotBlank(amap.get("famount").toString())) {
				info.delete(0, info.length());
				amap.put("famount", info.append(amap.get("famount").toString()).append("元").toString());
			}
			if (amap.get("forderChangelAmount") != null
					&& StringUtils.isNotBlank(amap.get("forderChangelAmount").toString())) {
				info.delete(0, info.length());
				amap.put("forderChangelAmount",
						info.append(amap.get("forderChangelAmount").toString()).append("元").toString());
			}
			if (amap.get("fpadinAmount") != null && StringUtils.isNotBlank(amap.get("fpadinAmount").toString())) {
				info.delete(0, info.length());
				amap.put("fpadinAmount", info.append(amap.get("fpadinAmount").toString()).append("元").toString());
			}
			if (amap.get("fpayType") != null && StringUtils.isNotBlank(amap.get("fpayType").toString())) {
				amap.put("fpayType", DictionaryUtil.getString(DictionaryUtil.PayType, ((Integer) amap.get("fpayType")),
						shiroUser.getLanguage()));
			}
			if (amap.get("fbdId") != null && StringUtils.isNotBlank(amap.get("fbdId").toString())) {
				amap.put("fbdId", userDAO.findOne((long) (amap.get("fbdId"))).getRealname());
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatusString", DictionaryUtil.getString(DictionaryUtil.OrderStatus,
						((Integer) amap.get("fstatus")), shiroUser.getLanguage()));
			}
		}
	}

	@Transactional(readOnly = true)
	public TSponsorStatement getSettlement(String statementId) {
		return sponsorStatementDAO.getOne(statementId);
	}

	@Transactional(readOnly = true)
	public void getSettlementDetailList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId,v.fsettlementType as fsettlementType,t.ftype as type,o.id as orderId, o.forderNum as orderNum, t.famount as famount, t.foriginalAmount as foriginalAmount,t.ftype as ftype, t.fobjectId as fobjectId, t.fobjectNum as fobjectNum, t.foperator as foperator, t.ftime as ftime, t.fremark as fremark ")
				.append(" ,v.fcreateTime as fverificationTime,v.fclientOperate as fclientOperate,o.feventTitle as feventTitle,c.fname as fverificationname from TOrderVerification v inner join v.TOrder o inner join v.TCustomerByFoperator c inner join TSponsorBalance t  on v.id = t.fobjectId  where t.TSponsorStatement.id = :statementId and t.fstatus < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("statementId", valueMap.get("statementId").toString());
		hql.append(" order by t.fobjectId");
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		StringBuilder info = new StringBuilder();
		Date date = null;
		for (Map<String, Object> amap : list) {
			if (amap.get("fverificationTime") != null
					&& StringUtils.isNotBlank(amap.get("fverificationTime").toString())) {
				date = (Date) amap.get("fverificationTime");
				amap.put("fverificationTime",
						DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
			}
			if (amap.get("fclientOperate") != null && StringUtils.isNotBlank(amap.get("fclientOperate").toString())) {
				if (((Integer) amap.get("fclientOperate")).intValue() == 1) {
					amap.put("fclientOperate", "二维码核销");
				} else if (((Integer) amap.get("fclientOperate")).intValue() == 2) {
					amap.put("fclientOperate", "手动核销");
				} else if (((Integer) amap.get("fclientOperate")).intValue() == 3) {
					amap.put("fclientOperate", "自动核销");
				}
			}
			if (amap.get("famount") != null && StringUtils.isNotBlank(amap.get("famount").toString())) {
				amap.put("ftotal", amap.get("famount").toString());
			}
			if (amap.get("fsettlementType") != null && StringUtils.isNotBlank(amap.get("fsettlementType").toString())) {
				amap.put("typeString", DictionaryUtil.getString(DictionaryUtil.SettlementType,
						((Integer) amap.get("fsettlementType")), shiroUser.getLanguage()));
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatusString", DictionaryUtil.getString(DictionaryUtil.OrderStatus,
						((Integer) amap.get("fstatus")), shiroUser.getLanguage()));
			}
			if (amap.get("ftype") != null && StringUtils.isNotBlank(amap.get("ftype").toString())) {
				if (((Integer) amap.get("ftype")).intValue() == 20) {
					amap.put("ftotal", "-" + amap.get("famount").toString());
				}
			}
		}
	}

	/**
	 * 定时自动结算方法，根据商家ID，将一个时间区间内的核销单记录
	 * 
	 * @param merchantId
	 */
	public void autoSettlement1(Date now) {
		Date startDate = DateUtils.truncate(DateUtils.addDays(now, -7), Calendar.DAY_OF_MONTH);
		Date end = DateUtils.truncate(now, Calendar.DAY_OF_MONTH);
		Date endDate = DateUtils.addSeconds(end, -1);
		List<TSponsor> sponsorList = sponsorDAO.findByStatus();

		List<TOrderVerification> list = orderVerificationDAO.findByTime(startDate, endDate);

		List<TSponsorStatement> sponsorStatementList = new ArrayList<TSponsorStatement>();
		Map<String, TSponsorStatement> sMap = new HashMap<String, TSponsorStatement>();
		for (TSponsor tSponsor : sponsorList) {
			TSponsorStatement sponsorStatement = new TSponsorStatement();
			sponsorStatement.setFstatementNum(tSponsor.getFnumber() + DateFormatUtils.format(endDate, "yyyyMMdd"));
			sponsorStatement.setFamount(BigDecimal.ZERO);
			sponsorStatement.setFbeginTime(startDate);
			sponsorStatement.setFendTime(endDate);
			sponsorStatement.setFoperator(0L);
			sponsorStatement.setFremark("系统自动结算生成。");
			sponsorStatement.setFtime(now);
			sponsorStatement.setTSponsor(tSponsor);
			sponsorStatement.setFstatus(10);
			sponsorStatement.setForderChangelAmount(BigDecimal.ZERO);
			sponsorStatement.setFpadinAmount(BigDecimal.ZERO);
			sMap.put(tSponsor.getId(), sponsorStatement);
		}

		List<TSponsorBalance> sponsorBalanceList = new ArrayList<TSponsorBalance>();
		TSponsorBalance tSponsorBalance = null;
		for (TOrderVerification tOrderVerification : list) {
			if (tOrderVerification.getFsettlementType().intValue() != 10) {
				String eventTitle = tOrderVerification.getTOrder().getFeventTitle();
				TSponsorStatement sponsorStatement = sMap.get(tOrderVerification.getTSponsor().getId());
				// 生成原价订单结算明细
				tSponsorBalance = new TSponsorBalance();
				tSponsorBalance.setFobjectId(tOrderVerification.getId());
				tSponsorBalance.setFobjectNum(eventTitle);
				tSponsorBalance.setFamount(tOrderVerification.getForderAmount());
				tSponsorBalance.setForiginalAmount(tOrderVerification.getForderOriginalAmount());
				tSponsorBalance.setFtype(10);
				tSponsorBalance.setFstatus(10);
				tSponsorBalance.setFoperator(0L);
				tSponsorBalance.setFtime(now);
				tSponsorBalance.setTSponsorStatement(sMap.get(tOrderVerification.getTSponsor().getId()));
				tSponsorBalance.setTSponsor(tOrderVerification.getTSponsor());
				sponsorBalanceList.add(tSponsorBalance);
				sponsorStatement.setFamount((sponsorStatement.getFamount()).add(tOrderVerification.getForderAmount()));
				sponsorStatement.setForderChangelAmount(sponsorStatement.getForderChangelAmount()
						.add(tOrderVerification.getForderChangelAmount() != null
								? tOrderVerification.getForderChangelAmount() : BigDecimal.ZERO));
				sponsorStatement.setFpadinAmount(
						sponsorStatement.getFpadinAmount().add(tOrderVerification.getForderOriginalAmount()));
			}

		}

		for (Entry<String, TSponsorStatement> entry : sMap.entrySet()) {
			sponsorStatementList.add(entry.getValue());
		}
		sponsorStatementDAO.save(sponsorStatementList);
		sponsorBalanceDAO.save(sponsorBalanceList);
		orderVerificationDAO.updateStatusByTime(startDate, endDate, 20);
		for (TSponsor tSponsor : sponsorList) {
			if (sMap.get(tSponsor.getId()).getFamount().compareTo(BigDecimal.ZERO) != 0) {
				sponsorDAO.updateBalance(tSponsor.getId(), sMap.get(tSponsor.getId()).getFamount());
			}
		}

	}

	@Transactional
	public void createStatementExcel(Map<String, Object> map, String datePath, String excelFileName, String sessionid) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.fstatementNum as fstatementNum, t.TSponsor.fname as fname,t.TSponsor.fbdId as fbdId,")
				.append(" t.fbeginTime as fbeginTime, t.fendTime as fendTime, t.famount as famount, t.foperator as foperator, ")
				.append("t.fstatus as fstatus,t.forderChangelAmount as forderChangelAmount,t.fpadinAmount as fpadinAmount from TSponsorStatement t where t.fstatus < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (map.containsKey("s_fsponsor") && StringUtils.isNotBlank(map.get("s_fsponsor").toString())) {
				hql.append(" and t.TSponsor.id = :s_fsponsor ");
				hqlMap.put("s_fsponsor", map.get("s_fsponsor").toString());
			}
			if (map.containsKey("s_forderNum") && StringUtils.isNotBlank(map.get("s_forderNum").toString())) {
				hql.append(" and t.fstatementNum = :s_forderNum ");
				hqlMap.put("s_forderNum", map.get("s_forderNum").toString());
			}
			if (map.containsKey("fcreateTimeStart") && StringUtils.isNotBlank(map.get("fcreateTimeStart").toString())) {
				hql.append(" and t.fendTime >= :fcreateTimeStart ");
				hqlMap.put("fcreateTimeStart",
						DateUtils.parseDate(map.get("fcreateTimeStart").toString(), "yyyy-MM-dd"));
			}
			if (map.containsKey("fcreateTimeEnd") && StringUtils.isNotBlank(map.get("fcreateTimeEnd").toString())) {
				hql.append(" and t.fendTime < :fcreateTimeEnd ");
				// 选择的截止日期加一天，保证了查询条件的完整性，解决了时分秒的问题。
				hqlMap.put("fcreateTimeEnd",
						DateUtils.addDays(DateUtils.parseDate(map.get("fcreateTimeEnd").toString(), "yyyy-MM-dd"), 1));
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}

		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		ExcelTemplate excel = ExcelTemplate.getInstance().readTemplateClassPath("/template/excel/statementTemp.xlsx");
		StringBuilder rootPath = new StringBuilder("E:/DB").append(PropertiesUtil.getProperty("excelStatementPath"))
				.append("/").append(datePath).append("/");

		StringBuilder info = new StringBuilder();
		Date date = null;
		for (Map<String, Object> amap : list) {
			excel.creatNewRow();
			if (amap.get("fstatementNum") != null && StringUtils.isNotBlank(amap.get("fstatementNum").toString())) {
				excel.createNewCol(amap.get("fstatementNum").toString());
			}
			if (amap.get("fname") != null && StringUtils.isNotBlank(amap.get("fname").toString())) {
				excel.createNewCol(amap.get("fname").toString());
			}
			if (amap.get("fbdId") != null && StringUtils.isNotBlank(amap.get("fbdId").toString())) {
				excel.createNewCol(userDAO.findOne((long) (amap.get("fbdId"))).getRealname());
			}
			if (amap.get("fbeginTime") != null && StringUtils.isNotBlank(amap.get("fbeginTime").toString())) {
				date = (Date) amap.get("fbeginTime");
				excel.createNewCol(DateFormatUtils.format(date, "yyyy-MM-dd"));
			}
			if (amap.get("fendTime") != null && StringUtils.isNotBlank(amap.get("fendTime").toString())) {
				date = (Date) amap.get("fendTime");
				excel.createNewCol(DateFormatUtils.format(date, "yyyy-MM-dd"));
			}
			if (amap.get("famount") != null && StringUtils.isNotBlank(amap.get("famount").toString())) {
				info.delete(0, info.length());
				excel.createNewCol(info.append(amap.get("famount").toString()).append("元").toString());
			}
			if (amap.get("forderChangelAmount") != null
					&& StringUtils.isNotBlank(amap.get("forderChangelAmount").toString())) {
				info.delete(0, info.length());
				excel.createNewCol(info.append(amap.get("forderChangelAmount").toString()).append("元").toString());
			}
			if (amap.get("fpadinAmount") != null && StringUtils.isNotBlank(amap.get("fpadinAmount").toString())) {
				info.delete(0, info.length());
				excel.createNewCol(info.append(amap.get("fpadinAmount").toString()).append("元").toString());
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

	public void allSettlement() {
		Date date = null;
		try {
			date = DateUtils.parseDate("2016-07-25", "yyyy-MM-dd");
		} catch (ParseException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
		Date now = null;
		for (int i = 0; i < 1000; i++) {
			date = DateUtils.addDays(date, 7);
			now = DateUtils.truncate(date, Calendar.DAY_OF_MONTH);
			if (now.after(new Date())) {
				break;
			} else {
				this.autoSettlement1(now);
			}
		}
	}

}