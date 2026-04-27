package com.jangchwisa.userservice.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jangchwisa.userservice.common.exception.BusinessException;
import com.jangchwisa.userservice.common.exception.ErrorCode;
import com.jangchwisa.userservice.user.dto.InternalUserResponse;
import com.jangchwisa.userservice.user.service.UserService;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserInternalGrpcServiceTest {

    @Mock
    private UserService userService;

    private UserInternalGrpcService userInternalGrpcService;

    @Mock
    private StreamObserver<GetUserByIdResponse> responseObserver;

    @BeforeEach
    void setUp() {
        userInternalGrpcService = new UserInternalGrpcService(userService, new GrpcExceptionMapper());
    }

    @Test
    void getUserByIdReturnsGrpcResponse() {
        when(userService.getInternalUser(1L)).thenReturn(new InternalUserResponse(
                1L,
                "user01",
                "홍길동",
                "user@example.com",
                "ACTIVE",
                List.of("발달장애"),
                "사무직"
        ));

        userInternalGrpcService.getUserById(
                GetUserByIdRequest.newBuilder().setUserId(1L).build(),
                responseObserver
        );

        ArgumentCaptor<GetUserByIdResponse> responseCaptor = ArgumentCaptor.forClass(GetUserByIdResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        GetUserByIdResponse response = responseCaptor.getValue();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getLoginId()).isEqualTo("user01");
        assertThat(response.getDisabilitiesList()).containsExactly("발달장애");
    }

    @Test
    void getUserByIdMapsBusinessExceptionToGrpcStatus() {
        when(userService.getInternalUser(99L)).thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        userInternalGrpcService.getUserById(
                GetUserByIdRequest.newBuilder().setUserId(99L).build(),
                responseObserver
        );

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(responseObserver).onError(errorCaptor.capture());

        Throwable throwable = errorCaptor.getValue();
        assertThat(throwable).isInstanceOf(StatusException.class);
        assertThat(((StatusException) throwable).getStatus().getCode()).isEqualTo(Status.NOT_FOUND.getCode());
    }
}
