package com.sayurbox.config4live.command;

import com.sayurbox.config4live.Config;
import com.sayurbox.config4live.ConfigRequest;
import com.sayurbox.config4live.ConfigResponse;
import com.sayurbox.config4live.LiveConfigurationGrpc;
import com.sayurbox.config4live.param.HystrixParams;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.*;

import java.io.IOException;

import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;

public class GrpServiceCommandTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private ManagedChannel channel;
    private String serverName;
    private GrpServiceCommand command;
    private LiveConfigurationGrpc.LiveConfigurationBlockingStub liveConfigStub;

    @Before
    public void before() {
        serverName = InProcessServerBuilder.generateName();
    }

    @Test
    public void getConfig_Found() throws Exception {
        prepareMessageChannel(serverName, new LiveConfigurationGrpc.LiveConfigurationImplBase() {
            @Override
            public void findConfig(ConfigRequest request, StreamObserver<ConfigResponse> responseObserver) {
                ConfigResponse rs = ConfigResponse.newBuilder().setName("result").setValue("resultValue")
                        .setFormat(ConfigResponse.Format.text).setDescription("desc").build();
                responseObserver.onNext(rs);
                responseObserver.onCompleted();
            }
        });

        command = new GrpServiceCommand(liveConfigStub, "default_wh", provideHystrixParam());
        Config actual = command.run();
        Assert.assertNotNull(actual);
        Assert.assertEquals("resultValue", actual.getValue());
        Assert.assertEquals("result", actual.getName());
        Assert.assertEquals("text", actual.getFormat().name());
    }

    @Test
    public void getConfig_NotFound() throws Exception {
        prepareMessageChannel(serverName, new LiveConfigurationGrpc.LiveConfigurationImplBase() {
            @Override
            public void findConfig(ConfigRequest request, StreamObserver<ConfigResponse> responseObserver) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("not found").asRuntimeException());
            }
        });
        command = new GrpServiceCommand(liveConfigStub, "default_wh", provideHystrixParam());
        Config actual = command.run();
        Assert.assertNull(actual);
    }

    @Test
    public void getConfig_internalServerErrorShouldReturnNull() throws Exception {
        prepareMessageChannel(serverName, new LiveConfigurationGrpc.LiveConfigurationImplBase() {
            @Override
            public void findConfig(ConfigRequest request, StreamObserver<ConfigResponse> responseObserver) {
                throw new RuntimeException("unknown exception");
            }
        });
        HystrixParams hystrixParams = new HystrixParams(1000, 400, 10, 500);
        command = new GrpServiceCommand(liveConfigStub, "default_wh", hystrixParams);
        Config actual = command.run();
        Assert.assertNull(actual);
    }

    @Test
    public void getConfig_TimeoutShouldFallback() throws Exception {
        prepareMessageChannel(serverName, new LiveConfigurationGrpc.LiveConfigurationImplBase() {
            @Override
            public void findConfig(ConfigRequest request, StreamObserver<ConfigResponse> responseObserver) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    responseObserver.onError(Status.UNKNOWN.withCause(e).asRuntimeException());
                }
                ConfigResponse rs = ConfigResponse.newBuilder().setName("result").setValue("resultValue")
                        .setFormat(ConfigResponse.Format.text).setDescription("desc").build();
                responseObserver.onNext(rs);
                responseObserver.onCompleted();
            }
        });

        HystrixParams hystrixParams = new HystrixParams(400, 400, 10, 500);
        command = new GrpServiceCommand(liveConfigStub, "default_wh", hystrixParams);
        Config actual = command.execute();
        Assert.assertNull(actual);
    }

    private void prepareMessageChannel(String serverName, LiveConfigurationGrpc.LiveConfigurationImplBase stub)
            throws IOException {
        LiveConfigurationGrpc.LiveConfigurationImplBase serviceImpl =
                mock(LiveConfigurationGrpc.LiveConfigurationImplBase.class, delegatesTo(stub));
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(serviceImpl).build().start());
        channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());
        liveConfigStub = LiveConfigurationGrpc.newBlockingStub(channel);
    }

    private HystrixParams provideHystrixParam() {
        return new HystrixParams(1000, 400, 10, 500);
    }
}
