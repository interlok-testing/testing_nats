package com.adaptris.nats.test;

import com.adaptris.testing.DockerComposeFunctionalTest;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultFunctionalTest extends DockerComposeFunctionalTest {
    protected CloseableHttpClient client = HttpClients.createDefault();

    protected static String INTERLOK_SERVICE_NAME = "interlok-1";
    protected static String NATS_SERVICE_NAME = "nats-1";
    protected static int INTERLOK_PORT = 8080;
    protected static int NATS_PORT = 4222;
    protected static int NATS_MANAGEMENT_PORT = 8222;
    protected static WaitStrategy defaultWaitStrategy = Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30));




    @Override
    protected ComposeContainer setupContainers() throws Exception {
        return new ComposeContainer(new File("docker-compose.yml"))
                .withExposedService(INTERLOK_SERVICE_NAME, INTERLOK_PORT, defaultWaitStrategy)
                .withExposedService(NATS_SERVICE_NAME, NATS_PORT, defaultWaitStrategy)
                .withExposedService(NATS_SERVICE_NAME, NATS_MANAGEMENT_PORT, defaultWaitStrategy);
    }

    @Test
    void test() throws Exception{
        Thread.sleep(10000);
        InetSocketAddress mgmt = getHostAddressForService(NATS_SERVICE_NAME, NATS_MANAGEMENT_PORT);
        HttpGet httpGet = new HttpGet(String.format("http://%s:%s/connz", mgmt.getHostName(), mgmt.getPort()));
        try (CloseableHttpResponse response = client.execute(httpGet)) {
            assertEquals(200, response.getCode());
            ObjectMapper mapper = new ObjectMapper();
            Map connz = mapper.readValue(response.getEntity().getContent(), Map.class);
            Assertions.assertFalse(((List<?>) connz.get("connections")).isEmpty());
        }


    }
}
