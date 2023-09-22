package self.xf.excelprocess.util;


import cn.hutool.extra.pinyin.PinyinUtil;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import self.xf.excelprocess.mapper.TableListMapper;

import java.util.List;
import java.util.Map;

@Component
public class DataBase {
    @Autowired
    private TableListMapper tableListMapper;
    public List<String> tableList(){
        // 获取当前配置文件中数据连接获取当前数据的表名
        return tableListMapper.tableList();
    }
    public void getTablename(String fileName, Row headRow){
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
        if(isChinese){
            temp = PinyinUtil.getPinyin(fileName, "");
        }
        temp = fileName;
        // 如果temp结尾是以.xlsx结尾的，那么就去掉.xlsx
        if(temp.endsWith(".xlsx")){
            temp = temp.substring(0,temp.length()-5);
        }
        // 如果temp结尾是以.xls结尾的，那么就去掉.xls
        if(temp.endsWith(".xls")){
            temp = temp.substring(0,temp.length()-4);
        }
//        if(tableList.contains(temp)){
            // 获取表中的字段，没有的字段进行追加
            filterField(temp,headRow);
//        }
    }
    public void filterField(String tableName,Row headRow){
        Map<String,String> map = tableListMapper.createTable();
        // 数据库中的字段
        List<String> list = tableListMapper.columnList(tableName);
        for (int i = 0; i < headRow.getLastCellNum(); i++) {
            String cellValue = headRow.getCell(i).getStringCellValue();
            if(!list.contains(cellValue)){
                // 进行追加，需要字段名，字段类型，字段注解
            }
        }
    }
    public void createTable(String tableName){

    }
}
