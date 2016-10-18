package org.hildan.fxlog.config.builtin;

interface Description {

    interface Access {

        String CLIENT = "The IP or hostname of the client (remote host) which made the request to the server."
                + " If a proxy server exists between the user and the server, this address will be the address of the"
                + " proxy, rather than the originating machine.";

        String IDENTD = "The RFC 1413 identity of the client determined by identd on the clients machine. This "
                + "information is highly unreliable and should almost never be used except on tightly controlled "
                + "internal networks. A \"-\" in the output indicates that the requested piece of information is not "
                + "available.";

        String USERID = "The userid of the person requesting the document as determined by HTTP "
                + "authentication. The same value is typically provided to CGI scripts in the REMOTE_USER environment"
                + " variable. If the status code for the request (see below) is 401, then this value should not be "
                + "trusted because the user is not yet authenticated. If the document is not password protected, this"
                + " part will be \"-\" just like the \"identd\" column.";

        String DATE = "The time that the request was received.";

        String REQUEST = "The request line from the client.";

        String STATUS = "The status code that the server sends back to the client. This information is very "
                + "valuable, because it reveals whether the request resulted in a successful response (codes "
                + "beginning in 2), a redirection (codes beginning in 3), an error caused by the client (codes "
                + "beginning in 4), or an error in the server (codes beginning in 5).";

        String RSIZE = "The size of the object returned to the client, not including the response headers. "
                + "If no content was returned to the client, this value will be \"-\".";

        String REFERER = "The \"Referer\" (sic) HTTP request header. This gives the site that the client reports "
                + "having been referred from.";

        String USERAGENT = "The User-Agent HTTP request header. This is the identifying information that the client "
                + "browser reports about itself.";
    }

    interface Server {

        String DATE = "Time and date when the message originated, in a format that is specific to the "
                + "locale.";

        String SEVERITY = "Indicates the degree of impact or seriousness of the event reported by the message.";

        String SUBSYSTEM = "The subsystem of WebLogic Server that was the source of the message; for "
                + "example, Enterprise Java Bean (EJB) container or Java Messaging Service (JMS).";

        String MACHINE_NAME = "The DNS name of the computer that hosts the server instance";

        String SERVER_NAME = "The name of the WebLogic Server instance on which the message was generated.";

        String THREAD_ID = "The ID that the JVM assigns to the thread in which the message originated.";

        String USER_ID = "The user ID under which the associated event was executed.";

        String TRANSACTION = "Present only for messages logged within the context of a transaction.";

        String DIAGNOSTIC_CTX_ID = "Context information to correlate messages coming from a specific request or "
                + "application.";

        String TIMESTAMP = "The timestamp in milliseconds.";

        String MSG_ID = "A unique six-digit identifier. All message IDs that WebLogic Server system messages generate"
                + " start with BEA- and fall within a numerical range of 0-499999. Your applications can use a Java "
                + "class called NonCatalogLogger to generate log messages instead of using an internationalized "
                + "message catalog. The message ID for NonCatalogLogger messages is always 000000.";

        String CLASS = "The class in which the message originated.";

        String MSG = "A description of the event or condition.";

        String JSESSIONID = "The ID of the session in which the message originated.";
    }

}
