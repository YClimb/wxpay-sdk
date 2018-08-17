package controller;

import com.alibaba.fastjson.JSONObject;
import com.weixin.pay.constants.WXConstants;
import com.weixin.pay.util.WXUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 微信用户授权控制类
 *
 * @author yclimb
 * @date 2018/7/30
 */
@Slf4j
@RestController
@RequestMapping("/weixin/auth")
public class WXAuthController {

    @Resource
    private WXUtils wxUtils;

    /**
     * 微信网页授权
     * https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140842
     * 第一步：用户同意授权，获取code
     * 第二步：通过code换取网页授权access_token
     * @return str
     *
     * @author yclimb
     * @date 2018/7/30
     */
    /*@ApiOperation(value = "微信支付|网页授权", httpMethod = "GET", notes = "获取前端微信用户的网页授权，得到用户基础信息")*/
    @GetMapping("/authorize")
    public String authorize(HttpServletRequest request) throws Exception {

        // 跳转页面标识
        String state = request.getParameter("state");
        // 通过code获取access_token
        String code = request.getParameter("code");
        log.info("authorize:code:{}", code);

        // 获取access_token和openid
        JSONObject jsonToken = wxUtils.getJsapiAccessTokenByCode(code);
        if (null == jsonToken) {
            return WXConstants.ERROR;
        }

        return WXConstants.SUCCESS;
    }

    /**
     * 通过access_token和openid请求获取用户信息(需scope为 snsapi_userinfo)
     * @return str
     *
     * @author yclimb
     * @date 2018/7/31
     */
    /*@ApiOperation(value = "微信支付|通过access_token和openid请求获取用户信息", httpMethod = "POST", notes = "通过access_token和openid请求获取用户信息")*/
    @PostMapping("/userinfo/{access_token}/{openid}")
    public String userinfo(@PathVariable String access_token, @PathVariable String openid) {

        // 通过access_token和openid请求获取用户信息
        JSONObject jsonUserinfo = wxUtils.getJsapiUserinfo(access_token, openid);
        if (null == jsonUserinfo) {
            return WXConstants.ERROR;
        }

        // 判断用户是否在系统中是一个用户
        String unionid = jsonUserinfo.getString("unionid");
        if (StringUtils.isBlank(unionid)) {
            return WXConstants.ERROR;
        }

        /*
        // 存储用户信息到数据库
        User user = userService.queryByUnionId(unionid);
        if (user == null) {
            user = JSON.parseObject(jsonUserinfo.toJSONString(), User.class);
            user.setUserId(user.getId());
            user.setCreateDate(new Date());
            user.setIsDel(CommonConstantEnum.UNDELETED.getCode());
            // 处理微信昵称emoji表情
            if (StringUtils.isNotBlank(user.getNickName())) {
                // 编码Base64.decodeBase64()
                user.setNickName(UserNickUtil.encodeNickName(user.getNickName()));
            }
            userService.createEntity(user);
        }

        // 用户账户信息
        Map<String, Object> map = new HashMap<>(2);
        // 用户名称解码
        user.setNickName(UserNickUtil.decodeNickName(user.getNickName()));
        UserAccount userAccount = userAccountService.queryByUserId(user.getId());
        map.put("user", user);

        return AppMessage.success(map);*/

        return WXConstants.SUCCESS;
    }

}
