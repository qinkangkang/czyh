package com.czyh.czyhweb.service.system;

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

import com.czyh.czyhweb.dao.AppChannelEventDAO;
import com.czyh.czyhweb.dao.AppChannelSettingDAO;
import com.czyh.czyhweb.dao.AppChannelSliderDAO;
import com.czyh.czyhweb.dao.AppFlashDAO;
import com.czyh.czyhweb.dao.AppIndexItemDAO;
import com.czyh.czyhweb.dao.AppNoticeDAO;
import com.czyh.czyhweb.dao.ColumnBannerDAO;
import com.czyh.czyhweb.dao.ImageDAO;
import com.czyh.czyhweb.dao.SubjectDAO;
import com.czyh.czyhweb.dao.SubobjectTimeStampDAO;
import com.czyh.czyhweb.dto.system.ObjectTimeStampDTO;
import com.czyh.czyhweb.entity.TAppChannelSetting;
import com.czyh.czyhweb.entity.TAppChannelSlider;
import com.czyh.czyhweb.entity.TAppFlash;
import com.czyh.czyhweb.entity.TAppIndexItem;
import com.czyh.czyhweb.entity.TAppNotice;
import com.czyh.czyhweb.entity.TColumnBanner;
import com.czyh.czyhweb.entity.TSubject;
import com.czyh.czyhweb.entity.TSubobjectTimeStamp;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.CommonService;
import com.czyh.czyhweb.service.FxlService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.PropertiesUtil;
import com.czyh.czyhweb.util.html.StaticHtmlBean;
import com.czyh.czyhweb.util.html.StaticHtmlManager;
import com.google.common.collect.Maps;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * app系统业务管理类.
 * 
 * @author jinshengzhi
 */
@Component
@Transactional
public class AppService {

	private static final Logger logger = LoggerFactory.getLogger(AppService.class);

	// private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private CommonService commonService;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private FxlService fxlService;

	@Autowired
	private AppChannelSettingDAO appChannelSettingDAO;

	@Autowired
	private AppChannelSliderDAO appChannelSliderDAO;

	@Autowired
	private AppIndexItemDAO appIndexItemDAO;

	@Autowired
	private AppChannelEventDAO appChannelEventDAO;

	@Autowired
	private AppFlashDAO appFlashDAO;

	@Autowired
	private SubjectDAO subjectDAO;

	@Autowired
	private ImageDAO imageDAO;

	@Autowired
	private ColumnBannerDAO columnBannerDAO;

	@Autowired
	private SubobjectTimeStampDAO subobjectTimeStampDAO;
	
	@Autowired
	private AppNoticeDAO appNoticeDAO;

