package self.xf.excelprocess.util;

import cn.hutool.extra.pinyin.PinyinUtil;
import cn.hutool.poi.excel.cell.CellUtil;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import self.xf.excelprocess.base.ExcelFormat;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class FileToMySql {

    @Autowired
    DataBase base;

    public ArrayList<Object> fileProcess(MultipartFile file) {

        createNewSheet(file);
        Map<String, Object> map = GlobalSession.get();
        String fileName = FileProcess.getTableName(Objects.requireNonNull(file.getOriginalFilename()));
        map.put("fileName", fileName);

        ArrayList<Object> result = new ArrayList<>();

        // 创建数据库表
        base.generateDataBase(map);
        return result;
    }

    public void createNewSheet(MultipartFile file) {
        createFileStream(file);

        Map<String, Object> map = GlobalSession.get();
        Workbook workbook = (Workbook) map.get("workbook");

        // create a new workbook
        Sheet sheetFirst = workbook.getSheetAt(0);
        // 拿到第一行每个单元格内容作为list中每个对象的name
        List<ExcelFormat> excelFormats = getFirstRowName(sheetFirst);

        // create a new sheet prepare for subsequent use
        Sheet resultSheet = workbook.createSheet("result");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        int readRow = 0;
        int currentConsolidation = 0;


        for (Row eachRow : sheetFirst) {
            Row newRow = resultSheet.createRow(readRow++);
            int readCell = 0;
            int lastCellNum = eachRow.getLastCellNum();

            for (int i = 0; i < lastCellNum; i++) {
                Cell cell = eachRow.getCell(i);
                if (CellUtil.isMergedRegion(cell)) {
                    splitCell(sheetFirst, currentConsolidation);
                }
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
                        if (!checkIfNotation(cell)) {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getFirstCellValueAndType(sheetFirst.getRow(1), excelFormats);
        map.put("sql", excelFormats);
        map.put("sheet", resultSheet);
        GlobalSession.set(map);
    }

    private void splitCell(Sheet sheet, int currentConsolidation) {
        sheet.getMergedRegion(currentConsolidation);
    }

    private boolean checkIfNotation(Cell cell) {
        String str = cell.getStringCellValue();
        if (str.startsWith("!注意:")) {
            return true;
        }
        return false;
    }

    public void createFileStream(MultipartFile file) {
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
        GlobalSession.set(map);
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
            // 中文提取首字母拼音
            cellValue = chineseToPinyin(cellValue);
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
                        excelFormat.setValue(cell.getDateCellValue());
                    } else if (Integer.class.equals(num_type)) {
                        excelFormat.setType("Integer");
                        excelFormat.setValue(Integer.parseInt(cell.getStringCellValue()));
                    } else if (Double.class.equals(num_type)) {
                        excelFormat.setType("Double");
                        excelFormat.setValue(cell.getNumericCellValue());
                    } else {
                        excelFormat.setType("String");
                        String cellValue = cell.getStringCellValue();
                        cellValue = chineseToPinyin(cellValue);
                        excelFormat.setValue(cellValue);
                    }
                    break;
                case STRING:
                    Object str_type = checkStringDataType(cell);
                    if (Integer.class.equals(str_type)) {
                        excelFormat.setType("Integer");
                        excelFormat.setValue(Integer.parseInt(cell.getStringCellValue()));
                    } else if (Double.class.equals(str_type)) {
                        excelFormat.setType("Double");
                        excelFormat.setValue(cell.getNumericCellValue());
                    } else {
                        excelFormat.setType("String");
                        String cellValue = cell.getStringCellValue();
                        cellValue = chineseToPinyin(cellValue);
                        excelFormat.setValue(cellValue);                    }
                    break;
                case BOOLEAN:
                    excelFormat.setType("boolean");
                    excelFormat.setValue(cell.getBooleanCellValue());
                    break;
                default:
                    excelFormat.setType("undefined");
                    excelFormat.setValue("");
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
        if (str.startsWith("\"")) {
            str = str.substring(1);
        }
        // 判断str是否全部为数字
        if (str.matches("\\d+")) {
            return (T) Integer.class;
        }

        // 判断str是否能转为Date格式
        SimpleDateFormat simpleDateFormat = null;
        if (str.contains("-")) {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date parse = simpleDateFormat.parse(str);
                return (T) Date.class;
            } catch (ParseException e) {
                return (T) String.class;
            }
        }
        if (str.contains("/")) {
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

    public static String chineseToPinyin(String cellValue) {
        if (cellValue.matches("[\\u4e00-\\u9fa5]+")) {
            String pinyin = PinyinUtil.getPinyin(cellValue);
            pinyin = pinyin.replaceAll(" ", "");
            return pinyin;
        }
        return cellValue;
    }


    public static void main(String[] args) {
    }


}
