package com.czyh.czyhweb.util.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtil {

	private Workbook workbook;

	private boolean isXls;

	public ExcelUtil(File excelFile) throws FileNotFoundException, IOException {
		if (excelFile.getName().toLowerCase().endsWith(".xls")) {
			workbook = new HSSFWorkbook(new FileInputStream(excelFile));
			isXls = true;
		} else {
			workbook = new XSSFWorkbook(new FileInputStream(excelFile));
			isXls = false;
		}
	}

	/**
	 * 获取工作薄数量
	 * 
	 * @return 工作薄数量
	 */
	public int getNumberOfSheets(Workbook book) {
		return book == null ? 0 : book.getNumberOfSheets();
	}

	/**
	 * 获取工作薄总行数
	 * 
	 * @param sheet
	 *            工作薄
	 * @return 工作薄总行数
	 */
	public int getRows(Sheet sheet) {
		return sheet == null ? 0 : sheet.getLastRowNum();
	}

	/**
	 * 获取最大列数
	 * 
	 * @param sheet
	 *            工作薄
	 * @return 总行数最大列数
	 */
	public int getColumns(Sheet sheet) {
		return sheet == null ? 0 : sheet.getRow(0).getLastCellNum();

	}

	/**
	 * 得到列的值
	 * 
	 * @param sheet
	 * @param row
	 * @param startcol
	 * @param endcol
	 * @return
	 */
	public Cell[] getRowCells(Sheet sheet, int row, int startcol, int endcol) {
		Cell[] cellArray = new Cell[endcol - startcol];
		int maxRow = this.getRows(sheet);
		int maxCos = this.getColumns(sheet);
		if (row < 0 || row > maxRow || startcol > maxCos || endcol < startcol) {
			return null;
		}
		if (startcol < 0) {
			startcol = 0;
		}
		Row rows = sheet.getRow(row);// 获得行对象
		for (int i = startcol; i < endcol && i < rows.getLastCellNum(); i++) {
			cellArray[i - startcol] = rows.getCell(i);
		}
		return cellArray;
	}

	/**
	 * 得到指定单元格的值
	 * 
	 * @param sheet
	 * @param row
	 * @param startcol
	 * @param endcol
	 * @return
	 */
	public Serializable getCell(Sheet sheet, int col, int row) {
		String thisSheetName = sheet.getSheetName();

		Cell cell = sheet.getRow(row).getCell(col);
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_BLANK:
			return "";
		case Cell.CELL_TYPE_NUMERIC:
			if (HSSFDateUtil.isCellDateFormatted(cell)) {
				return cell.getDateCellValue();
			} else {
				DecimalFormat df = new DecimalFormat("#.#");
				return df.format(cell.getNumericCellValue());
			}
		case Cell.CELL_TYPE_STRING:
			return cell.getRichStringCellValue().toString();
		case Cell.CELL_TYPE_FORMULA:
			FormulaEvaluator evaluator = null;
			if (isXls) {
				evaluator = new HSSFFormulaEvaluator((HSSFWorkbook) sheet.getWorkbook());
			} else {
				evaluator = new XSSFFormulaEvaluator((XSSFWorkbook) sheet.getWorkbook());
			}
			try {
				evaluator.evaluateFormulaCell(cell);// 检测公式有效性
			} catch (java.lang.IllegalArgumentException ex) {
				throw new RuntimeException("错误的单元格[" + thisSheetName + "-> 列：" + col + "；行：" + row + "]");
			}
			if (evaluator.evaluateFormulaCell(cell) == Cell.CELL_TYPE_ERROR) {
				throw new RuntimeException("错误的单元格[" + thisSheetName + "-> 列：" + col + "；行：" + row + "]");
			}
			if (evaluator.evaluateFormulaCell(cell) == Cell.CELL_TYPE_NUMERIC) {
				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					return cell.getDateCellValue();
				} else {
					DecimalFormat df = new DecimalFormat("#.#");
					return df.format(cell.getNumericCellValue());
				}
			} else if (evaluator.evaluateFormulaCell(cell) == Cell.CELL_TYPE_STRING) {
				return cell.getRichStringCellValue().toString();
			}
		case Cell.CELL_TYPE_BOOLEAN:
			return (cell.getBooleanCellValue());
		default:
			break;
		}
		return null;
	}

	/**
	 * 根据单元格坐标得出相应的值 如：B3
	 * 
	 * @param cellRowCode
	 *            :坐标
	 * @param sheet
	 *            ：工作表
	 * @return 返回值
	 */
	public Serializable getValueByCellCode(Sheet sheet, String cellRowCode) {
		String thisSheetName = sheet.getSheetName();
		CellReference ref = new CellReference(cellRowCode);
		int xy[] = { ref.getRow(), ref.getCol() };

		Cell cell = sheet.getRow(xy[0]).getCell(xy[1]);
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_BLANK:
			return "";
		case Cell.CELL_TYPE_NUMERIC:
			if (HSSFDateUtil.isCellDateFormatted(cell)) {
				return cell.getDateCellValue();
			} else {
				DecimalFormat df = new DecimalFormat("#.#");
				return df.format(cell.getNumericCellValue());
			}
		case Cell.CELL_TYPE_STRING:
			return cell.getRichStringCellValue().toString();
		case Cell.CELL_TYPE_FORMULA:
			FormulaEvaluator evaluator = null;
			if (isXls) {
				evaluator = new HSSFFormulaEvaluator((HSSFWorkbook) sheet.getWorkbook());
			} else {
				evaluator = new XSSFFormulaEvaluator((XSSFWorkbook) sheet.getWorkbook());
			}
			try {
				evaluator.evaluateFormulaCell(cell);// 检测公式有效性
			} catch (java.lang.IllegalArgumentException ex) {
				throw new RuntimeException("错误的单元格[" + thisSheetName + "->" + cellRowCode + "]");
			}
			if (evaluator.evaluateFormulaCell(cell) == Cell.CELL_TYPE_ERROR) {
				throw new RuntimeException("错误的单元格[" + thisSheetName + "->" + cellRowCode + "]");
			}
			if (evaluator.evaluateFormulaCell(cell) == Cell.CELL_TYPE_NUMERIC) {
				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					return cell.getDateCellValue();
				} else {
					DecimalFormat df = new DecimalFormat("#.#");
					return df.format(cell.getNumericCellValue());
				}
			} else if (evaluator.evaluateFormulaCell(cell) == Cell.CELL_TYPE_STRING) {
				return cell.getRichStringCellValue().toString();
			}
		case Cell.CELL_TYPE_BOOLEAN:
			return (cell.getBooleanCellValue());
		default:
			break;
		}
		return null;
	}

	/**
	 * 获得单元格中的内容
	 * 
	 * @param cell
	 * @return
	 */
	public Object getCellString(Cell cell) {
		Object result = null;
		if (cell != null) {
			int cellType = cell.getCellType();
			switch (cellType) {
			case Cell.CELL_TYPE_STRING:
				result = cell.getRichStringCellValue().getString();
				break;
			case Cell.CELL_TYPE_NUMERIC:
				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					result = cell.getDateCellValue();
				} else {
					DecimalFormat df = new DecimalFormat("#.#");
					result = df.format(cell.getNumericCellValue());
				}
				break;
			case Cell.CELL_TYPE_FORMULA:
				DecimalFormat df = new DecimalFormat("#.#");
				result = df.format(cell.getNumericCellValue());
				break;
			case Cell.CELL_TYPE_ERROR:
				result = StringUtils.EMPTY;
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				result = cell.getBooleanCellValue();
				break;
			case Cell.CELL_TYPE_BLANK:
				result = StringUtils.EMPTY;
				break;
			}
		} else {
			result = StringUtils.EMPTY;
		}
		return result;
	}

	public Workbook getWorkbook() {
		return workbook;
	}

	public void setWorkbook(Workbook workbook) {
		this.workbook = workbook;
	}

}
