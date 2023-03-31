package com.xuecheng.media.service.jobhandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Ke_han
 * @creat 2023-03-30 12:06
 */
@Component
@Slf4j
public class SampleXxlJob {

    /**
     * 1、简单任务示例（Bean模式）
     */
    @XxlJob("testJob")
    public void testJob() throws Exception {
        log.info("开始执行.....");

    }

    /**
     * 2、分片广播任务
     */
    @XxlJob("shardingJobHandler")
    public void shardingJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();  //执行器的序号
        int shardTotal = XxlJobHelper.getShardTotal();  //执行器的总数

        log.info("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);
        log.info("开始执行第"+shardIndex+"批任务");

    }


}
