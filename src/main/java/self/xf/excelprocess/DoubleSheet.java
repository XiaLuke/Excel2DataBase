package self.xf.excelprocess;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import self.xf.excelprocess.cqsy.Cbzzqd;
import self.xf.excelprocess.cqsy.CbzzqdBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DoubleSheet {
    private static final ThreadLocal<Object> threadLocal = new ThreadLocal<>();


    public void createNewSheet(MultipartFile file) {
        createFileStream(file);

        Map<String, Object> map = (Map<String, Object>) threadLocal.get();
        Workbook workbook = (Workbook) map.get("workbook");

        // create a new workbook
        int allSheet = workbook.getNumberOfSheets();
        for (int index = 0; index < allSheet; index++) {
            Sheet sheet = workbook.getSheetAt(index);
            String sheetName = sheet.getSheetName();
            if (sheetName.equals("before24hours")) {
                processFirst(sheet);
            }
            if (sheetName.equals("after24hours")) {
                processTwo(sheet);
            }
        }
        reflectToField();
        saveData();
    }

    private void processFirst(Sheet firstSheet) {
        Row row = firstSheet.getRow(0);
        int lastRowNum = firstSheet.getLastRowNum();
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i < lastRowNum; i++) {
            Row eachRow = firstSheet.getRow(i);
            if (eachRow.getCell(0) == null) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            for (int j = 0; j < row.getLastCellNum(); j++) {
                Cell cell = eachRow.getCell(j);
                CellType cellType = cell.getCellType();
                if (cellType == CellType.NUMERIC) {
                    double value = cell.getNumericCellValue();
                    map.put(row.getCell(j).getStringCellValue(), value);
                }
                if (cellType == CellType.STRING) {
                    String cellValue = cell.getStringCellValue();
                    if (cellValue != null && !cellValue.equals("")) {
                        map.put(row.getCell(j).getStringCellValue(), cellValue);
                    }
                }


            }
            if (map.size() > 0) {
                list.add(map);
            }
        }
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("firstSheet", list);
        threadLocal.set(objectMap);
    }

    private void processTwo(Sheet twoSheet) {
        Row row = twoSheet.getRow(0);
        int lastRowNum = twoSheet.getLastRowNum();
        Map<String, Object> map = (Map<String, Object>) threadLocal.get();
        List<Map<String, Object>> firstSheet = (List<Map<String, Object>>) map.get("firstSheet");
        List<String> keyCell = new ArrayList<>();
        for (Map<String, Object> stringStringMap : firstSheet) {
            keyCell.add((String) stringStringMap.get("hmhc"));
        }

        List<Map<String, String>> childList = new ArrayList<>();
        Map<String, Object> firstMap = null;
        for (int i = 1; i < lastRowNum; i++) {
            Row eachRow = twoSheet.getRow(i);
            Cell cell = eachRow.getCell(0);
            if (cell != null) {
                if (keyCell.contains(cell.getStringCellValue())) {
                    // 从第一张表中获取key为船名航次的数据
                    firstMap = firstSheet.get(keyCell.indexOf(cell.getStringCellValue()));
                    Map<String, String> childMap = new HashMap<>();
                    for (int j = 0; j < row.getLastCellNum(); j++) {
                        Cell cell1 = eachRow.getCell(j);
                        String cellValue = cell1.getStringCellValue();
                        if (cellValue != null && !("").equals(cellValue)) {
                            childMap.put(row.getCell(j).getStringCellValue(), cellValue);
                        }
                    }
                    childList.add(childMap);
                }
            }
        }
        firstMap.put("child", childList);
    }

    private void reflectToField() {
        List<Cbzzqd> list;
        Map<String, Object> map = (Map<String, Object>) threadLocal.get();
        List<Map<String, Object>> firstSheet = (List<Map<String, Object>>) map.get("firstSheet");
        list = firstSheet.stream().map(item -> {
            Cbzzqd cbzzqd = new Cbzzqd();
            item.entrySet().stream().filter(entry -> {
                String key = entry.getKey();
                return "hmhc".equals(key) || "qyg".equals(key) || "mdg".equals(key) || "yjcfsj".equals(key) || "yjddsj".equals(key) || "sfkh".equals(key) || "child".equals(key);
            }).forEach(entry -> {
                switch (entry.getKey()) {
                    case "hmhc":
                        cbzzqd.setCmhc((String) entry.getValue());
                        break;
                    case "qyg":
                        cbzzqd.setQyg((String) entry.getValue());
                        break;
                    case "mdg":
                        cbzzqd.setMdg((String) entry.getValue());
                        break;
                    case "yjcfsj":
                    case "yjddsj":
                        cbzzqd.setPreStartTime(numericToString(entry.getValue()));
                        break;
                    case "sfkh":
                        cbzzqd.setSfkb((String) entry.getValue());
                        break;
                    case "child":
                        putChildValue(entry.getValue());
                        break;
                }
            });
            return cbzzqd;
        }).collect(Collectors.toList());
        map.put("headList", list);
    }

    private void putChildValue(Object obj) {
        List<Map<String, Object>> list = (List<Map<String, Object>>) obj;
        List<CbzzqdBody> cbzzqdBodies;
        Map<String, Object> map = (Map<String, Object>) threadLocal.get();
        cbzzqdBodies = list.stream().map(item -> {
            CbzzqdBody cbzzqdBody = new CbzzqdBody();
            item.entrySet().stream().filter(entry -> {
                String key = entry.getKey();
                return "hmhc".equals(key) || "xh".equals(key) || "cc".equals(key) || "jzxcw".equals(key) || "nwm".equals(key) || "zywp".equals(key) || "mdg".equals(key);
            }).forEach(entry -> {
                switch (entry.getKey()) {
                    case "hmhc":
                        cbzzqdBody.setCmhc((String) entry.getValue());
                        break;
                    case "xh":
                        cbzzqdBody.setContainer((String) entry.getValue());
                        break;
                    case "cc":
                        cbzzqdBody.setSize((String) entry.getValue());
                        break;
                    case "jzxcw":
                        cbzzqdBody.setPosition((String) entry.getValue());
                        break;
                    case "nwm":
                        cbzzqdBody.setIO((String) entry.getValue());
                        break;
                    case "zywp":
                        cbzzqdBody.setPrpduct((String) entry.getValue());
                        break;
                    case "mdg":
                        cbzzqdBody.setTarget((String) entry.getValue());
                        break;
                    default:
                        break;
                }
            });
            return cbzzqdBody;
        }).collect(Collectors.toList());
        map.put("childList", cbzzqdBodies);
    }

    private void saveData() {
        Map<String, Object> map = (Map<String, Object>) threadLocal.get();
        List<Cbzzqd> list = (List<Cbzzqd>) map.get("headList");
        List<CbzzqdBody> cbzzqdBodies = (List<CbzzqdBody>) map.get("childList");
    }

    private Date numericToString(Object obj) {
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
        threadLocal.set(map);
    }

}
