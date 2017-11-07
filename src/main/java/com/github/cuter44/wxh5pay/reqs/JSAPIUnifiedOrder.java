package com.github.cuter44.wxh5pay.reqs;

import java.util.Properties;

import com.github.cuter44.wxh5pay.constants.TradeType;

public class JSAPIUnifiedOrder extends UnifiedOrder {
	// COSTRUCT
	public JSAPIUnifiedOrder(Properties prop) {
		super(prop);

		super.setTradeType(TradeType.JSAPI);

		return;
	}
}
