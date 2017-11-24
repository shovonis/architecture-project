package ris.arch.domain;

public class MemoryConf {
    private String level;
    private long hitTime;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public long getHitTime() {
        return hitTime;
    }

    public void setHitTime(long hitTime) {
        this.hitTime = hitTime;
    }

    @Override
    public String toString() {
        return "MemoryConf{" +
                "level='" + level + '\'' +
                ", hitTime=" + hitTime +
                '}';
    }
}
