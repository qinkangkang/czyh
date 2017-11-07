package com.czyh.czyhweb.service.order;

import java.io.File;
import java.math.BigDecimal;
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
import com.czyh.czyhweb.dao.CustomerDAO;
import com.czyh.czyhweb.dao.CustomerEventDistributionDAO;
import com.czyh.czyhweb.dao.CustomerInfoDAO;
import com.czyh.czyhweb.dao.EventBargainingDAO;
import com.czyh.czyhweb.dao.EventDAO;
import com.czyh.czyhweb.dao.EventSpecDAO;
import com.czyh.czyhweb.dao.ExpressDAO;
import com.czyh.czyhweb.dao.OrderDAO;
import com.czyh.czyhweb.dao.OrderGoodsDAO;
import com.czyh.czyhweb.dao.OrderRefundDAO;
import com.czyh.czyhweb.dao.OrderStatusChangeDAO;
import com.czyh.czyhweb.dao.PayInfoDAO;
import com.czyh.czyhweb.dao.WxPayDAO;
import com.czyh.czyhweb.dto.OrderRecipientDTO;
import com.czyh.czyhweb.dto.ResponseDTO;
import com.czyh.czyhweb.entity.TCustomerInfo;
import com.czyh.czyhweb.entity.TExpress;
import com.czyh.czyhweb.entity.TOrder;
import com.czyh.czyhweb.entity.TOrderGoods;
import com.czyh.czyhweb.entity.TOrderRefund;
import com.czyh.czyhweb.entity.TOrderStatusChange;
import com.czyh.czyhweb.entity.TPayInfo;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.CommonService;
import com.czyh.czyhweb.service.coupon.CouponService;
import com.czyh.czyhweb.service.customer.UserService;
import com.czyh.czyhweb.service.payInfo.ChargeService;
import com.czyh.czyhweb.service.push.PushService;
import com.czyh.czyhweb.service.wechat.WxService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.HttpClientUtil;
import com.czyh.czyhweb.util.PropertiesUtil;
import com.czyh.czyhweb.util.excel.ExcelTemplate;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pingplusplus.model.Refund;

/**
 * 订单业务管理类.
 * 
 * @author jinshengzhi
 */
@Component
@Transactional
public class OrderService {

	private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

	private static JsonMapper mapper = new JsonMapper(Include.ALWAYS);

	@Autowired
	private CommonService commonService;

	@Autowired
	private WxService wxService;

	@Autowired
	private EventDAO eventDAO;

	@Autowired
	private PayInfoDAO payInfoDAO;

	@Autowired
	private EventSpecDAO eventSpecDAO;

	@Autowired
	private OrderDAO orderDAO;

	@Autowired
	private OrderStatusChangeDAO orderStatusChangeDAO;

	@Autowired
	private ChargeService chargeService;

	@Autowired
	private CustomerInfoDAO customerInfoDAO;

	@Autowired
	private UserService userService;

	@Autowired
	private CustomerBargainingDAO customerBargainingDAO;

	@Autowired
	private EventBargainingDAO eventBargainingDAO;

	@Autowired
	private CustomerEventDistributionDAO customerEventDistributionDAO;

	@Autowired
	private OrderGoodsDAO orderGoodsDAO;

	@Autowired
	private ExpressDAO expressDAO;

	@Autowired
	private PushService pushService;
	
	@Autowired
	private OrderRefundDAO orderRefundDAO;

