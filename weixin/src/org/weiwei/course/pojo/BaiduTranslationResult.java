package org.weiwei.course.pojo;

import java.util.List;

/**
 * ���ðٶȷ���api��ѯ���
 * 
 * @author weiwei
 * @date 2015-3-2
 */
public class BaiduTranslationResult {
	// ʵ�ʲ��õ�Դ����
	private String from;
	// ʵ�ʲ��õ�Ŀ������
	private String to;
	// �����
	private List<BaiduTranslationResultPair> trans_result;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public List<BaiduTranslationResultPair> getTrans_result() {
		return trans_result;
	}

	public void setTrans_result(List<BaiduTranslationResultPair> trans_result) {
		this.trans_result = trans_result;
	}
}
