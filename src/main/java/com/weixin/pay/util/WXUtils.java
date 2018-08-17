package com.weixin.pay.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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

}
