package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
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


    /**
     * 根据课程 id查询课程信息
     * @param courseId
     * @return 课程详细信息
     */
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * 修改信息
     * @param editCourseDto
     * @return 课程详细信息
     */
    CourseBaseInfoDto updateCourseBase(Long companyId,EditCourseDto editCourseDto);
}
