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

    public static void complete(Elements context, String domain, String startsWith, String regex) {
        CompleteAllLabel completeAllLabel = new CompleteAllLabel();
        completeAllLabel.completeTheLabel(context, "a", domain, startsWith, regex);
        completeAllLabel.completeTheLabel(context, "img", domain, startsWith, regex);
        completeAllLabel.completeTheLabel(context, "iframe", domain, startsWith, regex);
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
            } else if (attributeValue.startsWith("file")) {
                nowLabel.remove();
            }
        }
    }

    /**
     * @param elements   节点
     * @param label      标签
     * @param domain     域
     * @param startsWith 匹配以什么开始的
     * @param regex      替换的正则规则
     */
    private void completeTheLabel(Elements elements, String label, String domain, String startsWith, String regex) {
        String attribute = "src";
        if (label.equals("a")) {
            attribute = "href";
        }
        Elements labelList = elements.select(label);
        for (Element nowLabel : labelList) {
            String attributeValue = nowLabel.attr(attribute);
            if (attributeValue.startsWith(startsWith)) {
                nowLabel.attr(attribute, attributeValue.replaceAll(regex, domain));
            } else if (attributeValue.startsWith("file")) {
                nowLabel.remove();
            }
        }
    }
}
