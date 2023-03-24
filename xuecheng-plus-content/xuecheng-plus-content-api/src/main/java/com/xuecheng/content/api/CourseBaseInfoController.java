package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Ke_han
 * @creat 2023-03-22 17:05
 *
 * 400 -> 请求数据与接口不匹配
 */
@RestController //@Controller + @ResponseBody :响应JSON数据给浏览器端
@Api(value = "课程信息管理接口",tags = "课程信息管理接口")
public class CourseBaseInfoController {

    @Resource
    CourseBaseInfoService courseBaseInfoService;




    /**
     * 请求 URL：POST /content/course/list?pageNo=2&pageSize=1
     * 请求数据类型：
     * Content-Type: application/json
     * {
     *   "auditStatus": "202002",
     *   "courseName": "",
     *   "publishStatus":""
     * }
     *
     * @param pageParams：使用 post方法提交的数据
     * @param queryCourseParamDto：使用 @RequestBody将 JSON转成对象，对应 {}中的 JSON对象
     * @return
     */
    @PostMapping("/course/list")
    @ApiOperation("课程查询接口")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamDto queryCourseParamDto){

        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParamDto);
        return courseBasePageResult;
    }


    @PostMapping("/course")
    @ApiOperation("新增课程")
    public CourseBaseInfoDto createCourseBase
            (@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto addCourseDto){

        Long companyId  = 1232141425L;
        //获取到用户所属机构的id
        CourseBaseInfoDto courseBase = courseBaseInfoService.createCourseBase(companyId, addCourseDto);

        return courseBase;
    }

    @GetMapping("/course/{courseId}")
    @ApiOperation("根据课程id查询")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId){

        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        return courseBaseInfo;
    }


    @PutMapping("/course")
    @ApiModelProperty("修改课程信息")
    public CourseBaseInfoDto modifyCourseBase
            (@RequestBody @Validated({ValidationGroups.Update.class}) EditCourseDto editCourseDto){

        //获取到用户所属机构的id
        Long companyId  = 1232141425L;
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.updateCourseBase(companyId, editCourseDto);

        return courseBaseInfoDto;
    }
}
