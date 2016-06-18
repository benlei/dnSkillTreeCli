package com.github.ben_lei.dncli.converter;

import com.beust.jcommander.IStringConverter;

import java.util.Objects;

/**
 * Created by blei on 6/17/16.
 */
public class ByteCharacterConverter implements IStringConverter<Long> {
    @Override
    public Long convert(String value) {
        Objects.requireNonNull(value, "byte value is null");
        long min;

        char c = value.toLowerCase().charAt(value.length() - 1);
        if (c == 'b' || c == 'k' || c == 'm' || c == 'g') {
            value = value.substring(0, value.length() - 1);
        } else if (c < '0' || c > '9') {
            throw new IllegalArgumentException(String.format("'%c' is not a valid file size", c));
        }

        // throw exception if size is too big
        min = Integer.parseInt(value);
        if (c == 'k') {
            min = Math.multiplyExact(min, 1024);
        } else if (c == 'm') {
            min = Math.multiplyExact(min, 1024 * 1024);
        } else if (c == 'g') {
            min = Math.multiplyExact(min, 1024 * 1024 * 1024);
        }

        return min;
    }
}
