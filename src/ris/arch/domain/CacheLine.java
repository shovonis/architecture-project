package ris.arch.domain;

public class CacheLine {
    private String level;
    private int validBit = 0;
    private int dirtyBit = 0;
    private long time = 0;
    private int tag;

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

    @Override
    public String toString() {
        return "CacheLine{" +
                "level='" + level + '\'' +
                ", validBit=" + validBit +
                ", dirtyBit=" + dirtyBit +
                ", time=" + time +
                ", tag=" + tag +
                '}';
    }
}
