package ru.kovyazin.lab4.service2.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.kovyazin.lab4.service2.exception.UnsupportedCodeException;
import ru.kovyazin.lab4.service2.exception.ValidationFailedException;
import ru.kovyazin.lab4.service2.model.*;
import ru.kovyazin.lab4.service2.service.ModifyResponseService;
import ru.kovyazin.lab4.service2.service.ValidationService;
import ru.kovyazin.lab4.service2.util.DateTimeUtil;
import java.util.Date;
import static ru.kovyazin.lab4.service2.web.ArrivalTimeFilter.*;

@Slf4j
@RestController
public class MyController {
    private final ValidationService validationService;
    private final ModifyResponseService modifyResponseService;

    public MyController(ValidationService validationService,
            @Qualifier("ModifySystemTimeResponseService")
            ModifyResponseService modifyResponseService) {
        this.validationService = validationService;
        this.modifyResponseService = modifyResponseService;
    }

    @PostMapping(value = "/feedback", consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<Response> feedback(
            @Valid @RequestBody Request request, BindingResult result,
            @RequestAttribute(name = RECEIVED_AT, required = false) Long receivedAt,
            @RequestHeader(name = SERVICE_1_HEADER, required = false) String sentFrom) {
        log.info("Получен request: {}", request);
        if (receivedAt != null && sentFrom != null) {
            logTransitTime(request.getUid(), receivedAt, sentFrom);
        }
        Response response = Response.builder()
                .uid(request.getUid() == null ? "" : request.getUid())
                .operationUid(request.getOperationUid() == null
                        ? "" : request.getOperationUid())
                .systemTime(DateTimeUtil.getCustomFormat().format(new Date()))
                .code(Codes.SUCCESS).errorCode(ErrorCodes.EMPTY)
                .errorMessage(ErrorMessages.EMPTY).build();
        log.info("Создан response: {}", response);
        try {
            validationService.isValid(result);
            if ("123".equals(request.getUid())) {
                log.error("uid={} выбрасывается UnsupportedCodeException", request.getUid());
                throw new UnsupportedCodeException("Неподдерживаемый код uid: 123");
            }
            response = modifyResponseService.modify(response);
            log.info("Отправлен response HTTP 200: {}", response);
            return ResponseEntity.ok(response);
        } catch (ValidationFailedException e) {
            return error(response, ErrorCodes.VALIDATION_EXCEPTION,
                    ErrorMessages.VALIDATION, 400);
        } catch (UnsupportedCodeException e) {
            return error(response, ErrorCodes.UNSUPPORTED_CODE_EXCEPTION,
                    ErrorMessages.UNSUPPORTED, 400);
        } catch (Exception e) {
            log.error("uid={} непредвиденная ошибка", request.getUid(), e);
            return error(response, ErrorCodes.UNKNOWN_EXCEPTION,
                    ErrorMessages.UNKNOWN, 500);
        }
    }

    private void logTransitTime(String uid, long receivedAt, String sentFrom) {
        try {
            long start = Long.parseLong(sentFrom);
            if (start > 0 && start <= receivedAt) {
                long elapsedMs = receivedAt - start;
                log.info("uid={} время передачи от Сервиса 1 до Сервиса 2: {} мс",
                        uid, elapsedMs);
            } else {
                log.warn("uid={} некорректная отметка времени Сервиса 1", uid);
            }
        } catch (NumberFormatException e) {
            log.warn("uid={} некорректный заголовок {}", uid, SERVICE_1_HEADER);
        }
    }

    private ResponseEntity<Response> error(Response response, ErrorCodes code,
            ErrorMessages message, int status) {
        response.setCode(Codes.FAILED);
        response.setErrorCode(code);
        response.setErrorMessage(message);
        log.info("Изменён и отправлен response HTTP {}: {}", status, response);
        return ResponseEntity.status(status).body(response);
    }
}
