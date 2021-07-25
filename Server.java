import java.net.*;
import java.io.*;
import java.nio.channels.*;
import java.util.zip.*;
/**
 * This Server will serve forever and will receive a file request from the client. If the file is found then the
 * server will send the file to the client. If the file is not found the FILE_NOT_FOUND flag is sent to Client.
 * 
 * @author Kegan Spence
 */
public class Server {
    // Status flags to send to Client
    public static final String READY = "R";
    public static final String FILE_NOT_FOUND = "F";

    private final int BUFF_SIZE = 1024; 
    private final String fileDirectory = "/Images/";
    protected String path;
    protected int port;
  
    /**
     * Purpose:
     * Server constructor to store the current port number and the path to the file directory.
     * 
     * @param port  Servers port number
     * @exception   SecurityException - Thrown by getProperty() if a security manager exists and its checkPropertyAccess method doesn't allow access to the specified system property.
     */
    public Server(int port) {
        try{
            this.path = System.getProperty("user.dir") + fileDirectory;
            this.port = port;
        } catch (SecurityException e){
            System.err.println(e);
            System.exit(-3);
        }
        
    }
    /**
     * Purpose:
     * If the requested file is found then the server will send the file to the client in buffers equal or less than the current
     * BUFF_SIZE and return true.
     * If the file is not found then the function will return false.
     * 
     * @param file          Name of the file being requested
     * @param out           A valid PrintWriter with a connection to the client
     * @param clientSocket  The current socket connection to the client
     * 
     * @exception   FileNotFoundException - Thrown by the FileInputStream if the requested file does not exists
     * @exception   IOException -  Thrown by the BufferedOutputStream if an I/O error occurs
     * 
     * @return      boolean - True if the file was found and sent successfully and false otherwise.
     */
    public boolean sendFile(String file, BufferedOutputStream outStream){
        try(      
            BufferedInputStream inFile = new BufferedInputStream(new FileInputStream(path + file), BUFF_SIZE);
            
            ){
            outStream.write(READY.getBytes(), 0 ,READY.length());   
            outStream.flush();            
            byte[] data = new byte[BUFF_SIZE];
            int bytesRead;
            while ((bytesRead = inFile.read(data)) != -1) {
                outStream.write(data, 0, bytesRead);
            }
            return true;
        }  catch (FileNotFoundException e){
            System.err.println(e);  
            return false;         
        }  catch (IOException e){
            System.err.println(e);
            return false;
        }
    }
    /**
     * Purpose:
     * Creates a new server socket and waits for a client to connect. Once a connection is established, the server
     * reads in the file name from the connection strips the '/' characters and calls sendFile.
     * If sendFile returns false the file was not found, the client is send the FILE_NOT_FOUND flag and the connection to the client is closed.
     * 
     * @exception   IOException - if an I/O error occurs while creating a socket or getting the output or input stream objects
     * @exception   SecurityException - if a security manager exists and its checkListen or checkAccept method's don't allow the operation.
     * @exception   IllegalArgumentException - if the port is out of range (must be between 0 and 65535 inclusive)
     * @exception   IllegalBlockingModeException -  while accepting a connection if this socket has an associated channel, the channel is in non-blocking mode, and there is no connection ready to be accepted
     * 
     * @return void
     */
    public void serve() {
        try(
            ServerSocket serverSocket = new ServerSocket(port);
        ){
            while (true){
                try(
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"), BUFF_SIZE);
                    BufferedOutputStream outStream = new BufferedOutputStream(clientSocket.getOutputStream());
                ){
                    String request = in.readLine();
                    String fileName = request.replaceAll("/","");
                    if (!sendFile(fileName, outStream)){
                        outStream.write(FILE_NOT_FOUND.getBytes(), 0 ,FILE_NOT_FOUND.length());
                        outStream.flush();
                    }
                } catch (IOException e){
                    System.err.println(e);
                } catch (SecurityException e){
                    System.err.println(e);
                } catch (IllegalArgumentException e){;
                    System.err.println(e);
                } catch (IllegalBlockingModeException e){
                    System.err.println(e);
                }
            }
        } catch (IOException e){
            System.out.println("Server socket could not be instantiated on port: " + port);
            System.err.println(e);
            System.exit(-1);
        } catch (SecurityException e){
            System.err.println(e);
            System.exit(-2);
        }

    }
    public static void main(String args[]){
        final int PORT = 12345;
        Server fileServer = new Server(PORT);
        fileServer.serve();
    }
}