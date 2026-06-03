package com.tiv.rating.system.util;

public interface Lock {

    boolean tryLock(Long timeout);

    void unlock();

}
