package com.github.cuter44.wxh5pay.resps;

import java.io.IOException;
import java.io.InputStream;

public class UnifiedOrderResponse extends WxpayResponseBase {
	public UnifiedOrderResponse(String respXml) {
		super(respXml);

		return;
	}

	public UnifiedOrderResponse(InputStream respXml) throws IOException {
		super(respXml);

		return;
	}
}
