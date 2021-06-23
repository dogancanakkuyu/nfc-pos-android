package com.example.mposapp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public class Service {
    private static WebService instance = null;
    private static TransactionService transaction_instance = null;
    private static MailService mail_instance=null;
    private static PaymentService payment_instance=null;
    private static PasswordChangeService password_instance=null;
    private static TransactionRequestService transactionRequest_instance=null;
    private static SignUpService signUp_instance=null;

    public static WebService getInstance() {
        if (instance == null) {
            OkHttpClient client = new OkHttpClient.Builder().callTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS).build();
            instance = new Retrofit.Builder().baseUrl("http://192.168.1.4:8081/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(WebService.class);
        }
        return instance;
    }
    public static TransactionService TransactionInstance() {
        if (transaction_instance == null) {
            OkHttpClient client = new OkHttpClient.Builder().callTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS).build();
            transaction_instance = new Retrofit.Builder().baseUrl("http://192.168.1.4:8081/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(TransactionService.class);
        }
        return transaction_instance;
    }

    public static MailService MailInstance() {
        if (mail_instance == null) {
            OkHttpClient client = new OkHttpClient.Builder().callTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS).build();
            mail_instance = new Retrofit.Builder().baseUrl("http://192.168.1.4:8081/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(MailService.class);
        }
        return mail_instance;
    }

    public static PaymentService paymentInstance(){
        if (payment_instance == null) {
            OkHttpClient client = new OkHttpClient.Builder().callTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS).build();
            payment_instance = new Retrofit.Builder().baseUrl("http://192.168.1.4:8081/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(PaymentService.class);
        }
        return payment_instance;
    }

    public static PasswordChangeService getPassword_instance(){
        if (password_instance == null) {
            OkHttpClient client = new OkHttpClient.Builder().callTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS).build();
            password_instance = new Retrofit.Builder().baseUrl("http://192.168.1.4:8081/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(PasswordChangeService.class);
        }
        return password_instance;
    }
    public static TransactionRequestService getTransactionRequestService_instance(){
        if (transactionRequest_instance == null) {
            OkHttpClient client = new OkHttpClient.Builder().callTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS).build();
            transactionRequest_instance = new Retrofit.Builder().baseUrl("http://192.168.1.4:8081/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(TransactionRequestService.class);
        }
        return transactionRequest_instance;
    }

    public static SignUpService getSignUp_instance(){
        if (signUp_instance == null) {
            OkHttpClient client = new OkHttpClient.Builder().callTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS).build();
            signUp_instance = new Retrofit.Builder().baseUrl("http://192.168.1.4:8081/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(SignUpService.class);
        }
        return signUp_instance;
    }

}

interface WebService {

    @POST("authenticate")
    public Call<UserResponse> authenticate(@Body User user);

}
interface TransactionService{

    @GET("api/history/{id}")
    public Call<List<TransactionInfo>> getTransaction(@Path(value = "id") String userid,@Header("Authorization") String token);
}

interface MailService{
    @POST("api/sendEmail")
    public Call<Void> sendEmail(@Body MailInfo mailInfo,@Header("Authorization") String token);
}
interface PaymentService{
    @POST("api/pay/{id}")
    public Call<Void> postPayment(@Path(value = "id") String userid, @Body PaymentInfo paymentInfo,@Header("Authorization") String token);
}

interface PasswordChangeService{
    @POST("api/change/{id}")
    public Call<Void> changePassword(@Path(value = "id") String userid, @Body PasswordInfo passwordInfo,@Header("Authorization") String token);
}

interface TransactionRequestService{
    @Multipart
    @POST("api/transaction/")
    public Call<TransactionReqInfo> transactionRequest(@Part MultipartBody.Part file, @Header("Authorization") String token);
}

interface SignUpService{
    @POST("register")
    public Call<Void> signUpMethod(@Body User user);
}