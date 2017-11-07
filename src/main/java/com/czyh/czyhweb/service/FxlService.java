package com.czyh.czyhweb.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;
import org.springside.modules.utils.Identities;

import com.czyh.czyhweb.dao.ImageDAO;
import com.czyh.czyhweb.dto.ImageUploadDTO;
import com.czyh.czyhweb.dto.ResponseDTO;
import com.czyh.czyhweb.entity.TImage;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.FileUtils;
import com.czyh.czyhweb.util.HttpClientUtil;
import com.czyh.czyhweb.util.ImageUtil;
import com.czyh.czyhweb.util.PropertiesUtil;
import com.czyh.czyhweb.util.dingTalk.DingTalkUtil;
import com.czyh.czyhweb.util.publicImage.common.QiniuException;
import com.czyh.czyhweb.util.publicImage.http.Response;
import com.czyh.czyhweb.util.publicImage.storage.UploadManager;
import com.czyh.czyhweb.util.publicImage.util.Auth;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * CZYH核心业务管理类.
 * 
 * @author jinshengzhi
 */
@Component
@Transactional
public class FxlService {

	private static final Logger logger = LoggerFactory.getLogger(FxlService.class);

	// private static JsonMapper mapper = new JsonMapper(Include.ALWAYS);
	private static final String imageMogr2 = "imageMogr2";// 图片高级处理

	private static final String thumbnail = "thumbnail";// 图片高级处理

	private static JsonMapper mapper = new JsonMapper(Include.ALWAYS);

	@Autowired
	private ImageDAO imageDAO;

	@Autowired
	private CacheManager cacheManager;

