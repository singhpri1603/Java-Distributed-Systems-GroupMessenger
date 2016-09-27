package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * 
 *
 */
public class GroupMessengerActivity extends Activity {

    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static final int SERVER_PORT = 10000;
    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";
    String myPort;
    int proposal=0;
    int msgID=0;
    Comparator comp=new QueueComparator();
    LinkedList<String> activePorts=new LinkedList<String>(Arrays.asList(REMOTE_PORT0,REMOTE_PORT1,REMOTE_PORT2,REMOTE_PORT3,REMOTE_PORT4));
    HashMap<Integer, LinkedList<String>> myMap= new HashMap <Integer, LinkedList<String>>();
    PriorityQueue <message> myPQ= new PriorityQueue <message>(50, comp);
    HashMap<Integer, LinkedList<message>> proposalMap= new HashMap <Integer, LinkedList<message>>();
    String failed="temp";
    boolean ifFailed=false;
    static final ReentrantLock lock = new ReentrantLock(true);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */

        try {
            TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

            String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
            myPort = String.valueOf((Integer.parseInt(portStr) * 2));
            Log.v("pri", myPort);
        }catch(Exception e){
            Log.e("excptn",e.getMessage());
        }
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

        final Button send = (Button) findViewById(R.id.button4);
        final EditText editText = (EditText) findViewById(R.id.editText1);
        Log.v("send button", send.toString());

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Log.v("clickcheck", "check1");
                String msg = editText.getText().toString() + "\n";

