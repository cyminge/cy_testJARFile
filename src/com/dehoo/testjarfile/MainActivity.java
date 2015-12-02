package com.dehoo.testjarfile;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		
		Utils util = new Utils();
//		util.getAllAppsResolve(this);
		util.getAllAppsInfo(this); // 获取所有应用
		
//		String assetPublicKey = util.getFromAssets(this, "publickey.txt");
//		Log.d("cyTest", "assetPublicKey = "+assetPublicKey);
		
//		String url = "/mnt/sdcard/cy_minge/PowerMTVALL.zip";
//		String url1 = "/mnt/sdcard/cy_minge/cyminge.txt";
//		String url = "/mnt/sdcard/cy_minge/tzt_mediavideo.zip";
//		String url = "/mnt/sdcard/cy_minge/test/origin-modify/tzt_mediavideo.zip";
//		String url = "/mnt/sdcard/cy_minge/test/zm/PowerMTVALL.zip";
		
//		String publicKey = util.verifyDigitalSigned(url);
//		publicKey = publicKey.trim();
//		Pattern p = Pattern.compile("\\s*|\t|\r|\n");  
//        Matcher m = p.matcher(publicKey);  
//        publicKey = m.replaceAll("");
//        Log.d("cyTest", "publicKey = "+publicKey);
        
//        util.write2sdcard(url1, publicKey);
   
//        if(assetPublicKey.equals(publicKey)){
//        	Log.d("cyTest", "公钥相同");
//        }else{
//        	Log.d("cyTest", "公钥不相同");
//        }
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
}
