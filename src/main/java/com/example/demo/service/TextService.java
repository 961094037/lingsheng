package com.example.demo.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.TextEntity;
import com.example.demo.entity.dto.TextAddDto;
import com.example.demo.entity.dto.TextEditDto;

/**
 * 
 *
 * @author hang
 * @email 961094037@qq.com
 * @date 2020-04-24 23:02:04
 */
public interface TextService extends IService<TextEntity> {

    R saveText(TextAddDto textAddDto);

    IPage<TextEntity> selectTextPage(Page<TextEntity> page);

    R editText(TextEditDto textEditDto);

    R updateText(TextEditDto textEditDto);

    void checkFileFold();
}

