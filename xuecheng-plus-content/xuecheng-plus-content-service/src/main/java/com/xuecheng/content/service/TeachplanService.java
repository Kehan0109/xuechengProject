package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.TeachplanMedia;

import java.util.List;

/**
 * @author Ke_han
 * @creat 2023-03-24 14:14
 * @description 课程计划管理相关接口
 */
public interface TeachplanService {

    List<TeachplanDto> findTeachplanTree(Long id);

    void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    void deleteTeachplan(Long id);

    void moveDownTeahplan(Long id);
}