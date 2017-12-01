package ris.arch.domain;

/**
 * This class contains the Request that will be processed next in the cache or memory
 */
public class ReferenceRequest {
    private int memoryRefReq;
    private int nextAccess;

    public ReferenceRequest(int memoryRefReq, int nextAccess) {
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
