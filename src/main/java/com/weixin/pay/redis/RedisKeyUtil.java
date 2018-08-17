package com.weixin.pay.redis;

/**
 * Redis 工具类
 *
 * @author yclimb
 * @date 2018/4/19
 */
public class RedisKeyUtil {

    /**
     * 主数据系统标识
     */
    public static final String KEY_PREFIX = "xxx";

    /**
     * 分割字符，默认[:]
     */
    public static final String KEY_SPLIT_CHAR = ":";

    /**
     * redis的key键规则定义
     *
     * @param module 模块名称
     * @param func   方法名称
     * @return key
     */
    public static String keyBuilder(String module, String func) {
        return keyBuilder(null, module, func, (String[]) null);
    }

    /**
     * redis的key键规则定义
     *
     * @param module 模块名称
     * @param func   方法名称
     * @param args   参数..
     * @return key
     */
    public static String keyBuilder(String module, String func, String... args) {
        return keyBuilder(null, module, func, args);
    }

    /**
     * redis的key键规则定义
     *
     * @param module 模块名称
     * @param func   方法名称
     * @param objStr 对象.toString()
     * @return key
     */
    public static String keyBuilder(String module, String func, String objStr) {
        return keyBuilder(null, module, func, new String[]{objStr});
    }

    /**
     * redis的key键规则定义
     *
     * @param prefix 项目前缀
     * @param module 模块名称
     * @param func   方法名称
     * @param objStr 对象.toString()
     * @return key
     */
    public static String keyBuilder(String prefix, String module, String func, String objStr) {
        return keyBuilder(prefix, module, func, new String[]{objStr});
    }

    /**
     * redis的key键规则定义
     *
     * @param prefix 项目前缀
     * @param module 模块名称
     * @param func   方法名称
     * @param args   参数..
     * @return key
     */
    public static String keyBuilder(String prefix, String module, String func, String... args) {
        // 项目前缀
        if (prefix == null) {
            prefix = KEY_PREFIX;
        }

        StringBuilder key = new StringBuilder(prefix);
        // KEY_SPLIT_CHAR 为分割字符
        key.append(KEY_SPLIT_CHAR).append(module).append(KEY_SPLIT_CHAR).append(func);

        // args 为空时不需要循环
        if (args == null || args.length <= 0) {
            return key.toString();
        }

        // args 不为空时循环拼接字符
        for (String arg : args) {
            key.append(KEY_SPLIT_CHAR).append(arg);
        }

        return key.toString();
    }

    /**
     * redis的key键规则定义
     *
     * @param redisEnum 枚举对象
     * @return key
     */
    public static String keyBuilder(RedisKeyEnum redisEnum) {
        return keyBuilder(redisEnum.getKeyPrefix(), redisEnum.getModule(), redisEnum.getFunc(), (String[]) null);
    }

    /**
     * redis的key键规则定义
     *
     * @param redisEnum 枚举对象
     * @param objStr    对象.toString()
     * @return key
     */
    public static String keyBuilder(RedisKeyEnum redisEnum, String objStr) {
        return keyBuilder(redisEnum.getKeyPrefix(), redisEnum.getModule(), redisEnum.getFunc(), objStr);
    }

}
