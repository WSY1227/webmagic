package com.example.webmagic.demo.reUtilTest;


import cn.hutool.core.util.ReUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * 正则表达式工具类Demo
 *
 * @author Looly
 */
public class ReUtilDemo {

    public static void main(String[] args) {
        String content = "ZZZaaabbbccc中文1234";

        //get demo 正则查找匹配的第一个字符串
        String resultGet = ReUtil.get("\\w{2}", content, 0);
        System.out.println("resultGet:" + resultGet);
        //抽取多个分组然后把它们拼接起来
        String resultExtractMulti = ReUtil.extractMulti("(\\w)aa(\\w)", content, "$1-$2");

        System.out.println("resultExtractMulti:" + resultExtractMulti);
        //抽取多个分组然后把原文匹配到位置之前的内容都删除
//        String[] contents = new String[]{content};
//        String resultExtractMultiAndDelPre = ReUtil.extractMultiAndDelPre("(\\w)aa(\\w)", contents, "$1-$2");

        //删除第一个匹配到的内容
        String resultDelFirst = ReUtil.delFirst("(\\w)aa(\\w)", content);
        System.out.println("resultDelFirst:" + resultDelFirst);
        //删除第一个匹配到的内容以及之前的文本
        String resultDelPre = ReUtil.delPre("(\\w)aa(\\w)", content);
        System.out.println("resultDelPre:" + resultDelPre);
        //查找所有匹配文本
        List<String> resultFindAll = ReUtil.findAll("\\w{2}", content, 0, new ArrayList<String>());
        //找到匹配的第一个数字
        Integer resultGetFirstNumber = ReUtil.getFirstNumber(content);

        //格式是否符合Ipv4格式

        //给定字符串是否匹配给定正则

        //通过正则查找到字符串，然后把匹配到的字符串加入到replacementTemplate中，$1表示分组1的字符串

        //转义给定字符串，为正则相关的特殊符号转义

    }
}