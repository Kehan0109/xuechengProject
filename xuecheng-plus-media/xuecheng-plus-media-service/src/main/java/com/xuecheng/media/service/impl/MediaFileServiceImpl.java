package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;

import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import jdk.nashorn.internal.ir.ReturnNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Resource
    MediaFilesMapper mediaFilesMapper;

    @Resource
    MinioClient minioClient;

    @Resource
    MediaFileService currentProxy;

    @Resource
    MediaProcessMapper mediaProcessMapper;

    @Resource
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    //桶：存储普通文件
    @Value("${minio.bucket.files}")
    private String bucket_mediaFiles;

    //桶：存储视频
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;


    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }


    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {


        //文件名
        String filename = uploadFileParamsDto.getFilename();
        //扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //获得mimeType
        String mimeType = getMimeType(extension);

        //根据当前日期获得ObjectName的路径的时间部分
        String folderPath = getDefaultFolderPath();
        //获得文件的md5值
        String fileMd5 = getFileMd5(new File(localFilePath));

        //拼接objetName 2023/03/27/md5.jpg
        String objectName = folderPath + fileMd5 + extension;
        //上传文件
        boolean result = addMediaFilesToMinIo(bucket_mediaFiles, localFilePath, mimeType, objectName);
        if (!result) {
            XueChengPlusException.cast("上传文件失败");
        }

        //将文件信息保存到数据库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediaFiles, objectName);
        //判断mediaFiles是否为空
        if (mediaFiles == null) {
            XueChengPlusException.cast("文件上传后，保存信息失败");
        }
        //准备要返回的对象
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }

    /**
     * 根据扩展名得到mimeType
     *
     * @param extension 扩展名
     * @return
     */
    public String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用的mimeType
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;

    }


    /**
     * 将文件上传到minio
     *
     * @param bucket        桶
     * @param localFilePath 文件本地路径
     * @param mimeType      文件类型
     * @param objectName    对象名
     * @return
     */

    public boolean addMediaFilesToMinIo(String bucket, String localFilePath, String mimeType, String objectName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .filename(localFilePath)
                    .object(objectName)
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            log.info("上传文件到minio成功,bucket:{},objectName:{}", bucket, objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件出错,bucket:{},objectName:{},错误信息:{}", bucket, objectName, e.getMessage());
        }
        return false;
    }

    /**
     * 查询媒资文件信息
     * @param mediaId
     * @return
     */
    @Override
    public MediaFiles getFileById(String mediaId) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
        return mediaFiles;
    }

    /**
     * 获得当前日期组成的文件路径
     *
     * @return 2023/03/27/
     */
    private String getDefaultFolderPath() {
        //设置时间日期格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //2023-03-27 -> 2023/03/27/
        //根据当前时间，拼接文件路径
        String folder = sdf.format(new Date()).replace("-", "/") + "/";
        return folder;
    }

    /**
     * 返回文件的 MD5值防止重复
     *
     * @param file 传入文件
     * @return 该文件的 md5值
     */
    private String getFileMd5(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param companyId           机构id
     * @param fileMd5             文件md5值
     * @param uploadFileParamsDto 上传文件的信息
     * @param bucket              桶
     * @param objectName          对象名称
     * @return com.xuecheng.media.model.po.MediaFiles
     * @description 将文件信息添加到文件表
     * @author Mr.M
     * @date 2022/10/12 21:22
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {

        //查询是否存在
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            //设置id
            mediaFiles.setId(fileMd5);
            //设置机构id
            mediaFiles.setCompanyId(companyId);
            //设置桶
            mediaFiles.setBucket(bucket);
            //设置file_path
            mediaFiles.setFilePath(objectName);
            //设置file_id
            mediaFiles.setFileId(fileMd5);
            //设置url
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            //设置createDate
            mediaFiles.setCreateDate(LocalDateTime.now());
            //设置status
            mediaFiles.setStatus("1");
            //设置审核状态
            mediaFiles.setAuditStatus("002003");
            //插入数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                log.error("向数据库保存文件信息失败，bucket:{},objectName:{}", bucket, objectName);
                return null;
            }
            //记录待处理的任务，为了交给xxl-job去查询待处理任务进行处理，将待处理任务写到数据表中
            //判断：需要交给执行器处理的视频类型，通过mineType判断
            addWaitingTask(mediaFiles);
            //向MediaProcess插入记录

        }
        return mediaFiles;
    }

    //检查文件
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        //先查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            //桶
            String bucket = mediaFiles.getBucket();
            //objectName
            String filePath = mediaFiles.getFilePath();
            //数据库存在则查询minio
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build();
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if (inputStream != null) {
                    //文件存在
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //文件不存在
        return RestResponse.success(false);
    }

    //检查分块
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {

        //分块路径：MD5前两位作为两个子目录 + chunk文件夹存储分块文件
        //路径：/m/d/chunk/分块文件

        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);

        GetObjectArgs args = GetObjectArgs.builder()
                .bucket(bucket_video)
                .object(chunkFileFolderPath + chunkIndex)
                .build();

        try {
            FilterInputStream filterInputStream = minioClient.getObject(args);
            if (filterInputStream != null) {
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.success(false);
    }

    //上传分块
    @Override
    public RestResponse uploadChunk(String fileMd5, int chunkIndex, String localChunkFilePath) {
        //分块文件的类型
        String mimeType = getMimeType(null);
        //分块文件的路径
        String objectName = getChunkFileFolderPath(fileMd5) + chunkIndex;
        //将分块文件上传到minio
        boolean b = addMediaFilesToMinIo(bucket_video, localChunkFilePath, mimeType, objectName);
        if (!b) {
            //失败
            return RestResponse.validfail(false, "上传分块文件失败");
        }
        //成功
        return RestResponse.validfail(true, "上传分块文件成功");
    }

    //合并分块
    @Override
    public RestResponse mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {

        //分块文件所在目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //找到所有的分块文件调用minio的sdk进行合并
        List<ComposeSource> sources = Stream.iterate(0, i -> i++)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder().bucket(bucket_video).object(chunkFileFolderPath + i).build())
                .collect(Collectors.toList());
        //源文件名
        String filename = uploadFileParamsDto.getFilename();
        //扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //合并后文件的objectName
        String objectName = getFilePathByMd5(fileMd5, extension);
        ComposeObjectArgs args = ComposeObjectArgs.builder()
                .bucket(bucket_video)
                .object(objectName)
                .sources(sources) //指定源文件
                .build();
        //==========合并文件==========
        try {
            minioClient.composeObject(args);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错，bucket:{}.objectName:{},错误信息：{}", bucket_video, objectName, e.getMessage());
            return RestResponse.validfail(false, "合并文件异常");
        }
        //==========校验==========
        //校验合并后的文件和源文件是否一致，先下载再校验
        File file = downloadFileFromMinIO(bucket_mediaFiles, objectName);
        //计算合并后文件的md5
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String mergeMd5 = DigestUtils.md5Hex(fileInputStream);
            //比较
            if(!fileMd5.equals(mergeMd5)){
                log.error("校验合并信息md5值不一致，原始文件：{}，合并文件：{}", fileMd5,mergeMd5);
                return RestResponse.validfail(false,"文件上传失败");
            }
            //设置文件大小
            uploadFileParamsDto.setFileSize(file.length());
        }catch (Exception e){
            return RestResponse.validfail(false,"文件上传失败");
        }
        //==========数据库写入==========
        //文件信息入库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, objectName);
        if (mediaFiles == null){
            return RestResponse.validfail(false,"文件上传失败");
        }
        //==========清理分块文件==========
        //清理分块文件
        clearChunkFiles(chunkFileFolderPath,chunkTotal);

        return RestResponse.success(true);
    }

    /**
     * 从minio下载文件
     *
     * @param bucket     桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket, String objectName) {
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile = File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream, outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    //得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }


    /**
     * 得到合并后的文件的地址
     *
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }


    /**
     * 清除分块文件
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal 分块文件总数
     */
    private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal){

        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());

            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder()
                    .bucket("video")
                    .objects(deleteObjects)
                    .build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            results.forEach(r->{
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清除分块文件失败,objectname:{}",deleteError.objectName(),e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清除分块文件失败,chunkFileFolderPath:{}",chunkFileFolderPath,e);
        }
    }

    /**
     * 添加待处理任务
     * @param mediaFiles 媒资文件信息
     */
    private void addWaitingTask(MediaFiles mediaFiles) {
        //获取文件的mimeType
        //文件的名称
        String filename = mediaFiles.getFilename();
        //文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        //判断视频类型，此处可以通过@value配置实现多格式视频判断
        if(mimeType.equals("video/x-msvideo")){
            //如果是avi文件，写入待处理任务
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);
            //设置状态，未处理
            mediaProcess.setStatus("1");
            //设置时间
            mediaProcess.setCreateDate(LocalDateTime.now());
            //设置失败次数，默认0
            mediaProcess.setFailCount(0);
            //设置url，防止拷贝
            mediaProcess.setUrl(null);

            mediaProcessMapper.insert(mediaProcess);

        }

    }


    }
