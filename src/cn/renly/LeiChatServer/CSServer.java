package cn.renly.LeiChatServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.fastjson.*;

/** 
 * Tcp通信服务器 
 * @author Renly
 * 
 */  
public class CSServer {  
    private static final int PORT = 43322;  
    private List<Socket> mClientList = new ArrayList<Socket>();  
    private ServerSocket server = null;  
    private ExecutorService mExecutors = null; // 线程池对象  
    // 用户ID socketID对应表
    private Map<String, Integer>userToSocket = new HashMap<String ,Integer>();
    
    public static void main(String[] args) {  
        new CSServer();  
    }  
  
    /** 
     * 构造方法：任务是启动服务器，等待客户端连接 
     */  
    public CSServer() {  
        try {  
            server = new ServerSocket(PORT);  
            mExecutors = Executors.newCachedThreadPool(); // 创建线程池  
            System.out.println("服务器已启动，等待客户端连接...");  
            System.out.println("本地服务器地址:" + InetAddress.getLocalHost());
            Socket client = null;  
            /* 
             * 用死循环等待多个客户端的连接，连接一个就启动一个线程进行管理 
             */  
            while (true) {  
                client = server.accept();  
                // 把客户端放入集合中  
                mClientList.add(client);  
                mExecutors.execute(new Service(client)); // 启动一个线程，用以守候从客户端发来的消息  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
  
    class Service implements Runnable {  
        private Socket socket;  
        private BufferedReader in = null;  
        private String message = "";  
  
        public Service(Socket socket) {  
            this.socket = socket;  
            try {  
                in = new BufferedReader(new InputStreamReader(  
                        socket.getInputStream()));// 获得输入流对象  
                // 客户端只要一连到服务器，便发送连接成功的信息  
                message = "当前连接总数:" + mClientList.size();  
                System.out.println(message);// 在控制台输出  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
  
        }  
  
        @Override  
        public void run() {  
            try {  
                while (true) {  
                    if ((message = in.readLine()) != null) {  
                        // 当客户端发送的信息为：exit时，关闭连接  
                        if (message.equals("exit")) {  
                            closeSocket();  
                            break;  
                        } else {  
                            // 接收客户端发过来的信息message，然后转发给客户端。  
                        	handleMsg(message);
                        }  
                    }  
                }  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
  
        private void handleMsg(String jsonString) {
        	JSONObject jsonObject = JSON.parseObject(jsonString);
        	//JSON数据内应包含字段 form content fromUserId toUserId
        	String form = jsonObject.getString("form");
        	if(form.equals("test")){
        		//空包
        		String fromUserId = jsonObject.getString("fromUserId");
        		userToSocket.put(fromUserId, mClientList.size()-1);
        	}else{
        		//发送消息
        		String msg = jsonObject.getString("content");
            	String toUserId = jsonObject.getString("toUserId");
            	int toUserSocket = userToSocket.get(toUserId);
            	
    			this.sendMessage(msg, toUserSocket);
        	}
		}

		/** 
         * 关闭客户端 
         *  
         * @throws IOException 
         */  
        public void closeSocket() throws IOException {  
            mClientList.remove(socket);  
            in.close();  
            message = "主机:" + socket.getInetAddress() + "关闭连接\n目前在线:"  
                    + mClientList.size();  
            socket.close();  
            System.out.println(message);// 在控制台输出  
//            this.sendMessage(message);  
        }  
  
        /** 
         * 将接收的消息转发给每一个客户端 
         *  
         * @param msg 
         */  
  
        //群发
//        public void sendMessages(String msg,int toUserSocket) {  
//            System.out.println(msg);// 在控制台输出  
//            int count = mClientList.size();  
//            // 遍历客户端集合  
//            for (int i = 0; i < count; i++) {  
//            	if(i == now)
//            		continue;
//                Socket mSocket = mClientList.get(i);  
//                PrintWriter out = null;  
//                try {  
//                    out = new PrintWriter(new BufferedWriter(  
//                            new OutputStreamWriter(mSocket.getOutputStream())),  
//                            true);// 创建输出流对象  
//                    out.println(msg);// 转发  
//                } catch (IOException e) {  
//                    e.printStackTrace();  
//                }  
//            }  
//        }  
//    }  
    
    //私聊
    public void sendMessage(String msg,int toUserSocket) {  
        System.out.println(msg);// 在控制台输出  
        Socket mSocket = mClientList.get(toUserSocket);  
        PrintWriter out = null;  
        try {  
        	out = new PrintWriter(new BufferedWriter(  
            	new OutputStreamWriter(mSocket.getOutputStream())),  
                	true);// 创建输出流对象  
        	out.println(msg);// 转发  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
    }  
}  
  
}  
