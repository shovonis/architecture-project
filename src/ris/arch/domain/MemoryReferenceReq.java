package ris.arch.domain;

import ris.arch.util.MemoryRefReq;

public class MemoryReferenceReq {
    private int memoryRefReq;
    private int nextAccess;

    public MemoryReferenceReq(int memoryRefReq, int nextAccess) {
        this.memoryRefReq = memoryRefReq;
        this.nextAccess = nextAccess;
    }

    public int getMemoryRefReq() {
        return memoryRefReq;
    }

    public void setMemoryRefReq(int memoryRefReq) {
        this.memoryRefReq = memoryRefReq;
    }

    public int getNextAccess() {
        return nextAccess;
    }

    public void setNextAccess(int nextAccess) {
        this.nextAccess = nextAccess;
    }
}
