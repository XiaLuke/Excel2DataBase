package self.xf.excelprocess.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FileToObject {

    @Autowired
    DataBase base;

    public String getSqlWithExcel(String sessionId) throws Exception {
        // 获取文件原始名与时间戳，尽量确保文件名不会重复
        String originalFileName = GlobalSession.getFile().getOriginalFilename();
        String timestamp = String.valueOf(System.currentTimeMillis());

        if (originalFileName.endsWith(".xlsx")) {
            originalFileName = originalFileName.substring(0, originalFileName.length() - 5);
        } else if (originalFileName.endsWith(".xls")) {
            originalFileName = originalFileName.substring(0, originalFileName.length() - 4);
        }

        // 添加时间戳确保文件名唯一
        String sqlFileName = originalFileName + "_" + timestamp + ".sql";
        GlobalSession.setLastProcessedFileName(sessionId, sqlFileName);

        // 处理内容，写入文件
        writeContentToSql(sqlFileName);
        return sqlFileName;
    }

    /**
     * 删表插入数据
     *
     * @return {@link ArrayList}<{@link Object}>
     */
    public ArrayList<Object> forceInsertTable() {
//        fileToList();
        return null;
    }

    /**
     * 写入sql文件
     * @param fileName
     */
    public void writeContentToSql(String fileName) {
        StaticMethod.init();
        Map<String, Object> mapList = GlobalSession.getListMap();

        List<Map<String, Object>> tableMap;
        if (mapList.size() < 1) {
            throw new RuntimeException("上传文件为空，请重新上传");
        } else {
            Set<String> keys = mapList.keySet();
            List<String> tableNames = keys.stream()
                    .filter(item -> item.contains("_CREATETABLE"))
                    .collect(Collectors.toList());

            List<String> containName = keys.stream()
                    .filter(item -> !item.contains("_CREATETABLE"))
                    .collect(Collectors.toList());

            for (String tableName : containName) {
                String createTableSql = tableNames.stream()
                        .filter(item -> item.contains(tableName))
                        .collect(Collectors.toList())
                        .get(0);

                tableMap = (List<Map<String, Object>>) mapList.get(tableName);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(mapList.get(createTableSql));
                stringBuilder.append(generateInsertSql(tableMap, tableName));

                String path = "E:\\File\\JavaProject\\XF\\Excel2DataBase\\src\\main\\resources\\" + fileName;
                File file = new File(path);

                try (FileWriter writer = new FileWriter(file, true)) {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    writer.write(stringBuilder.toString());
                    writer.flush();

                    // 添加文件到过期管理
                    GlobalSession.addFileWithExpiry(path);

                } catch (IOException ex) {
                    ex.printStackTrace();
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
    public StringBuilder generateInsertSql(List<Map<String, Object>> tableMap, String tableName) {
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
        return sb;
    }
}
