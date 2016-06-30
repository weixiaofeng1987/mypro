package org.weiwei.course.service;

import java.io.InputStream;
import java.net.URL;
import java.security.interfaces.RSAKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.weiwei.course.message.resp.Article;
import org.weiwei.course.message.resp.Music;
import org.weiwei.course.message.resp.MusicMessage;
import org.weiwei.course.message.resp.NewsMessage;
import org.weiwei.course.message.resp.TextMessage;
import org.weiwei.course.pojo.AibangGongjiao;
import org.weiwei.course.pojo.BaiduPlace;
import org.weiwei.course.pojo.UserLocation;
import org.weiwei.course.util.AibangGongjiaoUtil;
import org.weiwei.course.util.BaiduMapUtil;
import org.weiwei.course.util.BaiduMusicUtil;
import org.weiwei.course.util.BaiduTranslationUtil;
import org.weiwei.course.util.FaceplusplusDetectUtil;
import org.weiwei.course.util.MessageUtil;
import org.weiwei.course.util.MySQLUtil;
import org.weiwei.course.util.TodayInHistoryUtil;

/**
 * ���ķ�����
 * 
 * @author weiwei
 * @date 2013-11-19
 */
@SuppressWarnings("unused")
public class CoreService {
	/**
	 * ����΢�ŷ���������
	 * 
	 * @param request
	 * @return xml
	 */
	public static String processRequest(HttpServletRequest request) {
		// xml��ʽ����Ϣ����
		String respXml = null;
		// Ĭ�Ϸ��ص��ı���Ϣ����
		String respContent = null;
		try {
			// ����parseXml��������������Ϣ
			Map<String, String> requestMap = MessageUtil.parseXml(request);
			// ���ͷ��ʺ�
			String fromUserName = requestMap.get("FromUserName");
			
			// ������΢�ź�
			String toUserName = requestMap.get("ToUserName");
			// ��Ϣ����
			String msgType = requestMap.get("MsgType");
			// ��Ϣ����
			//String msgContent = requestMap.get("Content").trim();
			// ����ʱ��
			String msgCreateTime = requestMap.get("CreateTime");

			// �ظ��ı���Ϣ
			TextMessage textMessage = new TextMessage();
			textMessage.setToUserName(fromUserName);
			textMessage.setFromUserName(toUserName);
			textMessage.setCreateTime(new Date().getTime());
			textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);

			// �ı���Ϣ
			if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {
				String content = requestMap.get("Content").trim();
				if (content.equals("?")) {
					respContent = getMainMenu();
				}
				else if (content.equals("1")) {
					respContent = getLocationUsage();
				}
				else if (content.startsWith("����")) {
					// ������2���ּ����������+���ո�-���������ȥ��
					String keyWord = content.replaceAll("^����[\\+ ~!@��#%^-_=*$`]?", "");
					// �����������Ϊ��
					if ("".equals(keyWord)) {
						respContent = getSongsUsage();
					} 
					else {
						String[] kwArr = keyWord.split("@");
						// ��������
						String musicTitle = kwArr[0];
						// �ݳ���Ĭ��Ϊ��
						String musicAuthor = "";
						if (2 == kwArr.length)
							musicAuthor = kwArr[1];

						// ��������
						Music music = BaiduMusicUtil.searchMusic(musicTitle, musicAuthor);
						// δ����������
						if (null == music) {
							respContent = "�Բ���û���ҵ��������ĸ���<" + musicTitle + ">��";
						} else {
							// ������Ϣ
							MusicMessage musicMessage = new MusicMessage();
							musicMessage.setToUserName(fromUserName);
							musicMessage.setFromUserName(toUserName);
							musicMessage.setCreateTime(new Date().getTime());
							musicMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_MUSIC);
							musicMessage.setMusic(music);
							respXml = MessageUtil.messageToXml(musicMessage);
						}
					}
				}
				else if (content.equals("2")) {
					respContent = getSongsUsage();
				}
				else if (content.equals("3")) {
					//��ӱ���
					respContent = "<a href=\"http://duopao.com/\">";
					respContent = respContent.concat(emoji(0x1F449));
					respContent = respContent.concat("���뾭����Ϸ�б�</a>");
					respContent = respContent.concat(emoji(0x1F448));
				}
				else if (content.equals("4")) {
					respContent = getTranslationUsage();
				}
				else if (content.startsWith("����"))
				{
					String to = null;
					String tmpContent = content.replaceFirst("����", "").trim();
										
					int strPosition = tmpContent.lastIndexOf("Ϊ");
					if (-1 != strPosition) {
						to = tmpContent.substring(strPosition+1);
						String keyword = tmpContent.substring(0, strPosition);
	
						//����Ҫת�����������Ӧ�����Ӧ����
						to = getLanguageId(to);
						if (null != to) {
							respContent = BaiduTranslationUtil.translate(keyword, to);			
						}
						else {
							respContent = "�����ʽ������ο�����˵�����룺\n\n";
							respContent = respContent.concat(getTranslationUsage());
						}
					}
					else {
						respContent = "�����ʽ������ο�����˵�����룺\n\n";
						respContent = respContent.concat(getTranslationUsage());
					}
				}
				//��Ϣ���ݣ��ظ��ؼ��ʶ�Ӧ����
				else if((content.equals("google")) || 
						(content.equals("Google"))) {
					//��ӱ���
					respContent = "<a href=\"https://google3.azurewebsites.net/\">";
					respContent = respContent.concat(emoji(0x1F449));
					respContent = respContent.concat("Google</a>");
					respContent = respContent.concat(emoji(0x1F448));
				}
				// �ܱ�����
				else if (content.startsWith("����")) {
					String keyWord = content.replaceAll("����", "").trim();
					// ��ȡ�û����һ�η��͵ĵ���λ��
					UserLocation location = MySQLUtil.getLastLocation(request, fromUserName);
					// δ��ȡ��
					//call this. not store user's location parameters.
					if (null == location) {
						respContent = getLocationUsage();
					} else {
						// ����ת���󣨾�ƫ�������������ܱ�POI
						List<BaiduPlace> placeList = BaiduMapUtil.searchPlace(keyWord, location.getBd09Lng(), location.getBd09Lat());
						// δ������POI
						if (null == placeList || 0 == placeList.size()) {
							respContent = String.format("/�ѹ��������͵�λ�ø���δ��������%s����Ϣ��", keyWord);
						} else {
							List<Article> articleList = BaiduMapUtil.makeArticleList(placeList, location.getBd09Lng(), location.getBd09Lat());
							// �ظ�ͼ����Ϣ
							NewsMessage newsMessage = new NewsMessage();
							newsMessage.setToUserName(fromUserName);
							newsMessage.setFromUserName(toUserName);
							newsMessage.setCreateTime(new Date().getTime());
							newsMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);
							newsMessage.setArticles(articleList);
							newsMessage.setArticleCount(articleList.size());
							respXml = MessageUtil.messageToXml(newsMessage);
						}
					}
				}
				else if (content.equals("6")) {
					respContent = getBusLineUsage();
				}
				else if (content.contains("����")) {
					String cityline = content.replaceAll("[����\\+ ~!#%^-_=]?", "").trim();
					System.out.println(cityline);
					if ((!content.contains("@") || (content.equals("")))) {
						respContent = "�����ʽ�������������˵��������";
						respContent = respContent.concat(getBusLineUsage());
					}
					String[] cityLine = cityline.split("@");
					AibangGongjiao gongjiao = AibangGongjiaoUtil.searchGongjiao(cityLine[1].toString(), cityLine[0].toString());
					if (null == gongjiao) {
						respContent = "�����ʽ�������������˵��������";
						respContent = respContent.concat(getBusLineUsage());
					}
					else {
						respContent = emoji(0xE159);
						respContent = respContent.concat(gongjiao.getName().concat("\n"));
						respContent = respContent.concat(gongjiao.getJiage()).concat("\n\n");
						respContent = respContent.concat("����·�ߣ�").concat(gongjiao.getZhengxiang()).concat("\n\n");
						
						respContent = respContent.concat(emoji(0xE159));
						respContent = respContent.concat(gongjiao.getFname().concat("\n"));
						respContent = respContent.concat(gongjiao.getJiage()).concat("\n\n");
						respContent = respContent.concat("����·�ߣ�").concat(gongjiao.getFanxiang()).concat("\n");
					}
				}
				//��ʷ�ϵĽ���
				//������Ϊһ��ѧϰ��ҳ����ķ������ݲ������幦��
				else if (content.equals("��ʷ�ϵĽ���")) {
					respContent = TodayInHistoryUtil.getTodayInHistoryInfo();
				}
                else if (content.equals("7")) {
                    respContent = aboutUs();
                }
				else if (content.equals("8")) {
					respContent = getTodayInHistoryUsage();
				}
				else if (content.contains("����")) 
				{
//					String reply = null;
//					String picurl = null;
//					content = content.replaceAll("����", "").trim();
//					String contenturl=java.net.URLEncoder.encode(content,"UTF-8");
//		            URL newsurl = new URL("http://api.map.baidu.com/telematics/v3/weather?location="+contenturl+"&ak=AKcoyRWeP64QlvZCFKbs3zMb");
//		          	InputStream news=newsurl.openStream();
//	            	SAXReader newsReader = new SAXReader();
//	            	Document newsdocument = newsReader.read(news);
//	                String city=newsdocument.selectSingleNode("/CityWeatherResponse/results/currentCity").getText();
//	     			String temp=newsdocument.selectSingleNode("/CityWeatherResponse/results/weather_data/date[1]").getText();
//	                String imageurl=newsdocument.selectSingleNode("/CityWeatherResponse/results/weather_data/dayPictureUrl[1]").getText();
//	                String weather=newsdocument.selectSingleNode("/CityWeatherResponse/results/weather_data/weather[1]").getText();
//	                String wind=newsdocument.selectSingleNode("/CityWeatherResponse/results/weather_data/wind[1]").getText();
//	                reply=city+temp+weather+wind;
//	                picurl=imageurl;
//	                StringBuffer respMessage = new StringBuffer();
//	    			respMessage.append(("<xml><ToUserName><![CDATA["+fromUserName+"]]></ToUserName><FromUserName><![CDATA["+toUserName+"]]></FromUserName><CreateTime><![CDATA["+msgCreateTime+"]]></CreateTime><MsgType><![CDATA[news]]></MsgType><ArticleCount>1</ArticleCount><Articles><item><Title><![CDATA["+reply+"]]></Title><Description><![CDATA[]]></Description><PicUrl><![CDATA["+picurl+"]]></PicUrl><Url><![CDATA[]]></Url></item></Articles><FuncFlag>0</FuncFlag></xml>"));
//	    			respContent = respMessage.toString().trim();
				}
				else if (content.equals("9")) {
					respContent = getFaceDetectUsage();
				}
				else
				//�����û�Ĭ�����������Ϊ���ƻ����˻ظ� TBD
				{
					respContent = getMainMenu();					
				}
			}
			// ����λ����Ϣ
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {
				// �û����͵ľ�γ��
				String lng = requestMap.get("Location_Y");
				String lat = requestMap.get("Location_X");
				// ����ת����ľ�γ��
				String bd09Lng = null;
				String bd09Lat = null;
				// ���ýӿ�ת������
				UserLocation userLocation = BaiduMapUtil.convertCoord(lng, lat);
				if (null != userLocation) {
					bd09Lng = userLocation.getBd09Lng();
					bd09Lat = userLocation.getBd09Lat();
				}
				
				if (null == MySQLUtil.getLastLocation(request, fromUserName)) {				
					// �����û�����λ��
					MySQLUtil.saveUserLocation(request, fromUserName, lng, lat, bd09Lng, bd09Lat);
					System.out.println("save");
				} else {
					// ����Ѿ����ڣ������
					MySQLUtil.updateUserLocation(request, fromUserName, lng, lat, bd09Lng, bd09Lat);
					System.out.println("update");
				}

				StringBuffer buffer = new StringBuffer();
				buffer.append("[���]").append("�ɹ���������λ�ã�").append("\n\n");
				buffer.append("���������������ؼ��ʻ�ȡ�ܱ���Ϣ�ˣ����磺").append("\n");
				buffer.append("        ����ATM").append("\n");
				buffer.append("        ����KTV").append("\n");
				buffer.append("        ��������").append("\n");
				buffer.append("�����ԡ������������ֿ�ͷ��");
				respContent = buffer.toString();
			}
			// �¼�����
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {
				// �¼�����
				String eventType = requestMap.get("Event");
				// ��ע
				if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
					respContent = getSubscribeMsg();
				}
			} 
            // ͼƬ��Ϣ  
			else if (MessageUtil.REQ_MESSAGE_TYPE_IMAGE.equals(msgType)) {  
                // ȡ��ͼƬ��ַ  
                String picUrl = requestMap.get("PicUrl");  
                // �������  
                String detectResult = FaceplusplusDetectUtil.detect(picUrl);  

                respContent = detectResult.toString();
            } else {
				//������ʾ���Ʋ˵�ģʽ��ʹ��˵��/�ṩ�Ĳ���֮���������˵��
				respContent = getMainMenu();
			}
			if (null != respContent) {
				// �����ı���Ϣ������
				textMessage.setContent(respContent);
				// ���ı���Ϣ����ת����xml
				respXml = MessageUtil.messageToXml(textMessage);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return respXml;
	}
	
	/**
	 * emoji����
	 */
	 private static String emoji(int codePoint)
	 {
		 return String.valueOf(Character.toChars(codePoint));
	 }
	
	/**
	 * ��������������Ӧ����
	 * @param to ��������
	 */
	private static String getLanguageId(String to)
	{
		String languageZh = "���ġ�����������̩�����˹�����׻��ġ����"
				+ "�����Ӣ��������������������������ġ�������ϣ����";
		String languageId = "zh��jp��spa��th��ru��yue��zh��de��"
				+ "nl��en��kor��fra��ara��pt��wyw��it��el";
		String[] langZh = languageZh.split("��");
		String[] langId = languageId.split("��");
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < langId.length; i++) {
			map.put(langZh[i], langId[i]);
		}
		if (map.containsKey(to)) {
			return map.get(to);
		}
		return null;	
	}
	

	/**
	 * ��ע��ʾ��
	 * 
	 * @return
	 */
	private static String getSubscribeMsg() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("���� ��ӭ��ע����΢���").append("\n\n");
		buffer.append("�ظ�?�鿴���ǿ���Ϊ���ṩ�ķ���");
		
		return buffer.toString();
	}

	/**
	 * ʹ��˵��
	 * 
	 * @return
	 */
	private static String getLocationUsage() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(emoji(0x1F4A1));
		buffer.append("�ܱ�����ʹ��˵��").append("\n\n");
		buffer.append("1�����͵���λ��").append("\n");
		buffer.append("������ڵײ��ġ�+����ť��ѡ��λ�á����㡰���͡�").append("\n\n");
		buffer.append("2��ָ���ؼ�������").append("\n");
		buffer.append("��ʽ������+�ؼ���\n���磺����ATM������KTV����������").append("\n\n");
		buffer.append("�ظ�?��ʾ���˵�").append("\n");
		return buffer.toString();
	}
	private static String getSongsUsage() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(emoji(0x1F3B5));
		buffer.append("�����㲥ʹ��˵��").append("\n\n");
		buffer.append("�ظ�������+���������������Ư�����������").append("\n");
		buffer.append("���߸���Ư�����������@����ʢ").append("\n\n");
		buffer.append("�ظ�?��ʾ���˵�").append("\n");
		return buffer.toString();
	}
	private static String getTranslationUsage() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(emoji(0x1F511));
		buffer.append("���ܷ���ʹ��˵��").append("\n\n");
		buffer.append("�ظ�������+����+Ϊ+Ŀ���������ࡱ�����磺").append("\n");
		buffer.append("�������ΪӢ��").append("\n");
		buffer.append("����gorgeousΪ����").append("\n");
		buffer.append("���뤳��ˤ���Ϊ����˹��").append("\n");
		buffer.append("Ŀǰ֧�ֵ������������£�").append("\n");
		buffer.append("���ġ�����������̩�����˹�����׻��ġ���������"
				+ "Ӣ��������������������������ġ�������ϣ����").append("\n\n");
		
		buffer.append("�ظ�?��ʾ���˵�").append("\n");
		return buffer.toString();
	}
	private static String getTodayInHistoryUsage() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(emoji(0x26A0));
		buffer.append("��ʷ�ϵĽ���ʹ��˵��").append("\n\n");
		buffer.append("�ظ�����ʷ�ϵĽ��족���ɲ鿴��ʷ�ϵĽ����������Ĵ���").append("\n\n");
		buffer.append("�ظ�?��ʾ���˵�").append("\n");
		return buffer.toString();
	}
	private static String getBusLineUsage()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(emoji(0xE159));
		buffer.append("����·��ʹ��˵��").append("\n\n");
		buffer.append("�ظ�������+��·@���С������ɲ鿴�ó��и�·����·�ߣ����������򣩣����磺").append("\n");
		buffer.append("����874·@�Ϻ�").append("\n");
		buffer.append("������������@�Ϻ�").append("\n\n");

		buffer.append("�ظ�?��ʾ���˵�").append("\n");
		return buffer.toString();
	}
	private static String aboutUs()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(emoji(0x1F44D));
		buffer.append("����΢�������Ϊ���ṩ���·��񣨲����ڣ���").append("\n\n");
		buffer.append("1. ��������ʵ�֣������ۺ�ҵ��ϵͳ�����Ż���վ����Ϣ������վ���칫OA��" +
				"������վ��΢�Ź����˺�ƽ̨�ȣ�").append("\n");
		buffer.append("2. �������йܡ���������ޡ�����Ǩ�ƣ����ж�����˹�д��������ģ�").append("\n");
		buffer.append("3. ����֧�֣��������������ؾ��⡢���������ͻ�ά����").append("\n");
		buffer.append("4. ���򷢲����").append("\n");
		buffer.append("5. ����������������Ҫ������").append("\n");
		buffer.append("���к�����������Ӹ���΢�źţ�18947332840").append("\n\n");
		return buffer.toString();	
	}
	   private static String getFaceDetectUsage()
	    {
	        StringBuffer buffer = new StringBuffer();
	        buffer.append(emoji(0x1F4F7));
	        buffer.append("�������ʹ��˵��").append("\n\n");
	        buffer.append("����һ����������Ƭ�������Է��������塢�Ա��������Ϣ���������԰�").append("\n\n");

	        buffer.append("�ظ�?��ʾ���˵�").append("\n");
	        return buffer.toString();   
	    }
	private static String getMainMenu(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(emoji(0x1F60A));
		buffer.append("Hey�����ǿ���Ϊ���ṩ���·���").append("\n\n");
		buffer.append("1. �ܱ�����").append("\n");
		buffer.append("2. �����㲥").append("\n");
		buffer.append("3. ������Ϸ").append("\n");
		buffer.append("4. ���ܷ���").append("\n");
		buffer.append("5. ����Ԥ��").append("\n");
		buffer.append("6. ����·��").append("\n");
		buffer.append("7. ��������").append("\n\n");
		
		buffer.append("�ظ���Ӧ���ֿ�ʼ����ɣ�").append("\n");
		return buffer.toString();
	}
}
