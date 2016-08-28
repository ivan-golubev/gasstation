package net.ivango.gasstation;

import net.bigpoint.assessment.gasstation.GasPump;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentGasPump {

    private GasPump gasPump;
    private Lock lock = new ReentrantLock();

    public ConcurrentGasPump(GasPump gasPump){
        this.gasPump = gasPump;
    }

    /**
     * @return true if the associated gas pump has enough gas and the gas has been pumped successfully,
     * false otherwise
     * */
    public boolean pumpedGas(double amountToPump) {
        try {
            lock.lock();
            if (amountToPump > 0 && gasPump.getRemainingAmount() >= amountToPump ) {
                gasPump.pumpGas(amountToPump);
                return true;
            } else return false;
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        try {
            lock.lock();
            return gasPump.getRemainingAmount() == 0;
        } finally {
            lock.unlock();
        }
    }
}
