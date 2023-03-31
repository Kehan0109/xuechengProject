package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Ke_han
 * @creat 2023-03-31 14:53
 */
@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {

    @Resource
    CourseBaseInfoService courseBaseInfoService;

    @Resource
    TeachplanService teachplanService;

    @Resource
    CourseTeacherService courseTeacherService;

    @Resource
    CourseMarketMapper courseMarketMapper;

    @Resource
    CoursePublishPreMapper coursePublishPreMapper;

    @Resource
    CourseBaseMapper courseBaseMapper;

    @Resource
    CoursePublishMapper coursePublishMapper;



    /**
     * 课程预览
     * @param courseId 课程id
     * @return
     */
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        //查询课程基本信息，营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        //查询课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        //查询课程教师信息
        List<CourseTeacher> courseTeacher = courseTeacherService.getCourseTeacher(courseId);

        //封装数据
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanTree);
        coursePreviewDto.setCourseTeachers(courseTeacher);
        return coursePreviewDto;
    }

    //提交课程信息
    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {

        /*约束：
            课程的审核状态为已提交时，不允许提交
            课程图片、计划信息等没有填写不允许提交
         */

        //查询课程基本信息，营销信息，课程计划，教师信息插入到课程预发布表
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if (courseBaseInfo == null){
            XueChengPlusException.cast("课程找不到");
        }
        //审核状态
        String auditStatus = courseBaseInfo.getAuditStatus();
        //审核状态为已提交，不能提交
        if (auditStatus.equals("202003")){
            XueChengPlusException.cast("课程已提交，请等待审核");
        }
        //图片信息
        String pic = courseBaseInfo.getPic();
        //图片信息为空不能提交
        if (StringUtils.isEmpty(pic)){
            XueChengPlusException.cast("请上传图片");
        }
        //查询课程计划
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        //课程计划不能为空
        if (teachplanTree == null || teachplanTree.size() == 0){
            XueChengPlusException.cast("未发布课程计划，请发布课程计划后，提交课程");
        }
        //查询教师信息
        List<CourseTeacher> courseTeacher = courseTeacherService.getCourseTeacher(courseId);
        //校验教师信息：..........

        //本机构只能提交本机构的课程


        //插入信息到预发布表
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //封装数据
        //课程基本信息
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);
        //营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //转JSON
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);
        //计划信息
        //转JSON
        String teachplanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeJson);
        //教师信息
        //转JSON
        String courseTeacherJson = JSON.toJSONString(courseTeacher);
        coursePublishPre.setTeachers(courseTeacherJson);
        //设置状态为已提交
        coursePublishPre.setStatus("202003");
        //设置提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        //设置机构id
        coursePublishPre.setCompanyId(companyId);
        //查询预发布表，存在则更新，不存在则插入
        CoursePublishPre coursePublishPreOld = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreOld == null){
            //插入到预发布表
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            //更新预发布表
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        //更新课程基本信息表的状态为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //设置审核状态为已提交
        courseBase.setAuditStatus("202003");
        //更新数据表
        courseBaseMapper.updateById(courseBase);


    }

    //课程发布
    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {

        /*约束：
            课程未审核通过，不允许发布
         */

        //查询预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            XueChengPlusException.cast("课程没有审核记录，不可发布");
        }
        //查看审核状态
        String status = coursePublishPre.getStatus();
        //课程未审核通过不允许发布
        if(!status.equals("202004")){
            XueChengPlusException.cast("课程未审核通过，不可发布");
        }
        //封装对象
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        //向课程发布表写数据
        //先查询课程发布表，存在则更新，不存在则插入
        CoursePublish coursePublishOld = coursePublishMapper.selectById(courseId);
        if (coursePublishOld == null){
            //插入
            coursePublishMapper.insert(coursePublish);
        }else{
            //更新
            coursePublishMapper.updateById(coursePublish);
        }
        //设置课程基本信息表状态为发布
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //设置status为发布
        courseBase.setStatus("203002");
        //更新
        courseBaseMapper.updateById(courseBase);

        //向消息表写数据


        //删除课程预发布表的数据
        coursePublishPreMapper.deleteById(courseId);
    }
}
