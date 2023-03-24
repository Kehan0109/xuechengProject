package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.List;

/**
 * @author Ke_han
 * @creat 2023-03-24 13:16
 * @description 课程计划信息的模型类
 */
@Data
@ToString
public class TeachplanDto extends Teachplan {


    //与媒资关联的信息
    private TeachplanMedia teachplanMedia;

    //小章节List
    private List<TeachplanDto> teachPlanTreeNodes;
}
