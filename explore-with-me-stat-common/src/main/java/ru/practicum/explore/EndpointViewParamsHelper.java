package ru.practicum.explore;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.practicum.explore.config.Config;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class EndpointViewParamsHelper {

    private static final String KEY_VALUE_MASK = "%s=%s";
    private static final String START_PARAM_NAME = "start";
    private static final String END_PARAM_NAME = "end";
    private static final String URIS_PARAM_NAME = "uris";
    private static final String UNIQUE_PARAM_NAME = "unique";

    //Закодированное представление параметров
    private final String startEncoded;
    private final String endEncoded;
    private final List<String> urisEncoded;

    //Оригинальное представление параметров
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final List<String> uris;
    private Boolean unique;

    public static EndpointViewParamsHelper ofOriginalValues(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        return new EndpointViewParamsHelper(
                getEncodedDateTime(start),
                getEncodedDateTime(end),
                getEncodedListUri(uris),
                start,
                end,
                uris,
                unique
        );
    }

    public static EndpointViewParamsHelper ofEncodedValues(String startEncoded, String endEncoded, List<String> urisEncoded, Boolean unique) {
        return new EndpointViewParamsHelper(
                startEncoded,
                endEncoded,
                urisEncoded,
                getDateTimeFromEncodedValue(startEncoded),
                getDateTimeFromEncodedValue(endEncoded),
                getListUriFromEncodedList(urisEncoded),
                unique
        );
    }

    private static String getEncodedDateTime(LocalDateTime time) {
        return URLEncoder.encode(time.format(Config.formatter), StandardCharsets.UTF_8);
    }

    private static LocalDateTime getDateTimeFromEncodedValue(String timeEncoded) {
        return LocalDateTime.parse(URLDecoder.decode(timeEncoded, StandardCharsets.UTF_8), Config.formatter);
    }

    private static List<String> getEncodedListUri(List<String> uris) {
        if (uris == null || uris.isEmpty()) {
            return null;
        }

        List<String> result = new ArrayList<>();

        for (String uri: uris) {
            result.add(URLEncoder.encode(uri, StandardCharsets.UTF_8));
        }

        return result;
     }

    private static List<String> getListUriFromEncodedList(List<String> urisEncoded) {
        if (urisEncoded == null || urisEncoded.isEmpty()) {
            return null;
        }

        List<String> result = new ArrayList<>();

        for (String uri: urisEncoded) {
            result.add(URLDecoder.decode(uri, StandardCharsets.UTF_8));
        }

        return result;
    }

    public String getQuery() {
        //получить строку вида, пример: ?start=encoded&end=encoded&uris=encoded1&uris=encoded2&unique=false

        List<String> params = new ArrayList<>();

        if (startEncoded != null) {
           params.add(String.format(KEY_VALUE_MASK, START_PARAM_NAME, startEncoded));
        }

        if (endEncoded != null) {
            params.add(String.format(KEY_VALUE_MASK, END_PARAM_NAME, endEncoded));
        }

        if (urisEncoded != null && !urisEncoded.isEmpty()) {
            for (String encodedUri: urisEncoded) {
                params.add(String.format(KEY_VALUE_MASK, URIS_PARAM_NAME, encodedUri));
            }
        }

        if (unique != null) {
            params.add(String.format(KEY_VALUE_MASK, UNIQUE_PARAM_NAME, unique));
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            if (i == 0) {
                result.append("?");
                result.append(params.get(i));
                continue;
            }

            result.append("&");
            result.append(params.get(i));
        }

        return result.toString();
    }
}
