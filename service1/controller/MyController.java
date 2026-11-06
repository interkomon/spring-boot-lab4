package ru.kovyazin.lab4.service1.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.kovyazin.lab4.service1.exception.ForwardingException;
import ru.kovyazin.lab4.service1.exception.UnsupportedCodeException;
import ru.kovyazin.lab4.service1.exception.ValidationFailedException;
import ru.kovyazin.lab4.service1.model.*;
import ru.kovyazin.lab4.service1.service.ForwardRequestService;
import ru.kovyazin.lab4.service1.service.ModifyRequestService;
import ru.kovyazin.lab4.service1.service.ModifyResponseService;
import ru.kovyazin.lab4.service1.service.ValidationService;
import ru.kovyazin.lab4.service1.util.DateTimeUtil;
import java.util.Date;
import static ru.kovyazin.lab4.service1.web.ArrivalTimeFilter.RECEIVED_AT;

@Slf4j
@RestController
public class MyController {
    private final ValidationService validationService;
    private final ModifyResponseService modifyResponseService;

    private final ModifyRequestService modifySystemNameRequestService;
    private final ModifyRequestService modifySourceRequestService;
    private final ForwardRequestService forwardRequestService;

    public MyController(ValidationService validationService,
            @Qualifier("ModifySystemTimeResponseService")
            ModifyResponseService modifyResponseService,
            @Qualifier("modifySystemNameRequestService")
            ModifyRequestService modifySystemNameRequestService,
            @Qualifier("modifySourceRequestService")
            ModifyRequestService modifySourceRequestService,
            ForwardRequestService forwardRequestService) {
        this.validationService = validationService;
        this.modifyResponseService = modifyResponseService;
        this.modifySystemNameRequestService = modifySystemNameRequestService;
        this.modifySourceRequestService = modifySourceRequestService;
        this.forwardRequestService = forwardRequestService;
    }

    @PostMapping(value = "/feedback", consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<Response> feedback(
            @Valid @RequestBody Request request, BindingResult result,
            @RequestAttribute(RECEIVED_AT) long receivedAt) {
        log.info("Получен request: {}", request);
        Response response = Response.builder()
                .uid(request.getUid() == null ? "" : request.getUid())
                .operationUid(request.getOperationUid() == null
                        ? "" : request.getOperationUid())
                .systemTime(DateTimeUtil.getCustomFormat().format(new Date()))
                .code(Codes.SUCCESS).errorCode(ErrorCodes.EMPTY)
                .errorMessage(ErrorMessages.EMPTY).build();
        log.info("Создан response: {}", response);
        try {
            if (request.getSystemName() == Systems.SERVICE_1) {
                result.rejectValue("systemName", "reserved", "Service 1 — служебное значение");
            }
            validationService.isValid(result);
            if ("123".equals(request.getUid())) {
                log.error("uid={} выбрасывается UnsupportedCodeException", request.getUid());
                throw new UnsupportedCodeException("Неподдерживаемый код uid: 123");
            }
            modifySystemNameRequestService.modify(request);
            modifySourceRequestService.modify(request);
            forwardRequestService.forward(request, receivedAt);
            response = modifyResponseService.modify(response);
            log.info("Отправлен response HTTP 200: {}", response);
            return ResponseEntity.ok(response);
        } catch (ValidationFailedException e) {
            return error(response, ErrorCodes.VALIDATION_EXCEPTION,
                    ErrorMessages.VALIDATION, 400);
        } catch (UnsupportedCodeException e) {
            return error(response, ErrorCodes.UNSUPPORTED_CODE_EXCEPTION,
                    ErrorMessages.UNSUPPORTED, 400);
        } catch (ForwardingException e) {
            log.error("uid={} ошибка пересылки: {}", request.getUid(), e.getCause().getMessage());
            return error(response, ErrorCodes.FORWARDING_EXCEPTION,
                    ErrorMessages.FORWARDING, 502);
        } catch (Exception e) {
            log.error("uid={} непредвиденная ошибка", request.getUid(), e);
            return error(response, ErrorCodes.UNKNOWN_EXCEPTION,
                    ErrorMessages.UNKNOWN, 500);
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
