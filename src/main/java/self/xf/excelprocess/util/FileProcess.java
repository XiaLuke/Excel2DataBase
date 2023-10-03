package self.xf.excelprocess.util;

import cn.hutool.extra.pinyin.PinyinUtil;
import self.xf.excelprocess.mapper.TableListMapper;

import java.util.List;

public class FileProcess {
    public static String getTableName(String fileFullName){
        // 判断fileName是中文还是英文
        boolean isChinese = false;
        for (int i = 0; i < fileFullName.length(); i++) {
            char c = fileFullName.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FA5) {
                isChinese = true;
                break;
            }
        }
        String temp = "";
        if (isChinese) {
            temp = PinyinUtil.getPinyin(fileFullName, "");
        }
        temp = fileFullName;
        // 如果temp结尾是以.xlsx结尾的，那么就去掉.xlsx
        if (temp.endsWith(".xlsx")) {
            temp = temp.substring(0, temp.length() - 5);
        }
        // 如果temp结尾是以.xls结尾的，那么就去掉.xls
        if (temp.endsWith(".xls")) {
            temp = temp.substring(0, temp.length() - 4);
        }
        return temp;
    }

    public static boolean hasInDataBase(TableListMapper tableListMapper, String fileName){
        List<String> list = tableListMapper.tableList();
        return list.contains(fileName);
    }
}
