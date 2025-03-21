package org.armada.galileo.common.page;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PageResponse<T> {

	private Integer totalSize;

	private Boolean hasPre;

	private Boolean hasNext;

	private Integer pageIndex;

	private Integer totalPage;

	private Integer pageSize;

	protected List<T> list;

	public PageResponse() {
	}

	public PageResponse(PageList page) {
		this.totalSize = page.getTotalSize();
		this.pageIndex = page.getPageIndex();
		this.pageSize = page.getPageSize();
		this.totalPage = page.getTotalPage();
		this.hasPre = page.getHasPre();
		this.hasNext = page.getHasNext();
		this.list = page;
	}

}
