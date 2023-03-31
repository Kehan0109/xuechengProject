package com.xuecheng.media.service.impl;

import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Ke_han
 * @creat 2023-03-30 14:57
 */
@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Resource
    MediaProcessMapper mediaProcessMapper;

    @Resource
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Resource
    MediaFilesMapper mediaFilesMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
        return mediaProcesses;
    }

    @Override
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result <= 0 ? false : true;
    }

    @Transactional
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {

        //要更新的任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        //判断是否存在
        if(mediaProcess == null ){
            return;
        }
        //========如果任务执行失败========：status = 3，fail_count + 1
        if(status.equals("3")){
            //更新状态
            mediaProcess.setStatus("3");
            //失败次数+1
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            //失败原因
            mediaProcess.setErrormsg(errorMsg);
            //更新表
            mediaProcessMapper.updateById(mediaProcess);
            return;
        }

        //========如果任务执行成功========：
        // 更新media_files 的 url = xxxxxx.mp4，更新media_process status = 1状态，记录转移到media_process_history，删除记录
        //查询对应的mediaFiles信息
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        //更新url
        mediaFiles.setUrl(url);
        //更新表mediaFiles
        mediaFilesMapper.updateById(mediaFiles);

        //更新表mediaProcess的
        // status = 2 -> 成功
        mediaProcess.setStatus("2");
        // 完成时间
        mediaProcess.setFinishDate(LocalDateTime.now());
        // url
        mediaProcess.setUrl(url);
        //更新
        mediaProcessMapper.updateById(mediaProcess);

        //更新操作结束，将记录转移到历史表media_process_history
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        //拷贝
        BeanUtils.copyProperties(mediaProcess,mediaProcessHistory);
        //插入到历史表
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        //删除media_process表的记录
        mediaProcessMapper.deleteById(taskId);



    }
}
