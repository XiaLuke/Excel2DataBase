package self.xf.excelprocess;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.*;
import self.xf.excelprocess.util.UniqueIdGenerator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class ExcelToJava {
    public static final String Excel2003L = ".xls";
    public static final String Excel2007L = ".xlsx";
    public static final String PATH = "D:\\File\\Tencent\\WeChat\\WeChat Files\\wxid_it2vuwnjstek22\\FileStorage\\File\\2023-08\\zmqdsj.xlsx";

    public static ArrayList<Object> get() throws Exception {
        //获取文件流
        FileInputStream fileInputStream = new FileInputStream(PATH);

        //2.得到表
        Sheet sheet = importAndFilterExcel(fileInputStream);

        // Get the first row (which contains attribute names)
        Row headerRow = sheet.getRow(0);

        // Iterate over the remaining rows and create entity objects
        Iterator<Row> rowIterator = sheet.iterator();
        ArrayList<Object> entities = new ArrayList<>();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getRowNum() == 0) {
                continue;
            }

            // Create an instance of the entity class
            String className = "self.xf.excelprocess.hnps.entity.zmqd";
            Object entity = Class.forName(className).newInstance();

            // Set the entity's attributes based on the row's values
            for (int i = 0; i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);
                String attributeName = headerRow.getCell(i).getStringCellValue();
                Field field = entity.getClass().getDeclaredField(attributeName);
                field.setAccessible(true);
                if (field.getType() == Integer.class) {
                    field.set(entity, (int) cell.getNumericCellValue());
                } else if (field.getType() == String.class) {
                    field.set(entity, cell.getStringCellValue());
                } else if (field.getType() == Double.class) {
                    field.set(entity, cell.getNumericCellValue());
                }
                if("AUTO_ID".equals(field.getName())){
//                    SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();
//                    long uid = idGenerator.nextId();
                    String uid = UniqueIdGenerator.generateUniqueId();
                    field.set(entity,uid);
                }
                if("CREATE_DATE".equals(field.getName())){
                    field.set(entity,new Date());
                }
            }

            // Add the entity to the list
            entities.add(entity);
        }

        // Do something with the list of entities (e.g. save them to a database)
//        for (Object entity : entities) {
//            System.out.println(entity.toString());
//        }
        return entities;
    }

    public static Sheet importAndFilterExcel(FileInputStream inputStream) throws Exception {

        // Load the Excel file and select the first sheet
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        // Create a new sheet to store the valid data
        Sheet validSheet = workbook.createSheet("ValidData");

        // Define the date format used in the Excel file
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Iterate through each row in the sheet and filter out empty cells in the first column
        int currentRow = 0;
        ArrayList<Row> validRows = new ArrayList<>();
        for (Row row : sheet) {
            if (row.getCell(0) != null && !row.getCell(0).getStringCellValue().isEmpty()) {
                validRows.add(row);

                // Create a new row in the valid sheet and copy the data from the original row
                Row newRow = validSheet.createRow(currentRow++);
                int currentCol = 0;
                for (Cell cell : row) {
                    // Determine the data type of the cell and set the corresponding value in the new row
                    switch (cell.getCellType()) {
                        case NUMERIC:
//                            newRow.createCell(currentCol++).setCellValue(cell.getNumericCellValue());
                            String value;
                            if (HSSFDateUtil.isCellDateFormatted(cell)) {
                                Date date = HSSFDateUtil.getJavaDate(Double.parseDouble(String.valueOf(cell.getNumericCellValue())));
                                value = dateFormat.format(date);
                                newRow.createCell(currentCol++).setCellValue(value);
                            } else {
                                double dValue = cell.getNumericCellValue();
                                DecimalFormat df = new DecimalFormat("0");
                                value = df.format(dValue);
                                newRow.createCell(currentCol++).setCellValue(value);
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
            }
        }

        // Save the valid data in the new sheet to a new file
        FileOutputStream outputStream = new FileOutputStream("test2.xlsx");
        workbook.write(outputStream);
        outputStream.close();

        // Close the input stream and workbook
        inputStream.close();
        workbook.close();

        return validSheet;
    }

}
