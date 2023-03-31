package com.xuecheng.content.model.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xuecheng.base.exception.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

/**
 * @author Ke_han
 * @creat 2023-03-26 19:37
 */
@Data
@ApiModel(value = "AddCourseTeacherDto", description = "新增任课教师基本信息")
public class AddCourseTeacherDto {

    @ApiModelProperty(value = "任课id",required = true)
    private Long courseId;

    @NotEmpty(message = "新增教师姓名不能为空",groups = {ValidationGroups.Insert.class})
    @ApiModelProperty(value = "教师姓名",required = true)
    private String teacherName;

    @NotEmpty(message = "新增教师职位不能为空",groups = {ValidationGroups.Insert.class})
    @ApiModelProperty(value = "教师职位",required = true)
    private String position;

    @ApiModelProperty("教师简介")
    private String introduction;

}
