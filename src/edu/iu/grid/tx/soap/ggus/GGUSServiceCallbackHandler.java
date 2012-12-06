
/**
 * GGUSServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.4  Built on : Dec 19, 2010 (08:18:42 CET)
 */

    package edu.iu.grid.tx.soap.ggus;

    /**
     *  GGUSServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class GGUSServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public GGUSServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public GGUSServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for ticketGet method
            * override this method for handling normal response from ticketGet operation
            */
           public void receiveResultticketGet(
                    edu.iu.grid.tx.soap.ggus.GGUSServiceStub.TicketGetResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from ticketGet operation
           */
            public void receiveErrorticketGet(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for opCreateChild method
            * override this method for handling normal response from opCreateChild operation
            */
           public void receiveResultopCreateChild(
                    edu.iu.grid.tx.soap.ggus.GGUSServiceStub.OpCreateChildResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from opCreateChild operation
           */
            public void receiveErroropCreateChild(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for ticketModify method
            * override this method for handling normal response from ticketModify operation
            */
           public void receiveResultticketModify(
                    edu.iu.grid.tx.soap.ggus.GGUSServiceStub.TicketModifyResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from ticketModify operation
           */
            public void receiveErrorticketModify(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for opCreate method
            * override this method for handling normal response from opCreate operation
            */
           public void receiveResultopCreate(
                    edu.iu.grid.tx.soap.ggus.GGUSServiceStub.OpCreateResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from opCreate operation
           */
            public void receiveErroropCreate(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for ticketGetList method
            * override this method for handling normal response from ticketGetList operation
            */
           public void receiveResultticketGetList(
                    edu.iu.grid.tx.soap.ggus.GGUSServiceStub.TicketGetListResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from ticketGetList operation
           */
            public void receiveErrorticketGetList(java.lang.Exception e) {
            }
                


    }
    