package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.enums.ApiErrorCode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dao.TextDao;
import com.example.demo.entity.TextEntity;
import com.example.demo.entity.dto.TextAddDto;
import com.example.demo.service.TextService;
import com.example.demo.utils.RRException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.UUID;

@Service("textService")
public class TextServiceImpl extends ServiceImpl<TextDao, TextEntity> implements TextService {

    @Value("${text.basePath}")
    private String basePath;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public R saveText(TextAddDto textAddDto) {



        File file = new File(basePath);
        if (!file.exists()){
            file.mkdirs();
        }
        String textId = UUID.randomUUID().toString();
        String fileName = textAddDto.getTextName() + "_" + textId + ".txt";
        String fileUrl = basePath + "\\" + fileName;
        File newFile = new File(fileUrl);
        if (!newFile.exists()){
            try {
                newFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RRException("添加文本失败");
            }
        }
        try {
            FileOutputStream ps = new FileOutputStream(newFile);
            ps.write(textAddDto.getTextValue().getBytes());
            ps.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
            defeated(newFile);
        } catch (IOException e) {
            e.printStackTrace();
            defeated(newFile);
        }

        try {
            TextEntity textEntity = new TextEntity();
            textEntity.setTextId(textId);
            textEntity.setTextName(textAddDto.getTextName());
            textEntity.setTextValue(textAddDto.getTextValue());
            textEntity.setTextUrl(fileUrl);
            this.save(textEntity);
        }catch (Exception e){
            e.printStackTrace();
            defeated(newFile);
        }
        return R.ok("");
    }

    private R defeated(File newFile){
        if (newFile.exists()){
            newFile.delete();
        }
        throw new RRException("添加文本失败");
    }

    @Override
    public IPage<TextEntity> selectTextPage(Page<TextEntity> page) {
        return getBaseMapper().selectPageVo(page);
    }
}