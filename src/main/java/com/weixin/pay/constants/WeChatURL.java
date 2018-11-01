package com.weixin.pay.constants;

/**
 * 微信公众号相关接口
 *
 * @author yclimb
 * @date 2018/11/1
 */
public interface WeChatURL {

    /**
     * 请求URL之获取jsapi_ticket
     */
    String PAGE_URL_SIGN = "jsapi_ticket={0}&noncestr={1}&timestamp={2}&url={3}";
    /**
     * 请求URL之获取access_token
     */
    String BASE_ACCESS_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={0}&secret={1}";
    /**
     * 请求URL之获取jsapi_ticket
     */
    String BASE_JSAPI_TICKET = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={0}&type=jsapi";
    /**
     * 请求URL之创建菜单
     */
    String MENU_CREATE = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token={0}";
    /**
     * 请求URL之查询菜单
     */
    String MENU_QUERY = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token={0}";
    /**
     * 请求URL之删除菜单
     */
    String MENU_DELETE = "https://api.weixin.qq.com/cgi-bin/menu/delete?access_token={0}";

    /**
     * 页面授权获取code地址
     */
    String OAUTH_CODE_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=" + WXConstants.OAUTH_STATE + "#wechat_redirect";

    /**
     * 通过code换取网页授权access_token
     */
    String OAUTH_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid={0}&secret={1}&code={2}&grant_type=authorization_code";

    /**
     * 页面授权获取指定微信号的基础信息
     */
    String OAUTH_GET_USERINFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token={0}&openid={1}&lang=zh_CN";

    /**
     * 获取指定微信号的基础信息 通过全局access_token
     */
    String GET_USERINFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";

    /**
     * 微信模板消息发送
     */
    String WX_TEMPLATE_SEND_URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token={0}";

    /**
     * 微信客户消息发送
     */
    String WX_CUSTMOER_SERVICE_SEND_URL = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token={}";

    /***
     * 微信创建二维码ticket
     */
    String WX_TICKET_CREATE = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token={0}";


    /**
     * 小程序登录校验
     */
    String WX_MINI_LOGIN = "https://api.weixin.qq.com/sns/jscode2session?appid={0}&secret={1}&js_code={2}&grant_type=authorization_code";

    /**
     * 小程序模板信息
     */
    String WX_MINI_TEMPLATE_MSG = "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send?access_token={ACCESS_TOKEN}";

    /**
     * 获取小程序二维码，通过该接口生成的小程序码，永久有效，数量暂无限制
     */
    String WX_MINI_QR_CODE_URL = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token={0}";

    /**
     * 创建支付后领取立减金活动接口
     * 通过此接口创建立减金活动。
     * 将已创建的代金券cardid、跳转小程序appid、发起支付的商户号等信息通过此接口创建立减金活动，成功返回活动id即为创建成功。
     * 接口地址：https://mp.weixin.qq.com/wiki?t=resource/res_main&id=21515658940X5pIn
     *
     * 协议：https
     * http请求方式: POST
     * 请求URL：https://api.weixin.qq.com/card/mkt/activity/create?access_token=ACCESS_TOKEN
     * POST数据格式：JSON
     */
    String WX_CARD_ACTIVITY_CREATE_URL = "https://api.weixin.qq.com/card/mkt/activity/create?access_token={0}";

    /**
     * 卡券签名和JSSDK的签名完全独立，两者的算法和意义完全不同，请不要混淆。
     * JSSDK的签名是使用所有JS接口都需要走的一层鉴权，用以标识调用者的身份，和卡券本身并无关系。
     * 其次，卡券的签名考虑到协议的扩展性和简单的防数据擅改，设计了一套独立的签名协议。
     * 另外由于历史原因，卡券的JS接口先于JSSDK出现，当时的JSAPI并没有鉴权体系，所以在卡券的签名里也加上了appsecret/api_ticket这些身份信息，希望开发者理解。
     * 卡券 api_ticket 是用于调用卡券相关接口的临时票据，有效期为 7200 秒，通过 access_token 来获取。这里要注意与 jsapi_ticket 区分开来。
     * 由于获取卡券 api_ticket 的 api 调用次数非常有限，频繁刷新卡券 api_ticket 会导致 api 调用受限，影响自身业务，开发者必须在自己的服务全局缓存卡券 api_ticket 。
     */
    String BASE_API_TICKET = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={0}&type=wx_card";
}
