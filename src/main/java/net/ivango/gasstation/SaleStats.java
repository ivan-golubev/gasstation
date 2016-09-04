package net.ivango.gasstation;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

public class SaleStats {
    /* Accumulative statistics */
    private DoubleAdder revenue = new DoubleAdder();
    private AtomicInteger salesCount = new AtomicInteger(),
            cancellationsNoGasCount = new AtomicInteger(),
            cancellationsTooExpensiveCount = new AtomicInteger();

    public AtomicInteger getSalesCount() { return salesCount; }
    public AtomicInteger getCancellationsNoGasCount() { return cancellationsNoGasCount; }
    public AtomicInteger getCancellationsTooExpensiveCount() { return cancellationsTooExpensiveCount; }
    public DoubleAdder getRevenueAdder() { return revenue; }

    public double getTotalRevenue() { return revenue.sum(); }
    public int getNumberOfSales() { return salesCount.get(); }
    public int getNumberOfCancellationsNoGas() { return cancellationsNoGasCount.get(); }
    public int getNumberOfCancellationsTooExpensive() { return cancellationsTooExpensiveCount.get(); }
}
