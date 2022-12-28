package com.example.webmagic.util;

import com.sun.istack.internal.logging.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OracleUtils {
    private static final Logger logger = Logger.getLogger(OracleUtils.class);
    // 定义数据库连接字符串
    private static final String URL = "jdbc:oracle:thin:@192.168.2.42:1521:orcl";
    private static final String USERNAME = "bxkc";
    private static final String PASSWORD = "bidi";

    // 加载JDBC驱动
    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 获取数据库连接
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 使用Prepared Statement执行带有参数的SQL语句
    public static int executeUpdate(String sql, Object... params) throws SQLException {
        logger.info("开始更新数据库");

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            int updateNum = statement.executeUpdate();
            logger.info("更新结束共计更新" + updateNum + "条数据");
            return updateNum;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("数据库更新异常");
            return 0;
        } finally {
            close(connection, statement, null);
        }
    }

    public static boolean existsById(String id, String table_name) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            String sql = "SELECT COUNT(*) FROM " + table_name + " WHERE ID = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, id);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            } else {
                return false;
            }
        } catch (SQLException e) {
            // 处理异常
            return false;
        } finally {
            close(connection, statement, resultSet);
        }
    }


    // 使用Prepared Statement执行带有参数的SELECT语句
    public static List<Object[]> query(String sql, Object... params) {
        List<Object[]> results = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            // 执行SQL语句
            resultSet = statement.executeQuery();
            // 获取查询结果的列数
            int columnCount = resultSet.getMetaData().getColumnCount();
            // 遍历查询结果
            while (resultSet.next()) {
                Object[] row = new Object[columnCount];
                // 将查询结果添加到数组中
                for (int i = 0; i < columnCount; i++) {
                    row[i] = resultSet.getObject(i + 1);
                }
                results.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(connection, statement, resultSet);
        }
        return results;
    }

    public static <T> List<T> query(String sql, Class<T> clazz, Object... params) {
        List<T> results = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            resultSet = statement.executeQuery();
            // 执行SQL语句
            // 获取结果集中的列数
            int columnCount = resultSet.getMetaData().getColumnCount();
            // 遍历查询结果
            while (resultSet.next()) {
                // 创建Java对象
                T object = clazz.newInstance();
                // 遍历列
                for (int i = 1; i <= columnCount; i++) {
                    // 获取列名
                    String columnName = resultSet.getMetaData().getColumnName(i);
                    // 获取列值
                    Object columnValue = resultSet.getObject(i);
                    // 将列值赋值给对应的属性
                    Field field = clazz.getDeclaredField(columnName);
                    field.setAccessible(true);
                    field.set(object, columnValue);
                }
                // 将映射好的Java对象添加到结果列表中
                results.add(object);
            }

        } catch (SQLException | ReflectiveOperationException e) {
            e.printStackTrace();
        } finally {
            close(connection, statement, resultSet);
        }
        return results;
    }


    public static void close(Connection connection, PreparedStatement statement, ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
