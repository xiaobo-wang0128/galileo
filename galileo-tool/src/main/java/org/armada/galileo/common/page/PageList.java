package org.armada.galileo.common.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PageList<T> extends ArrayList<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer totalSize;

    private Boolean hasPre;

    private Boolean hasNext;

    private Integer pageIndex;

    private Integer totalPage;

    private Integer pageSize;

    public PageList() {
        super();
    }

    public PageList(int size) {
        super(size);
    }

    public PageList(Collection<? extends T> c) {
        super(c);
    }

    public Integer getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Integer totalSize) {
        this.totalSize = totalSize;
    }

    public Boolean getHasPre() {
        return hasPre;
    }

    public void setHasPre(Boolean hasPre) {
        this.hasPre = hasPre;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }


    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public static <T> PageList<T>  copyFrom(PageList<?> src) {
        PageList pl = new PageList();
        pl.setTotalSize(src.getTotalSize());
        pl.setHasPre(src.getHasPre());
        pl.setHasNext(src.getHasNext());
        pl.setPageIndex(src.getPageIndex());
        pl.setTotalPage(src.getTotalPage());
        pl.setPageSize(src.getPageSize());
        return pl;
    }

//    public PageList(Integer pageIndex, Integer pageSize, List<T> data) {
//        pageIndex = pageIndex == null ? 1 : pageIndex;
//        pageSize = pageSize == null ? 20 : pageSize;
//        this.setPageIndex(pageIndex);
//        this.setPageSize(pageSize);
//        this.setTotalSize(0);
//        this.setHasPre(false);
//        this.setHasNext(false);
//        if (data != null) {
//            this.setTotalSize(data.size());
//            this.setTotalPage(data.size() / this.pageSize + 1);
//            this.setHasPre(this.pageIndex > 1);
//            this.setHasNext(this.pageIndex < this.totalPage);
//            this.addAll(
//                    data.subList(
//                            Math.min((pageIndex - 1) * pageSize, data.size()),
//                            Math.min((pageIndex * pageSize), data.size())));
//        }
//
//    }

}
