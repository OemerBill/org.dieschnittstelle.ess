package org.dieschnittstelle.ess.wsv.interpreter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

import jakarta.ws.rs.*;
import org.apache.http.client.methods.*;
import org.apache.logging.log4j.Logger;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ByteArrayEntity;

import org.dieschnittstelle.ess.utils.Http;
import org.dieschnittstelle.ess.wsv.interpreter.json.JSONObjectSerialiser;

import static org.dieschnittstelle.ess.utils.Utils.show;

/*
 * TODO WSV1: implement this class such that the crud operations declared on ITouchpointCRUDService in .ess.wsv can be successfully called from the class AccessRESTServiceWithInterpreter in the .esa.wsv.client project
 */
public class JAXRSClientInterpreter implements InvocationHandler {

    // use a logger
    protected static Logger logger = org.apache.logging.log4j.LogManager.getLogger(JAXRSClientInterpreter.class);

    // declare a baseurl
    private String baseurl;

    // declare a common path segment
    private String commonPath;

    // use our own implementation JSONObjectSerialiser
    private JSONObjectSerialiser jsonSerialiser = new JSONObjectSerialiser();

    // use an attribute that holds the serviceInterface (useful, e.g. for providing a toString() method)
    private Class serviceInterface;

    // use a constructor that takes an annotated service interface and a baseurl. the implementation should read out the path annotation, we assume we produce and consume json, i.e. the @Produces and @Consumes annotations will not be considered here
    public JAXRSClientInterpreter(Class serviceInterface,String baseurl) {

        // TODO: implement the constructor!
        this.baseurl = baseurl;
        this.serviceInterface = serviceInterface;

        if(serviceInterface.isAnnotationPresent(Path.class)) {
            Path commonPathAnnotation = (Path) serviceInterface.getAnnotation(Path.class);
            this.commonPath = commonPathAnnotation.value();
        }

        logger.info("<constructor>: " + serviceInterface + " / " + baseurl + " / " + commonPath);
    }

    // TODO: implement this method interpreting jax-rs annotations on the meth argument
    @Override
    public Object invoke(Object proxy, Method meth, Object[] args)
            throws Throwable {

        // TODO check whether we handle the toString method and give some appropriate return value
        if("toString".equals(meth.getName())) {
            return "Proxy Object for accessing WebAPI resource described by: " + serviceInterface;
        }

        // use a default http client
        HttpClient client = Http.createSyncClient();

        // TODO: create the requestUrl using baseurl and commonpath (further segments may be added if the method has an own @Path annotation)
        String requestUrl = baseurl + commonPath;

        // TODO: check whether we have a path annotation and append the requestUrl (path params will be handled when looking at the method arguments)
        if (meth.isAnnotationPresent(Path.class)) {
            Path pathAnnotation = meth.getAnnotation(Path.class);
            requestUrl += pathAnnotation.value();
        }

        // a value that needs to be sent via the http request body
        Object requestBodyData = null;

        // TODO: check whether we have method arguments - only consider pathparam annotations (if any) on the first argument here - if no args are passed, the value of args is null! if no pathparam annotation is present assume that the argument value is passed via the body of the http request
        if (args != null && args.length > 0) {
            if (meth.getParameterAnnotations()[0].length > 0 && meth.getParameterAnnotations()[0][0].annotationType() == PathParam.class) {
                // TODO: handle PathParam on the first argument - do not forget that in this case we might have a second argument providing a requestBodyData
                Annotation[][] annotations = meth.getParameterAnnotations();
                PathParam pathParam = null;
                for (int i = 0; i < annotations.length; i++) {
                    for (int j = 0; j < annotations[i].length; j++) {
                        pathParam = (PathParam) annotations[i][j];

                // TODO: if we have a path param, we need to replace the corresponding pattern in the requestUrl with the parameter value
                        requestUrl = requestUrl.replace("{" + pathParam.value() + "}", args[j].toString());
                    }
                }
                if (args.length > 1) {
                    requestBodyData = args[1];
                }
            }
            else {
                // if we do not have a path param, we assume the argument value will be sent via the body of the request
                requestBodyData = args[0];
            }
        }

        // declare a HttpUriRequest variable
        HttpUriRequest request = null;

        // TODO: check which of the http method annotation is present and instantiate request accordingly passing the requestUrl
        if (meth.isAnnotationPresent(GET.class)){
            request = new HttpGet(requestUrl);
        } else if (meth.isAnnotationPresent(POST.class)){
            request = new HttpPost(requestUrl);
        } else if (meth.isAnnotationPresent(PUT.class)){
            request = new HttpPut(requestUrl);
        } else if (meth.isAnnotationPresent(DELETE.class)){
            request = new HttpDelete(requestUrl);
        } else if (meth.isAnnotationPresent(HEAD.class)) {
            request = new HttpHead(requestUrl);
        } else {
            throw new UnsupportedOperationException("Method not implemented for " + meth.getName());
        }

        // TODO: add a header on the request declaring that we accept json (for header names, you can use the constants declared in jakarta.ws.rs.core.HttpHeaders, for content types use the constants from jakarta.ws.rs.core.MediaType;)
        request.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        // if we need to send the method argument in the request body we need to declare an entity
        ByteArrayEntity requestBodyDataAsJson = null;

        // if a body shall be sent, convert the requestBodyData to json, create an entity from it and set it on the request
        if (requestBodyData != null) {

            // TODO: use a ByteArrayOutputStream for writing json
            ByteArrayOutputStream requestBodyDataContainer = new ByteArrayOutputStream();

            // TODO: write the object to the stream using the jsonSerialiser
            jsonSerialiser.writeObject(requestBodyData, requestBodyDataContainer);

            // TODO: create an ByteArrayEntity from the stream's content, assiging it to requestBodyDataAsJson
            ByteArrayEntity requestBody = new ByteArrayEntity(requestBodyDataContainer.toByteArray());

            // TODO: set the entity on the request, which must be cast to HttpEntityEnclosingRequest
            ((HttpEntityEnclosingRequest) request).setEntity(requestBody);

            // TODO: and add a content type header for the request
            request.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        }

        logger.info("invoke(): executing request: " + request);

        // then send the request to the server and get the response
        HttpResponse response = client.execute(request);

        logger.info("invoke(): received response: " + response);

        // check the response code
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

            // declare a variable for the return value
            Object returnValue = null;

            // TODO: convert the resonse body to a java object of an appropriate type considering the return type of the method as returned by getGenericReturnType() and set the object as value of returnValue
            Type returnType = meth.getGenericReturnType();
            InputStream dataFromResponseBody = response.getEntity().getContent();
            returnValue = jsonSerialiser.readObject(dataFromResponseBody, returnType);

            // and return the return value
            logger.info("invoke(): returning value: " + returnValue);
            return returnValue;

        }
        else {
            throw new RuntimeException("Got unexpected status from server: " + response.getStatusLine());
        }
    }

}