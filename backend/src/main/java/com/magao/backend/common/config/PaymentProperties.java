package com.magao.backend.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付网关配置（正式收费 P0-01/P0-02）
 * <p>
 * payment.mode:
 * - demo   ：演示/本地联调模式。下单返回受控演示二维码，仅当 isDemo=true 才允许，
 *            绝不用于生产收钱；生产配置此值会拒绝启动外部支付。
 * - wechat ：微信支付 V3 Native 扫码。
 * - alipay ：支付宝当面付（预下单扫码）。
 * </p>
 * 商户凭证一律从环境变量注入，禁止写入代码库。
 */
@Data
@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {

    /** 支付模式：demo / wechat / alipay */
    private String mode = "demo";

    /** 对外回调地址（需公网可达，如 https://wukonghuwei.cn/api/payment/notify/wechat） */
    private String notifyBaseUrl = "";

    /** 订单有效期（分钟） */
    private int orderExpireMinutes = 30;

    // ---------- 微信支付 V3 ----------
    private String wechatAppId;
    private String wechatMchId;
    /** 商户 API v3 密钥（32 字节） */
    private String wechatApiV3Key;
    /** 商户私钥（PEM 内容，raph 环境注入） */
    private String wechatPrivateKey;
    /** 商户证书序列号 */
    private String wechatCertSerialNo;
    /** 微信平台证书（PEM 内容），留空则 SDK 在线下载 */
    private String wechatPlatformCert;

    // ---------- 支付宝 ----------
    private String alipayAppId;
    /** 应用私钥（PKCS8） */
    private String alipayPrivateKey;
    /** 支付宝公钥（验签用） */
    private String alipayPublicKey;
    /** 支付宝签名类型，默认 RSA2 */
    private String alipaySignType = "RSA2";

    public boolean isDemo() {
        return "demo".equalsIgnoreCase(mode);
    }
}
