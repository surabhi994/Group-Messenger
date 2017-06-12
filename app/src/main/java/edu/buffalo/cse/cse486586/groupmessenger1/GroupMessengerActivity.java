package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import static java.lang.System.in;
import static java.security.AccessController.getContext;


public class GroupMessengerActivity extends Activity {
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT[] = {"11108", "11112", "11116", "11120", "11124"};
    static final int SERVER_PORT = 10000;
    int seqNum = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);


        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        final EditText editText = (EditText) findViewById(R.id.editText1);
        final Button b = (Button) findViewById(R.id.button4);

        try {

            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {

            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String msg = editText.getText().toString() + "\n";
                editText.setText("");
                TextView localTextView = (TextView) findViewById(R.id.textView1);
                localTextView.append("\t" + msg);

                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);

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
            String msg;

            ServerSocket serverSocket = sockets[0];


            while (true)

            {
                try {

                    Socket s = serverSocket.accept();


                    DataInputStream di = new DataInputStream(s.getInputStream());
                    DataOutputStream ack = new DataOutputStream(s.getOutputStream());


                    msg = di.readUTF();
                    ack.writeUTF("ACK");

                    publishProgress(msg);


                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        }

        protected void onProgressUpdate(String... strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim(); //code taken from pa1
            TextView TextView1 = (TextView) findViewById(R.id.textView1);
            TextView1.append(strReceived + "\t\n");
            TextView TextView2 = (TextView) findViewById(R.id.textView1);
            TextView2.append("\n");


            try {
                SharedPreferences sharedPref = getApplication().getSharedPreferences("Your Pref", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = sharedPref.edit();
                ed.putString(String.valueOf(seqNum), strReceived);
                ed.commit();
                seqNum++;
            } catch (Exception e) {
                Log.v("insert", "fail");
            }


            return;
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {

                for (int i = 0; i < 5; i++) {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(REMOTE_PORT[i]));

                    String sending_msg = msgs[0];

                    DataOutputStream d = new DataOutputStream(socket.getOutputStream());

                    d.writeUTF(sending_msg);
                    d.flush();
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    String s = in.readUTF();//reads msg from server
                    if (s == "ACK") {
                        socket.close(); // closes socket
                    }


                }
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;

        }
    }

}
