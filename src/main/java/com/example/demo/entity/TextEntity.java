package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.core.annotation.Order;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 * 
 * 
 * @author hang
 * @email 961094037@qq.com
 * @date 2020-04-24 23:02:04
 */
@Data
@TableName("text")
public class TextEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@TableId
	private String textId;

	/**
	 * 文本名称
	 */
	private String textName;

	/**
	 * 文本值
	 */
	private String textValue;

	/**
	 * 文本url
	 */
	private String textUrl;

}
