package com.akashrungta.utils;

import com.akashrungta.model.HttpEvent;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class LogUtilsTest {

    @Test
    public void testLogParsing(){
        Optional<HttpEvent> ret = LogUtils.parseLogLine("127.0.0.1 - mary [09/May/2018:16:00:42 +0100] \"POST /api/user HTTP/1.0\" 503 12");
        Assert.assertTrue(ret.isPresent());
        HttpEvent actual = ret.get();
        Assert.assertEquals("127.0.0.1", actual.getIp());
        Assert.assertEquals("mary", actual.getUserId());
        Instant expectedInstant = LocalDateTime.of(2018, 5, 9, 15, 0, 42, 0).toInstant(ZoneOffset.UTC);
        Assert.assertEquals(expectedInstant, actual.getInstant());
        Assert.assertEquals("POST", actual.getMethod());
        Assert.assertEquals("/api", actual.getSection());
        Assert.assertEquals("/api/user", actual.getPath());
        Assert.assertEquals(503, actual.getStatusCode());
        Assert.assertEquals(12, actual.getBytes());
    }

    @Test
    public void testEmptyLogParsing() {
        Optional<HttpEvent> ret = LogUtils.parseLogLine("abc");
        Assert.assertTrue(ret.isEmpty());
    }

}