	/**
	 * 保存上传的图片文件保存到服务器，并返回图片文件描述的ImageJsonBean对象
	 * 
	 * @param valueMap
	 */
	public Long imageUpload(ImageUploadDTO imageUploadDTO) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// String filePathVar =
		// PropertiesUtil.getProperty(imageUploadDTO.getPathVar());
		// MultipartFile file = imageUploadDTO.getFile();
		// // 定义相对路径，并且将日期作为分隔子目录
		// String relativePath = new
		// StringBuilder(filePathVar).append(DateFormatUtils.format(new Date(),
		// "yyyy-MM-dd"))
		// .append("/").toString();
		// // 定义全路径，为了以后将相对路径和文件名添加进去
		// StringBuilder rootPath = new StringBuilder(Constant.RootPath)
		// .append(PropertiesUtil.getProperty("imageRootPath")).append(relativePath);
		// File tempFileA = null;
		// File tempFileB = null;
		//
		// TImage tImage = new TImage();
		// try {
		// // 获取上传文件名
		// String fileName = file.getOriginalFilename();
		// // 获取上传文件扩展名
		// String fileExt = FileUtils.getFileExt(fileName);
		// // 保持原文件名到bean中
		// tImage.setFileName(fileName);
		// // 先创建保存文件的目录
		// File destDir = new File(rootPath.toString());
		// if (!destDir.exists()) {
		// destDir.mkdirs();
		// }
		// // 定义最终目标文件对象
		// File destFile = null;
		// // 定义存储文件名
		// String storeFileName = Identities.uuid2();
		// // 如果不是JPG文件的话，则压缩成同尺寸的JPG文件到目标文件对象
		// if (imageUploadDTO.isMandatoryJpg() &&
		// !fileExt.equalsIgnoreCase("jpg")) {
		// tempFileA = new File(System.getProperty("java.io.tmpdir"),
		// storeFileName + "." + fileExt);
		// file.transferTo(tempFileA);
		// tempFileB = new File(System.getProperty("java.io.tmpdir"),
		// storeFileName + ".jpg");
		// ImageUtil.toJpg(tempFileA, tempFileB);
		// tImage.setStorefileExt("jpg");
		// } else {
		// tempFileB = new File(System.getProperty("java.io.tmpdir"),
		// storeFileName + "." + fileExt);
		// file.transferTo(tempFileB);
		// tImage.setStorefileExt(fileExt);
		// }
		// destFile = new File(new
		// StringBuilder(rootPath).append(storeFileName).append(".")
		// .append(tImage.getStorefileExt()).toString());
		//
		// if (imageUploadDTO.isWatermark()) {
		// ImageUtil.thumbnailAndWatermark(tempFileB, destFile,
		// imageUploadDTO.getImageWidth(),
		// imageUploadDTO.getImageHeight(), false);
		// } else {
		// ImageUtil.thumbnail(tempFileB, destFile,
		// imageUploadDTO.getImageWidth(),
		// imageUploadDTO.getImageHeight(), false);
		// }
		// // 将存储相对路径、文件大小、存储文件名和上传时间保持到jsonBean中
		// tImage.setImageWidth(imageUploadDTO.getImageWidth());
		// tImage.setImageHeight(imageUploadDTO.getImageHeight());
		// tImage.setRelativePath(relativePath);
		// tImage.setFileSize(org.apache.commons.io.FileUtils.sizeOf(destFile));
		// tImage.setStoreFileName(storeFileName);
		// tImage.setUploadTime(new Date());
		// tImage.setIsThumbnail(imageUploadDTO.getIsThumbnail());
		// if (imageUploadDTO.getIsThumbnail() == 1) {
		// // 进行缩略图的生成，根据前台传递来的宽高尺寸生成缩略图
		// String thumbnailFileName = new
		// StringBuilder(storeFileName).append(ImageUtil.ThumbnailFileFlag)
		// .append(imageUploadDTO.getThumbnailWidth()).append("_")
		// .append(imageUploadDTO.getThumbnailHeight()).toString();
		// String thumbnailFilePath = new
		// StringBuilder(rootPath).append(thumbnailFileName).append(".")
		// .append(tImage.getStorefileExt()).toString();
		// if (imageUploadDTO.isWatermark()) {
		// ImageUtil.squareAndWatermark(destFile,
		// org.apache.commons.io.FileUtils.getFile(thumbnailFilePath),
		// imageUploadDTO.getThumbnailWidth());
		// } else {
		// ImageUtil.square(destFile,
		// org.apache.commons.io.FileUtils.getFile(thumbnailFilePath),
		// imageUploadDTO.getThumbnailWidth());
		// }
		//
		// tImage.setThumbnailFileName(thumbnailFileName);
		// tImage.setThumbnailWidth(imageUploadDTO.getThumbnailWidth());
		// tImage.setThumbnailHeight(imageUploadDTO.getThumbnailHeight());
		// }
		// tImage.setStatus(1);
		// tImage.setUploaderId(shiroUser.getId());
		// imageDAO.save(tImage);
		// } catch (Exception e) {
		// logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		// throw new ServiceException("保存上传的附件文件时出错！");
		// } finally {
		// if (tempFileA != null && tempFileA.exists()) {
		// tempFileA.delete();
		// }
		// if (tempFileB != null && tempFileB.exists()) {
		// tempFileB.delete();
		// }
		// }
		String res = null;
		TImage tImage = new TImage();

		String path = "eventImage/" + DateFormatUtils.format(new Date(), "yyyy-MM-dd") + "/";
		String name = Identities.uuid2();
		String type = "jpg";
		StringBuilder urlPath = new StringBuilder(path).append(name).append(".").append(type);

		MultipartFile file = imageUploadDTO.getFile();
		String fileName = file.getOriginalFilename();// 获取图片名称
		// String fileExt = FileUtils.getFileExt(fileName);//获取图片扩展名
		// MultipartFile 转换 File类型
		CommonsMultipartFile cf = (CommonsMultipartFile) file;
		DiskFileItem fi = (DiskFileItem) cf.getFileItem();
		File f = fi.getStoreLocation();
		try {
			String token = getQnToken();
			res = appuploadQn(f, urlPath.toString(), token);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			logger.info(res);
		}

		tImage.setFileName(fileName);// 图片名称
		tImage.setStoreFileName(name);// 物理扩展名
		tImage.setStorefileExt(type);// 获取图片扩展名
		tImage.setRelativePath(path);// 存储路径
		tImage.setFileSize(org.apache.commons.io.FileUtils.sizeOf(f));
		tImage.setImageWidth(imageUploadDTO.getImageWidth());
		tImage.setImageHeight(imageUploadDTO.getImageHeight());
		tImage.setUploadTime(new Date());
		tImage.setIsThumbnail(imageUploadDTO.getIsThumbnail());
		tImage.setStatus(1);
		tImage.setUploaderId(shiroUser.getId());
		tImage.setFlag(2);// 以后的图片上传默认值都为2 1.为老版本图片2.为新版本图片
		imageDAO.save(tImage);

