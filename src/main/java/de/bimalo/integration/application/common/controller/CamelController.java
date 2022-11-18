package de.bimalo.integration.application.common.controller;

import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.camel.ProducerTemplate;

@ApplicationScoped
public class CamelController {

  private ProducerTemplate producerTemplate;

  @Inject
  CamelController(ProducerTemplate producerTemplate) {
    this.producerTemplate = producerTemplate;
  }

  public void startCamelRouteSend(String camelRouteUri, Object body) throws Exception {
    producerTemplate.sendBody(camelRouteUri, body);
  }

  public void startCamelRouteSend(String camelRouteUri, Object body, Map<String, Object> headers)
      throws Exception {
    producerTemplate.sendBodyAndHeaders(camelRouteUri, body, headers);
  }

  public Object startCamelRouteRequest(String camelRouteUri, Object body) {
    return producerTemplate.requestBody(camelRouteUri, body);
  }

  public Object startCamelRouteRequest(
      String camelRouteUri, Object body, Map<String, Object> headers) {
    return producerTemplate.requestBodyAndHeaders(camelRouteUri, body, headers);
  }
}
