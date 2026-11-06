package ru.kovyazin.lab4.service2.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.kovyazin.lab4.service2.model.Response;
import ru.kovyazin.lab4.service2.util.DateTimeUtil;
import java.util.Date;

@Slf4j
@Service
@Qualifier("ModifySystemTimeResponseService")
public class ModifySystemTimeResponseService implements ModifyResponseService {
    @Override
    public Response modify(Response response) {
        String previous = response.getSystemTime();
        response.setSystemTime(DateTimeUtil.getCustomFormat().format(new Date()));
        log.info("uid={} response.systemTime: {} -> {}", response.getUid(),
                previous, response.getSystemTime());
        return response;
    }
}
