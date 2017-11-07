package com.czyh.czyhweb.service.customer;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springside.modules.mapper.BeanMapper;
import org.springside.modules.security.utils.Digests;
import org.springside.modules.utils.Encodes;
import org.springside.modules.utils.Exceptions;

import com.czyh.czyhweb.common.dict.ResponseConfigurationDict;
import com.czyh.czyhweb.dao.CommentDAO;
import com.czyh.czyhweb.dao.CustomerBalanceDetailDAO;
import com.czyh.czyhweb.dao.CustomerBonusDAO;
import com.czyh.czyhweb.dao.CustomerDAO;
import com.czyh.czyhweb.dao.CustomerInfoDAO;
import com.czyh.czyhweb.dao.CustomerTagDAO;
import com.czyh.czyhweb.dao.ImageDAO;
import com.czyh.czyhweb.dao.OrderDAO;
import com.czyh.czyhweb.dao.SponsorDAO;
import com.czyh.czyhweb.dao.UserDAO;
import com.czyh.czyhweb.dto.ImageUploadDTO;
import com.czyh.czyhweb.entity.TComment;
import com.czyh.czyhweb.entity.TCustomer;
import com.czyh.czyhweb.entity.TCustomerBalanceDetail;
import com.czyh.czyhweb.entity.TCustomerTag;
import com.czyh.czyhweb.entity.TOrder;
import com.czyh.czyhweb.entity.TSponsor;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.CommonService;
import com.czyh.czyhweb.service.wechat.WxService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.ConfigurationUtil;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.FileUtils;
import com.czyh.czyhweb.util.PropertiesUtil;
import com.czyh.czyhweb.util.excel.ExcelTemplate;
import com.czyh.czyhweb.util.excel.redExcel.ReadExcel;
import com.czyh.czyhweb.util.html.StaticHtmlBean;
import com.czyh.czyhweb.util.html.StaticHtmlManager;
import com.czyh.czyhweb.util.wx.WxPayUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * czyh用户业务管理类.
 * 
 * @author jinshengzhi
 */
@Component
@Transactional
public class CustomerService {

	private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

	@Autowired
	private CommonService commonService;

	@Autowired
	private SponsorDAO sponsorDAO;

	@Autowired
	private CustomerDAO customerDAO;

	@Autowired
	private ImageDAO imageDAO;

	@Autowired
	private CustomerTagDAO customerTagDAO;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private CommentDAO commentDAO;

	@Autowired
	private OrderDAO orderDAO;

	@Autowired
	private CustomerBonusDAO customerBonusDAO;

	@Autowired
	private CustomerInfoDAO customerInfoDAO;

	@Autowired
	private CustomerBalanceDetailDAO customerBalanceDetailDAO;

	@Autowired
	private WxService wxService;

	private void entryptPassword(TCustomer customer) {
		byte[] salt = Digests.generateSalt(ShiroDbRealm.SALT_SIZE);
		customer.setFsalt(Encodes.encodeHex(salt));

		byte[] hashPassword = Digests.sha1(customer.getFpassword().getBytes(), salt, ShiroDbRealm.INTERATIONS);
		customer.setFpassword(Encodes.encodeHex(hashPassword));
	}

	@Transactional(readOnly = true)
	public Long checkUsername(String username) {
		return customerDAO.checkUsername(username);
	}

	@Transactional(readOnly = true)
	public Long checkName(String name) {
		return sponsorDAO.checkName(name);
	}

	@Transactional(readOnly = true)
	public Long checkEditUsername(String id, String username) {
		return customerDAO.checkEditUsername(id, username);
	}

	@Transactional(readOnly = true)
	public Long checkEditName(String id, String name) {
		return sponsorDAO.checkEditName(id, name);
	}

