package com.example.myfirstapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.Cache;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

public class ShowImgActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_GALLERY = 0x10;// 图库选取图片标识请求码
    private static final int CROP_PHOTO = 0x12;// 裁剪图片标识请求码
    private static final int STORAGE_PERMISSION = 0x20;// 动态申请存储权限标识
    private Button mBtnUpload1;
    @BindView(R.id.iv_pic)
    ImageView iv_pic;// imageView控件

    private File imageFile = null;// 声明File对象
    private Uri imageUri = null;// 裁剪后的图片uri
    private String path = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_img);
        ButterKnife.bind(this);// 控件绑定
        // 动态申请存储权限，后面读取文件有用
        requestStoragePermission();
        mBtnUpload1 = findViewById(R.id.upload1);
        ProgressDialog progressDialog = new ProgressDialog(this);
        mBtnUpload1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Bitmap bitmap = null;
//                try {
//                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                PROgressDialog(progressDialog);

                sendImage(progressDialog);
            }
        });
    }

    /**
     * 单击事件绑定
     */
    @OnClick({R.id.btn_gallery})
    public void doClick(View view){

        switch (view.getId()){

            case R.id.btn_gallery:// 图库选择

                gallery();

                break;

//            case R.id.upload1:
//
//                sendImage();
//
//                break;

        }

    }



    /**
     * 图库选择图片
     */
    private void gallery() {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // 以startActivityForResult的方式启动一个activity用来获取返回的结果
        startActivityForResult(intent, REQUEST_CODE_GALLERY);

    }

    /**
     * 接收#startActivityForResult(Intent, int)调用的结果
     * @param requestCode 请求码 识别这个结果来自谁
     * @param resultCode    结果码
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {// 操作成功了

            switch (requestCode) {

                case REQUEST_CODE_GALLERY:// 图库选择图片

                    Uri uri = data.getData();// 获取图片的uri
//
//                    Intent intent_gallery_crop = new Intent("com.android.camera.action.CROP");
//                    intent_gallery_crop.setDataAndType(uri, "image/*");
//
//                    // 设置裁剪
//                    intent_gallery_crop.putExtra("crop", "true");
//                    intent_gallery_crop.putExtra("scale", true);
//                    // aspectX aspectY 是宽高的比例
//                    intent_gallery_crop.putExtra("aspectX", 1);
//                    intent_gallery_crop.putExtra("aspectY", 1);
//                    // outputX outputY 是裁剪图片宽高
//                    intent_gallery_crop.putExtra("outputX", 400);
//                    intent_gallery_crop.putExtra("outputY", 400);
//
//                    intent_gallery_crop.putExtra("return-data", false);
//
//                    // 创建文件保存裁剪的图片
//                    createImageFile();
                    imageUri = uri;
                    path = getRealPathFromURI(imageUri);
                    imageFile = new File(path);
                    if (imageUri != null) {
                        displayImage(imageUri);
                    }

//                    if (imageUri != null) {
//                        intent_gallery_crop.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//                        intent_gallery_crop.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
//                    }

//                    startActivityForResult(intent_gallery_crop, CROP_PHOTO);

                    break;

                case CROP_PHOTO:// 裁剪图片

                    try {

                        if (imageUri != null) {
                            displayImage(imageUri);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;

            }

        }
    }

    /**
     * Android6.0后需要动态申请危险权限
     * 动态申请存储权限
     */
    private void requestStoragePermission() {

        int hasCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        Log.e("TAG","开始" + hasCameraPermission);
        if (hasCameraPermission == PackageManager.PERMISSION_GRANTED){
            // 拥有权限，可以执行涉及到存储权限的操作
            Log.e("TAG", "你已经授权了该组权限");
        }else {
            // 没有权限，向用户申请该权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.e("TAG", "向用户申请该组权限");
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
            }
        }

    }

    /**
     * 动态申请权限的结果回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // 用户同意，执行相应操作
                Log.e("TAG","用户已经同意了存储权限");
            }else {
                // 用户不同意，向用户展示该权限作用
            }
        }

    }

    /**
     * 创建File保存图片
     */
    private void createImageFile() {

        try{

            if (imageFile != null && imageFile.exists()){
                imageFile.delete();
            }
            // 新建文件
            imageFile = new File(Environment.getExternalStorageDirectory(),
                    System.currentTimeMillis() + "galleryDemo.jpg");
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 显示图片
     * @param imageUri 图片的uri
     */
    private void displayImage(Uri imageUri) {
        try{
            // glide根据图片的uri加载图片
            Glide.with(this)
                    .load(imageUri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.mipmap.ic_launcher_round)// 占位图设置：加载过程中显示的图片
                    .error(R.mipmap.ic_launcher_round)// 异常占位图
                    .transform(new CenterCrop(this))
                    .into(iv_pic);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void PROgressDialog(ProgressDialog progressDialog) {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("正在运行.......");
        progressDialog.setIcon(R.drawable.ic_launcher_background);
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    private void alertDialog(String msg){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Result");
        dialog.setMessage(msg);
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog.show();
    }

    private void sendImage(ProgressDialog progressDialog)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bm.compress(Bitmap.CompressFormat.PNG, 60, stream);
//        byte[] bytes = stream.toByteArray();
//        String img = new String(Base64.encodeToString(bytes, Base64.DEFAULT));
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        try {
            params.put("image", imageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        client.post("http://23.224.152.72:6543/index_/paper1", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                progressDialog.dismiss();
                alertDialog(new String(bytes));
                //Message msg = Message.obtain();
//                Toast.makeText(ShowImgActivity.this, "Upload Success!", Toast.LENGTH_LONG).show();
                //Toast.makeText(ShowImgActivity.this, new String(bytes), Toast.LENGTH_LONG).show();

            }
            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                progressDialog.dismiss();
                alertDialog(new String(bytes));
//                Toast.makeText(ShowImgActivity.this, "Upload Fail!", Toast.LENGTH_LONG).show();
            }
        });
    }


    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){; int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        res = cursor.getString(column_index); } cursor.close(); return res; }


}

