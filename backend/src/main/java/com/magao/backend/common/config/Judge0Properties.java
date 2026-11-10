package com.magao.backend.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Judge0 判题引擎配置
 * <p>
 * 对应 application.yml / application-dev.yml / application-prod.yml 中的 judge0 配置块：
 * - api-url：Judge0 服务地址（如 http://localhost:2358 或 Docker 服务名 http://magao-judge0:2358）
 * - api-key：Judge0 访问密钥（部署开启鉴权时填写，未开启可留空）
 * - timeout：单次判题 HTTP 读超时基数（秒），实际读超时为 timeout+10 秒
 * </p>
 *
 * <p>Judge0 是基于 Docker 沙箱的业界标准开源评测引擎，原生支持超时限制、内存限制、
 * 网络隔离与系统调用拦截，满足大型竞赛对安全性与公平性的要求。</p>
 *
 * @author Magao开发团队
 */
@Data
@Component
@ConfigurationProperties(prefix = "judge0")
public class Judge0Properties {

    /** Judge0 服务基础地址（实例1，必填） */
    private String apiUrl = "http://localhost:2358";

    /** Judge0 服务地址（实例2，可选，多实例轮询+故障转移用） */
    private String apiUrl2;

    /** Judge0 服务地址（实例3，可选，多实例轮询+故障转移用） */
    private String apiUrl3;

    /** Judge0 访问密钥（可选，取决于部署是否开启鉴权） */
    private String apiKey;

    /** 超时基数（秒） */
    private int timeout = 30;
}
