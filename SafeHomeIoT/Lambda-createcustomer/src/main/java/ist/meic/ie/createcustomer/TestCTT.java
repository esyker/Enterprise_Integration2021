package ist.meic.ie.createcustomer;

import javax.xml.soap.*;
import java.io.IOException;

public class TestCTT {


    public static void main(String[] args) throws SOAPException, IOException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        String myNamespace = "ctt";
        String myNamespaceURI = "http://www.ctt.pt/pdcp";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("validatePostalCode", myNamespace);
        //SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("USCity", myNamespace);
        soapBodyElem.addTextNode("2435-477");
        soapMessage.saveChanges();
        soapMessage.writeTo(System.out);

    }
}
