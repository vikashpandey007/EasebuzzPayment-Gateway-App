package com.example.easebuzzpayment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugins.GeneratedPluginRegistrant;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import android.provider.Settings;
import android.view.WindowManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import com.easebuzz.payment.kit.PWECouponsActivity;
import datamodels.PWEStaticDataModel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import com.google.gson.Gson; 
import org.json.JSONObject;  
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import io.flutter.embedding.android.FlutterActivity;


public class MainActivity extends FlutterActivity {
     private static final String CHANNEL1 = "easebuzz";
    MethodChannel.Result channel_result;
    private boolean start_payment = true;

    private void startPayment(Object arguments) {
        try {
            Gson gson = new Gson();
            JSONObject parameters = new JSONObject(gson.toJson(arguments));
            Intent intentProceed = new Intent(getBaseContext(), PWECouponsActivity.class);
            intentProceed.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intentProceed.putExtra("access_key",parameters.getString("access_key"));
            intentProceed.putExtra("pay_mode",parameters.getString("pay_mode"));
            startActivityForResult(intentProceed, PWEStaticDataModel.PWE_REQUEST_CODE);
        }catch (Exception e) {
            start_payment=true;
            Map<String, Object> error_map = new HashMap<>();
            Map<String, Object> error_desc_map = new HashMap<>();
            String error_desc = "exception occured:"+e.getMessage();
            error_desc_map.put("error","Exception");
            error_desc_map.put("error_msg",error_desc);
            error_map.put("result",PWEStaticDataModel.TXN_FAILED_CODE);
            error_map.put("payment_response",error_desc_map);
            channel_result.success(error_map);
        }
    }
    

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // if (!this.setSecureSurfaceView()) {
        //     Log.e("MainActivity", "Could not secure the MainActivity!");
        // }
        start_payment = true;
        new MethodChannel(getFlutterEngine().getDartExecutor().getBinaryMessenger(), CHANNEL1).setMethodCallHandler(
                new MethodChannel.MethodCallHandler() {
                    @Override
                    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
                        channel_result = result;
                        if (call.method.equals("payWithEasebuzz")) {
                            if (start_payment) {
                                start_payment = false;
                                startPayment(call.arguments);
                            }
                        }
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data != null ) {
            if(requestCode==PWEStaticDataModel.PWE_REQUEST_CODE)
            {
                start_payment=true;
                JSONObject response = new JSONObject();
                Map<String, Object> error_map = new HashMap<>();
                if(data != null ) {
                    String result = data.getStringExtra("result");
                    String payment_response = data.getStringExtra("payment_response");
                    try {
                        JSONObject obj = new JSONObject(payment_response);
                        response.put("result", result);
                        response.put("payment_response", obj);
                        channel_result.success(JsonConverter.convertToMap(response));
                    }catch (Exception e){
                        Map<String, Object> error_desc_map = new HashMap<>();
                        error_desc_map.put("error",result);
                        error_desc_map.put("error_msg",payment_response);
                        error_map.put("result",result);
                        error_map.put("payment_response",error_desc_map);
                        channel_result.success(error_map);
                    }
                }else{
                    Map<String, Object> error_desc_map = new HashMap<>();
                    String error_desc = "Empty payment response";
                    error_desc_map.put("error","Empty error");
                    error_desc_map.put("error_msg",error_desc);
                    error_map.put("result","payment_failed");
                    error_map.put("payment_response",error_desc_map);
                    channel_result.success(error_map);
                }
            }else
            {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

}
