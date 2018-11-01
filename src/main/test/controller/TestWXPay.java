package controller;

import com.weixin.pay.WXPay;
import com.weixin.pay.WXPayConfigImpl;
import com.weixin.pay.XxxWXPayConfigImpl;
import com.weixin.pay.util.WXPayUtil;
import com.weixin.pay.util.WXUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一接口测试
 */
public class TestWXPay {

    private WXPay wxpay;
    private WXPayConfigImpl config;
    private String out_trade_no;
    private String total_fee;

    public TestWXPay() throws Exception {
        config = WXPayConfigImpl.getInstance();
        // wxpay = new WXPay(config);
        wxpay = new WXPay(config, true, true);
        total_fee = "1.01";
        // out_trade_no = "201701017496748980290321";
        out_trade_no = "20180912004";
    }

    /**
     * 获取微信签名
     * @param url
     */
    private void getWeixinMap(String url) {

        /*Map<String, Object> map = new HashMap<>();

        try {
            map = WXSignatureUtil.getSignature(request, url);
        } catch (IOException | CloneNotSupportedException e) {
            e.printStackTrace();
            System.out.println("获取微信签名信息异常！" + e.getMessage());
        }
        model.addAttribute("noncestr", map.get("noncestr"));
        model.addAttribute("timestamp", map.get("timestamp"));
        model.addAttribute("appid", map.get("appid"));
        model.addAttribute("signature", map.get("signature"));*/
    }

    /**
     * 获取微信签名
     */
    private void getWeixinMap() {
        getWeixinMap(getRequestURL());
    }

    private String getRequestURL() {
        String url = null;
        /*if (null == request.getQueryString()) {
            url = request.getRequestURL().toString();
        } else {
            url = request.getRequestURL() + "?" + request.getQueryString();
        }*/
        return url;
    }

    /**
     * 扫码支付  下单
     */
    private void doUnifiedOrder() throws Exception {
        WXPay wxPay = new WXPay(WXPayConfigImpl.getInstance());
        Map<String, String> resultMap = wxPay.unifiedOrder("https://api.uat.iyuedian.com/iyd-imall-manage/imall/v1/weixin/pay/wxnotify",
                "oPR7T5PFjcfgugIu2abQG6ijQGV4", "悦店-测试商品", WXPayUtil.getPayNo(), "10.01", "127.0.0.1",
                "vip", "",null,null);

        String prepay_id = resultMap.get("prepay_id");
        String nonce_str = resultMap.get("nonce_str");
        Map<String, String> map = wxPay.chooseWXPayMap(prepay_id, nonce_str);
        System.out.println("map:" + map);
    }


