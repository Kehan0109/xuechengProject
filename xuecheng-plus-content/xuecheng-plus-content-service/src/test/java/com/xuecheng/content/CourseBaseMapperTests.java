package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamDto;
import com.xuecheng.content.model.po.CourseBase;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Ke_han
 * @creat 2023-03-22 18:07
 */

@SpringBootTest
public class CourseBaseMapperTests {

    @Resource
    CourseBaseMapper courseBaseMapper;

    @Test
    public void testCourseBaseMapper(){
        CourseBase courseBase = courseBaseMapper.selectById(18);
        Assertions.assertNotNull(courseBase);

        //详细进行分页查询的单元测试
        //查询条件
        QueryCourseParamDto courseParamDto = new QueryCourseParamDto();
        courseParamDto.setCourseName("java");

        //拼装查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        //根据名称模糊查询，在sql中拼接course_base.name like '%值%'
        queryWrapper.like(StringUtils.isNotEmpty(courseParamDto.getCourseName()),
                CourseBase::getName,
                courseParamDto.getCourseName());

        //根据课程审核状态精确查询，在SQL中拼接course_base.audi_status = ?
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamDto.getAuditStatus()),
                CourseBase::getAuditStatus,
                courseParamDto.getAuditStatus());

        //根据课程发布状态精确查询，在SQL中拼接course_base.status = ?
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamDto.getPublishStatus()),
                CourseBase::getStatus,
                courseParamDto.getPublishStatus());


        //创建PageParams的对象:
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(1L);
        pageParams.setPageSize(2L);

        //创建page分页参数的对象: 参数：当前页码，每页记录数
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(),pageParams.getPageSize());

        //开始进行分页查询:
        /**
         * page对象：设置页码和记录数
         * 查询条件：不同的查询条件拼接不同的参数
         */
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);

        //数据列表：items
        /**
         * getRecords():返回List
         */
        List<CourseBase> records = pageResult.getRecords();

        //总记录数：counts
        long total = pageResult.getTotal();


        //Swagger接口返回类中的需求参数：List<T> items, long counts, long page, long pageSize
        PageResult<CourseBase> courseBasePageResult =
                new PageResult<>(records,total,pageParams.getPageNo(),pageParams.getPageSize());
        System.out.println(courseBasePageResult);

    }
}
