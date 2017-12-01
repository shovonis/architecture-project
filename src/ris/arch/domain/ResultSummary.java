package ris.arch.domain;

/**
 * Store the result from the cache and main memory.
 */
public class ResultSummary {
    private String level;
    private int access = 0;
    private int hit = 0;
    private int miss = 0;
    private int totalTime = 0;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getAccess() {
        return access;
    }

    public void setAccess(int access) {
        this.access = access;
    }

    public int getHit() {
        return hit;
    }

    public void setHit(int hit) {
        this.hit = hit;
    }

    public int getMiss() {
        return miss;
    }

    public void setMiss(int miss) {
        this.miss = miss;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    @Override
    public String toString() {
        return "" +
                "Level='" + level + '\'' +
                "\n access=" + access +
                "\n hit=" + hit +
                "\n miss=" + miss +
                "";
    }
}
