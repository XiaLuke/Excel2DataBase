package self.xf.excelprocess;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import self.xf.excelprocess.util.FileToObject;
import self.xf.excelprocess.util.GlobalStore;
import self.xf.excelprocess.util.StaticMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {
    @Autowired
    FileToObject fileToObject;


    @PostMapping("/getSqlWithExcel")
    public Map<String, Object> getSqlWithExcel(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Session-ID") String sessionId) throws Exception {
        // 设置文件信息

        Map<String, Object> result = new HashMap<>();
        try {
            if (GlobalStore.isSessionExpired(sessionId)) {
                result.put("success", false);
                result.put("message", "文件已超时被删除");
                return result;
            }
            GlobalStore.setFile(sessionId, file);

            // 处理文件
            String fileName = fileToObject.getSqlWithExcel(sessionId);

            result.put("success", true);
            result.put("fileNames", GlobalStore.getLastProcessedFileNames(sessionId));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/downloadSqlFile")
    public void downloadSqlFile(
            @RequestParam("requestName") String requestName,
            @RequestHeader("Session-ID") String sessionId,
            HttpServletResponse response) throws IOException {

        String fileName = GlobalStore.getLastProcessedFileName(sessionId, requestName);
        if (fileName == null) {
            throw new FileNotFoundException("没有找到可下载的文件");
        }

        String path = StaticMethod.getCurrentProjectDirectory() + fileName;
        File sqlFile = new File(path);
        if (!sqlFile.exists()) {
            throw new FileNotFoundException("SQL文件不存在");
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

        try (InputStream in = new FileInputStream(sqlFile);
             OutputStream out = response.getOutputStream()) {
            IOUtils.copy(in, out);
            out.flush();
        } finally {
            GlobalStore.removeProcessedFile(sessionId);
        }
    }
}
