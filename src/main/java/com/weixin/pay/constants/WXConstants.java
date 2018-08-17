package com.weixin.pay.constants;

/**
 * 微信基础常量类
 *
 * @author yclimb
 * @date 2018/8/17
 */
public class WXConstants {

    /**
     * 对于前端访问返回参数，本例使用string，推荐自主封装json对象
     */
    public static final String SUCCESS = "success";
    public static final String ERROR = "error";

    /**
     * 授权作用域  不弹出授权页面，直接跳转，只能获取用户openid
     **/
    public static final String OAUTH_BASE_SCOPE = "snsapi_base";

    /**
     * 授权作用域  弹出授权页面 能获取昵称、头像等信息
     **/
    public static final String OAUTH_USERINFO_SCOPE = "snsapi_userinfo";

    /**
     * 网页授权 重定向后会带上state参数
     */
    public static final String OAUTH_STATE = "xxx";

    /**
     * 微信全局accessToken
     */
    public static final String WECHAT_ACCESSTOKEN = OAUTH_STATE + ":wx:accessToken:";

    /**
     * 微信全局accessTokenLock
     */
    public static final String WECHAT_ACCESSTOKEN_LOCK = OAUTH_STATE + ":wx:accessTokenLock:";

    /**
     * 微信网页授权openid，时限：7200秒
     */
    public static final String WECHAT_JSAPI_OPENID = OAUTH_STATE + ":wx:jsapi:openid:";

}
