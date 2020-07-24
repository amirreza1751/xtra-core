package com.xtra.core.service;

import com.xtra.core.model.RequestOperation;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

@Service
public class CommunicationService {
    public boolean callMain(RequestMethod method, RequestOperation operation, Object data) {

        return true;
    }
}
