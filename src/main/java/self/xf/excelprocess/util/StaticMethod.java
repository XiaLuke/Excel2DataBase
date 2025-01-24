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
    public static void init(MultipartFile file) {
        // 根据全局文件创建工作流（Workbook）并保存到全局
        createFileStream(file);

        Map<String, Object> globalMap = GlobalStore.getOriginalDataMap();
        Workbook workbook = (Workbook) globalMap.get("workbook");

        Map<String,Object> afterMap = new HashMap<>();
        // 一个 sheet 为一个表
        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            Sheet eachSheet = workbook.getSheetAt(sheetIndex);
            String sheetName = eachSheet.getSheetName();

            List<Map<String, Object>> list = new ArrayList<>();

            // 获取首行和尾行
            Row firstRow = eachSheet.getRow(eachSheet.getFirstRowNum());
            int lastRowNum = eachSheet.getLastRowNum();

            for (int i = 1; i < lastRowNum; i++) {
                // 获取每行内容，若第一个单元格为空，跳过改行
                Row eachRow = eachSheet.getRow(i);
                if(eachRow == null || eachRow.getCell(0) == null) {
                    continue;
                }

                Map<String, Object> map = new HashMap<>();
                // 遍历首行的所有列信息
                for (int j = 0; j < firstRow.getLastCellNum(); j++) {
                    Cell cell = eachRow.getCell(j); // 当前行各个列对应单元格
                    if(cell == null) {
                        continue;
                    }
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
            List<Map<String, Object>> processData = reverseNumericToDate(list, sheetName);
            if(list.size() == 0) {
                continue;
            }
            Map<String, Object> oneLineData = list.get(0);
            String tableSql = generateTableCreateSql(oneLineData,sheetName);
            afterMap.put(sheetName+"_CREATETABLE", tableSql);
            afterMap.put(sheetName, processData);
        }
        GlobalStore.setListMap(afterMap);
    }

    private static String generateTableCreateSql(Map<String, Object> oneLineData, String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE " + tableName + " (");
        for (Map.Entry<String, Object> entry : oneLineData.entrySet()) {
            String originalKey = entry.getKey();
            String key = getUpper(originalKey);
            Object value = entry.getValue();
            if (value instanceof String) {
                sql.append(key + " varchar(255) DEFAULT NULL COMMENT '" + originalKey + "',");
            } else if (value instanceof Date) {
                sql.append(key + " datetime DEFAULT NULL COMMENT '" + originalKey + "',");
            } else if (value instanceof Double) {
                sql.append(key + " decimal(10,2) DEFAULT NULL COMMENT '" + originalKey + "',");
            } else if (value instanceof Integer) {
                sql.append(key + " int(11) DEFAULT NULL COMMENT '" + originalKey + "',");
            }
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        return sql.toString();
    }

    /**
     * 遍历从文件中解析后得到的list<Map> 内容，若key中包含 “时间”或“日期”内容，解析当前value
     *
     * @param list
     * @param sheetName
     */
    private static List<Map<String, Object>> reverseNumericToDate(List<Map<String, Object>> list, String sheetName) {
        list.stream().map(item -> {
            item.forEach((key, value) -> {
                if (key.contains("时间") || key.contains("日期")) {
                    Date date = numericToDate(value);
                    item.put(key, date);
                }
            });
            return item;
        }).collect(Collectors.toList());

        return reverseChineseToPinyin(list, sheetName);
    }

    private static List<Map<String, Object>> reverseChineseToPinyin(List<Map<String, Object>> list, String sheetName) {
        return list.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            item.forEach((key, value) -> {
                if (key instanceof String) {
                    key = getUpper(key);
                }
                String originalKey = key;
                int counter = 1;
                while (map.containsKey(key)) {
                    key = originalKey + "_" + counter;
                    counter++;
                }
                map.put(key, value);
            });
            item = map;
            return item;
        }).collect(Collectors.toList());

    }

    /**
     * 数字内容转日期格式
     *
     * @param obj
     * @return {@link Date}
     */
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


    /**
     * 从给定文件中创建文件流，初始化Workbook
     * 将Workbook 存储在全局映射中
     *
     */
    public static void createFileStream(MultipartFile file) {

        byte[] bytes;
        Workbook workbook;
        try {
            // 从 MultipartFile 获取字节数组
            bytes = file.getBytes();
            // 从字节数组创建 ByteArrayInputStream
            ByteArrayInputStream fileStream = new ByteArrayInputStream(bytes);
            // 从 ByteArrayInputStream 创建 Workbook
            workbook = WorkbookFactory.create(fileStream);
            // 关闭 ByteArrayInputStream
            fileStream.close();
        } catch (IOException e) {
            // 如果发生 I/O 错误，则抛出 RuntimeException
            throw new RuntimeException(e);
        }
        // 创建一个映射来存储 Workbook
        Map<String, Object> map = new HashMap<>();
        map.put("workbook", workbook);
        // 在 GlobalSession 中设置文件内容映射
        GlobalStore.setOriginalDataMap(map);
    }
    /**
     * 获取当前项目所在的目录
     * @return 当前项目所在的目录路径
     */
    public static String getCurrentProjectDirectory() {
        String property = System.getProperty("user.dir");
        StringBuilder sb = new StringBuilder();
        sb.append(property).append("\\src\\main\\resources\\");
        return sb.toString();
    }
}
