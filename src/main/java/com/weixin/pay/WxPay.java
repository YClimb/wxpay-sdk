package com.weixin.pay;

import com.weixin.pay.util.WxPayUtil;
import com.weixin.pay.constants.WxPayConstants;
import com.weixin.pay.constants.WxPayConstants.SignType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付对象，使用此对象操作微信支付方法
 *
 * @author yclimb
 * @date 2018/8/17
 */
public class WxPay {

    private WxPayConfig config;
    private SignType signType;
    private boolean autoReport;
    private boolean useSandbox;
    private String notifyUrl;
    private WxPayRequest wxPayRequest;

    public WxPay(final WxPayConfig config) throws Exception {
        this(config, null, true, false);
        // this(config, null, true, true);
    }

    public WxPay(final WxPayConfig config, final boolean autoReport) throws Exception {
        this(config, null, autoReport, false);
        // this(config, null, autoReport, true);
    }


    public WxPay(final WxPayConfig config, final boolean autoReport, final boolean useSandbox) throws Exception{
        this(config, null, autoReport, useSandbox);
    }

    public WxPay(final WxPayConfig config, final String notifyUrl) throws Exception {
        this(config, notifyUrl, true, false);
    }

    public WxPay(final WxPayConfig config, final String notifyUrl, final boolean autoReport) throws Exception {
        this(config, notifyUrl, autoReport, false);
    }

    public WxPay(final WxPayConfig config, final String notifyUrl, final boolean autoReport, final boolean useSandbox) throws Exception {
        this.config = config;
        this.notifyUrl = notifyUrl;
        this.autoReport = autoReport;
        this.useSandbox = useSandbox;
        if (useSandbox) {
            this.signType = SignType.MD5; // 沙箱环境
        }
        else {
            this.signType = SignType.MD5; // 此处原来不是MD5！！！
        }
        this.wxPayRequest = new WxPayRequest(config);
    }

    private void checkWXPayConfig() throws Exception {
        if (this.config == null) {
            throw new Exception("config is null");
        }
        if (this.config.getAppID() == null || this.config.getAppID().trim().length() == 0) {
            throw new Exception("appid in config is empty");
        }
        if (this.config.getMchID() == null || this.config.getMchID().trim().length() == 0) {
            throw new Exception("appid in config is empty");
        }
        if (this.config.getCertStream() == null) {
            throw new Exception("cert stream in config is empty");
        }
        if (this.config.getWXPayDomain() == null){
            throw new Exception("config.getWXPayDomain() is null");
        }

        if (this.config.getHttpConnectTimeoutMs() < 10) {
            throw new Exception("http connect timeout is too small");
        }
        if (this.config.getHttpReadTimeoutMs() < 10) {
            throw new Exception("http read timeout is too small");
        }

    }

    /**
     * 向 Map 中添加 appid、mch_id、nonce_str、sign_type、sign <br>
     * 该函数适用于商户适用于统一下单等接口，不适用于红包、代金券接口
     *
     * @param reqData
     * @return
     * @throws Exception
     */
    public Map<String, String> fillRequestData(Map<String, String> reqData) throws Exception {
        reqData.put("appid", config.getAppID());
        reqData.put("mch_id", config.getMchID());
        reqData.put("nonce_str", WxPayUtil.generateNonceStr());
        if (SignType.MD5.equals(this.signType)) {
            reqData.put("sign_type", WxPayConstants.MD5);
        }
        else if (SignType.HMACSHA256.equals(this.signType)) {
            reqData.put("sign_type", WxPayConstants.HMACSHA256);
        }
        reqData.put("sign", WxPayUtil.generateSignature(reqData, config.getKey(), this.signType));
        return reqData;
    }

    /**
     * 判断xml数据的sign是否有效，必须包含sign字段，否则返回false。
     *
     * @param reqData 向wxpay post的请求数据
     * @return 签名是否有效
     * @throws Exception
     */
    public boolean isResponseSignatureValid(Map<String, String> reqData) throws Exception {
        // 返回数据的签名方式和请求中给定的签名方式是一致的
        return WxPayUtil.isSignatureValid(reqData, this.config.getKey(), this.signType);
    }

