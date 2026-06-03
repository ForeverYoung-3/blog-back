package com.blog.back.scheduler;

import com.blog.back.service.PostBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 博客定时备份任务。
 * <p>
 * 每天凌晨 02:00（Asia/Shanghai）执行一次，
 * 将所有已发布文章各自导出为 PDF 文件并保存到服务器备份目录。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BackupScheduler {

    private final PostBackupService postBackupService;

    /**
     * Cron 表达式：秒 分 时 日 月 周
     * {@code 0 0 2 * * ?} = 每天 02:00:00
     *
     * <p>时区通过 {@code zone} 属性指定，确保即使服务器在 UTC 环境下也在北京时间凌晨执行。</p>
     */
    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Shanghai")
    public void dailyPdfBackup() {
        log.info("[PDF备份] 定时任务开始执行 …");
        try {
            int count = postBackupService.backupAllPublishedPosts();
            log.info("[PDF备份] 定时任务完成，共成功备份 {} 篇文章。", count);
        } catch (Exception e) {
            log.error("[PDF备份] 定时任务执行异常", e);
        }
    }
}
