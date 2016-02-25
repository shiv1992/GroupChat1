package edu.buffalo.cse.cse486586.groupmessenger1;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */

public class GroupMessengerActivity extends Activity {


    public static Uri uri = Uri.parse("content://edu.buffalo.cse.cse486586.groupmessenger1.provider/myTable");
    static final int SERVER_PORT = 10000;
    public static String TAG="A";
    public static int KEY=0;
   // EditText txt = (EditText)findViewById(R.id.editText1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */


        final TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());


        //Telephony
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        try {

            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
            */

            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
            */
            Log.e("TAG", "Can't create a ServerSocket");
            return;
        }



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

        final EditText txt = (EditText)findViewById(R.id.editText1);

        Button bt = (Button)findViewById(R.id.button4);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


               String ans=txt.getText().toString();


                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ans, myPort);
                txt.setText("");

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

        @Override
        protected Void doInBackground(ServerSocket... sockets) {


            while(true) {
                ServerSocket serverSocket = sockets[0];
                String message;
                Socket soc;


                try {
                    soc = serverSocket.accept();
                    InputStreamReader read = new InputStreamReader(soc.getInputStream());
                    BufferedReader bread = new BufferedReader(read);
                    message = bread.readLine();


                    ContentValues cv = new ContentValues();
                    cv.put("key", Integer.toString(KEY));
                    cv.put("value", message);
                    ContentResolver cr = getContentResolver();
                    cr.insert(uri, cv);
                    KEY++;

                    publishProgress(message);
                    Log.e(TAG, "Server Message Received");
                    soc.close();

                } catch (IOException e) {
                    Log.e(TAG, "ServerSocket IOException");
                    e.printStackTrace();
                }

            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */

            }
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");
            TextView localTextView = (TextView) findViewById(R.id.textView1);
            localTextView.append("\n");

            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */

            String filename = "GroupMessengerOutput";
            String string = strReceived + "\n";
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "File write failed");
            }

            return;
        }
    }

    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     *
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

            Log.e("Message Count : ", Integer.toString(KEY));


            int i;
            int port=11108;
            for(i=0;i<6;i++)
            {
            try {
                String remotePort = Integer.toString(port);
                port = port + 4;

                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(remotePort));

                String msgToSend = msgs[0];

                Log.e(TAG, "Server A4");
                OutputStream out = socket.getOutputStream();
                Log.e(TAG, "Server 1");
                OutputStreamWriter owrite = new OutputStreamWriter(out);
                Log.e(TAG, "Server 2");
                BufferedWriter bwrite = new BufferedWriter(owrite);
                Log.e(TAG, "Server 3");
                bwrite.write(msgToSend);
                Log.e(TAG, "Server 4");
                bwrite.flush();
                Log.e(TAG, "Server Message Sent");


                socket.close();
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }


        }

            return null;
        }
    }
}
