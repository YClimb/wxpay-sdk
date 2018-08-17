package com.weixin.pay.util;

import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * 微信支付xml转换工具类
 *
 * @author yclimb
 * @date 2018/8/17
 */
public final class WXPayXmlUtil {

    /**
     * 生成一个微信的xml文档
     * @return DocumentBuilder
     * @throws ParserConfigurationException e
     */
    public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        documentBuilderFactory.setXIncludeAware(false);
        documentBuilderFactory.setExpandEntityReferences(false);
        return documentBuilderFactory.newDocumentBuilder();
    }

    /**
     * 新建doc文档
     * @return Document
     * @throws ParserConfigurationException e
     */
    public static Document newDocument() throws ParserConfigurationException {
        return newDocumentBuilder().newDocument();
    }
}
