
/**
 * Grid_HistoryServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.4  Built on : Dec 19, 2010 (08:18:42 CET)
 */

    package edu.iu.grid.tx.soap.ggus;

    /**
     *  Grid_HistoryServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class Grid_HistoryServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public Grid_HistoryServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public Grid_HistoryServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for opGetHist method
            * override this method for handling normal response from opGetHist operation
            */
           public void receiveResultopGetHist(
                    edu.iu.grid.tx.soap.ggus.Grid_HistoryServiceStub.OpGetHistResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from opGetHist operation
           */
            public void receiveErroropGetHist(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for opGetTicketHist method
            * override this method for handling normal response from opGetTicketHist operation
            */
           public void receiveResultopGetTicketHist(
                    edu.iu.grid.tx.soap.ggus.Grid_HistoryServiceStub.OpGetTicketHistResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from opGetTicketHist operation
           */
            public void receiveErroropGetTicketHist(java.lang.Exception e) {
            }
                


    }
    