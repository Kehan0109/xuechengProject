package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ke_han
 * @creat 2023-03-22 21:52
 */
@Service
@Slf4j
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Resource
    CourseCategoryMapper courseCategoryMapper;

    /**
     * 将查询结果先找出一级节点，然后将该节点的子节点放入该节点的childrenTreeNodes属性中（是一个list）
     */
    @Override
    public List<CourseCategoryTreeDto> queryTreeNode(String id) {
        //调用mapper递归查询出分类信息
        List<CourseCategoryTreeDto> queryResult = courseCategoryMapper.selectTreeNodes(id);

        //找到每个节点的子节点，最终封装成List<CourseCategoryTreeDto>类型
        //先将list转成map，key就是结点的id，value就是CourseCategoryTreeDto对象，目的为了方便从map中获取节点
        /**
         * filter(item ->!id.equals(item.getId()))：排除根节点
         */
        Map<String, CourseCategoryTreeDto> map = queryResult.stream().filter(item -> !id.equals(item.getId()))
                .collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));

        //定义一个List作为最终返回的List
        ArrayList<CourseCategoryTreeDto> returnList = new ArrayList<>();

        //从头遍历查询结果queryResult，边遍历边找子节点并放在父节点的childrenTreeNodes中
        /**
         * filter(item ->!id.equals(item.getId()))：排除根节点
         */
        queryResult.stream().filter(item -> !id.equals(item.getId())).forEach(item -> {
            /**
             * 向结果集中写入元素
             * 写入的条件：当期节点的父节点就是查询的节点（一级节点写入）
             * 作用：排除二级节点以外的节点，
             *  如：查询id = 1 ，
             *      1-1 的 parentId = 1 写入结果集，
             *      1-1-1 的parentId = 1-1 不写入（该节点要作为属性写入 1-1的 childrenTreeNodes属性中）
             */
            if (item.getParentid().equals(id)) {
                returnList.add(item);
            }
            //找到当前节点的父节点
            CourseCategoryTreeDto courseCategoryParent = map.get(item.getParentid());
            //存在父节点
            if (courseCategoryParent != null) {
                /**
                 * 如果此父节点的childrenTreeNodes为空（还没有放过子节点），
                 * 要 new一个集合来存放子节点
                 */
                if (courseCategoryParent.getChildrenTreeNodes() == null) {
                    courseCategoryParent.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                //找到该节点的子节点放在父节点的childrenTreeNode属性（List）中
                courseCategoryParent.getChildrenTreeNodes().add(item);
            }
        });

        return returnList;

    }
}
