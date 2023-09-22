package self.xf.excelprocess.util;

import cn.hutool.extra.pinyin.PinyinUtil;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class FileToMySql {
    ThreadLocal<Map<String,Object>> threadLocal = new ThreadLocal<>();

    public ArrayList<Object> fileProcess(MultipartFile file) {
        Map<String,Object> map  = createNewSheet(file);
        Sheet sheet = map.get("sheet") == null ? null : (Sheet) map.get("sheet");
        String fileName = map.get("fileName") == null ? null : (String) map.get("fileName");
        Row headRow = sheet.getRow(0);

        ArrayList<Object> result = new ArrayList<>();

        // 判断fileName是中文还是英文
        boolean isChinese = false;
        for (int i = 0; i < fileName.length(); i++) {
            char c = fileName.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FA5) {
                isChinese = true;
                break;
            }
        }

        return result;
    }

    public Map<String,Object> createNewSheet(MultipartFile file) {
        Map<String,Object> map = createFileStream(file);
        Workbook workbook = (Workbook) map.get("workbook");
        // create a new workbook
        Sheet sheetFirst = workbook.getSheetAt(0);

        // create a new sheet prepare for subsequent use
        Sheet resultSheet = workbook.createSheet("result");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        int readRow = 0;
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

        map.put("sheet",resultSheet);
        return map;
    }

    public Map<String,Object> createFileStream(MultipartFile file) {
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
        Map<String,Object> map = new HashMap<>();
        map.put("fileName",file.getOriginalFilename());
        map.put("workbook",workbook);
        return map;
    }
}
