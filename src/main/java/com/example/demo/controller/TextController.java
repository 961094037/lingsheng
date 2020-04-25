package com.example.demo.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Arrays;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.IErrorCode;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.enums.ApiErrorCode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.TextEntity;
import com.example.demo.entity.dto.TextAddDto;
import com.example.demo.entity.dto.TextEditDto;
import com.example.demo.service.TextService;
import com.example.demo.utils.RRException;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    @ApiOperation(value = "列表接口")
    public IPage<TextEntity> list(@RequestBody Page page){
        return textService.selectTextPage(page);
    }

    /**
     * 信息
     */
    @GetMapping("/info/{textId}")
    @ApiOperation(value = "详情接口")
    public R info(@PathVariable("textId") String textId){
		TextEntity text = textService.getById(textId);
        return R.ok(text);
    }

    /**
     * 新增接口
     */
    @PostMapping("/save")
    @ApiOperation(value = "新增接口")
    public R save(@RequestBody @Valid TextAddDto text){
		textService.saveText(text);
        return R.ok("");
    }

    /**
     * 编辑接口
     */
    @GetMapping("/edit")
    @ApiOperation(value = "编辑接口")
    public R edit(@RequestParam("textId") String textId){
        return textService.editText(textId);
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    @ApiOperation(value = "修改接口")
    public R update(@RequestBody @Valid TextEditDto text){
        return textService.updateText(text);
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete")
    @ApiOperation(value = "删除接口")
    public R delete(@RequestBody Integer[] textIds){
		textService.removeByIds(Arrays.asList(textIds));
        return R.ok("");
    }

    @ApiOperation(value = "下载文本")
    @GetMapping("/download/{textId}")
    public R appQRCode(@PathVariable String textId, HttpServletRequest request, HttpServletResponse response) {

        TextEntity textEntity = textService.getById(textId);
        if (textEntity == null){
            return R.failed("该文件不存在");
        }
        File file = new File(textEntity.getTextUrl());
        if (!file.exists()){
            return R.failed("该文件不存在");
        }
        try {
            // 设置要下载的文件的名称
            response.setHeader("Content-disposition", "attachment;fileName=" + file.getName().getBytes());
            // 通知客服文件的MIME类型
            response.setContentType("text/plain;charset=UTF-8");
            InputStream input = new FileInputStream(file);
            OutputStream out = response.getOutputStream();
            byte[] b = new byte[1024];
            int len;
            while ((len = input.read(b)) != -1) {
                out.write(b, 0, len);
            }
            input.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RRException("获取文件异常");
        }
        return R.ok("");
    }

}
