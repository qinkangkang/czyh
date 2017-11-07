package com.czyh.czyhweb.util;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.czyh.czyhweb.service.FxlService;
import com.czyh.czyhweb.service.coupon.CouponService;
import com.czyh.czyhweb.service.finance.SettlementService;
import com.czyh.czyhweb.service.goods.GoodsService;
import com.czyh.czyhweb.service.system.OperatingService;

/**
 * 被Spring的Quartz MethodInvokingJobDetailFactoryBean定时执行的普通Spring Bean.
 */
public class FlxQuartzJob {

	private static Logger logger = LoggerFactory.getLogger(FlxQuartzJob.class);

	@Autowired
	private GoodsService goodsService;

	@Autowired
	private FxlService fxlService;

	@Autowired
	private OperatingService operatingService;

	@Autowired
	private SettlementService settlementService;

	@Autowired
	private CouponService couponService;

	public void clearTempEvent() {
		int i = goodsService.clearTempEvent();
		logger.warn("清理了" + i + "个临时性活动记录！");
		i = goodsService.clearTempImage();
		logger.warn("清理了" + i + "个临时性图片记录！");
	}

	public void autoSettlement() {
		Date now = new Date();
		settlementService.autoSettlement1(now);
	}

	public void updateCouponStatus() {
		couponService.updateCouponStatus();
		logger.warn("将过期优惠券迁移到历史表中");
	}

	public void callInterface() {
		fxlService.callInterface();
	}

	public void sendDingdingForConsult() {
		operatingService.sendDingdingForConsult();
	}

}