    /**
     * 判断支付结果通知中的sign是否有效
     *
     * @param reqData 向wxpay post的请求数据
     * @return 签名是否有效
     * @throws Exception
     */
    public boolean isPayResultNotifySignatureValid(Map<String, String> reqData) throws Exception {
        String signTypeInData = reqData.get(WxPayConstants.FIELD_SIGN_TYPE);
        SignType signType;
        if (signTypeInData == null) {
            signType = SignType.MD5;
        }
        else {
            signTypeInData = signTypeInData.trim();
            if (signTypeInData.length() == 0) {
                signType = SignType.MD5;
            }
            else if (WxPayConstants.MD5.equals(signTypeInData)) {
                signType = SignType.MD5;
            }
            else if (WxPayConstants.HMACSHA256.equals(signTypeInData)) {
                signType = SignType.HMACSHA256;
            }
            else {
                throw new Exception(String.format("Unsupported sign_type: %s", signTypeInData));
            }
        }
        return WxPayUtil.isSignatureValid(reqData, this.config.getKey(), signType);
    }


    /**
     * 不需要证书的请求
     * @param urlSuffix String
     * @param reqData 向wxpay post的请求数据
     * @param connectTimeoutMs 超时时间，单位是毫秒
     * @param readTimeoutMs 超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public String requestWithoutCert(String urlSuffix, Map<String, String> reqData,
                                     int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String msgUUID = reqData.get("nonce_str");
        String reqBody = WxPayUtil.mapToXml(reqData);

        String resp = this.wxPayRequest.requestWithoutCert(urlSuffix, msgUUID, reqBody, connectTimeoutMs, readTimeoutMs, autoReport);
        return resp;
    }


    /**
     * 需要证书的请求
     * @param urlSuffix String
     * @param reqData 向wxpay post的请求数据  Map
     * @param connectTimeoutMs 超时时间，单位是毫秒
     * @param readTimeoutMs 超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public String requestWithCert(String urlSuffix, Map<String, String> reqData,
                                  int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String msgUUID= reqData.get("nonce_str");
        String reqBody = WxPayUtil.mapToXml(reqData);

        String resp = this.wxPayRequest.requestWithCert(urlSuffix, msgUUID, reqBody, connectTimeoutMs, readTimeoutMs, this.autoReport);
        return resp;
    }

    /**
     * 处理 HTTPS API返回数据，转换成Map对象。return_code为SUCCESS时，验证签名。
     * @param xmlStr API返回的XML格式数据
     * @return Map类型数据
     * @throws Exception e
     */
    public Map<String, String> processResponseXml(String xmlStr) throws Exception {
        return processResponseXml(xmlStr, true);
    }

    /**
     * 处理 HTTPS API返回数据，转换成Map对象。return_code为SUCCESS时，验证签名。
     * @param xmlStr API返回的XML格式数据
     * @param isFlag 是否对返回的数据进行sign校验
     * @return Map类型数据
     * @throws Exception e
     */
    public Map<String, String> processResponseXml(String xmlStr, boolean isFlag) throws Exception {
        String RETURN_CODE = "return_code";
        String return_code;
        Map<String, String> respData = WxPayUtil.xmlToMap(xmlStr);
        if (respData.containsKey(RETURN_CODE)) {
            return_code = respData.get(RETURN_CODE);
        }
        else {
            throw new Exception(String.format("No `return_code` in XML: %s", xmlStr));
        }

        if (return_code.equals(WxPayConstants.FAIL)) {
            return respData;
        }
        else if (return_code.equals(WxPayConstants.SUCCESS)) {
            // 如果isFlag为false，则不需要进行sign校验
            if (!isFlag) {
                return respData;
            }
            if (this.isResponseSignatureValid(respData)) {
                return respData;
            }
            else {
                throw new Exception(String.format("Invalid sign value in XML: %s", xmlStr));
            }
        }
        else {
            throw new Exception(String.format("return_code value %s is invalid in XML: %s", return_code, xmlStr));
        }
    }

    /**
     * 作用：提交刷卡支付<br>
     * 场景：刷卡支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> microPay(Map<String, String> reqData) throws Exception {
        return this.microPay(reqData, this.config.getHttpConnectTimeoutMs(), this.config.getHttpReadTimeoutMs());
    }


    /**
     * 作用：提交刷卡支付<br>
     * 场景：刷卡支付
     * @param reqData 向wxpay post的请求数据
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs 读超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> microPay(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String url;
        if (this.useSandbox) {
            url = WxPayConstants.SANDBOX_MICROPAY_URL_SUFFIX;
        }
        else {
            url = WxPayConstants.MICROPAY_URL_SUFFIX;
        }
        String respXml = this.requestWithoutCert(url, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return this.processResponseXml(respXml);
    }

    /**
     * 提交刷卡支付，针对软POS，尽可能做成功
     * 内置重试机制，最多60s
     * @param reqData
     * @return
     * @throws Exception
     */
    public Map<String, String> microPayWithPos(Map<String, String> reqData) throws Exception {
        return this.microPayWithPos(reqData, this.config.getHttpConnectTimeoutMs());
    }

