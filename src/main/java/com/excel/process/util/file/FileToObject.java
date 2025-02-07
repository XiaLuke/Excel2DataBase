package com.excel.process.util.file;

import com.excel.process.util.DataBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

@Configuration
@ConditionalOnProperty(name = "project.excel2sql.enable",value = "true",matchIfMissing = false)
public class FileToObject {

    private final DataBase base;
    private final FileUtils fileUtils;

    // 构造函数注入（推荐）
    public FileToObject(DataBase base, FileUtils fileUtils) {
        this.base = base;
        this.fileUtils = fileUtils;
    }

    public String getSqlWithExcel(String sessionId, MultipartFile file) {
        // 添加时间戳确保文件名唯一
        String sqlFileName = fileUtils.generateName(file);

        // 处理内容，写入文件
        fileUtils.writeContentToSql(sqlFileName,file,sessionId);

        // 将 【SessionId + sqlFileName】 存储到 Redis 中
        return sqlFileName;
    }

}
