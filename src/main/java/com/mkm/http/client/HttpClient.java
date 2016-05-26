package com.mkm.http.client;

import com.mkm.http.client.domain.MyModel;
import com.mkm.http.client.exception.HttpClientException;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletableFuture;

/**
 * Created by mintik on 5/25/16.
 */
public class HttpClient {
    private WSClient wsClient = WS.newClient(9000);
    private String url = "http://localhost:8080/";
    private String uri = "";

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String constructFullUrl(Class clazz) {
        return url + uri + "/" + clazz.getSimpleName();
    }

    public <T extends MyModel> CompletableFuture<T> retrieve(Class<T> clazz) {
        return wsClient.url(constructFullUrl(clazz))
                .get()
                .toCompletableFuture()
                .handle((wsResponse, throwable) -> {
                    if (throwable != null) throw new HttpClientException(throwable.getMessage());
                    try {
                        return Json.fromJson(wsResponse.asJson(), clazz);
                    } catch (Exception e) {
                        throw new HttpClientException(e.getMessage());
                    }
                });
    }
}