    /**
     * 提交刷卡支付，针对软POS，尽可能做成功
     * 内置重试机制，最多60s
     * @param reqData
     * @param connectTimeoutMs
     * @return
     * @throws Exception
     */
    public Map<String, String> microPayWithPos(Map<String, String> reqData, int connectTimeoutMs) throws Exception {
        int remainingTimeMs = 60*1000;
        long startTimestampMs = 0;
        Map<String, String> lastResult = null;
        Exception lastException = null;

        while (true) {
            startTimestampMs = WxPayUtil.getCurrentTimestampMs();
            int readTimeoutMs = remainingTimeMs - connectTimeoutMs;
            if (readTimeoutMs > 1000) {
                try {
                    lastResult = this.microPay(reqData, connectTimeoutMs, readTimeoutMs);
                    String returnCode = lastResult.get("return_code");
                    if (returnCode.equals("SUCCESS")) {
                        String resultCode = lastResult.get("result_code");
                        String errCode = lastResult.get("err_code");
                        if (resultCode.equals("SUCCESS")) {
                            break;
                        }
                        else {
                            // 看错误码，若支付结果未知，则重试提交刷卡支付
                            if (errCode.equals("SYSTEMERROR") || errCode.equals("BANKERROR") || errCode.equals("USERPAYING")) {
                                remainingTimeMs = remainingTimeMs - (int)(WxPayUtil.getCurrentTimestampMs() - startTimestampMs);
                                if (remainingTimeMs <= 100) {
                                    break;
                                }
                                else {
                                    WxPayUtil.getLogger().info("microPayWithPos: try micropay again");
                                    if (remainingTimeMs > 5*1000) {
                                        Thread.sleep(5*1000);
                                    }
                                    else {
                                        Thread.sleep(1*1000);
                                    }
                                    continue;
                                }
                            }
                            else {
                                break;
                            }
                        }
                    }
                    else {
                        break;
                    }
                }
                catch (Exception ex) {
                    lastResult = null;
                    lastException = ex;
                }
            }
            else {
                break;
            }
        }

        if (lastResult == null) {
            throw lastException;
        }
        else {
            return lastResult;
        }
    }

    /**
     * 作用：商户平台-现金红包-发放普通红包<br>
     * 场景：现金红包发放后会以公众号消息的形式触达用户
     * 其他：需要证书
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception e
     */
    public Map<String, String> sendRedPack(Map<String, String> reqData) throws Exception {
        String url;
        if (this.useSandbox) {
            url = WxPayConstants.SANDBOX_SENDREDPACK_URL_SUFFIX;
        }
        else {
            url = WxPayConstants.SENDREDPACK_URL_SUFFIX;
        }
        String respXml = this.requestWithCert(url, this.redPackRequestData(reqData), config.getHttpConnectTimeoutMs(), this.config.getHttpReadTimeoutMs());
        return this.processResponseXml(respXml);
    }

    public Map<String, String> redPackRequestData(Map<String, String> reqData) throws Exception {
        reqData.put("wxappid", config.getAppID());
        reqData.put("mch_id", config.getMchID());
        reqData.put("nonce_str", WxPayUtil.generateUUID());
        reqData.put("sign", WxPayUtil.generateSignature(reqData, config.getKey(), this.signType));
        return reqData;
    }

    /**
     * 作用：商户平台-企业付款-企业向微信用户个人付款 <br>
     * 场景：企业付款到零钱资金使用商户号余额资金。
     * 其他：需要证书
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception e
     */
    public Map<String, String> transfers(Map<String, String> reqData) throws Exception {
        String url;
        if (this.useSandbox) {
            url = WxPayConstants.SANDBOX_TRANSFERS_URL_SUFFIX;
        }
        else {
            url = WxPayConstants.TRANSFERS_URL_SUFFIX;
        }
        String respXml = this.requestWithCert(url, this.transfersRequestData(reqData), config.getHttpConnectTimeoutMs(), this.config.getHttpReadTimeoutMs());
        return this.processResponseXml(respXml, false);
    }

    public Map<String, String> transfersRequestData(Map<String, String> reqData) throws Exception {
        reqData.put("mch_appid", WxPayConstants.APP_ID_XXX);
        reqData.put("mchid", WxPayConstants.MCH_ID_XXX);
        reqData.put("nonce_str", WxPayUtil.generateUUID());
        reqData.put("sign", WxPayUtil.generateSignature(reqData, WxPayConstants.API_KEY_XXX, this.signType));
        return reqData;
    }


