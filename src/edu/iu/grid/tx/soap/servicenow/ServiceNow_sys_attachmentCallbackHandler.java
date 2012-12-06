
/**
 * ServiceNow_sys_attachmentCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.4  Built on : Dec 19, 2010 (08:18:42 CET)
 */

    package edu.iu.grid.tx.soap.servicenow;

    /**
     *  ServiceNow_sys_attachmentCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class ServiceNow_sys_attachmentCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public ServiceNow_sys_attachmentCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public ServiceNow_sys_attachmentCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for insert method
            * override this method for handling normal response from insert operation
            */
           public void receiveResultinsert(
                    edu.iu.grid.tx.soap.servicenow.ServiceNow_sys_attachmentStub.InsertResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from insert operation
           */
            public void receiveErrorinsert(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for deleteRecord method
            * override this method for handling normal response from deleteRecord operation
            */
           public void receiveResultdeleteRecord(
                    edu.iu.grid.tx.soap.servicenow.ServiceNow_sys_attachmentStub.DeleteRecordResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from deleteRecord operation
           */
            public void receiveErrordeleteRecord(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for update method
            * override this method for handling normal response from update operation
            */
           public void receiveResultupdate(
                    edu.iu.grid.tx.soap.servicenow.ServiceNow_sys_attachmentStub.UpdateResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from update operation
           */
            public void receiveErrorupdate(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for deleteMultiple method
            * override this method for handling normal response from deleteMultiple operation
            */
           public void receiveResultdeleteMultiple(
                    edu.iu.grid.tx.soap.servicenow.ServiceNow_sys_attachmentStub.DeleteMultipleResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from deleteMultiple operation
           */
            public void receiveErrordeleteMultiple(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getRecords method
            * override this method for handling normal response from getRecords operation
            */
           public void receiveResultgetRecords(
                    edu.iu.grid.tx.soap.servicenow.ServiceNow_sys_attachmentStub.GetRecordsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRecords operation
           */
            public void receiveErrorgetRecords(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getKeys method
            * override this method for handling normal response from getKeys operation
            */
           public void receiveResultgetKeys(
                    edu.iu.grid.tx.soap.servicenow.ServiceNow_sys_attachmentStub.GetKeysResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getKeys operation
           */
            public void receiveErrorgetKeys(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for get method
            * override this method for handling normal response from get operation
            */
           public void receiveResultget(
                    edu.iu.grid.tx.soap.servicenow.ServiceNow_sys_attachmentStub.GetResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from get operation
           */
            public void receiveErrorget(java.lang.Exception e) {
            }
                


    }
    