	@Transactional(readOnly = true)
	public void getMerchantList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, c.fusername as fusername, t.fnumber as fnumber, t.fname as fname, t.fphone as fphone, t.ftype as ftype, t.flevel as flevel, t.fbdId as fbdId, t.fcreaterId as fcreaterId, t.fstatus as fstatus,t.frange as frange,t.fpinkage as fpinkage,t.fregion as fregion,t.fperPrice as fperPrice,t.fsponsorModel as fsponsorModel from TSponsor t left join t.TCustomer c where t.fstatus < 999 ");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_fname") && StringUtils.isNotBlank(valueMap.get("s_fname").toString())) {
				hql.append(" and (t.fname like :s_fname or t.ffullName like :s_fname)");
				hqlMap.put("s_fname", "%" + valueMap.get("s_fname").toString() + "%");
			}
			if (valueMap.containsKey("s_fnumber") && StringUtils.isNotBlank(valueMap.get("s_fnumber").toString())) {
				hql.append(" and t.fnumber like :s_fnumber");
				hqlMap.put("s_fnumber", "%" + valueMap.get("s_fnumber").toString() + "%");
			}
			if (valueMap.containsKey("s_fphone") && StringUtils.isNotBlank(valueMap.get("s_fphone").toString())) {
				hql.append(" and t.fphone like :s_fphone");
				hqlMap.put("s_fphone", "%" + valueMap.get("s_fphone").toString() + "%");
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
			// if (valueMap.containsKey("s_ftype") &&
			// StringUtils.isNotBlank(valueMap.get("s_ftype").toString())) {
			// hql.append(" and t.ftype = :s_ftype ");
			// hqlMap.put("s_ftype",
			// Integer.valueOf(valueMap.get("s_ftype").toString()));
			// }
			// if (valueMap.containsKey("s_flevel") &&
			// StringUtils.isNotBlank(valueMap.get("s_flevel").toString())) {
			// hql.append(" and t.flevel = :s_flevel ");
			// hqlMap.put("s_flevel",
			// Integer.valueOf(valueMap.get("s_flevel").toString()));
			// }
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

		// Date date = null;
		for (Map<String, Object> amap : list) {
			if (amap.get("fbdId") != null && StringUtils.isNotBlank(amap.get("fbdId").toString())) {
				amap.put("fbdId", userDAO.getOne((Long) amap.get("fbdId")).getRealname());
			}
			if (amap.get("fcreaterId") != null && StringUtils.isNotBlank(amap.get("fcreaterId").toString())) {
				amap.put("fcreaterId", userDAO.getOne((Long) amap.get("fcreaterId")).getRealname());
			}
			if (amap.get("ftype") != null && StringUtils.isNotBlank(amap.get("ftype").toString())) {
				amap.put("ftype", DictionaryUtil.getString(DictionaryUtil.SponsorType, (Integer) amap.get("ftype"),
						shiroUser.getLanguage()));
			}
			if (amap.get("flevel") != null && StringUtils.isNotBlank(amap.get("flevel").toString())) {
				amap.put("flevel", DictionaryUtil.getString(DictionaryUtil.SponsorLevel, (Integer) amap.get("flevel"),
						shiroUser.getLanguage()));
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatus", DictionaryUtil.getString(DictionaryUtil.UserStatus, (Integer) amap.get("fstatus"),
						shiroUser.getLanguage()));
			}
		}
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getMerchantMapList() {
		return commonService.find("select t.id as key, t.fname as value from TSponsor t where t.fstatus < 999");
	}

	@Transactional(readOnly = true)
	public TCustomer getCustomer(String customerId) {
		return customerDAO.getOne(customerId);
	}

	@Transactional(readOnly = true)
	public TSponsor getMerchant(String eventId) {
		return sponsorDAO.getOne(eventId);
	}

	public void saveMerchant(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		Date now = new Date();

		TCustomer tCustomer = new TCustomer();
		if (valueMap.containsKey("fusername") && StringUtils.isNotBlank(valueMap.get("fusername").toString())) {
			tCustomer.setFusername(valueMap.get("fusername").toString());
		}
		if (valueMap.containsKey("fname") && StringUtils.isNotBlank(valueMap.get("fname").toString())) {
			tCustomer.setFname(valueMap.get("fname").toString());
		}
		if (valueMap.containsKey("fmobile") && StringUtils.isNotBlank(valueMap.get("fmobile").toString())) {
			tCustomer.setFphone(valueMap.get("fmobile").toString());
		}
		if (valueMap.containsKey("fpassword") && StringUtils.isNotBlank(valueMap.get("fpassword").toString())) {
			tCustomer.setFpassword(valueMap.get("fpassword").toString());
		}
		if (valueMap.containsKey("fstatus") && StringUtils.isNotBlank(valueMap.get("fstatus").toString())) {
			tCustomer.setFstatus(Integer.valueOf(valueMap.get("fstatus").toString()));
		}
		tCustomer.setFtype(10);
		tCustomer.setFcreateTime(now);
		tCustomer.setFupdateTime(now);
		entryptPassword(tCustomer);
		customerDAO.save(tCustomer);

		TSponsor tSponsor = new TSponsor();
		BeanMapper.copy(valueMap, tSponsor);
		String maxNumber = sponsorDAO.getMaxNumber();
		if (StringUtils.isNotBlank(maxNumber)) {
			int number = Integer.parseInt(maxNumber) + 1;
			tSponsor.setFnumber("000" + number);
		} else {
			tSponsor.setFnumber("000100");
		}
		if (valueMap.containsKey("frate") && StringUtils.isNotBlank(valueMap.get("frate").toString())) {
			BigDecimal rate = new BigDecimal(valueMap.get("frate").toString()).divide(new BigDecimal(100), 2,
					RoundingMode.HALF_UP);
			tSponsor.setFrate(rate);
		}
		if (valueMap.containsKey("region") && StringUtils.isNotBlank(valueMap.get("region").toString())) {
			tSponsor.setFregion(valueMap.get("region").toString());
		}
		if (valueMap.containsKey("fsponsorTag") && StringUtils.isNotBlank(valueMap.get("fsponsorTag").toString())) {
			tSponsor.setFsponsorTag(Integer.valueOf(valueMap.get("fsponsorTag").toString()));
		}
		if (valueMap.containsKey("fperPrice") && StringUtils.isNotBlank(valueMap.get("fperPrice").toString())) {
			tSponsor.setFperPrice(valueMap.get("fperPrice").toString());
		}
		if (valueMap.containsKey("frange") && StringUtils.isNotBlank(valueMap.get("frange").toString())) {
			tSponsor.setFrange(new BigDecimal(valueMap.get("frange").toString()));
		}
		if (valueMap.containsKey("fpinkage") && StringUtils.isNotBlank(valueMap.get("fpinkage").toString())) {
			tSponsor.setFpinkage(new BigDecimal(valueMap.get("fpinkage").toString()));
		}

		tSponsor.setTCustomer(tCustomer);
		tSponsor.setFbankId(0);
		tSponsor.setFbalance(BigDecimal.ZERO);
		tSponsor.setFfreezeBalance(BigDecimal.ZERO);
		tSponsor.setFnoSettlementBalance(BigDecimal.ZERO);
		tSponsor.setFscore(new BigDecimal(5));
		tSponsor.setFcreateTime(now);
		tSponsor.setFupdateTime(now);
		tSponsor = sponsorDAO.save(tSponsor);

		if (StringUtils.isNotBlank(tSponsor.getFimage())) {
			tSponsor.setFimages(tSponsor.getFimage());
			String[] ids = tSponsor.getFimage().split(";");
			for (int i = 0; i < ids.length; i++) {
				// TImage tImage = imageDao.getOne(Long.valueOf(ids[i]));
				// tImage.setEntityId(tSponsor.getId());
				// tImage.setEntityType(4);
				// tImage.setStatus(2);
				// imageDao.save(tImage);
				imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(ids[i]), 2, tSponsor.getId(), 4);
			}
		}

		if (valueMap.containsKey("fdetail") && StringUtils.isNotBlank(valueMap.get("fdetail").toString())) {
			String relativePath = new StringBuilder("/merchant/").append(DateFormatUtils.format(now, "yyyy-MM-dd"))
					.append("/").append(tSponsor.getId()).append(".html").toString();
			tSponsor.setFdetailHtmlUrl(relativePath);
			// 创建异步页面静态化Bean
			StaticHtmlBean staticHtmlBean = new StaticHtmlBean();
			staticHtmlBean.setObjectId(tSponsor.getId());
			staticHtmlBean.setTemplateName(Constant.MerchantDetilTemplate);
			staticHtmlBean.setRelativePath(relativePath);

			Map<String, Object> map = Maps.newHashMap();
			map.put("detailHtml", valueMap.get("fdetail").toString());
			staticHtmlBean.setContentMap(map);
			// 异步生成活动静态页面，不占用本线程的执行时间
			StaticHtmlManager.put(staticHtmlBean);
		}
	}

	public void editMerchant(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		Date now = new Date();
		TSponsor db = sponsorDAO.getOne(valueMap.get("id").toString());
		TCustomer tCustomer = db.getTCustomer();

		if (valueMap.containsKey("fusername") && StringUtils.isNotBlank(valueMap.get("fusername").toString())) {
			tCustomer.setFusername(valueMap.get("fusername").toString());
		} else {
			tCustomer.setFusername(null);
		}
		if (valueMap.containsKey("fmobile") && StringUtils.isNotBlank(valueMap.get("fmobile").toString())) {
			tCustomer.setFphone(valueMap.get("fmobile").toString());
		} else {
			tCustomer.setFphone(null);
		}
		if (valueMap.containsKey("fname") && StringUtils.isNotBlank(valueMap.get("fname").toString())) {
			db.setFname(valueMap.get("fname").toString());
			tCustomer.setFname(valueMap.get("fname").toString());
		} else {
			db.setFname(null);
			tCustomer.setFname(null);
		}
		
		if (valueMap.containsKey("fsponsorModel") && StringUtils.isNotBlank(valueMap.get("fsponsorModel").toString())) {
			db.setFsponsorModel(Integer.valueOf(valueMap.get("fsponsorModel").toString()));
		} else {
			db.setFsponsorModel(null);
		}
		
		if (valueMap.containsKey("ffullName") && StringUtils.isNotBlank(valueMap.get("ffullName").toString())) {
			db.setFfullName(valueMap.get("ffullName").toString());
		} else {
			db.setFfullName(null);
		}
		if (valueMap.containsKey("fimage") && StringUtils.isNotBlank(valueMap.get("fimage").toString())) {
			if (!valueMap.get("fimage").toString().equalsIgnoreCase(db.getFimage())) {
				db.setFimage(valueMap.get("fimage").toString());
				db.setFimages(valueMap.get("fimage").toString());
				if (StringUtils.isNotBlank(db.getFimage())) {
					String[] ids = db.getFimage().split(";");
					for (int i = 0; i < ids.length; i++) {
						// TImage tImage =
						// imageDao.getOne(Long.valueOf(ids[i]));
						// tImage.setEntityId(db.getId());
						// tImage.setEntityType(4);
						// tImage.setStatus(2);
						// imageDao.save(tImage);
						imageDAO.saveStatusAndEntityIdAndEntityType(Long.valueOf(ids[i]), 2, db.getId(), 4);
					}
				}
			}
		} else {
			db.setFimage(null);
		}
		if (valueMap.containsKey("fphone") && StringUtils.isNotBlank(valueMap.get("fphone").toString())) {
			db.setFphone(valueMap.get("fphone").toString());
		} else {
			db.setFphone(null);
		}
		if (valueMap.containsKey("ftype") && StringUtils.isNotBlank(valueMap.get("ftype").toString())) {
			db.setFtype(Integer.valueOf(valueMap.get("ftype").toString()));
		} else {
			db.setFtype(null);
		}
		if (valueMap.containsKey("flevel") && StringUtils.isNotBlank(valueMap.get("flevel").toString())) {
			db.setFlevel(Integer.valueOf(valueMap.get("flevel").toString()));
		} else {
			db.setFlevel(null);
		}
		if (valueMap.containsKey("fstatus") && StringUtils.isNotBlank(valueMap.get("fstatus").toString())) {
			db.setFstatus(Integer.valueOf(valueMap.get("fstatus").toString()));
			tCustomer.setFstatus(Integer.valueOf(valueMap.get("fstatus").toString()));
		} else {
			db.setFstatus(null);
			tCustomer.setFstatus(null);
		}
		if (valueMap.containsKey("fwebSite") && StringUtils.isNotBlank(valueMap.get("fwebSite").toString())) {
			db.setFwebSite(valueMap.get("fwebSite").toString());
		} else {
			db.setFwebSite(null);
		}
		if (valueMap.containsKey("fcontractEffective")
				&& StringUtils.isNotBlank(valueMap.get("fcontractEffective").toString())) {
			db.setFcontractEffective(valueMap.get("fcontractEffective").toString());
		} else {
			db.setFcontractEffective(null);
		}
		if (valueMap.containsKey("frate") && StringUtils.isNotBlank(valueMap.get("frate").toString())) {
			BigDecimal rate = new BigDecimal(valueMap.get("frate").toString()).divide(new BigDecimal(100), 2,
					RoundingMode.HALF_UP);
			db.setFrate(rate);
		} else {
			db.setFrate(null);
		}
		if (valueMap.containsKey("fbank") && StringUtils.isNotBlank(valueMap.get("fbank").toString())) {
			db.setFbank(valueMap.get("fbank").toString());
		} else {
			db.setFbank(null);
		}
		if (valueMap.containsKey("fbankAccount") && StringUtils.isNotBlank(valueMap.get("fbankAccount").toString())) {
			db.setFbankAccount(valueMap.get("fbankAccount").toString());
		} else {
			db.setFbankAccount(null);
		}
		if (valueMap.containsKey("fbankAccountName")
				&& StringUtils.isNotBlank(valueMap.get("fbankAccountName").toString())) {
			db.setFbankAccountName(valueMap.get("fbankAccountName").toString());
		} else {
			db.setFbankAccountName(null);
		}
		if (valueMap.containsKey("fbankAccountPersonId")
				&& StringUtils.isNotBlank(valueMap.get("fbankAccountPersonId").toString())) {
			db.setFbankAccountPersonId(valueMap.get("fbankAccountPersonId").toString());
		} else {
			db.setFbankAccountPersonId(null);
		}
		if (valueMap.containsKey("faddress") && StringUtils.isNotBlank(valueMap.get("faddress").toString())) {
			db.setFaddress(valueMap.get("faddress").toString());
		} else {
			db.setFaddress(null);
		}
		if (valueMap.containsKey("fgps") && StringUtils.isNotBlank(valueMap.get("fgps").toString())) {
			db.setFgps(valueMap.get("fgps").toString());
		} else {
			db.setFgps(null);
		}
		if (valueMap.containsKey("fbrief") && StringUtils.isNotBlank(valueMap.get("fbrief").toString())) {
			db.setFbrief(valueMap.get("fbrief").toString());
		} else {
			db.setFbrief(null);
		}
		if (valueMap.containsKey("region") && StringUtils.isNotBlank(valueMap.get("region").toString())) {
			db.setFregion(valueMap.get("region").toString());
		} else {
			db.setFregion(null);
		}
		if (valueMap.containsKey("fsponsorTag") && StringUtils.isNotBlank(valueMap.get("fsponsorTag").toString())) {
			db.setFsponsorTag(Integer.valueOf(valueMap.get("fsponsorTag").toString()));
		} else {

		}
		if (valueMap.containsKey("fperPrice") && StringUtils.isNotBlank(valueMap.get("fperPrice").toString())) {
			db.setFperPrice(valueMap.get("fperPrice").toString());
		} else {
			db.setFperPrice(null);
		}
		if (valueMap.containsKey("frange") && StringUtils.isNotBlank(valueMap.get("frange").toString())) {
			db.setFrange(new BigDecimal(valueMap.get("frange").toString()));
		} else {
			db.setFrange(null);
		}
		if (valueMap.containsKey("fpinkage") && StringUtils.isNotBlank(valueMap.get("fpinkage").toString())) {
			db.setFpinkage(new BigDecimal(valueMap.get("fpinkage").toString()));
		} else {
			db.setFpinkage(null);
		}
		if (valueMap.containsKey("fbdId") && StringUtils.isNotBlank(valueMap.get("fbdId").toString())) {
			db.setFbdId(Long.parseLong(valueMap.get("fbdId").toString()));
		} else {
			db.setFbdId(null);
		}
		if (valueMap.containsKey("fcreaterId") && StringUtils.isNotBlank(valueMap.get("fcreaterId").toString())) {
			db.setFcreaterId(Long.parseLong(valueMap.get("fcreaterId").toString()));
		} else {
			db.setFcreaterId(null);
		}
		if (valueMap.containsKey("fdetail") && StringUtils.isNotBlank(valueMap.get("fdetail").toString())) {
			db.setFdetail(valueMap.get("fdetail").toString());

			String relativePath = new StringBuilder("/merchant/").append(DateFormatUtils.format(now, "yyyy-MM-dd"))
					.append("/").append(db.getId()).append(".html").toString();
			db.setFdetailHtmlUrl(relativePath);
			// 创建异步页面静态化Bean
			StaticHtmlBean staticHtmlBean = new StaticHtmlBean();
			staticHtmlBean.setObjectId(db.getId());
			staticHtmlBean.setTemplateName(Constant.MerchantDetilTemplate);
			staticHtmlBean.setRelativePath(relativePath);

			Map<String, Object> map = Maps.newHashMap();
			map.put("detailHtml", valueMap.get("fdetail").toString());
			staticHtmlBean.setContentMap(map);
			// 异步生成活动静态页面，不占用本线程的执行时间
			StaticHtmlManager.put(staticHtmlBean);
		} else {
			db.setFdetail(null);
		}
		if (valueMap.containsKey("fpassword") && StringUtils.isNotBlank(valueMap.get("fpassword").toString())) {
			tCustomer.setFpassword(valueMap.get("fpassword").toString());
			entryptPassword(tCustomer);
		}
		tCustomer.setFupdateTime(now);
		customerDAO.save(tCustomer);
		db.setFupdateTime(now);
		sponsorDAO.save(db);
	}

	public void delMerchant(String merchantId) {
		TSponsor tSponsor = sponsorDAO.getOne(merchantId);
		sponsorDAO.updateStatus(merchantId, 999);
		customerDAO.updateStatus(tSponsor.getTCustomer().getId(), 999);
	}

	@Transactional(readOnly = true)
	public List<TCustomer> getCusetomerList() {
		return customerDAO.findAll();
	}

	public void saveUnionId(String openid, String id) {
		try {
			String unionId = WxPayUtil.getUnionId(openid);
			customerDAO.saveUnionId(unionId, id);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
	}

	@Transactional(readOnly = true)
	public void getPersonalList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.fusername as fusername, t.fname as fname, t.fphone as fphone,i.fpoint as point, t.fsex as fsex, t.fstatus as fstatus, i.fregisterChannel as fregisterChannel , c.fsubscribe as fsubscribe from TCustomer t ")
				.append(" left join TCustomerInfo i on t.id = i.fcustomerId")
				.append(" left join TSceneUser c on t.fweixinId = c.fopenId where t.ftype = 1 and t.fstatus < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

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
			if (valueMap.containsKey("s_fphone") && StringUtils.isNotBlank(valueMap.get("s_fphone").toString())) {
				hql.append(" and t.fphone like :s_fphone");
				hqlMap.put("s_fphone", "%" + valueMap.get("s_fphone").toString() + "%");
			}
			if (valueMap.containsKey("s_fweixinName")
					&& StringUtils.isNotBlank(valueMap.get("s_fweixinName").toString())) {
				hql.append(" and t.fweixinName like :s_fweixinName");
				hqlMap.put("s_fweixinName", "%" + valueMap.get("s_fweixinName").toString() + "%");
			}
			if (valueMap.containsKey("s_sex") && StringUtils.isNotBlank(valueMap.get("s_sex").toString())) {
				hql.append(" and t.fsex = :s_sex ");
				hqlMap.put("s_sex", Integer.parseInt((valueMap.get("s_sex").toString())));
			}
			if (valueMap.containsKey("s_status") && StringUtils.isNotBlank(valueMap.get("s_status").toString())) {
				hql.append(" and t.fstatus = :s_status ");
				hqlMap.put("s_status", Integer.parseInt((valueMap.get("s_status").toString())));
			}
			if (valueMap.containsKey("s_subscribe") && StringUtils.isNotBlank(valueMap.get("s_subscribe").toString())) {
				hql.append(" and c.fsubscribe = :s_subscribe ");
				hqlMap.put("s_subscribe", Integer.parseInt((valueMap.get("s_subscribe").toString())));
			}
			if (valueMap.containsKey("s_registerChannel")
					&& StringUtils.isNotBlank(valueMap.get("s_registerChannel").toString())) {
				hql.append(" and i.fregisterChannel = :s_registerChannel ");
				hqlMap.put("s_registerChannel", valueMap.get("s_registerChannel").toString());
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
			if (amap.get("ftype") != null && StringUtils.isNotBlank(amap.get("ftype").toString())) {
				amap.put("ftype", DictionaryUtil.getString(DictionaryUtil.SponsorType, (Integer) amap.get("ftype"),
						shiroUser.getLanguage()));
			}
			if (amap.get("fsex") != null && StringUtils.isNotBlank(amap.get("fsex").toString())) {
				amap.put("fsex", DictionaryUtil.getString(DictionaryUtil.Sex, (Integer) amap.get("fsex"),
						shiroUser.getLanguage()));
			}
			if (amap.get("point") != null && StringUtils.isNotBlank(amap.get("point").toString())) {
				amap.put("point", (Integer) amap.get("point"));
			} else {
				amap.put("point", 0);
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatusString", DictionaryUtil.getString(DictionaryUtil.UserStatus,
						(Integer) amap.get("fstatus"), shiroUser.getLanguage()));
			}

			if (amap.get("fsubscribe") != null && StringUtils.isNotBlank(amap.get("fsubscribe").toString())) {
				amap.put("fsubscribe", DictionaryUtil.getString(DictionaryUtil.YesNo, (Integer) amap.get("fsubscribe"),
						shiroUser.getLanguage()));
			}

		}
	}

	@Transactional(readOnly = true)
	public List<String> getMerchantIdList() {
		StringBuilder hql = new StringBuilder();
		hql.append("select t.id as id from TSponsor t where t.fstatus = 1");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		List<String> list = Lists.newArrayList();
		List<Map<String, Object>> resultList = commonService.find(hql.toString(), hqlMap);

		for (Map<String, Object> amap : resultList) {
			if (amap.get("id") != null && StringUtils.isNotBlank(amap.get("id").toString())) {
				list.add(amap.get("id").toString());
			}
		}
		return list;
	}

	public void updateStatus(String customerId, Integer status) {
		customerDAO.updateStatus(customerId, status);
	}

	public void createMerchantDetailHtml() {
		List<TSponsor> merchantList = sponsorDAO.findByFdetailHtmlUrlIsnull();
		for (TSponsor tSponsor : merchantList) {
			String relativePath = new StringBuilder("/merchant/")
					.append(DateFormatUtils.format(new Date(), "yyyy-MM-dd")).append("/").append(tSponsor.getId())
					.append(".html").toString();
			tSponsor.setFdetailHtmlUrl(relativePath);
			// 创建异步页面静态化Bean
			StaticHtmlBean staticHtmlBean = new StaticHtmlBean();
			staticHtmlBean.setObjectId(tSponsor.getId());
			staticHtmlBean.setTemplateName(Constant.MerchantDetilTemplate);
			staticHtmlBean.setRelativePath(relativePath);

			Map<String, Object> map = Maps.newHashMap();
			map.put("detailHtml", tSponsor.getFdetail());
			staticHtmlBean.setContentMap(map);
			// 异步生成活动静态页面，不占用本线程的执行时间
			StaticHtmlManager.put(staticHtmlBean);
			sponsorDAO.updateFdetailHtmlUrl(tSponsor.getId(), relativePath);
		}
	}

	public void merchantNumber() {
		List<TSponsor> merchantList = sponsorDAO.findByFnumberIsNull();
		String maxNumber = sponsorDAO.getMaxNumber();

		int number = 0;
		if (StringUtils.isNotBlank(maxNumber)) {
			number = Integer.parseInt(maxNumber) + 1;
		} else {
			number = 100;
		}

		for (TSponsor tSponsor : merchantList) {
			number++;
			tSponsor.setFnumber("000" + number);
		}
		sponsorDAO.save(merchantList);
	}

	@Transactional(readOnly = true)
	public void getCustomerTagsList(Map<String, Object> valueMap, CommonPage page) {

		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.fcustomerId as fcustomerId, t.ftag as ftag, t.foperator as foperator, t.fcreateTime as fcreateTime from TCustomerTag t where 1=1");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {

			if (valueMap.containsKey("s_ftag") && StringUtils.isNotBlank(valueMap.get("s_ftag").toString())) {
				hql.append(" and t.ftag = :s_ftag ");
				hqlMap.put("s_ftag", Integer.valueOf(valueMap.get("s_ftag").toString()));

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

			if (amap.get("ftag") != null && StringUtils.isNotBlank(amap.get("ftag").toString())) {
				amap.put("ftag", DictionaryUtil.getString(DictionaryUtil.PushUserTag, (Integer) amap.get("ftag"),
						shiroUser.getLanguage()));
			}

			if (amap.get("foperator") != null && StringUtils.isNotBlank(amap.get("foperator").toString())) {
				amap.put("foperator", userDAO.getOne((Long) amap.get("foperator")).getRealname());
			}

			if (amap.get("fcreateTime") != null && StringUtils.isNotBlank(amap.get("fcreateTime").toString())) {
				date = (Date) amap.get("fcreateTime");
				amap.put("fcreateTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
		}
	}

	public void editCustomerTags(Map<String, Object> valueMap) {

		Date now = new Date();
		TCustomerTag tcustomerTag = customerTagDAO.getOne(valueMap.get("id").toString());

		if (valueMap.containsKey("ftag") && StringUtils.isNotBlank(valueMap.get("ftag").toString())) {
			tcustomerTag.setFtag(Integer.valueOf(valueMap.get("ftag").toString()));
		} else {
			tcustomerTag.setFtag(null);
		}

		tcustomerTag.setFcreateTime(now);
		customerTagDAO.save(tcustomerTag);
	}

	@Transactional(readOnly = true)
	public TCustomerTag getCustomerTags(String customerTagsId) {

		return customerTagDAO.getOne(customerTagsId);
	}

	@Transactional(readOnly = true)
	public void getCommentList(CommonPage page, Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"SELECT c.id as DT_RowId, e.ftitle as ftitle, s.fname as fsponsorName, s.fphone as fsponsorPhone, c.fcustomerName as fcustomerName, c.fcontent as fcontent, c.fcreateTime as fcreateTime, c.fscore as fscore, c.fobjectId as fobjectId, c.fuserName as fuserName, c.freplyTime as freplyTime, c.fstatus as fstatus FROM TComment c INNER JOIN TEvent e ON c.fobjectId = e.id LEFT JOIN TSponsor s on e.TSponsor.id = s.id WHERE c.ftype = :type");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("type", 2);
		try {
			if (valueMap.containsKey("s_fcustomerName")
					&& StringUtils.isNotBlank(valueMap.get("s_fcustomerName").toString())) {
				hql.append(" and c.fcustomerName like :s_fcustomerName ");
				hqlMap.put("s_fcustomerName", "%" + valueMap.get("s_fcustomerName").toString() + "%");
			}
			if (valueMap.containsKey("fcreateTimeStart")
					&& StringUtils.isNotBlank(valueMap.get("fcreateTimeStart").toString())) {
				hql.append(" and c.fcreateTime >= :fcreateTimeStart ");
				hqlMap.put("fcreateTimeStart",
						DateUtils.parseDate(valueMap.get("fcreateTimeStart").toString(), "yyyy-MM-dd"));
			}
			if (valueMap.containsKey("fcreateTimeEnd")
					&& StringUtils.isNotBlank(valueMap.get("fcreateTimeEnd").toString())) {
				hql.append(" and c.fcreateTime < :fcreateTimeEnd ");
				// 选择的截止日期加一天，保证了查询条件的完整性，解决了时分秒的问题。
				hqlMap.put("fcreateTimeEnd", DateUtils
						.addDays(DateUtils.parseDate(valueMap.get("fcreateTimeEnd").toString(), "yyyy-MM-dd"), 1));
			}
			hql.append(" and c.fstatus = :s_fstatus ");
			hqlMap.put("s_fstatus", Integer.valueOf(valueMap.get("s_fstatus").toString()));

			hql.append("ORDER BY c.fcreateTime DESC");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}

		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		Date date = null;
		StringBuilder info = new StringBuilder();
		int offset = page.getOffset();
		for (Map<String, Object> amap : list) {
			amap.put("serNum", ++offset);

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
	public void createCommentExcel(Map<String, Object> valueMap, String datePath, String excelFileName) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"SELECT c.id as DT_RowId, e.ftitle as ftitle, s.fname as fsponsorName, s.fphone as fsponsorPhone, c.fcustomerName as fcustomerName, c.fcreateTime as fcreateTime, c.fcontent as fcontent, c.fscore as fscore, c.fuserName as fuserName, c.freplyTime as freplyTime, c.freply as freply, c.fstatus as fstatus FROM TComment c ")
				.append("INNER JOIN TEvent e ON c.fobjectId = e.id LEFT JOIN TSponsor s on e.TSponsor.id = s.id WHERE c.ftype = :type");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("type", 2);
		try {
			// int status =
			// Integer.valueOf(valueMap.get("s_fstatus").toString());
			hql.append(" and c.fstatus = :s_fstatus ");
			hqlMap.put("s_fstatus", Integer.valueOf(valueMap.get("s_fstatus").toString()));

			hql.append("ORDER BY c.fcreateTime DESC");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}

		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		ExcelTemplate excel = ExcelTemplate.getInstance().readTemplateClassPath("/template/excel/commentTemp.xlsx");

		// 定义全路径，为了以后将相对路径和文件名添加进去
		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("excelCommentPath")).append("/").append(datePath).append("/");

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
			if (amap.get("fscore") != null && StringUtils.isNotBlank(amap.get("fscore").toString())) {
				excel.createNewCol(amap.get("fscore").toString());
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

	public void delComment(String commentId) {
		TComment c = commentDAO.getOne(commentId);
		if (c != null) {
			if (c.getFstatus().intValue() != 999) {
				commentDAO.updateStatus(c.getId(), 999);
			}
		}
	}

	public void editComment(Map<String, Object> map) {
		if (map.containsKey("forder") && StringUtils.isNotBlank(map.get("forder").toString()) && map.containsKey("id")
				&& StringUtils.isNotBlank(map.get("id").toString())) {
			commentDAO.updateOrder(map.get("id").toString(), Integer.valueOf(map.get("forder").toString()));
		}
	}

	public void passedComment(String cId, Integer status) {
		TComment tComment = commentDAO.findOne(cId);
		if (status.intValue() == 20 && StringUtils.isNotBlank(tComment.getForderId())) {
			TOrder tOrder = orderDAO.findOne(tComment.getForderId());
			Date now = new Date();
			String rebateTime = ConfigurationUtil
					.getPropertiesValue(ResponseConfigurationDict.RESPONSE_PROPERTIES_REBATETIME);
			if (StringUtils.isNotBlank(rebateTime) && rebateTime.equals("1")) {
				// if(tOrder.getTEvent().getFcommentRewardType().intValue()==10){
				// //反积分
				// Integer bonus = (new BigDecimal(ConfigurationUtil
				// .getPropertiesValue(ResponseConfigurationDict.RESPONSE_PROPERTIES_BONUSPROPORTION))
				// .multiply(tOrder.getFreceivableTotal()).setScale(0,
				// BigDecimal.ROUND_HALF_UP)).intValue();
				// orderDAO.updateOrderRewardBonus(bonus,
				// tComment.getForderId());
				// TCustomerBonus customerBonus = new TCustomerBonus();
				// customerBonus.setFbonus(bonus);
				// customerBonus.setFcreateTime(now);
				// customerBonus.setFcustermerId(tComment.getFcustomerId());
				// customerBonus.setFobject(tComment.getId());
				// customerBonus.setFtype(80);
				// customerBonusDAO.save(customerBonus);
				// customerInfoDAO.updatePoint(tComment.getFcustomerId(),
				// bonus);
				// commentDAO.updateRewardBonus(cId, status, bonus);
				// TCustomerInfo tCustomerInfo =
				// customerInfoDAO.getByCustomerId(tOrder.getTCustomer().getId());
				// wxService.pushBonusAccountMsg(tOrder.getTCustomer(),tCustomerInfo,80,bonus);
				// }else
				// if(tOrder.getTEvent().getFcommentRewardType().intValue()==20){
				// 反现金
				// orderDAO.updateOrderRewardAmount(tOrder.getTEvent().getFcommentRewardAmount(),
				// tComment.getForderId());
				TCustomerBalanceDetail tCustomerBalanceDetail = new TCustomerBalanceDetail();
				// tCustomerBalanceDetail.setFamount(tOrder.getTEvent().getFcommentRewardAmount());
				tCustomerBalanceDetail.setFcreateTime(now);
				tCustomerBalanceDetail.setFcustomerId(tComment.getFcustomerId());
				tCustomerBalanceDetail.setFinOut(1);// 入账
				tCustomerBalanceDetail.setFitemType(10);
				customerBalanceDetailDAO.save(tCustomerBalanceDetail);
				// customerInfoDAO.updateBalance(tComment.getFcustomerId(),
				// tOrder.getTEvent().getFcommentRewardAmount());
				// commentDAO.updateRewardAmount(cId, status,
				// tOrder.getTEvent().getFcommentRewardAmount());
				// }else{
				// commentDAO.updateStatus(cId, 20);
				// }
			} else {
				commentDAO.updateStatus(cId, 20);
			}
		} else {
			commentDAO.updateStatus(cId, 20);
		}

	}

	/**
	 * 用户标签导入
	 * 
	 */
	public void addUserTagExcel(ImageUploadDTO imageUploadDTO) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		try {
			MultipartFile file = imageUploadDTO.getFile();
			String fileName = file.getOriginalFilename();// 获取图片名称
			String fileExt = FileUtils.getFileExt(fileName);// 获取图片扩展名

			// MultipartFile 转换 File类型
			CommonsMultipartFile cf = (CommonsMultipartFile) file;
			DiskFileItem fi = (DiskFileItem) cf.getFileItem();
			File f = fi.getStoreLocation();

			List<List<Object>> listex = ReadExcel.readExcel(f, fileExt);

			for (List<Object> list2 : listex) {
				TCustomerTag tCustomerTag = new TCustomerTag();
				// 根据用户手机号查出用户的id
				TCustomer tCustomer = customerDAO.getByCustomerId(list2.get(0).toString());
				if (tCustomer != null) {
					tCustomerTag.setFcustomerId(tCustomer.getId());
					tCustomerTag.setFtag(Integer.parseInt(list2.get(1).toString()));
					tCustomerTag.setFoperator(shiroUser.getId());
					tCustomerTag.setFcreateTime(new Date());
					customerTagDAO.save(tCustomerTag);
				}
			}

		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("导入用户标签失败,请重新导入");
		}
	}

	@Transactional
	public void createCustomerExcel(Map<String, Object> map, String datePath, String excelFileName, String sessionid) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as id, t.fusername as fusername, t.fname as fname, t.fphone as fphone,i.fpoint as point, t.fsex as fsex, t.fstatus as fstatus from TCustomer t ")
				.append(" inner join TCustomerInfo i on t.id = i.fcustomerId where t.ftype = 1 and t.fstatus < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
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

		ExcelTemplate excel = ExcelTemplate.getInstance().readTemplateClassPath("/template/excel/customerTemp.xlsx");
		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("customerExcel")).append("/").append(datePath).append("/");
		for (Map<String, Object> amap : list) {
			excel.creatNewRow();
			if (amap.get("id") != null && StringUtils.isNotBlank(amap.get("id").toString())) {
				excel.createNewCol(amap.get("id").toString());
			}
			if (amap.get("fusername") != null && StringUtils.isNotBlank(amap.get("fusername").toString())) {
				excel.createNewCol(amap.get("fusername").toString());
			} else {
				excel.createNewCol("");
			}
			if (amap.get("fname") != null && StringUtils.isNotBlank(amap.get("fname").toString())) {
				excel.createNewCol(amap.get("fname").toString());
			} else {
				excel.createNewCol("");
			}
			if (amap.get("fphone") != null && StringUtils.isNotBlank(amap.get("fphone").toString())) {
				excel.createNewCol(amap.get("fphone").toString());
			} else {
				excel.createNewCol("");
			}
			if (amap.get("fsex") != null && StringUtils.isNotBlank(amap.get("fsex").toString())) {
				excel.createNewCol(DictionaryUtil.getString(DictionaryUtil.Sex, (Integer) amap.get("fsex"),
						shiroUser.getLanguage()));
			}
			if (amap.get("point") != null && StringUtils.isNotBlank(amap.get("point").toString())) {
				excel.createNewCol((Integer) amap.get("point"));
			} else {
				amap.put("point", 0);
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				excel.createNewCol(DictionaryUtil.getString(DictionaryUtil.UserStatus, (Integer) amap.get("fstatus"),
						shiroUser.getLanguage()));
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

	/**
	 * 
	 * @Title: getRewardChartsOrderList
	 * @Description: 奖金排行榜列表,默认按照总金额降序
	 * @param valueMap
	 *            参数集合
	 * @param page
	 *            分页对象
	 * @author zyp(添加方法的人)
	 * @Date 2016年11月16日 下午6:55:27
	 */
	@Transactional(readOnly = true)
	public void getRewardChartsOrderList(Map<String, Object> valueMap, CommonPage page) {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		try {
			hql.append(
					"SELECT c.id as id, c.fname as name,c.fweixinId as weiXinId,i.frewardOrderNumber as rewardOrderNumber, i.ftotalBalance as totalBalance,i.ffreezeBalance as freezeBalance, (i.fwithdrawalBalance+i.fbalance) as arrivaled,i.fwithdrawalBalance as withdrawalBalance,i.fbalance as balance")
					.append(" FROM TCustomer c JOIN TCustomerInfo i ON c.id = i.fcustomerId")
					.append(" WHERE c.ftype = 1 AND c.fstatus < 999 ");
			if (valueMap.containsKey("realname") && valueMap.get("realname") != null
					&& !valueMap.get("realname").equals("")) {
				logger.warn("getRewardChartsOrderList---按照名称模糊查询:" + valueMap.get("realname").toString());
				hql.append("and c.fname like :realname");
				hqlMap.put("realname", "%" + valueMap.get("realname").toString() + "%");
			}
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				logger.warn("getRewardChartsOrderList---排序规则:按照" + valueMap.get("order[0][column]").toString()
						+ ",升序还是降序:" + valueMap.get("order[0][dir]").toString());
				hql.append(" order by i.f").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by i.ftotalBalance DESC");
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}
		logger.warn("生成的hql为:" + hql.toString());
		commonService.findPage(hql.toString(), page, hqlMap);
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getRegionList(Integer cityId) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.value as key, t.name as value from TDictionaryRegion t where t.fstatus = 0 and t.cityValue = :cityId");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("cityId", cityId);
		return commonService.find(hql.toString(), hqlMap);
	}

}