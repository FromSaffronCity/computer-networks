package threads;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class ServerThread implements Runnable {
    private Socket socket;
    private String root;
    private String log;
    private int server_port;
    private static int request_no = 0;
    private Thread thread;

    public ServerThread(Socket socket, String root, String log, int server_port) {
        this.socket = socket;
        this.root = root;
        this.log = log;
        this.server_port = server_port;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        /* instantiating bufferedReader */
        BufferedReader bufferedReader = null;
        String httpRequest = null;

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch(IOException e) {
            e.printStackTrace();
        }

        /* receiving request message from client */
        try {
            httpRequest = bufferedReader.readLine();
        } catch(IOException e) {
            e.printStackTrace();
        }

        /* starting main process (checking whether GET or UPLOAD request) */
        if(httpRequest==null || httpRequest.startsWith("GET")) {
            /* processing http request line from client web browser */

            /* terminating process if no http request line is sent */
            if(httpRequest == null) {
                try {
                    bufferedReader.close();
                    socket.close();
                } catch(IOException e) {
                    e.printStackTrace();
                } finally {
                    return ;
                }
            }

            /* instantiating printWriter and fileWriter */
            PrintWriter printWriter=null, fileWriter=null;

            try {
                printWriter = new PrintWriter(socket.getOutputStream());
                /* NOTICE: static variable request_no used */
                fileWriter = new PrintWriter(log+"\\http_log_"+(++request_no)+".log");
            } catch(IOException e) {
                e.printStackTrace();
            }

            /* NOTICE: notifications on console */
            System.out.println(">> http request line from client: "+httpRequest);
            fileWriter.println("HTTP REQUEST LINE FROM CLIENT:\n"+httpRequest+"\n");

            /* *** a valid http request line is received, now processing http response *** */
            String path="";
            String[] array = httpRequest.split("/");
            File fileContent;
            File[] listOfContent;

            /* extracting path from uri of http request line */
            for(int i=1; i<array.length-1; i++) {
                if(i == (array.length-2)) {
                    path += array[i].replace(" HTTP","");
                } else {
                    path += array[i]+"\\";
                }
            }

            /* creating File object */
            if(path.equals("")) {
                fileContent = new File(root);
            } else {
                /* NOTICE: to allow file or directory name with space */
                path = path.replace("%20", " ")+"\\";
                fileContent = new File(root+"\\"+path);
            }

            /* preparing response body content */
            StringBuilder stringBuilder = new StringBuilder();
            if(fileContent.exists()) {
                /* requested content exists in current directory */
                if(fileContent.isDirectory()) {
                    listOfContent = fileContent.listFiles();
                    /* NOTICE: <link> for avoiding favicon */
                    stringBuilder.append("<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n<link rel=\"icon\" href=\"data:,\">\n</head>\n<body>\n");

                    for(int i=0; i<listOfContent.length; i++) {
                        /* NOTICE: hyperlink */
                        if(listOfContent[i].isDirectory()) {
                            stringBuilder.append("<font size=\"7\"><b><a href=\"http://localhost:"+server_port+"/"+path.replace("\\", "/")+listOfContent[i].getName()+"\"> "+listOfContent[i].getName()+" </a></b></font><br>\n");
                        }
                        if(listOfContent[i].isFile()) {
                            stringBuilder.append("<font size=\"7\"><a href=\"http://localhost:"+server_port+"/"+path.replace("\\", "/")+listOfContent[i].getName()+"\"> "+listOfContent[i].getName()+" </a></font><br>\n");
                        }
                    }
                    stringBuilder.append("</body>\n</html>");
                }
            } else {
                /* requested content does not exist in current directory */
                stringBuilder.append("<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n<link rel=\"icon\" href=\"data:,\">\n</head>\n<body>\n");
                stringBuilder.append("<h1> 404: Page not found </h1>\n");
                stringBuilder.append("</body>\n</html>");
            }

            /* sending http response line, headers, and body */
            fileWriter.println("HTTP RESPONSE TO CLIENT:");
            String httpResponse = "";

            if(httpRequest.length() > 0) {
                if(httpRequest.startsWith("GET")) {
                    if(fileContent.exists() && fileContent.isDirectory()) {
                        httpResponse += "HTTP/1.1 200 OK\r\nServer: Java HTTP Server: 1.0\r\nDate: "+new Date()+"\r\nContent-Type: text/html\r\nContent-Length: "+stringBuilder.toString().length()+"\r\n";
                        /* NOTICE: log file will contain request line, response line and response header; not response body */
                        fileWriter.println(httpResponse);

                        printWriter.write(httpResponse);
                        printWriter.write("\r\n");
                        printWriter.write(stringBuilder.toString());
                        printWriter.flush();
                    }
                    if(fileContent.exists() && fileContent.isFile()) {
                        /* NOTICE: application/force-download */
                        httpResponse += "HTTP/1.1 200 OK\r\nServer: Java HTTP Server: 1.0\r\nDate: "+new Date()+"\r\nContent-Type: application/force-download\r\nContent-Length: "+fileContent.length()+"\r\n";
                        fileWriter.println(httpResponse);

                        printWriter.write(httpResponse);
                        printWriter.write("\r\n");
                        printWriter.flush();

                        /* NOTICE: IMPORTANT: sending file using socket */
                        int count;
                        byte[] buffer = new byte[1024];

                        try {
                            OutputStream out = socket.getOutputStream();
                            BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileContent));

                            while((count=in.read(buffer)) > 0) {
                                out.write(buffer, 0, count);
                                out.flush();
                            }

                            in.close();
                            out.close();
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(!fileContent.exists()) {
                        httpResponse += "HTTP/1.1 404 NOT FOUND\r\nServer: Java HTTP Server: 1.0\r\nDate: "+new Date()+"\r\nContent-Type: text/html\r\nContent-Length: "+stringBuilder.toString().length()+"\r\n";
                        fileWriter.println(httpResponse);

                        printWriter.write(httpResponse);
                        printWriter.write("\r\n");
                        printWriter.write(stringBuilder.toString());
                        printWriter.flush();

                        /* NOTICE */
                        System.out.println(">> 404: Page not found");
                    }
                }
            }

            /* closing process */
            try {
                bufferedReader.close();
                printWriter.close();
                fileWriter.close();
                socket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        if(httpRequest.startsWith("UPLOAD")) {
            /* checking filename validity message from client */
            try {
                String isValid = bufferedReader.readLine();

                if(isValid.equals("invalid")) {
                    System.out.println(">> given file name is invalid");

                    bufferedReader.close();
                    socket.close();
                    return ;
                }
            } catch(IOException e) {
                e.printStackTrace();
            }

            /* NOTICE: receiving file from client */
            int count;
            byte[] buffer = new byte[1024];

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(new File(root+"\\"+httpRequest.substring(7)));
                InputStream in = socket.getInputStream();

                while((count=in.read(buffer)) > 0){
                    fileOutputStream.write(buffer);
                }

                in.close();
                fileOutputStream.close();
            } catch(IOException e) {
                e.printStackTrace();
            }

            /* closing process */
            try {
                bufferedReader.close();
                socket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        return ;
    }
}
