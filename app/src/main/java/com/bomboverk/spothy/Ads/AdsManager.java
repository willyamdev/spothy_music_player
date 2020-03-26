package com.bomboverk.spothy.Ads;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class AdsManager {

    //OFICIAL
    private String appID = "ca-app-pub-6089547548003012~5659500491";
    //ID DE TESTE
    //private String appID = "ca-app-pub-3940256099942544~3347511713";

    //INTERSTICAL TESTE
    //private String interID = "ca-app-pub-3940256099942544/1033173712";
    //INTERSTICAL OFICIAL
    private String interID = "ca-app-pub-6089547548003012/4810928031";

    private InterstitialAd interstitialAd;

    public AdsManager(Context context) {
        MobileAds.initialize(context, appID);
    }

    public void createInterstical(Context context){
        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(interID);
        interstitialAd.loadAd(new AdRequest.Builder().build());

        interstitialAd.setAdListener(new AdListener(){

            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                interstitialAd.show();
                super.onAdLoaded();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }
        });
    }
}
