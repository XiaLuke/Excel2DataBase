package self.xf.excelprocess.util;


import cn.hutool.extra.pinyin.PinyinUtil;
import org.apache.poi.ss.usermodel.Row;
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

    public void getTablename(Map<String, Object> map) {
        String fileName = map.get("fileName").toString();
        List<ExcelFormat> list = (List<ExcelFormat>) map.get("sql");
        List<String> tableList = tableList();
        // 判断fileName是中文还是英文
        boolean isChinese = false;
        for (int i = 0; i < fileName.length(); i++) {
            char c = fileName.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FA5) {
                isChinese = true;
                break;
            }
        }
        // 将fileName转换为拼音
        String temp = "";
        if (isChinese) {
            temp = PinyinUtil.getPinyin(fileName, "");
        }
        temp = fileName;
        // 如果temp结尾是以.xlsx结尾的，那么就去掉.xlsx
        if (temp.endsWith(".xlsx")) {
            temp = temp.substring(0, temp.length() - 5);
        }
        // 如果temp结尾是以.xls结尾的，那么就去掉.xls
        if (temp.endsWith(".xls")) {
            temp = temp.substring(0, temp.length() - 4);
        }
//        if(tableList.contains(temp)){
        // 获取表中的字段，没有的字段进行追加
        filterField(list, fileName);
//        }
    }

    public void filterField(List<ExcelFormat> list, String tableName) {
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

        String sql = createTable(tableName, list);
        // TODO: resolve SQL Execution Exceptions
        tableListMapper.create(sql);
    }

    private <T> T getType(Object type) {
        if (type.equals("String")) {
            return (T) "varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL";
        }
        if (type.equals("double")) {
            return (T) "decimal(10,2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL";
        }
        return null;
    }

    public String createTable(String tableName, List<ExcelFormat> list) {
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
