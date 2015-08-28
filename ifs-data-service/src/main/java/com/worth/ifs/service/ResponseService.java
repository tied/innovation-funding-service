package com.worth.ifs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worth.ifs.domain.Application;
import com.worth.ifs.domain.Response;
import com.worth.ifs.domain.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ApplicationService is a utility to use client-side to retrieve Application data from the data-service controllers.
 */

@Service
public class ResponseService extends BaseServiceProvider {
    @Value("${ifs.data.service.rest.response}")
    String responseRestURL;

    private final Log log = LogFactory.getLog(getClass());


    public List<Response> getResponsesByApplicationId(Long applicationId) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Response[]> responseEntity = restTemplate.getForEntity(dataRestServiceURL + responseRestURL + "/findResponsesByApplication/" + applicationId, Response[].class);
        Response[] responses = responseEntity.getBody();
        return Arrays.asList(responses);
    }

    public Boolean saveQuestionResponse(Long userId, Long applicationId, Long questionId, String value) {
        RestTemplate restTemplate = new RestTemplate();
        String url = dataRestServiceURL + responseRestURL + "/saveQuestionResponse/";

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("userId", userId);
        node.put("applicationId", applicationId);
        node.put("questionId", questionId);
        node.put("value", HtmlUtils.htmlEscape(value));


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(node.toString(), headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if (response.getStatusCode() == HttpStatus.ACCEPTED) {
            return true;
        } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            // nono... bad credentials
            log.info("Unauthorized.....");
            return false;
        }

        return false;
    }
    public Boolean markQuestionAsComplete(Long applicationId, Long questionId, Long userId, Boolean isComplete){
        RestTemplate restTemplate = new RestTemplate();
        String url = dataRestServiceURL + responseRestURL + "/markResponseAsComplete" +
                "?applicationId={applicationId}" +
                "&questionId={questionId}" +
                "&userId={userId}" +
                "&isComplete={isComplete}";


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        log.info("ApplicationService.markQuestionAsComplete send it!");
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, applicationId, questionId, userId, isComplete);

        if (response.getStatusCode() == HttpStatus.OK) {
            log.info("ApplicationService, marked as complete == ok : "+ response.getBody());
            return true;
        } else {
            log.warn("Call failed.....");
            log.error("failed with url "+ url);
        }
        return false;
    }
    public Boolean assignQuestion(Long applicationId, Long questionId, Long userId, Long assigneeId){
        RestTemplate restTemplate = new RestTemplate();
        String url = dataRestServiceURL + responseRestURL + "/assignQuestion" +
                "?applicationId={applicationId}" +
                "&questionId={questionId}" +
                "&userId={userId}" +
                "&assigneeId={assigneeId}";


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        log.info("ApplicationService.assignQuestion send it!");
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, applicationId, questionId, userId, assigneeId);

        if (response.getStatusCode() == HttpStatus.OK) {
            log.info("ApplicationService, assigned == ok : "+ response.getBody());
            return true;
        } else {
            log.warn("Call failed.....");
            log.error("failed with url "+ url);
        }
        return false;
    }
}
