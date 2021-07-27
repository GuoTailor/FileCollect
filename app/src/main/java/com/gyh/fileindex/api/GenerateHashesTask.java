package com.gyh.fileindex.api;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.gyh.fileindex.R;
import com.gyh.fileindex.bean.FileInfo;
import com.gyh.fileindex.util.Util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Generates hashes from files (MD5 and SHA256)
 *
 * Created by Vishal on 05-02-2015 edited by Emmanuel Messulam<emmanuelbendavid@gmail.com>
 */
public class GenerateHashesTask extends AsyncTask<Void, String, String[]> {

    private final FileInfo file;
    private final Context context;
    private final TextView md5HashText;
    private final TextView sha256Text;
    private final LinearLayout mMD5LinearLayout;
    private final LinearLayout mSHA256LinearLayout;
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    public GenerateHashesTask(FileInfo f, final Context c, final View view) {
        this.context = c;
        this.file = f;

        md5HashText = view.findViewById(R.id.t9);
        sha256Text = view.findViewById(R.id.t10);

        mMD5LinearLayout = view.findViewById(R.id.properties_dialog_md5);
        mSHA256LinearLayout = view.findViewById(R.id.properties_dialog_sha256);

    }

    @Override
    protected String[] doInBackground(Void... params) {
        String md5 = context.getString(R.string.error);
        String sha256 = context.getString(R.string.error);

        try {
            if (!file.getFile().isDirectory()) {
                md5 = getMD5Checksum();
                sha256 = getSHA256Checksum();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new String[] {md5, sha256};
    }

    @Override
    protected void onPostExecute(final String[] hashes) {
        super.onPostExecute(hashes);
        if (!file.getFile().isDirectory() && file.getFile().length() != 0) {
            md5HashText.setText(hashes[0]);
            sha256Text.setText(hashes[1]);

            mMD5LinearLayout.setOnLongClickListener(v -> {
                Util.copyToClipboard(context, hashes[0]);
                Toast.makeText(context, context.getResources().getString(R.string.md5).toUpperCase() + " " +
                        context.getResources().getString(R.string.properties_copied_clipboard), Toast.LENGTH_SHORT).show();
                return false;
            });
            mSHA256LinearLayout.setOnLongClickListener(v -> {
                Util.copyToClipboard(context, hashes[1]);
                Toast.makeText(context, context.getResources().getString(R.string.hash_sha256) + " " +
                        context.getResources().getString(R.string.properties_copied_clipboard), Toast.LENGTH_SHORT).show();
                return false;
            });
        } else {
            mMD5LinearLayout.setVisibility(View.GONE);
            mSHA256LinearLayout.setVisibility(View.GONE);
        }
    }

    // see this How-to for a faster way to convert a byte array to a HEX string

    private String getMD5Checksum() throws Exception {
        byte[] b = createChecksum();
        StringBuilder result = new StringBuilder();

        for (byte aB : b) {
            result.append(Integer.toString((aB & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    private String getSHA256Checksum() throws NoSuchAlgorithmException, IOException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] input = new byte[DEFAULT_BUFFER_SIZE];
        int length;
        InputStream inputStream = new FileInputStream(file.getFile());
        while ((length = inputStream.read(input)) != -1) {
            if (length > 0)
                messageDigest.update(input, 0, length);
        }

        byte[] hash = messageDigest.digest();

        StringBuilder hexString = new StringBuilder();

        for (byte aHash : hash) {
            // convert hash to base 16
            String hex = Integer.toHexString(0xff & aHash);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        inputStream.close();
        return hexString.toString();
    }

    private byte[] createChecksum() throws Exception {
        InputStream fis = new FileInputStream(file.getFile());

        byte[] buffer = new byte[8192];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }
}
