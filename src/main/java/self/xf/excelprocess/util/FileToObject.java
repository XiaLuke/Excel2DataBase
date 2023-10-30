package self.xf.excelprocess.util;

import cn.hutool.extra.pinyin.PinyinUtil;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import self.xf.excelprocess.base.ExcelFormat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class FileToObject {

    @Autowired
    DataBase base;

    public ArrayList<Object> fileProcess(MultipartFile file) {

        sheetToList(file);

        // 创建数据库表
        base.generateDataBase();
        return null;
    }

    public void sheetToList(MultipartFile file) {
        createFileStream(file);

        Map<String, Object> globalMap =  GlobalSession.getObjectMap();
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
                globalMap.put("list",new ArrayList<>());
            }
            reverseNumericToDate(list,sheetName);
        }
    }
    private void reverseNumericToDate(List<Map<String, Object>> list, String sheetName) {
        list.stream().map(item -> {
            item.forEach((key,value)->{
                if (key.contains("时间") || key.contains("日期")) {
                    Date date = numericToDate(value);
                    item.put(key, date);
                }
            });
            return item;
        }).collect(Collectors.toList());

        reverseChineseToPinyin(list,sheetName);
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
        List<Map<String, Object>> objectMapList = GlobalSession.getObjectMapList();
        Map<String,Object> objectMap = new HashMap<>();
//        sheetName = getUpper(sheetName);
        objectMap.put(sheetName,list);
        objectMapList.add(objectMap);
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

    private String getUpper(String value){
        StringBuilder pinyinText = new StringBuilder();
        if (value.matches("[\\u4e00-\\u9fa5]+")) {
            for (char c : value.toCharArray()) {
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
                if (pinyinArray != null && pinyinArray.length > 0) {
                    String pinyin = pinyinArray[0];
                    pinyinText.append(Character.toUpperCase(pinyin.charAt(0)));
                }
            }
        }
        return pinyinText.toString();
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


    public static void main(String[] args) {
    }


}
