package com.czyh.czyhweb.util.excel.redExcel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.utils.Exceptions;

import com.czyh.czyhweb.dao.CustomerTagDAO;
import com.czyh.czyhweb.entity.TCustomerTag;

public class ReadExcel {
	
	private static Logger logger = LoggerFactory.getLogger(ReadExcel.class);
	
	@Autowired
	private CustomerTagDAO customerTagDAO;
	
	/**
	 * 对外提供读取excel 的方法
	 * */
	public static List<List<Object>> readExcel(File file,String fileExt) throws IOException {
		
		if ("xls".equals(fileExt)) {
			return read2003Excel(file);
		} else if ("xlsx".equals(fileExt)) {
			return read2007Excel(file);
		} else {
			throw new IOException("不支持的文件类型");
		}
	}

	/**
	 * 读取 office 2003 excel
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static List<List<Object>> read2003Excel(File file) throws IOException {
		List<List<Object>> list = new LinkedList<List<Object>>();
		HSSFWorkbook hwb = new HSSFWorkbook(new FileInputStream(file));
		HSSFSheet sheet = hwb.getSheetAt(0);
		Object value = null;
		HSSFRow row = null;
		HSSFCell cell = null;
		int counter = 0;
		for (int i = sheet.getFirstRowNum(); counter < sheet
				.getPhysicalNumberOfRows(); i++) {
			row = sheet.getRow(i);
			if (row == null) {
				continue;
			} else {
				counter++;
			}
			List<Object> linked = new LinkedList<Object>();
			for (int j = row.getFirstCellNum(); j <= row.getLastCellNum(); j++) {
				cell = row.getCell(j);
				if (cell == null) {
					continue;
				}
				DecimalFormat df = new DecimalFormat("0");// 格式化 number String
															// 字符
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");// 格式化日期字符串
				DecimalFormat nf = new DecimalFormat("0");// 格式化数字
				switch (cell.getCellType()) {
				case XSSFCell.CELL_TYPE_STRING:
					value = cell.getStringCellValue();
					System.out.println(i + "行" + j + " 列 is String type"
							+ "\tValue:" + value);
					break;
				case XSSFCell.CELL_TYPE_NUMERIC:
					if ("@".equals(cell.getCellStyle().getDataFormatString())) {
						value = df.format(cell.getNumericCellValue());
					} else if ("General".equals(cell.getCellStyle()
							.getDataFormatString())) {
						value = nf.format(cell.getNumericCellValue());
					} else {
						value = sdf.format(HSSFDateUtil.getJavaDate(cell
								.getNumericCellValue()));
					}
					System.out.println(i + "行" + j
							+ " 列 is Number type ; DateFormt:"
							+ cell.getCellStyle().getDataFormatString()
							+ "\tValue:" + value);
					break;
				case XSSFCell.CELL_TYPE_BOOLEAN:
					value = cell.getBooleanCellValue();
					System.out.println(i + "行" + j + " 列 is Boolean type"
							+ "\tValue:" + value);
					break;
				case XSSFCell.CELL_TYPE_BLANK:
					value = "";
					System.out.println(i + "行" + j + " 列 is Blank type"
							+ "\tValue:" + value);
					break;
				default:
					value = cell.toString();
					System.out.println(i + "行" + j + " 列 is default type"
							+ "\tValue:" + value);
				}
				if (value == null || "".equals(value)) {
					continue;
				}
				linked.add(value);
			}
			list.add(linked);
		}
		
		return list;
	}

	/**
	 * 读取Office 2007 excel
	 * */
	private static List<List<Object>> read2007Excel(File file) throws IOException {
		List<List<Object>> list = new LinkedList<List<Object>>();
		// 构造 XSSFWorkbook 对象，strPath 传入文件路径
		XSSFWorkbook xwb = new XSSFWorkbook(new FileInputStream(file));
		// 读取第一章表格内容
		XSSFSheet sheet = xwb.getSheetAt(0);
		Object value = null;
		XSSFRow row = null;
		XSSFCell cell = null;
		int counter = 0;
		for (int i = sheet.getFirstRowNum(); counter < sheet
				.getPhysicalNumberOfRows(); i++) {
			row = sheet.getRow(i);
			if (row == null) {
				continue;
			} else {
				counter++;
			}
			List<Object> linked = new LinkedList<Object>();
			for (int j = row.getFirstCellNum(); j <= row.getLastCellNum(); j++) {
				cell = row.getCell(j);
				if (cell == null) {
					continue;
				}
				DecimalFormat df = new DecimalFormat("0");// 格式化 number String
															// 字符
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");// 格式化日期字符串
				DecimalFormat nf = new DecimalFormat("0");// 格式化数字
				switch (cell.getCellType()) {
				case XSSFCell.CELL_TYPE_STRING:
					System.out.println(i + "行" + j + " 列 is String type");
					value = cell.getStringCellValue();
					break;
				case XSSFCell.CELL_TYPE_NUMERIC:
					System.out.println(i + "行" + j
							+ " 列 is Number type ; DateFormt:"
							+ cell.getCellStyle().getDataFormatString());
					if ("@".equals(cell.getCellStyle().getDataFormatString())) {
						value = df.format(cell.getNumericCellValue());
					} else if ("General".equals(cell.getCellStyle()
							.getDataFormatString())) {
						value = nf.format(cell.getNumericCellValue());
					} else {
						value = sdf.format(HSSFDateUtil.getJavaDate(cell
								.getNumericCellValue()));
					}
					break;
				case XSSFCell.CELL_TYPE_BOOLEAN:
					System.out.println(i + "行" + j + " 列 is Boolean type");
					value = cell.getBooleanCellValue();
					break;
				case XSSFCell.CELL_TYPE_BLANK:
					System.out.println(i + "行" + j + " 列 is Blank type");
					value = "";
					break;
				default:
					System.out.println(i + "行" + j + " 列 is default type");
					value = cell.toString();
				}
				if (value == null || "".equals(value)) {
					continue;
				}
				linked.add(value);
			}
			list.add(linked);
		}
		System.out.println(list);
		return list;
	}
	

