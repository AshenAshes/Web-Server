import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

class WebServer extends Thread{
    ServerSocket s;
    private static final int PORT = 3294;
    BufferedReader reader;

    public WebServer(){
        try{
            s = new ServerSocket(PORT);
            System.out.println("Listening...");
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try{
            while(true){
                Socket socket = s.accept();
                HttpServer httpServer = new HttpServer(socket);
                Thread thread = new Thread(httpServer);
                thread.start();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    class HttpServer implements Runnable{
        private static final String ROOTPATH = "C:/homework/CN/lab8";
        private static final String LOGIN = "3170103294";
        private static final String PASS  = "3294";

        private Socket socket;
        private InputStream in;
        private OutputStream out;
        private String filePath;
        private String loginString;
        private int flag; //GET:flag=0  POST:flag=1  Not-post:flag=2     

        HttpServer(Socket s){
            try{
                this.socket = s;
                in = socket.getInputStream();
                out = socket.getOutputStream();
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String readString = read();

            System.out.println("flag:"+flag);
            //GET
            if(flag == 0){
                filePath = readString;
                if(filePath.equals("/noimg.html"))
                    filePath = "/html/noimg.html";
                else if(filePath.equals("/test.html"))
                    filePath = "/html/test.html";
                else if(filePath.equals("/logo.jpg"))
                    filePath = "/img/logo.jpg";
                else if(filePath.equals("/test.txt"))
                    filePath = "/txt/test.txt";
                System.out.println("FilePath: "+ ROOTPATH + filePath);
                File file = new File(ROOTPATH + filePath);
                if(file.exists()){
                    //file found!
                    System.out.println("File found!");
                    byte[] buffer = new byte[1024];
                    FileInputStream fis = null;
                    try{
                        StringBuffer result = new StringBuffer();
                        result.append("HTTP/1.1 200 OK \r\n");
                        result.append("Content-Type: text/html;charset=UTF-8 \r\n");
                        result.append("Content-Length:"+ file.length() + "\r\n");
                        result.append("\r\n");
                        out.write(result.toString().getBytes());
                        
                        fis = new FileInputStream(file);
                        int readLength;
                        while((readLength = fis.read(buffer, 0, 1024)) > 0)
                            out.write(buffer, 0, readLength);
                        
                        out.flush();
                        out.close();
                        System.out.println("Answer end!");
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                else{
                    //file not found!
                    System.out.println("File not found!");
                    StringBuffer error = new StringBuffer();
                    error.append("HTTP/1.1 404 Not Found\r\n");
                    error.append("Content-Type: text/html\r\n");
                    error.append("Content-Length: 20\r\n").append("\r\n");
                    error.append("<h1>File Not Found..</h1>");
                    try{
                        out.write(error.toString().getBytes());
                        out.flush();
                        out.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
            //POST
            else if(flag == 1){
                loginString = readString;
                String login = loginString.substring(loginString.indexOf("=")+1, loginString.lastIndexOf("&"));
                String pass  = loginString.substring(loginString.lastIndexOf("=")+1);
                System.out.println("login:"+login);
                System.out.println("pass:"+pass);
                if(login.equals(LOGIN) && pass.equals(PASS)){
                    //login success
                    String hint = "<h1>Login success!<h1>";
                    StringBuffer success = new StringBuffer();
                    success.append("HTTP/1.1 200 OK \r\n");
                    success.append("Content-Type: text/html \r\n");
                    success.append("Content-Length: "+ hint.length() +"\r\n").append("\r\n");
                    success.append(hint);
                    try{
                        out.write(success.toString().getBytes());
                        out.flush();
                        out.close();
                        socket.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
                else{
                    //login fail
                    String hint = "<h1>Login fail.<h1>";
                    StringBuffer fail = new StringBuffer();
                    fail.append("HTTP/1.1 200 OK \r\n");
                    fail.append("Content-Type: text/html \r\n");
                    fail.append("Content-Length: "+ hint.length() +"\r\n").append("\r\n");
                    fail.append(hint);
                    try{
                        out.write(fail.toString().getBytes());
                        out.flush();
                        out.close();
                        socket.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
            else if(flag == 2){
                //404
                System.out.println("File not found!");
                StringBuffer error = new StringBuffer();
                error.append("HTTP/1.1 404 Not Found\r\n");
                error.append("Content-Type: text/html\r\n");
                error.append("Content-Length: 20\r\n").append("\r\n");
                error.append("<h1>Not Found..</h1>");
                try{
                    out.write(error.toString().getBytes());
                    out.flush();
                    out.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }

        public String read(){
            byte[] buffer = new byte[1024];
            int len = 0;
            try{
                len = in.read(buffer);
            }catch(IOException e){
                e.printStackTrace();
            }
            String content = new String(buffer, 0, len);
            String[] lines = content.split("\r\n");
        
            String readLine = lines[0];
            String [] split = readLine.split(" ");
            if(split.length != 3){
                return null;
            }
            System.out.println(readLine);

            if(split[0].equals("GET")){
                flag = 0;
                return split[1];
            }
            else if(split[0].equals("POST")){
                flag = 1;
                if(!split[1].equals("/dopost")){
                    flag = 2;
                    return null;
                }

                String line = lines[lines.length-1];
                System.out.println(line);
                
                //request content
                return line;
            }
            return null;
        }
    }
}

public class server{
    public static void main(String[] args) {
        WebServer server = new WebServer();
        server.start();
    }
}
