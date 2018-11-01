package com.weixin.pay.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.weixin.pay.card.CardBgColorEnum;
import com.weixin.pay.constants.WXConstants;
import com.weixin.pay.constants.WXPayConstants;
import com.weixin.pay.constants.WXURL;
import com.weixin.pay.redis.RedisKeyEnum;
import com.weixin.pay.redis.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 微信小工具类
 *
 * @author yclimb
 * @date 2018/8/17
 */
@Slf4j
@Component
public class WXUtils {

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 获取微信全局accessToken
     *
     * @param code 标识
     * @return accessToken
     */
    public String getAccessToken(String code) {

        // 取redis数据
        String key = WXConstants.WECHAT_ACCESSTOKEN + code;
        String accessToken = (String) redisTemplate.opsForValue().get(key);
        if (accessToken != null) {
            return accessToken;
        }

        // 通过接口取得access_token
        JSONObject jsonObject = restTemplate.getForObject(MessageFormat.format(WXURL.BASE_ACCESS_TOKEN, WXPayConstants.APP_ID, WXPayConstants.SECRET), JSONObject.class);
        String token = (String) jsonObject.get("access_token");
        if (StringUtils.isNotBlank(token)) {
            // 存储redis
            redisTemplate.opsForValue().set(key, token, 7000, TimeUnit.SECONDS);
            return token;
        } else {
            log.error("获取微信accessToken出错，微信返回信息为：[{}]", jsonObject.toString());
        }
        return null;
    }

    /**
     * 获取小程序静默登录返回信息
     *
     * @param code code
     * @param appId appId
     * @param appSecret appSecret
     * @return json
     */
    public JSONObject getMiniBaseUserInfo(String code, String appId, String appSecret) {
        log.info("getMiniBaseUserInfo:params:[{}]", code);
        String data = restTemplate.getForObject(WXURL.WX_MINI_LOGIN, String.class, appId, appSecret, code);
        log.info("getMiniBaseUserInfo:result:[{}]", data);
        return JSONObject.parseObject(data);

    }

