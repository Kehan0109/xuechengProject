package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;

/**
 * <p>
 * 课程基本信息 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface CourseBaseMapper extends BaseMapper<CourseBase> {

    void selectList(QueryWrapper<CourseTeacher> queryWrapper);
}
