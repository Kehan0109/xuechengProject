package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author Mr.M
     * @date 2022/9/10 8:57
     */
    PageResult<MediaFiles> queryMediaFiels
    (Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);


    /**
     * 上传文件
     * @param companyId 机构id
     * @param uploadFileParamsDto 保存到数据库的文件信息
     * @param localFilePath 本地文件路径
     * @return
     */
    UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath);


    /**
     * 上传文件信息到数据库
     * @param companyId
     * @param fileMd5
     * @param uploadFileParamsDto
     * @param bucket
     * @param objectName
     * @return
     * @question 当未被事务控制的方法，调用了被事务控制的方法默认不能开启事务！
     * @answer 需要将被事务控制的方法暴露在接口层，在实现类中注入接口，进行调用即可
     *          原理：只有代理对象调用方法，才会开启事务。
     * @knowledge Spring中事务方法生效的条件：当前执行方法的对象是代理对象，被调用的方法上有 @Transactional
     */
    MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName);


    /**
     * @description 检查分块文件是否存在，先查数据库，存在 -> 再查 minio
     * @param fileMd5 文件的md5
     * @return false 不存在 true 存在
     */
    RestResponse<Boolean> checkFile(String fileMd5);


    /**
     * @description 检查分块是否存在于 minio
     * @param fileMd5
     * @param chunkIndex
     * @return false 不存在 true 存在
     */
    RestResponse<Boolean> checkChunk(String fileMd5,int chunkIndex);


    /**
     * @description 上传分块
     * @param fileMd5 文件的 MD5
     * @param chunkIndex 分块序号
     * @param localChunkFilePath 分块文件本地路径
     * @return
     */
    RestResponse uploadChunk(String fileMd5,int chunkIndex,String localChunkFilePath);


    /**
     * @description 合并分块
     * @param companyId  机构id
     * @param fileMd5  文件md5
     * @param chunkTotal 分块总和
     * @param uploadFileParamsDto 文件信息
     * @return com.xuecheng.base.model.RestResponse
     * @author Mr.M
     * @date 2022/9/13 15:56
     */
    RestResponse mergeChunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);


    /**
     * 从minio下载文件
     *
     * @param bucket     桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    File downloadFileFromMinIO(String bucket, String objectName);


    /**
     * 将文件上传到minio
     *
     * @param bucket        桶
     * @param localFilePath 文件本地路径
     * @param mimeType      文件类型
     * @param objectName    对象名
     * @return
     */
    boolean addMediaFilesToMinIo(String bucket, String localFilePath, String mimeType, String objectName);


    /**
     * 根据媒资id查询文件信息
     * @param mediaId
     * @return
     */
    MediaFiles getFileById(String mediaId);
}
