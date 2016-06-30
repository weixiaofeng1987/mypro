package org.weiwei.course.pojo;

public class BaiduWeather {
	//状态值
	private String status;
	//时间
	private String date;
	//查询地区
	private String currentcity;
	//pm2.5
	private int pm25;
	//穿衣
	private String dressTitle;
	private String dressZs;
	private String dressTipt;
	private String dressDesp;
	
	//洗车
	private String washCarTitle;
	private String washCarZs;
	private String washCarTipt;
	private String washCarDesp;
	
	//旅游
	private String travellingTitle;
	private String travellingZs;
	private String travellingTipt;
	private String travellingDesp;
	
	//感冒
	private String feverTitle;
	private String feverZs;
	private String feverTipt;
	private String feverDesp;
	
	//运动
	private String sportTitle;
	private String sportZs;
	private String sportTipt;
	private String sportDesp;
	
	//紫外线
	private String ultravioletRaysTitle;
	private String ultravioletRaysZs;
	private String ultravioletRaysTipt;
	private String ultravioletRaysDesp;
	
	//天气数据1
	private String weatherDataDate1;
	private String weatherDataDayPictureUrl1;
	private String weatherDataNightPictureUrl1;
	private String weatherDataWeather1;
	private String weatherDataWind1;
	private String weatherDataTemperature1;
	
	//天气数据2
	private String weatherDataDate2;
	private String weatherDataDayPictureUrl2;
	private String weatherDataNightPictureUrl2;
	private String weatherDataWeather2;
	private String weatherDataWind2;
	private String weatherDataTemperature2;
	
	//天气数据3
	private String weatherDataDate3;
	private String weatherDataDayPictureUrl3;
	private String weatherDataNightPictureUrl3;
	private String weatherDataWeather3;
	private String weatherDataWind3;
	private String weatherDataTemperature3;
	
	//天气数据4
	private String weatherDataDate4;
	private String weatherDataDayPictureUrl4;
	private String weatherDataNightPictureUrl4;
	private String weatherDataWeather4;
	private String weatherDataWind4;
	private String weatherDataTemperature4;
	
	//setters and getters
	public void setStatus(String status)
	{
		this.status = status;
	}
	public String getStatus()
	{
		return status;
	}
	
	public void setDate(String date)
	{
		this.date = date;
	}
	public String getDate()
	{
		return date;
	}
	
}
