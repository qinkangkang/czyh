package com.czyh.czyhweb.service.system;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.utils.Identities;

import com.czyh.czyhweb.dao.GoodsSpaceValueDAO;
import com.czyh.czyhweb.entity.TGoodsSpaceValue;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.PropertiesUtil;
import com.czyh.czyhweb.util.BufferedImage.DrawTableImg;
import com.google.common.collect.Lists;

/**
 * 系统资源类
 * 
 * @author jinsey
 *
 */
@Component
@Transactional
public class SystemService {

	private static final Logger logger = LoggerFactory.getLogger(SystemService.class);

	@Autowired
	private GoodsSpaceValueDAO goodsSpaceValueDAO;

	@Transactional(readOnly = true)
	public String getGoodsSpecValue(String goodsId) {

		List<TGoodsSpaceValue> goodsSpaceValueList = goodsSpaceValueDAO.findGoodsList(goodsId);

		List<String[]> list = Lists.newArrayList();

		StringBuilder httpUrl = null;

		String[][] bigarr = null;
		String[] arr = null;
		for (TGoodsSpaceValue tGoodsSpaceValue : goodsSpaceValueList) {
			String fspaceName = tGoodsSpaceValue.getFspaceName();
			String fvalueName = tGoodsSpaceValue.getFvalueName();
			arr = new String[2];
			arr[0] = fspaceName;
			arr[1] = fvalueName;
			list.add(arr);
		}
		bigarr = new String[goodsSpaceValueList.size()][];
		for (int i = 0; i < bigarr.length; i++) {
			bigarr[i] = list.get(i);
		}

		// 分配图片上传路径
		String filePathVar = PropertiesUtil.getProperty("goodsSkuPath");
		StringBuilder relativePath = new StringBuilder(filePathVar)
				.append(DateFormatUtils.format(new Date(), "yyyy-MM-dd")).append("/");
		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("imageRootPath")).append(relativePath);

		// 判断如果没有该目录则创建一个目录
		File destDir = new File(rootPath.toString());
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		String storeFileName;
		if (StringUtils.isBlank(goodsId)) {
			storeFileName = goodsId + ".png";
		} else {
			storeFileName = Identities.uuid2() + ".png";
		}

		relativePath.append(storeFileName);

		// 调用画图工具类
		DrawTableImg.myGraphicsGeneration(bigarr, rootPath.append(storeFileName).toString());

		httpUrl = new StringBuilder(PropertiesUtil.getProperty("fileServerUrl"))
				.append(PropertiesUtil.getProperty("imageRootPath")).append(relativePath.toString());

		return httpUrl.toString();
	}

}