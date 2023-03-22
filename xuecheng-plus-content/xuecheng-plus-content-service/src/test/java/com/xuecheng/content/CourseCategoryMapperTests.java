package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Ke_han
 * @creat 2023-03-22 21:45
 */
@SpringBootTest
public class CourseCategoryMapperTests {

    @Resource
    CourseCategoryMapper courseCategoryMapper;




    @Test
    public void testCourseCategoryMapperTests(){


        List<CourseCategoryTreeDto> list = courseCategoryMapper.selectTreeNodes("1");
        System.out.println(list);
    }


}
