package net.ivango.gasstation;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.testng.Assert.assertEquals;

public class TexasGasStationTest {

    private Logger logger = LoggerFactory.getLogger(TexasGasStationTest.class);

    @Test
    public void setPriceTest() {
        TexasGasStation gasStation = new TexasGasStation();
        gasStation.setPrice(GasType.REGULAR, 1);
        gasStation.setPrice(GasType.SUPER, 2);
        gasStation.setPrice(GasType.DIESEL, 3);

        assertEquals(gasStation.getPrice(GasType.REGULAR), 1.0d);
        assertEquals(gasStation.getPrice(GasType.SUPER), 2.0d);
        assertEquals(gasStation.getPrice(GasType.DIESEL), 3.0d);
    }

    @Test
    public void addPumpsTest() {
        TexasGasStation gasStation = new TexasGasStation();
        gasStation.addGasPump( new GasPump(GasType.REGULAR, 3));
        gasStation.addGasPump( new GasPump(GasType.SUPER, 3));
        gasStation.addGasPump( new GasPump(GasType.DIESEL, 3));

        assertEquals(gasStation.getGasPumps().size(), 3);
    }

    @Test
    public void simplePurchaseTest() throws InterruptedException {
        TexasGasStation gasStation = new TexasGasStation();
        gasStation.setPrice(GasType.REGULAR, 1);
        gasStation.setPrice(GasType.SUPER, 1);
        gasStation.setPrice(GasType.DIESEL, 1);

        gasStation.addGasPump( new GasPump(GasType.REGULAR, 3));
        gasStation.addGasPump( new GasPump(GasType.SUPER, 3));
        gasStation.addGasPump( new GasPump(GasType.DIESEL, 3));

        ExecutorService threadPool = Executors.newFixedThreadPool(4);
        List<Callable<Void>> callables = new ArrayList<>();
        for (int i=0; i < 3; i++) {
            callables.add(() -> {
                gasStation.buyGas(GasType.REGULAR, 1, 1);
                return null;
            });
            callables.add(() -> {
                gasStation.buyGas(GasType.SUPER, 1, 1);
                return null;
            });
            callables.add(() -> {
                gasStation.buyGas(GasType.DIESEL, 1, 1);
                return null;
            });
        }
        List<Future<Void>> futures = threadPool.invokeAll(callables);

        assertEquals(gasStation.getNumberOfSales(), 9);
        assertEquals(gasStation.getNumberOfCancellationsNoGas(), 0);
        assertEquals(gasStation.getNumberOfCancellationsTooExpensive(), 0);
        assertEquals(gasStation.getRevenue(), 9.0d);

        threadPool.shutdownNow();
    }

    @Test
    public void cancellationsTest() throws InterruptedException {
        TexasGasStation gasStation = new TexasGasStation();
        gasStation.setPrice(GasType.REGULAR, 9);
        gasStation.setPrice(GasType.SUPER, 9);
        gasStation.setPrice(GasType.DIESEL, 9);

        gasStation.addGasPump( new GasPump(GasType.REGULAR, 3));
        gasStation.addGasPump( new GasPump(GasType.SUPER, 3));
        gasStation.addGasPump( new GasPump(GasType.DIESEL, 10));

        ExecutorService threadPool = Executors.newFixedThreadPool(4);
        List<Callable<Void>> callables = new ArrayList<>();

        callables.add(() -> {
            gasStation.buyGas(GasType.REGULAR, 1, 1);
            return null;
        });
        callables.add(() -> {
            gasStation.buyGas(GasType.SUPER, 1, 2);
            return null;
        });
        for (int i=0; i<2; i++) {
            callables.add(() -> {
                gasStation.buyGas(GasType.DIESEL, 10, 10);
                return null;
            });
        }

        List<Future<Void>> futures = threadPool.invokeAll(callables);

        assertEquals(gasStation.getNumberOfSales(), 1);
        assertEquals(gasStation.getNumberOfCancellationsNoGas(), 1);
        assertEquals(gasStation.getNumberOfCancellationsTooExpensive(), 2);
        assertEquals(gasStation.getRevenue(), 100.0d);

        threadPool.shutdownNow();
    }
}
