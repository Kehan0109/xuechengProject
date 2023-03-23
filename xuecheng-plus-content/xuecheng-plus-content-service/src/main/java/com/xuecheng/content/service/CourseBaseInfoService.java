package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamDto;
import com.xuecheng.content.model.po.CourseBase;

import java.util.List;

/**
 * @author Ke_han
 * @creat 2023-03-22 19:04
 * @description 课程信息管理接口
 */
public interface CourseBaseInfoService {

    /**
     * 查询课程
     * @param pageParams
     * @param queryCourseParamDto
     * @return
     */
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamDto queryCourseParamDto);

    /**
     * 新增课程
     * @param companyId 机构 id
     * @param addCourseDto 课程信息
     * @return
     *
     */
    CourseBaseInfoDto createCourseBase(Long companyId ,AddCourseDto addCourseDto);
}
