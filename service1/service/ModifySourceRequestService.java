package ru.kovyazin.lab4.service1.service;

import ru.kovyazin.lab4.service1.model.Request;
import org.springframework.stereotype.Service;

@Service
public class ModifySourceRequestService implements ModifyRequestService {
    @Override
    public void modify(Request request) {
        request.setSource("service-1");
    }
}
