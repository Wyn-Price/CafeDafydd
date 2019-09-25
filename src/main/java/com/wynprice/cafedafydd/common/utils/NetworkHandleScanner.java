package com.wynprice.cafedafydd.common.utils;

import com.wynprice.cafedafydd.common.netty.NetworkHandler;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A util class to generate the {@link NetworkConsumer} for a given class.
 * @see #generateConsumer(Class)
 */
@Log4j2
public class NetworkHandleScanner {
    /**
     * Generates the {@link NetworkConsumer} for the given class. Essentially, this
     * just scans the {@code baseClass} for methods annotated with {@link NetworkHandle}
     * and with one parameter. That method is then assigned to the class of the parameter,
     * and when the returned {@link NetworkConsumer#accept(Object, Object)} is invoked, the
     * first argument is taken and used to find the method to invoke.
     * @param baseClass the class to scan
     * @return the packet consumer
     */
    public static NetworkConsumer generateConsumer(Class baseClass) {
        Map<Class, BiConsumer<NetworkHandler, Object>> maps = new HashMap<>();
        log.info("Scanning class " + baseClass + " for network handles");
        //Iterate though all the methods in the class, checking that the method is annotated with NetworkHandle
        for (Method method : baseClass.getDeclaredMethods()) {
            if(method.getAnnotation(NetworkHandle.class) != null) {
                //Start the log string, then check that the method only has one parameter. If it doesn't add an error to the log string.
                StringBuilder builder = new StringBuilder(String.format("%-45s", "Scanning method " + method.getName()) + ": ");
                if(method.getParameterCount() == 1) {
                    //Add the parameter type to the log string, then put the handler for that type into the map
                    builder.append(method.getParameterTypes()[0].getSimpleName());
                    maps.put(method.getParameterTypes()[0], (network, packet) -> {
                        try {
                            method.invoke(network, packet);
                        } catch (InvocationTargetException e)  {
                            throwException(e.getCause());
                        } catch (Throwable e) {
                            throwException(e);
                        }
                    });
                } else {
                    builder.append("Too many arguments!");
                }
                log.info(builder);
            }
        }
        //Return the handler that gets the handler in the map or throw an IllegalArgumentException if it isn't registered.
        return (handle, packet) ->
            maps.getOrDefault(packet.getClass(), (h, o) -> new IllegalArgumentException("Do not know how to handle class" + packet.getClass())).accept(handle, packet);
    }


    /**
     * Tricks the compiler to not care about compiler errors and throw the error e
     * @param e the exception to throw
     * @see SneakyThrows
     */
    @SneakyThrows
    private static void throwException(Throwable e) {
        throw e;
    }

}
