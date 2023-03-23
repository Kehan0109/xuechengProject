package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Ke_han
 * @creat 2023-03-23 14:27
 */
@SpringBootTest
public class CourseCategoryServiceTests {

    @Resource
    CourseCategoryService courseCategoryService;

    @Test
    public void testCourseCategoryService(){

        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeNode("1");
        System.out.println(courseCategoryTreeDtos);
    }

}
