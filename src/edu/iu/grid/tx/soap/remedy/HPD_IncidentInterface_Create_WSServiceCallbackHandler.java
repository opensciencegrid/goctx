
/**
 * HPD_IncidentInterface_Create_WSServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5  Built on : Apr 30, 2009 (06:07:24 EDT)
 */

    package edu.iu.grid.tx.soap.remedy;

    /**
     *  HPD_IncidentInterface_Create_WSServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class HPD_IncidentInterface_Create_WSServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public HPD_IncidentInterface_Create_WSServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public HPD_IncidentInterface_Create_WSServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for helpDesk_Submit_Service method
            * override this method for handling normal response from helpDesk_Submit_Service operation
            */
           public void receiveResulthelpDesk_Submit_Service(
                    edu.iu.grid.tx.soap.remedy.HPD_IncidentInterface_Create_WSServiceStub.HelpDesk_Submit_ServiceResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from helpDesk_Submit_Service operation
           */
            public void receiveErrorhelpDesk_Submit_Service(java.lang.Exception e) {
            }
                


    }
    