    private void doOrderClose() {
        System.out.println("关闭订单");
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("out_trade_no", out_trade_no);
        try {
            Map<String, String> r = wxpay.closeOrder(data);
            System.out.println(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doOrderQuery() {
        System.out.println("查询订单");
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("out_trade_no", out_trade_no);
//        data.put("transaction_id", "4008852001201608221962061594");
        try {
            Map<String, String> r = wxpay.orderQuery(data);
            System.out.println(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doOrderReverse() {
        System.out.println("撤销");
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("out_trade_no", out_trade_no);
//        data.put("transaction_id", "4008852001201608221962061594");
        try {
            Map<String, String> r = wxpay.reverse(data);
            System.out.println(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 长链接转短链接
     * 测试成功
     */
    private void doShortUrl() {
        String long_url = "weixin://wxpay/bizpayurl?pr=etxB4DY";
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("long_url", long_url);
        try {
            Map<String, String> r = wxpay.shortUrl(data);
            System.out.println(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 退款
     * 已测试
     */
    private void doRefund() {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("out_trade_no", out_trade_no);
        data.put("out_refund_no", out_trade_no);
        data.put("total_fee", total_fee);
        data.put("refund_fee", total_fee);
        data.put("refund_fee_type", "CNY");
        data.put("op_user_id", config.getMchID());

        try {
            Map<String, String> r = wxpay.refund(data);
            System.out.println(r);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 查询退款
     * 已经测试
     */
    private void doRefundQuery() {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("out_trade_no", out_trade_no);
        //data.put("transactionId", out_trade_no);
        data.put("out_refund_no", out_trade_no);
        //data.put("refund_id", out_trade_no);
        try {
            Map<String, String> r = wxpay.refundQuery(data);
            System.out.println(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 对账单下载
     * 已测试
     */
    private void doDownloadBill() {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("bill_date", "20161102");
        data.put("bill_type", "ALL");
        try {
            Map<String, String> r = wxpay.downloadBill(data);
            System.out.println(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取沙盒 sandbox_signkey
     *
     * @author yclimb
     * @date 2018/9/18
     */
    private void doGetSandboxSignKey() throws Exception {
        WXPayConfigImpl config = WXPayConfigImpl.getInstance();
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("mch_id", config.getMchID());
        data.put("nonce_str", WXPayUtil.generateNonceStr());
        String sign = WXPayUtil.generateSignature(data, config.getKey());
        data.put("sign", sign);
        WXPay wxPay = new WXPay(config);
        // String result = wxPay.requestWithoutCert("https://api.mch.weixin.qq.com/sandbox/pay/getsignkey", data, 10000, 10000);
        String result = wxPay.requestWithoutCert("/sandboxnew/pay/getsignkey", data, 10000, 10000);
        System.out.println(result);
    }

    private void doReport() {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("interface_url", "20160822");
        data.put("bill_type", "ALL");
    }

    /**
     * 小测试
     */
    private void test001() {
        String xmlStr="<xml><return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "<return_msg><![CDATA[OK]]></return_msg>\n" +
                "<appid><![CDATA[wx273fe72f2db863ed]]></appid>\n" +
                "<mch_id><![CDATA[1228845802]]></mch_id>\n" +
                "<nonce_str><![CDATA[lCXjx3wNx45HfTV2]]></nonce_str>\n" +
                "<sign><![CDATA[68D7573E006F0661FD2A77BA59124E87]]></sign>\n" +
                "<result_code><![CDATA[SUCCESS]]></result_code>\n" +
                "<openid><![CDATA[oZyc_uPx_oed7b4q1yKmj_3M2fTU]]></openid>\n" +
                "<is_subscribe><![CDATA[N]]></is_subscribe>\n" +
                "<trade_type><![CDATA[NATIVE]]></trade_type>\n" +
                "<bank_type><![CDATA[CFT]]></bank_type>\n" +
                "<total_fee>1</total_fee>\n" +
                "<fee_type><![CDATA[CNY]]></fee_type>\n" +
                "<transaction_id><![CDATA[4008852001201608221983528929]]></transaction_id>\n" +
                "<out_trade_no><![CDATA[20160822162018]]></out_trade_no>\n" +
                "<attach><![CDATA[]]></attach>\n" +
                "<time_end><![CDATA[20160822202556]]></time_end>\n" +
                "<trade_state><![CDATA[SUCCESS]]></trade_state>\n" +
                "<cash_fee>1</cash_fee>\n" +
                "</xml>";
        try {
            System.out.println(xmlStr);
            System.out.println("+++++++++++++++++");
            System.out.println(WXPayUtil.isSignatureValid(xmlStr, config.getKey()));
            Map<String, String> hm = WXPayUtil.xmlToMap(xmlStr);
            System.out.println("+++++++++++++++++");
            System.out.println(hm);
            System.out.println(hm.get("attach").length());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void testUnifiedOrderSpeed() throws Exception {
        TestWXPay dodo = new TestWXPay();

        for (int i=0; i<100; ++i) {
            long startTs = System.currentTimeMillis();
            out_trade_no = out_trade_no+i;
            dodo.doUnifiedOrder();
            long endTs = System.currentTimeMillis();
            System.out.println(endTs-startTs);
            Thread.sleep(1000);
        }

    }

    /**
     * 提现
     *
     * @author yclimb
     * @date 2018/9/18
     */
    public void doTranster() throws Exception {
        // 微信调用接口
        WXPay wxPay = new WXPay(WXPayConfigImpl.getInstance());
        Map<String, String> resultMap = wxPay.transfers("1507928321201809301504246520",
                "oPR7T5DWvXuhfKfyqNdi6MTQGaxo", "1.9", "测试退款", "127.0.0.1");
        System.out.println("wxPay.transfers:" + resultMap);
    }

    /**
     * 发送现金红包
     *
     * @author yclimb
     * @date 2018/9/18
     */
    private void sendRedPack() throws Exception {
        WXPay wxPay = new WXPay(WXPayConfigImpl.getInstance());
        wxPay.sendRedPack(WXPayUtil.getPayNo(), "obX_c0YRpT47zKcvq-ZYpjU6GFuA", "1",
                "活动名称", "红包祝福语", "备注", "127.0.0.1");
    }

    /**
     * 查询现金红包
     *
     * @author yclimb
     * @date 2018/9/18
     */
    private void getRedPackInfo() throws Exception {
        WXPay wxPay = new WXPay(WXPayConfigImpl.getInstance());
        wxPay.getRedPackInfo("1507928321201809171554055254");
    }

    /**
     * 发送代金券
     *
     * @author yclimb
     * @date 2018/9/18
     */
    private void sendCoupon() throws Exception {
        WXPay wxPay = new WXPay(WXPayConfigImpl.getInstance());
        wxPay.sendCoupon("9248266", WXPayUtil.getPayNo(), "obX_c0YRpT47zKcvq-ZYpjU6GFuA");
    }

    /**
     * 查询代金券信息
     *
     * @author yclimb
     * @date 2018/9/18
     */
    private void queryCouponsInfo() throws Exception {
        WXPay wxPay = new WXPay(XxxWXPayConfigImpl.getInstance());
        wxPay.queryCouponsInfo("3983069127", "9248266", "obX_c0YRpT47zKcvq-ZYpjU6GFuA");
    }

    /**
     * 查询代金券批次信息
     *
     * @author yclimb
     * @date 2018/9/18
     */
    private void queryCouponStock() throws Exception {
        WXPay wxPay = new WXPay(XxxWXPayConfigImpl.getInstance());
        wxPay.queryCouponStock("9248266");
    }

    /**
     * 创建支付后领取立减金活动接口
     *
     * @author yclimb
     * @date 2018/9/18
     */
    private void createCardActivity() {
        WXUtils wxUtils = new WXUtils();
        wxUtils.createCardActivity("2018-09-18 18:00:00", "2018-09-18 19:59:59", 3, 1,
                1, "pX2-vjpU_MT1gFDsP8lNl15PdaZE", "100",
                null, false);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("--------------->");

        TestWXPay dodo = new TestWXPay();
        //dodo.doGetSandboxSignKey();
        //dodo.doOrderQuery();
        //dodo.doRefundQuery();
        //dodo.doDownloadBill();
        //dodo.sendRedPack();
        //dodo.getRedPackInfo();
        //dodo.sendCoupon();
        //dodo.queryCouponsInfo();
        //dodo.queryCouponStock();
        //dodo.createCardActivity();
        //dodo.doUnifiedOrder();
        dodo.doTranster();


        // 沙箱环境测试
        //WXPay wxPay = new WXPay(WXPayConfigImpl.getInstance(), true, true);
        //WXPay wxPay = new WXPay(ChunboWXPayConfigImpl.getInstance());

        /*Map<String, String> resultMap = wxPay.refund("http://127.0.0.1:11000/weixin/pay/wxnotify", null,
                "20180912004", "20180912004", "5.52", "5.52", "测试退款");*/

        //System.out.println(resultMap);

        /*Map<String, String> resultMap = wxPay.refund(null, "10000", "10001", "1.01", "0.01", "测试微信退款");
        System.out.println(WXPayUtil.isSignatureValid(resultMap, WXPayConstants.API_KEY));*/


        System.out.println("<---------------");
    }

}
