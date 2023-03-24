package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Ke_han
 * @creat 2023-03-24 11:13
 */
@Data
public class EditCourseDto extends AddCourseDto{

    @ApiModelProperty(value = "课程id",required = true)
    private Long id;
}