    /**
     * 作用：统一下单<br>
     * 场景：公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> unifiedOrder(Map<String, String> reqData) throws Exception {
        return this.unifiedOrder(reqData, config.getHttpConnectTimeoutMs(), this.config.getHttpReadTimeoutMs());
    }


    /**
     * 作用：统一下单<br>
     * 场景：公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs 读超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> unifiedOrder(Map<String, String> reqData,  int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String url;
        if (this.useSandbox) {
            url = WxPayConstants.SANDBOX_UNIFIEDORDER_URL_SUFFIX;
        }
        else {
            url = WxPayConstants.UNIFIEDORDER_URL_SUFFIX;
        }
        if(this.notifyUrl != null) {
            reqData.put("notify_url", this.notifyUrl);
        }
        String respXml = this.requestWithoutCert(url, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return this.processResponseXml(respXml);
    }


    /**
     * 作用：查询订单<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> orderQuery(Map<String, String> reqData) throws Exception {
        return this.orderQuery(reqData, config.getHttpConnectTimeoutMs(), this.config.getHttpReadTimeoutMs());
    }


    /**
     * 作用：查询订单<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据 int
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs 读超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> orderQuery(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String url;
        if (this.useSandbox) {
            url = WxPayConstants.SANDBOX_ORDERQUERY_URL_SUFFIX;
        }
        else {
            url = WxPayConstants.ORDERQUERY_URL_SUFFIX;
        }
        String respXml = this.requestWithoutCert(url, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return this.processResponseXml(respXml);
    }


    /**
     * 作用：撤销订单<br>
     * 场景：刷卡支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> reverse(Map<String, String> reqData) throws Exception {
        return this.reverse(reqData, config.getHttpConnectTimeoutMs(), this.config.getHttpReadTimeoutMs());
    }


    /**
     * 作用：撤销订单<br>
     * 场景：刷卡支付<br>
     * 其他：需要证书
     * @param reqData 向wxpay post的请求数据
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs 读超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> reverse(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String url;
        if (this.useSandbox) {
            url = WxPayConstants.SANDBOX_REVERSE_URL_SUFFIX;
        }
        else {
            url = WxPayConstants.REVERSE_URL_SUFFIX;
        }
        String respXml = this.requestWithCert(url, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return this.processResponseXml(respXml);
    }


    /**
     * 作用：关闭订单<br>
     * 场景：公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> closeOrder(Map<String, String> reqData) throws Exception {
        return this.closeOrder(reqData, config.getHttpConnectTimeoutMs(), this.config.getHttpReadTimeoutMs());
    }


    /**
     * 作用：关闭订单<br>
     * 场景：公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs 读超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> closeOrder(Map<String, String> reqData,  int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String url;
        if (this.useSandbox) {
            url = WxPayConstants.SANDBOX_CLOSEORDER_URL_SUFFIX;
        }
        else {
            url = WxPayConstants.CLOSEORDER_URL_SUFFIX;
        }
        String respXml = this.requestWithoutCert(url, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return this.processResponseXml(respXml);
    }


    /**
     * 作用：申请退款<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> refund(Map<String, String> reqData) throws Exception {
        return this.refund(reqData, this.config.getHttpConnectTimeoutMs(), this.config.getHttpReadTimeoutMs());
    }


    /**
     * 作用：申请退款<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付<br>
     * 其他：需要证书
     * @param reqData 向wxpay post的请求数据
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs 读超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> refund(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String url;
        if (this.useSandbox) {
            url = WxPayConstants.SANDBOX_REFUND_URL_SUFFIX;
        }
        else {
            url = WxPayConstants.REFUND_URL_SUFFIX;
        }
        String respXml = this.requestWithCert(url, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return this.processResponseXml(respXml);
    }


    /**
     * 作用：退款查询<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> refundQuery(Map<String, String> reqData) throws Exception {
        return this.refundQuery(reqData, this.config.getHttpConnectTimeoutMs(), this.config.getHttpReadTimeoutMs());
    }


    /**
     * 作用：退款查询<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs 读超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> refundQuery(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String url;
        if (this.useSandbox) {
            url = WxPayConstants.SANDBOX_REFUNDQUERY_URL_SUFFIX;
        }
        else {
            url = WxPayConstants.REFUNDQUERY_URL_SUFFIX;
        }
        String respXml = this.requestWithoutCert(url, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return this.processResponseXml(respXml);
    }


    /**
     * 作用：对账单下载（成功时返回对账单数据，失败时返回XML格式数据）<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> downloadBill(Map<String, String> reqData) throws Exception {
        return this.downloadBill(reqData, this.config.getHttpConnectTimeoutMs(), this.config.getHttpReadTimeoutMs());
    }


    /**
     * 作用：对账单下载<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付<br>
     * 其他：无论是否成功都返回Map。若成功，返回的Map中含有return_code、return_msg、data，
     *      其中return_code为`SUCCESS`，data为对账单数据。
     * @param reqData 向wxpay post的请求数据
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs 读超时时间，单位是毫秒
     * @return 经过封装的API返回数据
     * @throws Exception
     */
    public Map<String, String> downloadBill(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String url;
        if (this.useSandbox) {
            url = WxPayConstants.SANDBOX_DOWNLOADBILL_URL_SUFFIX;
        }
        else {
            url = WxPayConstants.DOWNLOADBILL_URL_SUFFIX;
        }
        String respStr = this.requestWithoutCert(url, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs).trim();
        Map<String, String> ret;
        // 出现错误，返回XML数据
        if (respStr.indexOf("<") == 0) {
            ret = WxPayUtil.xmlToMap(respStr);
        }
        else {
            // 正常返回csv数据
            ret = new HashMap<String, String>();
            ret.put("return_code", WxPayConstants.SUCCESS);
            ret.put("return_msg", "ok");
            ret.put("data", respStr);
        }
        return ret;
    }


