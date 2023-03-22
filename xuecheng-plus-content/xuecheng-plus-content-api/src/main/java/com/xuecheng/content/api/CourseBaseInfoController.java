package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
