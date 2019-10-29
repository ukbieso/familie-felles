package no.nav.familie.http.azure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AzureAccessTokenException extends RuntimeException {

    private static final Logger logger = LoggerFactory.getLogger(AzureAccessTokenException.class);

    AzureAccessTokenException(String message, Throwable cause) {
        super(message, cause);

        logger.error(message, cause);
    }

    AzureAccessTokenException(String message) {
        super(message);

        logger.error(message);
    }
}
