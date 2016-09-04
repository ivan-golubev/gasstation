package net.ivango.gasstation;


import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PipeBalancer {

    private Comparator<GasPump> comparator = (c1, c2) -> c1.getRemainingAmount() > c2.getRemainingAmount() ? 1
                                             : c1.getRemainingAmount() < c2.getRemainingAmount() ? -1 : 0;

    private PriorityBlockingQueue<GasPump> pumps = new PriorityBlockingQueue<>(10, comparator);
    private AtomicInteger pipesCount = new AtomicInteger();
    private SaleStats saleStats;

    private Logger logger = LoggerFactory.getLogger(PipeBalancer.class);

    private double price;
    private GasType gasType;

    public PipeBalancer(SaleStats saleStats, GasType gasType) {
        this.saleStats = saleStats;
        this.gasType = gasType;
    }

    public void addGasPump(GasPump gasPump){
        this.pumps.add(gasPump);
        this.pipesCount.incrementAndGet();
    }

    public double buyGas( double amountInLiters, double maxPricePerLiter ) throws NotEnoughGasException, GasTooExpensiveException {
        if (maxPricePerLiter < getPrice()) {
            saleStats.getCancellationsTooExpensiveCount().incrementAndGet();
            logger.info(
                    String.format("Requested price is too low: user requested to buy %s liters of %s for %s price.",
                            amountInLiters, gasType, maxPricePerLiter)
            );
            throw new GasTooExpensiveException();
        }

        /* while there is at least one non-empty pipe */
        while ( pipesCount.get() != 0 ) {
            try {
                /* wait for the next pipe to become available */
                GasPump gasPump = pumps.poll(100, TimeUnit.MILLISECONDS);

                if (gasPump != null) {
                    double moneyEarned = 0.0d;
                    /* gas pipe with maximum gas of this type - check if it is enough */
                    if (gasPump.getRemainingAmount() >= amountInLiters) {
                        gasPump.pumpGas(amountInLiters);
                        /* we are selling at a maximum price */
                        moneyEarned = amountInLiters * maxPricePerLiter;
                        saleStats.getRevenueAdder().add(moneyEarned);
                        saleStats.getSalesCount().incrementAndGet();
                        logger.info(String.format("User bought %s liters of %s for %s", amountInLiters, gasType, moneyEarned));
                    }
                    /* if this pipe is still not empty - add it back to the queue */
                    if (gasPump.getRemainingAmount() != 0) { pumps.put(gasPump); }
                    else { pipesCount.decrementAndGet(); }
                    if (moneyEarned != 0.0d) { return moneyEarned; }
                }
            } catch (InterruptedException ie) {
                logger.info("Gas retrieval interrupted");
            }
        }
        notEnoughGas(amountInLiters);
        return 0;
    }

    private void notEnoughGas(double amountInLiters) throws NotEnoughGasException {
        /* either queue is empty or the remaining pipes are busy with long operations */
        saleStats.getCancellationsNoGasCount().incrementAndGet();
        logger.info(String.format("Cannot find %s liters of %s gas.", amountInLiters, gasType));
        /* if we got here - no suitable pump was found */
        throw new NotEnoughGasException();
    }

    public synchronized double getPrice() { return price; }
    public synchronized void setPrice(double v) { this.price = v; }

}
