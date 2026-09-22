const isLocalhost =
    window.location.hostname === "localhost" ||
    window.location.hostname === "127.0.0.1";

const usingIntelliJStaticServer =
    isLocalhost && window.location.port === "63342";

window.APP_CONFIG = {
    BOOKING_API:
        usingIntelliJStaticServer
            ? "http://localhost:8080"
            : window.location.origin,

    CUSTOMER_API:
        isLocalhost
            ? "http://localhost:8081"
            : "https://REPLACE_WITH_CUSTOMER_SERVICE_RAILWAY_URL"
};