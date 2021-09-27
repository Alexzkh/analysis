package com.zqykj.common.request;

import lombok.Data;
import lombok.ToString;

import java.util.Locale;
import java.util.Optional;

/**
 * @Description: sorting request
 * @Author zhangkehou
 * @Date 2021/9/23
 */
@Data
@ToString
public class SortingRequest {

    private String property;

    private Direction order;

    /**
     * Enumeration for sort directions.
     */
    public static enum Direction {

        ASC, DESC;

        /**
         * Returns whether the direction is ascending.
         *
         * @return
         */
        public boolean isAscending() {
            return this.equals(ASC);
        }

        /**
         * Returns whether the direction is descending.
         *
         * @return
         */
        public boolean isDescending() {
            return this.equals(DESC);
        }

        /**
         * Returns the {@link Direction} enum for the given {@link String} value.
         *
         * @param value
         * @throws IllegalArgumentException in case the given value cannot be parsed into an enum value.
         */
        public static Direction fromString(String value) {

            try {
                return Direction.valueOf(value.toUpperCase(Locale.US));
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(
                        "Invalid value '%s' for orders given! Has to be either 'desc' or 'asc' (case insensitive).", value), e);
            }
        }

        /**
         * Returns the {@link Direction} enum for the given {@link String} or null if it cannot be parsed into an enum
         * value.
         *
         * @param value
         * @return
         */
        public static Optional<Direction> fromOptionalString(String value) {

            try {
                return Optional.of(fromString(value));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
    }

}
