package com.example.demo.controller;

import java.util.Arrays;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.IErrorCode;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.enums.ApiErrorCode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.TextEntity;
import com.example.demo.entity.dto.TextAddDto;
import com.example.demo.service.TextService;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


/**
 * 
 *
 * @author hang
 * @email 961094037@qq.com
 * @date 2020-04-24 23:02:04
 */
@RestController
@RequestMapping("text")
public class TextController {

    @Autowired
    private TextService textService;

    /**
     * 列表
     */
    @PostMapping("/list")
    @ApiModelProperty(value = "列表接口")
    public IPage<TextEntity> list(@RequestBody Page page){
        return textService.selectTextPage(page);
    }

    /**
     * 信息
     */
    @GetMapping("/info/{textId}")
    @ApiModelProperty(value = "详情接口")
    public R info(@PathVariable("textId") Integer textId){
		TextEntity text = textService.getById(textId);

        return R.ok("text");
    }

    /**
     * 新增接口
     */
    @PostMapping("/save")
    @ApiModelProperty(value = "新增接口")
    public R save(@RequestBody @Valid TextAddDto text){
		textService.saveText(text);
        return R.ok("");
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    @ApiModelProperty(value = "修改接口")
    public R update(@RequestBody TextEntity text){
		textService.updateById(text);

        return R.ok("");
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete")
    @ApiModelProperty(value = "删除接口")
    public R delete(@RequestBody Integer[] textIds){
		textService.removeByIds(Arrays.asList(textIds));
        return R.ok("");
    }

}
