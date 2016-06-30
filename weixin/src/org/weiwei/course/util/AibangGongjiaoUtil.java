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
	 * ���﹫����·��ѯ ���ݳ��к͹ؼ��֣���·����ѯ
	 * 
	 * @param city
	 *            ����
	 * @param q
	 *            ��·
	 * @return gongjiao
	 */
	public static AibangGongjiao searchGongjiao(String city, String q) {
		// ����api��ַ
		String requestUrl = "http://openapi.aibang.com/bus/lines?app_key=b7fb1e41aa104e9c24b81894d8e5ab48&city={city}&q={q}";
		// �Գ��к���·���б���
		requestUrl = requestUrl.replace("{city}", urlEncodeUTF8(city));
		requestUrl = requestUrl.replace("{q}", urlEncodeUTF8(q));
		// �������ơ������м�Ŀո�
		requestUrl = requestUrl.replaceAll("\\+", "%20");
		// ��ѯ����ȡ���ؽ��
		InputStream inputStream = httpRequest(requestUrl);
		// �ӷ��ؽ���н�����AibangGongjiao
		AibangGongjiao gj = null;
		gj = parseGongjiao(inputStream);
		
		if (null == gj) {
			return null;
		}
		return gj;
	}

	/**
	 * UTF-8����
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
	 * ����http����ȡ�÷��ص�������
	 * 
	 * @param requestUrl
	 *            �����ַ
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
			// ��÷��ص�������
			inputStream = httpUrlConn.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inputStream;
	}

	/**
	 * ��������������ֵ
	 * 
	 * @param inputStream
	 *            ����������ֵ
	 * @return AibangGongjiao����
	 */
	@SuppressWarnings("unchecked")
	private static AibangGongjiao parseGongjiao(InputStream inputStream) {
		AibangGongjiao gj = null;
		try {
			// ʹ��dom4j����xml�ַ���
			SAXReader reader = new SAXReader();
			Document document = reader.read(inputStream);
			// �õ�xml��Ԫ��
			Element root = document.getRootElement();
			// result_num��ʾ��ѯ�ù���·������
			String num = root.element("result_num").getText();

			// ��������·����0ʱ
			if (!"0".equals(num)) {
				// ������Ϣ���ڵ�
				List<Element> lines = root.elements("lines");
				List<Element> line = lines.get(0).elements("line");

				String name = line.get(0).element("name").getText();
				String info = line.get(0).element("info").getText();
				String stats = line.get(0).element("stats").getText();
				String f_name = line.get(1).element("name").getText();
				String f_stats = line.get(1).element("stats").getText();
				gj = new AibangGongjiao();
				// ��ȡ����ʼ��վ���յ�վ��Ϣ
				gj.setName(name);
				// ��ȡ���򹫽�·��
				gj.setFname(f_name);
				// ��ȡ�����۸�
				gj.setJiage(info);
				// ��ȡ��������ĸ���վ��
				gj.setZhengxiang(stats);
				// ��ȡ�����������Ϣ
				gj.setFanxiang(f_stats);
			} else {
				// System.out.println("no information");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return gj;
	}

	// ���Է���
	public static void main(String[] args) {
		AibangGongjiao gj = searchGongjiao("�Ϻ�", "2·");
		System.out.println("�������ƣ�" + gj.getName());
		System.out.println("�������ƣ�" + gj.getFname());
		System.out.println("�����۸�" + gj.getJiage());
		System.out.println("���򹫽���Ϣ��" + gj.getZhengxiang());
		System.out.println("���򹫽���Ϣ��" + gj.getFanxiang());
	}
}
