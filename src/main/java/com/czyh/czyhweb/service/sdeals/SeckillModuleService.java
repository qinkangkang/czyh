package com.czyh.czyhweb.service.sdeals;

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

import com.czyh.czyhweb.dao.EventDAO;
import com.czyh.czyhweb.dao.SeckillModuleDAO;
import com.czyh.czyhweb.entity.TEvent;
import com.czyh.czyhweb.entity.TSeckillModule;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.CommonService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.DictionaryUtil;

/**
 * 秒杀模块业务管理类
 * 
 * @author jinsey
 *
 */
@Component
@Transactional
public class SeckillModuleService {

	private static final Logger logger = LoggerFactory.getLogger(SeckillModuleService.class);

	@Autowired
	private CommonService commonService;

	@Autowired
	private SeckillModuleDAO seckillModuleDAO;

	@Autowired
	private EventDAO eventDAO;

	/**
	 * 获取秒杀商品列表
	 * 
	 * @param valueMap
	 * @param page
	 */
	@Transactional(readOnly = true)
	public void getSeckillList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.fgoodsId as fgoodsId, g.ftitle as fgoodsTitle, g.fsubTitle as fgoodsSubTitle, g.fprice as fgoodsPrice, g.fpriceMoney as fgoodsPriceMoney, g.flimitation as fgoodsLimitation, g.fimage1 as fimage, t.ftype as ftype, t.ftodaySeckillType as ftodaySeckillType, t.fgoodsCreateTime as fgoodsCreateTime, t.fgoodsUpdateTime as fgoodsUpdateTime,t.fgoodstatus as fgoodstatus from TSeckillModule t");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hql.append(" inner join TEvent g on t.fgoodsId = g.id");
		hql.append(" where t.fgoodstatus < 999");

