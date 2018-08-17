package controller;

import com.weixin.pay.constants.WXConstants;
import com.weixin.pay.constants.WXPayConstants;
import com.weixin.pay.util.AESUtil;
import com.weixin.pay.util.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 微信支付Controller
 *
 * @author yclimb
 * @date 2018/6/15
 */
@Slf4j
@RestController
@RequestMapping("/weixin/pay")
public class WXPayController {

    /**
     * 返回成功xml
     */
    private String resSuccessXml = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";

    /**
     * 返回失败xml
     */
    private String resFailXml = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[报文为空]]></return_msg></xml>";

    /**
     * 该链接是通过【统一下单API】中提交的参数notify_url设置，如果链接无法访问，商户将无法接收到微信通知。
     * 通知url必须为直接可访问的url，不能携带参数。示例：notify_url：“https://pay.weixin.qq.com/wxpay/pay.action”
     * <p>
     * 支付完成后，微信会把相关支付结果和用户信息发送给商户，商户需要接收处理，并返回应答。
     * 对后台通知交互时，如果微信收到商户的应答不是成功或超时，微信认为通知失败，微信会通过一定的策略定期重新发起通知，尽可能提高通知的成功率，但微信不保证通知最终能成功。
     * （通知频率为15/15/30/180/1800/1800/1800/1800/3600，单位：秒）
     * 注意：同样的通知可能会多次发送给商户系统。商户系统必须能够正确处理重复的通知。
     * 推荐的做法是，当收到通知进行处理时，首先检查对应业务数据的状态，判断该通知是否已经处理过，如果没有处理过再进行处理，如果处理过直接返回结果成功。在对业务数据进行状态检查和处理之前，要采用数据锁进行并发控制，以避免函数重入造成的数据混乱。
     * 特别提醒：商户系统对于支付结果通知的内容一定要做签名验证，防止数据泄漏导致出现“假通知”，造成资金损失。
     *
     * @author yclimb
     * @date 2018/6/15
     */
    /*@ApiOperation(value = "微信支付|支付回调接口", httpMethod = "POST", notes = "该链接是通过【统一下单API】中提交的参数notify_url设置，如果链接无法访问，商户将无法接收到微信通知。")*/
    @RequestMapping("/wxnotify")
    public void wxnotify(HttpServletRequest request, HttpServletResponse response) {

        String resXml = "";
        InputStream inStream;
        try {

            inStream = request.getInputStream();
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }

            WXPayUtil.getLogger().info("wxnotify:微信支付----start----");

            // 获取微信调用我们notify_url的返回信息
            String result = new String(outSteam.toByteArray(), "utf-8");
            WXPayUtil.getLogger().info("wxnotify:微信支付----result----=" + result);

            // 关闭流
            outSteam.close();
            inStream.close();

            // xml转换为map
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            boolean isSuccess = false;
            if (WXPayConstants.SUCCESS.equalsIgnoreCase(map.get(WXPayConstants.RESULT_CODE))) {

                WXPayUtil.getLogger().info("wxnotify:微信支付----返回成功");

                if (WXPayUtil.isSignatureValid(map, WXPayConstants.API_KEY)) {

                    // 订单处理 操作 orderconroller 的回写操作?
                    WXPayUtil.getLogger().info("wxnotify:微信支付----验证签名成功");

                    // 通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.
                    resXml = resSuccessXml;
                    isSuccess = true;

                } else {
                    WXPayUtil.getLogger().error("wxnotify:微信支付----判断签名错误");
                }

            } else {
                WXPayUtil.getLogger().error("wxnotify:支付失败,错误信息：" + map.get(WXPayConstants.ERR_CODE_DES));
                resXml = resFailXml;
            }

            /*// 根据付款单号查询付款记录
            Payment payment = paymentService.queryPaymentByFlowNumer(map.get("out_trade_no"), PaymentConstantEnum.PAYMENT_TYPE_ORDER.getCode());

            // 付款记录修改 & 记录付款日志
            int resultPay = paymentService.modifyPaymentByWxnotify(payment, isSuccess);
            if (resultPay > 0) {
                // 处理业务 - 修改订单状态
                WXPayUtil.getLogger().info("wxnotify:微信支付回调：修改的订单===>" + map.get("out_trade_no"));
                int updateResult = tradeService.modifyWxnotifyByRelationId(payment.getRelationId(), payment.getPrepayId(), isSuccess);
                if (updateResult > 0) {
                    WXPayUtil.getLogger().info("wxnotify:微信支付回调：修改订单支付状态成功");
                } else {
                    WXPayUtil.getLogger().error("wxnotify:微信支付回调：修改订单支付状态失败");
                }
            }*/

        } catch (Exception e) {
            WXPayUtil.getLogger().error("wxnotify:支付回调发布异常：", e);
        } finally {
            try {
                // 处理业务完毕
                BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
                out.write(resXml.getBytes());
                out.flush();
                out.close();
            } catch (IOException e) {
                WXPayUtil.getLogger().error("wxnotify:支付回调发布异常:out：", e);
            }
        }

    }

    /**
     * 退款结果通知
     * <p>
     * 在申请退款接口中上传参数“notify_url”以开通该功能
     * 如果链接无法访问，商户将无法接收到微信通知。
     * 通知url必须为直接可访问的url，不能携带参数。示例：notify_url：“https://pay.weixin.qq.com/wxpay/pay.action”
     * <p>
     * 当商户申请的退款有结果后，微信会把相关结果发送给商户，商户需要接收处理，并返回应答。
     * 对后台通知交互时，如果微信收到商户的应答不是成功或超时，微信认为通知失败，微信会通过一定的策略定期重新发起通知，尽可能提高通知的成功率，但微信不保证通知最终能成功。
     * （通知频率为15/15/30/180/1800/1800/1800/1800/3600，单位：秒）
     * 注意：同样的通知可能会多次发送给商户系统。商户系统必须能够正确处理重复的通知。
     * 推荐的做法是，当收到通知进行处理时，首先检查对应业务数据的状态，判断该通知是否已经处理过，如果没有处理过再进行处理，如果处理过直接返回结果成功。在对业务数据进行状态检查和处理之前，要采用数据锁进行并发控制，以避免函数重入造成的数据混乱。
     * 特别说明：退款结果对重要的数据进行了加密，商户需要用商户秘钥进行解密后才能获得结果通知的内容
     * @param request req
     * @param response resp
     * @return res xml
     *
     * @author yclimb
     * @date 2018/6/21
     */
    /*@ApiOperation(value = "微信支付|微信退款回调接口", httpMethod = "POST", notes = "该链接是通过【微信退款API】中提交的参数notify_url设置，如果参数中传了notify_url，则商户平台上配置的回调地址将不会生效。")*/
    @RequestMapping("/refund")
    public void refund(HttpServletRequest request, HttpServletResponse response) {

        String resXml = "";
        InputStream inStream;
        try {

            inStream = request.getInputStream();
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            WXPayUtil.getLogger().info("refund:微信退款----start----");

            // 获取微信调用我们notify_url的返回信息
            String result = new String(outSteam.toByteArray(), "utf-8");
            WXPayUtil.getLogger().info("refund:微信退款----result----=" + result);

            // 关闭流
            outSteam.close();
            inStream.close();

            // xml转换为map
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            boolean isSuccess = false;
            if (WXPayConstants.SUCCESS.equalsIgnoreCase(map.get(WXPayConstants.RETURN_CODE))) {

                WXPayUtil.getLogger().info("refund:微信退款----返回成功");

                /*if (WXPayUtil.isSignatureValid(map, WXPayConstants.API_KEY)) {*/

                    /** 以下字段在return_code为SUCCESS的时候有返回： **/
                    // 加密信息：加密信息请用商户秘钥进行解密，详见解密方式
                    String req_info = map.get("req_info");

                    /**
                     * 解密方式
                     * 解密步骤如下：
                     * （1）对加密串A做base64解码，得到加密串B
                     * （2）对商户key做md5，得到32位小写key* ( key设置路径：微信商户平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置 )
                     * （3）用key*对加密串B做AES-256-ECB解密（PKCS7Padding）
                     */
                    String resultStr = AESUtil.decryptData(req_info);

                    // WXPayUtil.getLogger().info("refund:解密后的字符串:" + resultStr);
                    Map<String, String> aesMap = WXPayUtil.xmlToMap(resultStr);


                    /** 以下为返回的加密字段： **/
                    //	商户退款单号	是	String(64)	1.21775E+27	商户退款单号
                    String out_refund_no = aesMap.get("out_refund_no");
                    //	退款状态	是	String(16)	SUCCESS	SUCCESS-退款成功、CHANGE-退款异常、REFUNDCLOSE—退款关闭
                    String refund_status = aesMap.get("refund_status");
                    /*//	微信订单号	是	String(32)	1.21775E+27	微信订单号
                    String transaction_id = null;
                    //	商户订单号	是	String(32)	1.21775E+27	商户系统内部的订单号
                    String out_trade_no = null;
                    //	微信退款单号	是	String(32)	1.21775E+27	微信退款单号
                    String refund_id = null;
                    //	订单金额	是	Int	100	订单总金额，单位为分，只能为整数，详见支付金额
                    String total_fee = null;
                    //	应结订单金额	否	Int	100	当该订单有使用非充值券时，返回此字段。应结订单金额=订单金额-非充值代金券金额，应结订单金额<=订单金额。
                    String settlement_total_fee = null;
                    //	申请退款金额	是	Int	100	退款总金额,单位为分
                    String refund_fee = null;
                    //	退款金额	是	Int	100	退款金额=申请退款金额-非充值代金券退款金额，退款金额<=申请退款金额
                    String settlement_refund_fee = null;*/

                    // 退款是否成功
                    if (!WXPayConstants.SUCCESS.equals(refund_status)) {
                        resXml = resFailXml;
                    } else {
                        // 通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.
                        resXml = resSuccessXml;
                        isSuccess = true;
                    }

                    /*// 根据付款单号查询付款记录
                    Payment payment = paymentService.queryPaymentByFlowNumer(out_refund_no, PaymentConstantEnum.PAYMENT_TYPE_REFUND.getCode());

                    // 付款记录修改 & 记录付款日志
                    int resultPay = paymentService.modifyPaymentByWxnotify(payment, isSuccess);
                    if (resultPay > 0) {

                        // 退款订单记录
                        List<PaymentOrderRefund> paymentOrderRefundList = paymentOrderRefundService.queryListByPaymentId(payment.getId());

                        // 处理业务 - 修改订单状态
                        WXPayUtil.getLogger().info("refund:微信支付回调：修改的订单===>" + out_refund_no);
                        int updateResult = tradeService.modifyWxrefundByRelationId(payment.getRelationId(), paymentOrderRefundList, isSuccess);
                        if (updateResult > 0) {
                            WXPayUtil.getLogger().info("refund:微信支付回调：修改订单支付状态成功");
                        } else {
                            WXPayUtil.getLogger().error("refund:微信支付回调：修改订单支付状态失败");
                        }
                    }*/

                /*} else {
                    WXPayUtil.getLogger().error("refund:微信支付----判断签名错误");
                }*/

            } else {
                WXPayUtil.getLogger().error("refund:支付失败,错误信息：" + map.get(WXPayConstants.RETURN_MSG));
                resXml = resFailXml;
            }

        } catch (Exception e) {
            WXPayUtil.getLogger().error("refund:微信退款回调发布异常：", e);
        } finally {
            try {
                // 处理业务完毕
                BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
                out.write(resXml.getBytes());
                out.flush();
                out.close();
            } catch (IOException e) {
                WXPayUtil.getLogger().error("refund:微信退款回调发布异常:out：", e);
            }
        }
    }

    /**
     * 企业付款到零钱
     * @return msg
     *
     * @author yclimb
     * @date 2018/7/30
     */
    /*@Token(remove = true)
    @ApiOperation(value = "微信支付|企业付款到零钱", httpMethod = "POST", notes = "用于企业向微信用户个人付款")*/
    @PostMapping("/transfers")
    public String transfers(HttpServletRequest request) {
        try {
            String remoteAddr = request.getRemoteAddr();
            return WXConstants.SUCCESS;
        } catch (Exception e) {
            WXPayUtil.getLogger().error("transfers:微信提现支付异常：", e);
        }
        return WXConstants.ERROR;
    }

}
