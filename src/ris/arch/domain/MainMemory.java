package ris.arch.domain;

/**
 * Main memory entity class.
 */
public class MainMemory {
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
        return "MainMemory{" +
                "level='" + level + '\'' +
                ", hitTime=" + hitTime +
                '}';
    }
}
