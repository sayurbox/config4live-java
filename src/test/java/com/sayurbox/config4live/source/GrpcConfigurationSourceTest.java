package com.sayurbox.config4live.source;

import com.sayurbox.config4live.Config;
import com.sayurbox.shared.proto.consliveconfig.ConfigRequest;
import com.sayurbox.shared.proto.consliveconfig.ConfigResponse;
import com.sayurbox.shared.proto.consliveconfig.LiveConfigurationGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

import static org.mockito.AdditionalAnswers.delegatesTo;


public class GrpcConfigurationSourceTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
    private String serverName = "server.config:5555";
    private LiveConfigurationGrpc.LiveConfigurationBlockingStub liveConfigStub;

    @Before
    public void before() throws Exception {
        LiveConfigurationGrpc.LiveConfigurationImplBase serviceImpl =
                Mockito.mock(LiveConfigurationGrpc.LiveConfigurationImplBase.class,
                        delegatesTo(new LiveConfigurationGrpc.LiveConfigurationImplBase(){
                            @Override
                            public void findConfig(ConfigRequest request,
                                                   StreamObserver<ConfigResponse> responseObserver) {
                                if (request.getName().equals("active_config")) {
                                    ConfigResponse response = ConfigResponse.newBuilder().setName("active_config")
                                            .setValue("test value").build();
                                    responseObserver.onNext(response);
                                    responseObserver.onCompleted();
                                } else {
                                    responseObserver.onError(Status.NOT_FOUND.withDescription("not found")
                                            .asException());
                                }
                            }
                        }));
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(serviceImpl).build().start());
        ManagedChannel channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());
        liveConfigStub = LiveConfigurationGrpc.newBlockingStub(channel);
    }

    @Test
    public void getProperty_NotFound() {
        GrpcConfigurationSource src = new GrpcConfigurationSource.Builder()
                .withUrl(serverName)
                .withLoggerEnabled(false)
                .withExecutionTimeout(1000)
                .withCircuitBreakerEnabled(true)
                .withCircuitBreakerFailureVolumeThreshold(10)
                .withCircuitBreakerSlowResponseThreshold(1000)
                .withCircuitBreakerWaitDurationOpenState(300)
                .build();
        Whitebox.setInternalState(src, "liveConfigStub", liveConfigStub);
        Config actual = src.getProperty("unknown");
        Assert.assertNull(actual);
    }

    @Test
    public void getProperty_Found() {
        GrpcConfigurationSource src = new GrpcConfigurationSource.Builder()
                .withUrl(serverName)
                .withLoggerEnabled(false)
                .withExecutionTimeout(1000)
                .withCircuitBreakerEnabled(true)
                .withCircuitBreakerFailureVolumeThreshold(10)
                .withCircuitBreakerSlowResponseThreshold(1000)
                .withCircuitBreakerWaitDurationOpenState(300)
                .build();

        Whitebox.setInternalState(src, "liveConfigStub", liveConfigStub);
        Config actual = src.getProperty("active_config");
        Assert.assertNotNull(actual);
        Assert.assertEquals("test value", actual.getValue());
    }

}
