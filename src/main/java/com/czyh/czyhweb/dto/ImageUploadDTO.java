package com.czyh.czyhweb.dto;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

public class ImageUploadDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String pathVar = null;

	private boolean watermark = true;

	private boolean mandatoryJpg = true;

	private int imageWidth = 0;

	private int imageHeight = 0;

	private int isThumbnail = 0;

	private int thumbnailWidth = 0;

	private int thumbnailHeight = 0;

	private MultipartFile file = null;

	public String getPathVar() {
		return pathVar;
	}

	public void setPathVar(String pathVar) {
		this.pathVar = pathVar;
	}

	public int getIsThumbnail() {
		return isThumbnail;
	}

	public void setIsThumbnail(int isThumbnail) {
		this.isThumbnail = isThumbnail;
	}

	public int getThumbnailWidth() {
		return thumbnailWidth;
	}

	public void setThumbnailWidth(int thumbnailWidth) {
		this.thumbnailWidth = thumbnailWidth;
	}

	public int getThumbnailHeight() {
		return thumbnailHeight;
	}

	public void setThumbnailHeight(int thumbnailHeight) {
		this.thumbnailHeight = thumbnailHeight;
	}

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}

	public boolean isWatermark() {
		return watermark;
	}

	public void setWatermark(boolean watermark) {
		this.watermark = watermark;
	}

	public boolean isMandatoryJpg() {
		return mandatoryJpg;
	}

	public void setMandatoryJpg(boolean mandatoryJpg) {
		this.mandatoryJpg = mandatoryJpg;
	}

}