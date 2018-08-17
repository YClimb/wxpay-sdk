package com.weixin.pay.redis;

/**
 * Redis 枚举类
 *
 * @author yclimb
 * @date 2018/4/19
 */
public enum RedisKeyEnum {

    /**
     * 生成带参数的小程序二维码KEY
     */
    XXX_MINI_WX_CODE(RedisKeyUtil.KEY_PREFIX, "mini", "getwxacodeunlimit", "生成永久无限制微信二维码")

    ;

    /**
     * 系统标识
     */
    private String keyPrefix;

    /**
     * 模块名称
     */
    private String module;

    /**
     * 方法名称
     */
    private String func;

    /**
     * remark
     */
    private String remark;

    RedisKeyEnum(String keyPrefix, String module, String func, String remark) {
        this.keyPrefix = keyPrefix;
        this.module = module;
        this.func = func;
        this.remark = remark;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getFunc() {
        return func;
    }

    public void setFunc(String func) {
        this.func = func;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
