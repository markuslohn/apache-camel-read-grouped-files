package de.bimalo.integration.application.files;

import de.bimalo.integration.entity.RouteNameConstants;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public final class ReadGroupedFilesRoute extends RouteBuilder {

    public final static String ROUTE_ID = ReadGroupedFilesRoute.class.getName();
    public final static String ROUTE_URL = "ftp://{{ftp.hostname}}:{{ftp.port}}/input" +
            "?antInclude=*.xml" +
            "&passiveMode=true" +
            "&username={{ftp.username}}" +
            "&password={{ftp.password}}" +
            "&delete=true" +
            "&processStrategy=#GroupFilesProcessingStrategy";

     @Override
    public void configure() throws Exception {

         getCamelContext().setUseMDCLogging(true);
         getCamelContext().setUseBreadcrumb(true);

         bindToRegistry("GroupFilesProcessingStrategy", new GroupFilesProcessingStrategy());

        from(ROUTE_URL)
                .routeId(ROUTE_ID)
                .setHeader(Exchange.BREADCRUMB_ID,simple("${"+ RouteNameConstants.GROUPKEY_NAME_HEADER_NAME +"}"))
                .log(LoggingLevel.INFO, "Processing files with key = ${"+ RouteNameConstants.GROUPKEY_NAME_HEADER_NAME +"}")

                .log(LoggingLevel.INFO, "Found ${"+ RouteNameConstants.GROUPED_FILES_HEADER_NAME + ".size} files.")

                .loop(simple("${"+ RouteNameConstants.GROUPED_FILES_HEADER_NAME + ".size}"))
                    .log(LoggingLevel.INFO, "Download file ${"+ RouteNameConstants.GROUPED_FILES_HEADER_NAME + "[${exchangeProperty.CamelLoopIndex}]}...")

                    .pollEnrich()
                         .simple("ftp://{{ftp.hostname}}:{{ftp.port}}/input" +
                                 "?fileName=${"+ RouteNameConstants.GROUPED_FILES_HEADER_NAME +"[${exchangeProperty.CamelLoopIndex}]}" +
                                 "&passiveMode=true" +
                                 "&username={{ftp.username}}" +
                                 "&password={{ftp.password}}" +
                                 "&delete=true" +
                                 "&autoCreate=false" +
                                 "&bridgeErrorHandler=true" +
                                 "&throwExceptionOnConnectFailed=true"
                        ).timeout(0)

                    .choice()
                        .when(body().isNotNull())
                                .log(LoggingLevel.INFO, "Downloaded file ${"+ RouteNameConstants.GROUPED_FILES_HEADER_NAME + "[${exchangeProperty.CamelLoopIndex}]}.")
                        .otherwise()
                    .end()
                .end();

    }
}
