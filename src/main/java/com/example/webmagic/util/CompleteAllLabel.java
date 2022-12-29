package com.example.webmagic.util;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 补全补全工具类
 */
public class CompleteAllLabel {
    /**
     * @param context 需要补全的标签
     * @param domain  需要添加的域名
     */
    public static void complete(Elements context, String domain) {
        CompleteAllLabel completeAllLabel = new CompleteAllLabel();
        completeAllLabel.completeTheLabel(context, "a", domain);
        completeAllLabel.completeTheLabel(context, "img", domain);
        completeAllLabel.completeTheLabel(context, "iframe", domain);
    }

    private void completeTheLabel(Elements elements, String label, String domain) {
        String attribute = "src";
        if (label.equals("a")) {
            attribute = "href";
        }
        Elements labelList = elements.select(label);
        for (Element nowLabel : labelList) {
            String attributeValue = nowLabel.attr(attribute);
            if (attributeValue.startsWith("/")) {
                attributeValue = domain + attributeValue;
                nowLabel.attr(attribute, attributeValue);
            }
        }
    }
}
