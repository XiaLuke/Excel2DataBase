package com.excel.process.util;

import net.sourceforge.pinyin4j.PinyinHelper;

public class StaticMethod {

    public static String getUpper(String value) {
        StringBuilder pinyinText = new StringBuilder();
        if (value.matches("[\\u4e00-\\u9fa5]+")) {
            for (char c : value.toCharArray()) {
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
                if (pinyinArray != null && pinyinArray.length > 0) {
                    String pinyin = pinyinArray[0];
                    pinyinText.append(Character.toUpperCase(pinyin.charAt(0)));
                }
            }
            return pinyinText.toString();
        } else {
            return value;
        }
    }

    /**
     * 获取当前项目所在的目录
     * @return 当前项目所在的目录路径
     */
    public static String getCurrentProjectDirectory() {
        String property = System.getProperty("user.dir");
        StringBuilder sb = new StringBuilder();
        sb.append(property).append("\\src\\main\\resources\\");
        return sb.toString();
    }
}