    /**
     * 作用：交易保障<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> report(Map<String, String> reqData) throws Exception {
        return this.report(reqData, this.config.getHttpConnectTimeoutMs(), this.config.getHttpReadTimeoutMs());
    }


    /**
     * 作用：交易保障<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs 读超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> report(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String url;
        if (this.useSandbox) {
            url = WxPayConstants.SANDBOX_REPORT_URL_SUFFIX;
        }
        else {
            url = WxPayConstants.REPORT_URL_SUFFIX;
        }
        String respXml = this.requestWithoutCert(url, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return WxPayUtil.xmlToMap(respXml);
    }


    /**
     * 作用：转换短链接<br>
     * 场景：刷卡支付、扫码支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> shortUrl(Map<String, String> reqData) throws Exception {
        return this.shortUrl(reqData, this.config.getHttpConnectTimeoutMs(), this.config.getHttpReadTimeoutMs());
    }


    /**
     * 作用：转换短链接<br>
     * 场景：刷卡支付、扫码支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> shortUrl(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String url;
        if (this.useSandbox) {
            url = WxPayConstants.SANDBOX_SHORTURL_URL_SUFFIX;
        }
        else {
            url = WxPayConstants.SHORTURL_URL_SUFFIX;
        }
        String respXml = this.requestWithoutCert(url, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return this.processResponseXml(respXml);
    }


    /**
     * 作用：授权码查询OPENID接口<br>
     * 场景：刷卡支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> authCodeToOpenid(Map<String, String> reqData) throws Exception {
        return this.authCodeToOpenid(reqData, this.config.getHttpConnectTimeoutMs(), this.config.getHttpReadTimeoutMs());
    }


    /**
     * 作用：授权码查询OPENID接口<br>
     * 场景：刷卡支付
     * @param reqData 向wxpay post的请求数据
     * @param connectTimeoutMs 连接超时时间，单位是毫秒
     * @param readTimeoutMs 读超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> authCodeToOpenid(Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        String url;
        if (this.useSandbox) {
            url = WxPayConstants.SANDBOX_AUTHCODETOOPENID_URL_SUFFIX;
        }
        else {
            url = WxPayConstants.AUTHCODETOOPENID_URL_SUFFIX;
        }
        String respXml = this.requestWithoutCert(url, this.fillRequestData(reqData), connectTimeoutMs, readTimeoutMs);
        return this.processResponseXml(respXml);
    }

    /**
     * 作用：统一下单<br>
     * 场景：商户在小程序中先调用该接口在微信支付服务后台生成预支付交易单，返回正确的预支付交易后调起支付。
     * 接口链接：URL地址：https://api.mch.weixin.qq.com/pay/unifiedorder
     * 是否需要证书：否
     * 接口文档地址：https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_1
     *
     * @param notify_url 公众号用户openid
     * @param body 商品简单描述，该字段请按照规范传递，例：腾讯充值中心-QQ会员充值
     * @param out_trade_no 商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*且在同一个商户号下唯一
     * @param total_fee 订单总金额，传入参数单位为：元
     * @param spbill_create_ip APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP
     * @return API返回数据
     * @throws Exception e
     */
    public Map<String, String> unifiedOrder(String notify_url, String openid, String body, String out_trade_no, String total_fee, String spbill_create_ip) throws Exception {

        /** 构造请求参数数据 **/
        Map<String, String> data = new HashMap<>();

        // 微信支付对象
        WxPay wxPay = new WxPay(WxPayConfigImpl.getInstance());

        // 字段名	变量名	必填	类型	示例值	描述
        // 标价币种	fee_type	否	String(16)	CNY	符合ISO 4217标准的三位字母代码，默认人民币：CNY，详细列表请参见货币类型
        data.put("fee_type", WxPayConstants.FEE_TYPE_CNY);
        // 通知地址	notify_url	是	String(256)	http://www.weixin.qq.com/wxpay/pay.php	异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
        data.put("notify_url", notify_url);
        // 交易类型	trade_type	是	String(16)	JSAPI	小程序取值如下：JSAPI，详细说明见参数规定
        data.put("trade_type", WxPayConstants.TRADE_TYPE);
        // 用户标识	openid	否	String(128)	oUpF8uMuAJO_M2pxb1Q9zNjWeS6o	trade_type=JSAPI，此参数必传，用户在商户appid下的唯一标识。openid如何获取，可参考【获取openid】。
        data.put("openid", openid);
        // 商品描述	body	是	String(128)	腾讯充值中心-QQ会员充值 商品简单描述，该字段请按照规范传递，具体请见参数规定
        data.put("body", body);
        // 商户订单号	out_trade_no	是	String(32)	20150806125346	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*且在同一个商户号下唯一。详见商户订单号
        data.put("out_trade_no", out_trade_no);
        // 标价金额	total_fee	是	Int	88	订单总金额，单位为分，详见支付金额
        // 默认单位为分，系统是元，所以需要*100
        data.put("total_fee", String.valueOf(new BigDecimal(total_fee).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).intValue()));
        // 终端IP	spbill_create_ip	是	String(16)	123.12.12.123	APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP。
        data.put("spbill_create_ip", spbill_create_ip);