	@Transactional(readOnly = true)
	public void getChannelList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.fcode as fcode, t.ftitle as ftitle, t.fcity as fcity, t.ftype as ftype, t.fwebType as fwebType, t.fallEvent as fallEvent, t.forder as forder, t.fisVisible as fisVisible, t.fdefaultOrderType as fdefaultOrderType from TAppChannelSetting t where 1 = 1");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_fcode") && StringUtils.isNotBlank(valueMap.get("s_fcode").toString())) {
				hql.append(" and t.fcode like :s_fcode ");
				hqlMap.put("s_fcode", "%" + valueMap.get("s_fcode").toString() + "%");
			}
			if (valueMap.containsKey("s_ftitle") && StringUtils.isNotBlank(valueMap.get("s_ftitle").toString())) {
				hql.append(" and t.ftitle like :s_ftitle ");
				hqlMap.put("s_ftitle", "%" + valueMap.get("s_ftitle").toString() + "%");
			}
			if (valueMap.containsKey("s_fcity") && StringUtils.isNotBlank(valueMap.get("s_fcity").toString())) {
				hql.append(" and t.fcity = :s_fcity");
				hqlMap.put("s_fcity", Integer.valueOf(valueMap.get("s_fcity").toString()));
			}
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.fcity, t.forder");
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		for (Map<String, Object> amap : list) {
			if (amap.get("fcity") != null && StringUtils.isNotBlank(amap.get("fcity").toString())) {
				amap.put("fcity", DictionaryUtil.getString(DictionaryUtil.City, (Integer) amap.get("fcity"),
						shiroUser.getLanguage()));
			}
			if (amap.get("fisVisible") != null && StringUtils.isNotBlank(amap.get("fisVisible").toString())) {
				amap.put("fisVisible", DictionaryUtil.getString(DictionaryUtil.YesNo, (Integer) amap.get("fisVisible"),
						shiroUser.getLanguage()));
			}
			if (amap.get("fwebType") != null && StringUtils.isNotBlank(amap.get("fwebType").toString())) {
				amap.put("fwebType", DictionaryUtil.getString(DictionaryUtil.ChannelType,
						(Integer) amap.get("fwebType"), shiroUser.getLanguage()));
			}
			if (amap.get("ftype") != null && StringUtils.isNotBlank(amap.get("ftype").toString())) {
				amap.put("ftype", DictionaryUtil.getString(DictionaryUtil.ChannelType, (Integer) amap.get("ftype"),
						shiroUser.getLanguage()));
			}
			if (amap.get("fallEvent") != null && StringUtils.isNotBlank(amap.get("fallEvent").toString())) {
				amap.put("fallEvent", DictionaryUtil.getString(DictionaryUtil.YesNo, (Integer) amap.get("fallEvent"),
						shiroUser.getLanguage()));
			}
			if (amap.get("fdefaultOrderType") != null
					&& StringUtils.isNotBlank(amap.get("fdefaultOrderType").toString())) {
				amap.put("fdefaultOrderType", DictionaryUtil.getString(DictionaryUtil.ChannelDefaultOrderType,
						(Integer) amap.get("fdefaultOrderType"), shiroUser.getLanguage()));
			}
		}
	}

	@Transactional(readOnly = true)
	public TAppChannelSetting getChannel(String channelId) {
		return appChannelSettingDAO.getOne(channelId);
	}

	public String saveChannel(Map<String, Object> valueMap) {
		TAppChannelSetting tAppChannelSetting = new TAppChannelSetting();
		BeanMapper.copy(valueMap, tAppChannelSetting);

		tAppChannelSetting = appChannelSettingDAO.save(tAppChannelSetting);
		if (tAppChannelSetting.getFicon() != null) {
			imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(tAppChannelSetting.getFicon()), 2,
					tAppChannelSetting.getId(), 6);
		}
		if (tAppChannelSetting.getForder().intValue() < 99) {
			StringBuilder val = new StringBuilder();
			val.append(DictionaryUtil.getString(DictionaryUtil.City, tAppChannelSetting.getFcity())).append(" - ");
			val.append(tAppChannelSetting.getFtitle()).append(" - ");
			int i = tAppChannelSetting.getFisVisible().intValue();
			if (i == 1) {
				val.append("栏目显示");
			} else {
				val.append("栏目不显示");
			}
			val.append(" - ");
			val.append("栏目排序【").append(tAppChannelSetting.getForder()).append("】");
			Constant.appChannelMap.put(tAppChannelSetting.getId(), val.toString());
		}
		return tAppChannelSetting.getId();
	}

	public void eidtChannel(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		String id = valueMap.get("id").toString();
		TAppChannelSetting db = appChannelSettingDAO.getOne(id);

		if (valueMap.containsKey("fcode") && StringUtils.isNotBlank(valueMap.get("fcode").toString())) {
			db.setFcode(valueMap.get("fcode").toString());
		} else {
			db.setFcode(null);
		}
		if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
			db.setFtitle(valueMap.get("ftitle").toString());
		} else {
			db.setFtitle(null);
		}
		// if (valueMap.containsKey("fsubTitle") &&
		// StringUtils.isNotBlank(valueMap.get("fsubTitle").toString())) {
		// db.setFsubTitle(valueMap.get("fsubTitle").toString());
		// } else {
		// db.setFsubTitle(null);
		// }
		if (valueMap.containsKey("fcity") && StringUtils.isNotBlank(valueMap.get("fcity").toString())) {
			db.setFcity(Integer.valueOf(valueMap.get("fcity").toString()));
		} else {
			db.setFcity(null);
		}
		if (valueMap.containsKey("forder") && StringUtils.isNotBlank(valueMap.get("forder").toString())) {
			db.setForder(Integer.valueOf(valueMap.get("forder").toString()));
		} else {
			db.setForder(null);
		}
		if (valueMap.containsKey("ffrontType") && StringUtils.isNotBlank(valueMap.get("ffrontType").toString())) {
			db.setFfrontType(Integer.valueOf(valueMap.get("ffrontType").toString()));
		} else {
			db.setFfrontType(null);
		}
		if (valueMap.containsKey("fallEvent") && StringUtils.isNotBlank(valueMap.get("fallEvent").toString())) {
			db.setFallEvent(Integer.valueOf(valueMap.get("fallEvent").toString()));
		} else {
			db.setFallEvent(null);
		}
		if (valueMap.containsKey("fwebType") && StringUtils.isNotBlank(valueMap.get("fwebType").toString())) {
			db.setFwebType(Integer.valueOf(valueMap.get("fwebType").toString()));
		} else {
			db.setFwebType(null);
		}
		if (valueMap.containsKey("ftype") && StringUtils.isNotBlank(valueMap.get("ftype").toString())) {
			db.setFtype(Integer.valueOf(valueMap.get("ftype").toString()));
		} else {
			db.setFtype(null);
		}
		if (valueMap.containsKey("fisVisible") && StringUtils.isNotBlank(valueMap.get("fisVisible").toString())) {
			db.setFisVisible(Integer.valueOf(valueMap.get("fisVisible").toString()));
		} else {
			db.setFisVisible(null);
		}
		if (valueMap.containsKey("fdefaultOrderType")
				&& StringUtils.isNotBlank(valueMap.get("fdefaultOrderType").toString())) {
			db.setFdefaultOrderType(Integer.valueOf(valueMap.get("fdefaultOrderType").toString()));
		} else {
			db.setFdefaultOrderType(null);
		}
		// if (valueMap.containsKey("fpromotion") &&
		// StringUtils.isNotBlank(valueMap.get("fpromotion").toString())) {
		// db.setFpromotion(valueMap.get("fpromotion").toString());
		// } else {
		// db.setFpromotion(null);
		// }
		if (valueMap.containsKey("fimage") && StringUtils.isNotBlank(valueMap.get("fimage").toString())) {
			String icon = valueMap.get("fimage").toString();
			if (StringUtils.isBlank(db.getFicon()) || !db.getFicon().equals(icon)) {
				db.setFicon(icon);
				imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(icon), 2, db.getId(), 6);
			}
		} else {
			db.setFicon(null);
		}
		appChannelSettingDAO.save(db);
		if (db.getForder().intValue() < 99) {
			StringBuilder val = new StringBuilder();
			val.append(DictionaryUtil.getString(DictionaryUtil.City, db.getFcity())).append(" - ");
			val.append(db.getFtitle()).append(" - ");
			int i = db.getFisVisible().intValue();
			if (i == 1) {
				val.append("栏目显示");
			} else {
				val.append("栏目不显示");
			}
			val.append(" - ");
			val.append("栏目排序【").append(db.getForder()).append("】");
			Constant.appChannelMap.put(db.getId(), val.toString());
		}
	}

	public String delChannel(String id) {

		appChannelEventDAO.deleteByChannelId(id);
		appChannelSliderDAO.deleteByChannelId(id);
		TAppChannelSetting db = appChannelSettingDAO.getOne(id);
		if (db.getForder().intValue() < 99) {
			Constant.appChannelMap.remove(id);
		}
		appChannelSettingDAO.deleteByChannelId(id);
		return db.getFtitle();
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getAppFlashList() {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.fimage as fimage, t.furlType as furlType, t.fentityTitle as fentityTitle, t.fcity as fcity, t.fisVisible as fisVisible from TAppFlash t order by t.fcity");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		for (Map<String, Object> amap : list) {
			if (amap.get("fcity") != null && StringUtils.isNotBlank(amap.get("fcity").toString())) {
				amap.put("fcity", DictionaryUtil.getString(DictionaryUtil.City, (Integer) amap.get("fcity"),
						shiroUser.getLanguage()));
			}
			if (amap.get("furlType") != null && StringUtils.isNotBlank(amap.get("furlType").toString())) {
				amap.put("furlType", DictionaryUtil.getString(DictionaryUtil.ChannelSliderUrlType,
						(Integer) amap.get("furlType"), shiroUser.getLanguage()));
			}
			if (amap.get("fisVisible") != null && StringUtils.isNotBlank(amap.get("fisVisible").toString())) {
				amap.put("fisVisible", DictionaryUtil.getString(DictionaryUtil.YesNo, (Integer) amap.get("fisVisible"),
						shiroUser.getLanguage()));
			}
			if (amap.get("fimage") != null && StringUtils.isNotBlank(amap.get("fimage").toString())) {
				amap.put("imageUrl", fxlService.getImageUrl(amap.get("fimage").toString(), true));
			} else {
				amap.put("imageUrl",
						new StringBuilder(PropertiesUtil.getProperty("fileServerUrl"))
								.append(PropertiesUtil.getProperty("imageRootPath"))
								.append("/czyhweb/noPicThumbnail.jpg").toString());
			}
		}
		return list;
	}

	@Transactional(readOnly = true)
	public TAppFlash getAppFlash(String appFlashId) {
		return appFlashDAO.getOne(appFlashId);
	}

	public String editAppFlash(Map<String, Object> valueMap) {

		String id = valueMap.get("id").toString();
		TAppFlash db = appFlashDAO.getOne(id);

		if (valueMap.containsKey("fimage") && StringUtils.isNotBlank(valueMap.get("fimage").toString())) {
			if (StringUtils.isBlank(db.getFimage())
					|| !db.getFimage().toString().equals(valueMap.get("fimage").toString())) {
				db.setFimage(valueMap.get("fimage").toString());
				imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(valueMap.get("fimage").toString()), 2,
						db.getId(), 1);
			}
		}
		if (valueMap.containsKey("furlType") && StringUtils.isNotBlank(valueMap.get("furlType").toString())) {
			db.setFurlType(Integer.valueOf(valueMap.get("furlType").toString()));
		} else {
			db.setFurlType(null);
		}
		if (valueMap.containsKey("fentityId") && StringUtils.isNotBlank(valueMap.get("fentityId").toString())) {
			db.setFentityId(valueMap.get("fentityId").toString());
		} else {
			db.setFentityId(null);
		}
		if (valueMap.containsKey("fentityTitle") && StringUtils.isNotBlank(valueMap.get("fentityTitle").toString())) {
			db.setFentityTitle(valueMap.get("fentityTitle").toString());
		} else {
			db.setFentityTitle(null);
		}
		if (valueMap.containsKey("fexternalUrl") && StringUtils.isNotBlank(valueMap.get("fexternalUrl").toString())) {
			db.setFexternalUrl(valueMap.get("fexternalUrl").toString());
		} else {
			db.setFexternalUrl(null);
		}
		if (valueMap.containsKey("fisVisible") && StringUtils.isNotBlank(valueMap.get("fisVisible").toString())) {
			db.setFisVisible(Integer.valueOf(valueMap.get("fisVisible").toString()));
		} else {
			db.setFisVisible(null);
		}
		appFlashDAO.save(db);
		return id;
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getChannelSliderList(String channelId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.fimage as fimage, t.furlType as furlType, t.fentityTitle as fentityTitle, t.forder as forder, t.fisVisible as fisVisible from TAppChannelSlider t where t.TAppChannelSetting.id = :channelId order by t.forder");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("channelId", channelId);

		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		for (Map<String, Object> amap : list) {
			if (amap.get("furlType") != null && StringUtils.isNotBlank(amap.get("furlType").toString())) {
				amap.put("furlType", DictionaryUtil.getString(DictionaryUtil.ChannelSliderUrlType,
						(Integer) amap.get("furlType"), shiroUser.getLanguage()));
			}
			if (amap.get("fisVisible") != null && StringUtils.isNotBlank(amap.get("fisVisible").toString())) {
				amap.put("fisVisible", DictionaryUtil.getString(DictionaryUtil.YesNo, (Integer) amap.get("fisVisible"),
						shiroUser.getLanguage()));
			}
			if (amap.get("fimage") != null && StringUtils.isNotBlank(amap.get("fimage").toString())) {
				amap.put("imageUrl", fxlService.getImageUrl(amap.get("fimage").toString(), true));
			} else {
				amap.put("imageUrl",
						new StringBuilder(PropertiesUtil.getProperty("fileServerUrl"))
								.append(PropertiesUtil.getProperty("imageRootPath"))
								.append("/czyhweb/noPicThumbnail.jpg").toString());
			}
		}
		return list;
	}

	@Transactional(readOnly = true)
	public TAppChannelSlider getChannelSlider(String sliderId) {
		return appChannelSliderDAO.getOne(sliderId);
	}

	public String saveChannelSlider(Map<String, Object> valueMap) {
		TAppChannelSlider tAppChannelSlider = new TAppChannelSlider();

		BeanMapper.copy(valueMap, tAppChannelSlider);

		String channelId = valueMap.get("channelId").toString();

		tAppChannelSlider.setTAppChannelSetting(new TAppChannelSetting(channelId));
		tAppChannelSlider.setFsliderType(1);

		tAppChannelSlider = appChannelSliderDAO.save(tAppChannelSlider);

		if (StringUtils.isNotBlank(tAppChannelSlider.getFimage())) {
			imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(tAppChannelSlider.getFimage()), 2,
					tAppChannelSlider.getId(), 5);
		}
		return channelId;
	}

	public void editChannelSlider(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		TAppChannelSlider db = appChannelSliderDAO.getOne(valueMap.get("id").toString());

		if (valueMap.containsKey("fimage") && StringUtils.isNotBlank(valueMap.get("fimage").toString())) {
			if (StringUtils.isBlank(db.getFimage())
					|| !db.getFimage().toString().equals(valueMap.get("fimage").toString())) {
				db.setFimage(valueMap.get("fimage").toString());
				imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(valueMap.get("fimage").toString()), 2,
						db.getId(), 5);
			}
		}
		if (valueMap.containsKey("furlType") && StringUtils.isNotBlank(valueMap.get("furlType").toString())) {
			db.setFurlType(Integer.valueOf(valueMap.get("furlType").toString()));
		} else {
			db.setFurlType(null);
		}
		if (valueMap.containsKey("fentityId") && StringUtils.isNotBlank(valueMap.get("fentityId").toString())) {
			db.setFentityId(valueMap.get("fentityId").toString());
		} else {
			db.setFentityId(null);
		}
		if (valueMap.containsKey("fentityTitle") && StringUtils.isNotBlank(valueMap.get("fentityTitle").toString())) {
			db.setFentityTitle(valueMap.get("fentityTitle").toString());
		} else {
			db.setFentityTitle(null);
		}
		if (valueMap.containsKey("fexternalUrl") && StringUtils.isNotBlank(valueMap.get("fexternalUrl").toString())) {
			db.setFexternalUrl(valueMap.get("fexternalUrl").toString());
		} else {
			db.setFexternalUrl(null);
		}
		if (valueMap.containsKey("forder") && StringUtils.isNotBlank(valueMap.get("forder").toString())) {
			db.setForder(Integer.valueOf(valueMap.get("forder").toString()));
		} else {
			db.setForder(null);
		}
		if (valueMap.containsKey("fisVisible") && StringUtils.isNotBlank(valueMap.get("fisVisible").toString())) {
			db.setFisVisible(Integer.valueOf(valueMap.get("fisVisible").toString()));
		} else {
			db.setFisVisible(null);
		}
		appChannelSliderDAO.save(db);
	}

	public void delChannelSlider(String sliderId) {
		appChannelSliderDAO.delete(sliderId);
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getSliderTargetList(int cityId, int urlType, String searchKey) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		StringBuilder info = new StringBuilder();
		List<Map<String, Object>> list = null;
		// urlType为1代表检索活动
		if (urlType == 1) {
			hql.append(
					"select t.id as DT_RowId, t.ftitle as ftitle, t.ftypeA as ftypeA, t.TSponsor.fname as fname from TEvent t where t.fcity = :cityId and t.fstatus < 999");
			hqlMap.put("cityId", cityId);
			if (StringUtils.isNotBlank(searchKey)) {
				hql.append(" and t.ftitle like :searchKey");
				hqlMap.put("searchKey", "%" + searchKey + "%");
			}
			hql.append(" order by t.fcreateTime desc");
			list = commonService.find(hql.toString(), hqlMap);
			// 获取到活动类目缓存对象
			Cache eventCategoryCache = cacheManager.getCache(Constant.EventCategory);
			Element ele = null;

			for (Map<String, Object> amap : list) {
				info.delete(0, info.length());
				info.append("<dl class='dl-horizontal'>");
				// <dl class="dl-horizontal">
				// <dt>...</dt>
				// <dd>...</dd>
				// </dl>
				if (amap.get("ftitle") != null && StringUtils.isNotBlank(amap.get("ftitle").toString())) {
					amap.put("title", amap.get("ftitle"));
					info.append("<dt>活动标题：</dt><dd>").append(amap.get("ftitle").toString()).append("</dd>");
				}
				if (amap.get("ftypeA") != null && StringUtils.isNotBlank(amap.get("ftypeA").toString())) {
					ele = eventCategoryCache.get((Integer) amap.get("ftypeA"));
					info.append("<dt>活动类型：</dt><dd>")
							.append(ele != null ? ele.getObjectValue().toString() : StringUtils.EMPTY).append("</dd>");
				}
				if (amap.get("fname") != null && StringUtils.isNotBlank(amap.get("fname").toString())) {
					info.append("<dt>商家名称：</dt><dd>").append(amap.get("fname").toString()).append("</dd>");
				}
				info.append("</dl>");
				amap.put("info", info.toString());
			}
		} else if (urlType == 2) {
			// urlType为2代表检索商家
			hql.append(
					"select t.id as DT_RowId, t.fname as fname, t.ffullName as ffullName, t.fphone as fphone, t.faddress as faddress from TSponsor t where t.fstatus < 999");
			if (StringUtils.isNotBlank(searchKey)) {
				hql.append(
						" and (t.fname like :searchKey or t.ffullName like :searchKey or t.fphone like :searchKey or t.faddress like :searchKey)");
				hqlMap.put("searchKey", "%" + searchKey + "%");
			}
			hql.append(" order by t.fcreateTime desc");
			list = commonService.find(hql.toString(), hqlMap);

			for (Map<String, Object> amap : list) {
				info.delete(0, info.length());
				info.append("<dl class='dl-horizontal'>");
				// <dl class="dl-horizontal">
				// <dt>...</dt>
				// <dd>...</dd>
				// </dl>
				if (amap.get("fname") != null && StringUtils.isNotBlank(amap.get("fname").toString())) {
					amap.put("title", amap.get("fname"));
					info.append("<dt>商家简称：</dt><dd>").append(amap.get("fname").toString()).append("</dd>");
				}
				if (amap.get("ffullName") != null && StringUtils.isNotBlank(amap.get("ffullName").toString())) {
					info.append("<dt>商家全称：</dt><dd>").append(amap.get("ffullName").toString()).append("</dd>");
				}
				if (amap.get("fphone") != null && StringUtils.isNotBlank(amap.get("fphone").toString())) {
					info.append("<dt>联系电话：</dt><dd>").append(amap.get("fphone").toString()).append("</dd>");
				}
				if (amap.get("faddress") != null && StringUtils.isNotBlank(amap.get("faddress").toString())) {
					info.append("<dt>地址：</dt><dd>").append(amap.get("faddress").toString()).append("</dd>");
				}
				info.append("</dl>");
				amap.put("info", info.toString());
			}
		} else if (urlType == 3) {
			// urlType为3代表检索栏目
			hql.append(
					"select t.id as DT_RowId, t.ftitle as ftitle, t.ffrontType as ffrontType, t.fisVisible as fisVisible, t.fwebType as fwebType, t.ftype as ftype from TAppChannelSetting t where 1 = 1");
			if (StringUtils.isNotBlank(searchKey)) {
				hql.append(" and t.ftitle like :searchKey");
				hqlMap.put("searchKey", "%" + searchKey + "%");
			}
			hql.append(" order by t.forder asc");
			list = commonService.find(hql.toString(), hqlMap);

			for (Map<String, Object> amap : list) {
				info.delete(0, info.length());
				info.append("<dl class='dl-horizontal'>");
				// <dl class="dl-horizontal">
				// <dt>...</dt>
				// <dd>...</dd>
				// </dl>
				if (amap.get("ftitle") != null && StringUtils.isNotBlank(amap.get("ftitle").toString())) {
					amap.put("title", amap.get("ftitle"));
					info.append("<dt>栏目名称：</dt><dd>").append(amap.get("ftitle").toString()).append("</dd>");
				}
				if (amap.get("ffrontType") != null && StringUtils.isNotBlank(amap.get("ffrontType").toString())) {
					info.append("<dt>适用前端类型：</dt><dd>").append(
							DictionaryUtil.getString(DictionaryUtil.ChannelFrontType, (Integer) amap.get("ffrontType")))
							.append("</dd>");
				}
				if (amap.get("fwebType") != null && StringUtils.isNotBlank(amap.get("fwebType").toString())) {
					info.append("<dt>WEB端栏目类型：</dt><dd>").append(
							DictionaryUtil.getString(DictionaryUtil.ChannelType, (Integer) amap.get("fwebType")))
							.append("</dd>");
				}
				if (amap.get("ftype") != null && StringUtils.isNotBlank(amap.get("ftype").toString())) {
					info.append("<dt>APP端栏目类型：</dt><dd>")
							.append(DictionaryUtil.getString(DictionaryUtil.ChannelType, (Integer) amap.get("ftype")))
							.append("</dd>");
				}
				if (amap.get("fisVisible") != null && StringUtils.isNotBlank(amap.get("fisVisible").toString())) {
					info.append("<dt>是否显示：</dt><dd>")
							.append(DictionaryUtil.getString(DictionaryUtil.YesNo, (Integer) amap.get("fisVisible")))
							.append("</dd>");
				}
				info.append("</dl>");
				amap.put("info", info.toString());
			}
		} else if (urlType == 12) {
			// urlType为12代表检索文章
			hql.append("select t.id as DT_RowId, t.ftitle as ftitle from TArticle t where 1 = 1");
			if (StringUtils.isNotBlank(searchKey)) {
				hql.append(" and t.ftitle like :searchKey");
				hqlMap.put("searchKey", "%" + searchKey + "%");
			}
			hql.append(" order by t.forder asc");
			list = commonService.find(hql.toString(), hqlMap);

			for (Map<String, Object> amap : list) {
				info.delete(0, info.length());
				info.append("<dl class='dl-horizontal'>");
				if (amap.get("ftitle") != null && StringUtils.isNotBlank(amap.get("ftitle").toString())) {
					amap.put("title", amap.get("ftitle"));
					info.append("<dt>文章标题：</dt><dd>").append(amap.get("ftitle").toString()).append("</dd>");
				}
				info.append("</dl>");
				amap.put("info", info.toString());
			}
		}
		return list;
	}

	@Transactional(readOnly = true)
	public void getSubjectList(Map<String, Object> valueMap, CommonPage page) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.ftitle as ftitle, t.fcreateTime as fcreateTime, t.fupdateTime as fupdateTime from TSubject t where t.fstatus < 999 order by t.fcreateTime desc");

		commonService.findPage(hql.toString(), page);
		List<Map<String, Object>> list = page.getResult();

		Date date = null;
		for (Map<String, Object> amap : list) {
			if (amap.get("fcreateTime") != null && StringUtils.isNotBlank(amap.get("fcreateTime").toString())) {
				date = (Date) amap.get("fcreateTime");
				amap.put("fcreateTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
			if (amap.get("fupdateTime") != null && StringUtils.isNotBlank(amap.get("fupdateTime").toString())) {
				date = (Date) amap.get("fupdateTime");
				amap.put("fupdateTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
		}
	}

	@Transactional(readOnly = true)
	public TSubject getSubject(String subjectId) {
		return subjectDAO.getOne(subjectId);
	}

	public String saveSubject(Map<String, Object> valueMap) {
		TSubject tSubject = new TSubject();
		BeanMapper.copy(valueMap, tSubject);
		Date now = new Date();
		tSubject.setFcreateTime(now);
		tSubject.setFupdateTime(now);
		tSubject.setFstatus(10);
		tSubject = subjectDAO.save(tSubject);

		if (valueMap.containsKey("fdetail") && StringUtils.isNotBlank(valueMap.get("fdetail").toString())) {
			String relativePath = new StringBuilder("/subject/").append(DateFormatUtils.format(now, "yyyy-MM-dd"))
					.append("/").append(tSubject.getId()).append(".html").toString();
			tSubject.setFdetailHtmlUrl(relativePath);
			// 创建异步页面静态化Bean
			StaticHtmlBean staticHtmlBean = new StaticHtmlBean();
			staticHtmlBean.setObjectId(tSubject.getId());
			staticHtmlBean.setTemplateName(Constant.SubjectDetilTemplate);
			staticHtmlBean.setRelativePath(relativePath);

			Map<String, Object> map = Maps.newHashMap();
			map.put("detailHtml", valueMap.get("fdetail").toString());
			staticHtmlBean.setContentMap(map);
			// 异步生成活动静态页面，不占用本线程的执行时间
			StaticHtmlManager.put(staticHtmlBean);
		}
		return tSubject.getId();
	}

	public void eidtSubject(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		TSubject db = subjectDAO.getOne(valueMap.get("id").toString());
		Date now = new Date();
		if (valueMap.containsKey("ftitle") && StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
			db.setFtitle(valueMap.get("ftitle").toString());
		} else {
			db.setFtitle(null);
		}
		if (valueMap.containsKey("fdetail") && StringUtils.isNotBlank(valueMap.get("fdetail").toString())) {
			db.setFdetail(valueMap.get("fdetail").toString());

			String relativePath = new StringBuilder("/subject/").append(DateFormatUtils.format(now, "yyyy-MM-dd"))
					.append("/").append(db.getId()).append(".html").toString();
			db.setFdetailHtmlUrl(relativePath);
			// 创建异步页面静态化Bean
			StaticHtmlBean staticHtmlBean = new StaticHtmlBean();
			staticHtmlBean.setObjectId(db.getId());
			staticHtmlBean.setTemplateName(Constant.SubjectDetilTemplate);
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
		subjectDAO.save(db);
	}

	public void initAppChannelMap() {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as id, t.ftitle as ftitle, t.fcity as fcity, t.fisVisible as fisVisible, t.forder as forder from TAppChannelSetting t where t.forder < 99 order by t.fcity asc, t.forder asc");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);
		Map<String, String> appChannelMap = Maps.newLinkedHashMap();

		StringBuilder val = new StringBuilder();
		int i = 0;
		for (Map<String, Object> amap : list) {
			val.delete(0, val.length());
			if (amap.get("fcity") != null && StringUtils.isNotBlank(amap.get("fcity").toString())) {
				val.append(DictionaryUtil.getString(DictionaryUtil.City, (Integer) amap.get("fcity"))).append(" - ");
			}
			if (amap.get("ftitle") != null && StringUtils.isNotBlank(amap.get("ftitle").toString())) {
				val.append(amap.get("ftitle").toString()).append(" - ");
			}
			if (amap.get("fisVisible") != null && StringUtils.isNotBlank(amap.get("fisVisible").toString())) {
				i = ((Integer) amap.get("fisVisible")).intValue();
				if (i == 1) {
					val.append("栏目显示");
				} else {
					val.append("栏目不显示");
				}
				val.append(" - ");
			}
			if (amap.get("forder") != null && StringUtils.isNotBlank(amap.get("forder").toString())) {
				val.append("栏目排序【").append(amap.get("forder").toString()).append("】");
			}
			appChannelMap.put(amap.get("id").toString(), val.toString());
		}
		Constant.appChannelMap = appChannelMap;
	}

	/**
	 * 获取字典中热搜词
	 * 
	 * @param valueMap
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<Map<String, Object>> getTopSearchQuerieList(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append("select t.id as DT_RowId, t.name as name, t.code as code from TDictionaryClass t where t.id = 60");

		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_name") && StringUtils.isNotBlank(valueMap.get("s_name").toString())) {
				hql.append(" and t.name like :name ");
				hqlMap.put("name", "%" + valueMap.get("s_name").toString() + "%");
			}
			hql.append(" order by t.id");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询QL语句时出错！");
		}
		return commonService.find(hql.toString(), hqlMap);
	}

	/**
	 * 获取字典中的活动TAG标签
	 * 
	 * @param valueMap
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<Map<String, Object>> getTagList(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append("select t.id as DT_RowId, t.name as name, t.code as code from TDictionaryClass t where t.id = 3");

		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_name") && StringUtils.isNotBlank(valueMap.get("s_name").toString())) {
				hql.append(" and t.name like :name ");
				hqlMap.put("name", "%" + valueMap.get("s_name").toString() + "%");
			}
			hql.append(" order by t.id");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询QL语句时出错！");
		}
		return commonService.find(hql.toString(), hqlMap);
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getEventListByChannelId(Map<String, Object> valueMap) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, te.id as feventId, te.ftitle as ftitle, s.fname as fsponsor, te.foffSaleTime as foffSaleTime, t.forder as forder from TAppChannelEvent t inner join t.TEvent te left join te.TSponsor s where t.TAppChannelSetting.id = :channelId and te.fstatus = 20 order by t.forder desc");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("channelId", valueMap.get("channelId").toString());
		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		Date date = null;
		int i = 1;
		for (Map<String, Object> amap : list) {
			amap.put("xh", i);
			i++;
			if (amap.get("foffSaleTime") != null && StringUtils.isNotBlank(amap.get("foffSaleTime").toString())) {
				date = (Date) amap.get("foffSaleTime");
				amap.put("foffSaleTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
		}
		return list;
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getIndexItemList() {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.fimage as fimage, t.furlType as furlType, t.fentityTitle as fentityTitle, t.flocationNum as flocationNum, t.fisVisible as fisVisible ,flagType as flagType from TAppIndexItem t where 1 = 1");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		for (Map<String, Object> amap : list) {
			if (amap.get("ffrontType") != null && StringUtils.isNotBlank(amap.get("ffrontType").toString())) {
				amap.put("ffrontType", DictionaryUtil.getString(DictionaryUtil.ChannelFrontType,
						(Integer) amap.get("ffrontType"), shiroUser.getLanguage()));
			}
			if (amap.get("furlType") != null && StringUtils.isNotBlank(amap.get("furlType").toString())) {
				amap.put("furlType", DictionaryUtil.getString(DictionaryUtil.ChannelSliderUrlType,
						(Integer) amap.get("furlType"), shiroUser.getLanguage()));
			}
			if (amap.get("fisVisible") != null && StringUtils.isNotBlank(amap.get("fisVisible").toString())) {
				amap.put("fisVisible", DictionaryUtil.getString(DictionaryUtil.YesNo, (Integer) amap.get("fisVisible"),
						shiroUser.getLanguage()));
			}
			if (amap.get("flagType") != null && StringUtils.isNotBlank(amap.get("flagType").toString())) {
				amap.put("flagType", DictionaryUtil.getString(DictionaryUtil.YesNo, (Integer) amap.get("flagType"),
						shiroUser.getLanguage()));
			}
			if (amap.get("fimage") != null && StringUtils.isNotBlank(amap.get("fimage").toString())) {
				amap.put("imageUrl", fxlService.getImageUrl(amap.get("fimage").toString(), true));
			} else {
				amap.put("imageUrl",
						new StringBuilder(PropertiesUtil.getProperty("fileServerUrl"))
								.append(PropertiesUtil.getProperty("imageRootPath"))
								.append("/czyhweb/noPicThumbnail.jpg").toString());
			}
		}
		return list;
	}

	@Transactional(readOnly = true)
	public TAppIndexItem getIndexItem(String itemId) {
		return appIndexItemDAO.getOne(itemId);
	}

	public String saveIndexItem(Map<String, Object> valueMap) {
		TAppIndexItem tAppIndexItem = new TAppIndexItem();
		BeanMapper.copy(valueMap, tAppIndexItem);
		String flagType = valueMap.get("flagType").toString();

		tAppIndexItem.setFcity(1);
		tAppIndexItem.setFlagType(Integer.valueOf(flagType));
		tAppIndexItem = appIndexItemDAO.save(tAppIndexItem);

		List<TAppIndexItem> tAppIndexItemList = appIndexItemDAO.findBySliderList();

		if (tAppIndexItemList.size() > 1) {
			throw new ServiceException("首页弹出广告只能同时设置一个请您认真检查！");
		}

		if (StringUtils.isNotBlank(tAppIndexItem.getFimage())) {
			imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(tAppIndexItem.getFimage()), 2,
					tAppIndexItem.getId(), 9);
		}
		return tAppIndexItem.getId();
	}

	public void editIndexItem(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		TAppIndexItem db = appIndexItemDAO.getOne(valueMap.get("id").toString());

		if (valueMap.containsKey("fimage") && StringUtils.isNotBlank(valueMap.get("fimage").toString())) {
			if (StringUtils.isBlank(db.getFimage())
					|| !db.getFimage().toString().equals(valueMap.get("fimage").toString())) {
				db.setFimage(valueMap.get("fimage").toString());
				imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(valueMap.get("fimage").toString()), 2,
						db.getId(), 9);
			}
		}
		if (valueMap.containsKey("ffrontType") && StringUtils.isNotBlank(valueMap.get("ffrontType").toString())) {
			db.setFfrontType(Integer.valueOf(valueMap.get("ffrontType").toString()));
		} else {
			db.setFfrontType(null);
		}
		if (valueMap.containsKey("furlType") && StringUtils.isNotBlank(valueMap.get("furlType").toString())) {
			db.setFurlType(Integer.valueOf(valueMap.get("furlType").toString()));
		} else {
			db.setFurlType(null);
		}
		if (valueMap.containsKey("fentityId") && StringUtils.isNotBlank(valueMap.get("fentityId").toString())) {
			db.setFentityId(valueMap.get("fentityId").toString());
		} else {
			db.setFentityId(null);
		}
		if (valueMap.containsKey("fentityTitle") && StringUtils.isNotBlank(valueMap.get("fentityTitle").toString())) {
			db.setFentityTitle(valueMap.get("fentityTitle").toString());
		} else {
			db.setFentityTitle(null);
		}
		if (valueMap.containsKey("fexternalUrl") && StringUtils.isNotBlank(valueMap.get("fexternalUrl").toString())) {
			db.setFexternalUrl(valueMap.get("fexternalUrl").toString());
		} else {
			db.setFexternalUrl(null);
		}
		if (valueMap.containsKey("flocationNum") && StringUtils.isNotBlank(valueMap.get("flocationNum").toString())) {
			db.setFlocationNum(Integer.valueOf(valueMap.get("flocationNum").toString()));
		} else {
			db.setFlocationNum(null);
		}
		if (valueMap.containsKey("forder") && StringUtils.isNotBlank(valueMap.get("forder").toString())) {
			db.setForder(Integer.valueOf(valueMap.get("forder").toString()));
		} else {
			db.setForder(null);
		}
		if (valueMap.containsKey("fisVisible") && StringUtils.isNotBlank(valueMap.get("fisVisible").toString())) {
			db.setFisVisible(Integer.valueOf(valueMap.get("fisVisible").toString()));
		} else {
			db.setFisVisible(null);
		}
		if (valueMap.containsKey("flagType") && StringUtils.isNotBlank(valueMap.get("flagType").toString())) {
			db.setFlagType(Integer.valueOf(valueMap.get("flagType").toString()));
		} else {
			db.setFisVisible(null);
		}

		List<TAppIndexItem> tAppIndexItemList = appIndexItemDAO.findBySliderList();
		if (tAppIndexItemList.size() > 1) {
			throw new ServiceException("首页弹出广告只能同时设置一个请您认真检查！");
		}
		appIndexItemDAO.save(db);
	}

	public void delIndexItem(String itemId) {
		appIndexItemDAO.delete(itemId);
	}

	@Transactional(readOnly = true)
	public void getColumnBannerList(Map<String, Object> valueMap, CommonPage page) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.ftag as ftag ,t.fseckillTime as fseckillTime ,t.ftype as ftype from TColumnBanner t where 1 = 1");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_fcode") && StringUtils.isNotBlank(valueMap.get("s_fcode").toString())) {
				hql.append(" and t.fcode like :s_fcode ");
				hqlMap.put("s_fcode", "%" + valueMap.get("s_fcode").toString() + "%");
			}
			if (valueMap.containsKey("s_ftitle") && StringUtils.isNotBlank(valueMap.get("s_ftitle").toString())) {
				hql.append(" and t.ftitle like :s_ftitle ");
				hqlMap.put("s_ftitle", "%" + valueMap.get("s_ftitle").toString() + "%");
			}
			if (valueMap.containsKey("s_fcity") && StringUtils.isNotBlank(valueMap.get("s_fcity").toString())) {
				hql.append(" and t.fcity = :s_fcity");
				hqlMap.put("s_fcity", Integer.valueOf(valueMap.get("s_fcity").toString()));
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
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		for (Map<String, Object> amap : list) {
			if (amap.get("ftype") != null && StringUtils.isNotBlank(amap.get("ftype").toString())) {
				if (Integer.valueOf(amap.get("ftype").toString()) == 1) {
					amap.put("typeString", "头图");
				} else if (Integer.valueOf(amap.get("ftype").toString()) == 2) {
					amap.put("typeString", "秒杀栏目");
				}
				amap.put("ftype", amap.get("ftype").toString());
			}
			if (amap.get("fseckillTime") != null && StringUtils.isNotBlank(amap.get("fseckillTime").toString())) {
				amap.put("fseckillTime", amap.get("fseckillTime").toString());
			}
			if (amap.get("ftag") != null && StringUtils.isNotBlank(amap.get("ftag").toString())) {
				if (Integer.valueOf(amap.get("ftype").toString()) == 1) {
					amap.put("ftagString",
							DictionaryUtil.getString(DictionaryUtil.ColumnBanner, ((Integer) amap.get("ftag"))));
				} else if (Integer.valueOf(amap.get("ftype").toString()) == 2) {
					amap.put("ftagString",
							DictionaryUtil.getString(DictionaryUtil.TodaySeckillType, ((Integer) amap.get("ftag"))));
				}
				amap.put("ftag", amap.get("ftag").toString());
			}
		}
	}

	/**
	 * 系统静态资源管理
	 * 
	 * @param valueMap
	 * @param page
	 */
	@Transactional(readOnly = true)
	public void getSystemStaticList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.ftitle as ftitle, t.ftype as ftype, t.forder as forder, t.fdetailHtmlUrl as fdetailHtmlUrl, t.fstatus as fstatus from TArticle t");
		hql.append(" where t.ftype = 2 and t.fstatus < 999 ");
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

				amap.put("htmlDetail",
						new StringBuilder(PropertiesUtil.getProperty("fileServerUrl"))
								.append(PropertiesUtil.getProperty("htmlRootPath"))
								.append(amap.get("fdetailHtmlUrl").toString()).toString());
			}

		}
	}

	public String addColumnBanner(Map<String, Object> valueMap) {
		TColumnBanner tColumnBanner = new TColumnBanner();
		BeanMapper.copy(valueMap, tColumnBanner);
		if (Integer.parseInt(valueMap.get("ftype").toString()) == 2) {
			tColumnBanner.setFtag(Integer.parseInt(valueMap.get("ftype").toString()));
		}
		TColumnBanner banner = columnBannerDAO.findByType(tColumnBanner.getFtag(), tColumnBanner.getFtype());
		if (banner != null) {
			return "false";
		}
		tColumnBanner.setFimageUrl(valueMap.get("fimage").toString());
		imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(valueMap.get("fimage").toString()), 2, banner.getId(),
				6);
		tColumnBanner = columnBannerDAO.save(tColumnBanner);

		return tColumnBanner.getId();
	}

	public void updateObjectTimeStamp(ObjectTimeStampDTO objectTimeStampDTO) {

		try {
			TSubobjectTimeStamp tSubobjectTimeStamp = subobjectTimeStampDAO
					.findByObject(objectTimeStampDTO.getFsubObject());
			if (tSubobjectTimeStamp == null) {
				tSubobjectTimeStamp.setFsubObject(objectTimeStampDTO.getFsubObject());
				tSubobjectTimeStamp.setFsubUpdateTime(objectTimeStampDTO.getFsubUpdateTime());
				tSubobjectTimeStamp.setFsubCreateTime(objectTimeStampDTO.getFsubUpdateTime());
			} else {
				tSubobjectTimeStamp.setFsubUpdateTime(objectTimeStampDTO.getFsubUpdateTime());
			}

			subobjectTimeStampDAO.save(tSubobjectTimeStamp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void delBanner(String id) {

		TColumnBanner db = columnBannerDAO.findOne(id);
		columnBannerDAO.deleteById(id);
	}

	public void editBanner(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		String id = valueMap.get("id").toString();
		TColumnBanner db = columnBannerDAO.findOne(id);

		if (valueMap.containsKey("ftype") && StringUtils.isNotBlank(valueMap.get("ftype").toString())) {
			db.setFtype(Integer.parseInt(valueMap.get("ftype").toString()));
		}
		if (Integer.parseInt(valueMap.get("ftype").toString()) == 1) {
			db.setFtag(Integer.parseInt(valueMap.get("ftag").toString()));
		} else {
			db.setFtag(Integer.parseInt(valueMap.get("ftag1").toString()));
		}
		if (valueMap.containsKey("fchannelId") && StringUtils.isNotBlank(valueMap.get("fchannelId").toString())) {
			db.setFchannelId(valueMap.get("fchannelId").toString());
		} else {
			db.setFchannelId(null);
		}
		if (valueMap.containsKey("fstatus") && StringUtils.isNotBlank(valueMap.get("fstatus").toString())) {
			db.setFstatus(Integer.valueOf(valueMap.get("fstatus").toString()));
		}
		if (valueMap.containsKey("fimage") && StringUtils.isNotBlank(valueMap.get("fimage").toString())) {
			String icon = valueMap.get("fimage").toString();
			if (StringUtils.isBlank(db.getFimageUrl()) || !db.getFimageUrl().equals(icon)) {
				db.setFimageUrl(icon);
				imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(icon), 2, db.getId(), 6);
			}
		} else {
			db.setFimageUrl(null);
		}
		columnBannerDAO.save(db);
	}

	/**
	 * APP首頁公告列表
	 * 
	 * @param valueMap
	 * @param page
	 */
	@Transactional(readOnly = true)
	public List<Map<String, Object>> getAppNoticeList() {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.fnoticeName as fnoticeName, t.fnoticeType as fnoticeType, t.fnoticeTime as fnoticeTime, t.forder as forder from TAppNotice t ");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		Date date = null;
		for (Map<String, Object> amap : list) {
			if (amap.get("fnoticeType") != null && StringUtils.isNotBlank(amap.get("fnoticeType").toString())) {
				amap.put("fnoticeType", DictionaryUtil.getString(DictionaryUtil.ChannelSliderUrlType,
						(Integer) amap.get("fnoticeType"), shiroUser.getLanguage()));
			}

			if (amap.get("fnoticeTime") != null && StringUtils.isNotBlank(amap.get("fnoticeTime").toString())) {
				date = (Date) amap.get("fnoticeTime");
				amap.put("fnoticeTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
		}
		return list;
	}
	
	@Transactional(readOnly = true)
	public TAppNotice getTAppNotice(String appNoticeId) {
		return appNoticeDAO.getOne(appNoticeId);
	}
	
	public String addAppNotice(Map<String, Object> valueMap) {
		TAppNotice tAppNotice = new TAppNotice();
		BeanMapper.copy(valueMap, tAppNotice);
		String fnoticeType = valueMap.get("fnoticeType").toString();
		
		tAppNotice.setFnoticeType(Integer.valueOf(fnoticeType));
		tAppNotice.setFnoticeTime(new Date());
		tAppNotice = appNoticeDAO.save(tAppNotice);
		
		return tAppNotice.getId();
	}
	
	public void delAppNotice(String appNoticeId) {
		appNoticeDAO.delete(appNoticeId);
	}
	
	public void editAppNotice(Map<String, Object> valueMap) {
		
		TAppNotice db = appNoticeDAO.getOne(valueMap.get("id").toString());

		if (valueMap.containsKey("fnoticeName") && StringUtils.isNotBlank(valueMap.get("fnoticeName").toString())) {
			db.setFnoticeName(valueMap.get("fnoticeName").toString());
		} else {
			db.setFnoticeName(null);
		}		
		if (valueMap.containsKey("fnoticeType") && StringUtils.isNotBlank(valueMap.get("fnoticeType").toString())) {
			db.setFnoticeType(Integer.valueOf(valueMap.get("fnoticeType").toString()));
		} else {
			db.setFnoticeType(null);
		}
		if (valueMap.containsKey("fnoticeUrl") && StringUtils.isNotBlank(valueMap.get("fnoticeUrl").toString())) {
			db.setFnoticeUrl(valueMap.get("fnoticeUrl").toString());
		} else {
			db.setFnoticeUrl(null);
		}
		if (valueMap.containsKey("fnoticeId") && StringUtils.isNotBlank(valueMap.get("fnoticeId").toString())) {
			db.setFnoticeId(valueMap.get("fnoticeId").toString());
		} else {
			db.setFnoticeId(null);
		}
		if (valueMap.containsKey("fnoticeTitle") && StringUtils.isNotBlank(valueMap.get("fnoticeTitle").toString())) {
			db.setFnoticeTitle(valueMap.get("fnoticeTitle").toString());
		} else {
			db.setFnoticeTitle(null);
		}
		if (valueMap.containsKey("forder") && StringUtils.isNotBlank(valueMap.get("forder").toString())) {
			db.setForder(Integer.valueOf(valueMap.get("forder").toString()));
		} else {
			db.setForder(null);
		}
		db.setFnoticeTime(new Date());
		appNoticeDAO.save(db);
	}

}