package org.weiwei.course.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class BaiduWeatherUtil extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		 resp.setContentType("text/html;charset=UTF-8");
	        PrintWriter pw = resp.getWriter();
	        String echo = req.getParameter("echostr");
	        echo = new String(echo.getBytes("ISO-8859-1"),"UTF-8");
	        pw.println(echo);
			
	}
   
	
	public void	doPost(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException {
		resp.setCharacterEncoding("utf-8");
		req.setCharacterEncoding("utf-8");
		ServletInputStream weixinstr = req.getInputStream();
		SAXReader Reader = new SAXReader();
		try {
			Document document = Reader.read(weixinstr);
			String fromusername=document.selectSingleNode("//FromUserName").getText();
			String tousername=document.selectSingleNode("//ToUserName").getText();
			String msgtype=document.selectSingleNode("//MsgType").getText();
			String createtime=document.selectSingleNode("//CreateTime").getText();
           String reply = null ;
          String picurl=null;
		  if(msgtype.equals("text")){
           String content=document.selectSingleNode("//Content").getText();
           String contenturl=java.net.URLEncoder.encode(content,"UTF-8");
           URL newsurl = new URL("http://api.map.baidu.com/telematics/v3/weather?location="+contenturl+"&ak=1a3cde429f38434f1811a75e1a90310c");
          	InputStream news=newsurl.openStream();
            	SAXReader newsReader = new SAXReader();
            	Document newsdocument = newsReader.read(news);
                String city=newsdocument.selectSingleNode("/CityWeatherResponse/results/currentCity").getText();
     			String temp=newsdocument.selectSingleNode("/CityWeatherResponse/results/weather_data/date[1]").getText();
                String imageurl=newsdocument.selectSingleNode("/CityWeatherResponse/results/weather_data/dayPictureUrl[1]").getText();
                String weather=newsdocument.selectSingleNode("/CityWeatherResponse/results/weather_data/weather[1]").getText();
                String wind=newsdocument.selectSingleNode("/CityWeatherResponse/results/weather_data/wind[1]").getText();
                reply=city+temp+weather+wind;
                picurl=imageurl;
               } 
			
          else{
          reply="功能未开发";
          } 
			StringBuffer respMessage = new StringBuffer();
			respMessage.append(("<xml><ToUserName><![CDATA["+fromusername+"]]></ToUserName><FromUserName><![CDATA["+tousername+"]]></FromUserName><CreateTime><![CDATA["+createtime+"]]></CreateTime><MsgType><![CDATA[news]]></MsgType><ArticleCount>1</ArticleCount><Articles><item><Title><![CDATA["+reply+"]]></Title><Description><![CDATA[]]></Description><PicUrl><![CDATA["+picurl+"]]></PicUrl><Url><![CDATA[]]></Url></item></Articles><FuncFlag>0</FuncFlag></xml>"));
            PrintWriter pw = resp.getWriter();
			pw.println(respMessage);   
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		 
	}
}
