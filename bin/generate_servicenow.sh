
#which instance should I pull wsdl from? following is from Mike Baker

#  As far as fermidev and fermitrn, these are two separate development instances that we switch between.  
#  Currently we are using fermitrn, while fermidev has been upgraded to the latest release of ServiceNow 
#  in preparation for an upgrade in early May.  Apparently they have added a new field, which I will 
#  need to communicate to you when the time gets closer. If you ever need to pull the WSDL, feel free to 
#  reach out to me to ask which is “like-production”.

#Ping Mike B. to decide which wsdl to pull
url="https://fermi.service-now.com"
#url="https://fermitrn.service-now.com"
#url="https://fermidev.service-now.com"

wget --no-check-certificate -O servicenow.wsdl "$url/incident.do?WSDL"
wsdl2java.sh -ssi -or -uri servicenow.wsdl -p edu.iu.grid.tx.soap.servicenow -S ../src

wget --no-check-certificate -O servicenow_comments.wsdl "$url/sys_journal_field.do?WSDL"
wsdl2java.sh -ssi -or -uri servicenow_comments.wsdl -p edu.iu.grid.tx.soap.servicenow -S ../src

wget --no-check-certificate -O servicenow_attachments.wsdl "$url/sys_attachment.do?WSDL"
wsdl2java.sh -ssi -or -uri servicenow_attachments.wsdl -p edu.iu.grid.tx.soap.servicenow -S ../src

wget --no-check-certificate -O servicenow_attach.wsdl "$url/ecc_queue.do?WSDL"
wsdl2java.sh -ssi -or -uri servicenow_attach.wsdl -p edu.iu.grid.tx.soap.servicenow -S ../src

wget --no-check-certificate -O servicenow_user.wsdl "$url/sys_user.do?WSDL"
wsdl2java.sh -ssi -or -uri servicenow_user.wsdl -p edu.iu.grid.tx.soap.servicenow -S ../src
