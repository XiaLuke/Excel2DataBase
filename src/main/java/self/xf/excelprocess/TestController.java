package self.xf.excelprocess;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import self.xf.excelprocess.util.file.FileExpiryManager;
import self.xf.excelprocess.util.file.FileToObject;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {
    @Autowired
    FileToObject fileToObject;


    /**
     * 接收文件，将文件存储到本地 文件进行处理转为.sql文件，
     * 得到sql文件名，对其设置过期时间
     * @param file
     * @param sessionId
     * @return {@link Map}<{@link String}, {@link Object}>
     * @throws Exception
     */
    @PostMapping("/getSqlWithExcel")
    public Map<String, Object> getSqlWithExcel(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Session-ID") String sessionId) throws Exception {
        // 设置文件信息

        Map<String, Object> result = new HashMap<>();
        try {
            // 处理文件
            String fileName = fileToObject.getSqlWithExcel(sessionId,file);

            result.put("success", true);
            result.put("fileNames", fileName);
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

        String path = FileExpiryManager.getFilePath(sessionId,requestName);
        File sqlFile = new File(path);
        if (!sqlFile.exists()) {
            throw new FileNotFoundException("SQL文件不存在");
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(requestName, "UTF-8"));

        try (InputStream in = new FileInputStream(sqlFile);
             OutputStream out = response.getOutputStream()) {
            IOUtils.copy(in, out);
            out.flush();
        } finally {
//            GlobalStore.removeProcessedFile(sessionId);
        }
    }
}
