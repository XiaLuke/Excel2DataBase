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
        List<ExcelFormat> headExcelFormat = new ArrayList<>();
        // create a new workbook
        Sheet sheetFirst = workbook.getSheetAt(0);
        // 拿到第一行每个单元格内容作为list中每个对象的name
        List<ExcelFormat> excelFormats = getFirstRowName(sheetFirst);

        // create a new sheet prepare for subsequent use
        Sheet resultSheet = workbook.createSheet("result");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        int readRow = 0;
        excelFormats = getFirstCellValueAndType(sheetFirst.getRow(1));
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
                        newRow.createCell(readCell++).setCellValue(cell.getStringCellValue());
                        break;
                    case BOOLEAN:
                        newRow.createCell(readCell++).setCellValue(cell.getBooleanCellValue());
                        break;
                    case FORMULA:
                        newRow.createCell(readCell++).setCellValue(cell.getCellFormula());
                        break;
                    case BLANK:
                        newRow.createCell(readCell++).setCellValue("");
                        break;
                    case ERROR:
                        newRow.createCell(readCell++).setCellValue("ERROR");
                        break;
                    case _NONE:
                        newRow.createCell(readCell++).setCellValue("");
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
        map.put("sql",excelFormats);
        map.put("sheet", resultSheet);
        return map;
    }

    public Map<String, Object> createFileStream(MultipartFile file) {
        byte[] bytes = new byte[0];
        Workbook workbook = null;
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

    private List<ExcelFormat> getFirstCellValueAndType(Row row) {
        List<ExcelFormat> result = new ArrayList<>();
        for (Cell cell : row) {
            ExcelFormat excelFormat = new ExcelFormat();
            CellType cellType = cell.getCellType();
            switch (cellType) {
                case NUMERIC:
                    excelFormat.setType("double");
                    break;
                case STRING:
                    excelFormat.setType("String");
                    break;
                case BOOLEAN:
                    excelFormat.setType("boolean");
                    break;
                case FORMULA:
                    excelFormat.setType("formula");
                    break;
                case BLANK:
                    excelFormat.setType("blank");
                    break;
                case ERROR:
                    excelFormat.setType("error");
                    break;
                case _NONE:
                    excelFormat.setType("none");
                    break;
                default:
                    excelFormat.setType("undefined");
            }
//            excelFormat.setValue(cell.getStringCellValue());
            result.add(excelFormat);
        }
        return result;
    }
}
