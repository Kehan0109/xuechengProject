package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Ke_han
 * @creat 2023-03-22 21:02
 */
@Data
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {


    //子节点
    List<CourseCategoryTreeDto> childrenTreeNodes;
}
