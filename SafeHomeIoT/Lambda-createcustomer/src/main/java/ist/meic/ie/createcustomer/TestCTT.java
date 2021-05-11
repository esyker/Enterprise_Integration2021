package ist.meic.ie.createcustomer;

import javax.xml.soap.*;
import java.io.IOException;

public class TestCTT {


    public static void main(String[] args) throws SOAPException, IOException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        String soapEndpointUrl = "http://localhost:8180/ctt";
        String soapAction = "http://www.webserviceX.NET/GetInfoByCity";

        String myNamespace = "ctt";
        String myNamespaceURI = "http://www.ctt.pt/pdcp";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("validatePostalCode", myNamespace);
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("arg0");
        soapBodyElem1.addTextNode("1000-001");
        soapMessage.saveChanges();
        soapMessage.writeTo(System.out);

        MimeHeaders headers = soapMessage.getMimeHeaders();

        try {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            // Send SOAP Message to SOAP Server
            SOAPMessage soapResponse = soapConnection.call(soapMessage, soapEndpointUrl);

            // Print the SOAP Response
            System.out.println("Response SOAP Message:");
            soapResponse.writeTo(System.out);
            System.out.println();

            soapConnection.close();
        } catch (Exception e) {
            System.err.println("\nError occurred while sending SOAP Request to Server!\nMake sure you have the correct endpoint URL and SOAPAction!\n");
            e.printStackTrace();
        }

    }
}
