
/**
 * GGUS_ATTACHServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.4  Built on : Dec 19, 2010 (08:18:42 CET)
 */

    package edu.iu.grid.tx.soap.ggus;

    /**
     *  GGUS_ATTACHServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class GGUS_ATTACHServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public GGUS_ATTACHServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public GGUS_ATTACHServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getOneAttachment method
            * override this method for handling normal response from getOneAttachment operation
            */
           public void receiveResultgetOneAttachment(
                    edu.iu.grid.tx.soap.ggus.GGUS_ATTACHServiceStub.GetOneAttachmentResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getOneAttachment operation
           */
            public void receiveErrorgetOneAttachment(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAttachIDs method
            * override this method for handling normal response from getAttachIDs operation
            */
           public void receiveResultgetAttachIDs(
                    edu.iu.grid.tx.soap.ggus.GGUS_ATTACHServiceStub.GetAttachIDsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAttachIDs operation
           */
            public void receiveErrorgetAttachIDs(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addAttachment method
            * override this method for handling normal response from addAttachment operation
            */
           public void receiveResultaddAttachment(
                    edu.iu.grid.tx.soap.ggus.GGUS_ATTACHServiceStub.AddAttachmentResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addAttachment operation
           */
            public void receiveErroraddAttachment(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAttachments method
            * override this method for handling normal response from getAttachments operation
            */
           public void receiveResultgetAttachments(
                    edu.iu.grid.tx.soap.ggus.GGUS_ATTACHServiceStub.GetAttachmentsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAttachments operation
           */
            public void receiveErrorgetAttachments(java.lang.Exception e) {
            }
                


    }
    