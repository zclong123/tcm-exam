package com.tcm.exam.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * 分页结果
 */
@Data
public class PageResult<T> {
    private long total;
    private long current;
    private long size;
    private List<T> records;

    public static <T> PageResult<T> of(Page<T> page) {
        PageResult<T> r = new PageResult<>();
        r.total = page.getTotal();
        r.current = page.getCurrent();
        r.size = page.getSize();
        r.records = page.getRecords();
        return r;
    }

    public static <T> PageResult<T> of(long total, List<T> records) {
        PageResult<T> r = new PageResult<>();
        r.total = total;
        r.records = records;
        return r;
    }
}
