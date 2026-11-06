package ru.kovyazin.lab4.service1.service;

import ru.kovyazin.lab4.service1.exception.ForwardingException;
import ru.kovyazin.lab4.service1.model.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;

import static ru.kovyazin.lab4.service1.web.ArrivalTimeFilter.SERVICE_1_HEADER;

@Slf4j
@Service
public class ForwardRequestService {
    private final RestClient client;
    private final URI endpoint;

    public ForwardRequestService(RestClient.Builder builder,
            @Value("${lab4.service2.url}") URI endpoint) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.client = builder.requestFactory(factory).build();
        this.endpoint = endpoint;
    }

    public void forward(Request request, long receivedAt) {
        log.info("Пересылается request в Сервис 2 ({}): {}", endpoint, request);
        try {
            var result = client.post().uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(SERVICE_1_HEADER, Long.toString(receivedAt))
                    .body(request)
                    .retrieve().toBodilessEntity();
            if (!result.getStatusCode().is2xxSuccessful()) {
                throw new RestClientException("Сервис 2 вернул " + result.getStatusCode());
            }
            log.info("uid={} Сервис 2 принял request, HTTP {}",
                    request.getUid(), result.getStatusCode().value());
        } catch (RestClientException e) {
            throw new ForwardingException(e);
        }
    }
}
