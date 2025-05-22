package com.example.mobile_termproject.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RecipeResponse implements Serializable {
    public CookRcp COOKRCP01;

    public static class CookRcp {
        public List<RecipeData> row;
    }

    public static class RecipeData {
        public String RCP_SEQ;
        public String RCP_NM;
        public String RCP_WAY2;
        public String RCP_PAT2;
        public String INFO_WGT;
        public String INFO_ENG;
        public String INFO_CAR;
        public String INFO_PRO;
        public String INFO_FAT;
        public String INFO_NA;
        public String HASH_TAG;
        public String ATT_FILE_NO_MAIN;
        public String ATT_FILE_NO_MK;
        public String RCP_PARTS_DTLS;
        public String RCP_NA_TIP;

        public String MANUAL01, MANUAL02, MANUAL03, MANUAL04, MANUAL05,
                MANUAL06, MANUAL07, MANUAL08, MANUAL09, MANUAL10,
                MANUAL11, MANUAL12, MANUAL13, MANUAL14, MANUAL15,
                MANUAL16, MANUAL17, MANUAL18, MANUAL19, MANUAL20;

        public String MANUAL_IMG01, MANUAL_IMG02, MANUAL_IMG03, MANUAL_IMG04, MANUAL_IMG05,
                MANUAL_IMG06, MANUAL_IMG07, MANUAL_IMG08, MANUAL_IMG09, MANUAL_IMG10,
                MANUAL_IMG11, MANUAL_IMG12, MANUAL_IMG13, MANUAL_IMG14, MANUAL_IMG15,
                MANUAL_IMG16, MANUAL_IMG17, MANUAL_IMG18, MANUAL_IMG19, MANUAL_IMG20;

        public List<String> getManualList() {
            List<String> manuals = new ArrayList<>();
            if (MANUAL01 != null && !MANUAL01.trim().isEmpty()) manuals.add(MANUAL01);
            if (MANUAL02 != null && !MANUAL02.trim().isEmpty()) manuals.add(MANUAL02);
            if (MANUAL03 != null && !MANUAL03.trim().isEmpty()) manuals.add(MANUAL03);
            if (MANUAL04 != null && !MANUAL04.trim().isEmpty()) manuals.add(MANUAL04);
            if (MANUAL05 != null && !MANUAL05.trim().isEmpty()) manuals.add(MANUAL05);
            if (MANUAL06 != null && !MANUAL06.trim().isEmpty()) manuals.add(MANUAL06);
            if (MANUAL07 != null && !MANUAL07.trim().isEmpty()) manuals.add(MANUAL07);
            if (MANUAL08 != null && !MANUAL08.trim().isEmpty()) manuals.add(MANUAL08);
            if (MANUAL09 != null && !MANUAL09.trim().isEmpty()) manuals.add(MANUAL09);
            if (MANUAL10 != null && !MANUAL10.trim().isEmpty()) manuals.add(MANUAL10);
            if (MANUAL11 != null && !MANUAL11.trim().isEmpty()) manuals.add(MANUAL11);
            if (MANUAL12 != null && !MANUAL12.trim().isEmpty()) manuals.add(MANUAL12);
            if (MANUAL13 != null && !MANUAL13.trim().isEmpty()) manuals.add(MANUAL13);
            if (MANUAL14 != null && !MANUAL14.trim().isEmpty()) manuals.add(MANUAL14);
            if (MANUAL15 != null && !MANUAL15.trim().isEmpty()) manuals.add(MANUAL15);
            if (MANUAL16 != null && !MANUAL16.trim().isEmpty()) manuals.add(MANUAL16);
            if (MANUAL17 != null && !MANUAL17.trim().isEmpty()) manuals.add(MANUAL17);
            if (MANUAL18 != null && !MANUAL18.trim().isEmpty()) manuals.add(MANUAL18);
            if (MANUAL19 != null && !MANUAL19.trim().isEmpty()) manuals.add(MANUAL19);
            if (MANUAL20 != null && !MANUAL20.trim().isEmpty()) manuals.add(MANUAL20);
            return manuals;
        }

        public List<String> getManualImageList() {
            List<String> images = new ArrayList<>();
            if (MANUAL_IMG01 != null && !MANUAL_IMG01.trim().isEmpty()) images.add(MANUAL_IMG01);
            if (MANUAL_IMG02 != null && !MANUAL_IMG02.trim().isEmpty()) images.add(MANUAL_IMG02);
            if (MANUAL_IMG03 != null && !MANUAL_IMG03.trim().isEmpty()) images.add(MANUAL_IMG03);
            if (MANUAL_IMG04 != null && !MANUAL_IMG04.trim().isEmpty()) images.add(MANUAL_IMG04);
            if (MANUAL_IMG05 != null && !MANUAL_IMG05.trim().isEmpty()) images.add(MANUAL_IMG05);
            if (MANUAL_IMG06 != null && !MANUAL_IMG06.trim().isEmpty()) images.add(MANUAL_IMG06);
            if (MANUAL_IMG07 != null && !MANUAL_IMG07.trim().isEmpty()) images.add(MANUAL_IMG07);
            if (MANUAL_IMG08 != null && !MANUAL_IMG08.trim().isEmpty()) images.add(MANUAL_IMG08);
            if (MANUAL_IMG09 != null && !MANUAL_IMG09.trim().isEmpty()) images.add(MANUAL_IMG09);
            if (MANUAL_IMG10 != null && !MANUAL_IMG10.trim().isEmpty()) images.add(MANUAL_IMG10);
            if (MANUAL_IMG11 != null && !MANUAL_IMG11.trim().isEmpty()) images.add(MANUAL_IMG11);
            if (MANUAL_IMG12 != null && !MANUAL_IMG12.trim().isEmpty()) images.add(MANUAL_IMG12);
            if (MANUAL_IMG13 != null && !MANUAL_IMG13.trim().isEmpty()) images.add(MANUAL_IMG13);
            if (MANUAL_IMG14 != null && !MANUAL_IMG14.trim().isEmpty()) images.add(MANUAL_IMG14);
            if (MANUAL_IMG15 != null && !MANUAL_IMG15.trim().isEmpty()) images.add(MANUAL_IMG15);
            if (MANUAL_IMG16 != null && !MANUAL_IMG16.trim().isEmpty()) images.add(MANUAL_IMG16);
            if (MANUAL_IMG17 != null && !MANUAL_IMG17.trim().isEmpty()) images.add(MANUAL_IMG17);
            if (MANUAL_IMG18 != null && !MANUAL_IMG18.trim().isEmpty()) images.add(MANUAL_IMG18);
            if (MANUAL_IMG19 != null && !MANUAL_IMG19.trim().isEmpty()) images.add(MANUAL_IMG19);
            if (MANUAL_IMG20 != null && !MANUAL_IMG20.trim().isEmpty()) images.add(MANUAL_IMG20);
            return images;
        }
    }
}
