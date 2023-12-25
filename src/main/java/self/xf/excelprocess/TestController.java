package self.xf.excelprocess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import self.xf.excelprocess.test.Test11;
import self.xf.excelprocess.util.ExcelProcess;
import self.xf.excelprocess.util.FileToObject;
import self.xf.excelprocess.util.GlobalSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

@RestController
public class TestController {
    @Autowired
    ExcelProcess excelProcess;
    @Autowired
    FileToObject fileToObject;


    @RequestMapping("/test")
    public void test11111() throws Exception {
        String path = "D:\\File\\Tencent\\WeChat\\WeChat Files\\wxid_it2vuwnjstek22\\FileStorage\\File\\2023-08\\zmqdsj.xlsx";
        String path1 = "C:\\Users\\XF\\Desktop\\test.xlsx";
        ArrayList<Object> sheetToJava = excelProcess.getSheetToJava(path1, Test11.class, false);
//        testMapper.save(sheetToJava);
    }

    @PostMapping("/importExcel")
    public void importExcel(@RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception {
        GlobalSession.set(file);
        ArrayList<Object> sheetToJava = fileToObject.fileProcess(request,response);
    }

    @PostMapping("/forceInsertTable")
    public void forceInsertTable(@RequestParam("file") MultipartFile file) throws Exception {
        GlobalSession.set(file);
        ArrayList<Object> sheetToJava = fileToObject.forceInsertTable();
    }

    @PostMapping("/softInsertTable")
    public void softInsertTable(@RequestParam("file") MultipartFile file) throws Exception {
//        ArrayList<Object> sheetToJava = fileToObject.softInsertTable(file);
    }

    @PostMapping("/genearateSql")
    public void genearateSql(@RequestParam("file") MultipartFile file) {
        GlobalSession.set(file);
//        ArrayList<Object> sheetToJava = fileToObject.genearateSql(file);
    }

}
