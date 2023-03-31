package com.xuecheng.media;

import io.minio.ComposeObjectArgs;
import io.minio.ComposeSource;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Ke_han
 * @creat 2023-03-28 20:14
 * 测试分块上传和合并
 */
public class BigFileTest {

    MinioClient minioClient = MinioClient.builder()
            .endpoint("http://192.168.146.1:9000")
            .credentials("minioadmin","minioadmin")
            .build();

    /**
     * 测试分块
     */
    @Test
    public void testChunk() throws IOException {
        //源文件
        File sourceFile = new File("D:\\obs output\\2022-05-02 15-50-11.mkv");
        //分块文件的存储路径
        String chunkFilePath = "D:\\minio_data_download\\chunk\\";
        //分块文件大小
        int chunkSize = 1024 * 1024 * 5;
        //分块文件的个数
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        //使用流从源文件读数据，向分块文件中写数据
        RandomAccessFile r = new RandomAccessFile(sourceFile,"r");
        byte[] buffer = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            //分块文件
            File chunkFile = new File(chunkFilePath + i);
            //分块文件写入流
            RandomAccessFile w = new RandomAccessFile(chunkFile, "rw");
            int len;
            while ((len = r.read(buffer)) != -1){
                w.write(buffer,0,len);
                if(chunkFile.length() >= chunkSize){
                    break;
                }
            }

            w.close();
        }
        r.close();

    }

    /**
     * 测试合并
     */
    @Test
    public void testMerge() throws IOException {
        //块文件目录
        File chunkFolder = new File("D:\\minio_data_download\\chunk");
        //源文件
        File sourceFile = new File("D:\\obs output\\2022-05-02 15-50-11.mkv");
        //合并文件
        File mergeFile = new File("D:\\minio_data_download\\合并文件.mkv");

        //取出所有的分块文件
        File[] files = chunkFolder.listFiles();
        //将数组转为list
        List<File> filesList = Arrays.asList(files);
        Collections.sort(filesList, new Comparator<File>() {
            //升序排
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });
        //向合并文件写的流
        RandomAccessFile w = new RandomAccessFile(mergeFile, "rw");
        byte[] buffer = new byte[1024];
        //遍历分块文件
        for (File file : filesList) {
            //读分块的流
            RandomAccessFile r = new RandomAccessFile(file, "r");
            int len;
            while ((len = r.read(buffer)) != -1){
                w.write(buffer,0,len);
            }
            r.close();
        }
        w.close();

        //合并完成，比较md5值
        FileInputStream mergeFileInputStream = new FileInputStream(mergeFile);
        String merge = DigestUtils.md5Hex(mergeFileInputStream);
        FileInputStream sourceFileInputStream = new FileInputStream(sourceFile);
        String source = DigestUtils.md5Hex(sourceFileInputStream);
        System.out.println(merge.equals(source) + "***************");
    }


    @Test
    //将分块文件上传到minio
    public void uploadChunk() throws IOException, ServerException, InsufficientDataException, InternalException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, XmlParserException, ErrorResponseException {

        for (int i = 0; i <= 56; i++) {
            //上传文件参数信息
            UploadObjectArgs args = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .filename("D:\\minio_data_download\\chunk\\" + i)
                    .object("chunk/" + i)
                    .build();

            minioClient.uploadObject(args);
            System.out.println("上传分块：" + i + "成功");
        }



    }

    //调用minio的接口合并分块
    @SneakyThrows
    @Test
    public void testMinioMerge() throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {

//        List<ComposeSource> sources = new ArrayList<>();
//        for (int i = 0; i <= 280; i++) {
//            //指定分块文件的信息
//            ComposeSource composeSource = ComposeSource.builder()
//                    .bucket("testbucket")
//                    .object("chunk/" + i)
//                    .build();
//
//            sources.add(composeSource);
//        }
        List<ComposeSource> sources = Stream.iterate(0, i -> i++)
                .limit(56)
                .map(i -> ComposeSource.builder().bucket("testbucket").object("chunk/" + i).build())
                .collect(Collectors.toList());

        ComposeObjectArgs args = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge01.mkv")
                .sources(sources) //指定源文件
                .build();


        minioClient.composeObject(args);

    }

    //批量清理分块文件
}
