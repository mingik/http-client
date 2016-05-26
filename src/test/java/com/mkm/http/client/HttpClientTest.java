package com.mkm.http.client;

import com.mkm.http.client.domain.MyModel;
import com.mkm.http.client.domain.MyModelOne;
import com.mkm.http.client.exception.HttpClientException;
import junit.framework.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.fail;


/**
 * Make sure you run Server.java prior to executing this test!!!
 *
 * Created by mintik on 5/25/16.
 */
public class HttpClientTest {
    @Test
    public void testClientNoConnection() throws InterruptedException {
        HttpClient httpClient = new HttpClient();
        httpClient.setUrl("http://localhost:9999/");
        CompletableFuture<MyModelOne> myModelFuture = httpClient.retrieve(MyModelOne.class);
        myModelFuture.whenComplete((myModelOne, throwable) -> {
            System.out.println("myModelOne = " + myModelOne);
            System.out.println("throwable = " + throwable);
        });

        try {
            MyModel myModel = myModelFuture.get(1000, TimeUnit.SECONDS);
            fail("This should throw an exception because Connection is refused.");
        } catch (Exception e) {
            Assert.assertTrue(e.getCause() instanceof HttpClientException);
        }
    }

    @Test
    public void testClientBadResponse() throws Exception {
        HttpClient httpClient = new HttpClient();
        httpClient.setUri("notFound");
        CompletableFuture<MyModelOne> myModelFuture = httpClient.retrieve(MyModelOne.class);
        myModelFuture.whenComplete((myModelOne, throwable) -> {
            System.out.println("myModelOne = " + myModelOne);
            System.out.println("throwable = " + throwable);
        });

        try {
            MyModel myModel = myModelFuture.get(1000, TimeUnit.SECONDS);
            fail("This should throw an exception because we couldn't parse response.");
        } catch (Exception e) {
            Assert.assertTrue(e.getCause() instanceof HttpClientException);
        }
    }

    @Test
    public void testClientGoodResponse() throws Exception {
        HttpClient httpClient = new HttpClient();

        CompletableFuture<MyModelOne> myModelFuture = httpClient.retrieve(MyModelOne.class);
        myModelFuture.whenComplete((myModelOne, throwable) -> {
            System.out.println("myModelOne = " + myModelOne);
            System.out.println("throwable = " + throwable);
        });

        try {
            MyModel myModel = myModelFuture.get(1000, TimeUnit.SECONDS);
            Assert.assertTrue(myModel instanceof MyModelOne);
            Assert.assertTrue(((MyModelOne)myModel).getName().equals("one"));
            Assert.assertTrue(((MyModelOne)myModel).getId() == 1);
        } catch (Exception e) {
            fail("This should throw an exception because we couldn't parse response.");
        }
    }


}
