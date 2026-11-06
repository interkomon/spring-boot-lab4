package ru.kovyazin.lab4.service1.service;

import ru.kovyazin.lab4.service1.model.Request;
import ru.kovyazin.lab4.service1.model.Systems;
import org.springframework.stereotype.Service;

@Service
public class ModifySystemNameRequestService implements ModifyRequestService {
    @Override
    public void modify(Request request) {
        request.setSystemName(Systems.SERVICE_1);
    }
}
