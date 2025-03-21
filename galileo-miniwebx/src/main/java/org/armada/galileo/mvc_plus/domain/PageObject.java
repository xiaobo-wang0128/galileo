package org.armada.galileo.mvc_plus.domain;


import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.common.page.PageList;

import java.util.List;

@Data
@Accessors(chain = true)
public class PageObject {

	private Object rows;

	// 当前页
	private Integer pageIndex;

	// 每页显示数量
	private Integer pageSize;

	// 总页数
	private Integer totalPage;

	// 总记录数
	private Integer totalSize;

	// 是否有下一页
	private Boolean hasNext;

	// 是否有上一页
	private Boolean hasPre;

	public PageObject(){
	}

	public PageObject(PageList pageList){
		this.setRows(pageList);
		this.setPageIndex(pageList.getPageIndex());
		this.setHasNext(pageList.getHasNext());
		this.setHasPre(pageList.getHasPre());
		this.setPageSize(pageList.getPageSize());
		this.setTotalPage(pageList.getTotalPage());
		this.setTotalSize(pageList.getTotalSize());
	}

}
