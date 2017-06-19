package com.xika.takephotodemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnTakePhoto, btnChoosePhoto;

    private ImageView picture;

    private static final int REQUEST_TAKE_PHOTO = 1; // 拍照标识
    private static final int REQUEST_CHOOSE_PHOTO = 2; // 选择相册标示符

    // 获取拍照权限标识
    private static final int PERMISSION_REQUEST_TAKE_PHONE = 6;
    private static final int PERMISSION_REQUEST_CHOOSE_PICTURE = 7;

    private File output;  // 设置拍照的图片文件
    private Uri photoUri;  // 拍摄照片的路径


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnTakePhoto = (Button) findViewById(R.id.btn_take_photo);
        btnChoosePhoto = (Button) findViewById(R.id.btn_choose_photo);
        picture = (ImageView) findViewById(R.id.image_picture);

        btnTakePhoto.setOnClickListener(this);
        btnChoosePhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 拍摄照片
            case R.id.btn_take_photo:
                checkPremissionTakePhoto(v);
                break;
            // 相册中选择
            case R.id.btn_choose_photo:
                checkPremissionTakePhoto(v);
                break;
        }

    }

    // 检查相机权限
    private void checkPremissionTakePhoto(View view) {
        switch (view.getId()) {
            case R.id.btn_take_photo:
                // 检查是否有读取权限,如果需要设置就让他设置;
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_TAKE_PHONE);
                } else {
                    takePhoto();
                }
                break;
            case R.id.btn_choose_photo:
                // 检查是否有读取权限,如果需要设置就让他设置;
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CHOOSE_PICTURE);
                } else {
                    choosePhoto();
                }
                break;
        }

    }

    /**
     * 调取相机拍摄照片.
     */
    private void takePhoto() {
        File file = new File(Environment.getExternalStorageDirectory(), "takePhotoDemo");
        if (!file.exists()) {
            // 如果文件路径不存在则直接创建一个文件夹
            file.mkdir();
        }
        // 把时间作为拍摄照片的保存路径;
        output = new File(file, System.currentTimeMillis() + ".jpq");
        // 如果该照片已经存在就删除它,然后新创建一个
        try {
            if (output.exists()) {
                output.delete();
            }
            output.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 隐式打开拍摄照片
        photoUri = Uri.fromFile(output);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    /**
     * 从相册选择照片
     */
    private void choosePhoto() {
        // 选择相册操作
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CHOOSE_PHOTO);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // 拍摄照片的回调
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri));
                        picture.setImageBitmap(bit);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Log.d("tag", e.getMessage());
                        Toast.makeText(this, "程序崩溃", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.i("REQUEST_TAKE_PHOTO", "拍摄失败");
                }
                break;
            // 调用系统相册的回调
            case REQUEST_CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    try {
                        Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                        picture.setImageBitmap(bit);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Log.d("tag", e.getMessage());
                        Toast.makeText(this, "程序崩溃", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.i("REQUEST_TAKE_PHOTO", "拍摄失败");
                }
                break;
        }
    }

    // 动态检察权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 设置写入权限
        if (requestCode == PERMISSION_REQUEST_TAKE_PHONE) {
            // 打开了读写权限
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(MainActivity.this, "请打开应用相机权限", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == PERMISSION_REQUEST_CHOOSE_PICTURE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                choosePhoto();
            } else {
                Toast.makeText(MainActivity.this, "请打开读取相册权限", Toast.LENGTH_LONG).show();
            }
        }

    }
}
