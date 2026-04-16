package es.urjc.ecomostoles.backend.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Platform-wide utility for standardized numeric formatting.
 * 
 * Enforces the rule:
 * - 2 decimal places by default (e.g., 0.1 -> 0,10).
 * - Integer representation if no significant decimals exist (e.g., 5.0 -> 5).
 * - Uses Spanish locale (comma as decimal separator) for consistent UX.
 */
public class NumberFormatter {

    private static final DecimalFormat decimalFormat;
    private static final DecimalFormat integerFormat;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("es", "ES"));
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');

        decimalFormat = new DecimalFormat("#,##0.00", symbols);
        integerFormat = new DecimalFormat("#,##0", symbols);
    }

    /**
     * Formats a double value according to platform rules.
     * 
     * @param value numerical payload
     * @return formatted string (e.g. "5", "5,10", "1.250")
     */
    public static String format(Double value) {
        if (value == null) return "0";
        
        // Check if value is essentially an integer
        if (Math.abs(value - Math.floor(value)) < 0.000001) {
            return integerFormat.format(value);
        } else {
            return decimalFormat.format(value);
        }
    }

    /**
     * Formats a value adding the Euro currency symbol.
     * 
     * @param value numerical payload
     * @return formatted string (e.g. "5 €", "5,10 €")
     */
    public static String formatCurrency(Double value) {
        return format(value) + " €";
    }
}
