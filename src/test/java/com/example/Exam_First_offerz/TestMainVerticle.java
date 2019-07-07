package com.example.Exam_First_offerz;

import io.vertx.core.Verticle;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.codec.BodyCodec;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

  @BeforeEach
  public void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle((Verticle) testContext.succeeding(id -> {
      WebClient client = WebClient.create(vertx);
      client.get(8080, "localhost", "/healthcheck")
        .as(BodyCodec.string())
        .send(testContext.succeeding(response -> testContext.verify(() -> {
          Assert.assertEquals(response.body(), "I'm alive!!!");
          testContext.completeNow();
        })));
    }));
    // vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  public void verticle_deployed(Vertx vertx, VertxTestContext testContext) throws Throwable {
    testContext.completeNow();

  }
}
