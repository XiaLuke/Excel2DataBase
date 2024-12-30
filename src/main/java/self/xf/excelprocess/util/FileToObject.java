package self.xf.excelprocess.util;

import cn.hutool.extra.pinyin.PinyinUtil;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import self.xf.excelprocess.base.ExcelFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class FileToObject {

    @Autowired
    DataBase base;

    public ArrayList<Object> getSqlWithExcel(HttpServletRequest request, HttpServletResponse response) throws Exception{
        fileToList();

        // 创建文件后，将内容写入，并下载该文件
        File logFile = new File("E:\\File\\Study\\JetBrains\\IdeaProject\\ExcelProcess\\text.sql");
        InputStream in = new FileInputStream(logFile);
        String filenamedisplay = URLEncoder.encode("text.sql", "UTF-8");
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + filenamedisplay);
        response.setContentType("application/x-download;charset=utf-8");
        OutputStream out = response.getOutputStream();
        IOUtils.copy(in, out);
        out.flush();
        in.close();
        // 创建数据库表
//        base.generateDataBase();
        return null;
    }


    /**
     * 删表插入数据
     * @return {@link ArrayList}<{@link Object}>
     */
    public ArrayList<Object> forceInsertTable() {
        fileToList();
        return null;
    }

    public void fileToList(){
        StaticMethod.init();
        // {表:{行：{列：值}}
        Map<String, Object> mapList = GlobalSession.getListMap();

        List<Map<String, Object>> tableMap;
        if (mapList.size() < 1) {
            throw new RuntimeException("请先上传文件");
        } else if (mapList.size() == 1) {
            String tableName = mapList.keySet().iterator().next();
            tableMap = (List<Map<String, Object>>)mapList.get(tableName);
            String insertSql = generateInsertSql(tableMap, tableName); // 生成 insert 数据

            // 保存到文件中
            FileWriter writer = null;
            String path = "E:\\File\\Study\\JetBrains\\IdeaProject\\ExcelProcess\\text.sql";
            try {
                // true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好
                writer = new FileWriter(path, true);
                writer.write(insertSql);
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            Set<String> set = mapList.keySet();
            // 将set转为list
            List<String> list = new ArrayList<>(set);
            for (String tableName : list) {
                tableMap = (List<Map<String, Object>>) mapList.get(tableName);
                String insertSql = generateInsertSql(tableMap, tableName); // 生成 insert 数据

                // 保存到文件中
                FileWriter writer = null;
                String path = "E:\\File\\Study\\JetBrains\\IdeaProject\\ExcelProcess\\text.sql";
                try {
                    // true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好
                    writer = new FileWriter(path, true);
                    writer.write(insertSql);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        writer.flush();
                        writer.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 生成插入语句
     *
     * @param tableMap
     * @param tableName
     */
    public String generateInsertSql(List<Map<String, Object>> tableMap, String tableName) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> lineMap : tableMap) {
            String sql = "INSERT INTO " + tableName + " (";
            Set<String> strings1 = lineMap.keySet();
            List<String> list1 = new ArrayList<>(strings1);
            for (String s : list1) {
                sql += s + ",";
            }
            sql = sql.substring(0, sql.length() - 1);
            sql += ") VALUES (";
            for (String s : list1) {
                Object o = lineMap.get(s);
                if (o instanceof String) {
                    sql += "'" + o + "',";
                } else {
                    sql += o + ",";
                }
            }
            sql = sql.substring(0, sql.length() - 1);
            sql += ");\n";
            sb.append(sql);
        }
        return sb.toString();
    }

    public void sheetToList(MultipartFile file) {
        createFileStream(file);

        Map<String, Object> globalMap = GlobalSession.getFileContentMap();
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
            if (GlobalSession.getListMap() == null) {
                globalMap.put("list", new ArrayList<>());
            }
            reverseNumericToDate(list, sheetName);
        }
    }

    private void reverseNumericToDate(List<Map<String, Object>> list, String sheetName) {
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

    private void reverseChineseToPinyin(List<Map<String, Object>> list, String sheetName) {
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
        Map<String, Object> objectMapList = GlobalSession.getListMap();
        Map<String, Object> objectMap = new HashMap<>();
//        sheetName = getUpper(sheetName);
        objectMap.put(sheetName, list);
    }

    private Date numericToDate(Object obj) {
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
        GlobalSession.setFileContentMap(map);
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

    private String getUpper(String value) {
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
                        excelFormat.setValue(cellValue);
                    }
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

    public ArrayList<Object> genearateSql(MultipartFile file) {
        return null;
    }
}
