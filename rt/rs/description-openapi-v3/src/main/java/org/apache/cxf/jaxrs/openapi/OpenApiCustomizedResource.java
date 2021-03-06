/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.jaxrs.openapi;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.jaxrs2.integration.ServletConfigContextUtils;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.integration.GenericOpenApiContext;
import io.swagger.v3.oas.integration.OpenApiContextLocator;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiContext;

public class OpenApiCustomizedResource extends OpenApiResource {

    private final OpenApiCustomizer customizer;

    public OpenApiCustomizedResource(final OpenApiCustomizer customizer) {
        this.customizer = customizer;
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON, "application/yaml" })
    @Operation(hidden = true)
    public Response getOpenApi(@Context ServletConfig config, @Context HttpHeaders headers,
            @Context UriInfo uriInfo, @PathParam("type") String type) throws Exception {

        if (customizer != null) {
            final OpenAPIConfiguration configuration = customizer.customize(getOpenApiConfiguration());
            setOpenApiConfiguration(configuration);

            // By default, the OpenApiContext instance is cached. It means that the configuration
            // changes won't be taken into account (due to the deep copying rather than reference 
            // passing). In order to reflect any changes which customization may do, we have to 
            // update reader's configuration directly.
            final String ctxId = ServletConfigContextUtils.getContextIdFromServletConfig(config);
            final OpenApiContext ctx = OpenApiContextLocator.getInstance().getOpenApiContext(ctxId);
            if (ctx instanceof GenericOpenApiContext<?>) {
                ((GenericOpenApiContext<?>) ctx).getOpenApiReader().setConfiguration(configuration);
                customizer.customize(ctx.read());
            }
        }

        return super.getOpenApi(headers, uriInfo, type);
    }
}