                editText.setText(""); // This is one way to reset the input box.
//                TextView localTextView = (TextView) findViewById(R.id.textView1);
//                localTextView.append("\n" + msg); // This is one way to display a string.
                // TextView remoteTextView = (TextView) findViewById(R.id.textView1);
                //localTextView.append("\n");

                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg);
                return;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
        Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
        ContentValues cv = new ContentValues();
        ContentResolver cr=getContentResolver();
        int counter=0;
        //String KEY_FIELD = "key";
        //String VALUE_FIELD = "value";


        Uri buildUri(String scheme, String authority) {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
        }
        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];





                while(true) {
                    try {

                    Socket clientSocket = serverSocket.accept(); // accept the client connection
                    lock.lock();
//                    InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
//                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader); // get the client message
//
//                    String message = bufferedReader.readLine();
//
//
//                    Log.e(TAG,message+"  balleeee!!!!");
//
//                    publishProgress(message, clientSocket.getInetAddress().toString());}

                    ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
                    message msg = (message) inputStream.readObject();
                        Log.v("whilestart",msg.msgeID+" "+msg.msge+" "+msg.tag);




                    //******* if its a new message *****

                    int stat=msg.tag;
                    Log.v("msgtag",msg.tag+" "+msg.msgeID+" "+msg.orig_process_id);
                    if(stat==1){
                        proposal=proposal+1;
                        msg.proposal=proposal;
                        msg.ppsl_process_id=myPort;
                        msg.tag=2;

                        myPQ.add(msg);

                        try {

                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        Integer.parseInt(msg.orig_process_id));

                                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//
                                out.writeObject(msg);

                                socket.close();
                                Log.v("client each ","message tagged 2 "+msg.msge);

                        } catch (UnknownHostException e) {
                            Log.e(TAG, "ClientTask UnknownHostException");
                        } catch (IOException e) {
                            Log.v(TAG, "ClientTask socket IOException");
                            Log.v("Exc proposal sending", " " + msg.orig_process_id + " " + msg.ppsl_process_id);
                            excHandler(msg.orig_process_id);
                        }

                        catch(Exception e){
                            //Log.e(TAG, e.getMessage());
                            Log.v("MainExc on sending prop", " " + msg.orig_process_id+" "+msg.ppsl_process_id);
                        }

                    }

                    //******* if its a message with proposal *****

                    else if(stat==2) {

                        //try{

                        Log.v("proposal received", msg.ppsl_process_id+" "+msg.msge);

                        if (myMap.containsKey(msg.msgeID)) {

                            Log.v("if condition passed","after proposal received");

                            LinkedList<String> receivedPorts = myMap.get(msg.msgeID);

                            receivedPorts.remove(msg.ppsl_process_id);

                            LinkedList<message> propList = proposalMap.get(msg.msgeID);

                            propList.add(msg);

                            boolean flag = false;

                            if (receivedPorts.size() == 1) {
                                if (!activePorts.contains(receivedPorts.getFirst())) {
                                    flag = true;
                                }
                            }


                            if (receivedPorts.size() == 0 || flag) {

                                Log.v("here","here");
                                Collections.sort(propList, new QueueComparator());

                                Iterator it = propList.iterator();
                                while (it.hasNext()) {
                                    message msg2 = (message) it.next();
                                    Log.v("proposals", msg2.msge + " " + msg2.msgeID + " " + msg2.ppsl_process_id + " " + msg2.proposal);
                                }

                                msg = propList.getLast();

                                myMap.remove(msg.msgeID);
                                proposalMap.remove(msg.msgeID);


                                Log.v("proposals0000", msg.msge + " " + msg.msgeID + " " + msg.ppsl_process_id + " " + msg.proposal);

                                msg.tag = 3;

                                try {
                                    String[] remotePort = activePorts.toArray(new String[activePorts.size()]);
                                    for (String s : remotePort) {

                                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                                Integer.parseInt(s));

                                        try {

                                            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//
                                            out.writeObject(msg);

                                            socket.close();
                                            Log.v("client message tagged 3", s);
                                        } catch (IOException e) {
                                            //Log.e(TAG, e.getMessage());
                                            Log.v("Exc on sending del", " " + s);
                                            excHandler(s);
                                            continue;
                                        } catch (Exception e) {
                                            //Log.e(TAG, e.getMessage());
                                            Log.v("Main Exc on sending del", " " + s);
                                            continue;
                                        }
                                    }
                                } catch (UnknownHostException e) {
                                    Log.e(TAG, "ClientTask UnknownHostException");
                                } catch (IOException e) {
                                    Log.e(TAG, "ClientTask socket IOException");
                                    //Log.v("pri sending for deli", e.getMessage());
                                }



                            }
//                        }catch(NullPointerException e) {
//                            Log.e("Null","Pointer");
//                        }


                    }

                    }
                    //******* if its a message ready for delivery *****

                    else if (stat == 3) {

                        Log.v("here", "I am finally here");
                        myPQ.remove(msg);
                        myPQ.add(msg);

                        Log.v("print to PQ 1", "{{{{{{{{{");


                        //PriorityQueue<message> tempPQ=new PriorityQueue<message>(25,new QueueComparator());



                                Iterator it = myPQ.iterator();
                        while(it.hasNext()){
                            message currentmsg= (message)it.next();

                                    Log.v("print to PQ TAG:",currentmsg.tag+" "+currentmsg.msge+" "+currentmsg.orig_process_id+" "+currentmsg.proposal+" "+currentmsg.ppsl_process_id);

                        }



                        Iterator it2 = myPQ.iterator();
                        while (it2.hasNext()){
                            message currentmsg2= (message)myPQ.peek();
                            Log.v("curent msg","tag:"+currentmsg2.tag);
                            if(currentmsg2.tag==3 ){
                                Log.v("bfor publish","sdsd");
                                myPQ.remove(currentmsg2);
                                Log.v(TAG, currentmsg2.msge+"I am at publish progress");
                                publishProgress(currentmsg2.msge, clientSocket.getInetAddress().toString());
                            }
                            else if(!activePorts.contains(currentmsg2.orig_process_id)){
                                myPQ.remove(currentmsg2);
                            }
                            else{
                                break;
                            }

                        }
                    }

                        if(ifFailed) {
                            Log.v("if failed",msg.msge);

                            Iterator itt2 = myMap.entrySet().iterator();
                            while (itt2.hasNext()) {
                                Map.Entry pair = (Map.Entry) itt2.next();
                                LinkedList<String> list = (LinkedList<String>) pair.getValue();
                                if (list.contains(failed)) {
                                    Log.v("inside","failed");
                                    message dummymsg = new message();
                                    dummymsg.orig_process_id = myPort;
                                    dummymsg.ppsl_process_id = failed;
                                    dummymsg.msge = "dummy";
                                    dummymsg.msgeID = (Integer) pair.getKey();
                                    dummymsg.proposal = 0;
                                    dummymsg.tag = 2;

                                    Log.v("dummy"+dummymsg.orig_process_id,dummymsg.ppsl_process_id+" "+dummymsg.msgeID);


                                    try {
                                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                                Integer.parseInt(dummymsg.orig_process_id));

                                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                                        //
                                        out.writeObject(dummymsg);

                                        socket.close();
                                        Log.v("client each ", "message tagged 2 "+dummymsg.msge);

                                    }catch(Exception e){
                                        Log.e("Exc inside failed"," ");
                                    }

                                }

                            }

                            Log.v("if failedend",msg.msge);
                        }


                    }catch (IOException exc) {
                        Log.d("SocketChat", "IO ecxeption");
                    }
                    catch(ClassNotFoundException exc){
                        Log.d("Socket problem", "Class not found");
                    }
                    catch(Exception e){
                        Log.e("Main Exc","Main");
                    }
                    finally {
                        lock.unlock();
                    }

            }



        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append("\n"+counter+"::"+strReceived );
            //TextView localTextView = (TextView) findViewById(R.id.textView1);
            //remoteTextView.append("\n");

            //String uri= GroupMessengerProvider.insert();

            cv.put(KEY_FIELD, Integer.toString(counter));
            cv.put(VALUE_FIELD, strReceived);
            counter++;

            cr.insert(mUri,cv);

