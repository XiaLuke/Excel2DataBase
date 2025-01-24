package self.xf.excelprocess.util;

import cn.hutool.db.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import self.xf.excelprocess.util.redis.RedisService;

import java.util.ArrayList;
import java.util.List;

@Component
public class FileUtils {
    private RedisService redisService;

    @Autowired
    private void setRedisTemplate(RedisService _redisService) {
        redisService = _redisService;
    }

    // sessionId为前端会话是否还有连接，若长时间未操作，session超时后，所有文件将被删除
    public boolean fileExist(String sessionId) {
        // 从全局获取当前会话存活时间，与当前时间比较判断是否超时
        return false;
    }

    // 设置上传文件的过期时间
    public void setUploadFileTimeOut(String sessionId, List<String> fileNames) {
        // 文件过期需要 文件名 + 过期时间

    }

    public void deleteAllFileForSession(String sessionId){
        List<String> fileList = SessionUtils.getProcessFileMaps(sessionId);
    }
}
