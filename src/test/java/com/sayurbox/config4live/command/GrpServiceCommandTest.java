package com.sayurbox.config4live.command;

import com.sayurbox.config4live.Config;
import com.sayurbox.config4live.ConfigRequest;
import com.sayurbox.config4live.ConfigResponse;
import com.sayurbox.config4live.LiveConfigurationGrpc;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

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
        CircuitBreaker circuitBreaker = provideCircuitBreaker(30);
        command = new GrpServiceCommand(liveConfigStub, "default_wh",
                circuitBreaker, true, 30_000);
        Config actual = command.execute();
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
        CircuitBreaker circuitBreaker = provideCircuitBreaker(30);
        command = new GrpServiceCommand(liveConfigStub, "default_wh",
                circuitBreaker, true, 30_000);
        Config actual = command.execute();
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
        CircuitBreaker circuitBreaker = provideCircuitBreaker(30);
        command = new GrpServiceCommand(liveConfigStub, "default_wh",
                circuitBreaker, true, 30_000);
        Config actual = command.execute();
        Assert.assertNull(actual);
    }

    @Test
    public void getConfig_TimeoutShouldFallback() throws Exception {
        prepareMessageChannel(serverName, new LiveConfigurationGrpc.LiveConfigurationImplBase() {
            @Override
            public void findConfig(ConfigRequest request, StreamObserver<ConfigResponse> responseObserver) {
                try {
                    Thread.sleep(2_000);
                } catch (InterruptedException e) {
                    responseObserver.onError(Status.UNKNOWN.withCause(e).asRuntimeException());
                }
                ConfigResponse rs = ConfigResponse.newBuilder().setName("result").setValue("resultValue")
                        .setFormat(ConfigResponse.Format.text).setDescription("desc").build();
                responseObserver.onNext(rs);
                responseObserver.onCompleted();
            }
        });

        CircuitBreaker circuitBreaker = provideCircuitBreaker(500);
        command = new GrpServiceCommand(liveConfigStub, "default_wh",
                circuitBreaker, true, 1_000);
        Config actual = command.execute();
        Assert.assertNull(actual);
    }

    @Test
    public void getConfig_CircuitOpen_AfterSlidingWindowThreshold() throws Exception {
        prepareMessageChannel(serverName, new LiveConfigurationGrpc.LiveConfigurationImplBase() {
            @Override
            public void findConfig(ConfigRequest request, StreamObserver<ConfigResponse> responseObserver) {
                try {
                    Thread.sleep(2_000);
                } catch (InterruptedException e) {
                    responseObserver.onError(Status.UNKNOWN.withCause(e).asRuntimeException());
                }
                ConfigResponse rs = ConfigResponse.newBuilder().setName("result").setValue("resultValue")
                        .setFormat(ConfigResponse.Format.text).setDescription("desc").build();
                responseObserver.onNext(rs);
                responseObserver.onCompleted();
            }
        });

        CircuitBreaker circuitBreaker = provideCircuitBreaker(500);
        command = new GrpServiceCommand(liveConfigStub, "default_wh",
                circuitBreaker, true, 1_000);
        Assert.assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
        Config actual = command.execute();
        Assert.assertNull(actual);
        Assert.assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        GrpServiceCommand command2nd = new GrpServiceCommand(liveConfigStub, "default_wh",
                circuitBreaker, true, 1_000);
        Config actual2nd = command2nd.execute();
        Assert.assertNull(actual2nd);
    }

    @Test
    public void getConfig_CircuitBreaker_disabled() throws Exception {
        prepareMessageChannel(serverName, new LiveConfigurationGrpc.LiveConfigurationImplBase() {
            @Override
            public void findConfig(ConfigRequest request, StreamObserver<ConfigResponse> responseObserver) {
                try {
                    Thread.sleep(2_000);
                } catch (InterruptedException e) {
                    responseObserver.onError(Status.UNKNOWN.withCause(e).asRuntimeException());
                }
                ConfigResponse rs = ConfigResponse.newBuilder().setName("result").setValue("resultValue")
                        .setFormat(ConfigResponse.Format.text).setDescription("desc").build();
                responseObserver.onNext(rs);
                responseObserver.onCompleted();
            }
        });

        CircuitBreaker circuitBreaker = provideCircuitBreaker(500);
        command = new GrpServiceCommand(liveConfigStub, "default_wh",
                circuitBreaker, false, 1_000);
        Assert.assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
        Config actual = command.execute();
        Assert.assertNull(actual);
        Assert.assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
        GrpServiceCommand command2nd = new GrpServiceCommand(liveConfigStub, "default_wh",
                circuitBreaker, true, 1_000);
        Config actual2nd = command2nd.execute();
        Assert.assertNull(actual2nd);
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

    private CircuitBreaker provideCircuitBreaker(long slowCallDuration) {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(1)
                .slowCallRateThreshold(70.0f)
                .slowCallDurationThreshold(Duration.ofMillis(slowCallDuration))
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .recordExceptions(IOException.class, TimeoutException.class,
                        StatusRuntimeException.class)
                .build();

        CircuitBreakerRegistry circuitBreakerRegistry =
                CircuitBreakerRegistry.of(circuitBreakerConfig);
        return circuitBreakerRegistry.circuitBreaker("test");
    }

}
