package self.xf.excelprocess.util;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import self.xf.excelprocess.inter.EntryKey;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

// TODO：可删
@Component
public class ExcelProcess {
    public static ArrayList<Object> getSheetToJava(String path, Class clazz, Boolean useNewDate) throws Exception {
        Sheet sheet = importAndFilterExcel(path);

        // Get the first row (which contains attribute names)
        Row headerRow = sheet.getRow(0);

        // Iterate over the remaining rows and create entity objects
        Iterator<Row> rowIterator = sheet.iterator();
        ArrayList<Object> entities = new ArrayList<>();

        // process each row
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getRowNum() == 0) {
                continue;
            }


            // get the class name of the entity
            String className = clazz.getName();
            Object entity = Class.forName(className).newInstance();

            // traversing the fields labeled in the excel
            for (int i = 0; i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);

                // get the cell defined in the header row
                String attributeName = headerRow.getCell(i).getStringCellValue();

                // get the field in the entity class
                Field field = entity.getClass().getDeclaredField(attributeName);

                entity = processEachCellToEntity(field, cell, entity);
            }
            // If the field does not exist in the table but exists in the entity class，
            // get properties in filter entity
            Object livePropertiesInEntity = filterPropertiesInEntity(row, entity);


            Field[] fields = livePropertiesInEntity.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                // If the field is annotated with @EntryKey, generate a unique id
                if (field.isAnnotationPresent(EntryKey.class)) {
                    if (String.class.equals(field.getType()) && ("undefined").equals(field.get(entity))) {
                        String uid = UniqueIdGenerator.generateUniqueId();
                        field.set(entity, uid);
                    }
                }
            }

            // Add the entity to the list
            entities.add(entity);
        }
        return entities;
    }

    private static Sheet importAndFilterExcel(String path) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(path);

        // Load the Excel file and select the first sheet
        Workbook workbook = WorkbookFactory.create(fileInputStream);
        // get the first sheet
        Sheet sheet = workbook.getSheetAt(0);

        // Create a new sheet to store the valid data
        Sheet validSheet = workbook.createSheet("ValidData");

        // Define the date format used in the Excel file
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Iterate through each row in the sheet and filter out empty cells in the first column
        int currentRow = 0;
        ArrayList<Row> validRows = new ArrayList<>();

        for (Row row : sheet) {
            // if the first column is empty, skip the row
//            if (row.getCell(0) != null && !row.getCell(0).getStringCellValue().isEmpty()) {
            validRows.add(row);

            // Create a new row in the valid sheet and copy the data from the original row
            Row newRow = validSheet.createRow(currentRow++);
            int currentCol = 0;

            int lastCellNum = row.getLastCellNum();
            for (int cellIndex = 0; cellIndex < lastCellNum; cellIndex++) {
                Cell cell = row.getCell(cellIndex);
                if (cell == null) {
                    row.createCell(cellIndex).setCellValue("undefined");
                    cell = row.getCell(cellIndex);
//                    cell.setCellValue("undefined");
                }
                // Determine the data type of the cell and set the corresponding value in the new row
                // 975773
                switch (cell.getCellType()) {
                    case NUMERIC:
                        double numericCellValue = cell.getNumericCellValue();
                        String value;

                        if (HSSFDateUtil.isCellDateFormatted(cell)) {
                            Date date = HSSFDateUtil.getJavaDate(Double.parseDouble(String.valueOf(numericCellValue)));
                            value = dateFormat.format(date);
                            newRow.createCell(currentCol++).setCellValue(value);
                        } else {
                            double dValue = cell.getNumericCellValue();
                            String s = String.valueOf(dValue);

                            newRow.createCell(currentCol++).setCellValue(s);
                        }
                        break;
                    case STRING:
                        newRow.createCell(currentCol++).setCellValue(cell.getStringCellValue());
                        break;
                    case BOOLEAN:
                        newRow.createCell(currentCol++).setCellValue(cell.getBooleanCellValue());
                        break;
                    case FORMULA:
                        newRow.createCell(currentCol++).setCellValue(cell.getCellFormula());
                        break;
                    case BLANK:
                        newRow.createCell(currentCol++).setCellValue("");
                        break;
                    case ERROR:
                        newRow.createCell(currentCol++).setCellValue("ERROR");
                        break;
                    case _NONE:
                        newRow.createCell(currentCol++).setCellValue("");
                        break;
                    default:
                        newRow.createCell(currentCol++).setCellValue("");
                }
            }
//            }
        }

        // Save the valid data in the new sheet to a new file
        FileOutputStream outputStream = new FileOutputStream("test2.xlsx");
        workbook.write(outputStream);
        outputStream.close();

        // Close the input stream and workbook
        fileInputStream.close();
        workbook.close();

        return validSheet;
    }

    private static Object filterPropertiesInEntity(Row row, Object entity) {
        Field[] fieldsInEntity = entity.getClass().getDeclaredFields();
        // get each field in row and check if it exists in entity,if exists, delete from entity, then return entity
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            String attributeName = cell.getStringCellValue();
            for (Field field : fieldsInEntity) {
                if (field.getName().equals(attributeName)) {
                    field.setAccessible(true);
                    try {
                        field.set(entity, null);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return entity;
    }

    /**
     * 处理每个单元格的值，转为实体类中属性的类型
     *
     * @param field 实体类中属性
     * @param cell  excel中每个单元格
     */
    private static Object processEachCellToEntity(Field field, Cell cell, Object entity) throws Exception {
        field.setAccessible(true);
        // 获取cell中的值，判断是否存在某些特殊字符

        Class<?> fieldType = field.getType();
        Map<String, Object> cellTypeAndValue = getCellTypeAndValue(cell);
        Object value = cellTypeAndValue.get("value");
        if (fieldType.equals(cellTypeAndValue.get("type"))) {
            field.set(entity, value);
        } else if (fieldType.equals(Integer.class) && cellTypeAndValue.get("type").equals(BigDecimal.class)) {
            float floatValue = Float.parseFloat(value.toString());
            int intValue = (int) floatValue;
            field.set(entity, intValue);
        } else if (fieldType.equals(BigDecimal.class) && cellTypeAndValue.get("type").equals(Integer.class)) {
            field.set(entity, new BigDecimal(value.toString()));
        } else {
            System.out.printf("字段%s", field.getName());
        }
        return entity;
    }

    private static Map<String, Object> getCellTypeAndValue(Cell cell) throws Exception {
        CellType cellType = cell.getCellType();
        Map<String, Object> result = new HashMap();
        if (CellType.NUMERIC.equals(cellType)) {
            double numericCellValue = cell.getNumericCellValue();
            // 判断是否有小数，有小数转BigDecimal，没有转整数
            if (numericCellValue % 1 == 0) {
                // 转整数
                Integer integer = Integer.parseInt(String.valueOf(numericCellValue));
                result.put("type", Integer.class);
                result.put("value", integer);
            } else {
                // 转BigDecimal
                BigDecimal bigDecimal = new BigDecimal(String.valueOf(numericCellValue));
                result.put("type", BigDecimal.class);
                result.put("value", bigDecimal);
            }
        } else if (CellType.STRING.equals(cellType)) {
            String stringCellValue = cell.getStringCellValue();
            Boolean tag = stringIsBoolean(stringCellValue);
            if (tag) {
                SimpleDateFormat dateFormat = null;
                dateFormat = (stringCellValue.contains(" ")) ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") : new SimpleDateFormat("yyyy-MM-dd");
                Date date1 = dateFormat.parse(stringCellValue);
                result.put("type", Date.class);
                result.put("value", date1);
            } else if (stringCellValue.matches("\\d+\\.\\d+")) {
                BigDecimal bigDecimal = new BigDecimal(stringCellValue);
                result.put("type", BigDecimal.class);
                result.put("value", bigDecimal);
            } else if (stringCellValue.matches("\\d+")) {
                // 判断stringCellValue是否为日期格式
                // 如果不是小数，转为Integer
                Integer integer = Integer.parseInt(stringCellValue);
                result.put("type", Integer.class);
                result.put("value", integer);
            } else {
                result.put("type", String.class);
                result.put("value", stringCellValue);
            }
        } else if (CellType.BOOLEAN.equals(cellType)) {
            boolean booleanCellValue = cell.getBooleanCellValue();
            result.put("type", Boolean.class);
            result.put("value", booleanCellValue);
        } else if (CellType.FORMULA.equals(cellType)) {

        } else if (CellType.BLANK.equals(cellType)) {
            result.put("type", String.class);
            result.put("value", "undefined");
        } else if (CellType.ERROR.equals(cellType)) {
            result.put("type", String.class);
            result.put("value", "undefined");
        } else if (CellType._NONE.equals(cellType)) {
            result.put("type", String.class);
            result.put("value", "undefined");
        }
        return result;
    }

    private static Boolean stringIsBoolean(String value) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        sdf1.setLenient(false);
        sdf2.setLenient(false);
        Boolean tag = false;
        try {
            Date date = sdf1.parse(value);
            tag = true;
        } catch (ParseException e) {
            tag = false;
        }
        try {
            Date date = sdf2.parse(value);
            tag = true;
        } catch (ParseException e) {
            tag = false;
        }
        return tag;
    }
}
