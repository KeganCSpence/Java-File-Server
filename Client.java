import java.io.*;
import java.net.*;
import java.util.zip.*;
/**
 * This Client connects to the server and sends the first commandline argument as a file request from the server.
 * The client then writes the received file data to a file in its current directory. If the file is not found on the server end then
 * the server will send a FILE_NOT_FOUND flag and the client will print an appropriate error message
 * .
 * @author  Kegan Spence
 */
public class Client {
    //Status flags sent by server
    public static final String READY = "R";
    public static final String FILE_NOT_FOUND = "F";

    protected final int BUFFER_SIZE = 1024;
    protected String serverName;
    protected int serverPort;
    protected String file;
    protected String path;
    /**
     * Purpose:
     * Client constructor to store the server's name and port, the requested file name and the path to the
     * current directory.
     * 
     * @param serverName    The servers ip address.
     * @param serverPort    The servers port
     * @param file          The file to be requested from the server server
     * @exception   SecurityException - Thrown by getProperty() if a security manager exists and its checkPropertyAccess method doesn't allow access to the specified system property.
     */
    public Client(String serverName, int serverPort, String file) {
        try{
            this.path = System.getProperty("user.dir") + '/';
            this.serverName = serverName;
            this.serverPort = serverPort;
            this.file = file;
        } catch (SecurityException e){
            System.err.println(e);
            System.exit(-3);
        }
    }
    /**
     * Purpose:
     * Reads in data over the current connection and writes it to a file. 
     * The 'file' is the name of the file requested from the server. 
     * 
     * @param inStream    A BufferedInputStream with a connection to the server.
     * @exception   IOException thrown by either the buffered output or input stream if an I/O error occurs.
     * @return void
     */
    public void getFile(BufferedInputStream inStream){
        try(    
            BufferedOutputStream outFile = new BufferedOutputStream(new FileOutputStream(path + file), BUFFER_SIZE);
        ){
            byte[] data = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inStream.read(data)) != -1){
                outFile.write(data , 0, bytesRead);
            }
        } catch (IOException e){
            System.err.println(e);
            System.exit(-2);
        }
    }

    /**
     * Purpose:
     * Creates initial connection to the server, a print writer containing the current socket 
     * connections output stream and a buffered input stream with the current socket connections input stream.
     * 
     * Notes:
     * The BufferedInputStream buffer size is set to the Client class's current BUFFER_SIZE
     * The file name sent to the server is UTF-8 encoded.
     * 
     * @exception   UnknownHostException        If the host ip address could not be determined
     * @exception   IOException                 If an I/O error occurs while creating the socket or getting the output or input stream and the connection is closed or unavailable.
     * @exception   SecurityException           If a security manager exists and the checkConnect method does not allow the operation while creating a socket connection 
     * @exception   IllegalArgumentException    If the port is out of range when creating the socket connection (must be between 0 and 65535 inclusive).
     * 
     * @return void
     */
    public void connect() {
        try(
            Socket socket = new Socket(serverName, serverPort);
            BufferedInputStream inStream = new BufferedInputStream(socket.getInputStream(), BUFFER_SIZE);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            ){
            out.println(file);
            byte[] bFlag = new byte[1];
            inStream.read(bFlag);
            String flag = new String(bFlag);
            if(flag.equals(READY)){
                getFile(inStream);
            } else if(flag.equals(FILE_NOT_FOUND)) {
                System.out.println("File " + file + " not found.");
            }
        } catch (UnknownHostException e){
            System.err.println(e);
            System.exit(-1);
        } catch (IOException e){
            System.err.println(e);
            System.exit(-2);
        } catch (SecurityException e){
            System.err.println(e);
            System.exit(-3);
        } catch (IllegalArgumentException e){
            System.err.println(e);
            System.exit(-4);
        }
    }
    
    public static void main(String[] args){
        final String SERVER_NAME = "10.21.75.6";
        final int PORT = 12345;
        if (args.length > 0){
            Client client = new Client(SERVER_NAME, PORT, args[0]);
            client.connect();
        }
        else{
            System.out.print("Must pass in file name argument when running file");
        }
    }
}

