package com.ridealongug.backend.api;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ridealongug.backend.services.WebActionsService;
import com.ridealongug.backend.utils.OperationReturnObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/")
@RequiredArgsConstructor
@Slf4j
public class WebActionsController {

    public final WebActionsService webActionsService;

    @Value("${app.version}")
    private String appVersion;

    @GetMapping
    public OperationReturnObject pingMe() {
        OperationReturnObject returnObject = new OperationReturnObject();
        returnObject.setCodeAndMessageAndReturnObject(0, "pong", appVersion);
        return returnObject;
    }

    @PostMapping
    public OperationReturnObject processServiceRequest(@RequestBody String requestBody) {
        log.info("Request:{}", requestBody);
        try {
            JSONObject jsonObject = JSON.parseObject(requestBody);
            if (!jsonObject.containsKey("SERVICE")) {
                throw new IllegalStateException("SERVICE UNDEFINED");
            } else if (!jsonObject.containsKey("ACTION")) {
                throw new IllegalStateException("ACTION UNDEFINED");
            } else {
                String service = jsonObject.getString("SERVICE").trim();
                String action = jsonObject.getString("ACTION").trim();
                return webActionsService.processAction(service, action, jsonObject);
            }
        } catch (Exception e) {
            OperationReturnObject responseWithError = new OperationReturnObject();
            responseWithError.setReturnCodeAndReturnMessage(500, e.getMessage());
            return responseWithError;
        }
    }
}
