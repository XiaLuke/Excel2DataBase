package self.xf.excelprocess.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class StaticMethod {
    static  {
        MultipartFile file = GlobalSession.getFile();
        createFileStream(file);

        Map<String, Object> globalMap = GlobalSession.getObjectMap();
        Workbook workbook = (Workbook) globalMap.get("workbook");

        // create a new workbook
        int sheetCount = workbook.getNumberOfSheets();
        for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
            // 每个表
            Sheet eachSheet = workbook.getSheetAt(sheetIndex);
            String sheetName = eachSheet.getSheetName();

            List<Map<String, Object>> list = new ArrayList<>();

            // 头行
            Row firstRow = eachSheet.getRow(eachSheet.getFirstRowNum());
            int lastRowNum = eachSheet.getLastRowNum();

            for (int i = 1; i < lastRowNum; i++) {
                Row eachRow = eachSheet.getRow(i);
                if (eachRow.getCell(0) == null) {
                    continue;
                }
                Map<String, Object> map = new HashMap<>();
                for (int j = 0; j < firstRow.getLastCellNum(); j++) {
                    Cell cell = eachRow.getCell(j);
                    CellType cellType = cell.getCellType();
                    if (cellType == CellType.NUMERIC) {
                        double value = cell.getNumericCellValue();
                        map.put(firstRow.getCell(j).getStringCellValue(), value);
                    }
                    if (cellType == CellType.STRING) {
                        String cellValue = cell.getStringCellValue();
                        if (cellValue != null && !cellValue.equals("")) {
                            map.put(firstRow.getCell(j).getStringCellValue(), cellValue);
                        }
                    }
                }
                if (map.size() > 0) {
                    list.add(map);
                }
            }
            if (GlobalSession.getObjectMapList() == null) {
                globalMap.put("list", new ArrayList<>());
            }
            reverseNumericToDate(list, sheetName);
        }
    }

    private static void reverseNumericToDate(List<Map<String, Object>> list, String sheetName) {
        list.stream().map(item -> {
            item.forEach((key, value) -> {
                if (key.contains("时间") || key.contains("日期")) {
                    Date date = numericToDate(value);
                    item.put(key, date);
                }
            });
            return item;
        }).collect(Collectors.toList());

        reverseChineseToPinyin(list, sheetName);
    }

    private static void reverseChineseToPinyin(List<Map<String, Object>> list, String sheetName) {
        list = list.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            item.forEach((key, value) -> {
                if (key instanceof String) {
                    key = getUpper(key);
                }
                if (map.get(key) != null) {
                    key = key + "_1";
                }
                map.put(key, value);
            });
            item = map;
            return item;
        }).collect(Collectors.toList());
        List<Map<String, Object>> objectMapList = GlobalSession.getObjectMapList();
        Map<String, Object> objectMap = new HashMap<>();
//        sheetName = getUpper(sheetName);
        objectMap.put(sheetName, list);
        objectMapList.add(objectMap);
    }

    private static Date numericToDate(Object obj) {
        double value = Double.parseDouble(obj.toString());
        if (value > 25569 && value < 290000000) {
            Date javaDate = DateUtil.getJavaDate(value);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(javaDate);
            try {
                return dateFormat.parse(formattedDate);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private static String getUpper(String value) {
        StringBuilder pinyinText = new StringBuilder();
        if (value.matches("[\\u4e00-\\u9fa5]+")) {
            for (char c : value.toCharArray()) {
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
                if (pinyinArray != null && pinyinArray.length > 0) {
                    String pinyin = pinyinArray[0];
                    pinyinText.append(Character.toUpperCase(pinyin.charAt(0)));
                }
            }
            return pinyinText.toString();
        } else {
            return value;
        }
    }

    public static void createFileStream(MultipartFile file) {
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

}
