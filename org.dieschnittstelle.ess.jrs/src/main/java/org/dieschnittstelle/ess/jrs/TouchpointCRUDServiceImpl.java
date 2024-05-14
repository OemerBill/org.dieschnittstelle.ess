package org.dieschnittstelle.ess.jrs;

import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;

import org.apache.logging.log4j.Logger;
import org.dieschnittstelle.ess.entities.crm.StationaryTouchpoint;
import org.dieschnittstelle.ess.entities.GenericCRUDExecutor;

public class TouchpointCRUDServiceImpl implements ITouchpointCRUDService {

    protected static Logger logger = org.apache.logging.log4j.LogManager.getLogger(TouchpointCRUDServiceImpl.class);

    /**
     * this accessor will be provided by the ServletContext, to which it is written by the TouchpointServletContextListener
     */
    private GenericCRUDExecutor<StationaryTouchpoint> touchpointCRUD;

    /**
     * here we will be passed the context parameters by the resteasy framework. Alternatively @Context
     * can  be declared on the respective instance attributes. note that the request context is only
     * declared for illustration purposes, but will not be further used here
     *
     * @param servletContext
     */
    public TouchpointCRUDServiceImpl(@Context ServletContext servletContext, @Context HttpServletRequest request) {
        logger.info("<constructor>: " + servletContext + "/" + request);
        // read out the dataAccessor
        this.touchpointCRUD = (GenericCRUDExecutor<StationaryTouchpoint>) servletContext.getAttribute("touchpointCRUD");

        logger.debug("read out the touchpointCRUD from the servlet context: " + this.touchpointCRUD);
    }


    @Override
    public List<StationaryTouchpoint> readAllTouchpoints() {
        return (List) this.touchpointCRUD.readAllObjects();
    }

    @Override
    public StationaryTouchpoint createTouchpoint(StationaryTouchpoint touchpoint) {
        return (StationaryTouchpoint) this.touchpointCRUD.createObject(touchpoint);
    }

    @Override
    public boolean deleteTouchpoint(long id) {
        return this.touchpointCRUD.deleteObject(id);
    }

    @Override
    public StationaryTouchpoint updateTouchpoint(long id, StationaryTouchpoint touchpoint) {
        return this.touchpointCRUD.updateObject(touchpoint);
    }

    @Override
    public StationaryTouchpoint readTouchpoint(long id) {
        StationaryTouchpoint tp = (StationaryTouchpoint) this.touchpointCRUD.readObject(id);

        // this shows how JAX-RS WebApplicationException can be used to return HTTP error status codes
        // NOTE, HOWEVER, THAT FOR THE JRS EXERCISES null needs to be turned in case of non existence in order for the jUnit testcases
        // to work properly
        if (tp != null) {
            return tp;
        } else {
            throw new NotFoundException("The touchpoint with id " + id + " does not exist!");
        }
    }

    /*
     * UE JRS1: implement the method for updating touchpoints
     */

}