		try {
			if (valueMap.containsKey("s_ftitle") && StringUtils.isNotBlank(valueMap.get("s_ftitle").toString())) {
				hql.append(" and t.fgoodsTitle like :s_ftitle ");
				hqlMap.put("s_ftitle", "%" + valueMap.get("s_ftitle").toString() + "%");
			}
			if (valueMap.containsKey("s_status") && StringUtils.isNotBlank(valueMap.get("s_status").toString())) {
				hql.append(" and t.fgoodstatus = :s_status ");
				hqlMap.put("s_status", Integer.valueOf(valueMap.get("s_status").toString()));
			}
			if (valueMap.containsKey("fcreateTimeStart")
					&& StringUtils.isNotBlank(valueMap.get("fcreateTimeStart").toString())) {
				hql.append(" and t.fgoodsCreateTime >= :fcreateTimeStart ");
				hqlMap.put("fcreateTimeStart",
						DateUtils.parseDate(valueMap.get("fcreateTimeStart").toString(), "yyyy-MM-dd"));
			}
			if (valueMap.containsKey("fcreateTimeEnd")
					&& StringUtils.isNotBlank(valueMap.get("fcreateTimeEnd").toString())) {
				hql.append(" and t.fgoodsCreateTime < :fcreateTimeEnd ");
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
					hql.append(" order by t.fgoodsCreateTime desc");
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

			if (amap.get("ftodaySeckillType") != null
					&& StringUtils.isNotBlank(amap.get("ftodaySeckillType").toString())) {
				amap.put("ftodaySeckillTypeString", DictionaryUtil.getString(DictionaryUtil.TodaySeckillType,
						((Integer) amap.get("ftodaySeckillType")), shiroUser.getLanguage()));
			}

			if (amap.get("fgoodsCreateTime") != null
					&& StringUtils.isNotBlank(amap.get("fgoodsCreateTime").toString())) {
				date = (Date) amap.get("fgoodsCreateTime");
				amap.put("fgoodsCreateTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
			}
			if (amap.get("fgoodsUpdateTime") != null
					&& StringUtils.isNotBlank(amap.get("fgoodsUpdateTime").toString())) {
				date = (Date) amap.get("fgoodsUpdateTime");
				amap.put("fgoodsUpdateTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
			}
			if (amap.get("fgoodstatus") != null && StringUtils.isNotBlank(amap.get("fgoodstatus").toString())) {
				amap.put("fstatusString", DictionaryUtil.getString(DictionaryUtil.EventStatus,
						((Integer) amap.get("fgoodstatus")), shiroUser.getLanguage()));
			}
		}
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> seckilModulelList() {
		return commonService.find(
				"select t.id as key, t.ftitle as value from TEvent t where t.fsdealsModel = 3 and t.fstatus < 999");
	}

	/**
	 * 添加秒杀商品
	 * 
	 * @param valueMap
	 */
	public void saveSeckillModule(Map<String, Object> valueMap) {

		TSeckillModule tSeckillModule = new TSeckillModule();

		// TEvent tEvent =
		// eventDAO.getSeckillModule(valueMap.get("goodsId").toString());

		TSeckillModule tmodule = seckillModuleDAO.getGoodsIdDetail(valueMap.get("goodsId").toString());
		if (tmodule != null) {
			throw new ServiceException("秒杀商品只能同时存在一个,请核对后录入！");
		}

		if (valueMap.containsKey("goodsId") && StringUtils.isNotBlank(valueMap.get("goodsId").toString())) {
			tSeckillModule.setFgoodsId(valueMap.get("goodsId").toString());
		}

		if (valueMap.containsKey("ftype") && StringUtils.isNotBlank(valueMap.get("ftype").toString())) {
			tSeckillModule.setFtype(Integer.valueOf(valueMap.get("ftype").toString()));
		}
		if (valueMap.containsKey("ftodaySeckillType")
				&& StringUtils.isNotBlank(valueMap.get("ftodaySeckillType").toString())) {
			tSeckillModule.setFtodaySeckillType(Integer.valueOf(valueMap.get("ftodaySeckillType").toString()));
		}

		tSeckillModule.setFgoodstatus(10);
		tSeckillModule.setFgoodsCreateTime(new Date());
		seckillModuleDAO.save(tSeckillModule);
	}

	public void editSeckillModule(Map<String, Object> valueMap) {

		TSeckillModule tSeckillModule = seckillModuleDAO.findOne(valueMap.get("id").toString());

		if (valueMap.containsKey("goodsId") && StringUtils.isNotBlank(valueMap.get("goodsId").toString())) {
			tSeckillModule.setFgoodsId(valueMap.get("goodsId").toString());
		} else {
			tSeckillModule.setFgoodsId(null);
		}
		if (valueMap.containsKey("ftodaySeckillType")
				&& StringUtils.isNotBlank(valueMap.get("ftodaySeckillType").toString())) {
			tSeckillModule.setFtodaySeckillType(Integer.valueOf(valueMap.get("ftodaySeckillType").toString()));
		} else {
			tSeckillModule.setFtodaySeckillType(null);
		}

		if (valueMap.containsKey("ftype") && StringUtils.isNotBlank(valueMap.get("ftype").toString())) {
			tSeckillModule.setFtype(Integer.valueOf(valueMap.get("ftype").toString()));
		} else {
			tSeckillModule.setFtype(null);
		}
		tSeckillModule.setFgoodsUpdateTime(new Date());

		seckillModuleDAO.save(tSeckillModule);
	}

	public void delSeckillModule(String goodsId) {
		seckillModuleDAO.saveSaleSeckillModule(999, goodsId, new Date());
	}

	@Transactional(readOnly = true)
	public TEvent getGoodsModule(String goodsId) {
		return eventDAO.findOne(goodsId);
	}

	@Transactional(readOnly = true)
	public TSeckillModule getSeckillModule(String goodsId) {
		return seckillModuleDAO.findOne(goodsId);
	}

	public void onSaleSeckillModule(String fID, Integer Status) {
		seckillModuleDAO.saveSaleSeckillModule(Status, fID, new Date());

	}

}