package com.czyh.czyhweb.service.finance;

import java.io.File;
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

import com.czyh.czyhweb.dao.SponsorDAO;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.CommonService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.PropertiesUtil;
import com.czyh.czyhweb.util.excel.ExcelTemplate;

/**
 * 财务管理类
 * 
 * @author hxting
 *
 */
@Component
@Transactional
public class FinanceService {
	private static final Logger logger = LoggerFactory.getLogger(FinanceService.class);

	@Autowired
	private CommonService commonService;

	@Autowired
	private SponsorDAO sponsorDAO;

	@Transactional(readOnly = true)
	public void getReconciliationList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT c.fname as fname,c.fphone as fphone, o.forderNum as forderNum,")
				.append(" o.fpostage as fpostage, o.ftotal as ftotal, o.fstatus as fstatus,")
				.append(" p.fcreateTime as fpayTime, p.fchannel as fpayType, p.fchannel as fchannel")
				.append(" FROM TOrder o LEFT JOIN TCustomer c ON o.TCustomer.id = c.id")
				.append(" inner join TPayInfo p on p.forderId = o.id ")
				.append(" WHERE o.fstatus < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("o_fpayType") && StringUtils.isNotBlank(valueMap.get("o_fpayType").toString())) {
				hql.append(" and p.fchannel = :o_fpayType ");
				hqlMap.put("o_fpayType", valueMap.get("o_fpayType").toString());
			}
			if (valueMap.containsKey("o_forderStatus") && StringUtils.isNotBlank(valueMap.get("o_forderStatus").toString())) {
				hql.append(" and p.fstatus = :o_forderStatus ");
				hqlMap.put("o_forderStatus", Integer.valueOf(valueMap.get("o_forderStatus").toString()));
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
			hql.append(" order by p.fcreateTime desc");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		Date date = null;
		for (Map<String, Object> amap : list) {
			if (amap.get("fchannel") != null && StringUtils.isNotBlank(amap.get("fchannel").toString())) {
				if(amap.get("fchannel").toString().equals("wx")){
					amap.put("fchannelString","微信支付");
				}else if(amap.get("fchannel").toString().equals("wx_pub")){
					amap.put("fchannelString","微信H5支付");
				}else if(amap.get("fchannel").toString().equals("alipay")){
					amap.put("fchannelString","支付宝支付");
				}
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatus", DictionaryUtil.getString(DictionaryUtil.OrderStatus,
						((Integer) amap.get("fstatus")), shiroUser.getLanguage()));
			}
			if (amap.get("fpayTime") != null && StringUtils.isNotBlank(amap.get("fpayTime").toString())) {
				date = (Date) amap.get("fpayTime");
				amap.put("fpayTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}

		}

	}

	@Transactional(readOnly = true)
	public void createReconciliationExcel(Map<String, Object> valueMap, String datePath, String excelFileName) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT c.fname as fname,c.fphone as fphone, o.forderNum as forderNum,")
		.append(" o.fpostage as fpostage, o.ftotal as ftotal, o.fstatus as fstatus,")
		.append(" p.fcreateTime as fpayTime, p.fchannel as fpayType, p.fchannel as fchannel")
		.append(" FROM TOrder o LEFT JOIN TCustomer c ON o.TCustomer.id = c.id")
		.append(" inner join TPayInfo p on p.forderId = o.id ")
		.append(" WHERE o.fstatus < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		
		try {
			if (valueMap.containsKey("o_fpayType") && StringUtils.isNotBlank(valueMap.get("o_fpayType").toString())) {
				hql.append(" and p.fchannel = :o_fpayType ");
				hqlMap.put("o_fpayType", valueMap.get("o_fpayType").toString());
			}
			if (valueMap.containsKey("o_forderStatus") && StringUtils.isNotBlank(valueMap.get("o_forderStatus").toString())) {
				hql.append(" and p.fstatus = :o_forderStatus ");
				hqlMap.put("o_forderStatus", Integer.valueOf(valueMap.get("o_forderStatus").toString()));
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
			hql.append(" order by p.fcreateTime desc");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}

		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);
		ExcelTemplate excel = ExcelTemplate.getInstance()
				.readTemplateClassPath("/template/excel/reconciliationTemp.xlsx");
		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("excelreconciliationPath")).append("/").append(datePath).append("/");
		Date date = null;
		for (Map<String, Object> amap : list) {
			excel.creatNewRow();
			if (amap.get("fname") != null && StringUtils.isNotBlank(amap.get("fname").toString())) {
				excel.createNewCol(amap.get("fname").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fphone") != null && StringUtils.isNotBlank(amap.get("fphone").toString())) {
				excel.createNewCol(amap.get("fphone").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("forderNum") != null && StringUtils.isNotBlank(amap.get("forderNum").toString())) {
				excel.createNewCol(amap.get("forderNum").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fpostage") != null && StringUtils.isNotBlank(amap.get("fpostage").toString())) {
				excel.createNewCol(amap.get("fpostage").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("ftotal") != null && StringUtils.isNotBlank(amap.get("ftotal").toString())) {
				excel.createNewCol(amap.get("ftotal").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				excel.createNewCol(DictionaryUtil.getString(DictionaryUtil.OrderStatus, ((Integer) amap.get("fstatus")),
						shiroUser.getLanguage()));
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fpayTime") != null && StringUtils.isNotBlank(amap.get("fpayTime").toString())) {
				date = (Date) amap.get("fpayTime");
				excel.createNewCol(DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss"));
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fchannel") != null && StringUtils.isNotBlank(amap.get("fchannel").toString())) {
				if(amap.get("fchannel").toString().equals("wx")){
					excel.createNewCol("微信支付");
				}else if(amap.get("fchannel").toString().equals("wx_pub")){
					excel.createNewCol("微信H5支付");
				}else if(amap.get("fchannel").toString().equals("alipay")){
					excel.createNewCol("支付宝支付");
				}
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