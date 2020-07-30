package retwis.pojo;

import lombok.Data;

@Data
public class Range {

    public static final int SIZE = 10;

    // 起始,从0开始
    private int start = 0;

    // 结束,总是小于total,但大于等于start
    private int end = start + SIZE;

    private int total; // 总记录数

    public Range(int pageNum) {
        this.start = (pageNum - 1) * SIZE;
        this.end = start + SIZE - 1;
    }

    public Range(int pageNum, int size) {
        this.start = (pageNum - 1) * size;
        this.end = start + size - 1;
    }

    /**
     * 此方法调用前需确保设置了total
     */
    public void checkValid() {
        if (total == 0) {
            end = start = 0;
            return;
        }
        end = end < total ? end : total - 1;
        start = start < 0 ? 0 : start;
    }

    public int next(int page) {
        int s = total % 10 == 0 ? total / 10 : total / 10 + 1;
        return page + 1 >  s ? s : page + 1;
    }

    public int prev(int page) {
        return page < 2 ? 1 : page - 1;
    }
}
