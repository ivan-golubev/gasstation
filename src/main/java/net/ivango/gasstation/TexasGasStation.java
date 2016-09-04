package net.ivango.gasstation;


import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasStation;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class TexasGasStation implements GasStation {

    private List<GasPump> allGasPumps = new CopyOnWriteArrayList<>();
    private SaleStats saleStats = new SaleStats();
    private Map<GasType, PipeBalancer> balancers = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(TexasGasStation.class);

    public TexasGasStation() {
        balancers.put(GasType.REGULAR, new PipeBalancer(saleStats, GasType.REGULAR));
        balancers.put(GasType.SUPER, new PipeBalancer(saleStats, GasType.SUPER));
        balancers.put(GasType.DIESEL, new PipeBalancer(saleStats, GasType.DIESEL));
    }

    public void addGasPump(GasPump gasPump) {
        logger.info(String.format("Adding a gas pump: %s", gasPump.getGasType()));
        balancers.get(gasPump.getGasType()).addGasPump(gasPump);
        allGasPumps.add(gasPump);
    }

    public Collection<GasPump> getGasPumps() { return new ArrayList<>(allGasPumps); }

    public double buyGas(GasType gasType, double amountInLiters, double maxPricePerLiter) throws NotEnoughGasException,
                                                                                                 GasTooExpensiveException {
        return balancers.get(gasType).buyGas(amountInLiters, maxPricePerLiter);
    }

    public double getRevenue() { return saleStats.getTotalRevenue(); }
    public int getNumberOfSales() { return saleStats.getNumberOfSales(); }
    public int getNumberOfCancellationsNoGas() { return saleStats.getNumberOfCancellationsNoGas(); }
    public int getNumberOfCancellationsTooExpensive() { return saleStats.getNumberOfCancellationsTooExpensive(); }

    public double getPrice(GasType gasType) { return balancers.get(gasType).getPrice(); }
    public void setPrice(GasType gasType, double v) { balancers.get(gasType).setPrice(v); }
}
