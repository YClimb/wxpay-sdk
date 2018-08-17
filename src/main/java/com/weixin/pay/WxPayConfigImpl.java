package com.weixin.pay;

import com.weixin.pay.constants.WxPayConstants;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 微信支付配置接口实现类
 *
 * @author yclimb
 * @date 2018/8/17
 */
public class WxPayConfigImpl extends WxPayConfig {

    private byte[] certData;
    private static WxPayConfigImpl INSTANCE;

    private WxPayConfigImpl() throws Exception {
        String certPath = WxPayConstants.APICLIENT_CERT;
        File file = new File(certPath);
        InputStream certStream = new FileInputStream(file);
        this.certData = new byte[(int) file.length()];
        certStream.read(this.certData);
        certStream.close();
    }

    public static WxPayConfigImpl getInstance() throws Exception {
        if (INSTANCE == null) {
            synchronized (WxPayConfigImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WxPayConfigImpl();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public String getAppID() {
        return WxPayConstants.APP_ID;
    }

    @Override
    public String getMchID() {
        return WxPayConstants.MCH_ID;
    }

    @Override
    public String getKey() {
        return WxPayConstants.API_KEY;
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
    WxPayDomain getWXPayDomain() {
        return WxPayDomainSimpleImpl.instance();
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
