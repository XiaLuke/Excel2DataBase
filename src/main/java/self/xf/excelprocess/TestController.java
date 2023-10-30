package self.xf.excelprocess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import self.xf.excelprocess.eci.mapper.HeadMapper;
import self.xf.excelprocess.eci.mapper.ListMapper;
import self.xf.excelprocess.eci.hnps.mapper.ZmqdMapper;
import self.xf.excelprocess.eci.mapper.TestMapper;
import self.xf.excelprocess.test.Test11;
import self.xf.excelprocess.util.ExcelProcess;
import self.xf.excelprocess.util.FileToObject;

import java.util.ArrayList;
import java.util.List;

// TODO：可删
@RestController
public class TestController {
    @Autowired
    ListMapper listMapper;
    @Autowired
    HeadMapper headMapper;
    @Autowired
    ZmqdMapper zmqdMapper;
    @Autowired
    ExcelProcess excelProcess;
    @Autowired
    FileToObject fileToObject;

    @Autowired
    TestMapper testMapper;

    @RequestMapping("qwe")
    public void test11() throws Exception {
        List<Object> objects = ExcelToJava.get();
        System.out.println(objects);
        zmqdMapper.save(objects);
    }

    @RequestMapping("hnzfHead")
    public void test22() throws Exception {
        List<Object> objects = ExcelToJava.get();
        System.out.println(objects);
        headMapper.save(objects);
    }

    @RequestMapping("/test")
    public void test11111() throws Exception {
        String path = "D:\\File\\Tencent\\WeChat\\WeChat Files\\wxid_it2vuwnjstek22\\FileStorage\\File\\2023-08\\zmqdsj.xlsx";
        String path1 = "C:\\Users\\XF\\Desktop\\test.xlsx";
        ArrayList<Object> sheetToJava = excelProcess.getSheetToJava(path1, Test11.class, false);
//        testMapper.save(sheetToJava);
    }

    @PostMapping("/importExcel")
    public void importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        ArrayList<Object> sheetToJava = fileToObject.fileProcess(file);
    }

}
