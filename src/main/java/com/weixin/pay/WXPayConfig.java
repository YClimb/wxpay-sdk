package com.weixin.pay;

import java.io.InputStream;

/**
 * 微信支付配置接口，用户设置获取微信支付关键信息抽象方法
 *
 * @author yclimb
 * @date 2018/8/17
 */
public abstract class WXPayConfig {

    /**
     * 获取 App ID
     *
     * @return App ID
     */
    abstract String getAppID();

    /**
     * 获取 Mch ID
     *
     * @return Mch ID
     */
    abstract String getMchID();

    /**
     * 获取 API 密钥
     *
     * @return API密钥
     */
    abstract String getKey();

    /**
     * 获取商户证书内容
     *
     * @return 商户证书内容
     */
    abstract InputStream getCertStream();

    /**
     * HTTP(S) 连接超时时间，单位毫秒
     *
     * @return int
     */
    public int getHttpConnectTimeoutMs() {
        return 6 * 1000;
    }

    /**
     * HTTP(S) 读数据超时时间，单位毫秒
     *
     * @return int
     */
    public int getHttpReadTimeoutMs() {
        return 8 * 1000;
    }

    /**
     * 获取WXPayDomain, 用于多域名容灾自动切换
     *
     * @return IWXPayDomain
     */
    abstract WXPayDomain getWXPayDomain();

    /**
     * 是否自动上报。
     * 若要关闭自动上报，子类中实现该函数返回 false 即可。
     *
     * @return boolean
     */
    public boolean shouldAutoReport() {
        return true;
    }

    /**
     * 进行健康上报的线程的数量
     *
     * @return int
     */
    public int getReportWorkerNum() {
        return 6;
    }

    /**
     * 健康上报缓存消息的最大数量。会有线程去独立上报
     * 粗略计算：加入一条消息200B，10000消息占用空间 2000 KB，约为2MB，可以接受
     *
     * @return int
     */
    public int getReportQueueMaxSize() {
        return 10000;
    }

    /**
     * 批量上报，一次最多上报多个数据
     *
     * @return int
     */
    public int getReportBatchSize() {
        return 10;
    }

}
