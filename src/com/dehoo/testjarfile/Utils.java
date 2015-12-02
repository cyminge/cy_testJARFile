package com.dehoo.testjarfile;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.http.util.EncodingUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Environment;
import android.util.Log;

public class Utils {

	private static final String TAG = "cyTest";

	private static final Object mSync = new Object();
	private static WeakReference<byte[]> mReadBuffer;

	/**
	 * 加载证书
	 * 
	 * @param jarFile
	 * @param je
	 * @param readBuffer
	 * @return
	 */
	private Certificate[] loadCertificates(JarFile jarFile, JarEntry je, byte[] readBuffer) {
		try {
			// We must read the stream for the JarEntry to retrieve
			// its certificates.
			InputStream is = new BufferedInputStream(jarFile.getInputStream(je));
			while (is.read(readBuffer, 0, readBuffer.length) != -1) {
				// not using
			}
			is.close();
			return je != null ? je.getCertificates() : null;
		} catch (IOException e) {
			Log.w(TAG, "Exception reading " + je.getName() + " in " + jarFile.getName(), e);
		} catch (RuntimeException e) {
			Log.w(TAG, "Exception reading " + je.getName() + " in " + jarFile.getName(), e);
		}
		return null;
	}

	/**
	 * 验证数字签名
	 */
	public String verifyDigitalSigned(String url) {

		WeakReference<byte[]> readBufferRef;
		byte[] readBuffer = null;
		synchronized (mSync) {
			readBufferRef = mReadBuffer;
			if (readBufferRef != null) {
				mReadBuffer = null;
				readBuffer = readBufferRef.get();
			}
			if (readBuffer == null) {
				readBuffer = new byte[8192];
				readBufferRef = new WeakReference<byte[]>(readBuffer);
			}
		}
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Certificate[] certs = null;
		Enumeration<JarEntry> entries = jarFile.entries();
		try {
			final Manifest manifest = jarFile.getManifest();
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (entries.hasMoreElements()) {
			final JarEntry je = entries.nextElement();
			if (je.isDirectory())
				continue;

			final String name = je.getName();
			if (name.startsWith("META-INF/")) {
				continue;
			}
			final Certificate[] localCerts = loadCertificates(jarFile, je, readBuffer);
			if (localCerts == null) {
				try {
					jarFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (certs == null) {
				certs = localCerts;
			} else {
				// Ensure all certificates match.
				for (int i = 0; i < certs.length; i++) {
					boolean found = false;
					for (int j = 0; j < localCerts.length; j++) {
						if (certs[i] != null && certs[i].equals(localCerts[j])) {
							found = true;
							break;
						}
					}
					if (!found || certs.length != localCerts.length) {
						try {
							jarFile.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		try {
			jarFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		synchronized (mSync) {
			mReadBuffer = readBufferRef;
		}
		if (certs != null && certs.length > 0) {
			final int N = certs.length;
			for (int i = 0; i < N; i++) {

				// Log.i(TAG, "  Public key: " +
				// certs[i].getPublicKey().getEncoded() + " ------------ " +
				// certs[i].getPublicKey());
				Log.i(TAG, "  Public key : " + certs[i].getPublicKey() + " ------------ ");
				return certs[i].getPublicKey().toString();
				// pkg.mSignatures[i] = new Signature(certs[i].getEncoded());
			}
		}
		return null;
	}

	// 从assets 文件夹中获取文件并读取数据
	public String getFromAssets(Context context, String fileName) {
		String result = "";
		try {
			InputStream in = context.getResources().getAssets().open(fileName);
			// 获取文件的字节数
			int lenght = in.available();
			// 创建byte数组
			byte[] buffer = new byte[lenght];
			// 将文件中的数据读到byte数组中
			in.read(buffer);
			result = EncodingUtils.getString(buffer, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 写入文件夹
	 * 
	 * @param context
	 * @param str
	 */
	public void write2Assets(Context context, String str) {
		OutputStream os = null;
		try {
			os = context.openFileOutput("cyminge.txt", Activity.MODE_PRIVATE);
			os.write(str.getBytes("utf-8"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 写入文件夹
	 * 
	 * @param context
	 * @param str
	 */
	public void write2sdcard(String url, String publicKey) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(new File(url));
			// 获取文件的字节数
			// int lenght = publicKey.length();
			// 创建byte数组
			// byte[] buffer = new byte[lenght];
			os.write(publicKey.getBytes("utf-8"));
			os.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将字符串写入文件
	 * 
	 * @param url
	 * @param publicKey
	 */
	public void writeString2sdcard(String url, String publicKey) {

		File file = new File(url);
		try {
			FileWriter filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
			BufferedWriter bufWriter = new BufferedWriter(filerWriter);
			bufWriter.write(publicKey);
			bufWriter.newLine();
			bufWriter.close();
			filerWriter.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * 获取系统所有应用
	 * 
	 * @param context
	 * @return
	 */
	public List<PackageInfo> getAllAppsPackage(Context context) {
		List<PackageInfo> apps = new ArrayList<PackageInfo>();
		PackageManager pManager = context.getPackageManager();
		List<PackageInfo> packlist = pManager.getInstalledPackages(0);
		for (int i = 0; i < packlist.size(); i++) {
			PackageInfo pak = (PackageInfo) packlist.get(i);
			// if()里的值如果<=0则为自己装的程序，否则为系统工程自带
			// if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM)
			// <= 0) {
			apps.add(pak);
			// }

		}
		return apps;
	}



	/**
	 * 获取非系统应用
	 * @param context
	 * @return
	 */
	public List<ResolveInfo> getAllAppsResolve(Context context) {
		List<ResolveInfo> resolveInfoList = null;
		PackageManager pManager = context.getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveInfoList = pManager.queryIntentActivities(mainIntent, 0);
		// 获取应用程序包名，程序名称，程序图标
		// Resolve res = resInfo.get(position);
		// holder.appIcon.setImageDrawable(res.loadIcon(mPackageManager));
		// holder.tvAppLabel.setText(res.loadLabel(mPackageManager).toString());
		// holder.tvPkgName.setText(res.activityInfo.packageName+'\n'+res.activityInfo.name);
		for(int i=0; i<resolveInfoList.size(); i++){
			Log.d(TAG, "resolveInfo.packageName = " + resolveInfoList.get(i).activityInfo.packageName);
		}
		
		return resolveInfoList;
	}
	
	/**
	 * 用来存储获取的应用信息数据　　
	 * 
	 * @param context
	 * @return
	 */
	public List<AppInfo> getAllAppsInfo(Context context) {
		ArrayList<AppInfo> appList = new ArrayList<AppInfo>();
		List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);

		for (int i = 0; i < packages.size(); i++) {
			PackageInfo packageInfo = packages.get(i);
			AppInfo tmpInfo = new AppInfo();
			tmpInfo.appName = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
			tmpInfo.packageName = packageInfo.packageName;
			tmpInfo.versionName = packageInfo.versionName;
			tmpInfo.versionCode = packageInfo.versionCode;
			tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(context.getPackageManager());
			
			Log.d(TAG, "packageInfo.packageName = " + packageInfo.packageName);
//			Log.d(TAG, "packageInfo.versionName = " + packageInfo.versionName);
//			Log.d(TAG, "packageInfo.versionCode = " + packageInfo.versionCode);
			
			// 设置图片 应用程序名字 应用程序的包名
			// shareItem.setIcon(pManager.getApplicationIcon(pinfo.applicationInfo));
			// shareItem.setLabel(pManager.getApplicationLabel(pinfo.applicationInfo).toString());
			// shareItem.setPackageName(pinfo.applicationInfo.packageName);
			appList.add(tmpInfo);

		}
		return appList;
	}

	// 通过 PackageInfo 获取具体信息方法：
	//
	// 包名获取方法：packageInfo.packageName
	// icon获取获取方法：packageManager.getApplicationIcon(applicationInfo)
	// 应用名称获取方法：packageManager.getApplicationLabel(applicationInfo)
	// 使用权限获取方法：packageManager.getPackageInfo(packageName,PackageManager.GET_PERMISSIONS)
	// .requestedPermissions
	//
	// 通过 ResolveInfo 获取具体信息方法：
	//
	// 包名获取方法：resolve.activityInfo.packageName
	// icon获取获取方法：resolve.loadIcon(packageManager)
	// 应用名称获取方法：resolve.loadLabel(packageManager).toString()
}
