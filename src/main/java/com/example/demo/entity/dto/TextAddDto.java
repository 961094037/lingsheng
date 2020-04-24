package com.example.demo.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * @Author: 翁舒航
 * @Description: ${description}
 * @Date: 2020-04-24 23:14
 * @Version: 1.0
 */
@Data
public class TextAddDto {

    /**
     * 文本名称
     */
    @NotEmpty(message = "请输入文件名")
    @ApiModelProperty(required = true, name = "文本名称")
    @Size(min=1, max=100, message = "请输入长度100以内的文件名")
    private String textName;

    /**
     * 文本值
     */
    @NotEmpty(message = "请输入文本内容")
    @ApiModelProperty(required = true, name = "文本内容")
    @Size(min=1, max=65535, message = "请输入长度60000以内的文本内容")
    private String textValue;
}
