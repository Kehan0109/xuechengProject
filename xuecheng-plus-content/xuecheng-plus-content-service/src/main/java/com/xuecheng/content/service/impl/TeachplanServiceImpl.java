package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import net.sf.jsqlparser.statement.Commit;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.awt.geom.AreaOp;
import sun.awt.windows.WPrinterJob;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Ke_han
 * @creat 2023-03-24 14:15
 */
@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Resource
    TeachplanMapper teachplanMapper;

    @Resource
    TeachplanMediaMapper teachplanMediaMapper;

    @Resource
    DataSourceTransactionManager dataSourceTransactionManager;

    /**
     * 根据课程id查询课程计划
     *
     * @param courseId
     * @return
     */
    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachplanDtos;
    }

    /**
     * 新增、修改、保存 课程计划
     *
     * @param saveTeachplanDto
     */
    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        //通过课程计划的id判断是新增操作还是修改操作
        Long teachplanId = saveTeachplanDto.getId();
        if (teachplanId == null) {
            //新增操作
            Teachplan teachplan = new Teachplan();
            //封装数据
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            Long courseId = saveTeachplanDto.getCourseId();
            Long parentId = saveTeachplanDto.getParentid();
            int orderBy = getTeachplanCount(courseId, parentId);
            teachplan.setOrderby(orderBy+1);
            teachplanMapper.insert(teachplan);
            //更新新数据
            teachplanMapper.selectById(teachplanId);

        } else {
            //修改操作
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            //将封装数据
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);

            Long courseId = saveTeachplanDto.getCourseId();
            Long parentId = saveTeachplanDto.getParentid();

            //调用方法查询当前的同级节点个数，设置orderBy参数
            int orderBy = getTeachplanCount(courseId, parentId);
            //设置orderBy属性
            teachplan.setOrderby(orderBy+1);
            //修改
            teachplanMapper.updateById(teachplan);

            teachplanMapper.selectById(teachplanId);

        }
    }

    @Override
    public void deleteTeachplan(Long id) {
        /* 判断当前id的级别：
            - 如果是大章节要查看是否包含小章节
            - 如果是小章节要在删除的同时删除关联的teachplan_media信息 关联数据teachplan id = teachplan_media teachplan_id
         */
        //先根据id查询到数据
        Teachplan teachplan = teachplanMapper.selectById(id);
        Long parentId = teachplan.getParentid();
        //大章节
        if (parentId == 0){
            QueryWrapper<Teachplan> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("parentid",id);
            Teachplan containSecond = teachplanMapper.selectOne(queryWrapper);
            //包含小章节
            if (containSecond != null){
                XueChengPlusException.cast("请先删除该章节中包含的小章节后重试");
            //不包含小章节
            }else {
                teachplanMapper.deleteById(id);
            }
        //小章节
        } else {
            //查询在teachplan_media表中是否存在内容：存在则删除
            QueryWrapper<TeachplanMedia> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("teachplan_id", id);
            TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(queryWrapper);
            //查询到了数据，删除关联的数据
            if (teachplanMedia != null) {
                teachplanMediaMapper.deleteById(teachplanMedia.getId());
            }
            //删除teachplan表的内容
            teachplanMapper.deleteById(id);

        }
    }

    /**
     * 向下移动课程计划
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveDownTeahplan(Long id) {
        //根据id查询到对象
        Teachplan teachplan = teachplanMapper.selectById(id);
        //获取排序信息
        Integer orderby = teachplan.getOrderby();
        Long courseId = teachplan.getCourseId();
        Long parentid = teachplan.getParentid();
        int teachplanCount = getTeachplanCount(courseId, parentid);
        //判断是否是最后一条数据
        if (orderby > teachplanCount) {
            XueChengPlusException.cast("本条数据是最后一条，无法下移");
        }else {
            //定义新的orderby
            int newOrderby = orderby + 1;
            //先将下一个设置为老的orderby，如果后设置就会根据parentid和orderby查询到两个（新移动的和老的orderby会相同）
            QueryWrapper<Teachplan> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("parentid", parentid)
                    .and(wp -> wp.eq("orderby", newOrderby))
                    .and(wq -> wq.eq("course_id", courseId));
            Teachplan oldTeachplan = teachplanMapper.selectOne(queryWrapper);
            oldTeachplan.setOrderby(orderby);
            teachplanMapper.updateById(oldTeachplan);

            Teachplan teachplanDown = new Teachplan();
            // 向下移动的操作
            //封装对象
            BeanUtils.copyProperties(teachplan, teachplanDown);
            //设置新的orderBy
            teachplanDown.setOrderby(newOrderby);
            teachplanMapper.updateById(teachplanDown);
            //排第一个的数据不做操作
        }
    }

    /**
     * 向上移动课程计划
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveUpTeachplan(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        Long parentid = teachplan.getParentid();
        Long courseId = teachplan.getCourseId();
        Integer orderby = teachplan.getOrderby();
        //如果orderby - 1 存在 说明不是第一条数据，如果不存在则是
        QueryWrapper<Teachplan> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("parentid",parentid)
                .and(wq -> wq.eq("course_id",courseId))
                .and(wq -> wq.eq("orderby",(orderby-1)));
        Teachplan teachplanOld = teachplanMapper.selectOne(queryWrapper);
        if(teachplanOld == null){
            XueChengPlusException.cast("本条数据是第一条数据，无法向上移动");
        }else {
            //先将上面的数据向下移动
            teachplanOld.setOrderby(orderby);
            teachplanMapper.updateById(teachplanOld);
            //再将下面的数据向上移动
            teachplan.setOrderby(orderby-1);
            teachplanMapper.updateById(teachplan);
        }

    }

    //绑定媒资信息
    @Transactional
    @Override
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {

        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan == null){
            XueChengPlusException.cast("课程计划不存在");
        }

        //删除原有绑定媒资记录，根据课程计划的id删除绑定的媒资
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanMedia::getTeachplanId,bindTeachplanMediaDto.getTeachplanId());
        //删除原有的记录
        teachplanMediaMapper.delete(queryWrapper);

        //添加新的
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachplanMediaDto,teachplanMedia);
        //设置course_id
        teachplanMedia.setCourseId(teachplan.getCourseId());
        //设置MediaFileName
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMediaMapper.insert(teachplanMedia);

    }

    /**
     * 确定排序字段，找到同级节点的个数，排序字段就是个数+1
     * 在大章节中添加：
     * select count(*) from teachplan where course_id = 117 and parent_id = 0;
     * 在小章节中添加：
     * select count(*) from teachplan where course_id = 117 and parent_id = 268;
     *
     * @param courseId
     * @param parentId
     * @return
     */
    private int getTeachplanCount(Long courseId, Long parentId) {

        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);

        return count;

    }
}
