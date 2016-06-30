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
 * Mysql���ݿ������
 * 
 * @author weiwei
 * @date 2013-11-19
 */
public class MySQLUtil {
	/**
	 * ��ȡMysql���ݿ�����
	 * 
	 * @return Connection
	 */
	private Connection getConn(HttpServletRequest request) {
		Connection conn = null;

		// ��request����ͷ��ȡ��IP���˿ڡ��û���������
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

		// ���ݿ�����
//		String dbName = "app_weichattestpage";
		//String dbName = request.getHeader("SAE_MYSQL_DB");
		// JDBC URL
//		String url = String.format("jdbc:mysql://%s:%s/%s", host, port, dbName);
		String url = "jdbc:mysql://w.rdc.sae.sina.com.cn:3307/app_weichattestpage";
		
		try {
			// ����MySQL����
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			// ��ȡ���ݿ�����
			conn = DriverManager.getConnection(url, username, password);
			//add print
			System.out.println("Connect Mysql!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * �����û�����λ��
	 * 
	 * @param request �������
	 * @param openId �û���OpenID
	 * @param lng �û����͵ľ���
	 * @param lat �û����͵�γ��
	 * @param bd09_lng �����ٶ�����ת����ľ���
	 * @param bd09_lat �����ٶ�����ת�����γ��
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
			// �ͷ���Դ
			ps.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * �����û�����λ��
	 * д�˺�����Ŀ�ľ��ǲ����û����͵�ÿһ��λ����Ϣ���������ݿ⣬ֻ����һ�����µģ��Ӷ���С���ݿ��С
	 * 
	 * @param request �������
	 * @param openId �û���OpenID
	 * @param lng �û����͵ľ���
	 * @param lat �û����͵�γ��
	 * @param bd09_lng �����ٶ�����ת����ľ���
	 * @param bd09_lat �����ٶ�����ת�����γ��
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
			// �ͷ���Դ
			ps.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ȡ�û����һ�η��͵ĵ���λ��
	 * 
	 * @param request �������
	 * @param openId �û���OpenID
	 * @return UserLocation
	 */
	public static UserLocation getLastLocation(HttpServletRequest request, String openId) {	
		UserLocation userLocation = null;
		//order by id�������м�����id�����򲢻�ȡ���һ�δ洢����
		//˵���������Ϊʹ��updateUserLocation()���洢�������ݣ�����Ҫorder���������ݿ�Ҳ
		//��Ҫ���½ṹ
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
			// �ͷ���Դ
			rs.close();
			ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return userLocation;
	}
}
