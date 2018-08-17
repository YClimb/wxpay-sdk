package com.weixin.pay.constants;

/**
 * 微信基础URL链接
 *
 * @author yclimb
 * @date 2018/8/17
 */
public class WXURL {

    /**
     * 请求URL之获取jsapi_ticket
     */
    public static final String PAGE_URL_SIGN = "jsapi_ticket={0}&noncestr={1}&timestamp={2}&url={3}";

    /**
     * 请求URL之获取access_token
     */
    public static final String BASE_ACCESS_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={0}&secret={1}";

    /**
     * 请求URL之获取jsapi_ticket
     */
    public static final String BASE_JSAPI_TICKET = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={0}&type=jsapi";

    /**
     * 请求URL之创建菜单
     */
    public static final String MENU_CREATE = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token={0}";

    /**
     * 请求URL之查询菜单
     */
    public static final String MENU_QUERY = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token={0}";

    /**
     * 请求URL之删除菜单
     */
    public static final String MENU_DELETE = "https://api.weixin.qq.com/cgi-bin/menu/delete?access_token={0}";

    /**
     * 页面授权获取code地址
     */
    public static final String OAUTH_CODE_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=" + WXConstants.OAUTH_STATE + "#wechat_redirect";

    /**
     * 通过code换取网页授权access_token
     */
    public static final String OAUTH_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid={0}&secret={1}&code={2}&grant_type=authorization_code";

    /**
     * 页面授权获取指定微信号的基础信息
     */
    public static final String OAUTH_GET_USERINFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token={0}&openid={1}&lang=zh_CN";

    /**
     * 获取指定微信号的基础信息 通过全局access_token
     */
    public static final String GET_USERINFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";

    /**
     * 微信模板消息发送
     */
    public static final String WX_TEMPLATE_SEND_URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token={0}";

    /**
     * 微信客户消息发送
     */
    public static final String WX_CUSTMOER_SERVICE_SEND_URL = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token={0}";

    /***
     * 微信创建二维码ticket
     */
    public static final String WX_TICKET_CREATE = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token={0}";

    /**
     * 小程序登录校验
     */
    public static final String WX_MINI_LOGIN = "https://api.weixin.qq.com/sns/jscode2session?appid={0}&secret={1}&js_code={2}&grant_type=authorization_code";

    /**
     * 小程序模板信息
     */
    public static final String WX_MINI_TEMPLATE_MSG = "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send?access_token={0}";

    /**
     * 获取小程序二维码，通过该接口生成的小程序码，永久有效，数量暂无限制
     */
    public static final String WX_MINI_QR_CODE_URL = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token={0}";
}
