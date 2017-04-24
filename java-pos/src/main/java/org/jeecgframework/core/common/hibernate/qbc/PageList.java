package org.jeecgframework.core.common.hibernate.qbc;

import java.util.List;
/**
 * 
 *类描述：分页查询结果封装类
 *@author: jeecg
 *@date： 日期：2012-12-7 时间：上午10:20:04
 *@version 1.0
 */
@SuppressWarnings("unchecked")
public class PageList {
	private int curPageNO;
	private int offset;
	private String toolBar;//分页工具条
	private int count;
	@SuppressWarnings("rawtypes")
	private List resultList = null;//结果集
	public PageList() {

	}
	/**
	 * 不使用分页标签的初始化构造方法
	 * @param resultList
	 * @param toolBar
	 * @param offset
	 * @param curPageNO
	 * @param count
	 */
	@SuppressWarnings("rawtypes")
	public PageList(List resultList, String toolBar, int offset, int curPageNO, int count) {
		this.curPageNO = curPageNO;
		this.offset = offset;
		this.toolBar = toolBar;
		this.resultList = resultList;
		this.count = count;
	}
	/**
	 * 使用分页标签的初始化构造方法
	 * @param resultList
	 * @param toolBar
	 * @param offset
	 * @param curPageNO
	 * @param count
	 */
	@SuppressWarnings("rawtypes")
	public PageList(CriteriaQuery cq,List resultList, int offset, int curPageNO, int count) {
		this.curPageNO = curPageNO;
		this.offset = offset;
		this.resultList = resultList;
		this.count = count;
	}
	@SuppressWarnings("rawtypes")
	public PageList(HqlQuery cq,List resultList, int offset, int curPageNO, int count) {
		this.curPageNO = curPageNO;
		this.offset = offset;
		this.resultList = resultList;
		this.count = count;
	}
	public <T> List<T> getResultList() {
		return resultList;
	}

	@SuppressWarnings("rawtypes")
	public void setResultList(List resultList) {
		this.resultList = resultList;
	}

	public String getToolBar() {
		return toolBar;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	public int getCurPageNO() {
		return curPageNO;
	}

	public void setCurPageNO(int curPageNO) {
		this.curPageNO = curPageNO;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
	

}
