package com.mkm.http.client;

import com.mkm.http.client.domain.MyModel;
import com.mkm.http.client.domain.MyModelOne;
import com.mkm.http.client.domain.MyModelTwo;
import com.mkm.http.client.exception.HttpClientException;
import com.mkm.http.client.app.*;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static junit.framework.Assert.fail;
import static junit.framework.TestCase.assertTrue;


/**
 * Make sure you run Server.java prior to executing this test!!!
 *
 * Created by mintik on 5/25/16.
 */
public class HttpClientTest {
    @BeforeClass
    public static void onlyOnce() {
	// Run Server in the background...
	CompletableFuture.runAsync(() -> {
		try {
		    Server.main(new String[]{});
		} catch (Exception e) {
		    e.printStackTrace();
		    fail("Couldn't start the Server");
		};
	    }, Executors.newFixedThreadPool(1));
    }

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
        CompletableFuture<MyModel> myModelFuture = httpClient.retrieve(MyModel.class);
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
            Assert.assertTrue(((MyModelOne) myModel).getId() == 1);
        } catch (Exception e) {
            fail("This should throw an exception because we couldn't parse response.");
        }
    }

    @Test
    public void testParallelGoodResponses() throws Exception {
        HttpClient httpClient = new HttpClient();
        final MyModel modelOne = new MyModelOne("dup", 2);
        final MyModel modelTwo = new MyModelTwo("dup", 2, 33.33);

        List<CompletableFuture<MyModel>> listOfCFs = new ArrayList<>();

        CompletableFuture<MyModel> myModelOneCF = httpClient.retrieve(MyModelOne.class).thenApply(myModelOne -> (MyModel)myModelOne);
        CompletableFuture<MyModel> myModelTwoCF = httpClient.retrieve(MyModelTwo.class).thenApply(myModelTwo -> (MyModel)myModelTwo);
        CompletableFuture<MyModel> myModelOneDupCF = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return modelOne;
        });
        CompletableFuture<MyModel> myModelTwoDupCF = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return modelTwo;
        });

        listOfCFs.add(myModelOneCF);
        listOfCFs.add(myModelTwoCF);
        listOfCFs.add(myModelOneDupCF);
        listOfCFs.add(myModelTwoDupCF);

        CompletableFuture<List<MyModel>> listOfModelsCF = CompletableFuture.allOf(listOfCFs.toArray(new CompletableFuture[listOfCFs.size()]))
                .thenApply(aVoid -> listOfCFs.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        listOfModelsCF.whenComplete((myModels, throwable) -> {
            if (throwable != null) {
                System.out.println("Received throwable = " + throwable);
            } else {
                System.out.println("Received list of Models: " + myModels);
            }
        });

        List<MyModel> myModels = listOfModelsCF.get(30, TimeUnit.SECONDS);
        assertTrue("Should be 4 models", myModels.size() == 4);
    }

    @Test
    public void testParallelGoodAndBadResponses() throws Exception {
        HttpClient httpClient = new HttpClient();
        final MyModel modelOne = new MyModelOne("dup", 2);
        final MyModel modelTwo = new MyModelTwo("dup", 2, 33.33);

        List<CompletableFuture<MyModel>> listOfCFs = new ArrayList<>();

        CompletableFuture<MyModel> myModelOneCF = httpClient.retrieve(MyModelOne.class).thenApply(myModelOne -> (MyModel)myModelOne);
        CompletableFuture<MyModel> myModelTwoCF = httpClient.retrieve(MyModelTwo.class).thenApply(myModelTwo -> (MyModel)myModelTwo);
        CompletableFuture<MyModel> myModelOneDupCF = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return modelOne;
        });
        CompletableFuture<MyModel> myModelTwoDupCF = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return modelTwo;
        });
        CompletableFuture<MyModel> badCF = httpClient.retrieve(MyModel.class);

        listOfCFs.add(myModelOneCF);
        listOfCFs.add(myModelTwoCF);
        listOfCFs.add(myModelOneDupCF);
        listOfCFs.add(myModelTwoDupCF);
        listOfCFs.add(badCF);

        CompletableFuture<List<MyModel>> listOfModelsCF = CompletableFuture.allOf(listOfCFs.toArray(new CompletableFuture[listOfCFs.size()]))
                .thenApply(aVoid -> listOfCFs.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        listOfModelsCF.whenComplete((myModels, throwable) -> {
            if (throwable != null) {
                System.out.println("Received throwable = " + throwable);
            } else {
                System.out.println("Received list of Models: " + myModels);
            }
        });

        try {
            List<MyModel> myModels = listOfModelsCF.get(30, TimeUnit.SECONDS);
            TestCase.fail("Should throw an exception");
        } catch (Exception e) {
            assertTrue("Should be HttpClientException", e.getCause() instanceof HttpClientException);
        }
    }

    @Test
    public void testParallelGoodAndBadResponseConvertedToGood() throws Exception {
        HttpClient httpClient = new HttpClient();
        final MyModel modelOne = new MyModelOne("dup", 2);
        final MyModel modelTwo = new MyModelTwo("dup", 2, 33.33);

        List<CompletableFuture<MyModel>> listOfCFs = new ArrayList<>();

        CompletableFuture<MyModel> myModelOneCF = httpClient.retrieve(MyModelOne.class).thenApply(myModelOne -> (MyModel)myModelOne);
        CompletableFuture<MyModel> myModelTwoCF = httpClient.retrieve(MyModelTwo.class).thenApply(myModelTwo -> (MyModel) myModelTwo);
        CompletableFuture<MyModel> myModelOneDupCF = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return modelOne;
        });
        CompletableFuture<MyModel> myModelTwoDupCF = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return modelTwo;
        });
        CompletableFuture<MyModel> badCF = httpClient.retrieve(MyModel.class);

        listOfCFs.add(myModelOneCF);
        listOfCFs.add(myModelTwoCF);
        listOfCFs.add(myModelOneDupCF);
        listOfCFs.add(myModelTwoDupCF);
        // transform bad future to return null
        listOfCFs.add(badCF.handle((myModel, throwable1) -> {
            if (throwable1 != null) return null;
            else return myModel;
        }));

        final CompletableFuture<MyModel> promise = new CompletableFuture<>();
        CompletableFuture<MyModel> badCF2 = httpClient.retrieve(MyModel.class);
        // configure bad future to complete 'another CompletableFuture' with null when it throws an exception
        badCF2.whenComplete((myModel, throwable1) -> {
            if (throwable1 != null) promise.complete(null);
            else promise.complete(myModel);
        });
        // add 'another CompletableFuture' to the list (note: badCF2 is not int the list!)
        listOfCFs.add(promise);


        CompletableFuture<List<MyModel>> listOfModelsCF = CompletableFuture.allOf(listOfCFs.toArray(new CompletableFuture[listOfCFs.size()]))
                .thenApply(aVoid -> listOfCFs.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        listOfModelsCF.whenComplete((myModels, throwable) -> {
            if (throwable != null) {
                System.out.println("Received throwable = " + throwable);
            } else {
                System.out.println("Received list of Models: " + myModels);
            }
        });

        List<MyModel> myModels = listOfModelsCF.get(30, TimeUnit.SECONDS);
        Assert.assertTrue(myModels.get(4) == null);
        Assert.assertTrue(myModels.get(5) == null);
    }

}
