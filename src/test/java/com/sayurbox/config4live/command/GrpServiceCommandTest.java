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
import org.powermock.reflect.Whitebox;

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

    @After
    public void after() {
        if (command != null) {
            command.close();
        }
    }

    @Test
    public void getConfig_Found() throws Exception {
        prepareMessageChannel(serverName, new LiveConfigurationGrpc.LiveConfigurationImplBase() {
            @Override
            public void findConfig(ConfigRequest request, StreamObserver<ConfigResponse> responseObserver) {
                ConfigResponse rs = ConfigResponse.newBuilder().setName("result").setValue("resultValue")
                        .setDescription("desc").build();
                responseObserver.onNext(rs);
                responseObserver.onCompleted();
            }
        });

        command = new GrpServiceCommand(serverName, "default_wh", provideHystrixParam());
        Whitebox.setInternalState(command, "channel", channel);
        Whitebox.setInternalState(command, "liveConfigStub", liveConfigStub);

        Config actual = command.run();
        Assert.assertNotNull(actual);
        Assert.assertEquals("resultValue", actual.getValue());
    }

    @Test
    public void getConfig_NotFound() throws Exception {
        prepareMessageChannel(serverName, new LiveConfigurationGrpc.LiveConfigurationImplBase() {
            @Override
            public void findConfig(ConfigRequest request, StreamObserver<ConfigResponse> responseObserver) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("not found").asRuntimeException());
            }
        });
        command = new GrpServiceCommand(serverName, "default_wh", provideHystrixParam());
        Whitebox.setInternalState(command, "channel", channel);
        Whitebox.setInternalState(command, "liveConfigStub", liveConfigStub);

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
        command = new GrpServiceCommand(serverName, "default_wh", hystrixParams);
        Whitebox.setInternalState(command, "channel", channel);
        Whitebox.setInternalState(command, "liveConfigStub", liveConfigStub);

        Config actual = command.run();
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
