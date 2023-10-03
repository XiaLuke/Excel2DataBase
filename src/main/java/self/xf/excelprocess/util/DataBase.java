package self.xf.excelprocess.util;


import cn.hutool.extra.pinyin.PinyinUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import self.xf.excelprocess.base.ExcelFormat;
import self.xf.excelprocess.mapper.TableListMapper;

import java.util.List;
import java.util.Map;

@Component
public class DataBase {
    @Autowired
    private TableListMapper tableListMapper;

    public List<String> tableList() {
        // 获取当前配置文件中数据连接获取当前数据的表名
        return tableListMapper.tableList();
    }

    public void generateDataBase(Map<String, Object> map) {
        String fileName = map.get("fileName").toString();
        List<ExcelFormat> list = (List<ExcelFormat>) map.get("sql");
        boolean isInDataBase = FileProcess.hasInDataBase(tableListMapper, fileName);
        if (isInDataBase) {
            appendFieldToDataBase(list, fileName);
        } else {
            createTableToDataBase(list, fileName);
        }

        insertDataToDataBase(list, fileName);
    }

    private void insertDataToDataBase(List<ExcelFormat> list, String fileName) {
        for (ExcelFormat item : list) {
            String name = item.getName();
            Object value = item.getValue();
            String sql = "insert into " + fileName + "(" + name + ") values(\"" + value + "\")";
            tableListMapper.createTable(sql);
        }
    }

    private void appendFieldToDataBase(List<ExcelFormat> list, String fileName) {
        // 1. 查询当前表中存在的字段
        List<String> columnList = tableListMapper.columnList(fileName);
        // 2. 将list中的字段名和数据库中的字段名进行比较，如果数据库中没有，那么就添加
        for (ExcelFormat excelFormat : list) {
            String name = excelFormat.getName();
            if (!columnList.contains(name)) {
                // 3. 如果数据库中没有，那么就添加
                String sql = "alter table " + fileName + " add " + name + " " + getType(excelFormat.getType());
                tableListMapper.createTable(sql);
            }
        }
    }

    private void createTableToDataBase(List<ExcelFormat> list, String tableName) {
        // list中包含字段名，字段类型
        if (tableName.endsWith(".xlsx")) {
            tableName = tableName.substring(0, tableName.length() - 5);
        }
        if (tableName.endsWith(".xls")) {
            tableName = tableName.substring(0, tableName.length() - 4);
        }
        // 如果tableName中包含中文，那么就转换为拼音
        boolean isChinese = false;
        for (int i = 0; i < tableName.length(); i++) {
            char c = tableName.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FA5) {
                isChinese = true;
                break;
            }
        }
        if (isChinese) {
            tableName = PinyinUtil.getPinyin(tableName, "");
        }

        String sql = createTableToDataBase(tableName, list);
        // TODO: resolve SQL Execution Exceptions
        tableListMapper.createTable(sql);
    }

    private <T> T getType(Object type) {
        if (type.equals("String")) {
            return (T) "varchar(255) CHARACTER SET utf8mb4  DEFAULT NULL";
        }
        if (type.equals("double")) {
            return (T) "decimal(10,2) CHARACTER SET utf8mb4  DEFAULT NULL";
        }
        if (type.equals("Integer")) {
            return (T) "int(11) DEFAULT NULL";
        }
        return null;
    }

    public String createTableToDataBase(String tableName, List<ExcelFormat> list) {
        String sqlHead = "create table " + tableName + "(";
        StringBuffer sql = new StringBuffer(sqlHead);
        list.forEach(item -> {
            String name = item.getName();
            Object type = item.getType();
            String typeValue = getType(type);
            sql.append(name + " " + typeValue + ",");
        });
        sql.deleteCharAt(sql.length() - 1);
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        System.out.println(sql.toString());
        return sql.toString();
    }


}
