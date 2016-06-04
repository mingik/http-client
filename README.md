# http-client

Async standalone Http Client based on Play's WS library for Java. Returns java.util.concurrent.CompletableFuture that is:

* completed with domain class instance constructed from successful JSON response 
* completed exceptionally with HttpClientException, holding the message of underlying exception ("Connection refused" or 
"Unable to parse error")

See unit tests and #Server.java (for local testing).
