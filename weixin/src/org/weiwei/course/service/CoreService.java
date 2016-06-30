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
 * 核心服务类
 * 
 * @author weiwei
 * @date 2013-11-19
 */
@SuppressWarnings("unused")
public class CoreService {
	/**
	 * 处理微信发来的请求
	 * 
	 * @param request
	 * @return xml
	 */
	public static String processRequest(HttpServletRequest request) {
		// xml格式的消息数据
		String respXml = null;
		// 默认返回的文本消息内容
		String respContent = null;
		try {
			// 调用parseXml方法解析请求消息
			Map<String, String> requestMap = MessageUtil.parseXml(request);
			// 发送方帐号
			String fromUserName = requestMap.get("FromUserName");
			
			// 开发者微信号
			String toUserName = requestMap.get("ToUserName");
			// 消息类型
			String msgType = requestMap.get("MsgType");
			// 消息内容
			//String msgContent = requestMap.get("Content").trim();
			// 创建时间
			String msgCreateTime = requestMap.get("CreateTime");

			// 回复文本消息
			TextMessage textMessage = new TextMessage();
			textMessage.setToUserName(fromUserName);
			textMessage.setFromUserName(toUserName);
			textMessage.setCreateTime(new Date().getTime());
			textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);

			// 文本消息
			if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {
				String content = requestMap.get("Content").trim();
				if (content.equals("?")) {
					respContent = getMainMenu();
				}
				else if (content.equals("1")) {
					respContent = getLocationUsage();
				}
				else if (content.startsWith("歌曲")) {
					// 将歌曲2个字及歌曲后面的+、空格、-等特殊符号去掉
					String keyWord = content.replaceAll("^歌曲[\\+ ~!@￥#%^-_=*$`]?", "");
					// 如果歌曲名称为空
					if ("".equals(keyWord)) {
						respContent = getSongsUsage();
					} 
					else {
						String[] kwArr = keyWord.split("@");
						// 歌曲名称
						String musicTitle = kwArr[0];
						// 演唱者默认为空
						String musicAuthor = "";
						if (2 == kwArr.length)
							musicAuthor = kwArr[1];

						// 搜索音乐
						Music music = BaiduMusicUtil.searchMusic(musicTitle, musicAuthor);
						// 未搜索到音乐
						if (null == music) {
							respContent = "对不起，没有找到你想听的歌曲<" + musicTitle + ">。";
						} else {
							// 音乐消息
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
					//添加表情
					respContent = "<a href=\"http://duopao.com/\">";
					respContent = respContent.concat(emoji(0x1F449));
					respContent = respContent.concat("进入经典游戏列表</a>");
					respContent = respContent.concat(emoji(0x1F448));
				}
				else if (content.equals("4")) {
					respContent = getTranslationUsage();
				}
				else if (content.startsWith("翻译"))
				{
					String to = null;
					String tmpContent = content.replaceFirst("翻译", "").trim();
										
					int strPosition = tmpContent.lastIndexOf("为");
					if (-1 != strPosition) {
						to = tmpContent.substring(strPosition+1);
						String keyword = tmpContent.substring(0, strPosition);
	
						//将所要转换的语言与对应代码对应起来
						to = getLanguageId(to);
						if (null != to) {
							respContent = BaiduTranslationUtil.translate(keyword, to);			
						}
						else {
							respContent = "输入格式错误！请参考以下说明输入：\n\n";
							respContent = respContent.concat(getTranslationUsage());
						}
					}
					else {
						respContent = "输入格式错误！请参考以下说明输入：\n\n";
						respContent = respContent.concat(getTranslationUsage());
					}
				}
				//消息内容：回复关键词对应内容
				else if((content.equals("google")) || 
						(content.equals("Google"))) {
					//添加表情
					respContent = "<a href=\"https://google3.azurewebsites.net/\">";
					respContent = respContent.concat(emoji(0x1F449));
					respContent = respContent.concat("Google</a>");
					respContent = respContent.concat(emoji(0x1F448));
				}
				// 周边搜索
				else if (content.startsWith("附近")) {
					String keyWord = content.replaceAll("附近", "").trim();
					// 获取用户最后一次发送的地理位置
					UserLocation location = MySQLUtil.getLastLocation(request, fromUserName);
					// 未获取到
					//call this. not store user's location parameters.
					if (null == location) {
						respContent = getLocationUsage();
					} else {
						// 根据转换后（纠偏）的坐标搜索周边POI
						List<BaiduPlace> placeList = BaiduMapUtil.searchPlace(keyWord, location.getBd09Lng(), location.getBd09Lat());
						// 未搜索到POI
						if (null == placeList || 0 == placeList.size()) {
							respContent = String.format("/难过，您发送的位置附近未搜索到“%s”信息！", keyWord);
						} else {
							List<Article> articleList = BaiduMapUtil.makeArticleList(placeList, location.getBd09Lng(), location.getBd09Lat());
							// 回复图文消息
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
				else if (content.contains("公交")) {
					String cityline = content.replaceAll("[公交\\+ ~!#%^-_=]?", "").trim();
					System.out.println(cityline);
					if ((!content.contains("@") || (content.equals("")))) {
						respContent = "输入格式错误！请参照以下说明操作：";
						respContent = respContent.concat(getBusLineUsage());
					}
					String[] cityLine = cityline.split("@");
					AibangGongjiao gongjiao = AibangGongjiaoUtil.searchGongjiao(cityLine[1].toString(), cityLine[0].toString());
					if (null == gongjiao) {
						respContent = "输入格式错误！请参照以下说明操作：";
						respContent = respContent.concat(getBusLineUsage());
					}
					else {
						respContent = emoji(0xE159);
						respContent = respContent.concat(gongjiao.getName().concat("\n"));
						respContent = respContent.concat(gongjiao.getJiage()).concat("\n\n");
						respContent = respContent.concat("正向路线：").concat(gongjiao.getZhengxiang()).concat("\n\n");
						
						respContent = respContent.concat(emoji(0xE159));
						respContent = respContent.concat(gongjiao.getFname().concat("\n"));
						respContent = respContent.concat(gongjiao.getJiage()).concat("\n\n");
						respContent = respContent.concat("反向路线：").concat(gongjiao.getFanxiang()).concat("\n");
					}
				}
				//历史上的今天
				//仅仅作为一个学习网页爬虫的方法，暂不做具体功能
				else if (content.equals("历史上的今天")) {
					respContent = TodayInHistoryUtil.getTodayInHistoryInfo();
				}
                else if (content.equals("7")) {
                    respContent = aboutUs();
                }
				else if (content.equals("8")) {
					respContent = getTodayInHistoryUsage();
				}
				else if (content.contains("天气")) 
				{
//					String reply = null;
//					String picurl = null;
//					content = content.replaceAll("天气", "").trim();
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
				//处理用户默认输入可以作为类似机器人回复 TBD
				{
					respContent = getMainMenu();					
				}
			}
			// 地理位置消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {
				// 用户发送的经纬度
				String lng = requestMap.get("Location_Y");
				String lat = requestMap.get("Location_X");
				// 坐标转换后的经纬度
				String bd09Lng = null;
				String bd09Lat = null;
				// 调用接口转换坐标
				UserLocation userLocation = BaiduMapUtil.convertCoord(lng, lat);
				if (null != userLocation) {
					bd09Lng = userLocation.getBd09Lng();
					bd09Lat = userLocation.getBd09Lat();
				}
				
				if (null == MySQLUtil.getLastLocation(request, fromUserName)) {				
					// 保存用户地理位置
					MySQLUtil.saveUserLocation(request, fromUserName, lng, lat, bd09Lng, bd09Lat);
					System.out.println("save");
				} else {
					// 如果已经存在，则更新
					MySQLUtil.updateUserLocation(request, fromUserName, lng, lat, bd09Lng, bd09Lat);
					System.out.println("update");
				}

				StringBuffer buffer = new StringBuffer();
				buffer.append("[愉快]").append("成功接收您的位置！").append("\n\n");
				buffer.append("您可以输入搜索关键词获取周边信息了，例如：").append("\n");
				buffer.append("        附近ATM").append("\n");
				buffer.append("        附近KTV").append("\n");
				buffer.append("        附近厕所").append("\n");
				buffer.append("必须以“附近”两个字开头！");
				respContent = buffer.toString();
			}
			// 事件推送
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {
				// 事件类型
				String eventType = requestMap.get("Event");
				// 关注
				if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
					respContent = getSubscribeMsg();
				}
			} 
            // 图片消息  
			else if (MessageUtil.REQ_MESSAGE_TYPE_IMAGE.equals(msgType)) {  
                // 取得图片地址  
                String picUrl = requestMap.get("PicUrl");  
                // 人脸检测  
                String detectResult = FaceplusplusDetectUtil.detect(picUrl);  

                respContent = detectResult.toString();
            } else {
				//可以显示类似菜单模式的使用说明/提供的操作之类的文字性说明
				respContent = getMainMenu();
			}
			if (null != respContent) {
				// 设置文本消息的内容
				textMessage.setContent(respContent);
				// 将文本消息对象转换成xml
				respXml = MessageUtil.messageToXml(textMessage);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return respXml;
	}
	
	/**
	 * emoji表情
	 */
	 private static String emoji(int codePoint)
	 {
		 return String.valueOf(Character.toChars(codePoint));
	 }
	
	/**
	 * 将语言与其代码对应起来
	 * @param to 语言种类
	 */
	private static String getLanguageId(String to)
	{
		String languageZh = "中文、日语、西班牙语、泰语、俄罗斯语、粤语、白话文、德语、"
				+ "荷兰语、英语、韩语、法语、阿拉伯语、葡萄牙语、文言文、意大利语、希腊语";
		String languageId = "zh、jp、spa、th、ru、yue、zh、de、"
				+ "nl、en、kor、fra、ara、pt、wyw、it、el";
		String[] langZh = languageZh.split("、");
		String[] langId = languageId.split("、");
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
	 * 关注提示语
	 * 
	 * @return
	 */
	private static String getSubscribeMsg() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("您好 欢迎关注互联微生活！").append("\n\n");
		buffer.append("回复?查看我们可以为您提供的服务");
		
		return buffer.toString();
	}

	/**
	 * 使用说明
	 * 
	 * @return
	 */
	private static String getLocationUsage() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(emoji(0x1F4A1));
		buffer.append("周边搜索使用说明").append("\n\n");
		buffer.append("1）发送地理位置").append("\n");
		buffer.append("点击窗口底部的“+”按钮，选择“位置”，点“发送”").append("\n\n");
		buffer.append("2）指定关键词搜索").append("\n");
		buffer.append("格式：附近+关键词\n例如：附近ATM、附近KTV、附近厕所").append("\n\n");
		buffer.append("回复?显示主菜单").append("\n");
		return buffer.toString();
	}
	private static String getSongsUsage() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(emoji(0x1F3B5));
		buffer.append("歌曲点播使用说明").append("\n\n");
		buffer.append("回复“歌曲+歌名”，例如歌曲漂洋过海来看你").append("\n");
		buffer.append("或者歌曲漂洋过海来看你@李宗盛").append("\n\n");
		buffer.append("回复?显示主菜单").append("\n");
		return buffer.toString();
	}
	private static String getTranslationUsage() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(emoji(0x1F511));
		buffer.append("智能翻译使用说明").append("\n\n");
		buffer.append("回复“翻译+内容+为+目的语言种类”，例如：").append("\n");
		buffer.append("翻译你好为英语").append("\n");
		buffer.append("翻译gorgeous为中文").append("\n");
		buffer.append("翻译こんにちは为俄罗斯语").append("\n");
		buffer.append("目前支持的语言种类如下：").append("\n");
		buffer.append("中文、日语、西班牙语、泰语、俄罗斯语、粤语、白话文、德语、荷兰语、"
				+ "英语、韩语、法语、阿拉伯语、葡萄牙语、文言文、意大利语、希腊语").append("\n\n");
		
		buffer.append("回复?显示主菜单").append("\n");
		return buffer.toString();
	}
	private static String getTodayInHistoryUsage() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(emoji(0x26A0));
		buffer.append("历史上的今天使用说明").append("\n\n");
		buffer.append("回复“历史上的今天”即可查看历史上的今天所发生的大事").append("\n\n");
		buffer.append("回复?显示主菜单").append("\n");
		return buffer.toString();
	}
	private static String getBusLineUsage()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(emoji(0xE159));
		buffer.append("公交路线使用说明").append("\n\n");
		buffer.append("回复“公交+线路@城市”，即可查看该城市该路公交路线（包括正反向），例如：").append("\n");
		buffer.append("公交874路@上海").append("\n");
		buffer.append("公交大桥五线@上海").append("\n\n");

		buffer.append("回复?显示主菜单").append("\n");
		return buffer.toString();
	}
	private static String aboutUs()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(emoji(0x1F44D));
		buffer.append("互联微生活可以为您提供以下服务（不限于）：").append("\n\n");
		buffer.append("1. 软件需求的实现（侧重综合业务系统，如门户网站、信息发布网站、办公OA、" +
				"电商网站、微信公众账号平台等）").append("\n");
		buffer.append("2. 服务器托管、虚拟机租赁、数据迁移（依托鄂尔多斯市大数据中心）").append("\n");
		buffer.append("3. 技术支持（包括服务器负载均衡、并发、大型机维护）").append("\n");
		buffer.append("4. 定向发布广告").append("\n");
		buffer.append("5. 其他技术方面你需要帮助的").append("\n");
		buffer.append("如有合作意向请添加个人微信号：18947332840").append("\n\n");
		return buffer.toString();	
	}
	   private static String getFaceDetectUsage()
	    {
	        StringBuffer buffer = new StringBuffer();
	        buffer.append(emoji(0x1F4F7));
	        buffer.append("人脸检测使用说明").append("\n\n");
	        buffer.append("发送一张清晰的照片，即可以分析出种族、性别、年龄等信息，快来试试吧").append("\n\n");

	        buffer.append("回复?显示主菜单").append("\n");
	        return buffer.toString();   
	    }
	private static String getMainMenu(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(emoji(0x1F60A));
		buffer.append("Hey！我们可以为您提供以下服务：").append("\n\n");
		buffer.append("1. 周边搜索").append("\n");
		buffer.append("2. 歌曲点播").append("\n");
		buffer.append("3. 经典游戏").append("\n");
		buffer.append("4. 智能翻译").append("\n");
		buffer.append("5. 天气预报").append("\n");
		buffer.append("6. 公交路线").append("\n");
		buffer.append("7. 关于我们").append("\n\n");
		
		buffer.append("回复对应数字开始体验吧！").append("\n");
		return buffer.toString();
	}
}
