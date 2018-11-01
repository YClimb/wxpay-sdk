package com.weixin.pay.card;

/**
 * 微信卡券背景颜色枚举类
 *
 * @author yclimb
 * @date 2018/9/18
 */
public enum CardBgColorEnum {

    /**
     * 淡绿色
     */
    COLOR_010("Color010", "#63b359"),
    /**
     * 深绿色
     */
    COLOR_020("Color020", "#2c9f67"),
    /**
     * 淡蓝色
     */
    COLOR_030("Color030", "#509fc9"),
    /**
     * 深蓝色
     */
    COLOR_040("Color040", "#5885cf"),
    /**
     * 淡紫色
     */
    COLOR_050("Color050", "#9062c0"),
    /**
     * 土黄色
     */
    COLOR_060("Color060", "#d09a45"),
    /**
     * 淡黄色
     */
    COLOR_070("Color070", "#e4b138"),
    /**
     * 橘黄色
     */
    COLOR_080("Color080", "#ee903c"),
    /**
     * 橘黄色 plus
     */
    COLOR_081("Color081", "#f08500"),
    /**
     * 青色
     */
    COLOR_082("Color082", "#a9d92d"),
    /**
     * 淡红色
     */
    COLOR_090("Color090", "#dd6549"),
    /**
     * 深红色
     */
    COLOR_100("Color100", "#cc463d"),
    /**
     * 玫红色
     */
    COLOR_101("Color101", "#cf3e36"),
    /**
     * 深灰色
     */
    COLOR_102("Color102", "#5E6671")
    ;

    /**
     * 背景颜色名称
     */
    private String bgName;

    /**
     * 色值
     */
    private String bgVal;

    CardBgColorEnum(String bgName, String bgVal) {
        this.bgName = bgName;
        this.bgVal = bgVal;
    }

    public String getBgName() {
        return bgName;
    }

    public String getBgVal() {
        return bgVal;
    }
}
