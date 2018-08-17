package com.weixin.pay.constants;

import org.apache.http.client.HttpClient;

/**
 * 微信支付SDK常量
 *
 * @author yclimb
 * @date 2018/8/17
 */
public class WxPayConstants {

    /**
     * 异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
     */
    public static String NOTIFY_URL = "https://xxx.com/v1/weixin/pay/wxnotify";

    /**
     * 异步接收微信支付退款结果通知的回调地址，通知URL必须为外网可访问的url，不允许带参数，如果参数中传了notify_url，则商户平台上配置的回调地址将不会生效。
     */
    public static String NOTIFY_URL_REFUND = "https://xxx.com/v1/weixin/pay/refund";

    /**
     * 微信签名枚举类型
     */
    public enum SignType {
        MD5, HMACSHA256
    }

    /**
     * 公众号、小程序appid
     */
    public static String APP_ID = "xxx"; // 真实
    public static String APP_ID_XXX = "xxx"; // 测试/第二个账号

    /**
     * AppSecret
     */
    public static String SECRET = "xxx"; // 真实
    public static String SECRET_XXX = "xxx"; // 测试/第二个账号

    /**
     * 商户号
     */
    public static final String MCH_ID = "xxx"; // 真实
    public static final String MCH_ID_XXX = "xxx"; // 测试/第二个账号


    /**
     * API密钥，在商户平台设置
     */
    public static final String API_KEY = "xxx"; // 真实
    public static final String API_KEY_XXX = "xxx"; // 测试/第二个账号
    public static final String API_KEY_SANDBOX = "xxx"; // sandbox_signkey

    /**
     * 证书路径
     */
    public static String APICLIENT_CERT = "/data/ops/cert/apiclient_cert.p12"; // 真实
    public static String APICLIENT_CERT_XXX = "/data/ops/cert_xxx/apiclient_cert.p12"; // 真实

    /**
     * 交易类型
     * JSAPI--公众号支付、NATIVE--原生扫码支付、APP--app支付，统一下单接口trade_type的传参可参考这里
     * MICROPAY--刷卡支付，刷卡支付有单独的支付接口，不调用统一下单接口
     */
    public static String TRADE_TYPE =  "JSAPI";
    public static String TRADE_TYPE_APP =  "APP";
    public static String TRADE_TYPE_NATIVE =  "NATIVE";

    /**
     * 微信 - API域名地址
     * 域名管理实现主备域名自动切换
     */
    public static final String DOMAIN_API = "api.mch.weixin.qq.com";
    public static final String DOMAIN_API2 = "api2.mch.weixin.qq.com";
    public static final String DOMAIN_APIHK = "apihk.mch.weixin.qq.com";
    public static final String DOMAIN_APIUS = "apius.mch.weixin.qq.com";

    /**
     * 微信 - 默认接口返回状态码
     * SUCCESS/FAIL
     * 此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断
     */
    public static final String RESULT_CODE     = "result_code";
    public static final String FAIL     = "FAIL";
    public static final String SUCCESS  = "SUCCESS";
    /**
     * 返回状态码 return_code SUCCESS/FAIL 此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断
     * 返回信息 return_msg 当return_code为FAIL时返回信息为错误原因
     * 错误代码 err_code 当result_code为FAIL时返回错误代码，详细参见下文错误列表
     * 错误代码描述 err_code_des 当result_code为FAIL时返回错误描述，详细参见下文错误列表
     */
    public static final String RETURN_CODE = "return_code";
    public static final String RETURN_MSG = "return_msg";
    public static final String ERR_CODE = "err_code";
    public static final String ERR_CODE_DES  = "err_code_des";

    /**
     * 签名类型，默认为MD5，支持HMAC-SHA256和MD5。
     */
    public static final String HMACSHA256 = "HMAC-SHA256";
    public static final String MD5 = "MD5";

    /**
     * 标价币种：fee_type
     * 符合ISO 4217标准的三位字母代码，默认人民币：CNY，详细列表请参见货币类型
     */
    public static final String FEE_TYPE_CNY = "CNY";

    /**
     * 微信签名：通过签名算法计算得出的签名值
     */
    public static final String FIELD_SIGN = "sign";
    public static final String FIELD_SIGN_TYPE = "sign_type";

    /**
     * 微信支付版本
     */
    public static final String WXPAYSDK_VERSION = "WXPaySDK/3.0.9";
    public static final String USER_AGENT = WXPAYSDK_VERSION +
            " (" + System.getProperty("os.arch") + " " + System.getProperty("os.name") + " " + System.getProperty("os.version") +
            ") Java/" + System.getProperty("java.version") + " HttpClient/" + HttpClient.class.getPackage().getImplementationVersion();

