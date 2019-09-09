package com.wynprice.cafedafydd.common.utils;

import com.wynprice.cafedafydd.common.netty.NetworkHandler;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Log4j2
public class NetworkHandleScanner {
    public static NetworkConsumer generate(Class baseClass) {
        Map<Class, BiConsumer<NetworkHandler, Object>> maps = new HashMap<>();
        log.info("Scanning class " + baseClass + " for network handles");
        for (Method method : baseClass.getDeclaredMethods()) {
            log.info("Scanning method " + method.getName() + " for network handles");
            if(method.getAnnotation(NetworkHandle.class) != null && method.getParameterCount() == 1) {
                log.info("Added method '" + method.getName() + "' to accept '" + method.getParameterTypes()[0] + "'");
                maps.put(method.getParameterTypes()[0], (network, packet) -> {
                    try {
                        method.invoke(network, packet);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        return (handle, packet) ->
            maps.getOrDefault(packet.getClass(), (h, o) -> new IllegalArgumentException("Do not know how to handle class" + packet.getClass())).accept(handle, packet);
    }

}
