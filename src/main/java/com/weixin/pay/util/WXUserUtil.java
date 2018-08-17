package com.weixin.pay.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

/**
 * 对于微信用户名称emoji等特殊字符处理
 *
 * @author yclimb
 * @date 2018/8/17
 */
@Slf4j
public class WXUserUtil {

    /**
     * 编码用户昵称
     *
     * @param nickName 未编码等名称
     * @return base64 str
     */
    public static String encodeNickName(String nickName) {
        try {
            return Base64.encodeBase64String(nickName.toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("编码用户昵称报错", e);
        }
        return null;
    }

    /**
     * 解码用户昵称
     *
     * @param nickName base64 str
     * @return 原始名称
     */
    public static String decodeNickName(String nickName) {
        return new String(Base64.decodeBase64(nickName));
    }
}