    /**
     * 作用：企业付款到零钱资金使用商户号余额资金<br>
     * 场景：用于企业向微信用户个人付款
     */
    public static final String TRANSFERS_URL_SUFFIX = "/mmpaymkttransfers/promotion/transfers";
    /**
     * 作用：商户平台-现金红包-发放普通红包<br>
     * 场景：现金红包发放后会以公众号消息的形式触达用户
     * 其他：需要证书
     */
    public static final String SENDREDPACK_URL_SUFFIX = "/mmpaymkttransfers/sendredpack";
    /**
     * 作用：提交刷卡支付<br>
     * 场景：刷卡支付
     */
    public static final String MICROPAY_URL_SUFFIX     = "/pay/micropay";
    /**
     * 作用：统一下单<br>
     * 场景：公共号支付、扫码支付、APP支付
     */
    public static final String UNIFIEDORDER_URL_SUFFIX = "/pay/unifiedorder";
    /**
     * 作用：查询订单<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     */
    public static final String ORDERQUERY_URL_SUFFIX   = "/pay/orderquery";
    /**
     * 作用：撤销订单<br>
     * 场景：刷卡支付<br>
     * 其他：需要证书
     */
    public static final String REVERSE_URL_SUFFIX      = "/secapi/pay/reverse";
    /**
     * 作用：关闭订单<br>
     * 场景：公共号支付、扫码支付、APP支付
     */
    public static final String CLOSEORDER_URL_SUFFIX   = "/pay/closeorder";
    /**
     * 作用：申请退款<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付<br>
     * 其他：需要证书
     */
    public static final String REFUND_URL_SUFFIX       = "/secapi/pay/refund";
    /**
     * 作用：退款查询<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     */
    public static final String REFUNDQUERY_URL_SUFFIX  = "/pay/refundquery";
    /**
     * 作用：对账单下载<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付<br>
     * 其他：无论是否成功都返回Map。若成功，返回的Map中含有return_code、return_msg、data，
     *      其中return_code为`SUCCESS`，data为对账单数据。
     */
    public static final String DOWNLOADBILL_URL_SUFFIX = "/pay/downloadbill";
    /**
     * 作用：交易保障<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     */
    public static final String REPORT_URL_SUFFIX       = "/payitil/report";
    /**
     * 作用：转换短链接<br>
     * 场景：刷卡支付、扫码支付
     */
    public static final String SHORTURL_URL_SUFFIX     = "/tools/shorturl";
    /**
     * 作用：授权码查询OPENID接口<br>
     * 场景：刷卡支付
     */
    public static final String AUTHCODETOOPENID_URL_SUFFIX = "/tools/authcodetoopenid";

    /**
     * 沙箱说明：sandbox
     *
     * 微信支付沙箱环境，是提供给微信支付商户的开发者，用于模拟支付及回调通知。以验证商户是否理解回调通知、账单格式，以及是否对异常做了正确的处理。
     * ◆ 如何对接沙箱环境？
     * 1、修改商户自有程序或配置中，微信支付api的链接，如：被扫支付官网的url为：https://api.mch.weixin.qq.com/pay/micropay增加sandbox路径，变更为https://api.mch.weixin.qq.com/sandbox/pay/micropay， 即可接入沙箱验收环境，其它接口类似；
     * 2、在微信支付开发调试站点（站点链接：http://mch.weixin.qq.com/wiki/doc/api/index.php），按接口文档填入正确的支付参数，发起微信支付请求，完成支付；
     * 3、验收完成后，修改程序或配置中的api链接（重要！），去掉sandbox路径。对接现网环境。
     *
     * 说明地址：https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=23_1
     *         https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=21_2
     */
    public static final String SANDBOX_MICROPAY_URL_SUFFIX     = "/sandboxnew/pay/micropay";
    public static final String SANDBOX_UNIFIEDORDER_URL_SUFFIX = "/sandboxnew/pay/unifiedorder";
    public static final String SANDBOX_ORDERQUERY_URL_SUFFIX   = "/sandboxnew/pay/orderquery";
    public static final String SANDBOX_REVERSE_URL_SUFFIX      = "/sandboxnew/secapi/pay/reverse";
    public static final String SANDBOX_CLOSEORDER_URL_SUFFIX   = "/sandboxnew/pay/closeorder";
    public static final String SANDBOX_REFUND_URL_SUFFIX       = "/sandboxnew/secapi/pay/refund";
    public static final String SANDBOX_REFUNDQUERY_URL_SUFFIX  = "/sandboxnew/pay/refundquery";
    public static final String SANDBOX_DOWNLOADBILL_URL_SUFFIX = "/sandboxnew/pay/downloadbill";
    public static final String SANDBOX_REPORT_URL_SUFFIX       = "/sandboxnew/payitil/report";
    public static final String SANDBOX_SHORTURL_URL_SUFFIX     = "/sandboxnew/tools/shorturl";
    public static final String SANDBOX_AUTHCODETOOPENID_URL_SUFFIX = "/sandboxnew/tools/authcodetoopenid";
    public static final String SANDBOX_SENDREDPACK_URL_SUFFIX  = "/sandboxnew/mmpaymkttransfers/sendredpack";
    public static final String SANDBOX_TRANSFERS_URL_SUFFIX    = "/sandboxnew/mmpaymkttransfers/promotion/transfers";

}

