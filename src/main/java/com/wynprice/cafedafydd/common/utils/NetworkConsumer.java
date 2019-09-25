package com.wynprice.cafedafydd.common.utils;

import com.wynprice.cafedafydd.common.netty.NetworkHandler;

import java.util.function.BiConsumer;

/**
 * Just used to make the code look nice.
 */
public interface NetworkConsumer extends BiConsumer<NetworkHandler, Object> {
}
