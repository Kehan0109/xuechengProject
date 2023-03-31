package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BEncoderStream;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.AddCourseTeacherDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.CourseTeacherService;
import javafx.scene.shape.CircleBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Ke_han
 * @creat 2023-03-26 19:12
 */
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Resource
    CourseTeacherMapper courseTeacherMapper;

    @Resource
    CourseBaseMapper courseBaseMapper;


    /**
     * 查询当前课程的教师信息
     *
     * @param courseId
     * @return
     */
    @Override
    public List<CourseTeacher> getCourseTeacher(Long courseId) {
        QueryWrapper<CourseTeacher> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(queryWrapper);
        return courseTeachers;
    }

    /**
     * 添加、修改当前课程的教师信息
     * @param courseTeacher
     * @return
     */
    @Override
    public CourseTeacher saveCourseTeacher(CourseTeacher courseTeacher, Long companyId) {
        Long id = courseTeacher.getId();
        //判断当前是添加操作还是修改操作
        if (id == null) {
            //添加操作
            CourseTeacher courseTeacherInsert = new CourseTeacher();
            BeanUtils.copyProperties(courseTeacher, courseTeacherInsert);
            courseTeacherInsert.setCreateDate(LocalDateTime.now());
            courseTeacherMapper.insert(courseTeacherInsert);
            return courseTeacherInsert;
        } else {
            //修改操作
            Long courseId = courseTeacher.getCourseId();
            QueryWrapper<CourseBase> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("company_id", companyId);
            List<CourseBase> courseBases = courseBaseMapper.selectList(queryWrapper);
            if (courseBases == null) {
                XueChengPlusException.cast("请修改本机构的教师");
                return null;
            } else {
                CourseTeacher courseTeacherUpdate = courseTeacherMapper.selectById(id);
                BeanUtils.copyProperties(courseTeacher, courseTeacherUpdate);
                courseTeacherMapper.updateById(courseTeacherUpdate);
                return courseTeacherUpdate;
            }
        }
    }

//    /**
//     * 修改课程的任课教师信息
//     * @param courseTeacher
//     * @return
//     * @attention 只能修改自己机构老师的信息
//     */
//    @Override
//    public CourseTeacher updateCourseTeacher(CourseTeacher courseTeacher,Long companyId) {
//        Long courseId = courseTeacher.getCourseId();
//        QueryWrapper<CourseBase> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("company_id",companyId);
//        List<CourseBase> courseBases = courseBaseMapper.selectList(queryWrapper);
//        if (courseBases == null){
//            XueChengPlusException.cast("该老师不属于本机构，无法修改！");
//        }else {
//            Long id = courseTeacher.getId();
//            CourseTeacher courseTeacherNew = courseTeacherMapper.selectById(id);
//            BeanUtils.copyProperties(courseTeacher,courseTeacherNew);
//            courseTeacherMapper.updateById(courseTeacherNew);
//        }
//        return courseTeacher;
//    }
}
