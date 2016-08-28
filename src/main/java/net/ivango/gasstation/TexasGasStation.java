package net.ivango.gasstation;


import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasStation;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Collection;

public class TexasGasStation implements GasStation {

    private Logger logger = LoggerFactory.getLogger(TexasGasStation.class);


    public void addGasPump(GasPump gasPump) {

    }

    public Collection<GasPump> getGasPumps() {
        return null;
    }

    public double buyGas(GasType gasType, double v, double v1) throws NotEnoughGasException, GasTooExpensiveException {
        return 0;
    }

    public double getRevenue() {
        return 0;
    }

    public int getNumberOfSales() {
        return 0;
    }

    public int getNumberOfCancellationsNoGas() {
        return 0;
    }

    public int getNumberOfCancellationsTooExpensive() {
        return 0;
    }

    public double getPrice(GasType gasType) {
        return 0;
    }

    public void setPrice(GasType gasType, double v) {

    }
}
