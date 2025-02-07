package com.excel.process.util.file;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.excel.process.util.SqlGenerateUtils;
import com.excel.process.util.StaticMethod;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Component
public class FileUtils {

    public void writeContentToSql(String fileName, MultipartFile file, String sessionId) {
        Workbook workbook = init(file); // 创建 workbook 工作流

        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            Sheet eachSheet = workbook.getSheetAt(sheetIndex);
            String sheetName = eachSheet.getSheetName();

            List<Map<String, Object>> list = new ArrayList<>();

            // 获取首行作为列名
            Row firstRow = eachSheet.getRow(eachSheet.getFirstRowNum());
            List<String> headers = new ArrayList<>();
            for (int j = 0; j < firstRow.getLastCellNum(); j++) { // 读取首行第一列到最后一列
                Cell headerCell = firstRow.getCell(j);
                if (headerCell != null) {
                    headers.add(StaticMethod.getUpper(headerCell.getStringCellValue()));
                }
            }

            // 跳过首行开始遍历
            for (Row eachRow : eachSheet) {
                if (eachRow.getRowNum() == 0) continue; // 跳过首行
                Map<String, Object> rowData = readRow(eachRow, headers);
                if (rowData != null) {
                    list.add(rowData);
                }
            }

            //  生成 sql 语句
            if (!list.isEmpty()) {
                String path = StaticMethod.getCurrentProjectDirectory() + fileName;
                try {
                    SqlGenerateUtils.generateSqlToFile(list, sheetName, path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // 设置文件的过期时间
                FileExpiryManager.addFileWithExpiry(path, sessionId);
            }
        }

    }

    /**
     * 处理每一行数据转为Map<String,Object>
     *
     * @param eachRow
     * @param headers
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    private static Map<String, Object> readRow(Row eachRow, List<String> headers) {
        if (eachRow == null) return null;
        HashMap<String, Object> hashMap = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = eachRow.getCell(i);
            Object cellValue = readCell(cell);
            if (cellValue != null) {
                hashMap.put(headers.get(i), cellValue);
            }
        }
        return hashMap.isEmpty() ? null : hashMap;
    }

    /**
     * 读取每个单元格中内容
     *
     * @param cell
     * @return {@link Object}
     */
    private static Object readCell(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
//                    return cell.getDateCellValue();
                    return SqlGenerateUtils.numericToDate(cell.getDateCellValue());
                }
                return new BigDecimal(cell.getNumericCellValue()).toPlainString(); // **转换为普通数字字符串**
            case STRING:
                String value = cell.getStringCellValue();
                return value != null && !value.isEmpty() ? value : null;
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
            default:
                return null;
        }
    }

    /**
     * 创建新的 sql 文件名
     *
     * @param file
     * @return {@link String}
     */
    public String generateName(MultipartFile file) {
        // 获取文件原始名与时间戳，尽量确保文件名不会重复
        String originalFileName = file.getOriginalFilename();
        String timestamp = String.valueOf(System.currentTimeMillis());

        if (originalFileName.endsWith(".xlsx")) {
            originalFileName = originalFileName.substring(0, originalFileName.length() - 5);
        } else if (originalFileName.endsWith(".xls")) {
            originalFileName = originalFileName.substring(0, originalFileName.length() - 4);
        }

        // 添加时间戳确保文件名唯一
        return originalFileName + "_" + timestamp + ".sql";
    }

    /**
     * 根据文件初始化生成Workbook
     *
     * @param file
     * @return {@link Workbook}
     */
    public static Workbook init(MultipartFile file) {

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
        return workbook;
    }
}
