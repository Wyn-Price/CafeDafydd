package com.wynprice.cafedafydd.common.utils;

import com.wynprice.cafedafydd.common.netty.NetworkHandler;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class NetworkHandleScanner {
    private static final Matcher LAMBDA_METHOD_PATTERN = Pattern.compile("lambda\\$(.+)\\$\\d+").matcher("");

    public static NetworkConsumer generateConsumer(Class baseClass) {
        Map<Class, BiConsumer<NetworkHandler, Object>> maps = new HashMap<>();
        log.info("Scanning class " + baseClass + " for network handles");
        for (Method method : baseClass.getDeclaredMethods()) {
            if(method.getAnnotation(NetworkHandle.class) != null) {
                StringBuilder builder = new StringBuilder(String.format("%-45s", "Scanning method " + method.getName()) + ": ");
                if(method.getParameterCount() == 1) {
                    builder.append(method.getParameterTypes()[0].getSimpleName());
                    maps.put(method.getParameterTypes()[0], (network, packet) -> {
                        try {
                            method.invoke(network, packet);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    builder.append("Too many arguments!");
                }
                log.info(builder);
            }
        }
        return (handle, packet) ->
            maps.getOrDefault(packet.getClass(), (h, o) -> new IllegalArgumentException("Do not know how to handle class" + packet.getClass())).accept(handle, packet);
    }

}
