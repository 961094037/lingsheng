package com.example.demo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.TextEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author hang
 * @email 961094037@qq.com
 * @date 2020-04-24 23:02:04
 */
@Mapper
public interface TextDao extends BaseMapper<TextEntity> {

    IPage<TextEntity> selectPageVo(Page<?> page);
}
