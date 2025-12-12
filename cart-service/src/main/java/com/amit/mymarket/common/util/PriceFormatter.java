package com.amit.mymarket.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PriceFormatter {

    public static String formatPrice(Long minor) {
        if (minor == null) {
            return "0";
        }
        BigDecimal price = BigDecimal.valueOf(minor).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return price.toString();
    }

    public static long convertToPriceMinor(String price) {
        return new BigDecimal(price)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    private PriceFormatter() {
        throw new UnsupportedOperationException();
    }

}
