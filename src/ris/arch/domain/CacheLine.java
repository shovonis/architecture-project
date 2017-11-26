package ris.arch.domain;

public class CacheLine {
    private String level;
    private int validBit = 0;
    private int dirtyBit = 0;
    private long time = 0;
    private int tag;

    //Meta Data of the Cache Lines. This is duplicated intentionally for ease of access.
    private int line;
    private int way;
    private String size;
    private long hitTime;
    private String writePolicy;
    private String allocationPolicy;


    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getValidBit() {
        return validBit;
    }

    public void setValidBit(int validBit) {
        this.validBit = validBit;
    }

    public int getDirtyBit() {
        return dirtyBit;
    }

    public void setDirtyBit(int dirtyBit) {
        this.dirtyBit = dirtyBit;
    }

    public int getTag(int i) {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getTag() {
        return tag;
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

    @Override
    public String toString() {
        return "Block{" +
                "level='" + level + '\'' +
                ", validBit=" + validBit +
                ", dirtyBit=" + dirtyBit +
                ", time=" + time +
                ", tag=" + tag +
                ", line=" + line +
                ", way=" + way +
                ", size='" + size + '\'' +
                ", hitTime=" + hitTime +
                ", writePolicy='" + writePolicy + '\'' +
                ", allocationPolicy='" + allocationPolicy + '\'' +
                '}';
    }
}
