package com.weixin.pay;

import com.weixin.pay.constants.WxPayConstants;
import org.apache.http.conn.ConnectTimeoutException;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付域名实现类（官方简单版）
 *
 * @author yclimb
 * @date 2018/8/17
 */
public class WxPayDomainSimpleImpl implements WxPayDomain {
    private WxPayDomainSimpleImpl() {
    }

    private static class WxpayDomainHolder {
        private static WxPayDomain holder = new WxPayDomainSimpleImpl();
    }

    public static WxPayDomain instance() {
        return WxpayDomainHolder.holder;
    }

    @Override
    public synchronized void report(final String domain, long elapsedTimeMillis, final Exception ex) {
        DomainStatics info = domainData.get(domain);
        if (info == null) {
            info = new DomainStatics(domain);
            domainData.put(domain, info);
        }

        // success
        if (ex == null) {
            if (info.succCount >= 2) {
                // continue succ, clear error count
                info.connectTimeoutCount = info.dnsErrorCount = info.otherErrorCount = 0;
            } else {
                ++info.succCount;
            }
        } else if (ex instanceof ConnectTimeoutException) {
            info.succCount = info.dnsErrorCount = 0;
            ++info.connectTimeoutCount;
        } else if (ex instanceof UnknownHostException) {
            info.succCount = 0;
            ++info.dnsErrorCount;
        } else {
            info.succCount = 0;
            ++info.otherErrorCount;
        }
    }

    @Override
    public synchronized DomainInfo getDomain(final WxPayConfig config) {
        DomainStatics primaryDomain = domainData.get(WxPayConstants.DOMAIN_API);
        if (primaryDomain == null ||
                primaryDomain.isGood()) {
            return new DomainInfo(WxPayConstants.DOMAIN_API, true);
        }

        long now = System.currentTimeMillis();
        if (switchToAlternateDomainTime == 0) {
            // first switch
            switchToAlternateDomainTime = now;
            return new DomainInfo(WxPayConstants.DOMAIN_API2, false);
        } else if (now - switchToAlternateDomainTime < MIN_SWITCH_PRIMARY_MSEC) {
            DomainStatics alternateDomain = domainData.get(WxPayConstants.DOMAIN_API2);
            if (alternateDomain == null ||
                    alternateDomain.isGood() ||
                    alternateDomain.badCount() < primaryDomain.badCount()) {
                return new DomainInfo(WxPayConstants.DOMAIN_API2, false);
            } else {
                return new DomainInfo(WxPayConstants.DOMAIN_API, true);
            }
        } else {  //force switch back
            switchToAlternateDomainTime = 0;
            primaryDomain.resetCount();
            DomainStatics alternateDomain = domainData.get(WxPayConstants.DOMAIN_API2);
            if (alternateDomain != null) {
                alternateDomain.resetCount();
            }
            return new DomainInfo(WxPayConstants.DOMAIN_API, true);
        }
    }

    static class DomainStatics {
        final String domain;
        int succCount = 0;
        int connectTimeoutCount = 0;
        int dnsErrorCount = 0;
        int otherErrorCount = 0;

        DomainStatics(String domain) {
            this.domain = domain;
        }

        void resetCount() {
            succCount = connectTimeoutCount = dnsErrorCount = otherErrorCount = 0;
        }

        boolean isGood() {
            return connectTimeoutCount <= 2 && dnsErrorCount <= 2;
        }

        int badCount() {
            return connectTimeoutCount + dnsErrorCount * 5 + otherErrorCount / 4;
        }
    }

    // 3 minutes
    private final int MIN_SWITCH_PRIMARY_MSEC = 3 * 60 * 1000;
    private long switchToAlternateDomainTime = 0;
    private Map<String, DomainStatics> domainData = new HashMap<String, DomainStatics>();
}