	public void ImportExcel(File path) {
		try {
			System.out.println(path+"这是什么");
			String pathurl="D://测试.xls";
			List<TCustomerTag> list = new ArrayList<TCustomerTag>();
			// 创建对Excel工作簿文件的引用­
			HSSFWorkbook wookbook = new HSSFWorkbook(new FileInputStream(pathurl));
			// 在Excel文档中，第一张工作表的缺省索引是0
			// 其语句为：HSSFSheet sheet = workbook.getSheetAt(0);
			HSSFSheet sheet = wookbook.getSheet("Sheet1");
			// 获取到Excel文件中的所有行数­
			int rows = sheet.getPhysicalNumberOfRows();
			// 遍历行­
			for (int i = 1; i < rows; i++) {
				// 读取左上端单元格­
				HSSFRow row = sheet.getRow(i);
				// 行不为空­
				if (row != null) {
					// 获取到Excel文件中的所有的列­
					int cells = row.getPhysicalNumberOfCells();

					String value = "";
					// 遍历列­
					for (int j = 0; j < cells; j++) {
						// 获取到列的值­
						HSSFCell cell = row.getCell(j);

						if (cell != null) {
							switch (cell.getCellType()) {
							case HSSFCell.CELL_TYPE_FORMULA:// 时间
								break;
							case HSSFCell.CELL_TYPE_NUMERIC:// 数字
								value += cell.getNumericCellValue() + "∈";
								break;
							case HSSFCell.CELL_TYPE_STRING:// 字符串
								value += cell.getStringCellValue() + "∈";
								break;
							case HSSFCell.CELL_TYPE_BLANK:// 空格
								value += " " + "∈";
								break;
							default:
								value += "0";
								break;
							}
						}
					}
					//将导入的数据插入到数据库中
					String[] val = value.split("∈");
					TCustomerTag tCustomerTag = new TCustomerTag();

					tCustomerTag.setId(val[0]);
					tCustomerTag.setFcustomerId(val[1]);
					tCustomerTag.setFtag((int) Double.parseDouble(val[2]));
					tCustomerTag.setFtag((int) Double.parseDouble(val[3]));
					tCustomerTag.setFcreateTime(new Date());
					
					list.add(tCustomerTag);
				}
			}
			customerTagDAO.save(list);
		} catch (FileNotFoundException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		} catch (IOException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
	}

	public static void main(String[] args) {
		try {
			readExcel(new File("D:\\test.xls"), null);
			// readExcel(new File("D:\\test.xls"));
		} catch (IOException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
	}
}
