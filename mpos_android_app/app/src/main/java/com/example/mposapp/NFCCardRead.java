package com.example.mposapp;


import android.nfc.tech.IsoDep;
import android.util.Log;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.parser.impl.EmvParser;

import java.io.IOException;

public class NFCCardRead implements IProvider {
    private IsoDep mTagCom;

    public NFCCardRead(IsoDep mTagCom) {
        this.mTagCom = mTagCom;
    }

    @Override
    public byte[] transceive(final byte[] pCommand) throws CommunicationException {

        byte[] response;
        byte[] result;
        String r;
        try {
            // send command to emv card
            response = mTagCom.transceive(pCommand);
            //result = mTagCom.transceive(HexStringToByteArray("00B2010C00"));
            //r=ByteArrayToHexString(result);
        } catch (IOException e) {
            throw new CommunicationException(e.getMessage());
        }
        //Log.d("Result:",r);
        return response;
    }

    @Override
    public byte[] getAt() {
        // For NFC-A
        return mTagCom.getHistoricalBytes();
        // For NFC-B
        // return mTagCom.getHiLayerResponse();
    }



}
