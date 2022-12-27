package com.example.webmagic.dao;

import com.example.webmagic.entity.XinXi;
import com.example.webmagic.util.OracleUtils;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class XinXiDao {

    public void saveXinxi(XinXi xinXi) {
        String sql = "INSERT INTO XIN_XI_INFO_TEST(ID, SOURCE_NAME, DETAIL_LINK, DETAIL_TITLE, DETAIL_CONTENT, PAGE_TIME, CREATE_TIME, LIST_TITLE, CREATE_BY) VALUES (?, ?, ?, ?, ?, ?, ?, ? ,?)";
        try {
            OracleUtils.executeUpdate(sql, xinXi.getID(), "安徽省港航集团有限公司",
                    xinXi.getDETAIL_LINK(),
                    xinXi.getDETAIL_TITLE(),
                    xinXi.getDETAIL_CONTENT(),
                    xinXi.getPAGE_TIME(),
                    new Timestamp(new Date().getTime()),
                    xinXi.getLIST_TITLE(),
                    "XU");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
