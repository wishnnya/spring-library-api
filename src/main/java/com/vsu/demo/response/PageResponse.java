package com.vsu.demo.response;

import java.util.List;

public class PageResponse<T> {
    public List<T> data;
    public int total;
    public int currentPage;

    public PageResponse(List<T> data, int total, int currentPage) {
        this.data = data;
        this.total = total;
        this.currentPage = currentPage;
    }
}