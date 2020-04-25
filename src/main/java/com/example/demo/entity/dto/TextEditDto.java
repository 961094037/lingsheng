package com.example.demo.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @Author: 翁舒航
 * @Description: ${description}
 * @Date: 2020-04-25 13:09
 * @Version: 1.0
 */
@Data
public class TextEditDto {

    @NotEmpty
    private String textId;

    @NotEmpty
    private String textValue;

    @NotEmpty
    private String auth;
}
