package com.dragons.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class HelloGrpcService extends HelloServiceGrpc.HelloServiceImplBase {

  @Override
  public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
    HelloResponse response = HelloResponse.newBuilder()
        .setMessage("Hello " + request.getName())
        .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
