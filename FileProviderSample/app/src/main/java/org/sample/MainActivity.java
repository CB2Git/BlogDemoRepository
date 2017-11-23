package org.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onWriteFile(View view) {
        File externalCacheDir = getExternalCacheDir();
        File newTextFile = new File(externalCacheDir, "a.txt");
        if (!newTextFile.exists()) {
            try {
                newTextFile.createNewFile();
                FileOutputStream out = new FileOutputStream(newTextFile);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write("write a string to file~~~~");
                writer.close();
                out.close();
                Log.i(TAG, "onWriteFile: Success~~~");
                Toast.makeText(this, "write file success~~~", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "onWriteFile: File is Exists");
        }
    }

    public void onOpenFile(View view) {
        File newTextFile = new File(getExternalCacheDir(), "a.txt");
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uriForFile = FileProvider.getUriForFile(this, getPackageName() + ".provider", newTextFile);
            intent.setDataAndType(uriForFile, "text/plain");
            //授予此URL临时读写权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            intent.setDataAndType(Uri.fromFile(newTextFile), "text/plain");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
