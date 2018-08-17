package com.weixin.pay.constants;

/**
 * 微信支付code码常量类
 *
 * @author yclimb
 * @date 2018/8/6
 */
public enum WXPayCodeEnum {

    /**
     * 余额不足
     */
    ERR_CODE_NOTENOUGH("NOTENOUGH", "余额不足");

    private String code;
    private String des;

    WXPayCodeEnum(String code, String des) {
        this.code = code;
        this.des = des;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }
}
