package com.xuecheng.content.api;

import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Ke_han
 * @creat 2023-03-24 13:19
 */
@RestController
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
public class TeachplanController {

    @Resource
    TeachplanService teachplanService;

    //查询课程计划
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    @ApiOperation("查询课程计划树形结构")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId){

        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);

        return teachplanTree;
    }

    //
    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto saveTeachplanDto){

        teachplanService.saveTeachplan(saveTeachplanDto);
    }


    //删除大章节或小章节及小章节关联数据
    @ApiOperation("课程计划删除")
    @DeleteMapping("/teachplan/{id}")
    public void deleteTechplan(@PathVariable Long id){

        teachplanService.deleteTeachplan(id);
    }

    //
    @ApiOperation("章节向下移动")
    @PostMapping("/teachplan/movedown/{id}")
    public void moveDownTechplan(@PathVariable Long id){

        teachplanService.moveDownTeahplan(id);
    }

}
