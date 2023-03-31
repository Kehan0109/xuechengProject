package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 视频处理任务类
 * @author Ke_han
 * @creat 2023-03-30 15:44
 *
 */
@Component
@Slf4j
public class VideoTask {

    @Resource
    MediaFileProcessService mediaFileProcessService;

    @Resource
    MediaFileService mediaFileService;

    //ffmpeg路径
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegpath;


    /**
     * 视频转码处理，写入到数据库信息的方法，被调度中心调度
     * @throws Exception
     * 使用计数器，防止线程未执行完成内在逻辑就结束了方法的执行
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception{

        //分片参数
        int shardIndex = XxlJobHelper.getShardIndex();//执行器序号 ，从0开始
        int shardTotal = XxlJobHelper.getShardTotal();//执行器总数

        //确定cpu核心数
        int cpuProcessors = Runtime.getRuntime().availableProcessors();
        //查询待处理的任务，取出任务数就是CPU核心数
        List<MediaProcess> mediaProcessList =
                mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, cpuProcessors);
        //任务数
        int size = mediaProcessList.size();
        //没有任务就不处理
        if (size <= 0){
            log.debug("取到的视频处理任务数：{}",size);
            return;
        }
        //创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(size);

        //使用计数器，等待所有线程都完成任务后再结束方法
        CountDownLatch countDownLatch = new CountDownLatch(size);

        //拿到每个任务，遍历放到线程池执行
        mediaProcessList.forEach(mediaProcess -> {
            //将任务加入线程池
            executorService.execute(() ->{
                try {
                    //执行：
                    //1.开启任务（争抢，数据库乐观锁，实现分布式锁）
                    //任务id：
                    Long taskId = mediaProcess.getId();
                    //开启任务：使用数据库乐观锁
                    boolean b = mediaFileProcessService.startTask(taskId);
                    if (!b) {
                        log.debug("抢占任务失败，当前任务id：{}", taskId);
                        return;
                    }
                    //2.执行视频转码
                    //拿到每个任务中视频所属的桶
                    String bucket = mediaProcess.getBucket();
                    //拿到....的filePath即ObjectName
                    String objectName = mediaProcess.getFilePath();
                    //拿到文件的id，即md5值
                    String fileId = mediaProcess.getFileId();
                    //下载minio视频到本地：
                    File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                    if (file == null) {
                        log.debug("下载视频出错，当前任务id：{}，bucket:{},objectName:{}", taskId, bucket, objectName);
                        //更新任务处理失败的数据表信息
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频到本地失败");
                        return;
                    }
                    //转码参数2：源avi视频的路径
                    String video_path = file.getAbsolutePath();
                    //转码参数3：转换后mp4文件的名称：md5 + .mp4
                    String mp4_name = fileId + ".mp4";
                    //先创建临时文件，作为转换后的文件
                    File castFinishTemp = null;
                    try {
                        castFinishTemp = File.createTempFile("castFinish", ".mp4");
                    } catch (IOException e) {
                        log.error("创建临时文件异常：{}", e.getMessage());
                        //更新任务处理失败的数据表信息
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件失败");
                        return;
                    }
                    //转码参数4：转换后mp4文件的路径
                    String mp4_path = castFinishTemp.getAbsolutePath();
                    //创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, video_path, mp4_name, mp4_path);
                    //开始视频转换，成功将返回success
                    String result = videoUtil.generateMp4();
                    //更新任务处理失败的数据表信息
                    if (!result.equals("success")) {
                        log.error("视频转码失败，bucket:{},objectName:{},原因：{}", bucket, objectName, result);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "视频转码失败");
                        return;
                    }
                    //视频转码成功：
                    //3.视频文件上传minio
                    boolean b1 = mediaFileService.addMediaFilesToMinIo(bucket, mp4_path, "video/mp4", objectName);
                    //上传失败：
                    if (!b1) {
                        log.error("视频上传minio失败，taskId:{}，转码后的视频路径：{}", taskId, mp4_path);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "视频上传minio失败");
                        return;
                    }
                    //4.保存任务的处理结果
                    //拼接文件的url
                    String url = getFilePathByMd5(fileId, ".mp4");
                    //更新任务处理成功的数据表信息：
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, null);
                }finally {
                    //计数器-1
                    countDownLatch.countDown();
                }
            });
        });
        //阻塞，指定最大限度的等待时间，阻塞最多等待一定的时间后就解除阻塞，防止由于意外原因导致计数器没有到0长时间等待
        countDownLatch.await(30, TimeUnit.MINUTES);

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
}
