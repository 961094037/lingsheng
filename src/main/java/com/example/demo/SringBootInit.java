package com.example.demo;

import com.example.demo.service.TextService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;


/**
 * @Author: 翁舒航
 * @Description: ${description}
 * @Date: 2020-04-24 23:20
 * @Version: 1.0
 */
@Log4j2
@Component
public class SringBootInit implements CommandLineRunner {

    @Value("${text.basePath}")
    private String basePath;

    @Autowired
    private TextService textService;

    @Override
    public void run(String... strings) throws Exception {
        textService.checkFileFold();
    }
}
