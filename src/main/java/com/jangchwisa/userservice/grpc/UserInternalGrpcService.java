package com.jangchwisa.userservice.grpc;

import com.jangchwisa.userservice.common.exception.BusinessException;
import com.jangchwisa.userservice.user.dto.InternalUserResponse;
import com.jangchwisa.userservice.user.service.UserService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserInternalGrpcService extends UserInternalServiceGrpc.UserInternalServiceImplBase {

    private final UserService userService;
    private final GrpcExceptionMapper grpcExceptionMapper;

    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<GetUserByIdResponse> responseObserver) {
        try {
            InternalUserResponse user = userService.getInternalUser(request.getUserId());
            GetUserByIdResponse response = GetUserByIdResponse.newBuilder()
                    .setUserId(user.userId())
                    .setLoginId(user.loginId())
                    .setName(user.name())
                    .setEmail(user.email())
                    .setAccountStatus(user.accountStatus())
                    .addAllDisabilities(user.disabilities())
                    .setDesiredJob(user.desiredJob() != null ? user.desiredJob() : "")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (BusinessException exception) {
            responseObserver.onError(grpcExceptionMapper.toStatusException(exception));
        } catch (Exception exception) {
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription("INTERNAL_SERVER_ERROR").asException());
        }
    }
}
