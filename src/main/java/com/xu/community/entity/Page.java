package com.xu.community.entity;

/**
 * 作用：封装分页相关的信息
 */
public class Page {
    // 当前页码
    private int current = 1;
    // 每页最大显示数量
    private int limit = 10;
    // 贴子总数(用于计算总页数)
    private int rows;
    // 查询路径（用于复用分页链接）
    private String staticPath;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;

        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {

            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0) {

            this.rows = rows;
        }
    }

    public String getStaticPath() {
        return staticPath;
    }

    public void setStaticPath(String staticPath) {
        this.staticPath = staticPath;
    }

    /**
     * 返回该页第一条数据在数据库中的起始行数
     *
     * @return
     */
    public int getOffset() {
        return (current - 1) * limit;
    }

    /**
     * 返回总页数:使用总帖数/每页显示数计算，特殊处理除不尽的情况。
     *
     * @return
     */
    public int getTotal() {
        if (rows % limit == 0) {
            return rows / limit;
        } else {
            return rows / limit + 1;
        }
    }

    /**
     * 计算起始页码
     */
    public int getFrom() {
        int from = current - 2;
        return from < 1 ? 1 : from;
    }

    /**
     * 计算终止页码
     */
    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return to > total ? total : to;
    }
}
