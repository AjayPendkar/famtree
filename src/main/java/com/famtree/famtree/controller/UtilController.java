package com.famtree.famtree.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.*;

@RestController
@Tag(name = "Utility", description = "Utility endpoints")
public class UtilController {

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @GetMapping("/endpoints")
    @Operation(summary = "List all API endpoints")
    public Map<String, List<String>> getEndpoints() {
        Map<String, List<String>> apiEndpoints = new TreeMap<>();
        
        Map<RequestMappingInfo, HandlerMethod> handlers = handlerMapping.getHandlerMethods();
        handlers.forEach((mapping, method) -> {
            String className = method.getBeanType().getSimpleName();
            String methodDetails = String.format("%s %s", 
                mapping.toString(), 
                method.getMethod().getName());
            
            apiEndpoints.computeIfAbsent(className, k -> new ArrayList<>())
                       .add(methodDetails);
        });
        
        // Sort the endpoints within each controller
        apiEndpoints.forEach((key, value) -> Collections.sort(value));
        
        return apiEndpoints;
    }
} 