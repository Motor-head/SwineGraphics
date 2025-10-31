package mod.swinegraphics.service;

import java.util.HashMap;
import java.util.Map;
import mod.swinegraphics.service.anix.AnifServiceImpl;
import mod.swinegraphics.service.dxt.DxtServiceImpl;
import mod.swinegraphics.service.dxt.DxtWorker;

/**
 * Factory for Service.
 *
 * @author Nani
 */
public class ServiceFactory {

    private ServiceFactory() {
        throw new UnsupportedOperationException("ServiceFactory is a utility class.");
    }

    private static final Map<Class<?>, Service> serviceMap = new HashMap<>();
    private static DxtWorker dxtWorker;

    private static DxtWorker getDxtWorker() {
        if (dxtWorker == null) {
            dxtWorker = new DxtWorker();
        }
        return dxtWorker;
    }

    public static AnifService getAnifService() {
        Service service = serviceMap.computeIfAbsent(AnifService.class,
                s -> new AnifServiceImpl(
                        getDxtWorker()
                )
        );
        return AnifService.class.cast(service);
    }

    public static DxtService getDxtService() {
        Service service = serviceMap.computeIfAbsent(DxtService.class,
                s -> new DxtServiceImpl(
                        getDxtWorker()
                )
        );
        return DxtService.class.cast(service);
    }
}
