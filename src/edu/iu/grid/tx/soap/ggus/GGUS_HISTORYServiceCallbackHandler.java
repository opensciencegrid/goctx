
/**
 * GGUS_HISTORYServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.4  Built on : Dec 19, 2010 (08:18:42 CET)
 */

    package edu.iu.grid.tx.soap.ggus;

    /**
     *  GGUS_HISTORYServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class GGUS_HISTORYServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public GGUS_HISTORYServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public GGUS_HISTORYServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for opGetTicketHist method
            * override this method for handling normal response from opGetTicketHist operation
            */
           public void receiveResultopGetTicketHist(
                    edu.iu.grid.tx.soap.ggus.GGUS_HISTORYServiceStub.OpGetTicketHistResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from opGetTicketHist operation
           */
            public void receiveErroropGetTicketHist(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for opGetOpsTicketHist method
            * override this method for handling normal response from opGetOpsTicketHist operation
            */
           public void receiveResultopGetOpsTicketHist(
                    edu.iu.grid.tx.soap.ggus.GGUS_HISTORYServiceStub.OpGetOpsTicketHistResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from opGetOpsTicketHist operation
           */
            public void receiveErroropGetOpsTicketHist(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for opGetTeamTicketHist method
            * override this method for handling normal response from opGetTeamTicketHist operation
            */
           public void receiveResultopGetTeamTicketHist(
                    edu.iu.grid.tx.soap.ggus.GGUS_HISTORYServiceStub.OpGetTeamTicketHistResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from opGetTeamTicketHist operation
           */
            public void receiveErroropGetTeamTicketHist(java.lang.Exception e) {
            }
                


    }
    