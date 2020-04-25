package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.enums.ApiErrorCode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dao.TextDao;
import com.example.demo.entity.TextEntity;
import com.example.demo.entity.dto.TextAddDto;
import com.example.demo.entity.dto.TextEditDto;
import com.example.demo.service.TextService;
import com.example.demo.utils.RRException;
import com.example.demo.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.UUID;

@Service("textService")
public class TextServiceImpl extends ServiceImpl<TextDao, TextEntity> implements TextService {

    @Value("${text.basePath}")
    private String basePath;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public R saveText(TextAddDto textAddDto) {

        checkFileFold();
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

    @Override
    public R editText(TextEditDto textEditDto) {
        String oldAuth = redisUtils.get(textEditDto.getTextId());
        if (StringUtils.isEmpty(oldAuth)){
            String auth = UUID.randomUUID().toString();
            redisUtils.set(textEditDto.getTextId(), auth, 60);
            return R.ok(auth);
        }else if(oldAuth.equals(textEditDto.getAuth())){
            return R.ok(textEditDto.getAuth());
        }else {
            return R.failed("该文件正在被他人修改");
        }
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public R updateText(TextEditDto textEditDto) {
        TextEntity entity = getById(textEditDto.getTextId());
        if (entity != null){
            checkFileFold();
            String oldAuth = redisUtils.get(textEditDto.getTextId());
            if (StringUtils.isEmpty(oldAuth)){
                entity.setTextValue(textEditDto.getTextValue());
                this.updateById(entity);
                updateTextFile(entity.getTextUrl(), textEditDto.getTextValue());
                return R.ok("");
            }else if (oldAuth.equals(textEditDto.getAuth())){
                entity.setTextValue(textEditDto.getTextValue());
                this.updateById(entity);
                updateTextFile(entity.getTextUrl(), textEditDto.getTextValue());
                redisUtils.delete(entity.getTextId());
                return R.ok("");
            }else {
                return R.failed("该文件正在被他人修改");
            }
        }else {
            return R.failed("该文件不存在");
        }
    }

    /**
     * 修改文本文件
     */
    private void updateTextFile(String textUrl, String textValue){
        File file = new File(textUrl);
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RRException("更新文本失败");
            }
        }
        try {
            FileOutputStream ps = new FileOutputStream(file);
            ps.write(textValue.getBytes());
            ps.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
            throw new RRException("修改文本失败");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RRException("修改文本失败");
        }
    }

    @Override
    public void checkFileFold(){
        File file = new File(basePath);
        if (!file.exists()){
            file.mkdirs();
        }
    }
}