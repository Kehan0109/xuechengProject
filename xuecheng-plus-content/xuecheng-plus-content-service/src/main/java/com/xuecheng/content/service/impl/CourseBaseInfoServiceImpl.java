package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Ke_han
 * @creat 2023-03-22 19:07
 */
@Service
@Slf4j
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Resource
    CourseBaseMapper courseBaseMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams,QueryCourseParamDto queryCourseParamDto) {

        //使用mybatis plus拼接查询条件

        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        //课程名的模糊查询：
        queryWrapper.like(StringUtils.isNotEmpty
                (queryCourseParamDto.getCourseName()),CourseBase::getName, queryCourseParamDto.getCourseName());

        //发布状态的精确查询：
        queryWrapper.eq(StringUtils.isNotEmpty
                (queryCourseParamDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamDto.getPublishStatus());


        //审核状态的精确查询
        queryWrapper.eq(StringUtils.isNotEmpty
                (queryCourseParamDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamDto.getAuditStatus());


        //创建page对象，添加分页查询的参数
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(),pageParams.getPageSize());

        //开始进行分页查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);

        //items：
        List<CourseBase> items = pageResult.getRecords();

        //counts:
        long counts = pageResult.getTotal();

        //创建要返回的对象
        PageResult<CourseBase> result = new PageResult<>(items,counts,pageParams.getPageNo(),pageParams.getPageSize());

        return result;
    }
}
