package net.ivango.gasstation;


import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasStation;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

public class TexasGasStation implements GasStation {

    private List<ConcurrentGasPump> regularFilledPumps = new CopyOnWriteArrayList<>(),
                                    superFilledPumps = new CopyOnWriteArrayList<>(),
                                    dieselFilledPumps = new CopyOnWriteArrayList<>();

    private List<GasPump> allGasPumps = new CopyOnWriteArrayList<>();
    private Map<GasType, Double> price = new ConcurrentHashMap<>();

    /* Accumulative statistics */
    private DoubleAdder revenue = new DoubleAdder();
    private AtomicInteger salesCount = new AtomicInteger(),
                          cancellationsNoGasCount = new AtomicInteger(),
                          cancellationsTooExpensiveCount = new AtomicInteger();

    private Logger logger = LoggerFactory.getLogger(TexasGasStation.class);

    public void addGasPump(GasPump gasPump) {
        logger.info(String.format("Adding a gas pump: %s", gasPump.getGasType()));
        getFilledPumps(gasPump.getGasType()).add( new ConcurrentGasPump(gasPump) );
        allGasPumps.add(gasPump);
    }

    private List<ConcurrentGasPump> getFilledPumps(GasType gasType) {
        switch(gasType) {
            case REGULAR:   return regularFilledPumps;
            case SUPER:     return superFilledPumps;
            case DIESEL:    return dieselFilledPumps;
        }
        throw new IllegalArgumentException("Unknown gas type");
    }

    public Collection<GasPump> getGasPumps() { return new ArrayList<>(allGasPumps); }

    public double buyGas(GasType gasType, double amountInLiters, double maxPricePerLiter) throws NotEnoughGasException,
                                                                                                 GasTooExpensiveException {
        if (maxPricePerLiter < price.get(gasType)) {
            cancellationsTooExpensiveCount.incrementAndGet();
            logger.info(
                    String.format("Requested price is too low: user requested to buy %s liters of %s for %s price.",
                            amountInLiters, gasType, maxPricePerLiter)
            );
            throw new GasTooExpensiveException();
        }

        List<ConcurrentGasPump> filledPumps = getFilledPumps(gasType);
        double earnings = 0;
        if ( !filledPumps.isEmpty() ) {

            List<ConcurrentGasPump> toRemove = new ArrayList<>();

            for (ConcurrentGasPump gasPump: filledPumps) {

                /* gas found and pumped - exit, we are selling at a maximum price */
                if (gasPump.pumpedGas(amountInLiters)) {
                    double moneyEarned = amountInLiters * maxPricePerLiter;
                    revenue.add(moneyEarned);
                    salesCount.incrementAndGet();
                    logger.info(String.format("User bought %s liters of %s for %s", amountInLiters, gasType, moneyEarned));
                    earnings = moneyEarned;
                    break;
                }
                /* clean up the gas pump, so that we don't need to iterate over it the next time */
                if (gasPump.isEmpty()) { toRemove.add(gasPump); }
            }
            filledPumps.removeAll(toRemove);
        }
        if ( earnings != 0.0d ) {
            return earnings;
        } else {
            cancellationsNoGasCount.incrementAndGet();
            logger.info(String.format("Cannot find %s liters of %s gas.", amountInLiters, gasType));
            /* if we got here - no suitable pump was found */
            throw new NotEnoughGasException();
        }
    }

    public double getRevenue() { return revenue.sum(); }
    public int getNumberOfSales() { return salesCount.get(); }
    public int getNumberOfCancellationsNoGas() { return cancellationsNoGasCount.get(); }
    public int getNumberOfCancellationsTooExpensive() { return cancellationsTooExpensiveCount.get(); }

    public double getPrice(GasType gasType) { return price.get(gasType); }
    public void setPrice(GasType gasType, double v) { price.put(gasType, v); }
}