//            String filename = "SimpleMessengerOutput";
//            String string = strReceived + "\n";
//            FileOutputStream outputStream;
//
//            try {
//                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
//                outputStream.write(string.getBytes());
//                outputStream.close();
//            } catch (Exception e) {
//                Log.e(TAG, "File write failed");
//            }

            return;
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String[] remotePort = activePorts.toArray(new String[activePorts.size()] );

                String msgToSend = msgs[0];

                message msg= new message();

                msg.msgeID=msgID++;
                msg.msge= msgToSend;
                msg.tag=1;
                msg.orig_process_id= myPort;

                LinkedList<String> aList=(LinkedList) activePorts.clone() ;
                LinkedList<message> pList=new LinkedList<message>();
                myMap.put(msg.msgeID,aList);
                proposalMap.put(msg.msgeID,pList);

                for(String s: remotePort){

                    Log.v("here", "here i am!!!");

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(s));

                    Log.v("msg", msgs[0]);



                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                    Log.v("here","now here");

                    try {

                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//                    out.println(msgToSend);
//                    out.flush();
                        out.writeObject(msg);

                        socket.close();
                        Log.v("client each ", "port client");
                    }
                    catch(SocketTimeoutException e){
                       // Log.e(TAG,e.getMessage());
                        Log.v("timeout exc",s);

                    }
                    catch(IOException e){
                      //  Log.e(TAG, e.getMessage());
                        Log.v("Exc on sending msg"," "+s);
                        excHandler(s);
                        continue;
                    }

                    catch(Exception e){
                        //Log.e(TAG, e.getMessage());
                        Log.v("Main Exc on sending msg", " " + s);
                        continue;
                    }
                }
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
                //Log.v("priyankaclientSending",e.getMessage());
            }
            catch (Exception e){
                Log.e(TAG, "MAin Exception");
               // Log.v("excptn caught",e.getMessage());
                //e.printStackTrace();
            }

            return null;
        }
    }

    public void excHandler(String portID){

        Log.e("Exc", "Handler reached " + portID);

        activePorts.remove(portID);

        failed=portID;
        ifFailed=true;

//        Iterator itt= myPQ.iterator();
//        while(itt.hasNext()){
//            message garbageMsg=(message)itt.next();
//            if(garbageMsg.orig_process_id.equals(portID)){
//                myPQ.remove(garbageMsg);
//            }
//        }

//        Iterator itt2=myMap.entrySet().iterator();
//        while (itt2.hasNext()){
//            Map.Entry pair=(Map.Entry)itt2.next();
//            LinkedList<String> list = (LinkedList<String>)pair.getValue();
//            String value= list.getFirst();
//            if(list.size()==1){
//                if(!activePorts.contains(value)){
//                    int key=(Integer)pair.getKey();
//
//                    LinkedList<message> delList=proposalMap.get(key);
//                    Collections.sort(delList, new QueueComparator());
//                    message msg=delList.getLast();
//
//
//                    msg.tag = 3;
//
//                    try {
//                        String[] remotePort = activePorts.toArray(new String[activePorts.size()] );
//                        for (String s : remotePort) {
//
//                            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
//                                    Integer.parseInt(s));
//
//                            try {
//
//                                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
////
//                                out.writeObject(msg);
//
//                                socket.close();
//                                Log.v("hndlr message tagged 3", s);
//                            }
//                            catch(IOException e){
//                                //Log.e(TAG, e.getMessage());
//                                Log.v("Exc on sending del"," "+s);
//                                excHandler(s);
//                                continue;
//                            }
//                            catch(Exception e){
//                               // Log.e(TAG, e.getMessage());
//                                Log.v("Main Exc on sending del", " " + s);
//                                continue;
//                            }
//                        }
//                    } catch (UnknownHostException e) {
//                        Log.e(TAG, "ClientTask UnknownHostException");
//                    } catch (IOException e) {
//                        Log.e(TAG, "ClientTask socket IOException");
//                        //Log.v("pri sending for deli", e.getMessage());
//                    }
//
//
//                    myMap.remove(msg.msgeID);
//                    proposalMap.remove(msg.msgeID);
//
//
//
//                }
//            }

     //   }

    }
}

