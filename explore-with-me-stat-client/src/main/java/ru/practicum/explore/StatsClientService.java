package ru.practicum.explore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.explore.dto.EndpointHitDto;
import ru.practicum.explore.dto.EndpointViewDto;

import java.util.Arrays;
import java.util.List;


@Slf4j
@Service
public class StatsClientService {

    private static final String HIT_URL = "/hit";
    private static final String STATS_URL = "/stats";
    private static final String ERROR_MESSAGE = "Ошибка отправки запроса на endpoint %s для объекта %s. Причина %s";
    private final RestTemplate rest;

    @Autowired
    public StatsClientService(RestTemplateBuilder builder, Environment env) {
        rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(env.getProperty("explore-stat.url")))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public EndpointHitDto hit(EndpointHitDto endpointHitDto) {

        HttpEntity<EndpointHitDto> request = new HttpEntity<>(endpointHitDto, defaultHeaders());
        ResponseEntity<EndpointHitDto> response;

        try {
            response = rest.exchange(HIT_URL, HttpMethod.POST, request, EndpointHitDto.class);
        } catch (RestClientException exception) {
            log.error(String.format(ERROR_MESSAGE, HIT_URL, endpointHitDto, exception.getMessage()), exception);
            throw exception;
        }

        return response.getBody();
    }

    public List<EndpointViewDto> getEndpointStatsByParams(String query) {

        HttpEntity<Object> request = new HttpEntity<>(null, defaultHeaders());
        ResponseEntity<EndpointViewDto[]> response;

        final String resultUrl;

        if (query.isBlank()) {
            resultUrl = STATS_URL;
        } else {
            resultUrl = STATS_URL + query;
        }

        try {
            response = rest.exchange(resultUrl, HttpMethod.GET, request, EndpointViewDto[].class);
        } catch (RestClientException exception) {
            log.error(String.format(ERROR_MESSAGE, resultUrl, query, exception.getMessage()), exception);
            throw exception;
        }

        return response != null ? Arrays.asList(response.getBody()) : null;
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
