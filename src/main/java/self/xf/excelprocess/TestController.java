package self.xf.excelprocess;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import self.xf.excelprocess.util.FileToObject;
import self.xf.excelprocess.util.GlobalSession;

import javax.servlet.http.HttpServletRequest;
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
            HttpServletRequest request) throws Exception {
        // 设置文件信息

        Map<String, Object> result = new HashMap<>();
        try {
            String sessionId = request.getSession().getId();
            if (GlobalSession.isSessionExpired(sessionId)) {
                result.put("success", false);
                result.put("message", "文件已超时被删除");
                return result;
            }
            GlobalSession.setFile(sessionId, file);

            // 处理文件
            String fileName = fileToObject.getSqlWithExcel(sessionId);

            result.put("success", true);
            result.put("fileNames", GlobalSession.getLastProcessedFileNames(sessionId));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/downloadSqlFile")
    public void downloadSqlFile(
            @RequestParam("requestName") String requestName,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String sessionId = request.getSession().getId();

        String fileName = GlobalSession.getLastProcessedFileName(sessionId, requestName);
        if (fileName == null) {
            throw new FileNotFoundException("没有找到可下载的文件");
        }

        String path = "E:\\File\\JavaProject\\XF\\Excel2DataBase\\src\\main\\resources\\" + fileName;
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
            GlobalSession.removeProcessedFile(sessionId);
        }
    }
}
