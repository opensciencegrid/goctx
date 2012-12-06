
/**
 * Grid_AttachmentServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.4  Built on : Dec 19, 2010 (08:18:42 CET)
 */

    package edu.iu.grid.tx.soap.ggus;

    /**
     *  Grid_AttachmentServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class Grid_AttachmentServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public Grid_AttachmentServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public Grid_AttachmentServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for setTransferFlag method
            * override this method for handling normal response from setTransferFlag operation
            */
           public void receiveResultsetTransferFlag(
                    edu.iu.grid.tx.soap.ggus.Grid_AttachmentServiceStub.SetTransferFlagResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from setTransferFlag operation
           */
            public void receiveErrorsetTransferFlag(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for deleteAttachment method
            * override this method for handling normal response from deleteAttachment operation
            */
           public void receiveResultdeleteAttachment(
                    edu.iu.grid.tx.soap.ggus.Grid_AttachmentServiceStub.DeleteAttachmentResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from deleteAttachment operation
           */
            public void receiveErrordeleteAttachment(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getOneAttachment method
            * override this method for handling normal response from getOneAttachment operation
            */
           public void receiveResultgetOneAttachment(
                    edu.iu.grid.tx.soap.ggus.Grid_AttachmentServiceStub.GetOneAttachmentResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getOneAttachment operation
           */
            public void receiveErrorgetOneAttachment(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllAttachments method
            * override this method for handling normal response from getAllAttachments operation
            */
           public void receiveResultgetAllAttachments(
                    edu.iu.grid.tx.soap.ggus.Grid_AttachmentServiceStub.GetAllAttachmentsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllAttachments operation
           */
            public void receiveErrorgetAllAttachments(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addAttachment method
            * override this method for handling normal response from addAttachment operation
            */
           public void receiveResultaddAttachment(
                    edu.iu.grid.tx.soap.ggus.Grid_AttachmentServiceStub.AddAttachmentResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addAttachment operation
           */
            public void receiveErroraddAttachment(java.lang.Exception e) {
            }
                


    }
    