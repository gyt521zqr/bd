package com.magao.backend.module.vod.service;

import com.magao.backend.module.vod.vo.VodPlayInfoVO;
import com.magao.backend.module.vod.vo.VodUploadAuthVO;

/**
 * 火山引擎 VOD（视频点播）服务接口
 * <p>
 * 提供视频上传凭证获取、播放地址获取等能力。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public interface VodService {

    /**
     * 获取视频上传凭证（STS）
     * <p>
     * 前端使用此凭证直接上传视频到 VOD，无需经过后端中转。
     * </p>
     *
     * @param fileName 文件名（如：course_intro.mp4）
     * @param title    视频标题
     * @return 上传凭证信息
     */
    VodUploadAuthVO getUploadAuth(String fileName, String title);

    /**
     * 获取视频播放信息
     * <p>
     * 根据视频 ID 获取播放地址、封面、时长等信息。
     * </p>
     *
     * @param vid 视频 ID（VOD 返回的唯一标识）
     * @return 播放信息
     */
    VodPlayInfoVO getPlayInfo(String vid);

    /**
     * 删除视频
     *
     * @param vid 视频 ID
     */
    void deleteVideo(String vid);
}