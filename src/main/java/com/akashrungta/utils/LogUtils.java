package com.akashrungta.utils;

import com.akashrungta.model.HttpEvent;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogUtils {

    private static final Pattern LOG_PATTERN = Pattern.compile("(?<ip>[\\d\\.]+)\\s+(?<userIdentifier>[\\w-]+)\\s(?<userId>[\\w-]+)\\s\\[(?<dateTime>.*)\\]\\s\"(?<method>\\w+)\\s+(?<path>(?<section>\\/\\w+)\\/?[\\w\\/]*).*\"\\s(?<statusCode>\\d+)\\s(?<bytes>\\d+)");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");

    public static Optional<HttpEvent> parseLogLine(String logLine) {
        Matcher matcher = LOG_PATTERN.matcher(logLine);
        if(matcher.matches()){
            try {
                return Optional.of(buildEvent(matcher));
            } catch (Exception e){
                //TODO: log
            }
        }
        return Optional.empty();
    }

    private static HttpEvent buildEvent(Matcher matcher){
        HttpEvent.HttpEventBuilder builder = HttpEvent.builder();
        builder.ip(matcher.group("ip"));
        String userId = matcher.group("userId");
        // only if userId is present
        if(!"-".equals(userId)) {
            builder.userId(userId);
        }
        builder.instant(getInstant(matcher.group("dateTime")));
        builder.method(matcher.group("method"));
        builder.path(matcher.group("path"));
        builder.section(matcher.group("section"));
        builder.statusCode(Integer.parseInt(matcher.group("statusCode")));
        builder.bytes(Integer.parseInt(matcher.group("bytes")));
        return builder.build();
    }

    private static Instant getInstant(String dateTime) {
        return ZonedDateTime.parse(dateTime, FORMATTER).toInstant();
    }
}
