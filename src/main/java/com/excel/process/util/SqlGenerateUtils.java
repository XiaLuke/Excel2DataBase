package com.excel.process.util;

import org.apache.poi.ss.usermodel.DateUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SqlGenerateUtils {
    private static final int BATCH_SIZE = 1000;

    public static void generateSqlToFile(List<Map<String, Object>> data, String sheetName, String outputFilePath) throws IOException {
        if (data.isEmpty()) return;

        // 生成建表语句
        String createSql = generateCreateSql(data.get(0), sheetName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write(createSql + "\n");

            SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);

            // 分批处理数据
            for (int i = 0; i < data.size(); i += BATCH_SIZE) {
                int endIndex = Math.min(i + BATCH_SIZE, data.size());
                List<Map<String, Object>> batch = data.subList(i, endIndex);

                // 生成当前批次的 SQL
                String batchSql = generateBatchSql(batch, sheetName, idGenerator);
                writer.write(batchSql);
                writer.flush(); // 立即写入文件，释放内存
            }
        }
    }

    // 生成单个批次的 SQL
    private static String generateBatchSql(List<Map<String, Object>> batch, String sheetName, SnowflakeIdGenerator idGenerator) {
        StringBuilder sql = new StringBuilder();
        for (Map<String, Object> lineMap : batch) {
            String rowSql = buildInsertSql(lineMap, sheetName, idGenerator);
            sql.append(rowSql);
        }
        return sql.toString();
    }

    // 构建单条 INSERT 语句
    private static String buildInsertSql(Map<String, Object> lineMap, String sheetName, SnowflakeIdGenerator idGenerator) {
        StringBuilder rowSql = new StringBuilder("INSERT INTO ");
        rowSql.append(sheetName).append(" ("); // 修正：原代码中的 data 应为 sheetName

        // 添加列名
        rowSql.append("ID,");
        for (String key : lineMap.keySet()) {
            rowSql.append(key).append(",");
        }
        rowSql.deleteCharAt(rowSql.length() - 1); // 删除末尾多余的逗号

        // 添加值
        rowSql.append(") VALUES ('").append(idGenerator.generateId()).append("',");
        for (Object value : lineMap.values()) {
            if (value instanceof String) {
//                value = escapeSqlValue(value);
                rowSql.append(escapeSqlValue(value)).append(",");
            } else {
                rowSql.append(value).append(",");
            }
        }
        rowSql.deleteCharAt(rowSql.length() - 1); // 删除末尾多余的逗号
        rowSql.append(");\n");

        return rowSql.toString();
    }

    /**
     * 数字内容转日期格式
     *
     * @param obj
     * @return {@link Date}
     */
    public static Date numericToDate(Object obj) {
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

    private static String generateCreateSql(Map<String, Object> oneLineData, String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE " + tableName + " (");
        for (Map.Entry<String, Object> entry : oneLineData.entrySet()) {
            String originalKey = entry.getKey();
            String key = StaticMethod.getUpper(originalKey);
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

    public static String escapeSqlValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        String strValue = value.toString();

        // 替换单引号为两个单引号（SQL 语法规定）
        strValue = strValue.replace("'", "''");
        // 替换双引号
        strValue = strValue.replace("\"", "\\\"");
        // 处理反斜杠
        strValue = strValue.replace("\\", "\\\\");
        // 处理换行符
        strValue = strValue.replace("\n", " ");
        // 处理制表符
        strValue = strValue.replace("\t", " ");

        return "'" + strValue + "'";  // 返回带引号的值
    }

}
