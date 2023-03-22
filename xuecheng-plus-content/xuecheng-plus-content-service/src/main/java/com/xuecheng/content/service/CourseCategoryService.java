package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @author Ke_han
 * @creat 2023-03-22 21:52
 */
public interface CourseCategoryService {

    List<CourseCategoryTreeDto> queryTreeNode(String id);
}
