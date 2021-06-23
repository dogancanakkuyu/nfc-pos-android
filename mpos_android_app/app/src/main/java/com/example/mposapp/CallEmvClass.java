package com.example.mposapp;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.Afl;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.parser.impl.EmvParser;

import java.util.List;

public class CallEmvClass extends EmvParser {

    /**
     * Default constructor
     *
     * @param pTemplate parser template
     */
    public CallEmvClass(EmvTemplate pTemplate) {
        super(pTemplate);
    }

    public byte[] callgpo(byte [] pdol) throws CommunicationException {
        return getGetProcessingOptions(pdol);
    }
    public List<Afl> callAflList(byte [] afl){
        return extractAfl(afl);
    }
}
