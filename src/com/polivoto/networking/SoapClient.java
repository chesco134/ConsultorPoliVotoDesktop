/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.polivoto.networking;

import java.io.IOException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import org.json.JSONObject;

/**
 *
 * @author jcapiz
 */
public class SoapClient {
    /** El presente servicio está pensado para interactuar con un servidor remoto **/
    
    private String host;
    private final JSONObject json;
    
    public SoapClient(JSONObject json) {
        this.json = json;
    }
    
    public String main() throws SOAPException, IOException {
    	String resp = "NaN";
        // Create SOAP Connection
        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection soapConnection = soapConnectionFactory.createConnection();

        // Send SOAP Message to SOAP Server
        String url = "http://" + host + "/FistVotingServiceBank/services/ServAvailableVoteProcesses.ServAvailableVoteProcessesHttpSoap11Endpoint/";
        SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(), url);

        // print SOAP Response
        System.out.print("Response SOAP Message:");
        soapResponse.writeTo(System.out);
        SOAPBody body = soapResponse.getSOAPBody();
        java.util.Iterator updates = body.getChildElements();
        while (updates.hasNext()) {
            System.out.println();
            // The listing and its ID
            SOAPElement update = (SOAPElement)updates.next();
            java.util.Iterator i = update.getChildElements();
            while( i.hasNext() ){
                SOAPElement e = (SOAPElement)i.next();
                String name = e.getLocalName();
                String value = e.getValue();
                resp = value; // Am I getting last response?
                System.out.println( name + ": " + value);
            }
        }
        soapConnection.close();
        return resp;
    }

    private SOAPMessage createSOAPRequest() throws SOAPException, IOException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String serverURI = "http://votingservice.develops.capiz.org";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("example", serverURI);

        /*
        Constructed SOAP Request Message:
        <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:example="http://ws.cdyne.com/">
            <SOAP-ENV:Header/>
            <SOAP-ENV:Body>
                <example:VerifyEmail>
                    <example:email>mutantninja@gmail.com</example:email>
                    <example:LicenseKey>123</example:LicenseKey>
                </example:VerifyEmail>
            </SOAP-ENV:Body>
        </SOAP-ENV:Envelope>
         */

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("serviceChooser", "example");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("json", "example");
        soapBodyElem1.addTextNode(json.toString());
//        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("LicenseKey", "example");
//        soapBodyElem2.addTextNode("123");

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "serviceChooser");

        soapMessage.saveChanges();

        /* Print the request message */
        System.out.print("Request SOAP Message:");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }

    public void setHost(String host) {
        this.host = host;
    }

}