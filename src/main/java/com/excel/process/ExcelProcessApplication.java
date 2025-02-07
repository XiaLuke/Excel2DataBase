package com.excel.process;

import com.excel.process.util.file.FileExpiryManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExcelProcessApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExcelProcessApplication.class, args);

        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            FileExpiryManager.shutDown();
        }));
    }

}
