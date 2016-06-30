package org.weiwei.course.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.weiwei.course.pojo.UserLocation;

import com.sina.sae.util.SaeUserInfo;

/**
 * Mysql数据库操作类
 * 
 * @author weiwei
 * @date 2013-11-19
 */
public class MySQLUtil {
	/**
	 * 获取Mysql数据库连接
	 * 
	 * @return Connection
	 */
	private Connection getConn(HttpServletRequest request) {
		Connection conn = null;

		// 从request请求头中取出IP、端口、用户名和密码
		//For BAE. weifeng
//		String host = request.getHeader("BAE_ENV_ADDR_SQL_IP");
//		String port = request.getHeader("BAE_ENV_ADDR_SQL_PORT");
//		String username = request.getHeader("BAE_ENV_AK");
//		String password = request.getHeader("BAE_ENV_SK");
		
		//For SAE. weifeng
//		String host = request.getHeader("SAE_MYSQL_HOST_M");
//		String port = request.getHeader("SAE_MYSQL_PORT");
//		String username = request.getHeader("SAE_MYSQL_USER");
//		String password = request.getHeader("SAE_MYSQL_PASS");
		//sae
		String username = SaeUserInfo.getAccessKey();
		String password = SaeUserInfo.getSecretKey();

		// 数据库名称
//		String dbName = "app_weichattestpage";
		//String dbName = request.getHeader("SAE_MYSQL_DB");
		// JDBC URL
//		String url = String.format("jdbc:mysql://%s:%s/%s", host, port, dbName);
		String url = "jdbc:mysql://w.rdc.sae.sina.com.cn:3307/app_weichattestpage";
		
		try {
			// 加载MySQL驱动
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			// 获取数据库连接
			conn = DriverManager.getConnection(url, username, password);
			//add print
			System.out.println("Connect Mysql!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * 保存用户地理位置
	 * 
	 * @param request 请求对象
	 * @param openId 用户的OpenID
	 * @param lng 用户发送的经度
	 * @param lat 用户发送的纬度
	 * @param bd09_lng 经过百度坐标转换后的经度
	 * @param bd09_lat 经过百度坐标转换后的纬度
	 */
	public static void saveUserLocation(HttpServletRequest request, String openId, String lng, String lat, String bd09_lng, String bd09_lat) {
		String sql = "insert into user_location(open_id, lng, lat, bd09_lng, bd09_lat) values (?, ?, ?, ?, ?)";
		try {
			Connection conn = new MySQLUtil().getConn(request);
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, openId);
			ps.setString(2, lng);
			ps.setString(3, lat);
			ps.setString(4, bd09_lng);
			ps.setString(5, bd09_lat);
			ps.executeUpdate();
			//add print
			System.out.println("insert data finished!");
			// 释放资源
			ps.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新用户地理位置
	 * 写此函数的目的就是不将用户发送的每一天位置信息都存入数据库，只保存一条最新的，从而减小数据库大小
	 * 
	 * @param request 请求对象
	 * @param openId 用户的OpenID
	 * @param lng 用户发送的经度
	 * @param lat 用户发送的纬度
	 * @param bd09_lng 经过百度坐标转换后的经度
	 * @param bd09_lat 经过百度坐标转换后的纬度
	 */
	public static void updateUserLocation(HttpServletRequest request, String openId, String lng, String lat, String bd09_lng, String bd09_lat) {
		String sql = "update user_location set lng=?, lat=?, bd09_lng=?, bd09_lat=? where open_id=?";
		try {
			Connection conn = new MySQLUtil().getConn(request);
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, lng);
			ps.setString(2, lat);
			ps.setString(3, bd09_lng);
			ps.setString(4, bd09_lat);
			ps.setString(5, openId);
			ps.executeUpdate();
			//add print
			System.out.println("update data finished!");
			// 释放资源
			ps.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取用户最后一次发送的地理位置
	 * 
	 * @param request 请求对象
	 * @param openId 用户的OpenID
	 * @return UserLocation
	 */
	public static UserLocation getLastLocation(HttpServletRequest request, String openId) {	
		UserLocation userLocation = null;
		//order by id：将所有检索按id来排序并获取最后一次存储数据
		//说明：如果改为使用updateUserLocation()来存储最新数据，则不需要order。这样数据库也
		//需要更新结构
		String sql = "select open_id, lng, lat, bd09_lng, bd09_lat from user_location where open_id=? order by id desc limit 0,1";
		try {
			Connection conn = new MySQLUtil().getConn(request);
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, openId);
			ResultSet rs = (ResultSet)ps.executeQuery();
			if (rs.next()) {
				userLocation = new UserLocation();
				userLocation.setOpenId(rs.getString("open_id"));
				userLocation.setLng(rs.getString("lng"));
				userLocation.setLat(rs.getString("lat"));
				userLocation.setBd09Lng(rs.getString("bd09_lng"));
				userLocation.setBd09Lat(rs.getString("bd09_lat"));
				//add print
				System.out.println(rs.getString("open_id") + "\t" +
						rs.getString("lng") + "\t" +
						rs.getString("lat") + "\t" +
						rs.getString("bd09_lng") + "\t" +
						rs.getString("bd09_lat"));
			}
			// 释放资源
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return userLocation;
	}
}
