package com.akashrungta.utils;

import com.akashrungta.model.HttpEvent;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.Assert.*;

public class LogUtilsTest {

    @Test
    public void testLogParsing(){
        Optional<HttpEvent> ret = LogUtils.parseLogLine("127.0.0.1 - mary [09/May/2018:16:00:42 +0100] \"POST /api/user HTTP/1.0\" 503 12");
        Assert.assertTrue(ret.isPresent());
        HttpEvent actual = ret.get();
        Assert.assertEquals("127.0.0.1", actual.getIp());
        Assert.assertEquals("mary", actual.getHostname());
        Instant expectedInstant = LocalDateTime.of(2018, 05, 9, 15, 00, 42, 0).toInstant(ZoneOffset.UTC);
        Assert.assertEquals(expectedInstant, actual.getDateTime());
        Assert.assertEquals("POST", actual.getMethod());
        Assert.assertEquals("/api", actual.getSection());
        Assert.assertEquals(503, actual.getStatusCode());
    }

}