package self.xf.excelprocess.util.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import self.xf.excelprocess.util.DataBase;

@Component
public class FileToObject {

    @Autowired
    DataBase base;
    @Autowired
    private FileUtils fileUtils;

    public String getSqlWithExcel(String sessionId, MultipartFile file) {
        // 添加时间戳确保文件名唯一
        String sqlFileName = fileUtils.generateName(file);

        // 处理内容，写入文件
        fileUtils.writeContentToSql(sqlFileName,file,sessionId);

        // 将 【SessionId + sqlFileName】 存储到 Redis 中
        return sqlFileName;
    }

}
