package self.xf.excelprocess.util;

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
        return SessionUtils.isSessionTimeout(sessionId);
    }

    public void setFile(String sessionId, List<String> fileNames) {
        // 上传的文件先保存到指定位置中，再将名字存储到redis中
        Object o = redisService.get(sessionId);
        List<String> list = null;
        if (o instanceof List) {
             list = (List<String>) o;
        }
        list.addAll(fileNames);
        redisService.set(sessionId,list);
    }
}
