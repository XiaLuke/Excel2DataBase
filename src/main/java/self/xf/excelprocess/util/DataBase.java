package self.xf.excelprocess.util;


import cn.hutool.extra.pinyin.PinyinUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import self.xf.excelprocess.base.ExcelFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DataBase {

    public void generateDataBase() {
        Map<String, Object> mapList = GlobalSession.getListMap();
        Set<String> set = mapList.keySet();
        List<String> list = new ArrayList<>(set);
        list.forEach(item->{
            Map<String,Object> map = (Map<String,Object>)mapList.get(item);

        });
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
