package com.weixin.pay;

import com.weixin.pay.constants.WXPayConstants;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 测试/第二个微信支付实体的微信支付实现类
 *
 * @author yclimb
 * @date 2018/7/31
 */
public class XxxWXPayConfigImpl extends WXPayConfig {

    private byte[] certData;
    private static XxxWXPayConfigImpl INSTANCE;

    private XxxWXPayConfigImpl() throws Exception{
        String certPath = WXPayConstants.APICLIENT_CERT_XXX;
        File file = new File(certPath);
        InputStream certStream = new FileInputStream(file);
        this.certData = new byte[(int) file.length()];
        certStream.read(this.certData);
        certStream.close();
    }

    public static XxxWXPayConfigImpl getInstance() throws Exception{
        if (INSTANCE == null) {
            synchronized (XxxWXPayConfigImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new XxxWXPayConfigImpl();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public String getAppID() {
        return WXPayConstants.APP_ID_XXX;
    }

    @Override
    public String getMchID() {
        return WXPayConstants.MCH_ID_XXX;
    }

    @Override
    public String getKey() {
        return WXPayConstants.API_KEY_XXX;
    }

    @Override
    public InputStream getCertStream() {
        ByteArrayInputStream certBis;
        certBis = new ByteArrayInputStream(this.certData);
        return certBis;
    }

    @Override
    public int getHttpConnectTimeoutMs() {
        return 2000;
    }

    @Override
    public int getHttpReadTimeoutMs() {
        return 10000;
    }

    @Override
    WXPayDomain getWXPayDomain() {
        return WXPayDomainSimpleImpl.instance();
    }

    public String getPrimaryDomain() {
        return "api.mch.weixin.qq.com";
    }

    public String getAlternateDomain() {
        return "api2.mch.weixin.qq.com";
    }

    @Override
    public int getReportWorkerNum() {
        return 1;
    }

    @Override
    public int getReportBatchSize() {
        return 2;
    }
}
