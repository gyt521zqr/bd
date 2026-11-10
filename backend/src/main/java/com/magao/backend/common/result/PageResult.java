package com.magao.backend.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装类
 * <p>
 * 封装分页查询的结果数据，包含：
 * - total：总记录数
 * - list：当前页数据列表（与前端 PageResult{list,...} 契约对齐）
 * - pages：总页数
 * - pageNum：当前页码
 * - pageSize：每页大小
 * </p>
 * <p>
 * 使用示例：
 * PageResult&lt;UserVO&gt; pageResult = PageResult.from(page);
 * </p>
 *
 * @param <T> 数据列表元素类型
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private Long total;

    /** 当前页数据列表（与前端契约字段名一致） */
    private List<T> list;

    /** 总页数 */
    private Long pages;

    /** 当前页码 */
    private Long pageNum;

    /** 每页大小 */
    private Long pageSize;

    /**
     * 默认构造方法
     */
    public PageResult() {
    }

    /**
     * 根据MyBatis-Plus分页对象和数据列表构造分页结果
     *
     * @param page    MyBatis-Plus分页对象
     * @param list    数据列表
     */
    public PageResult(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page, List<T> list) {
        this.total = page.getTotal();
        this.list = list;
        this.pages = page.getPages();
        this.pageNum = page.getCurrent();
        this.pageSize = page.getSize();
    }

    /**
     * 根据总记录数、数据列表、页码和每页大小构造分页结果
     *
     * @param total    总记录数
     * @param list     数据列表
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     */
    public PageResult(Long total, List<T> list, Long pageNum, Long pageSize) {
        this.total = total;
        this.list = list;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        // 计算总页数
        this.pages = (total + pageSize - 1) / pageSize;
    }

    /**
     * 由 MyBatis-Plus 分页对象构造 PageResult，字段名对齐前端契约（list）
     *
     * @param page MyBatis-Plus 分页对象
     * @param <T>  数据元素类型
     * @return 分页结果
     */
    public static <T> PageResult<T> from(IPage<T> page) {
        PageResult<T> pr = new PageResult<>();
        pr.setTotal(page.getTotal());
        pr.setList(page.getRecords());
        pr.setPages(page.getPages());
        pr.setPageNum(page.getCurrent());
        pr.setPageSize(page.getSize());
        return pr;
    }
}
