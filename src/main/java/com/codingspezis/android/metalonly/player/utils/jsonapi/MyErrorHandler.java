package com.codingspezis.android.metalonly.player.utils.jsonapi;

import org.androidannotations.annotations.*;
import org.androidannotations.api.rest.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.web.client.*;

@EBean
public class MyErrorHandler implements RestErrorHandler {

    public static final Logger LOGGER = LoggerFactory.getLogger(MyErrorHandler.class);

    @Override
    public void onRestClientExceptionThrown(NestedRuntimeException e) {
        LOGGER.error(e.getMessage(), e);
    }
}
