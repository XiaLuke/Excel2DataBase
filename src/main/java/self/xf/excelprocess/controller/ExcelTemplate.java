package self.xf.excelprocess.controller;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import self.xf.excelprocess.base.TemplateEntity;
import self.xf.excelprocess.inter.FilterIntroduction;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/exportExcel")
public class ExcelTemplate {
    @GetMapping("/export")
    public void export() {
        // 创建工作簿
        Workbook workbook = new XSSFWorkbook();

        // 创建工作表
        Sheet sheet = workbook.createSheet("Sheet1");

        // 获取模板类，根据模板类判断
        Class<TemplateEntity> clazz = TemplateEntity.class;
        Map<String, Integer> filterMap = new HashMap<>();

        Field[] declaredFields = clazz.getDeclaredFields();
        Row row1 = sheet.createRow(0);
        // 设置字体大小，字体加粗，单元格高度

        row1.setHeight((short) 500);

        for (int i = 0; i < declaredFields.length; i++) {
            Field field = declaredFields[i];
            field.setAccessible(true);
            String name = field.getName();
            // 获取注解
            String filterDes = "";
            FilterIntroduction annotation = field.getAnnotation(FilterIntroduction.class);
            if (annotation != null) {
                filterDes = annotation.description();
            }
            // 创建第一行属性

            Cell cell1 = row1.createCell(i);
            cell1.setCellValue(filterDes);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            // 为每个单元格设置边框
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
            // 设置宽度
            sheet.setColumnWidth(i, 20 * 256);
            // 设置字体
            Font font = workbook.createFont();
            font.setFontHeightInPoints((short) 16);
            font.setBold(true);
            cellStyle.setFont(font);
            cell1.setCellStyle(cellStyle);
            filterMap.put(name, i);
        }

        // key: filterName，value：colIndex
        Row row2 = sheet.createRow(1);
        filterMap.forEach((k, v) -> {
            Object cellValue = null;
            switch (k) {
                case "typeString":
                    cellValue = "中文描述等";
                    break;
                case "typeInteger":
                    cellValue = "整数类型，如：123";
                    break;
                case "typeDouble":
                    cellValue = "小数类型，如：123.456";
                    break;
                case "typeBoolean":
                    cellValue = "布尔类型，如：true";
                    break;
            }
            Cell cell2 = row2.createCell(v);

            cell2.setCellValue(String.valueOf(cellValue));
            System.out.println(k + " " + v);
        });


        // 写入数据到文件
        try (FileOutputStream outputStream = new FileOutputStream("example.xlsx")) {
            workbook.write(outputStream);
            System.out.println("Excel文件已创建并保存。");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
