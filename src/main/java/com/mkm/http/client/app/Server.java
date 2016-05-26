package com.mkm.http.client.app;

import com.mkm.http.client.domain.MyModel;
import com.mkm.http.client.domain.MyModelOne;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import play.libs.Json;


import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.Values;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

/**
 * Created by mintik on 5/25/16.
 */
public class Server {

    public static void main(String[] args) throws Exception {
        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel> () {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();
                            p.addLast(new HttpServerCodec());
                            p.addLast(new JsonServerHandler());
                        }
                    });

            Channel ch = b.bind(8080).sync().channel();

            System.err.println("Open your web browser and navigate to " +
                    "http://127.0.0.1:8080/");

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

class JsonServerHandler extends ChannelInboundHandlerAdapter { // (1)
    private MyModel myModel = new MyModelOne("one", 1);
    private String jsonPayload = Json.stringify(Json.toJson(myModel));

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;

            if (req.getUri().contains("notFound")) {
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND, Unpooled.copiedBuffer("Failure: " + NOT_FOUND + "\r\n", CharsetUtil.UTF_8));
                response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
                response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            } else {

                if (HttpHeaders.is100ContinueExpected(req)) {
                    ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
                }

                boolean keepAlive = HttpHeaders.isKeepAlive(req);
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(jsonPayload.getBytes()));
                response.headers().set(CONTENT_TYPE, "application/json");
                response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

                if (!keepAlive) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                    ctx.write(response);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
