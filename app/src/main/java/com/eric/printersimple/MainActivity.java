package com.eric.printersimple;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.eric.printersimple.databinding.ActivityMainBinding;
import com.newland.sdk.me.module.printer.ErrorCode;
import com.newland.sdk.me.module.printer.ModuleManage;
import com.newland.sdk.me.module.printer.PrintListener;
import com.newland.sdk.me.module.printer.PrinterModule;
import com.newland.sdk.me.module.printer.PrinterStatus;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding mBinding;

    private PrinterModule mPrinterModule;

    private StringBuffer mResultText = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        //Must init first
        mBinding.btnInit.setOnClickListener(view -> {
            ModuleManage.getInstance().init();
            mPrinterModule = ModuleManage.getInstance().getPrinterModule();
            runOnUiThread(() -> displayResult("Init Success!\n"));
        });

        mBinding.btnPrint.setOnClickListener(view -> {
            if (mPrinterModule == null) {
                runOnUiThread(() -> displayResult("Please init first\n"));
                return;
            }
            printTest();
        });
    }

    private void printTest() {
        //Obtain Status of Printer
        PrinterStatus printerStatus = mPrinterModule.getStatus();
        if (!PrinterStatus.NORMAL.name().equals(printerStatus.name())) {
            new Handler(Looper.getMainLooper()).post(() -> displayResult("Status:" + printerStatus.name() + "\n"));
            return;
        }
        //Assembly Data
        StringBuffer printDara = new StringBuffer();
        String fontsPath = mPrinterModule.setFont(this, "simsun.ttc");
        if (fontsPath != null) {
            //Set Font
            printDara.append("!font " + fontsPath + "\n");
            //Set concentration
            printDara.append("!gray 8\n");
        }
        Map<String, Bitmap> bitmaps = new HashMap<>();
        Bitmap logo = BitmapFactory.decodeResource(this.getResources(), R.drawable.logo);
        String bitmapName1 = "logo";
        bitmaps.put(bitmapName1, logo);
        //Image centered
        printDara.append("*image c 370*120 path:" + bitmapName1 + "\n");
        //Set Font Size,small
        printDara.append("!hz s\n!asc s\n");
        //text-align left
        printDara.append("!NLFONT 15 15 3\n*text l MID:123456789012345\n");
        printDara.append("!NLFONT 15 15 3\n*text l TID:12345678\n");
        //text-align center ，small
        printDara.append("!NLFONT 10 22 3\n*text c ------------------\n");
        //card number
        printDara.append("!NLFONT 6 36 0\n*text c 621669********1111\n");
        printDara.append("!NLFONT 15 15 3\n*TEXT l BATCH NO.:\n!NLFONT 15 15 3\n*text r 000002\n");
        printDara.append("!NLFONT 15 15 3\n*TEXT l VOUCHER NO.:\n!NLFONT 15 15 3\n*text r 000045\n");
        printDara.append("!NLFONT 15 15 3\n*TEXT l Auth NO.:\n!NLFONT 15 15 3\n*text r 123456\n");
        //Amount
        printDara.append("!NLFONT 6 36 0\n*TEXT l Amount:\n!NLFONT 6 36 0\n*text c $\n!NLFONT 6 36 0\n*text r 123456\n");
        printDara.append("!NLFONT 10 22 3\n*text c ------------------\n");
        //small
        printDara.append("!NLFONT 6 1 3\n*text l AID:A0000003301101\n");
        printDara.append("!NLFONT 6 1 3\n*text l TSI:0000\n");
        //Barcode
        printDara.append("!BARCODE 8 120 1 3\n*BARCODE c ABC123456123\n");
        //QRCODE
        printDara.append("!QRCODE 300 0 3\n*QRCODE c Test123456788\n");
        //Skip line
        printDara.append("*feedline 8\n");


        mPrinterModule.print(printDara.toString(), bitmaps, new PrintListener() {
            @Override
            public void onSuccess() {
                displayResult("Print Success!\n");
            }

            @Override
            public void onError(ErrorCode errorCode, String s) {
                displayResult(errorCode + ":" + s + "\n");
            }
        });

    }

    private void displayResult(String item) {
        mResultText.append(item);
        MainActivity.this.runOnUiThread(() -> mBinding.tvResult.setText(mResultText.toString()));
    }
}