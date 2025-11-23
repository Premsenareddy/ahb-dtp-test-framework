package uk.co.deloitte.banking.ahb.dtp.test.proxy;


import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.client.ProxyHttpClient;
import io.micronaut.http.filter.OncePerRequestHttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;

import static uk.co.deloitte.banking.ahb.dtp.test.util.PhoneNumberUtils.FORBIDDEN_NUMBER;

@Filter("/service/**")
@Slf4j
public class ServiceProxyFilter extends OncePerRequestHttpServerFilter {

    public static final int PORT = 8080;



    private final ProxyHttpClient client;

    public ServiceProxyFilter(ProxyHttpClient client) {
        this.client = client;
    }

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(HttpRequest<?> request, ServerFilterChain chain) {


        final String path = request.getPath();

        //Check for forbidden number in path - unable to do body as micronaut does not expose it to filters
        if(path.contains(FORBIDDEN_NUMBER)){
            log.error("SEVERE:: FORBIDDEN_NUMBER number detected on path [{}]", path);
            return Publishers.just(
                    HttpResponse.status(HttpStatus.FORBIDDEN));
        }



        final String pathStripped = path.replace("/service/", "");
        log.info("ServiceProxyFilter::pathStripped" + pathStripped);

        final String[] split = pathStripped.split("/");

        log.info("ServiceProxyFilter:: split[" + split.length + "]");

        for (int i = 0; i < split.length; i++) {
            log.info("ServiceProxyFilter:: split[" + i + "]" + split[i]);
        }

        final String service = split[0];
        log.info("ServiceProxyFilter::service -> " + service);
        final String newPath = pathStripped.replace(service, "");

        log.info("ServiceProxyFilter::newPath " + newPath);

        final MutableHttpRequest<?> uri = request.mutate()
                .uri(b -> b
                        .replacePath(newPath)
                        .scheme("http")
                        .host(service)
                        .port(PORT)
                );


        log.info("ServiceProxyFilter::request::");
        uri.getHeaders().forEach(header -> {
            if (header.getKey().equalsIgnoreCase("if-none-match") || header.getKey().equalsIgnoreCase("if" +
                    "-modified-since") || header.getKey().equalsIgnoreCase("host")) {
                log.info("Remove Header [{}][{}]", header.getKey(), header.getValue());
                uri.getHeaders().remove(header.getKey());
            }
        });

        uri.getHeaders().forEach(header -> {
            log.info("Header [{}][{}]", header.getKey(), header.getValue());
        });


        log.info("ServiceProxyFilter::request:path: " + uri.getPath());
        log.info("ServiceProxyFilter::request:server: " + uri.getServerName());

        return Publishers
                .map(client.proxy(uri), response -> {
                    log.info("ServiceProxyFilter::Responding... [{}] status[{}]", uri.getPath(),
                            response.getStatus());
                    return response;
                });

    }
}
