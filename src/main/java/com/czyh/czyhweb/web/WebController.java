package com.czyh.czyhweb.web;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;

import com.czyh.czyhweb.dto.ImageUploadDTO;
import com.czyh.czyhweb.service.FxlService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Maps;

@Controller
@RequestMapping("/web")
public class WebController {

	private static final Logger logger = LoggerFactory.getLogger(WebController.class);

	private static JsonMapper mapper = new JsonMapper(Include.ALWAYS);

	@Autowired
	private FxlService fxlService;

	private final static String ueditorConfig = "{\"imageActionName\":\"ueditorImageUpload\",\"imageFieldName\":\"upfile\",\"imageMaxSize\":2048000,\"imageAllowFiles\":[\".png\",\".jpg\",\".jpeg\",\".gif\",\".bmp\"],\"imageCompressEnable\":true,\"imageCompressBorder\":1600,\"imageInsertAlign\":\"none\",\"imageUrlPrefix\":\"\",\"imagePathFormat\":\"\"}";

	/**
	 * webuploader上传图片文件的方法
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/imageUpload", method = RequestMethod.POST)
	public String imageUpload(HttpServletRequest request, Model model, @RequestParam(value = "file") MultipartFile file
	/*
	 * @RequestParam(value = "pathVar") String pathVar, @RequestParam(required =
	 * false) int imageWidth,
	 * 
	 * @RequestParam(required = false) int imageHeight, @RequestParam(required =
	 * false) boolean watermark,
	 * 
	 * @RequestParam(required = false, defaultValue = "true") boolean
	 * mandatoryJpg
	 */) {

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		ImageUploadDTO imageUploadDTO = new ImageUploadDTO();
		// imageUploadDTO.setPathVar(pathVar);
		// imageUploadDTO.setImageWidth(imageWidth);
		// imageUploadDTO.setImageHeight(imageHeight);
		// imageUploadDTO.setWatermark(watermark);
		// imageUploadDTO.setMandatoryJpg(mandatoryJpg);

		imageUploadDTO.setIsThumbnail(1);
		imageUploadDTO.setThumbnailWidth(200);
		imageUploadDTO.setThumbnailHeight(200);
		imageUploadDTO.setFile(file);
		try {
			Long imageId = fxlService.imageUpload(imageUploadDTO);
			returnMap.put("imageId", imageId);
			returnMap.put("success", true);
			returnMap.put("msg", "图片上传保存成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "图片上传保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/ueditor/config", method = RequestMethod.GET)
	public String ueditor(HttpServletRequest request, HttpServletResponse response) {
		return ueditorConfig;
	}

	/**
	 * ueditor上传图片文件的方法
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/ueditor/ueditorImageUpload", method = RequestMethod.POST)
	public String ueditorImageUpload(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "upfile") MultipartFile file) {

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = null;
		// {"original":"demo.jpg","name":"demo.jpg","url":"/server/ueditor/upload/image/demo.jpg","size":"99697","type":".jpg","state":"SUCCESS"}
		try {
			returnMap = fxlService.ueditorImageUpload(file);
			returnMap.put("state", "SUCCESS");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap = Maps.newHashMap();
			returnMap.put("state", "FAIL");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 公共上传图片接口
	 * 
	 * @param request
	 * @param response
	 * @param file
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/publicImageUpload", method = RequestMethod.POST)
	public String addUserFile(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "file") MultipartFile file) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// MultipartFile 转换 File类型
			CommonsMultipartFile cf = (CommonsMultipartFile) file;
			DiskFileItem fi = (DiskFileItem) cf.getFileItem();
			File f = fi.getStoreLocation();

			fxlService.publicImageUpload(f);
			// Long imageId = fxlService.imageUpload(imageUploadDTO);
			// returnMap.put("imageId", imageId);
			returnMap.put("success", true);
			returnMap.put("msg", "上传图片到服务器成功!");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "上传图片到服务器失败!");
		}

		return mapper.toJson(returnMap);
	}

	/**
	 * webuploader上传图片文件的方法
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/imageUploadDG", method = RequestMethod.POST)
	public String imageUploadDG(HttpServletRequest request, Model model,
			@RequestParam(value = "file") MultipartFile file, @RequestParam(value = "pathVar") String pathVar,
			@RequestParam(required = false) int imageWidth, @RequestParam(required = false) int imageHeight,
			@RequestParam(required = false) boolean watermark,
			@RequestParam(required = false, defaultValue = "true") boolean mandatoryJpg) {

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		ImageUploadDTO imageUploadDTO = new ImageUploadDTO();
		imageUploadDTO.setPathVar(pathVar);
		imageUploadDTO.setImageWidth(imageWidth);
		imageUploadDTO.setImageHeight(imageHeight);
		imageUploadDTO.setWatermark(watermark);
		imageUploadDTO.setMandatoryJpg(mandatoryJpg);
		imageUploadDTO.setIsThumbnail(1);
		imageUploadDTO.setThumbnailWidth(200);
		imageUploadDTO.setThumbnailHeight(200);
		imageUploadDTO.setFile(file);
		try {
			Long imageId = fxlService.imageUploadDG(imageUploadDTO);
			returnMap.put("imageId", imageId);
			returnMap.put("success", true);
			returnMap.put("msg", "图片上传保存成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "图片上传保存失败！");
		}
		return mapper.toJson(returnMap);
	}

}