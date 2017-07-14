package com.example.timmy.faceppdemotransfer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import com.megvii.cloud.http.CommonOperate;
import com.megvii.cloud.http.FaceSetOperate;
import com.megvii.cloud.http.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import com.megvii.cloud.http.CommonOperate;
import com.megvii.cloud.http.FaceSetOperate;
import com.megvii.cloud.http.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.jar.Attributes;

public class MainActivity extends AppCompatActivity {
    TextView mTextView;
 private String attributes="emotion,gender,age,smiling,glass,headpose,facequality,blur";
    String key = "-09MfhydPuTqnptP0osrI7eOauP90aTu";//api_key
    String secret = "gPpSvUkJNEb9MNFGeeH0creMcfl7r-M8";//api_secret
    String imageUrl = "http://pic1.hebei.com.cn/003/005/869/00300586905_449eedbb.jpg";//来自网络上的一张图片
    StringBuffer sb = new StringBuffer();//字符串缓冲区

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.text);//初始化控件
        //判断api_key或者secret是否为空
        if(TextUtils.isEmpty(key) || TextUtils.isEmpty(secret)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);//创建对话框
            builder.setMessage("please enter key and secret");//添加对话框信息
            builder.setTitle("");//将对话框标题设为空
            builder.show();//让对话框显示
        }else{
        //如果秘钥都不为空，即可联网发送请求，注意要用线程方式访问网络
            //为了避免因网络问题而靠成的阻塞，建议将 API 调用放进异步线程里执行。
            new Thread(new Runnable() {
                @Override
                public void run() {
                    CommonOperate commonOperate = new CommonOperate(key, secret, false);//创建连接
                    FaceSetOperate FaceSet = new FaceSetOperate(key, secret, false);//创建用于存储脸库的集合
                    ArrayList<String> faces = new ArrayList<>();
                    try {
                        //检测第一个人脸，传的是本地图片文件
                        //detect first face by local file
                        Response response1 = commonOperate.detectByte(getBitmap(R.mipmap.c04), 0, attributes);//以本地而形式探测

                        String faceToken1 = getFaceToken(response1);//提取这个人的faceToken;
                  //      Log.e("TAG",paras);
                        faces.add(faceToken1);//将这个人脸假如列表用于后续搜索比对
                        sb.append("faceToken1: ");//将建加入缓冲区
                        sb.append(faceToken1);//将这个人加入缓冲区
                        runOnUiThread(new Runnable() {//更新ui线程，如果当前线程是ui线程，会立刻得到执行，否则，会加入队列。
                            @Override
                            public void run() {
                                mTextView.setText(sb.toString());//打印这个人的face_taken信息
                            }
                        });
                        //检测第二个人脸，传的是网络图片地址
                        //detect first face by intenal image
                        Response response2 = commonOperate.detectUrl(imageUrl, 0, null);//以网址的形式
                        String faceToken2 = getFaceToken(response2);//获取face_taken
                        faces.add(faceToken2);//加入脸库
                        sb.append("\n");//
                        sb.append("faceToken2: ");
                        sb.append(faceToken2);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextView.setText(sb.toString());//更新UI1线程
                            }
                        });
                        //检测第三个人脸，传的是base64格式的数据
                        //detect first face by local file use base64
                        String base64 = Base64.encodeToString(getBitmap(R.mipmap.c032), Base64.NO_WRAP);//将图片转成base64格式的数据
                        Response response3 = commonOperate.detectBase64(base64, 0, null);//以base64形式传入，进行探测
                        String faceToken3 = getFaceToken(response3);//获取face_token
                        faces.add(faceToken3);
                        sb.append("\n");
                        sb.append("faceToken3: ");
                        sb.append(faceToken3);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextView.setText(sb.toString());
                            }
                        });
                        //创建人脸库，并往里加人脸
                        //create faceSet and add face
                        String faceTokens = creatFaceTokens(faces);
                        Response faceset = FaceSet.createFaceSet(null,"test",null,faceTokens,null, 1);//test为脸集在系统中的标识
                        String faceSetResult = new String(faceset.getContent());//获取脸集创建的信息
                        Log.e("faceSetResult",faceSetResult);//打印信息
                        if(faceset.getStatus() == 200){//如果连接网络成功
                            sb.append("\n");
                            sb.append("\n");
                            sb.append("faceSet creat success");
                            sb.append("\n");
                            sb.append("create result: ");
                            sb.append(faceSetResult);
                        }else{//否则
                            sb.append("\n");
                            sb.append("\n");
                            sb.append("faceSet creat faile");
                            sb.append("\n");
                            sb.append("create result: ");
                            sb.append(faceSetResult);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {//更新ui线程信息
                                mTextView.setText(sb.toString());
                            }
                        });

                        //调用搜索API，得到结果
                        //use search API to find face
                        Response res = commonOperate.searchByOuterId(null, imageUrl, null, "test", 1);
                        String result = new String(res.getContent());//获取比对结果
                        Log.e("result", result);
                        sb.append("\n");
                        sb.append("\n");
                        sb.append("search result: ");
                        sb.append(result);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextView.setText(sb.toString());
                            }
                        });


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private String creatFaceTokens(ArrayList<String> faceTokens){//创建人脸库字符组；
        if(faceTokens == null || faceTokens.size() == 0){//判断是否为空，如果为空，则返回主程序
            return "";
        }
        StringBuffer face = new StringBuffer();//建立缓冲区
        for (int i = 0; i < faceTokens.size(); i++){
            if(i == 0){//建立以逗号为间隔的字符组。
                face.append(faceTokens.get(i));
            }else{
                face.append(",");
                face.append(faceTokens.get(i));
            }
        }
        return face.toString();//将缓冲区转成字符组返回给主程序
    }

    private byte[] getBitmap(int res){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), res);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private String getFaceToken(Response response) throws JSONException {
        if(response.getStatus() != 200){//连接不成功
            return new String(response.getContent());
        }
        String res = new String(response.getContent());//连接成功
        Log.e("response", res);//将返回的数据打印出来
        JSONObject json = new JSONObject(res);//将返回的数据转化为json数据格式
        String faceToken = json.optJSONArray("faces").optJSONObject(0).optString("face_token");//将face_token提取出来
        return faceToken;//返回字符串
    }


}
