package com.xuecheng.content.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Ke_han
 * @creat 2023-03-22 16:56
 * @description 课程查询条件模型类
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class QueryCourseParamDto {

    //审核状态
    private String auditStatus;

    //课程名称
    private String courseName;

    //发布状态
    private String publishStatus;
}
