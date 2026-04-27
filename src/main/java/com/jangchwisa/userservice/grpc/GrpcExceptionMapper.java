package com.jangchwisa.userservice.grpc;

import com.jangchwisa.userservice.common.exception.BusinessException;
import com.jangchwisa.userservice.common.exception.ErrorCode;
import io.grpc.Status;
import io.grpc.StatusException;
import org.springframework.stereotype.Component;

@Component
public class GrpcExceptionMapper {

    public StatusException toStatusException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return switch (errorCode) {
            case USER_NOT_FOUND -> Status.NOT_FOUND
                    .withDescription(errorCode.code() + ": " + errorCode.message())
                    .asException();
            case INVALID_REQUEST, DUPLICATED_EMAIL, DUPLICATED_LOGIN_ID -> Status.INVALID_ARGUMENT
                    .withDescription(errorCode.code() + ": " + errorCode.message())
                    .asException();
            default -> Status.FAILED_PRECONDITION
                    .withDescription(errorCode.code() + ": " + errorCode.message())
                    .asException();
        };
    }
}
