package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.AddCourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * @author Ke_han
 * @creat 2023-03-26 19:11
 */
public interface CourseTeacherService {

    //查询课程的教师信息
    List<CourseTeacher> getCourseTeacher(Long courseId);

    //添加课程的任课教师信息
    CourseTeacher saveCourseTeacher(CourseTeacher courseTeacher,Long companyId);

//    //修改课程的任课教师信息
//    CourseTeacher updateCourseTeacher(CourseTeacher courseTeacher,Long companyId);
}
