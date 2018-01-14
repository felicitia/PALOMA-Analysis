package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JSONUtil {

	final static String inputFile = "/Users/felicitia/Google_Drive/Arcade/ICSE_2016_data/yixue_arch_result/class_smell/cxf_acdc.xls";
	final static String outputFile = "/Users/felicitia/Google_Drive/Arcade/ICSE_2016_data/yixue_arch_result/class_smell_json/cxf_acdc.json";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		writeJSONArray2File(xls2JSON(inputFile), outputFile);
	}

	public static JSONArray xls2JSON(String filename) {
		JSONArray resultArray = new JSONArray();
		try {
			FileInputStream file = new FileInputStream(new File(filename));
			HSSFWorkbook workbook = new HSSFWorkbook(file);
			String[] smellPerClassHeader = {"classname", "buo", "bdc", "spf", "bco", "all"};
			// Iterate through each sheets
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				HSSFSheet sheet = workbook.getSheetAt(i);
				JSONObject resultPerVersion = new JSONObject();
				JSONArray smellsPerVersion = new JSONArray();
				resultPerVersion.put("version", StringUtil.extractVersionPretty(sheet.getSheetName()));
				// Iterate through each rows from first sheet
				Iterator<Row> rowIterator = sheet.iterator();
				rowIterator.next();
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					JSONObject smellPerClass = new JSONObject();
					// For each row, iterate through each columns
					Iterator<Cell> cellIterator = row.cellIterator();
					int headerIdx = 0;
					while (cellIterator.hasNext()) {
						Cell cell = cellIterator.next();
						switch(cell.getCellType()){
						case Cell.CELL_TYPE_NUMERIC:
							smellPerClass.put(smellPerClassHeader[headerIdx++], (int)cell.getNumericCellValue());
							break;
						case Cell.CELL_TYPE_STRING:
							smellPerClass.put(smellPerClassHeader[headerIdx++], cell.getStringCellValue());
							break;
						}
					}
//					resultPerVersion.put("smells", smellPerClass);
					smellsPerVersion.add(smellPerClass);
				}
				resultPerVersion.put("smells", smellsPerVersion);
				resultArray.add(resultPerVersion);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("xls2JSON() done! (๑•ᴗ•๑)♡‼");
		return resultArray;
	}
	
	public static void writeJSONArray2File(JSONArray obj, final String jsonFile){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(jsonFile, "UTF-8");
			writer.println(obj.toJSONString());
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}
	
	public static void writeJSON2File(JSONObject obj, final String jsonFile){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(jsonFile, "UTF-8");
			writer.println(obj.toJSONString());
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}
	
	/**
	 * 
	 * @param filename
	 *            full file name
	 * @return
	 */
	public static Object readJsonFromFile(String filename) {
		Object obj = new JSONObject();
		JSONParser parser = new JSONParser();
		try {
			obj = parser.parse(new FileReader(filename));
			;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}
}