	@Transactional(readOnly = true)
	public void getOrderList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.forderNum as forderNum, t.fsponsorName as sponsorName, t.fsponsorPhone as sponsorPhone, t.fcustomerName as customerName, t.fcustomerPhone as customerPhone, t.fcustomerSex as customerSex, t.fstatus as fstatus,")
				.append(" t.fprice as fprice, t.fcount as fcount, t.fpostage as fpostage, t.fchangeAmount as fchangeAmount, t.freceivableTotal as freceivableTotal, t.ftotal as ftotal, t.fcreateTime as fcreateTime, t.fpayType as fpayType, ")
				.append(" t.fpayTime as fpayTime, t.fverificationTime as fverificationTime, t.flockFlag as flockFlag, t.fcsRemark as fcsRemark,")
				.append(" t.fsource as fsource,t.fsellModel as fsellModel,t.fchannel as fchannel,r.frefundStatus as frefundStatus,r.frefundType as frefundType,r.fRefundTotal as fRefundTotal")
				.append(" from TOrder t left join TOrderRefund r on t.id = r.forderId and r.frefundStatus <100");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_fstatus") && StringUtils.isNotBlank(valueMap.get("s_fstatus").toString())) {
				int status = Integer.valueOf(valueMap.get("s_fstatus").toString());
				if (status == 0) {
					hql.append(" where 1 = 1 and (t.fstatus != 999)");
				} else if (status == 100) {
					hql.append(" where 1 = 1 and (t.fstatus between 100 and 109)");
				} else if (status == 110) {
					hql.append(" where 1 = 1 and (t.fstatus between 110 and 119) and r.frefundStatus = 10 ");
				} else if (status == 115) {
					hql.append(" and r.frefundStatus > 10 where 1 = 1 and (t.fstatus between 110 and 119)");
				} else if (status == 190) {
					hql.append(" where 1 = 1 and (t.fstatus between 190 and 199)");
				} else if (status == -1) {
					hql.append(" where 1 = 1 and (t.fstatus between 110 and 199)");
				} else {
					hql.append(" where 1 = 1 and t.fstatus = :status");
					hqlMap.put("status", status);
				}
			}
			if (valueMap.containsKey("s_forderNum") && StringUtils.isNotBlank(valueMap.get("s_forderNum").toString())) {
				hql.append(" and t.forderNum like :s_forderNum ");
				hqlMap.put("s_forderNum", "%" + valueMap.get("s_forderNum").toString() + "%");
			}
			if (valueMap.containsKey("s_feventTitle")
					&& StringUtils.isNotBlank(valueMap.get("s_feventTitle").toString())) {
				hql.append(
						" and t.id in (select g.forderId from TOrderGoods g where g.feventTitle like :s_feventTitle)");
				hqlMap.put("s_feventTitle", "%" + valueMap.get("s_feventTitle").toString() + "%");
			}
			if (valueMap.containsKey("s_fcustomerName")
					&& StringUtils.isNotBlank(valueMap.get("s_fcustomerName").toString())) {
				hql.append(" and t.fcustomerName like :s_fcustomerName ");
				hqlMap.put("s_fcustomerName", "%" + valueMap.get("s_fcustomerName").toString() + "%");
			}
			if (valueMap.containsKey("s_fcustomerPhone")
					&& StringUtils.isNotBlank(valueMap.get("s_fcustomerPhone").toString())) {
				hql.append(" and t.fcustomerPhone like :s_fcustomerPhone ");
				hqlMap.put("s_fcustomerPhone", "%" + valueMap.get("s_fcustomerPhone").toString() + "%");
			}
			if (valueMap.containsKey("s_fsponsor") && StringUtils.isNotBlank(valueMap.get("s_fsponsor").toString())) {
				hql.append(" and t.TSponsor.id = :s_fsponsor ");
				hqlMap.put("s_fsponsor", valueMap.get("s_fsponsor").toString());
			}
			if (valueMap.containsKey("s_source") && StringUtils.isNotBlank(valueMap.get("s_source").toString())) {
				hql.append(" and t.fsource = :s_source ");
				hqlMap.put("s_source", Integer.valueOf(valueMap.get("s_source").toString()));
			}
			if (valueMap.containsKey("s_sellModel") && StringUtils.isNotBlank(valueMap.get("s_sellModel").toString())) {
				hql.append(" and t.fsellModel = :s_sellModel ");
				hqlMap.put("s_sellModel", Integer.valueOf(valueMap.get("s_sellModel").toString()));
			}
			if (valueMap.containsKey("s_changeAmount")
					&& StringUtils.isNotBlank(valueMap.get("s_changeAmount").toString())) {
				if (Integer.valueOf(valueMap.get("s_changeAmount").toString()) == 1) {
					hql.append(" and t.fchangeAmount is not null ");
				}
			}
			if (valueMap.containsKey("s_fbdId") && StringUtils.isNotBlank(valueMap.get("s_fbdId").toString())) {
				hql.append(" and e.fbdId = :s_fbdId ");
				hqlMap.put("s_fbdId", Long.valueOf(valueMap.get("s_fbdId").toString()));
			}
			if (valueMap.containsKey("s_channel") && StringUtils.isNotBlank(valueMap.get("s_channel").toString())) {
				hql.append(" and t.fchannel = :s_channel ");
				hqlMap.put("s_channel", Integer.valueOf(valueMap.get("s_channel").toString()));
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
			//hql.append(" and r.frefundStatus <100");
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
		List<Map<String, Object>> orderStatusList = null;

		StringBuilder info = new StringBuilder();
		Date date = null;
		int status = 0;
		String channel = "";
		for (Map<String, Object> amap : list) {
			if (amap.get("fpayTime") != null && StringUtils.isNotBlank(amap.get("fpayTime").toString())) {
				date = (Date) amap.get("fpayTime");
				amap.put("fpayTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
			} else {
				date = (Date) amap.get("fcreateTime");
				amap.put("fpayTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
			}
			if (amap.get("fverificationTime") != null
					&& StringUtils.isNotBlank(amap.get("fverificationTime").toString())) {
				date = (Date) amap.get("fverificationTime");
				amap.put("fverificationTime",
						DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
			}
			if (amap.get("fpayType") != null && StringUtils.isNotBlank(amap.get("fpayType").toString())) {
				if (amap.get("fchannel") != null && StringUtils.isNotBlank(amap.get("fchannel").toString())) {
					if ((Integer) amap.get("fchannel") == 1) {
						channel = "(H5)";
					} else {
						channel = "(APP)";
					}
				}
				amap.put("fpayType", DictionaryUtil.getString(DictionaryUtil.PayType, ((Integer) amap.get("fpayType")),
						shiroUser.getLanguage()) + channel);
			}
			if (amap.get("fsellModel") != null && StringUtils.isNotBlank(amap.get("fsellModel").toString())) {
				amap.put("fsellmodelString", DictionaryUtil.getString(DictionaryUtil.SellModel,
						((Integer) amap.get("fsellModel")), shiroUser.getLanguage()));
			}
			if (amap.get("frefundType") != null && StringUtils.isNotBlank(amap.get("frefundType").toString())) {
				amap.put("frefundType", DictionaryUtil.getString(DictionaryUtil.RefundType,
						((Integer) amap.get("frefundType")), shiroUser.getLanguage()));
			}	
			if (amap.get("frefundStatus") != null && StringUtils.isNotBlank(amap.get("frefundStatus").toString())) {
				status = ((Integer) amap.get("frefundStatus")).intValue();
				if(status==10){
					amap.put("frefundStatusString", "待审核");
				}else if(status==20){
					amap.put("frefundStatusString", "待发货");
				}else if(status==30){
					amap.put("frefundStatusString", "待退款");
				}
				amap.put("frefundStatus", status);
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				status = ((Integer) amap.get("fstatus")).intValue();

				info.delete(0, info.length());
				// 获取该订单下的状态变更记录
				hql.delete(0, hql.length());
				hql.append(
						"select t.fafterStatus as fafterStatus, t.fcreateTime as fcreateTime from TOrderStatusChange t where t.TOrder.id = :orderId order by t.fcreateTime asc");
				hqlMap.clear();
				hqlMap.put("orderId", amap.get("DT_RowId").toString());
				orderStatusList = commonService.find(hql.toString(), hqlMap);
				info.append("<ul>");
				for (Map<String, Object> bmap : orderStatusList) {
					info.append("<li>");
					if (bmap.get("fafterStatus") != null
							&& StringUtils.isNotBlank(bmap.get("fafterStatus").toString())) {
						info.append(DictionaryUtil.getString(DictionaryUtil.OrderStatus,
								(Integer) bmap.get("fafterStatus")));
					}
					if (bmap.get("fcreateTime") != null && StringUtils.isNotBlank(bmap.get("fcreateTime").toString())) {
						info.append("&nbsp;&nbsp;&nbsp;")
								.append(DateFormatUtils.format((Date) bmap.get("fcreateTime"), "yyyy-MM-dd HH:mm"));
					}
					info.append("</li>");
				}
				info.append("</ul>");
				// 将根据订单的状态让订单列表中显示不同的颜色背景
				if (status == 10) {
					amap.put("fstatusString",
							info.insert(0,
									"<a id='orderStatus' tabindex='0' class='btn btn-xs btn-warning' role='button' data-toggle='popover' data-placement='left' data-trigger='hover' data-html='true' title='订单历史' data-content='")
									.append("'>").append(DictionaryUtil.getString(DictionaryUtil.OrderStatus, status,
											shiroUser.getLanguage()))
									.append("</a>").toString());
				} else if (status == 20) {
					amap.put("fstatusString",
							info.insert(0,
									"<a id='orderStatus' tabindex='0' class='btn btn-xs btn-info' role='button' data-toggle='popover' data-placement='left' data-trigger='hover' data-html='true' title='订单历史' data-content='")
									.append("'>").append(DictionaryUtil.getString(DictionaryUtil.OrderStatus, status,
											shiroUser.getLanguage()))
									.append("</a>").toString());
				} else if (status == 60) {
					amap.put("fstatusString",
							info.insert(0,
									"<a id='orderStatus' tabindex='0' class='btn btn-xs btn-primary' role='button' data-toggle='popover' data-placement='left' data-trigger='hover' data-html='true' title='订单历史' data-content='")
									.append("'>").append(DictionaryUtil.getString(DictionaryUtil.OrderStatus, status,
											shiroUser.getLanguage()))
									.append("</a>").toString());
				} else if (status == 70) {
					amap.put("fstatusString",
							info.insert(0,
									"<a id='orderStatus' tabindex='0' class='btn btn-xs btn-success' role='button' data-toggle='popover' data-placement='left' data-trigger='hover' data-html='true' title='订单历史' data-content='")
									.append("'>").append(DictionaryUtil.getString(DictionaryUtil.OrderStatus, status,
											shiroUser.getLanguage()))
									.append("</a>").toString());
				} else if (status >= 100 || status <= 199) {
					amap.put("fstatusString",
							info.insert(0,
									"<a id='orderStatus' tabindex='0' class='btn btn-xs btn-danger' role='button' data-toggle='popover' data-placement='left' data-trigger='hover' data-html='true' title='订单历史' data-content='")
									.append("'>").append(DictionaryUtil.getString(DictionaryUtil.OrderStatus, status,
											shiroUser.getLanguage()))
									.append("</a>").toString());
				}

			}
			if (amap.get("fRefundTotal") != null && StringUtils.isNotBlank(amap.get("fRefundTotal").toString())) {
				amap.put("fRefundTotal",amap.get("fRefundTotal").toString());
			}

			info.delete(0, info.length());
			if (amap.get("sponsorName") != null && StringUtils.isNotBlank(amap.get("sponsorName").toString())) {
				info.append(amap.get("sponsorName").toString());
			}
			if (amap.get("sponsorPhone") != null && StringUtils.isNotBlank(amap.get("sponsorPhone").toString())) {
				info.append("<br/>").append(amap.get("sponsorPhone").toString());
			}
			amap.put("sponsorInfo", info.toString());

			info.delete(0, info.length());
			if (amap.get("customerName") != null && StringUtils.isNotBlank(amap.get("customerName").toString())) {
				info.append(amap.get("customerName").toString());
			}
			if (amap.get("customerSex") != null && StringUtils.isNotBlank(amap.get("customerSex").toString())) {
				info.append("<br/>").append(amap.get("customerSex").toString());
			}
			if (amap.get("customerPhone") != null && StringUtils.isNotBlank(amap.get("customerPhone").toString())) {
				info.append("<br/>").append(amap.get("customerPhone").toString());
			}
			amap.put("customerInfo", info.toString());

			info.delete(0, info.length());
			if (amap.get("fprice") != null && StringUtils.isNotBlank(amap.get("fprice").toString())) {
				info.append("<strong>单价：</strong>").append(amap.get("fprice").toString());
			}
			if (amap.get("fcount") != null && StringUtils.isNotBlank(amap.get("fcount").toString())) {
				info.append("<br/>").append("<strong>数量：</strong>").append(" ╳ ").append(amap.get("fcount").toString());
			}
			if (amap.get("fpostage") != null && StringUtils.isNotBlank(amap.get("fpostage").toString())) {
				info.append("<br/>").append("<strong>邮费：</strong>").append(amap.get("fpostage").toString());
			}
			if (amap.get("fchangeAmount") != null && StringUtils.isNotBlank(amap.get("fchangeAmount").toString())) {
				if (amap.get("") != null && StringUtils.isNotBlank(amap.get("fsource").toString())
						&& ((Integer) amap.get("fsource")).intValue() == 20) {
					info.append("<br/>").append("<strong>折扣价格：</strong>").append(amap.get("fchangeAmount").toString());
				} else {
					info.append("<br/>").append("<strong>变更金额：</strong>").append(amap.get("fchangeAmount").toString());
				}
			}
			amap.put("priceInfo", info.toString());

			info.delete(0, info.length());
			if (amap.get("freceivableTotal") != null
					&& StringUtils.isNotBlank(amap.get("freceivableTotal").toString())) {
				info.append("<strong>应收：</strong>").append(amap.get("freceivableTotal").toString()).append("<br/>");
			}
			if (amap.get("ftotal") != null && StringUtils.isNotBlank(amap.get("ftotal").toString())) {
				info.append("<strong>实收：</strong>").append(amap.get("ftotal").toString());
			}
			amap.put("totalInfo", info.toString());
		}
	}

	@Transactional(readOnly = true)
	public TOrder getOrder(String orderId) {
		return orderDAO.getOne(orderId);
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getOrderStatusList(String orderId) {
		StringBuilder hql = new StringBuilder();

		hql.append(
				"select t.fafterStatus as fafterStatus, t.fcreateTime as fcreateTime from TOrderStatusChange t where t.TOrder.id = :orderId order by t.fcreateTime asc");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("orderId", orderId);
		List<Map<String, Object>> orderStatusList = commonService.find(hql.toString(), hqlMap);
		Date date = null;
		for (Map<String, Object> bmap : orderStatusList) {
			if (bmap.get("fafterStatus") != null && StringUtils.isNotBlank(bmap.get("fafterStatus").toString())) {
				bmap.put("statusString",
						DictionaryUtil.getString(DictionaryUtil.OrderStatus, (Integer) bmap.get("fafterStatus")));
			}
			if (bmap.get("fcreateTime") != null && StringUtils.isNotBlank(bmap.get("fcreateTime").toString())) {
				date = (Date) bmap.get("fcreateTime");
				bmap.put("fcreateTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
		}
		return orderStatusList;
	}

	/**
	 * 客服进行订单锁定的的方法
	 * 
	 * @param orderId
	 */
	public void csLock(String orderId) {
		orderDAO.updateOrderLockFlag(1, orderId);
	}

	/**
	 * 客服进行订单锁定的的方法
	 * 
	 * @param orderId
	 */
	public void csUnLock(String orderId) {
		orderDAO.updateOrderLockFlag(0, orderId);
	}

	/**
	 * 客服进行订单解锁的的方法
	 * 
	 * @param orderId
	 */
	public String csCancel(String orderId) {
		StringBuilder msg = new StringBuilder();
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		TOrder tOrder = orderDAO.getOne(orderId);
		if (tOrder.getFstatus().intValue() >= 100) {
			throw new ServiceException("本订单无法执行取消！原因：该订单中已经是取消状态");
		}
		// String orderInfo = new
		// StringBuilder().append("用户:").append(tOrder).append("|订单ID:").append(orderId)
		// .append("|订单内容:").append(tOrder.getFeventTitle()).append("|下单时间:")
		// .append(DateFormatUtils.format(tOrder.getFcreateTime(), "yyyy-MM-dd
		// HH:mm")).toString();

		// 如果订单是零元订单将订单占用的库存数量退回
		if (tOrder.getFtotal().compareTo(BigDecimal.ZERO) == 0) {
			this.orderStatusChange(4, shiroUser.getName(), orderId, null, tOrder.getFstatus(), 101);
			// 如果是零元订单，设置订单状态为“客服取消”状态
			orderDAO.updateOrderStatus(101, orderId);

			eventSpecDAO.addStock(tOrder.getFcount(), tOrder.getTEventSpec().getId());

//			eventDAO.addStock(tOrder.getFcount(), tOrder.getTEvent().getId());
			msg.append("该订单取消成功，已被置为已取消状态。");
			// TODO 需要远程通知FXL接口修改限购器的数量
			// QuotaUtil.subCustomerBuy(tOrder.getTEvent().getId(),
			// tOrder.getTCustomer().getId(), tOrder.getFcount());
		} else {
			// 如果是非零元订单，设置订单状态为“客服取消申请退款”状态
			if (tOrder.getFstatus().intValue() == 10) {
				orderDAO.updateOrderStatus(101, orderId);

				eventSpecDAO.addStock(tOrder.getFcount(), tOrder.getTEventSpec().getId());

//				eventDAO.addStock(tOrder.getFcount(), tOrder.getTEvent().getId());
				msg.append("该订单取消成功，已被置为已取消状态。");
			} else if (tOrder.getFstatus().intValue() == 20) {
				orderDAO.updateOrderStatus(111, orderId);
				msg.append("该订单取消成功，请进入“退款管理”模块办理退款。");
			}
		}
		return msg.toString();
	}

	public boolean resend(String orderId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		TOrder tOrder = orderDAO.getOne(orderId);
		ResponseDTO responseDTO = new ResponseDTO();
		try {
			String postUrl = new StringBuilder().append(Constant.czyhFscUrl).toString();
			Map<String, Object> paramsMap = Maps.newHashMap();
			paramsMap.put("osFlag", "czyhweb");
			paramsMap.put("operator", shiroUser.getName());
			paramsMap.put("type", "repeat_order");
			paramsMap.put("clientOrderNo", tOrder.getForderNum());
			String reusltSt = HttpClientUtil.callUrlPostForJson(postUrl, paramsMap);
			responseDTO = mapper.fromJson(reusltSt, ResponseDTO.class);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
		return responseDTO.isSuccess();
	}

	/**
	 * 客服进行订单核销的的方法
	 * 
	 * @param orderId
	 */
	public void csVerification(String orderId, boolean ifPush) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		this.orderStatusChange(4, shiroUser.getName(), orderId, null, 20, 60);
		// 记录订单变更状态，“客服核销”状态
		orderDAO.updateOrderStatus(60, orderId);
		/*
		 * // 将核销订单数据加入到结算表中 Date now = new Date(); TOrder tOrder =
		 * orderDAO.findOne(orderId); TCustomer tCustomer =
		 * customerDAO.findOne(tOrder.getTCustomer().getId()); TSponsor tSponsor
		 * = tOrder.getTSponsor(); TEvent event =
		 * eventDAO.findOne(tOrder.getTEvent().getId());
		 * 
		 * TOrderVerification tOrderVerification = new TOrderVerification();
		 * tOrderVerification.setTCustomerByFcustomerId(tOrder.getTCustomer());
		 * tOrderVerification.setTCustomerByFoperator(tOrder.getTSponsor().
		 * getTCustomer()); tOrderVerification.setTOrder(tOrder);
		 * tOrderVerification.setTSponsor(tSponsor);
		 * tOrderVerification.setFcreateTime(now);
		 * tOrderVerification.setFclientOperate(2);
		 * tOrderVerification.setForderOriginalAmount(tOrder.getFtotal());
		 * tOrderVerification.setFsettlementType(event.getFsettlementType());
		 * tOrderVerification.setFstatus(10); if (tOrder.getFchangeAmount() !=
		 * null) {
		 * tOrderVerification.setForderChangelAmount(tOrder.getFchangeAmount());
		 * } else { tOrderVerification.setForderChangelAmount(BigDecimal.ZERO);
		 * } BigDecimal originalOrderAmount = tOrder.getFtotal(); TEventSpec
		 * tEventSpec = eventSpecDAO.findOne(tOrder.getTEventSpec().getId()); if
		 * (event.getFsettlementType().intValue() == 20) {
		 * tOrderVerification.setForderRate(tEventSpec.getFpointsPrice());
		 * BigDecimal rate = tEventSpec.getFpointsPrice() != null ?
		 * tEventSpec.getFpointsPrice() : BigDecimal.ZERO; BigDecimal res =
		 * BigDecimal.ONE.subtract(rate, new MathContext(2,
		 * RoundingMode.HALF_UP)); BigDecimal orderAmount =
		 * originalOrderAmount.multiply(res, new MathContext(2,
		 * RoundingMode.DOWN)); tOrderVerification.setForderAmount(orderAmount);
		 * } else if (event.getFsettlementType().intValue() == 30) {
		 * tOrderVerification.setForderAmount(new
		 * BigDecimal(tOrder.getFcount()).multiply(
		 * tEventSpec.getFsettlementPrice() != null ?
		 * tEventSpec.getFsettlementPrice() : BigDecimal.ZERO)); }
		 * 
		 * orderVerificationDAO.save(tOrderVerification);
		 */

		// 如果是分销返利订单，修改不可用余额为可用余额
		// if (StringUtils.isNotBlank(tOrder.getFdistributionerId())) {
		// customerInfoDAO.updateFreezeBalanceAndBalance(tOrder.getFdistributionerId(),
		// tOrder.getFdistributionRebateAmount());

		// TCustomerEventDistribution tCustomerEventDistribution =
		// customerEventDistributionDAO.getByFcustomerIdAndFeventId
		// (tOrder.getFdistributionerId(), tOrder.getTEvent().getId());
		// if(tCustomerEventDistribution!=null){
		// tCustomerEventDistribution.setFbuyCount(tCustomerEventDistribution.getFbuyCount()+1);
		// tCustomerEventDistribution.setFdistributionCount(tCustomerEventDistribution.getFdistributionCount()+1);
		// tCustomerEventDistribution.setForderTotal(tCustomerEventDistribution.getForderTotal().add(tOrder.getFtotal()));
		// tCustomerEventDistribution.setFupdateTime(now);
		// tCustomerEventDistribution.setFdistributionRewardAmount(tCustomerEventDistribution.getFdistributionRewardAmount().add(tOrder.getFdistributionRebateAmount()));
		// customerEventDistributionDAO.save(tCustomerEventDistribution);
		// }
		// }

		// if (ifPush) {
		// if (tOrder.getFcommentRewardType() == null ||
		// tOrder.getFcommentRewardType().intValue() == 0) {
		// String orderPushWeiXin = ConfigurationUtil
		// .getPropertiesValue(ResponseConfigurationDict.RESPONSE_PROPERTIES_ORDEREVALUATION);
		// if (StringUtils.isNotBlank(orderPushWeiXin) &&
		// orderPushWeiXin.equals("1")
		// && StringUtils.isNotBlank(tCustomer.getFweixinId())) {
		// wxService.pushOrderEvaluationMsg(tOrder, tCustomer.getFweixinId());
		// }
		// } else if (tOrder.getFcommentRewardType().intValue() == 10) {
		// String orderPushWeiXin = ConfigurationUtil
		// .getPropertiesValue(ResponseConfigurationDict.RESPONSE_PROPERTIES_ORDEREVALUATIONBONUS);
		// if (StringUtils.isNotBlank(orderPushWeiXin) &&
		// orderPushWeiXin.equals("1")
		// && StringUtils.isNotBlank(tCustomer.getFweixinId())) {
		// wxService.pushOrderEvaluationBonusMsg(tOrder,
		// tCustomer.getFweixinId());
		// }
		// } else if (tOrder.getFcommentRewardType().intValue() == 20) {
		// String orderPushWeiXin = ConfigurationUtil
		// .getPropertiesValue(ResponseConfigurationDict.RESPONSE_PROPERTIES_ORDERCASHBACK);
		// if (StringUtils.isNotBlank(orderPushWeiXin) &&
		// orderPushWeiXin.equals("1")
		// && StringUtils.isNotBlank(tCustomer.getFweixinId())) {
		// wxService.pushOrderCashBackMsg(tOrder, tCustomer.getFweixinId());
		// }
		// }
		// }
	}

	public ResponseDTO queryTicket(String orderId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		TOrder tOrder = orderDAO.findOne(orderId);

		ResponseDTO responseDTO = new ResponseDTO();
		try {
			String postUrl = new StringBuilder().append(Constant.czyhFscUrl).toString();

			Map<String, Object> paramsMap = Maps.newHashMap();
			paramsMap.put("osFlag", "czyhweb");
			paramsMap.put("operator", shiroUser.getName());
			paramsMap.put("type", "query_order");
			paramsMap.put("clientOrderNo", tOrder.getForderNum());
			String reusltSt = HttpClientUtil.callUrlPostForJson(postUrl, paramsMap);

			responseDTO = mapper.fromJson(reusltSt, ResponseDTO.class);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
		return responseDTO;
	}

	/**
	 * 客服进行订单取消的的方法
	 * 
	 * @param orderId
	 */
	public String saveCsRemark(String orderId, String csRemark) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder remark = new StringBuilder();
		remark.append("＜－－－－－－－－－－ ").append(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm"))
				.append(" －－－－－－－－－－ ").append(StringUtils.SPACE).append(StringUtils.SPACE).append("填写人：")
				.append(shiroUser.getName()).append(" －－－－－－－－－－＞").append(StringUtils.LF).append(csRemark);
		// orderDAO.appendCsRemark(orderId, remark.toString());
		TOrder order = orderDAO.getOne(orderId);
		if (StringUtils.isNotBlank(order.getFcsRemark())) {
			remark.insert(0, new StringBuilder().append(order.getFcsRemark()).append(StringUtils.LF));
		}
		orderDAO.updateCsRemark(orderId, remark.toString());
		// order.setFcsRemark(remark.toString());
		// orderDAO.save(order);
		return remark.toString();
	}

	/**
	 * 客服进行支付订单退款的的方法
	 * 
	 * @param orderId
	 */
	public Map<String, Object> refund(String orderId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		TOrder tOrder = orderDAO.getOne(orderId);
		TOrderRefund orderRefund = null;
		orderRefund = orderRefundDAO.findByOrder(orderId);
		if (tOrder.getFstatus().intValue() < 100 && tOrder.getFstatus().intValue() > 199) {
			throw new ServiceException("本订单无法执行退款！原因：该订单中不是可退款订单");
		}
		if (tOrder.getFpayType() == null) {
			throw new ServiceException("本订单无法执行退款！原因：该订单中没有支付类型信息");
		}
		if (tOrder.getFtotal().compareTo(BigDecimal.ZERO) == 0) {
			throw new ServiceException("本订单无法执行退款！原因：该订单是零元订单");
		}

		String msg = null;
		boolean flag = false;

		// app ping++支付
		TPayInfo tPayInfo = payInfoDAO.getByOrderId(orderId, 1);
		Map<String, Object> params = Maps.newHashMap();
		if(orderRefund!=null && orderRefund.getfRefundTotal().compareTo(tPayInfo.getFpayAmount())<=0){
			params.put("amount", orderRefund.getfRefundTotal());
		}else{
			params.put("amount", tPayInfo.getFpayAmount());
		}
		
		params.put("description", "退款");
		Refund refund = chargeService.createRefund(tPayInfo);
		flag = refund.getSucceed();
		String url = "";
		System.out.println(flag);
		if (refund.getStatus().equals("pending") && tPayInfo.getFpayType().intValue() == 30) {
			this.orderStatusChange(4, shiroUser.getName(), orderId, null, tOrder.getFstatus(), 115);
			orderDAO.updateOrderStatus(115, orderId);
			url = refund.getFailureMsg().split(" ")[1];
		}
		if (tPayInfo.getFpayType().intValue() == 20) {
			orderDAO.updateOrderStatus(115, orderId);
		}
		Map<String, Object> refundMsg = Maps.newHashMap();
		refundMsg.put("url", url);

		return refundMsg;
	}
	
	public Map<String, Object> refundAudit(String orderId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		TOrder tOrder = orderDAO.getOne(orderId);
		TOrderRefund orderRefund = orderRefundDAO.findByOrder(orderId);
		if (tOrder.getFstatus().intValue() < 100 && tOrder.getFstatus().intValue() > 199) {
			throw new ServiceException("本订单无法执行退款！原因：该订单中不是可退款订单");
		}
		if (tOrder.getFpayType() == null) {
			throw new ServiceException("本订单无法执行退款！原因：该订单中没有支付类型信息");
		}
		if (tOrder.getFtotal().compareTo(BigDecimal.ZERO) == 0) {
			throw new ServiceException("本订单无法执行退款！原因：该订单是零元订单");
		}

		if(orderRefund.getFrefundType().intValue()==1){
			orderRefundDAO.updateOrderStatus(20, orderRefund.getId());
		}else{
			orderRefundDAO.updateOrderStatus(30, orderRefund.getId());
		}
		Map<String, Object> refundMsg = Maps.newHashMap();
		refundMsg.put("url", "");
		return refundMsg;
	}

	public void orderStatusChange(int operatorType, String operatorId, String orderId, String changeReason,
			int beforeStatus, int afterStatus) {
		Date now = new Date();

		TOrderStatusChange tOrderStatusChange = new TOrderStatusChange();
		tOrderStatusChange.setFoperatorId(operatorId);
		tOrderStatusChange.setFoperatorType(operatorType);
		tOrderStatusChange.setTOrder(new TOrder(orderId));
		tOrderStatusChange.setFcreateTime(now);
		tOrderStatusChange.setFupdateTime(now);
		tOrderStatusChange.setFchangeReason(changeReason);
		tOrderStatusChange.setFbeforeStatus(beforeStatus);
		tOrderStatusChange.setFafterStatus(afterStatus);

		orderStatusChangeDAO.save(tOrderStatusChange);
	}

	@Transactional(readOnly = true)
	public void createOrderExcel(Map<String, Object> valueMap, String datePath, String excelFileName) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.forderNum as forderNum, t.TCustomer.id as customerId, t.frecipient as frecipient,t.fsponsorName as sponsorName, t.fsponsorPhone as sponsorPhone, t.fcustomerName as customerName, t.fcustomerPhone as customerPhone, t.fcustomerSex as customerSex, t.fstatus as fstatus,")
				.append(" t.fprice as fprice, t.fcount as fcount, t.fpostage as fpostage, t.fchangeAmount as fchangeAmount, t.fchangeAmountInstruction as fchangeAmountInstruction, t.freceivableTotal as freceivableTotal, t.ftotal as ftotal, t.fcreateTime as fcreateTime, t.fpayType as fpayType, t.fpayTime as fpayTime, t.fverificationTime as fverificationTime, t.frefundTime as frefundTime, t.frefundReason as frefundReason, t.flockFlag as flockFlag, t.freturn as freturn, t.fremark as fremark from TOrder t where 1 = 1");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_forderNum") && StringUtils.isNotBlank(valueMap.get("s_forderNum").toString())) {
				hql.append(" and t.forderNum like :s_forderNum ");
				hqlMap.put("s_forderNum", "%" + valueMap.get("s_forderNum").toString() + "%");
			}
			if (valueMap.containsKey("s_feventTitle")
					&& StringUtils.isNotBlank(valueMap.get("s_feventTitle").toString())) {
				hql.append(" and t.feventTitle like :s_feventTitle ");
				hqlMap.put("s_feventTitle", "%" + valueMap.get("s_feventTitle").toString() + "%");
			}
			if (valueMap.containsKey("s_fbdId") && StringUtils.isNotBlank(valueMap.get("s_fbdId").toString())) {
				hql.append(" and e.fbdId = :s_fbdId ");
				hqlMap.put("s_fbdId", Long.valueOf(valueMap.get("s_fbdId").toString()));
			}
			if (valueMap.containsKey("s_fcustomerName")
					&& StringUtils.isNotBlank(valueMap.get("s_fcustomerName").toString())) {
				hql.append(" and t.fcustomerName like :s_fcustomerName ");
				hqlMap.put("s_fcustomerName", "%" + valueMap.get("s_fcustomerName").toString() + "%");
			}
			if (valueMap.containsKey("s_fcustomerPhone")
					&& StringUtils.isNotBlank(valueMap.get("s_fcustomerPhone").toString())) {
				hql.append(" and t.fcustomerPhone like :s_fcustomerPhone ");
				hqlMap.put("s_fcustomerPhone", "%" + valueMap.get("s_fcustomerPhone").toString() + "%");
			}
			if (valueMap.containsKey("s_fsponsor") && StringUtils.isNotBlank(valueMap.get("s_fsponsor").toString())) {
				hql.append(" and t.TSponsor.id = :s_fsponsor ");
				hqlMap.put("s_fsponsor", valueMap.get("s_fsponsor").toString());
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
				if (status == 0) {
					hql.append(" and (t.fstatus != 999)");
				} else if (status == 10) {
					hql.append(" and (t.fstatus between 10 and 19)");
				} else if (status == 20) {
					hql.append(" and (t.fstatus between 20 and 59)");
				} else if (status == 100) {
					hql.append(" and (t.fstatus between 100 and 109)");
				} else if (status == 110) {
					hql.append(" and (t.fstatus between 110 and 119)");
				} else if (status == 190) {
					hql.append(" and (t.fstatus between 190 and 199)");
				} else if (status == -1) {
					hql.append(" and (t.fstatus between 110 and 199)");
				} else {
					hql.append(" and t.fstatus = :status");
					hqlMap.put("status", status);
				}
			}
			hql.append(" order by t.fcreateTime desc");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询HQL语句时出错！");
		}

		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);

		ExcelTemplate excel = ExcelTemplate.getInstance().readTemplateClassPath("/template/excel/orderTemp.xlsx");

		// 定义全路径，为了以后将相对路径和文件名添加进去
		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("excelOrderPath")).append("/").append(datePath).append("/");
		List<Map<String, Object>> userList = userService.getUserListByCategoryId(2);
		Map<String, String> userMap = Maps.newHashMap();
		for (Map<String, Object> uMap : userList) {
			userMap.put(uMap.get("key").toString(), uMap.get("value").toString());
		}

		OrderRecipientDTO recipientDTO = null;
		String orderNum = null;
		for (Map<String, Object> amap : list) {
			excel.creatNewRow();
			if (amap.get("frecipient") != null && StringUtils.isNotBlank(amap.get("frecipient").toString())) {
				recipientDTO = mapper.fromJson(amap.get("frecipient").toString(), OrderRecipientDTO.class);
			}
			if (amap.get("forderNum") != null && StringUtils.isNotBlank(amap.get("forderNum").toString())) {
				orderNum = amap.get("forderNum").toString();
				excel.createNewCol(orderNum);
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("sponsorName") != null && StringUtils.isNotBlank(amap.get("sponsorName").toString())) {
				excel.createNewCol(amap.get("sponsorName").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("sponsorPhone") != null && StringUtils.isNotBlank(amap.get("sponsorPhone").toString())) {
				excel.createNewCol(amap.get("sponsorPhone").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("customerId") != null && StringUtils.isNotBlank(amap.get("customerId").toString())) {
				excel.createNewCol(amap.get("customerId").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("customerName") != null && StringUtils.isNotBlank(amap.get("customerName").toString())) {
				excel.createNewCol(amap.get("customerName").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("customerPhone") != null && StringUtils.isNotBlank(amap.get("customerPhone").toString())) {
				excel.createNewCol(amap.get("customerPhone").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("customerSex") != null && StringUtils.isNotBlank(amap.get("customerSex").toString())) {
				excel.createNewCol(amap.get("customerSex").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (recipientDTO != null && StringUtils.isNotBlank(recipientDTO.getRecipient())) {
				excel.createNewCol(recipientDTO.getRecipient());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (recipientDTO != null && StringUtils.isNotBlank(recipientDTO.getPhone())) {
				excel.createNewCol(recipientDTO.getPhone());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (recipientDTO != null && StringUtils.isNotBlank(recipientDTO.getAddress())) {
				excel.createNewCol(recipientDTO.getAddress());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fpostage") != null && StringUtils.isNotBlank(amap.get("fpostage").toString())) {
				excel.createNewCol(((BigDecimal) amap.get("fpostage")).doubleValue());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fchangeAmount") != null && StringUtils.isNotBlank(amap.get("fchangeAmount").toString())) {
				excel.createNewCol(((BigDecimal) amap.get("fchangeAmount")).doubleValue());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fchangeAmountInstruction") != null
					&& StringUtils.isNotBlank(amap.get("fchangeAmountInstruction").toString())) {
				excel.createNewCol(amap.get("fchangeAmountInstruction").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("freceivableTotal") != null
					&& StringUtils.isNotBlank(amap.get("freceivableTotal").toString())) {
				excel.createNewCol(((BigDecimal) amap.get("freceivableTotal")).doubleValue());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("ftotal") != null && StringUtils.isNotBlank(amap.get("ftotal").toString())) {
				excel.createNewCol(((BigDecimal) amap.get("ftotal")).doubleValue());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				excel.createNewCol(DictionaryUtil.getString(DictionaryUtil.OrderStatus, (Integer) amap.get("fstatus")));
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fcreateTime") != null && StringUtils.isNotBlank(amap.get("fcreateTime").toString())) {
				excel.createNewCol(DateFormatUtils.format((Date) amap.get("fcreateTime"), "yyyy-MM-dd HH:mm"));
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fpayTime") != null && StringUtils.isNotBlank(amap.get("fpayTime").toString())) {
				excel.createNewCol(DateFormatUtils.format((Date) amap.get("fpayTime"), "yyyy-MM-dd HH:mm"));
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fverificationTime") != null
					&& StringUtils.isNotBlank(amap.get("fverificationTime").toString())) {
				excel.createNewCol(DateFormatUtils.format((Date) amap.get("fverificationTime"), "yyyy-MM-dd HH:mm"));
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("frefundTime") != null && StringUtils.isNotBlank(amap.get("frefundTime").toString())) {
				excel.createNewCol(DateFormatUtils.format((Date) amap.get("frefundTime"), "yyyy-MM-dd HH:mm"));
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("frefundReason") != null && StringUtils.isNotBlank(amap.get("frefundReason").toString())) {
				excel.createNewCol(amap.get("frefundReason").toString());
			} else {
				excel.createNewCol(StringUtils.EMPTY);
			}
			if (amap.get("fremark") != null && StringUtils.isNotBlank(amap.get("fremark").toString())) {
				excel.createNewCol(amap.get("fremark").toString());
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

	// 退款之后，改变用户附加信息表中的下单金额
	public void backCustomerInfo(TOrder tOrder) {
		try {
			TCustomerInfo tCustomerInfo = customerInfoDAO.getByCustomerId(tOrder.getTCustomer().getId());
			tCustomerInfo.setForderTotal(tCustomerInfo.getForderTotal().subtract(tOrder.getFtotal()));
			tCustomerInfo.setFpayOrderNumber(tCustomerInfo.getFpayOrderNumber() - 1);
			tCustomerInfo.setForderNumber(tCustomerInfo.getForderNumber() - 1);
			if (tOrder.getFtotal() != null) {
				if (tOrder.getFtotal().compareTo(BigDecimal.ZERO) == 0) {
					tCustomerInfo.setFpayZeroOrderNumber(tCustomerInfo.getFpayZeroOrderNumber() - 1);
				}
			}
			if (tCustomerInfo.getFfirstOrderTime() != null) {
				if (tCustomerInfo.getFfirstOrderTime().compareTo(tOrder.getFcreateTime()) == 0) {
					tCustomerInfo.setFfirstOrderTime(null);
				}
			}
			customerInfoDAO.save(tCustomerInfo);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
	}

	@Transactional(readOnly = true)
	public void getOrderListAudit(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.forderNum as forderNum, t.fsponsorName as sponsorName, t.fsponsorPhone as sponsorPhone, t.fcustomerName as customerName, t.fcustomerPhone as customerPhone, t.fcustomerSex as customerSex, t.fstatus as fstatus,")
				.append(" t.fprice as fprice, t.fcount as fcount, t.fpostage as fpostage, t.fchangeAmount as fchangeAmount, t.freceivableTotal as freceivableTotal, t.ftotal as ftotal, t.fcreateTime as fcreateTime, t.fpayType as fpayType, t.fpayTime as fpayTime, t.fverificationTime as fverificationTime, t.flockFlag as flockFlag, t.fcsRemark as fcsRemark,")
				.append(" t.fsource as fsource,t.fsellModel as fsellModel from TOrder t where 1 = 1");

		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_forderNum") && StringUtils.isNotBlank(valueMap.get("s_forderNum").toString())) {
				hql.append(" and t.forderNum like :s_forderNum ");
				hqlMap.put("s_forderNum", "%" + valueMap.get("s_forderNum").toString() + "%");
			}
			if (valueMap.containsKey("s_feventTitle")
					&& StringUtils.isNotBlank(valueMap.get("s_feventTitle").toString())) {
				hql.append(" and t.feventTitle like :s_feventTitle ");
				hqlMap.put("s_feventTitle", "%" + valueMap.get("s_feventTitle").toString() + "%");
			}
			if (valueMap.containsKey("s_fcustomerName")
					&& StringUtils.isNotBlank(valueMap.get("s_fcustomerName").toString())) {
				hql.append(" and t.fcustomerName like :s_fcustomerName ");
				hqlMap.put("s_fcustomerName", "%" + valueMap.get("s_fcustomerName").toString() + "%");
			}
			if (valueMap.containsKey("s_fcustomerPhone")
					&& StringUtils.isNotBlank(valueMap.get("s_fcustomerPhone").toString())) {
				hql.append(" and t.fcustomerPhone like :s_fcustomerPhone ");
				hqlMap.put("s_fcustomerPhone", "%" + valueMap.get("s_fcustomerPhone").toString() + "%");
			}
			if (valueMap.containsKey("s_fsponsor") && StringUtils.isNotBlank(valueMap.get("s_fsponsor").toString())) {
				hql.append(" and t.TSponsor.id = :s_fsponsor ");
				hqlMap.put("s_fsponsor", valueMap.get("s_fsponsor").toString());
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
				if (status == 0) {
					hql.append(" and (t.fstatus between 10 and 99)");
				} else if (status == 10) {
					hql.append(" and (t.fstatus between 10 and 19)");
				} else if (status == 20) {
					hql.append(" and (t.fstatus between 20 and 59)");
				} else if (status == 100) {
					hql.append(" and (t.fstatus between 100 and 109)");
				} else if (status == 110) {
					hql.append(" and (t.fstatus between 110 and 111)");
				} else if (status == 115) {
					hql.append(" and (t.fstatus between 115 and 119)");
				} else if (status == 190) {
					hql.append(" and (t.fstatus between 190 and 199)");
				} else if (status == -1) {
					hql.append(" and (t.fstatus between 110 and 199)");
				} else {
					hql.append(" and t.fstatus = :status");
					hqlMap.put("status", status);
				}
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
		List<Map<String, Object>> orderStatusList = null;

		StringBuilder info = new StringBuilder();
		Date date = null;
		int status = 0;
		for (Map<String, Object> amap : list) {
			if (amap.get("fpayTime") != null && StringUtils.isNotBlank(amap.get("fpayTime").toString())) {
				date = (Date) amap.get("fpayTime");
				amap.put("fpayTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
			} else {
				date = (Date) amap.get("fcreateTime");
				amap.put("fpayTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
			}
			if (amap.get("fverificationTime") != null
					&& StringUtils.isNotBlank(amap.get("fverificationTime").toString())) {
				date = (Date) amap.get("fverificationTime");
				amap.put("fverificationTime",
						DateFormatUtils.format(date, "yyyy-MM-dd HH:mm").replaceAll(" ", "<br/>"));
			}
			if (amap.get("fpayType") != null && StringUtils.isNotBlank(amap.get("fpayType").toString())) {
				amap.put("fpayType", DictionaryUtil.getString(DictionaryUtil.PayType, ((Integer) amap.get("fpayType")),
						shiroUser.getLanguage()));
			}
			if (amap.get("fsellModel") != null && StringUtils.isNotBlank(amap.get("fsellModel").toString())) {
				amap.put("fsellmodelString", DictionaryUtil.getString(DictionaryUtil.SellModel,
						((Integer) amap.get("fsellModel")), shiroUser.getLanguage()));
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				status = ((Integer) amap.get("fstatus")).intValue();

				info.delete(0, info.length());
				// 获取该订单下的状态变更记录
				hql.delete(0, hql.length());
				hql.append(
						"select t.fafterStatus as fafterStatus, t.fcreateTime as fcreateTime from TOrderStatusChange t where t.TOrder.id = :orderId order by t.fcreateTime asc");
				hqlMap.clear();
				hqlMap.put("orderId", amap.get("DT_RowId").toString());
				orderStatusList = commonService.find(hql.toString(), hqlMap);
				info.append("<ul>");
				for (Map<String, Object> bmap : orderStatusList) {
					info.append("<li>");
					if (bmap.get("fafterStatus") != null
							&& StringUtils.isNotBlank(bmap.get("fafterStatus").toString())) {
						info.append(DictionaryUtil.getString(DictionaryUtil.OrderStatus,
								(Integer) bmap.get("fafterStatus")));
					}
					if (bmap.get("fcreateTime") != null && StringUtils.isNotBlank(bmap.get("fcreateTime").toString())) {
						info.append("&nbsp;&nbsp;&nbsp;")
								.append(DateFormatUtils.format((Date) bmap.get("fcreateTime"), "yyyy-MM-dd HH:mm"));
					}
					info.append("</li>");
				}
				info.append("</ul>");
				// 将根据订单的状态让订单列表中显示不同的颜色背景
				if (status == 10) {
					amap.put("fstatusString",
							info.insert(0,
									"<a id='orderStatus' tabindex='0' class='btn btn-xs btn-warning' role='button' data-toggle='popover' data-placement='left' data-trigger='hover' data-html='true' title='订单历史' data-content='")
									.append("'>").append(DictionaryUtil.getString(DictionaryUtil.OrderStatus, status,
											shiroUser.getLanguage()))
									.append("</a>").toString());
				} else if (status == 20) {
					amap.put("fstatusString",
							info.insert(0,
									"<a id='orderStatus' tabindex='0' class='btn btn-xs btn-info' role='button' data-toggle='popover' data-placement='left' data-trigger='hover' data-html='true' title='订单历史' data-content='")
									.append("'>").append(DictionaryUtil.getString(DictionaryUtil.OrderStatus, status,
											shiroUser.getLanguage()))
									.append("</a>").toString());
				} else if (status == 60) {
					amap.put("fstatusString",
							info.insert(0,
									"<a id='orderStatus' tabindex='0' class='btn btn-xs btn-primary' role='button' data-toggle='popover' data-placement='left' data-trigger='hover' data-html='true' title='订单历史' data-content='")
									.append("'>").append(DictionaryUtil.getString(DictionaryUtil.OrderStatus, status,
											shiroUser.getLanguage()))
									.append("</a>").toString());
				} else if (status == 70) {
					amap.put("fstatusString",
							info.insert(0,
									"<a id='orderStatus' tabindex='0' class='btn btn-xs btn-success' role='button' data-toggle='popover' data-placement='left' data-trigger='hover' data-html='true' title='订单历史' data-content='")
									.append("'>").append(DictionaryUtil.getString(DictionaryUtil.OrderStatus, status,
											shiroUser.getLanguage()))
									.append("</a>").toString());
				} else if (status >= 100 || status <= 199) {
					amap.put("fstatusString",
							info.insert(0,
									"<a id='orderStatus' tabindex='0' class='btn btn-xs btn-danger' role='button' data-toggle='popover' data-placement='left' data-trigger='hover' data-html='true' title='订单历史' data-content='")
									.append("'>").append(DictionaryUtil.getString(DictionaryUtil.OrderStatus, status,
											shiroUser.getLanguage()))
									.append("</a>").toString());
				}

			}

			info.delete(0, info.length());
			if (amap.get("sponsorName") != null && StringUtils.isNotBlank(amap.get("sponsorName").toString())) {
				info.append(amap.get("sponsorName").toString());
			}
			if (amap.get("sponsorPhone") != null && StringUtils.isNotBlank(amap.get("sponsorPhone").toString())) {
				info.append("<br/>").append(amap.get("sponsorPhone").toString());
			}
			amap.put("sponsorInfo", info.toString());

			info.delete(0, info.length());
			if (amap.get("customerName") != null && StringUtils.isNotBlank(amap.get("customerName").toString())) {
				info.append(amap.get("customerName").toString());
			}
			if (amap.get("customerSex") != null && StringUtils.isNotBlank(amap.get("customerSex").toString())) {
				info.append("<br/>").append(amap.get("customerSex").toString());
			}
			if (amap.get("customerPhone") != null && StringUtils.isNotBlank(amap.get("customerPhone").toString())) {
				info.append("<br/>").append(amap.get("customerPhone").toString());
			}
			amap.put("customerInfo", info.toString());

			info.delete(0, info.length());
			if (amap.get("fprice") != null && StringUtils.isNotBlank(amap.get("fprice").toString())) {
				info.append("<strong>单价：</strong>").append(amap.get("fprice").toString());
			}
			if (amap.get("fcount") != null && StringUtils.isNotBlank(amap.get("fcount").toString())) {
				info.append("<br/>").append("<strong>数量：</strong>").append(" ╳ ").append(amap.get("fcount").toString());
			}
			if (amap.get("fpostage") != null && StringUtils.isNotBlank(amap.get("fpostage").toString())) {
				info.append("<br/>").append("<strong>邮费：</strong>").append(amap.get("fpostage").toString());
			}
			if (amap.get("fchangeAmount") != null && StringUtils.isNotBlank(amap.get("fchangeAmount").toString())) {
				if (amap.get("") != null && StringUtils.isNotBlank(amap.get("fsource").toString())
						&& ((Integer) amap.get("fsource")).intValue() == 20) {
					info.append("<br/>").append("<strong>折扣价格：</strong>").append(amap.get("fchangeAmount").toString());
				} else {
					info.append("<br/>").append("<strong>变更金额：</strong>").append(amap.get("fchangeAmount").toString());
				}
			}
			amap.put("priceInfo", info.toString());

			info.delete(0, info.length());
			if (amap.get("freceivableTotal") != null
					&& StringUtils.isNotBlank(amap.get("freceivableTotal").toString())) {
				info.append("<strong>应收：</strong>").append(amap.get("freceivableTotal").toString()).append("<br/>");
			}
			if (amap.get("ftotal") != null && StringUtils.isNotBlank(amap.get("ftotal").toString())) {
				info.append("<strong>实收：</strong>").append(amap.get("ftotal").toString());
			}
			amap.put("totalInfo", info.toString());
		}
	}

	/**
	 * 将退款订单进行审核
	 * 
	 * @param pushId
	 * @param times
	 */
	public void saveUpdateOrderAudit(String orderId, Integer flag) {

		if (flag == 1) {
			orderDAO.updateOrderStatus(115, orderId);
		} else {
			TOrderStatusChange t = orderStatusChangeDAO.getByOrderId(orderId).get(0);
			orderDAO.updateOrderStatus(t.getFbeforeStatus(), orderId);
			TOrderRefund orderRefund = null;
			orderRefund = orderRefundDAO.findByOrder(orderId);
			if(orderRefund!=null){
				orderRefundDAO.updateOrderStatus(100, orderRefund.getId());
			}
		}

	}

	/**
	 * 将退款订单进行审核
	 * 
	 * @param pushId
	 * @param times
	 */
	public String getInsuredInfo(OrderRecipientDTO recipientDTO) {

		StringBuilder insuredInfo = new StringBuilder();
		int specPerson = recipientDTO.getSpecPerson();
		if (specPerson > 0) {
			insuredInfo.append(recipientDTO.getInsuranceInfo().replace("@", " 【身份证号】").replace("[", "").replace("]", "")
					.replace("\"", ""));
		}
		return insuredInfo.toString();

	}

	public void test() {
		List<TOrder> orderList = orderDAO.findAllByStatus();
		for (TOrder tOrder : orderList) {
			this.csVerification(tOrder.getId(), false);
		}
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getOrderExtList(String orderId) {
		StringBuilder hql = new StringBuilder();

		hql.append(
				"select t.fname as fname, t.fvalue as fvalue from TOrderExtInfo t where t.forderId = :orderId order by t.forder asc");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("orderId", orderId);
		List<Map<String, Object>> List = commonService.find(hql.toString(), hqlMap);
		Date date = null;
		for (Map<String, Object> bmap : List) {
			if (bmap.get("fvalue") != null && StringUtils.isNotBlank(bmap.get("fvalue").toString())) {
				String value = bmap.get("fvalue").toString().replace("&", "   ");
				bmap.put("fvalue", value);
			}
		}
		return List;
	}

	/**
	 * 客服回复订单锁定的的方法
	 * 
	 * @param orderId
	 */
	public void orderReplay(String orderId, String replay) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder remark = new StringBuilder();
		remark.append("＜－－－－－－－－－－ ").append(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm"))
				.append(" －－－－－－－－－－ ").append(StringUtils.SPACE).append(StringUtils.SPACE).append("填写人：")
				.append(shiroUser.getName()).append(" －－－－－－－－－－＞").append(StringUtils.LF).append(replay);
		// orderDAO.appendCsRemark(orderId, remark.toString());
		TOrder order = orderDAO.getOne(orderId);
		if (StringUtils.isNotBlank(order.getFcsRemark())) {
			remark.insert(0, new StringBuilder().append(order.getFcsRemark()).append(StringUtils.LF));
		}
		order.setFcsRemark(remark.toString());
		order.setFreply(replay);
		order.setFreplyTime(new Date());
		orderDAO.save(order);
		// 发送微信push
		wxService.pushOrderReplayTemplate(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm"), shiroUser.getName(),
				order, replay);
	}

	public List<TOrderGoods> getOrderGoods(String id) {
		List<TOrderGoods> list = Lists.newArrayList();
		list = orderGoodsDAO.findByOrderId(id);
		return list;

	}

	public void saveExpress(String orderId, Integer expressCode, String expressName, String expressFrom,
			String expressTo) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		this.orderStatusChange(4, shiroUser.getName(), orderId, null, 20, 30);
		orderDAO.updateOrderStatus(30, orderId);
		TOrder tOrder = orderDAO.findOne(orderId);
		TCustomerInfo customerInfo = customerInfoDAO.getByCustomerId(tOrder.getTCustomer().getId());
		TExpress express = new TExpress();
		express.setFcreateTime(new Date());
		express.setFupdateTime(new Date());
		express.setFexpressNum(expressName);
		express.setFexYpressType(expressCode);
		express.setFstarting(expressFrom);
		express.setFreach(expressTo);
		express.setForderId(orderId);
		express = expressDAO.save(express);
		if (customerInfo != null && customerInfo.getFregisterDeviceTokens() != null) {
			pushService.confirmGoods(tOrder.getForderNum(), expressName, orderId,
					customerInfo.getFregisterDeviceTokens(), tOrder.getTCustomer().getId());
		}
	}

}