		return tImage.getId();
	}

	/**
	 * ueditor编辑器图片上传的方法，保存到文件服务器上之后返回指定格式的图片文件描述的Map对象
	 * 
	 * @param file
	 * @return
	 */
	public Map<String, Object> ueditorImageUpload(MultipartFile file) {
		// 返回页面的json格式
		//
		// {"original":"demo.jpg","name":"demo.jpg","url":"/server/ueditor/upload/image/demo.jpg","size":"99697","type":".jpg","state":"SUCCESS"}
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String createDate = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
		// 文件保存路径，并且将日期作为分隔子目录
		String allPath = new StringBuilder(Constant.RootPath).append(PropertiesUtil.getProperty("imageRootPath"))
				.append(PropertiesUtil.getProperty("umeditorPath")).append(createDate).append("/").toString();
		StringBuilder httpUrl = new StringBuilder(PropertiesUtil.getProperty("fileServerUrl"))
				.append(PropertiesUtil.getProperty("imageRootPath")).append(PropertiesUtil.getProperty("umeditorPath"))
				.append(createDate).append("/");

		File tempFile = null;
		File tempFile2 = null;
		try {
			// 获取上传文件名
			String fileName = file.getOriginalFilename();
			// 获取上传文件扩展名
			String fileExt = FileUtils.getFileExt(fileName);
			// 保持原文件名到bean中
			returnMap.put("originalName", fileName);

			// 先创建保存文件的目录
			File destDir = new File(allPath);
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
			// 定义最终目标文件对象
			File destFile = null;

			// 定义存储文件名
			String storeFileName = Identities.uuid2();
			// 如果不是JPG文件的话，则压缩成同尺寸的JPG文件到目标文件对象
			if (!fileExt.equalsIgnoreCase("jpg")) {
				tempFile = new File(System.getProperty("java.io.tmpdir"), storeFileName + "." + fileExt);
				file.transferTo(tempFile);
				tempFile2 = new File(System.getProperty("java.io.tmpdir"), storeFileName + ".jpg");

				ImageUtil.toJpg(tempFile, tempFile2);
				returnMap.put("type", ".jpg");
				returnMap.put("name", storeFileName + ".jpg");
				returnMap.put("url", httpUrl.append(storeFileName).append(".jpg"));

				destFile = new File(new StringBuilder(allPath).append(storeFileName).append(".jpg").toString());
			} else {
				tempFile2 = new File(System.getProperty("java.io.tmpdir"), storeFileName + "." + fileExt);

				file.transferTo(tempFile2);
				returnMap.put("type", "." + fileExt);
				returnMap.put("name", storeFileName + "." + fileExt);
				returnMap.put("url", httpUrl.append(storeFileName).append(".").append(fileExt));

				destFile = new File(
						new StringBuilder(allPath).append(storeFileName).append(".").append(fileExt).toString());
			}
			//这个工具类可以进行加水印
			ImageUtil.thumbnail(tempFile2, destFile,
					Integer.valueOf(PropertiesUtil.getProperty("umeditorImageWidth")), Integer.MAX_VALUE, true);
			returnMap.put("size", org.apache.commons.io.FileUtils.sizeOf(destFile));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("保存上传的附件文件时出错！");
		} finally {
			if (tempFile != null && tempFile.exists()) {
				tempFile.delete();
			}
			if (tempFile2 != null && tempFile2.exists()) {
				tempFile2.delete();
			}
		}
		// String res = null;
		// Map<String, Object> returnMap;
		// try {
		// returnMap = new HashMap<String, Object>();
		// // MultipartFile 转换 File类型
		// CommonsMultipartFile cf = (CommonsMultipartFile) file;
		// DiskFileItem fi = (DiskFileItem) cf.getFileItem();
		// File f = fi.getStoreLocation();
		// res = publicImageUpload(f);
		// } catch (Exception e) {
		// logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		// throw new ServiceException("保存上传的附件文件时出错！");
		// }

		// returnMap.put("url", getThumbnailview(res, 572) + getWatermark(2));
		// returnMap.put("url", getThumbnailview(res, 572));
		return returnMap;
	}

	/**
	 * 保存移动端上传的头像图片保存起来，并且返回图片的相对路径
	 * 
	 * @param pathVar
	 * @param file
	 *            Base64格式的图片文件字符串
	 * @return
	 */
	public String saveBase64Image(String pathVar, String file) {
		String filePathVar = PropertiesUtil.getProperty(pathVar);// personalCustomerLogoPath
		// 定义相对路径，并且将日期作为分隔子目录
		StringBuilder relativePath = new StringBuilder(filePathVar)
				.append(DateFormatUtils.format(new Date(), "yyyy-MM-dd")).append("/");
		// 定义全路径，为了以后将相对路径和文件名添加进去
		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("imageRootPath")).append(relativePath);

		File tempFile = null;
		try {
			String[] headAndBody = StringUtils.split(file, ",");
			// String head = headAndBody[0];
			String body = headAndBody[1];

			// 先创建保存文件的目录
			File destDir = new File(rootPath.toString());
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
			// 定义最终目标文件对象
			File destFile = null;
			// 定义存储文件名
			String storeFileName = Identities.uuid2() + ".jpg";
			tempFile = new File(System.getProperty("java.io.tmpdir"), storeFileName);
			// 将Base64字符串图片内容转换成byte数组
			byte[] fileByte = Base64Utils.decodeFromString(body);

			// 将byte数组写入临时文件中
			org.apache.commons.io.FileUtils.writeByteArrayToFile(tempFile, fileByte, false);
			// 目标文件
			destFile = new File(rootPath.append(storeFileName).toString());
			// 将临时文件剪裁成为200高宽的正方图保存到目标文件中
			ImageUtil.square(tempFile, destFile, 200);
			// 相对路径加上保存文件名得到图片文件的相对全路径
			relativePath.append(storeFileName);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("保存上传的附件文件时出错！");
		} finally {
			if (tempFile != null && tempFile.exists()) {
				tempFile.delete();
			}
		}
		return relativePath.toString();
	}

	/**
	 * 保存移动端上传的头像图片保存起来，并且返回图片的相对路径
	 * 
	 * @param pathVar
	 * @param file
	 *            Base64格式的图片文件字符串
	 * @return
	 */
	public String saveFileImage(String pathVar, MultipartFile file) {
		String filePathVar = PropertiesUtil.getProperty(pathVar);// personalCustomerLogoPath
		// 定义相对路径，并且将日期作为分隔子目录
		StringBuilder relativePath = new StringBuilder(filePathVar)
				.append(DateFormatUtils.format(new Date(), "yyyy-MM-dd")).append("/");
		// 定义全路径，为了以后将相对路径和文件名添加进去
		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("imageRootPath")).append(relativePath);

		File tempFile = null;
		try {

			// 先创建保存文件的目录
			File destDir = new File(rootPath.toString());
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
			// 定义最终目标文件对象
			File destFile = null;
			// 定义存储文件名
			String storeFileName = Identities.uuid2() + ".jpg";
			tempFile = new File(System.getProperty("java.io.tmpdir"), storeFileName);
			file.transferTo(tempFile);
			// 目标文件
			destFile = new File(rootPath.append(storeFileName).toString());
			// 将临时文件剪裁成为200高宽的正方图保存到目标文件中
			ImageUtil.square(tempFile, destFile, 200);
			// 相对路径加上保存文件名得到图片文件的相对全路径
			relativePath.append(storeFileName);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("保存上传的附件文件时出错！");
		} finally {
			if (tempFile != null && tempFile.exists()) {
				tempFile.delete();
			}
		}
		return relativePath.toString();
	}

	@Transactional(readOnly = true)
	public String getImageUrl(String id, boolean isThumbnail) {
		String res = null;
		if (StringUtils.isBlank(id)) {
			return StringUtils.EMPTY;
		}
		TImage tImage = imageDAO.getOne(Long.valueOf(id));
		try {
			if (tImage.getFlag() == 1) {// 返回老版本图片服务器图片路径
				StringBuilder imageUrl = new StringBuilder(PropertiesUtil.getProperty("fileServerUrl"))
						.append(PropertiesUtil.getProperty("imageRootPath")).append(tImage.getRelativePath());
				if (isThumbnail && tImage.getIsThumbnail().equals(1)) {
					imageUrl.append(tImage.getThumbnailFileName());
				} else {
					imageUrl.append(tImage.getStoreFileName());
				}
				imageUrl.append(".").append(tImage.getStorefileExt());
				res = imageUrl.toString();
			} else if (tImage.getFlag() == 2) {// 返回新版本七牛图片服务器路径
				StringBuilder imageUrl = new StringBuilder(PropertiesUtil.getProperty("publicQnEventService"))
						.append(tImage.getRelativePath()).append(tImage.getStoreFileName()).append(".")
						.append(tImage.getStorefileExt());
				// 返回 图片尺寸可调整 现在为100p
				res = getThumbnailview(imageUrl.toString(), 100);
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			if (isThumbnail) {
				res = new StringBuilder(PropertiesUtil.getProperty("fileServerUrl"))
						.append(PropertiesUtil.getProperty("imageRootPath")).append("/czyhweb/noPicThumbnail.jpg")
						.toString();
			} else {
				res = new StringBuilder(PropertiesUtil.getProperty("fileServerUrl"))
						.append(PropertiesUtil.getProperty("imageRootPath")).append("/czyhweb/noPic.jpg").toString();
			}

		}

		return res;
	}

	@Transactional(readOnly = true)
	public String[] getImageUrls(String ids, boolean isThumbnail) {
		if (StringUtils.isBlank(ids)) {
			return ArrayUtils.EMPTY_STRING_ARRAY;
		}
		String[] urls = ids.split(";");
		List<Long> idList = Lists.newArrayList();
		for (String string : urls) {
			if (StringUtils.isNotBlank(string)) {
				idList.add(Long.valueOf(string));
			}
		}
		List<TImage> imageList = imageDAO.findByIdIn(idList);

		String rootUrl = PropertiesUtil.getProperty("fileServerUrl") + PropertiesUtil.getProperty("imageRootPath");
		String[] imageUrls = ArrayUtils.EMPTY_STRING_ARRAY;

		StringBuilder imageUrl = new StringBuilder();
		try {
			for (TImage tImage : imageList) {

				if (tImage.getFlag() == 1) {
					imageUrl.delete(0, imageUrl.length());
					imageUrl.append(rootUrl).append(tImage.getRelativePath());

					if (isThumbnail && tImage.getIsThumbnail().equals(1)) {
						imageUrl.append(tImage.getThumbnailFileName());
					} else {
						imageUrl.append(tImage.getStoreFileName());
					}
					imageUrl.append(".").append(tImage.getStorefileExt());

					imageUrls = ArrayUtils.add(imageUrls, imageUrl.toString());
				} else if (tImage.getFlag() == 2) {
					imageUrl.delete(0, imageUrl.length());
					imageUrl.append(PropertiesUtil.getProperty("publicQnEventService")).append(tImage.getRelativePath())
							.append(tImage.getStoreFileName()).append(".").append(tImage.getStorefileExt());

					// 返回 图片尺寸可调整 现在为100p
					imageUrls = ArrayUtils.add(imageUrls, getThumbnailview(imageUrl.toString(), 100));

				}
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			if (isThumbnail) {
				imageUrls = ArrayUtils.add(imageUrls,
						new StringBuilder(PropertiesUtil.getProperty("fileServerUrl"))
								.append(PropertiesUtil.getProperty("imageRootPath"))
								.append("/czyhweb/noPicThumbnail.jpg").toString());
			} else {
				imageUrls = ArrayUtils.add(imageUrls, new StringBuilder(PropertiesUtil.getProperty("fileServerUrl"))
						.append(PropertiesUtil.getProperty("imageRootPath")).append("/czyhweb/noPic.jpg").toString());
			}
		}
		return imageUrls;
	}

	@Transactional(readOnly = true)
	public TImage getImage(Long id) {
		return imageDAO.findOne(id);
	}

	/**
	 * 图片上传公共方法
	 * 
	 * @param id
	 * @return
	 */
	public String publicImageUpload(File file) {

		String res = null;
		String path = "eventImage/" + DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm") + "/";
		String name = Identities.uuid2();
		String type = ".jpg";

		StringBuilder urlPath = new StringBuilder(path).append(name).append(type);
		try {
			String token = getQnToken();
			res = appuploadQn(file, urlPath.toString(), token);
		} catch (IOException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			logger.info(res);
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("保存上传的附件文件时出错！");
		}
		return PropertiesUtil.getProperty("publicQnEventService") + urlPath.toString();
	}

	/**
	 * 上传图片七牛方法
	 * 
	 * @param FilePath
	 * @param name
	 * @param token
	 * @return
	 * @throws IOException
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String appuploadQn(File FilePath, String name, String token) throws IOException {
		Response res = null;

		try {
			UploadManager uploadManager = new UploadManager();
			res = uploadManager.put(FilePath, name, token);
			// System.out.println(res.bodyString());
		} catch (QiniuException e) {
			Response r = e.response;
			logger.error(r.bodyString());
			// System.out.println(res.bodyString());
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("保存上传的附件文件时出错！");

		}
		return res.bodyString();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String getQnToken() {

		String qtoken = "QntokenService";
		String res = null;

		// 获取缓存中的key
		Cache customerEentityCache = cacheManager.getCache(Constant.QnUserUploadToken);
		// 获取缓存中的token
		Element elementB = customerEentityCache.get(qtoken);
		if (elementB == null) {
			// token默认失效时间为86400 为24小时
			Auth auth = Auth.create(PropertiesUtil.getProperty("qiniuAk"), PropertiesUtil.getProperty("qiniuSK"));
			String token = auth.uploadToken(PropertiesUtil.getProperty("bucket"));
			// System.out.println(token);
			elementB = new Element(qtoken, token);
			customerEentityCache.put(elementB);
			res = token;
		} else {
			res = (String) elementB.getObjectValue();
			// System.out.println(res+"----ss");
		}
		return res;
	}

	/**
	 * 指定新宽度为？px
	 * 
	 * @return
	 */
	@Transactional(readOnly = true)
	public String getThumbnailview(String urlPath, Integer proportion) {

		StringBuffer sb = new StringBuffer();
		sb.append(urlPath);
		sb.append("?");
		sb.append(imageMogr2).append("/");
		// sb.append(thumbnail).append("/").append("572x350");
		sb.append(proportion).append("x");

		return sb.toString();
	}

	/**
	 * bg sm 水印处理方法 flag 1.sm水印 2.Bg水印
	 * 
	 * @return
	 */
	@Transactional(readOnly = true)
	public String getWatermark(Integer flag) {

		if (flag == 1) {
			StringBuffer sb = new StringBuffer();
			sb.append("|");
			sb.append(PropertiesUtil.getProperty("qiniuWatermarkSm"));
			return sb.toString();
		} else {
			StringBuffer sb = new StringBuffer();
			sb.append("|");
			sb.append(PropertiesUtil.getProperty("qiniuWatermarkBg"));
			return sb.toString();
		}
	}

	/**
	 * 保存上传的图片文件保存到服务器，并返回图片文件描述的ImageJsonBean对象
	 * 
	 * @param valueMap
	 */
	public Long imageUploadDG(ImageUploadDTO imageUploadDTO) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		String filePathVar = PropertiesUtil.getProperty(imageUploadDTO.getPathVar());
		MultipartFile file = imageUploadDTO.getFile();
		// 定义相对路径，并且将日期作为分隔子目录
		String relativePath = new StringBuilder(filePathVar).append(DateFormatUtils.format(new Date(), "yyyy-MM-dd"))
				.append("/").toString();
		// 定义全路径，为了以后将相对路径和文件名添加进去
		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("imageRootPath")).append(relativePath);
		File tempFileA = null;
		File tempFileB = null;

		TImage tImage = new TImage();
		try {
			// 获取上传文件名
			String fileName = file.getOriginalFilename();
			// 获取上传文件扩展名
			String fileExt = FileUtils.getFileExt(fileName);
			// 保持原文件名到bean中
			tImage.setFileName(fileName);
			// 先创建保存文件的目录
			File destDir = null;
			// 如果是读写网络路径，并且是windows服务器时，多处理一下
			if (rootPath.indexOf("//") == 0 && SystemUtils.IS_OS_WINDOWS) {
				File cs = new File(rootPath.toString());
				destDir = new File(cs.getAbsolutePath());
			} else {
				destDir = new File(rootPath.toString());
			}
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
			// 定义最终目标文件对象
			File destFile = null;
			// 定义存储文件名
			String storeFileName = Identities.uuid2();
			// 如果不是JPG文件的话，则压缩成同尺寸的JPG文件到目标文件对象
			if (imageUploadDTO.isMandatoryJpg() && !fileExt.equalsIgnoreCase("jpg")) {
				tempFileA = new File(System.getProperty("java.io.tmpdir"), storeFileName + "." + fileExt);
				file.transferTo(tempFileA);
				tempFileB = new File(System.getProperty("java.io.tmpdir"), storeFileName + ".jpg");
				ImageUtil.toJpg(tempFileA, tempFileB);
				tImage.setStorefileExt("jpg");
			} else {
				tempFileB = new File(System.getProperty("java.io.tmpdir"), storeFileName + "." + fileExt);
				file.transferTo(tempFileB);
				tImage.setStorefileExt(fileExt);
			}
			destFile = new File(new StringBuilder(rootPath).append(storeFileName).append(".")
					.append(tImage.getStorefileExt()).toString());

			if (imageUploadDTO.isWatermark()) {
				ImageUtil.thumbnail(tempFileB, destFile, imageUploadDTO.getImageWidth(),
						imageUploadDTO.getImageHeight(), false);
			} else {
				ImageUtil.thumbnail(tempFileB, destFile, imageUploadDTO.getImageWidth(),
						imageUploadDTO.getImageHeight(), false);
			}
			// 将存储相对路径、文件大小、存储文件名和上传时间保持到jsonBean中
			tImage.setImageWidth(imageUploadDTO.getImageWidth());
			tImage.setImageHeight(imageUploadDTO.getImageHeight());
			tImage.setRelativePath(relativePath);
			tImage.setFileSize(org.apache.commons.io.FileUtils.sizeOf(destFile));
			tImage.setStoreFileName(storeFileName);
			tImage.setUploadTime(new Date());
			tImage.setIsThumbnail(imageUploadDTO.getIsThumbnail());
			tImage.setFlag(1);// 1.老版本图片上传
			if (imageUploadDTO.getIsThumbnail() == 1) {
				// 进行缩略图的生成，根据前台传递来的宽高尺寸生成缩略图
				String thumbnailFileName = new StringBuilder(storeFileName).append(ImageUtil.ThumbnailFileFlag)
						.append(imageUploadDTO.getThumbnailWidth()).append("_")
						.append(imageUploadDTO.getThumbnailHeight()).toString();
				String thumbnailFilePath = new StringBuilder(rootPath).append(thumbnailFileName).append(".")
						.append(tImage.getStorefileExt()).toString();
				if (imageUploadDTO.isWatermark()) {
					ImageUtil.square(destFile, org.apache.commons.io.FileUtils.getFile(thumbnailFilePath),
							imageUploadDTO.getThumbnailWidth());
				} else {
					ImageUtil.square(destFile, org.apache.commons.io.FileUtils.getFile(thumbnailFilePath),
							imageUploadDTO.getThumbnailWidth());
				}

				tImage.setThumbnailFileName(thumbnailFileName);
				tImage.setThumbnailWidth(imageUploadDTO.getThumbnailWidth());
				tImage.setThumbnailHeight(imageUploadDTO.getThumbnailHeight());
			}
			tImage.setStatus(1);
			tImage.setUploaderId(shiroUser.getId());
			imageDAO.save(tImage);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("保存上传的附件文件时出错！");
		} finally {
			if (tempFileA != null && tempFileA.exists()) {
				tempFileA.delete();
			}
			if (tempFileB != null && tempFileB.exists()) {
				tempFileB.delete();
			}
		}
		return tImage.getId();
	}

	@Transactional(readOnly = true)
	public void callInterface() {
		StringBuilder czyhServiceUrl = new StringBuilder();
		czyhServiceUrl.append(PropertiesUtil.getProperty("czyhServiceUrl"));
		czyhServiceUrl.append("/api/system/callInterface");
		String returnString = "";
		try {
			returnString = HttpClientUtil.callUrlGet(czyhServiceUrl.toString(), Maps.newHashMap());
			ResponseDTO responseDTO = mapper.fromJson(returnString, ResponseDTO.class);
			if (responseDTO.getStatusCode() != 0) {
				// 发送钉钉
				this.sendDingDing();
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			// 发送钉钉
			this.sendDingDing();
		}
	}

	public void sendDingDing() {
		DingTalkUtil.sendDingTalk("服务器发生异常，相关人员麻溜的搞事情", DingTalkUtil.getGoDieDingTalk());
	}

}