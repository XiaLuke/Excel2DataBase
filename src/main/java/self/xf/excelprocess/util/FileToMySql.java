package self.xf.excelprocess.util;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import self.xf.excelprocess.base.ExcelFormat;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class FileToMySql {
    ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<>();

    @Autowired
    DataBase base;

    public ArrayList<Object> fileProcess(MultipartFile file) {

        Map<String, Object> map = createNewSheet(file);
        map.put("fileName", file.getOriginalFilename());
        Sheet sheet = map.get("sheet") == null ? null : (Sheet) map.get("sheet");
        String fileName = map.get("fileName") == null ? null : (String) map.get("fileName");
        Row headRow = sheet.getRow(0);

        ArrayList<Object> result = new ArrayList<>();

        base.getTablename(map);

        // 判断temp是否在list中

        return result;
    }

    public Map<String, Object> createNewSheet(MultipartFile file) {
        Map<String, Object> map = createFileStream(file);
        Workbook workbook = (Workbook) map.get("workbook");
        // create a new workbook
        Sheet sheetFirst = workbook.getSheetAt(0);
        // 拿到第一行每个单元格内容作为list中每个对象的name
        List<ExcelFormat> excelFormats = getFirstRowName(sheetFirst);

        // create a new sheet prepare for subsequent use
        Sheet resultSheet = workbook.createSheet("result");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        int readRow = 0;
        excelFormats = getFirstCellValueAndType(sheetFirst.getRow(1), excelFormats);
        for (Row eachRow : sheetFirst) {
            Row newRow = resultSheet.createRow(readRow++);
            int readCell = 0;
            int lastCellNum = eachRow.getLastCellNum();

            for (int i = 0; i < lastCellNum; i++) {
                Cell cell = eachRow.getCell(i);
                if (cell == null) {
                    eachRow.createCell(i).setCellValue("undefined");
                    cell = eachRow.getCell(i);
                }

                switch (cell.getCellType()) {
                    case NUMERIC:
                        double numericCellValue = cell.getNumericCellValue();
                        String value;
                        if (HSSFDateUtil.isCellDateFormatted(cell)) {
                            Date date = HSSFDateUtil.getJavaDate(Double.parseDouble(String.valueOf(numericCellValue)));
                            value = dateFormat.format(date);
                            newRow.createCell(readCell++).setCellValue(value);
                        } else {
                            double dValue = cell.getNumericCellValue();
                            String s = String.valueOf(dValue);

                            newRow.createCell(readCell++).setCellValue(s);
                        }
                        break;
                    case STRING:
                        if(!checkIfNotation(cell)){
                            newRow.createCell(readCell++).setCellValue(cell.getStringCellValue());
                        }
                        break;
                    case BOOLEAN:
                        newRow.createCell(readCell++).setCellValue(cell.getBooleanCellValue());
                        break;
                    case FORMULA:
                        newRow.createCell(readCell++).setCellValue(cell.getCellFormula());
                        break;
                    case ERROR:
                        newRow.createCell(readCell++).setCellValue("ERROR");
                        break;
                    default:
                        newRow.createCell(readCell++).setCellValue("");
                }
            }
        }
        try {
            FileOutputStream fileOut = new FileOutputStream("processFile.xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        map.put("sql", excelFormats);
        map.put("sheet", resultSheet);
        return map;
    }

    private boolean checkIfNotation(Cell cell) {
        String str = cell.getStringCellValue();
        if(str.startsWith("!注意:")){
            return true;
        }
        return false;
    }

    public Map<String, Object> createFileStream(MultipartFile file) {
        byte[] bytes;
        Workbook workbook;
        try {
            bytes = file.getBytes();
            ByteArrayInputStream fileStream = new ByteArrayInputStream(bytes);
            workbook = WorkbookFactory.create(fileStream);
            fileStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("workbook", workbook);
        return map;
    }

    private List<ExcelFormat> getFirstRowName(Sheet headSheet) {
        Row headRow = headSheet.getRow(0);
        List<ExcelFormat> result = new ArrayList<>();
        for (Cell cell : headRow) {
            CellType cellType = cell.getCellType();
            if (!cellType.equals(CellType.STRING)) {
                throw new RuntimeException("请确保表格第一行格式正确");
            }
            String cellValue = cell.getStringCellValue();
            ExcelFormat excelFormat = new ExcelFormat();
            excelFormat.setName(cellValue);
            result.add(excelFormat);
        }
        return result;
    }

    private List<ExcelFormat> getFirstCellValueAndType(Row row, List<ExcelFormat> excelFormats) {
        for (int i = 0; i < excelFormats.size(); i++) {
            ExcelFormat excelFormat = excelFormats.get(i);
            Cell cell = row.getCell(i);
            CellType cellType = cell.getCellType();
            switch (cellType) {
                case NUMERIC:
                    Object num_type = checkNumDataType(cell);
                    if (Date.class.equals(num_type)) {
                        excelFormat.setType("Date");
                    } else if (Integer.class.equals(num_type)) {
                        excelFormat.setType("Integer");
                    } else if (Double.class.equals(num_type)) {
                        excelFormat.setType("Double");
                    } else {
                        excelFormat.setType("String");
                    }
                    break;
                case STRING:
                    Object str_type = checkStringDataType(cell);
                    if (Integer.class.equals(str_type)) {
                        excelFormat.setType("Integer");
                    } else if (Double.class.equals(str_type)) {
                        excelFormat.setType("Double");
                    } else {
                        excelFormat.setType("String");
                    }
                    break;
                case BOOLEAN:
                    excelFormat.setType("boolean");
                    break;
                default:
                    excelFormat.setType("undefined");
            }
        }
        return excelFormats;
    }

    private <T> T checkNumDataType(Cell cell) {
        double numericCellValue = cell.getNumericCellValue();
        String str = String.valueOf(numericCellValue);

        if (str.contains("-")) {
            return (T) Date.class;
        }
        // 是否包含小数
        if (str.matches("\\d+(\\.\\d+)?")) {
            return (T) Double.class;
        }
        return null;
    }

    private <T> T checkStringDataType(Cell cell) {
        String str = cell.getStringCellValue();
        if(str.startsWith("\"")){
            str = str.substring(1);
        }
        // 判断str是否全部为数字
        if (str.matches("\\d+")) {
            return (T) Integer.class;
        }

        // 判断str是否能转为Date格式
        SimpleDateFormat simpleDateFormat = null;
        if(str.contains("-")){
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date parse = simpleDateFormat.parse(str);
                return (T) Date.class;
            } catch (ParseException e) {
                return (T) String.class;
            }
        }
        if(str.contains("/")){
            simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
            try {
                Date parse = simpleDateFormat.parse(str);
                return (T) Date.class;
            } catch (ParseException e) {
                return (T) String.class;
            }
        }
        return (T) String.class;

    }

    public static void main(String[] args) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date parse = simpleDateFormat.parse("2023/9/12");
            System.out.println(111);
        } catch (ParseException e) {
            System.out.println(22);
        }
    }

}
