package org.weiwei.course.util;

//http://saebbs.com/forum.php?mod=viewthread&tid=23455&extra=page%3D4
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.weiwei.course.pojo.AibangGongjiao;

public class AibangGongjiaoUtil {
	/**
	 * 爱帮公交线路查询 根据城市和关键字（线路）查询
	 * 
	 * @param city
	 *            城市
	 * @param q
	 *            线路
	 * @return gongjiao
	 */
	public static AibangGongjiao searchGongjiao(String city, String q) {
		// 爱帮api地址
		String requestUrl = "http://openapi.aibang.com/bus/lines?app_key=b7fb1e41aa104e9c24b81894d8e5ab48&city={city}&q={q}";
		// 对城市和线路进行编码
		requestUrl = requestUrl.replace("{city}", urlEncodeUTF8(city));
		requestUrl = requestUrl.replace("{q}", urlEncodeUTF8(q));
		// 处理名称、作者中间的空格
		requestUrl = requestUrl.replaceAll("\\+", "%20");
		// 查询并获取返回结果
		InputStream inputStream = httpRequest(requestUrl);
		// 从返回结果中解析出AibangGongjiao
		AibangGongjiao gj = null;
		gj = parseGongjiao(inputStream);
		
		if (null == gj) {
			return null;
		}
		return gj;
	}

	/**
	 * UTF-8编码
	 * 
	 * @param source
	 * @return
	 */
	private static String urlEncodeUTF8(String source) {
		String result = source;
		try {
			result = java.net.URLEncoder.encode(source, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 发送http请求取得返回的输入流
	 * 
	 * @param requestUrl
	 *            请求地址
	 * @return InputStream
	 */
	private static InputStream httpRequest(String requestUrl) {
		InputStream inputStream = null;
		try {
			URL url = new URL(requestUrl);
			HttpURLConnection httpUrlConn = (HttpURLConnection) url
					.openConnection();
			httpUrlConn.setDoInput(true);
			httpUrlConn.setRequestMethod("GET");
			httpUrlConn.connect();
			// 获得返回的输入流
			inputStream = httpUrlConn.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inputStream;
	}

	/**
	 * 解析请求所返回值
	 * 
	 * @param inputStream
	 *            请求所返回值
	 * @return AibangGongjiao对象
	 */
	@SuppressWarnings("unchecked")
	private static AibangGongjiao parseGongjiao(InputStream inputStream) {
		AibangGongjiao gj = null;
		try {
			// 使用dom4j解析xml字符串
			SAXReader reader = new SAXReader();
			Document document = reader.read(inputStream);
			// 得到xml根元素
			Element root = document.getRootElement();
			// result_num表示查询得公交路线数量
			String num = root.element("result_num").getText();

			// 当公交线路大于0时
			if (!"0".equals(num)) {
				// 公交信息根节点
				List<Element> lines = root.elements("lines");
				List<Element> line = lines.get(0).elements("line");

				String name = line.get(0).element("name").getText();
				String info = line.get(0).element("info").getText();
				String stats = line.get(0).element("stats").getText();
				String f_name = line.get(1).element("name").getText();
				String f_stats = line.get(1).element("stats").getText();
				gj = new AibangGongjiao();
				// 获取公交始发站和终点站信息
				gj.setName(name);
				// 获取反向公交路线
				gj.setFname(f_name);
				// 获取公交价格
				gj.setJiage(info);
				// 获取公交正向的各个站点
				gj.setZhengxiang(stats);
				// 获取公交反向的信息
				gj.setFanxiang(f_stats);
			} else {
				// System.out.println("no information");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return gj;
	}

	// 测试方法
	public static void main(String[] args) {
		AibangGongjiao gj = searchGongjiao("上海", "2路");
		System.out.println("公交名称：" + gj.getName());
		System.out.println("反向名称：" + gj.getFname());
		System.out.println("公交价格：" + gj.getJiage());
		System.out.println("正向公交信息：" + gj.getZhengxiang());
		System.out.println("反向公交信息：" + gj.getFanxiang());
	}
}
