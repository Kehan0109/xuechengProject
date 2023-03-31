package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.content.model.dto.AddCourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Ke_han
 * @creat 2023-03-26 19:00
 */
@RestController
@Api(value = "教师信息编辑接口",tags = "教师信息编辑接口")
public class CourseTeacherController {

    @Resource
    CourseTeacherService courseTeacherService;

    @ApiOperation("查询教师信息")
    @GetMapping("/courseTeacher/list/{id}")
    public List<CourseTeacher> getCourseTeacher(@PathVariable("id") Long courseId){

        List<CourseTeacher> courseTeacher = courseTeacherService.getCourseTeacher(courseId);

        return courseTeacher;
    }

//    @ApiOperation("修改当前课程的教师信息")
//    @PutMapping("/courseTeacher")
//    public CourseTeacher updateCourseTeacher
//            (@RequestBody @Validated(ValidationGroups.Update.class)CourseTeacher courseTeacher){
//
//        Long companyId = 1232141425L;
//        CourseTeacher updateCourseTeacher = courseTeacherService.updateCourseTeacher(courseTeacher, companyId);
//        return updateCourseTeacher;
//    }

    @ApiOperation("添加修改教师信息")
    @PostMapping("/courseTeacher")
    public CourseTeacher saveCourseTeacher
            (@RequestBody @Validated({ValidationGroups.Insert.class,ValidationGroups.Update.class})CourseTeacher courseTeacher){
        Long companyId = 1232141425L;
        CourseTeacher courseTeacherNew = courseTeacherService.saveCourseTeacher(courseTeacher, companyId);
        return courseTeacherNew;
    }



}
