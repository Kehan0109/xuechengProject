package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import jdk.nashorn.internal.runtime.regexp.joni.constants.CCSTATE;
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
 * @creat 2023-03-22 19:07
 */
@Service
@Slf4j
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Resource
    CourseBaseMapper courseBaseMapper;

    @Resource
    CourseMarketMapper courseMarketMapper;

    @Resource
    CourseCategoryMapper courseCategoryMapper;

    /**
     * 课程信息分页查询方法
     * @param pageParams
     * @param queryCourseParamDto
     * @return
     */
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamDto queryCourseParamDto) {

        //使用mybatis plus拼接查询条件

        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        //课程名的模糊查询：
        queryWrapper.like(StringUtils.isNotEmpty
                (queryCourseParamDto.getCourseName()), CourseBase::getName, queryCourseParamDto.getCourseName());

        //发布状态的精确查询：
        queryWrapper.eq(StringUtils.isNotEmpty
                (queryCourseParamDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamDto.getPublishStatus());


        //审核状态的精确查询
        queryWrapper.eq(StringUtils.isNotEmpty
                (queryCourseParamDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamDto.getAuditStatus());


        //创建page对象，添加分页查询的参数
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        //开始进行分页查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);

        //items：
        List<CourseBase> items = pageResult.getRecords();

        //counts:
        long counts = pageResult.getTotal();

        //创建要返回的对象
        PageResult<CourseBase> result = new PageResult<>(items, counts, pageParams.getPageNo(), pageParams.getPageSize());

        return result;
    }

    /**
     * 新建课程信息方法：
     * @param companyId 机构 id
     * @param addCourseDto 课程信息：基本信息和营销信息
     * @return
     * @mind  - 参数合法性校验
     *        - 将课程基本信息插入基本信息表 ：
     *          - 封装 CourseBase对象：使用BeanUtils.copyProperties批量覆盖，设置默认属性和防止覆盖属性
     *          - 调用courseBaseMapper.insert()方法插入数据表(course_base)
     *        - 将课程营销信息插入营销信息表
     *          - 封装 CourseMarket对象：同上
     *          - 将封装好的对象放到数据表：具体逻辑在saveCourseMarket()中
     *              - 新建：
     *              - 更新：
     *        - 查询最后结果封装CourseBaseInfoDto返回
     */
    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {

        //mind ①参数的合法性校验
//        if (StringUtils.isBlank(addCourseDto.getName())) {
////            throw new RuntimeException("课程名称为空");
//            XueChengPlusException.cast("课程名称为空");
//        }
//        if (StringUtils.isBlank(addCourseDto.getUsers())) {
//            throw new RuntimeException("适用人群为空");
//        }
//        if (StringUtils.isBlank(addCourseDto.getMt())) {
//            throw new RuntimeException("课程分类为空");
//        }
//        if (StringUtils.isBlank(addCourseDto.getSt())) {
//            throw new RuntimeException("课程分类为空");
//        }
//        if (StringUtils.isBlank(addCourseDto.getGrade())) {
//            throw new RuntimeException("课程等级为空");
//        }
//        if (StringUtils.isBlank(addCourseDto.getCharge())) {
//            throw new RuntimeException("收费规则为空");
//        }
//        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
//            throw new RuntimeException("教育模式为空");
//        }

        //mind ② 向课程基本信息表course_base表写入数据
        CourseBase courseBase = new CourseBase();
        //将传入的页面的参数方到courseBase中
        //比较复杂
//        courseBase.setName(addCourseDto.getName());
        //使用工具类
        BeanUtils.copyProperties(addCourseDto, courseBase);//只要属性名称一致就可以拷贝，新的数据会覆盖老的数据
        //放在copy的后面防止覆盖
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        //默认审核状态为未提交
        courseBase.setAuditStatus("202002");
        //默认发布状态为未发布
        courseBase.setStatus("203001");

        //插入数据库
        int insert = courseBaseMapper.insert(courseBase);
        if (insert <= 0) {
            throw new RuntimeException("添加课程失败");
        }



        //mind ③ 向课程营销表course_market表写入数据
        CourseMarket courseMarket = new CourseMarket();
        //将页面输入的数据copy到courseMarket中
        BeanUtils.copyProperties(addCourseDto, courseMarket);
        //设置主键：课程id
        courseMarket.setId(courseBase.getId());
        //保存营销信息
        saveCourseMarket(courseMarket);
        //从数据库查出课程的详细信息：包括两部分：courseBase courseMarket
        //courseBaseInfo包含插入数据库的courseBase 和 新建或更新的 courseMarket
        // 其中courseBase中没有mt和st name，均在getCourseBaseInfo()设置好了
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(courseBase.getId());

        return courseBaseInfo;
    }

    /**
     * @description 保存营销信息的方法
     * @param courseMarket
     * @return
     * @mind - 参数的合法性校验
     *          - 是否填写收费规则
     *          - 填写收费时：课程价格是否为 0
     *       - 数据库查询该营销信息是否存在
     *          - 不存在则新建：调用courseMarketMapper.insert()直接新建即可
     *          - 存在则更新：注意更新结果的覆盖问题，用
     */
    private int saveCourseMarket(CourseMarket courseMarket) {

        //参数的合法性校验
        if (StringUtils.isEmpty(courseMarket.getCharge())) {
            new RuntimeException("收费规则为空");
        }

        //如果课程收费，价格没填写
        if (courseMarket.equals("201001")) {
            if (courseMarket.getPrice() == null || courseMarket.getPrice() <= 0) {
                new RuntimeException("课程价格不能为空且必须大于0");
            }
        }
        //从数据库查询营销信息，存在就更新，不存在就添加
        CourseMarket courseMarketResult = courseMarketMapper.selectById(courseMarket.getId());
        if (courseMarketResult == null) {
            //数据不存在，则新建，用传入的对象直接新建即可
            int insert = courseMarketMapper.insert(courseMarket);
            return insert;

        } else {
            /**数据存在：则更新
             * 查询结果：courseMarketResult，
             * 更新数据：courseMarket
             * 方法：由于传入进来的courseMarket可能为 null，所以要将courseMarket的属性 copy到 courseMarketResult中
             */
            BeanUtils.copyProperties(courseMarket, courseMarketResult);
//            courseMarketResult.setId(courseMarket.getId());
            //更新
            int i = courseMarketMapper.updateById(courseMarketResult);
            return i;

        }
    }

    //查询课程信息
    private CourseBaseInfoDto getCourseBaseInfo(Long courseId) {

        //从课程基本信息表查询
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }
        //从营销信息表查询
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        //组装成CourseBaseInfoDto
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if(courseMarket!=null){
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }
        //通过courseCategoryMapper查询分类信息，然后设置到courseBaseInfoDto中
        //查询
        CourseCategory mt = courseCategoryMapper.selectById(courseBase.getMt());
        //设置mt的name
        courseBaseInfoDto.setMtName(mt.getName());
        //查询小分类st
        CourseCategory st = courseCategoryMapper.selectById(courseBase.getSt());
        //设置st的name
        courseBaseInfoDto.setStName(st.getName());
        //返回
        return courseBaseInfoDto;
    }
}
