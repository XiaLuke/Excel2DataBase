package self.xf.excelprocess;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class JavaToExcel {

    public static final String Excel2003L = ".xls";
    public static final String Excel2007L = ".xlsx";
    public static final String PATH = "c://Users/Administrator/Desktop/";

    public static void main(String[] args) {
        //double hssfTime = hssfWorkBookTest("self.xf.excelprocess.JavaToExcel-XLS");
        //System.out.println("hssf耗时："+ hssfTime);
        double xlsxTime = xssfWorkBookTest("self.xf.excelprocess.JavaToExcel-XLSX");
        System.out.println("xlsx耗时："+xlsxTime);
    }
    /**
     *
     * @param fileName 文件名
     * @param begin 记录开始时间
     * @param workbook
     * @return
     */
    private static double common(String fileName, long begin, Workbook workbook, String format) {
        //创建表
        Sheet sheet = workbook.createSheet();
        for (int rowNumber = 0; rowNumber < 65536; rowNumber++) {
            //创建列
            Row row = sheet.createRow(rowNumber);
            for (int cellNumber = 0; cellNumber < 10; cellNumber++) {
                //创建列
                Cell cell = row.createCell(cellNumber);
                //设置值
                cell.setCellValue(cellNumber);
            }
        }
        System.out.println("结束");

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(PATH + fileName + format);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                assert fileOutputStream != null;
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        return (double) (end - begin) / 1000;
    }

    /**
     * 这种方式导出的文件格式为office 2003专用格式，即.xls，优点是导出数据速度快，但是 最多65536行 数据
     * @param fileName
     * @return
     */
    public static double hssfWorkBookTest(String fileName){
        //1.导入apace poi的依赖
        //1.1 poi(03版excel) poi-ooxml(07版excel)
        //2.hssf（03）方式进行导出
        //记录时间
        long begin = System.currentTimeMillis();
        //创建工作普
        HSSFWorkbook workbook = new HSSFWorkbook();
        return common(fileName, begin, workbook,Excel2003L);
    }

    /**
     *  这种方式导出的文件格式为office 2007专用格式，即.xlsx，优点是导出的数据不受行数限制，缺点导出速度慢
     * @param fileName
     * @return
     */
    public static double xssfWorkBookTest(String fileName){
        //1.导入apace poi的依赖
        //1.1 poi(03版excel) poi-ooxml(07版excel)
        //2.xssf（07）方式进行导出
        //记录时间
        long begin = System.currentTimeMillis();
        //创建工作普
        XSSFWorkbook workbook = new XSSFWorkbook();
        //创建表
        return common(fileName,begin,workbook,Excel2007L);
    }

    /**
     * SXSSF 是 XSSF API的兼容流式扩展，主要解决当使用 XSSF 方式导出大数据量时，内存溢出的问题，支持导出大批量的excel数据
     * @param fileName
     * @return
     */
    public static double sxssfWorkBookTest(String fileName){
        //1.导入apace poi的依赖
        //1.1 poi(03版excel) poi-ooxml(07版excel)
        //2.xssf（07）方式进行导出
        //记录时间
        long begin = System.currentTimeMillis();
        //创建工作普
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        return common(fileName,begin,workbook,Excel2007L);
    }
}