    /**
     * 网页授权获取用户信息时用于获取access_token以及openid
     * 请求路径：https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code(最后一个参数不变)
     * @param code c
     * @return access_token json obj
     *
     * @author yclimb
     * @date 2018/7/30
     */
    public JSONObject getJsapiAccessTokenByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        try {
            // 获取access_token
            String access_token_json = restTemplate.getForObject(WXURL.OAUTH_ACCESS_TOKEN_URL, String.class,
                    WXPayConstants.APP_ID_XXX, WXPayConstants.SECRET_XXX, code);
            log.info("getAccessToken:access_token_json:{}", access_token_json);
            if (StringUtils.isBlank(access_token_json)) {
                return null;
            }
            JSONObject jsonObject = JSON.parseObject(access_token_json);
            if (StringUtils.isBlank(jsonObject.getString("access_token"))) {
                return null;
            }
            return jsonObject;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 通过access_token和openid请求获取用户信息
     * 请求路径：https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN
     * @param access_token t
     * @param openid o
     * @return userinfo json obj
     *
     * @author yclimb
     * @date 2018/7/30
     */
    public JSONObject getJsapiUserinfo(String access_token, String openid) {
        if (StringUtils.isBlank(access_token) || StringUtils.isBlank(openid)) {
            return null;
        }
        try {
            // 获取access_token和openid
            String userinfo_json = restTemplate.getForObject(WXURL.OAUTH_GET_USERINFO_URL, String.class, access_token, openid);
            log.info("getUserinfo:userinfo_json:{}", userinfo_json);
            if (StringUtils.isBlank(userinfo_json)) {
                return null;
            }
            JSONObject jsonObject = JSON.parseObject(userinfo_json);
            if (0 != jsonObject.getIntValue("errcode")) {
                return null;
            }
            return jsonObject;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 生成带参数的小程序二维码[]
     *
     * @param scene 参数
     * @param page  小程序页面
     * @return img path
     */
    public String getWxMiniQRImg(String scene, String page) {
        InputStream inputStream = null;
        String imgUrl = "";
        try {

            // redis key
            String redisKey = RedisKeyUtil.keyBuilder(RedisKeyEnum.XXX_MINI_WX_CODE, scene + RedisKeyUtil.KEY_SPLIT_CHAR + page);

            // 从redis中获取缓存图片
            Object obj = redisTemplate.opsForValue().get(redisKey);
            if (obj != null) {
                return obj.toString();
            }

            // 获取微信永久无限制二维码
            byte[] code = this.getwxacodeunlimit(scene, page);
            if (code == null || code.length <= 0) {
                return imgUrl;
            }

            // 将返回字节数组转为输入流
            inputStream = new ByteArrayInputStream(code);

            // 取得uuid的文件名称
            String newFileName = UUID.randomUUID().toString().replaceAll("-", "").replace(".", "") + ".png";
            log.info("getWxMiniQRImg:fileName:" + newFileName);

            // 上传图片到OSS服务器
            // imgUrl = ossUtils.uploadOss(inputStream, ossUtils.getImgPathYYYYMMDD(), newFileName);

            // 图片为空直接返回
            if (StringUtils.isBlank(imgUrl)) {
                return imgUrl;
            }
            // 设置到redis中，下次取直接拿缓存即可，防止多次生成
            redisTemplate.opsForValue().set(redisKey, imgUrl);

        } catch (Exception e) {
            log.error("getWxMiniQRImg:调用小程序生成微信永久小程序码URL接口异常", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return imgUrl;
    }

    /**
     * 获取 application/json;charset=UTF-8 的 HttpHeaders 对象
     *
     * @return HttpHeaders
     * @author yclimb
     * @date 2018/7/18
     */
    public HttpHeaders getHttpHeadersUTF8JSON() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    /**
     * 作用：生成永久无限制微信二维码<br>
     * 场景：微信二维码生成，根据参数和页面配置微信二维码，返回二维码字节流
     * 接口链接：https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=ACCESS_TOKEN
     * 接口文档地址：https://developers.weixin.qq.com/miniprogram/dev/api/qrcode.html
     *
     * @param scene 最大32个可见字符，只支持数字，大小写英文以及部分特殊字符：!#$&'()*+,/:;=?@-._~，其它字符请自行编码为合法字符（因不支持%，中文无法使用 urlencode 处理，请使用其他编码方式）
     * @param page  必须是已经发布的小程序存在的页面（否则报错），例如 "pages/index/index" ,根路径前不要填加'/',不能携带参数（参数请放在scene字段里），如果不填写这个字段，默认跳主页面
     * @return 二维码字节流
     * @author yclimb
     * @date 2018/7/18
     */
    public byte[] getwxacodeunlimit(String scene, String page) {
        try {

            // 获取access token
            String accessToken = this.getAccessToken("xxx");

            // 拼接传入参数
            Map<String, Object> param = new HashMap<>(5);
            param.put("scene", scene);
            param.put("page", page);
            // 默认：430；二维码的宽度，最小为280
            param.put("width", 280);
            // 默认：false；自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调
            param.put("auto_color", false);

            // 默认：{"r":"0","g":"0","b":"0"}；二维码图片颜色参数，auto_color 为 false 时生效，使用 rgb 设置颜色 例如 {"r":"xxx","g":"xxx","b":"xxx"} 十进制表示
            Map<String, Object> line_color = new HashMap<>(3);
            line_color.put("r", 0);
            line_color.put("g", 0);
            line_color.put("b", 0);
            param.put("line_color", line_color);

            // map转换为json传输
            String jsonParam = JSON.toJSONString(param);
            log.info("getwxacodeunlimit:param:" + jsonParam);

            // 请求微信接口，得到返回结果[二进制流]
            HttpEntity<String> entity = new HttpEntity<>(jsonParam, this.getHttpHeadersUTF8JSON());
            ResponseEntity<byte[]> responseEntity = restTemplate.postForEntity(WXURL.WX_MINI_QR_CODE_URL, entity, byte[].class, accessToken);

            // return byte[]
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("getwxacodeunlimit:postForEntity:" + e.getMessage(), e);
        }

        return null;
    }

    /**
     * 创建支付后领取立减金活动接口
     * 通过此接口创建立减金活动。
     * 将已创建的代金券cardid、跳转小程序appid、发起支付的商户号等信息通过此接口创建立减金活动，成功返回活动id即为创建成功。
     * 接口地址：https://mp.weixin.qq.com/wiki?t=resource/res_main&id=21515658940X5pIn
     *
     * @param begin_time               活动开始时间，精确到秒
     * @param end_time                 活动结束时间，精确到秒
     * @param gift_num                 单个礼包社交立减金数量（3-15个）
     * @param max_partic_times_act     每个用户活动期间最大领取次数,最大为50，默认为1
     * @param max_partic_times_one_day 每个用户活动期间单日最大领取次数,最大为50，默认为1
     * @param card_id                  卡券ID
     * @param min_amt                  最少支付金额，单位是元
     * @param membership_appid         奖品指定的会员卡appid。如用户标签有选择商户会员，则需要填写会员卡appid，该appid需要跟所有发放商户号有绑定关系。
     * @param new_tinyapp_user         可以指定为是否小程序新用户（membership_appid为空、new_tinyapp_user为false时，指定为所有用户）
     * @return json
     * @author yclimb
     * @date 2018/9/18
     */
    public JSONObject createCardActivity(String begin_time, String end_time, int gift_num, int max_partic_times_act,
                                         int max_partic_times_one_day, String card_id, String min_amt,
                                         String membership_appid, boolean new_tinyapp_user) {
        try {

            // 创建活动接口之前的验证
            String msg = checkCardActivity(begin_time, end_time, gift_num, max_partic_times_act, max_partic_times_one_day, min_amt);
            if (null != msg) {
                JSONObject resultJson = new JSONObject(2);
                resultJson.put("errcode", "1");
                resultJson.put("errmsg", msg);
                return resultJson;
            }

            // 获取[商户名称]公众号的 access_token
            String accessToken = this.getAccessToken(WXConstants.WX_MINI_PROGRAM_CODE);

            // 调用接口传入参数
            JSONObject paramJson = new JSONObject(1);

            // info 包含 basic_info、card_info_list、custom_info
            JSONObject info = new JSONObject(3);

            // 基础信息对象
            JSONObject basic_info = new JSONObject(8);
            // activity_bg_color	是	活动封面的背景颜色，可参考：选取卡券背景颜色
            basic_info.put("activity_bg_color", CardBgColorEnum.COLOR_090.getBgName());
            // activity_tinyappid	是	用户点击链接后可静默添加到列表的小程序appid；
            basic_info.put("activity_tinyappid", WXPayConstants.APP_ID);
            // mch_code	是	支付商户号
            basic_info.put("mch_code", WXPayConstants.MCH_ID);
            // begin_time	是	活动开始时间，精确到秒（unix时间戳）
            basic_info.put("begin_time", DateTimeUtil.getTenTimeByDate(begin_time));
            // end_time	是	活动结束时间，精确到秒（unix时间戳）
            basic_info.put("end_time", DateTimeUtil.getTenTimeByDate(end_time));
            // gift_num	是	单个礼包社交立减金数量（3-15个）
            basic_info.put("gift_num", gift_num);
            // max_partic_times_act	否	每个用户活动期间最大领取次数,最大为50，不填默认为1
            basic_info.put("max_partic_times_act", max_partic_times_act);
            // max_partic_times_one_day	否	每个用户活动期间单日最大领取次数,最大为50，不填默认为1
            basic_info.put("max_partic_times_one_day", max_partic_times_one_day);

            // card_info_list	是	可以配置两种发放规则：小程序新老用户、新老会员
            JSONArray card_info_list = new JSONArray(1);
            JSONObject card_info = new JSONObject(3);
            // card_id	是	卡券ID
            card_info.put("card_id", card_id);
            // min_amt	是	最少支付金额，单位是分
            card_info.put("min_amt", String.valueOf(new BigDecimal(min_amt).multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).intValue()));
            /*
             * membership_appid	是	奖品指定的会员卡appid。如用户标签有选择商户会员，则需要填写会员卡appid，该appid需要跟所有发放商户号有绑定关系。
             * new_tinyapp_user	是	可以指定为是否小程序新用户
             * total_user	是	可以指定为所有用户
             * membership_appid、new_tinyapp_user、total_user以上字段3选1，未选择请勿填，不必故意填写false
             */
            if (StringUtils.isNotBlank(membership_appid)) {
                card_info.put("membership_appid", membership_appid);
            } else {
                if (new_tinyapp_user) {
                    card_info.put("new_tinyapp_user", true);
                } else {
                    card_info.put("total_user", true);
                }
            }
            card_info_list.add(card_info);

            // 自定义字段，表示支付后领券
            JSONObject custom_info = new JSONObject(1);
            custom_info.put("type", "AFTER_PAY_PACKAGE");

            // 拼装json对象
            info.put("basic_info", basic_info);
            info.put("card_info_list", card_info_list);
            info.put("custom_info", custom_info);
            paramJson.put("info", info);

            // 请求微信接口，得到返回结果[json]
            HttpEntity<JSONObject> entity = new HttpEntity<>(paramJson, this.getHttpHeadersUTF8JSON());
            JSONObject resultJson = restTemplate.postForObject(WXURL.WX_CARD_ACTIVITY_CREATE_URL, entity, JSONObject.class, accessToken);

            // {"errcode":0,"errmsg":"ok","activity_id":"4728935"}
            System.out.println(resultJson.toJSONString());

            return resultJson;
        } catch (Exception e) {
            WXPayUtil.getLogger().error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 创建活动接口之前的验证
     *
     * @param begin_time               活动开始时间，精确到秒
     * @param end_time                 活动结束时间，精确到秒
     * @param gift_num                 单个礼包社交立减金数量（3-15个）
     * @param max_partic_times_act     每个用户活动期间最大领取次数,最大为50，默认为1
     * @param max_partic_times_one_day 每个用户活动期间单日最大领取次数,最大为50，默认为1
     * @param min_amt                  最少支付金额，单位是元
     * @return msg str
     * @author yclimb
     * @date 2018/9/18
     */
    public String checkCardActivity(String begin_time, String end_time, int gift_num, int max_partic_times_act,
                                    int max_partic_times_one_day, String min_amt) {

        // 开始时间不能小于结束时间
        if (DateTimeUtil.latterThan(end_time, begin_time, DateTimeUtil.TIME_FORMAT_NORMAL)) {
            return "活动开始时间不能小于活动结束时间";
        }

        // 单个礼包社交立减金数量（3-15个）
        if (gift_num < 3 || gift_num > 15) {
            return "单个礼包社交立减金数量（3-15个）";
        }

        // 每个用户活动期间最大领取次数,最大为50，默认为1
        if (max_partic_times_act <= 0 || max_partic_times_act > 50) {
            return "每个用户活动期间最大领取次数,最大为50，默认为1";
        }

        // 每个用户活动期间单日最大领取次数,最大为50，默认为1
        if (max_partic_times_one_day <= 0 || max_partic_times_one_day > 50) {
            return "每个用户活动期间单日最大领取次数,最大为50，默认为1";
        }

        // 最少支付金额，单位是元
        if (BigDecimal.ONE.compareTo(new BigDecimal(min_amt)) > 0) {
            return "最少支付金额必须大于1元";
        }

        return null;
    }

    /**
     * 获取卡券 api_ticket 的 api
     * 请求路径：https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={0}&type=wx_card
     *
     * @param access_token token
     * @return api_ticket json obj
     * @author yclimb
     * @date 2018/9/21
     */
    public String getWxCardApiTicket(String access_token) {
        if (StringUtils.isBlank(access_token)) {
            return null;
        }
        try {

            // redis key
            String redisKey = RedisKeyUtil.keyBuilder(RedisKeyEnum.IMALL_WXCARD_APITICKET, access_token);

            // 从redis中获取缓存
            Object obj = redisTemplate.opsForValue().get(redisKey);
            if (obj != null) {
                return obj.toString();
            }

            // 获取卡券 api_ticket
            String api_ticket = restTemplate.getForObject(WXURL.BASE_API_TICKET, String.class, access_token);
            WXPayUtil.getLogger().info("getWxCardApiTicket:api_ticket:{}", api_ticket);
            if (StringUtils.isBlank(api_ticket)) {
                return null;
            }
            JSONObject jsonObject = JSON.parseObject(api_ticket);
            if (0 != jsonObject.getIntValue("errcode")) {
                return null;
            }

            // 设置到redis中，下次取直接拿缓存即可，防止多次生成
            String ticket = jsonObject.getString("ticket");
            redisTemplate.opsForValue().set(redisKey, ticket, jsonObject.getIntValue("expires_in"), TimeUnit.SECONDS);

            return ticket;
        } catch (Exception e) {
            WXPayUtil.getLogger().error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取卡券 api_ticket 的 api
     * 请求路径：https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={0}&type=jsapi
     *
     * @param access_token token
     * @return api_ticket json obj
     * @author yclimb
     * @date 2018/9/25
     */
    public String getWxApiTicket(String access_token) {
        if (StringUtils.isBlank(access_token)) {
            return null;
        }
        try {

            // redis key
            String redisKey = RedisKeyUtil.keyBuilder(RedisKeyEnum.IMALL_WX_APITICKET, access_token);

            // 从redis中获取缓存
            Object obj = redisTemplate.opsForValue().get(redisKey);
            if (obj != null) {
                return obj.toString();
            }

            // 获取 api_ticket
            String api_ticket = restTemplate.getForObject(WXURL.BASE_JSAPI_TICKET, String.class, access_token);
            WXPayUtil.getLogger().info("getWxApiTicket:api_ticket:{}", api_ticket);
            if (StringUtils.isBlank(api_ticket)) {
                return null;
            }
            JSONObject jsonObject = JSON.parseObject(api_ticket);
            if (0 != jsonObject.getIntValue("errcode")) {
                return null;
            }

            // 设置到redis中，下次取直接拿缓存即可，防止多次生成
            String ticket = jsonObject.getString("ticket");
            redisTemplate.opsForValue().set(redisKey, ticket, jsonObject.getIntValue("expires_in"), TimeUnit.SECONDS);

            return ticket;
        } catch (Exception e) {
            WXPayUtil.getLogger().error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 根据代金券批次ID得到组合的cardList
     *
     * @param cardId 卡包ID
     * @return cardList
     * @author yclimb
     * @date 2018/9/21
     */
    public JSONArray getCardList(String cardId) {
        if (StringUtils.isBlank(cardId)) {
            return null;
        }
        try {

            // 获取[商户名称]公众号的 access_token
            String accessToken = this.getAccessToken(WXConstants.WX_MINI_PROGRAM_CODE);
            String timestamp = String.valueOf(WXPayUtil.getCurrentTimestamp());
            String nonce_str = WXPayUtil.generateNonceStr();

            // 卡券的扩展参数。需进行 JSON 序列化为字符串传入
            JSONObject cardExt = new JSONObject();
            //cardExt.put("code", "");
            //cardExt.put("openid", "");
            //cardExt.put("fixed_begintimestamp", "");
            //cardExt.put("outer_str", "");
            cardExt.put("timestamp", timestamp);
            cardExt.put("nonce_str", nonce_str);

            /**
             * 1.将 api_ticket、timestamp、card_id、code、openid、nonce_str的value值进行字符串的字典序排序。
             * 2.将所有参数字符串拼接成一个字符串进行sha1加密，得到signature。
             * 3.signature中的timestamp，nonce字段和card_ext中的timestamp，nonce_str字段必须保持一致。
             */
            Map<String, String> map = new HashMap<>(8);
            //map.put("code", "");
            //map.put("openid", "");
            map.put("api_ticket", this.getWxCardApiTicket(accessToken));
            map.put("timestamp", timestamp);
            map.put("card_id", cardId);
            map.put("nonce_str", nonce_str);
            cardExt.put("signature", WXPayUtil.SHA1(WXPayUtil.dictionaryOrder(map, 2)));

            // 卡券对象
            JSONObject cardInfo = new JSONObject();
            cardInfo.put("cardId", cardId);
            cardInfo.put("cardExt", cardExt.toJSONString());

            // 需要添加的卡券列表
            JSONArray cardList = new JSONArray(1);
            cardList.add(cardInfo);

            return cardList;
        } catch (Exception e) {
            WXPayUtil.getLogger().error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取微信签名信息
     *
     * @param requestUrl 请求页面地址
     * @param appid      appid
     * @param code       code
     * @return 返回map：noncestr:随机字符串；timestamp：签名时间戳；appid;微信公众号Id;signature:签名串
     * @author yclimb
     * @date 2018/9/25
     */
    public Map<String, Object> getSignature(String requestUrl, String appid, String code) {
        Map<String, Object> map = new HashMap<>();
        try {

            // 获取公众号的 access_token、jsapi_ticket
            String accessToken = this.getAccessToken(code);
            String jsapi_ticket = this.getWxApiTicket(accessToken);
            String nonce_str = WXPayUtil.generateNonceStr();
            String timestamp = Long.toString(WXPayUtil.getCurrentTimestamp());

            // 注意这里参数名必须全部小写，且必须有序
            String dataStr = "jsapi_ticket=" + jsapi_ticket +
                    "&noncestr=" + nonce_str +
                    "&timestamp=" + timestamp +
                    "&url=" + requestUrl;
            WXPayUtil.getLogger().info(dataStr);

            String signature = WXPayUtil.SHA1(dataStr);
            map.put("noncestr", nonce_str);
            map.put("timestamp", timestamp);
            map.put("appid", appid);
            map.put("signature", signature);
        } catch (Exception e) {
            WXPayUtil.getLogger().error(e.getMessage(), e);
        }

        return map;
    }

}
