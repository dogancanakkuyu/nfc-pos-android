package com.example.mposapp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.projectdesign.R;
import com.github.devnied.emvnfccard.enums.CommandEnum;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.iso7816emv.TerminalTransactionQualifiers;
import com.github.devnied.emvnfccard.model.Afl;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.model.enums.TransactionTypeEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.parser.impl.AbstractParser;
import com.github.devnied.emvnfccard.parser.impl.EmvParser;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.TlvUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.sf.scuba.util.Hex;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.devnied.bitlib.BytesUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TapToPay extends AppCompatActivity {


    private IsoDep isoDep;
    private ProgressBar progressBar;
    private NfcAdapter mAdapter;
    private String token,id,paymentAmountStr,passToken;
    private  List<TransactionInfo> transactionInfos;
    private ImageView imageApprove,imageTapToPay,imageRejected;
    private TextView approveText,rejectText;
    private static final Logger LOGGER = LoggerFactory.getLogger(TapToPay.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tap_to_pay);
        mAdapter= NfcAdapter.getDefaultAdapter(this);
        token=getIntent().getStringExtra("token");
        passToken="";
        for (int i=7;i<token.length();i++){
            passToken+=token.charAt(i);
        }
        id=getIntent().getStringExtra("id");
        paymentAmountStr=getIntent().getStringExtra("paymentAmountStr");
        imageApprove=findViewById(R.id.tickImage);
        progressBar=findViewById(R.id.progressBar);
        imageTapToPay=findViewById(R.id.imageView2);
        imageRejected=findViewById(R.id.closeImage);
        approveText=findViewById(R.id.approvedText);
        rejectText=findViewById(R.id.rejectedText);
        //transactionInfos=(List<TransactionInfo>) getIntent().getSerializableExtra("tr");

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected, tagDetected, ndefDetected};

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if (mAdapter != null)
            mAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);

    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        isoDep = IsoDep.get(tag);
        try {
            isoDep.connect();
            IProvider provider = new NFCCardRead(isoDep);


            EmvTemplate.Config config = EmvTemplate.Config()
                    .setContactLess(true) // Enable contact less reading (default: true)
                    .setReadAllAids(true) // Read all aids in card (default: true)
                    .setReadTransactions(true) // Read all transactions (default: true)
                    .setReadCplc(false) // Read and extract CPCLC data (default: false)
                    .setRemoveDefaultParsers(false) // Remove default parsers for GeldKarte and EmvCard (default: false)
                    .setReadAt(true) // Read and extract ATR/ATS and description
                    ;
// Create Parser
            EmvTemplate parser = EmvTemplate.Builder() //
                    .setProvider(provider) // Define provider
                    .setConfig(config) // Define config
                    //.setTerminal(terminal) (optional) you can define a custom terminal implementation to create APDU
                    .build();
            EmvCard card=parser.readEmvCard();
            CallEmvClass callEmvClass=new CallEmvClass(parser);
            byte [] firstResponse=provider.transceive(HexStringToByteArray("00A404000E325041592E5359532E444446303100"));
            byte[] aid=TlvUtil.getValue(firstResponse,EmvTags.AID_CARD);
            byte[] selectCommandBase={(byte) 0x00,(byte) 0xA4,(byte) 0x04,(byte) 0x00,(byte) aid.length};
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            output.write(selectCommandBase);
            output.write(aid);
            output.write((byte) 0x00);
            byte[] selectCommand=output.toByteArray();
            byte [] selectResponse=provider.transceive(selectCommand);
            byte[] pdol = TlvUtil.getValue(selectResponse, EmvTags.PDOL);
            byte[] gpoBase={(byte) 0x80,(byte) 0xA8,(byte) 0x00,(byte) 0x00};
            List<TagAndLength> parsedpdol = TlvUtil.parseTagAndLength(pdol);
            int tagsLength=0;
            for (int i=0;i<parsedpdol.size();i++){
                tagsLength+=parsedpdol.get(i).getLength();
            }
            int completeLength=(tagsLength*2+4)/2;
            byte[] gpoBaseContinue={(byte) completeLength,(byte) 0x83,(byte) tagsLength};
            ByteArrayOutputStream outputGPO = new ByteArrayOutputStream();
            outputGPO.write(gpoBase);
            outputGPO.write(gpoBaseContinue);
            for (int i=0;i<parsedpdol.size();i++){
                outputGPO.write(constructgpo(parsedpdol.get(i)));
            }
            outputGPO.write((byte) 0x00);
            byte[] gpoCommand=outputGPO.toByteArray();
            byte[] gpoResponse=provider.transceive(gpoCommand);
            JSONObject jsonEmvRequest=new JSONObject();
            jsonEmvRequest.put("not-found","6700");
            jsonEmvRequest.put("randoms","ABABABAB");
            jsonEmvRequest.put("date",getDatetime());
            jsonEmvRequest.put("time","000000");
            jsonEmvRequest.put("transaction-req","9F02060000000000015F2A0209499C0100");
            JSONArray jsonArrayApdus=new JSONArray();
            JSONObject jsonFirstCommandResp=new JSONObject();
            jsonFirstCommandResp.put("cmd", "00A404000E325041592E5359532E444446303100");
            jsonFirstCommandResp.put("resp",ByteArrayToHexString(firstResponse));
            JSONObject jsonSecondCommandResp=new JSONObject();
            jsonSecondCommandResp.put("cmd",ByteArrayToHexString(selectCommand));
            jsonSecondCommandResp.put("resp",ByteArrayToHexString(selectResponse));
            JSONObject jsonGPOCommandResp=new JSONObject();
            jsonGPOCommandResp.put("cmd",ByteArrayToHexString(gpoCommand));
            jsonGPOCommandResp.put("resp",ByteArrayToHexString(gpoResponse));
            jsonArrayApdus.put(jsonFirstCommandResp);
            jsonArrayApdus.put(jsonSecondCommandResp);
            jsonArrayApdus.put(jsonGPOCommandResp);
            byte[] afl=TlvUtil.getValue(gpoResponse,EmvTags.APPLICATION_FILE_LOCATOR);
            if(afl!=null){
                List<Afl> appFileLocatorList=callEmvClass.callAflList(afl);
                byte[] readAppDataBase={(byte) 0x00,(byte) 0xB2};
                for(int i=0;i<appFileLocatorList.size();i++){
                    ByteArrayOutputStream outputreadAppData = new ByteArrayOutputStream();
                    outputreadAppData.write(readAppDataBase);
                    byte[] firstrecord={(byte) appFileLocatorList.get(i).getFirstRecord()};
                    outputreadAppData.write(firstrecord);
                    String tmp="";
                    tmp+=convertBinary(appFileLocatorList.get(i).getSfi());
                    tmp+="100";
                    String tmp_hex=Long.toHexString(Long.parseLong(tmp,2));
                    tmp_hex.toUpperCase();
                    byte[] secondrecord=HexStringToByteArray(tmp_hex);
                    outputreadAppData.write(secondrecord);
                    outputreadAppData.write((byte) 0x00);
                    byte[] readAppDataCommand=outputreadAppData.toByteArray();
                    byte[] readAppDataResponse=provider.transceive(readAppDataCommand);
                    JSONObject jsonAppDataCommandResp=new JSONObject();
                    jsonAppDataCommandResp.put("cmd",ByteArrayToHexString(readAppDataCommand));
                    jsonAppDataCommandResp.put("resp",ByteArrayToHexString(readAppDataResponse));
                    jsonArrayApdus.put(jsonAppDataCommandResp);
                }
            }
            jsonEmvRequest.put("apdus",jsonArrayApdus);


            try {
                String xxx="{\n" +
                        "  \"not-found\" : \"6700\",\n" +
                        "  \"randoms\" : \"ABABABAB\",\n" +
                        "  \"date\" : \"190819\",\n" +
                        "  \"time\" : \"000000\",\n" +
                        "  \"transaction-req\" : \"9F02060000000002005F2A0208409C0100\",\n" +
                        "  \"apdus\": [\n" +
                        "    {\n" +
                        "      \"cmd\": \"00A404000E325041592E5359532E444446303100\",\n" +
                        "      \"resp\": \"6F2D840E325041592E5359532E4444463031A51BBF0C1861164F07A0000000031010500B56495341204352454449549000\"\n" +
                        "    },\n" +
                        "    { \"cmd\": \"00A4040007A000000003101000\",\n" +
                        "      \"resp\": \"6F278407A0000000031010A51C500B56495341204352454449549F380C9F66049F02065F2A029F37049000\"\n" +
                        "    },\n" +
                        "    { \"cmd\": \"80A80000128310240040000000000002000840ABABABAB00\",\n" +
                        "      \"resp\" : \"7781B69F4B7007C80CE709D528ABC39618AE71BB80BDA976AC4F220A2E77D96A03615C5ED6CCC18FB6F72F8D229C72CACEC29FBF1F1B233248F9D961E1887D009DC9AC146963137CC76ECC69C74399B339EEAC184B839D9220EAECCBA56774DD86EF74F07028DB37DD5EB621814BA71B7F8BB6AAE0F4940C1801010110010100100303009F10070601110390000057104761739001010010D191220111438825820220009F360200159F260820261203C3227D269F6C0200009000\"\n" +
                        "    },\n" +
                        "    { \"cmd\": \"00B2011C00\",\n" +
                        "      \"resp\": \"70145A0847617390010100105F24031912315F3401019000\"\n" +
                        "    },\n" +
                        "    { \"cmd\": \"00B2011400\",\n" +
                        "      \"resp\": \"7081C08F01929081B05FACBFDA21EE8128D057A3BBA9EB5971EBE09B3095C9F4623D62417149F33B20CC596DD24F97C005AC15449C56D5B26DFA93C5EA23BFA04A3B92BFD0569F4521B7528781BB3C067544042734F85AE9BD1753825805AC4255CE8885F7565F1B65BE4517AA149D72EA548440F7C462BB77F76642344BBC616370C317E73542D78ED234AD84C59E2CD2323F22AA36A2835C8054C6607433CC65307ED05D787667DF32483BD6507EA6861B11BE8345F5656C92044B76E0F79F3201039000\"\n" +
                        "    },\n" +
                        "    { \"cmd\": \"00B2031400\",\n" +
                        "      \"resp\" : \"7081AD9F468190A7805CB24AC17B36F81F753EA9D37F1D0578DF95F6979C1FA4EE505B712C1A31C19FE5E3B8A8C1E47A560DF17C056E8FDB53D81A2648589CE9F3F1E16F229D200CCD6A780573871D244D4A90C6ABB596EC55AD1E6596443B3748C752B878605D62DCD2A8F7E5A8E0CBFF63C60740A8338F0952782D683B4AE8E70DCD03D94730D566267E89DB20C514A20757FD1AE4079F4701039F480A761CCBE81F44003478E99F69050169C16C999000\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}\n" +
                        "\n";
                String rootPath =getCacheDir().getAbsolutePath();
                File f = new File(rootPath + "newTransaction.json");

                if (f.exists()) {
                    f.delete();
                }
                f.createNewFile();
                FileOutputStream out = new FileOutputStream(f);
                out.write(jsonEmvRequest.toString().getBytes());
                //out.write(xxx.getBytes());
                out.flush();
                out.close();

                RequestBody requestFile = RequestBody.create(f,MediaType.parse("multipart/form-data"));
                MultipartBody.Part multipartBody = MultipartBody.Part.createFormData("transactionReqFile",f.getName(),requestFile);

                try {

                    Service.getTransactionRequestService_instance()
                            .transactionRequest(multipartBody,token)
                            .enqueue(new Callback<TransactionReqInfo>() {
                                @Override
                                public void onResponse(Call<TransactionReqInfo> call, Response<TransactionReqInfo> response) {
                                    if(response.isSuccessful()){

                                        if(response.body().transaction_response.equals("Approved")){
                                            imageTapToPay.setVisibility(View.INVISIBLE);
                                            progressBar.setVisibility(View.VISIBLE);
                                            final int[] i = {0};
                                            final Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (i[0] <= 100) {
                                                        progressBar.setProgress(i[0]);
                                                        i[0]+=7;
                                                        handler.postDelayed(this, 200);
                                                    } else {
                                                        handler.removeCallbacks(this);
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        imageApprove.setVisibility(View.VISIBLE);
                                                        approveText.setVisibility(View.VISIBLE);
                                                        final Handler innerHandler=new Handler();
                                                        handler.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                AlertDialog.Builder builder = new AlertDialog.Builder(TapToPay.this);
                                                                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(TapToPay.this);
                                                                View view2 = layoutInflaterAndroid.inflate(R.layout.popuplayout, null);
                                                                builder.setView(view2);
                                                                //builder.setCancelable(false);
                                                                final AlertDialog alertDialog = builder.create();
                                                                alertDialog.show();

                                                                Button submit=alertDialog.findViewById(R.id.submitButton);
                                                                EditText enteredMail=alertDialog.findViewById(R.id.enteredMail1);
                                                                submit.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view) {
                                                                        try {

                                                                            Service.MailInstance()
                                                                                    .sendEmail(new MailInfo(enteredMail.getText().toString(),paymentAmountStr+" TL",getDatetime()),token)
                                                                                    .enqueue(new Callback<Void>() {
                                                                                        @Override
                                                                                        public void onResponse(Call<Void> call, Response<Void> response) {
                                                                                            if(response.isSuccessful()){

                                                                                            }

                                                                                            Toast.makeText(getBaseContext(), "Email is sent", Toast.LENGTH_LONG).show();
                                                                                            alertDialog.dismiss();
                                                                                            onBackPressed();
                                                                                        }

                                                                                        @Override
                                                                                        public void onFailure(Call<Void> call, Throwable t) {
                                                                                            Toast.makeText(getBaseContext(), "service error", Toast.LENGTH_LONG).show();

                                                                                        }
                                                                                    });

                                                                        }catch (Exception e){
                                                                            e.printStackTrace();
                                                                            Toast.makeText(getBaseContext(), "error!!!", Toast.LENGTH_LONG).show();
                                                                        }

                                                                    }
                                                                });
                                                                try {

                                                                    Service.paymentInstance()
                                                                            .postPayment(id,new PaymentInfo(Float.parseFloat(paymentAmountStr),"Approved","192.168.2.2",getDatetime2()),token)
                                                                            .enqueue(new Callback<Void>() {
                                                                                @Override
                                                                                public void onResponse(Call<Void> call, Response<Void> response) {
                                                                                    Toast.makeText(getBaseContext(), "Payment is done", Toast.LENGTH_LONG).show();
                                                                                    //transactionInfos.clear();

                                                                                }

                                                                                @Override
                                                                                public void onFailure(Call<Void> call, Throwable t) {
                                                                                    Toast.makeText(getBaseContext(), "service error", Toast.LENGTH_LONG).show();

                                                                                }
                                                                            });

                                                                }
                                                                catch (Exception e){
                                                                    e.printStackTrace();
                                                                    Toast.makeText(getBaseContext(), "error!!!", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        },2000);

                                                    }
                                                }
                                            }, 200);


                                        }
                                        else{
                                            imageTapToPay.setVisibility(View.INVISIBLE);
                                            progressBar.setVisibility(View.VISIBLE);
                                            final int[] i = {0};
                                            final Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (i[0] <= 100) {
                                                        progressBar.setProgress(i[0]);
                                                        i[0]+=7;
                                                        handler.postDelayed(this, 200);
                                                    }
                                                    else{
                                                        handler.removeCallbacks(this);
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        rejectText.setText(response.body().transaction_response+"...");
                                                        rejectText.setVisibility(View.VISIBLE);
                                                        try {
                                                            Service.paymentInstance()
                                                                    .postPayment(id,new PaymentInfo(Float.parseFloat(paymentAmountStr),response.body().transaction_response,"192.168.2.2",getDatetime2()),token)
                                                                    .enqueue(new Callback<Void>() {
                                                                        @Override
                                                                        public void onResponse(Call<Void> call, Response<Void> response) {
                                                                            final Handler innerHandler=new Handler();
                                                                            innerHandler.postDelayed(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    onBackPressed();
                                                                                }
                                                                            },2000);
                                                                            //transactionInfos.clear();
                                                                        }

                                                                        @Override
                                                                        public void onFailure(Call<Void> call, Throwable t) {
                                                                            Toast.makeText(getBaseContext(), "service error", Toast.LENGTH_LONG).show();

                                                                        }
                                                                    });

                                                        }catch (Exception e){
                                                            e.printStackTrace();
                                                            Toast.makeText(getBaseContext(), "error!!!", Toast.LENGTH_LONG).show();
                                                        }

                                                    }
                                                }
                                            },200);
                                        }
                                    }

                                }


                                @Override
                                public void onFailure(Call<TransactionReqInfo> call, Throwable t) {
                                    Toast.makeText(getBaseContext(), "service error", Toast.LENGTH_LONG).show();


                                }
                            });
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), "error!!!", Toast.LENGTH_LONG).show();
                }


            }catch (IOException e){
                e.printStackTrace();
            }


            isoDep.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }



    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2]; // Each byte has two hex characters (nibbles)
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned value
            hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from upper nibble
            hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character from lower nibble
        }
        return new String(hexChars);
    }

    public static byte[] HexStringToByteArray(String s) throws IllegalArgumentException {
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }
        byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
        for (int i = 0; i < len; i += 2) {
            // Convert each character into a integer (base-16), then bit-shift into place
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    public static String getDatetime() {
        Calendar c = Calendar .getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyMMdd");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }
    public byte[] constructgpo(final TagAndLength pTagAndLength) {
        byte ret[] = new byte[pTagAndLength.getLength()];
        byte val[] = null;
        if (pTagAndLength.getTag() == EmvTags.TERMINAL_TRANSACTION_QUALIFIERS) {
            val = new byte[]{(byte) 0x24,(byte) 0x00, (byte) 0x40,(byte) 0x00};
        } else if (pTagAndLength.getTag() == EmvTags.TERMINAL_COUNTRY_CODE) {
            val = new byte[]{(byte) 0x08,(byte) 0x40};
        } else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_CURRENCY_CODE) {
            val = new byte[]{(byte) 0x09,(byte) 0x49};
        } else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_DATE) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
            val = BytesUtils.fromString(sdf.format(new Date()));
        } else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_TYPE || pTagAndLength.getTag() == EmvTags.TERMINAL_TRANSACTION_TYPE) {
            val = new byte[] { (byte) TransactionTypeEnum.PURCHASE.getKey() };
        } else if (pTagAndLength.getTag() == EmvTags.AMOUNT_AUTHORISED_NUMERIC) {
            val = BytesUtils.fromString("01");
        } else if (pTagAndLength.getTag() == EmvTags.TERMINAL_TYPE) {
            val = new byte[] { 0x22 };
        } else if (pTagAndLength.getTag() == EmvTags.TERMINAL_CAPABILITIES) {
            val = new byte[] { (byte) 0xE0, (byte) 0xA0, 0x00 };
        } else if (pTagAndLength.getTag() == EmvTags.ADDITIONAL_TERMINAL_CAPABILITIES) {
            val = new byte[] { (byte) 0x8e, (byte) 0, (byte) 0xb0, 0x50, 0x05 };
        } else if (pTagAndLength.getTag() == EmvTags.DS_REQUESTED_OPERATOR_ID) {
            val = BytesUtils.fromString("7A45123EE59C7F40");
        } else if (pTagAndLength.getTag() == EmvTags.UNPREDICTABLE_NUMBER) {
            val = new byte[]{(byte) 0xAB,(byte) 0xAB, (byte) 0xAB,(byte) 0xAB};
        } else if (pTagAndLength.getTag() == EmvTags.MERCHANT_TYPE_INDICATOR) {
            val = new byte[] { 0x01 };
        } else if (pTagAndLength.getTag() == EmvTags.TERMINAL_TRANSACTION_INFORMATION) {
            val = new byte[] { (byte) 0xC0, (byte) 0x80, 0 };
        }


        if (val != null) {
            System.arraycopy(val, 0, ret, Math.max(ret.length - val.length, 0), Math.min(val.length, ret.length));
        }
        return ret;
    }
    public String convertBinary(int num){
        int[] binary = new int[40];
        String binaryStr="";
        int index = 0;
        while(num > 0){
            binary[index++] = num%2;
            num = num/2;
        }
        for(int i = index-1;i >= 0;i--){
            binaryStr+=binary[i];
        }
        return binaryStr;
    }
    public static String getDatetime2() {
        Calendar c = Calendar .getInstance();
        System.out.println("Current time => "+c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}