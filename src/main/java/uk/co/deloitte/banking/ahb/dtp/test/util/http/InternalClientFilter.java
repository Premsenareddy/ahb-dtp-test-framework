package uk.co.deloitte.banking.ahb.dtp.test.util.http;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Strings;
import org.reactivestreams.Publisher;
import org.slf4j.MDC;

import java.util.Optional;

/**
 * Adds the JWT token to the outgoing requests
 */
@Filter(value = "/**/internal/**")
@Slf4j
class InternalClientFilter implements HttpClientFilter {

    public static final String AUTHORIZATION = "Authorization";

    /**
     * @param targetRequest The target request
     * @param chain         The filter chain
     * @return The publisher of the response
     */
    @Override
    public Publisher<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> targetRequest, ClientFilterChain chain) {

        log.info("InternalClientFilter::doFilter...");
        final Optional<String> authorization = targetRequest.getHeaders().getAuthorization();
        if (!authorization.isPresent()) {

            final String jwt = MDC.get("JWT");
            if (!Strings.isNullOrEmpty(jwt)) {
                log.info("InternalClientFilter::doFilter:jwt[{}]", jwt);
                targetRequest.getHeaders().add(AUTHORIZATION, "Bearer " + jwt);
            } else {
                log.info("InternalClientFilter::doFilter:Authorization not present!");
            }

        } else {
            log.info("InternalClientFilter::doFilter:Authorization already present!");
        }

        log.info("InternalClientFilter::doFilter:proceed");
        return chain.proceed(targetRequest);
    }
}
