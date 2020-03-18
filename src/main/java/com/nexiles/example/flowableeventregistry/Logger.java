/*
 * Copyright (c) 2020 nexiles GmbH.  All rights reserved.
 */

package com.nexiles.example.flowableeventregistry;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

@Slf4j
@Component("Logger")
public class Logger {

    @SuppressWarnings("unused")
    public void log(DelegateExecution execution, String message) {
       log.info("LOG: Process {} activity {}: {}", execution.getProcessDefinitionId(), execution.getCurrentActivityId(), message);
       execution.getVariables().forEach( (name, value) -> log.debug("LOG: var {} := {}", name, value));
    }
}
