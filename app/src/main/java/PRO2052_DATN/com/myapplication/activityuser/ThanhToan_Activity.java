package PRO2052_DATN.com.myapplication.activityuser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import PRO2052_DATN.com.myapplication.R;
import PRO2052_DATN.com.myapplication.TrangChuActivity;
import PRO2052_DATN.com.myapplication.dao.HdctDao;
import PRO2052_DATN.com.myapplication.dao.UserDao;
import PRO2052_DATN.com.myapplication.model.Billdetail;
import PRO2052_DATN.com.myapplication.paypal.Paypal;

public class ThanhToan_Activity extends AppCompatActivity {
    public static final int PAYPAL_REQUEST_CODE=7171;
    private static PayPalConfiguration config= new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Paypal.PAYPAL_CLIENT_ID);
    Spinner spnthanhtoan;
    Button btnPay;
    EditText edtdiachi;
    ImageView imgbackt;
    String ht,diachi,ngay,username,detailid;
    FirebaseDatabase database;
    DatabaseReference bill;
    DatabaseReference billdetail;
    int total;
    List<Billdetail> cart = new ArrayList<>();
    HdctDao hdctDao;

    @Override
    protected void onDestroy() {
        stopService(new Intent(this,PayPalService.class));
        super.onDestroy();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanh_toan_);
        hdctDao= new HdctDao(this);
        spnthanhtoan= findViewById(R.id.spnthanhtoan);
        btnPay = findViewById(R.id.btnPay);
        edtdiachi = findViewById(R.id.edtdiachi);
        imgbackt=findViewById(R.id.imgbackt);
        database= FirebaseDatabase.getInstance();
        bill= database.getReference("Bill");
        billdetail= database.getReference("Billdetail");
        total= getIntent().getIntExtra("tt",0);
        imgbackt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        List<String> listtt = new ArrayList<String>();
        listtt.add("CASH ON DELIVERY");
        listtt.add("PAYPAL");
        ArrayAdapter arrayAdapter = new ArrayAdapter(ThanhToan_Activity.this,
                R.layout.support_simple_spinner_dropdown_item, listtt);
        spnthanhtoan.setAdapter(arrayAdapter);

        spnthanhtoan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ht = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
            Intent intent= new Intent(this,PayPalService.class);
            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
            startService(intent);

        // btn Thanh toán

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (edtdiachi.getText().toString().isEmpty()){
                    Toast.makeText(ThanhToan_Activity.this, "nhập địa chỉ", Toast.LENGTH_SHORT).show();
                }else {
                    Calendar calendar= Calendar.getInstance();
                    SimpleDateFormat curentDate=new SimpleDateFormat("dd-MM-yyyy");
                    ngay=curentDate.format(calendar.getTime());

                    diachi= edtdiachi.getText().toString();
                    username= UserDao.cruser.getUsname();
                    cart= new ArrayList<>();
                    cart=hdctDao.getall();

                    if (ht.equals("CASH ON DELIVERY")) {
                        final String billid= UserDao.cruser.getPhone();

                        final String a1= String.valueOf(System.currentTimeMillis());
                        HashMap<String,Object>objectHashMap= new HashMap<>();
                        objectHashMap.put("billid",a1);
                        objectHashMap.put("date",ngay);
                        objectHashMap.put("payments",ht);
                        objectHashMap.put("address",diachi);
                        objectHashMap.put("state","Pending");
                        objectHashMap.put("total",total);
                        objectHashMap.put("username",username);
                        objectHashMap.put("foods",cart);

                        bill.child(a1).updateChildren(objectHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull final Task<Void> task) {
                                if(task.isSuccessful()){
                                    String a= UserDao.cruser.getPhone();

                                    hdctDao.deleteItem();
                                    Toast.makeText(ThanhToan_Activity.this, "Gọi món thành công", Toast.LENGTH_SHORT).show();
                                    Intent intent= new Intent(ThanhToan_Activity.this, TrangChuActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
//                                billdetail.child("Admin View").child(a).child("Products").addValueEventListener(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                        for (DataSnapshot addressSnapshot: dataSnapshot.getChildren()){
//                                            billdetails.add(String.valueOf(addressSnapshot.getKey()));
//
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                    }
//                                });

                                }
                            }
                        });

                    }else {

                        PayPalPayment payPalPayment= new PayPalPayment(new BigDecimal(String.valueOf(total)),
                                "USD","Total",PayPalPayment.PAYMENT_INTENT_ORDER);
                        Intent intent= new Intent(ThanhToan_Activity.this, PaymentActivity.class);
                        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
                        intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payPalPayment);
                       // Start PaymentActivity với request code vừa được khai báo trước đó
                        startActivityForResult(intent,PAYPAL_REQUEST_CODE);



                    }
                }
                }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Kiểm tra requestCode có trùng với REQUEST_CODE vừa dùng
        if (requestCode==PAYPAL_REQUEST_CODE){
            // resultCode được set bởi PaymentActivity
            // RESULT_OK chỉ ra rằng kết quả này đã thành công
            if(resultCode==RESULT_OK){
                        PaymentConfirmation confirmation= data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
               if (confirmation!=null){
                   try {
                       String paymentDetails=confirmation.toJSONObject().toString(4);
                       JSONObject jsonObject= new JSONObject(paymentDetails);
                       //String s= jsonObject.getJSONObject("response").getString("state");
                       final String a1= String.valueOf(System.currentTimeMillis());
                       HashMap<String,Object>objectHashMap= new HashMap<>();
                       objectHashMap.put("billid",a1);
                       objectHashMap.put("date",ngay);
                       objectHashMap.put("payments",ht);
                       objectHashMap.put("address",diachi);
                       objectHashMap.put("state","Pending");
                       objectHashMap.put("total",total);
                       objectHashMap.put("username",username);
                       objectHashMap.put("foods",cart);


                       bill.child(a1).updateChildren(objectHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull final Task<Void> task) {
                               if(task.isSuccessful()){

                                   hdctDao.deleteItem();
                                   Toast.makeText(ThanhToan_Activity.this, "Thank you ", Toast.LENGTH_SHORT).show();
                                   Intent intent= new Intent(ThanhToan_Activity.this, BillUserActivity.class);
                                   startActivity(intent);
//

                               }
                           }
                       });
                   }catch (JSONException e){
                       e.printStackTrace();
                   }
               }
            }
            else if (resultCode== Activity.RESULT_CANCELED){
                Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
            }
        }
        else if (resultCode==PaymentActivity.RESULT_EXTRAS_INVALID){
            Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
        }
    }
}
