package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseTeacher;
import lombok.Data;

import java.util.List;

/**
 * @author Ke_han
 * @creat 2023-03-31 14:49
 * 课程预览的属性
 */
@Data
public class CoursePreviewDto {

    //课程基本信息,课程营销信息
    private CourseBaseInfoDto courseBase;


    //课程计划信息
    List<TeachplanDto> teachplans;

    //师资信息
    List<CourseTeacher> courseTeachers;

}
