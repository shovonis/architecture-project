package ris.arch.domain;

public class CacheConf {
    private String level;
    private int line;
    private int way;
    private String size;
    private long hitTime;
    private String writePolicy;
    private String allocationPolicy;
    private String tmpWay;


    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getWay() {
        return way;
    }

    public void setWay(int way) {
        this.way = way;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public long getHitTime() {
        return hitTime;
    }

    public void setHitTime(long hitTime) {
        this.hitTime = hitTime;
    }

    public String getWritePolicy() {
        return writePolicy;
    }

    public void setWritePolicy(String writePolicy) {
        this.writePolicy = writePolicy;
    }

    public String getAllocationPolicy() {
        return allocationPolicy;
    }

    public void setAllocationPolicy(String allocationPolicy) {
        this.allocationPolicy = allocationPolicy;
    }

    public String getTmpWay() {
        return tmpWay;
    }

    public void setTmpWay(String tmpWay) {
        this.tmpWay = tmpWay;
    }

    @Override
    public String toString() {
        return "CacheConf{" +
                "level='" + level + '\'' +
                ", line='" + line + '\'' +
                ", way=" + way +
                ", size='" + size + '\'' +
                ", hitTime=" + hitTime +
                ", writePolicy='" + writePolicy + '\'' +
                ", allocationPolicy='" + allocationPolicy + '\'' +
                '}';
    }
}
