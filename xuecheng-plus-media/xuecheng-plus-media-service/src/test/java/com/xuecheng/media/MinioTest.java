package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;

/**
 * @author Ke_han
 * @creat 2023-03-27 19:
 * @description 测试 minio 的SDK
 */
public class MinioTest {

    MinioClient minioClient = MinioClient.builder()
            .endpoint("http://192.168.146.1:9000")
            .credentials("minioadmin","minioadmin")
            .build();

    @Test
    public void test_upload() throws Exception {

        //通过扩展名得到媒体资源类型 mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".jpg");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用的mimeType
        if(extensionMatch != null){
            mimeType = extensionMatch.getMimeType();
        }


        //上传文件的参数信息
        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("testbucket")//确定桶
                .filename("C:\\Users\\豚豚\\Pictures\\壁纸\\wallhaven-7pjv3o.jpg")//指定本地文件路径
//                .object("1.jpg")//对象名，在桶下存储该文件
                .object("/test/01/1.jpg")//对象名，放在子目录下
                .contentType(mimeType)//设置媒体文件类型
                .build();

        //上传文件
        minioClient.uploadObject(uploadObjectArgs);

    }

    @Test
    public void test_delete() throws Exception{

        //删除文件的参数信息
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket("testbucket")
                .object("1.jpg")
                .build();

        //删除文件
        minioClient.removeObject(removeObjectArgs);
    }

    //查询文件，从minio中下载
    @Test
    public void test_getFile()throws Exception{

        //设置查询参数
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket("testbucket")
                .object("/test/01/1.jpg")
                .build();
        //查询远程服务器获取到的流对象
        FilterInputStream filterInputStream = minioClient.getObject(getObjectArgs);
        //指定输出流
        FileOutputStream fileOutputStream = new FileOutputStream("D:\\minio_data_download\\1.jpg");
        IOUtils.copy(filterInputStream,fileOutputStream);

        //校验文件完整性：对文件的内容进行md5
        FileInputStream fileInputStream = new FileInputStream("C:\\Users\\豚豚\\Pictures\\壁纸\\wallhaven-7pjv3o.jpg");
        String source_md5 = DigestUtils.md5Hex(fileInputStream);
        String local_md5 = DigestUtils.md5Hex(new FileInputStream("D:\\minio_data_download\\1.jpg")); //本地的md5
        if (source_md5.equals(local_md5)){
            System.out.println("下载成功");
        }

    }

}