        /** 以下参数为非必填参数 **/
		/*// 交易起始时间	time_start	否	String(14)	20091225091010	订单生成时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010。其他详见时间规则
		data.put("time_start", null);
		// 交易结束时间	time_expire	否	String(14)	20091227091010	订单失效时间，格式为yyyyMMddHHmmss，如2009年12月27日9点10分10秒表示为20091227091010。订单失效时间是针对订单号而言的，由于在请求支付的时候有一个必传参数prepay_id只有两小时的有效期，所以在重入时间超过2小时的时候需要重新请求下单接口获取新的prepay_id。其他详见时间规则,建议：最短失效时间间隔大于1分钟
		data.put("time_expire", null);
		// 订单优惠标记	goods_tag	否	String(32)	WXG	订单优惠标记，使用代金券或立减优惠功能时需要的参数，说明详见代金券或立减优惠
		data.put("goods_tag", null);
		// 商品ID	product_id	否	String(32)	12235413214070356458058	trade_type=NATIVE时（即扫码支付），此参数必传。此参数为二维码中包含的商品ID，商户自行定义。
		data.put("product_id", null);
		// 指定支付方式	limit_pay	否	String(32)	no_credit	上传此参数no_credit--可限制用户不能使用信用卡支付
		data.put("limit_pay", null);
		// 设备号	device_info	否	String(32)	013467007045764	自定义参数，可以为终端设备号(门店号或收银设备ID)，PC网页或公众号内支付可以传"WEB"
		data.put("device_info", null);
		// 商品详情	detail	否	String(6000)	 	商品详细描述，对于使用单品优惠的商户，改字段必须按照规范上传，详见“单品优惠参数说明”
		data.put("detail", null);
		// 附加数据	attach	否	String(127)	深圳分店	附加数据，在查询API和支付通知中原样返回，可作为自定义参数使用。
		data.put("attach", null);*/

        /** 以下五个参数，在 this.fillRequestData 方法中会自动赋值 **/
		/*// 小程序ID	appid	是	String(32)	wxd678efh567hg6787	微信分配的小程序ID
        data.put("appid", WXPayConstants.APP_ID);
        // 商户号	mch_id	是	String(32)	1230000109	微信支付分配的商户号
        data.put("mch_id", WXPayConstants.MCH_ID);
		// 随机字符串	nonce_str	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	随机字符串，长度要求在32位以内。推荐随机数生成算法
		data.put("nonce_str", nonce_str);
		// 签名类型	sign_type	否	String(32)	MD5	签名类型，默认为MD5，支持HMAC-SHA256和MD5。
        data.put("sign_type", WXPayConstants.MD5);
		// 签名	sign	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	通过签名算法计算得出的签名值，详见签名生成算法
		data.put("sign", sign);*/

