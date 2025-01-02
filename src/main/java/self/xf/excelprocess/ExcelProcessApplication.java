package self.xf.excelprocess;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import self.xf.excelprocess.util.GlobalSession;

@SpringBootApplication
public class ExcelProcessApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExcelProcessApplication.class, args);

        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            GlobalSession.shutdown();
        }));
    }

}