        // 微信统一下单接口请求地址
        Map<String, String> resultMap = wxPay.unifiedOrder(data);

        WxPayUtil.getLogger().info("wxPay.unifiedOrder:" + resultMap);

        return resultMap;
    }

    /**
     * 作用：生成微信支付所需参数，微信支付二次签名<br>
     * 场景：根据微信统一下单接口返回的 prepay_id 生成微信支付所需的参数
     * 接口文档地址：https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=7_7&index=6
     *
     * @param prepay_id 预支付id
     * @param nonce_str 随机字符串
     * @return 支付方法调用所需参数map
     * @throws Exception e
     */
    public Map<String, String> chooseWXPayMap(String prepay_id, String nonce_str) throws Exception {

        // 支付方法调用所需参数map
        Map<String, String> chooseWXPayMap = new HashMap<>();
        chooseWXPayMap.put("appId", WxPayConstants.APP_ID);
        chooseWXPayMap.put("timeStamp", String.valueOf(WxPayUtil.getCurrentTimestamp()));
        chooseWXPayMap.put("nonceStr", nonce_str);
        chooseWXPayMap.put("package", "prepay_id=" + prepay_id);
        chooseWXPayMap.put("signType", WxPayConstants.MD5);

        WxPayUtil.getLogger().info("wxPay.chooseWXPayMap:" + chooseWXPayMap.toString());

        // 生成支付签名
        String paySign = WxPayUtil.generateSignature(chooseWXPayMap, WxPayConstants.API_KEY);
        chooseWXPayMap.put("paySign", paySign);

        WxPayUtil.getLogger().info("wxPay.paySign:" + paySign);

        return chooseWXPayMap;
    }

    /**
     * 作用：申请退款<br>
     * 场景：当交易发生之后一段时间内，由于买家或者卖家的原因需要退款时，卖家可以通过退款接口将支付款退还给买家，
     *      微信支付将在收到退款请求并且验证成功之后，按照退款规则将支付款按原路退到买家帐号上。
     * 接口文档地址：https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=9_4
     *
     * @param notify_url 回调地址
     * @param transaction_id 微信生成的订单号，在支付通知中有返回
     * @param out_trade_no 商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。
     * @param out_refund_no 商户系统内部的退款单号，商户系统内部唯一，只能是数字、大小写字母_-|*@ ，同一退款单号多次请求只退一笔。
     * @param total_fee 订单总金额，传入参数单位为：元
     * @param refund_fee 退款总金额，订单总金额，传入参数单位为：元
     * @param refund_desc 退款原因，若商户传入，会在下发给用户的退款消息中体现退款原因
     * @return API返回数据
     * @throws Exception e
     */
    public Map<String, String> refund(String notify_url, String transaction_id, String out_trade_no, String out_refund_no,
                                               String total_fee, String refund_fee, String refund_desc) throws Exception {

        /** 构造请求参数数据 **/
        Map<String, String> data = new HashMap<>();

        // 变量名		字段名	必填	类型	示例值	描述
        // 微信订单号	二选一	String(32)	1.21775E+27	微信生成的订单号，在支付通知中有返回
        if (transaction_id != null) {
            data.put("transaction_id", transaction_id);
        }
        // 商户订单号	String(32)	1.21775E+27	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。
        data.put("out_trade_no", out_trade_no);
        // 商户退款单号	是	String(64)	1.21775E+27	商户系统内部的退款单号，商户系统内部唯一，只能是数字、大小写字母_-|*@ ，同一退款单号多次请求只退一笔。
        data.put("out_refund_no", out_refund_no);
        // 订单金额	是	Int	100	订单总金额，单位为分，只能为整数，详见支付金额
        data.put("total_fee", String.valueOf(new BigDecimal(total_fee).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).intValue()));
        // 退款金额	是	Int	100	退款总金额，订单总金额，单位为分，只能为整数，详见支付金额
        // 默认单位为分，系统是元，所以需要*100
        data.put("refund_fee", String.valueOf(new BigDecimal(refund_fee).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).intValue()));
        // 退款原因	否	String(80)	商品已售完	若商户传入，会在下发给用户的退款消息中体现退款原因
        data.put("refund_desc", refund_desc);
        // 货币种类	否	String(8)	CNY	货币类型，符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型
        data.put("refund_fee_type", WxPayConstants.FEE_TYPE_CNY);
        // 退款结果通知url	否	String(256)	https://weixin.qq.com/notify/	异步接收微信支付退款结果通知的回调地址，通知URL必须为外网可访问的url，不允许带参数,如果参数中传了notify_url，则商户平台上配置的回调地址将不会生效。
        data.put("notify_url", notify_url);

        /** 以下参数为非必填参数 **/
        // 退款资金来源	否	String(30)	REFUND_SOURCE_RECHARGE_FUNDS	仅针对老资金流商户使用;REFUND_SOURCE_UNSETTLED_FUNDS---未结算资金退款（默认使用未结算资金退款）;REFUND_SOURCE_RECHARGE_FUNDS---可用余额退款
        // data.put("refund_account", null);


        /** 以下五个参数，在 this.fillRequestData 方法中会自动赋值 **/
		/*// 小程序ID	appid	是	String(32)	wxd678efh567hg6787	微信分配的小程序ID
        data.put("appid", WXPayConstants.APP_ID);
        // 商户号	mch_id	是	String(32)	1230000109	微信支付分配的商户号
        data.put("mch_id", WXPayConstants.MCH_ID);
		// 随机字符串	nonce_str	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	随机字符串，长度要求在32位以内。推荐随机数生成算法
		data.put("nonce_str", nonce_str);
		// 签名类型	sign_type	否	String(32)	MD5	签名类型，默认为MD5，支持HMAC-SHA256和MD5。
        data.put("sign_type", WXPayConstants.MD5);
		// 签名	sign	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	通过签名算法计算得出的签名值，详见签名生成算法
		data.put("sign", sign);*/

        // 微信支付对象
        WxPay wxPay = new WxPay(WxPayConfigImpl.getInstance());

        // 微信退款接口
        Map<String, String> resultMap = wxPay.refund(data);

        WxPayUtil.getLogger().info("wxPay.refund:" + resultMap);

        return resultMap;
    }

    /**
     * 作用：企业向微信用户个人付款<br>
     * 场景：企业付款为企业提供付款至用户零钱的能力，支持通过API接口付款，或通过微信支付商户平台（pay.weixin.qq.com）网页操作付款。
     * 接口文档地址：https://pay.weixin.qq.com/wiki/doc/api/tools/mch_pay.php?chapter=14_2
     *
     * @param partner_trade_no 商户订单号
     * @param openid 用户openid
     * @param amount 企业付款金额
     * @param desc 企业付款描述信息
     * @param spbill_create_ip 该IP可传用户端或者服务端的IP
     * @return API返回数据
     * @throws Exception e
     */
    public Map<String, String> transfers(String partner_trade_no, String openid, String amount, String desc, String spbill_create_ip) throws Exception {

        /** 构造请求参数数据 **/
        Map<String, String> data = new HashMap<>();

        // 商户订单号	partner_trade_no	是	10000098201411111234567890	String	商户订单号，需保持唯一性(只能是字母或者数字，不能包含有符号)
        data.put("partner_trade_no", partner_trade_no);
        // 用户openid	openid	是	oxTWIuGaIt6gTKsQRLau2M0yL16E	String	商户appid下，某用户的openid
        data.put("openid", openid);
        // 校验用户姓名选项	check_name	是	FORCE_CHECK	String	NO_CHECK：不校验真实姓名,FORCE_CHECK：强校验真实姓名
        data.put("check_name", "NO_CHECK");
        // 金额	amount	是	10099	int	企业付款金额，单位为分
        data.put("amount", String.valueOf(new BigDecimal(amount).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).intValue()));
        // 企业付款描述信息	desc	是	理赔	String	企业付款操作说明信息。必填。
        data.put("desc", desc);
        // Ip地址	spbill_create_ip	是	192.168.0.1	String(32)	该IP同在商户平台设置的IP白名单中的IP没有关联，该IP可传用户端或者服务端的IP。
        data.put("spbill_create_ip", spbill_create_ip);

        /** 以下参数为非必填参数 **/

        /*// 设备号	device_info	否	013467007045764	String(32)	微信支付分配的终端设备号
        data.put("device_info", "xxx");
        // 收款用户姓名	re_user_name	可选	王小王	String	收款用户真实姓名。(如果check_name设置为FORCE_CHECK，则必填用户真实姓名)
        data.put("re_user_name", "xxx");*/

        // 微信支付对象
        WxPay wxPay = new WxPay(XxxWxPayConfigImpl.getInstance());

        // 微信调用接口
        Map<String, String> resultMap = wxPay.transfers(data);

        WxPayUtil.getLogger().info("wxPay.transfers:" + resultMap);

        return resultMap;
    }

} // end class
