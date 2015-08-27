package com.apicloud.moduleDemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

public class PhotoGet extends UZModule {

	static final int ACTIVITY_REQUEST_CODE_A = 100;
	private Bitmap photo;
	private String picPath;
	private File delFile;
	private String path;
	private File relPath;
	JSONObject ret;
	UZModuleContext jsContext;


	public PhotoGet(UZWebView webView) {
		super(webView);
	}

	

	public void jsmethod_takePhoto(final UZModuleContext moduleContext) {
		this.jsContext = moduleContext;
		path = moduleContext.optString("path");
		ret = new JSONObject();
		doPhoto();

	}


	public void jsmethod_delPhoto(final UZModuleContext moduleContext) {
		Log.e("sdfa", "sdfdsaf");
		String delPath = moduleContext.optString("delPath");		
		relPath = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera");
		Log.e("out", delPath+"1");
		if(delPath!=""){
			delFile = new File(Environment.getExternalStorageDirectory() + "/"+ delPath);
			Log.e("in", delPath+"2");
			deleteFile(delFile);
		}
		deleteFile(relPath);
		
	}

	public void doPhoto() {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
			startActivityForResult(intent, 1);
		} else {
			try {
				ret.put("state", "无内存");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Uri uri = data.getData();
		if (uri != null) {
			this.photo = BitmapFactory.decodeFile(uri.getPath());
		}
		if (this.photo == null) {
			Bundle bundle = data.getExtras();
			if (bundle != null) {
				this.photo = (Bitmap) bundle.get("data");
			} else {
				try {
					ret.put("state", "拍照失败");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
		}

		FileOutputStream fileOutputStream = null;
		try {
			// 获取 SD 卡根目录
			String saveDir = Environment.getExternalStorageDirectory() + "/"
					+ path;
			delFile = new File(saveDir);
			// 新建目录
			File dir = new File(saveDir);
			if (!dir.exists())
				dir.mkdir();
			// 生成文件名
			SimpleDateFormat t = new SimpleDateFormat("yyyyMMdd_HHmmSS");
			Date    curDate    =   new    Date(System.currentTimeMillis());
			String filename = "IMG_" + (t.format(curDate)) + ".jpg";
			// 新建文件
			File file = new File(saveDir, filename);
			// 打开文件输出流
			fileOutputStream = new FileOutputStream(file);
			// 生成图片文件
			this.photo.compress(Bitmap.CompressFormat.JPEG, 100,
					fileOutputStream);
			// 相片的完整路径
			this.picPath = file.getPath();
			Log.e("path", picPath);
			ret.put("picPath", picPath);
			jsContext.success(ret, true);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 销毁图片文件
	 */
	public void deleteFile(File file) {
		mContext.getContentResolver().delete(Media.EXTERNAL_CONTENT_URI, Media.DATA + "=?",new String[]{file.getAbsolutePath()});
		if (file.exists() == false) {
			return;
		} else {
			if (file.isFile()) {
				file.delete();
				return;
			}
			if (file.isDirectory()) {
				File[] childFile = file.listFiles();
				if (childFile == null || childFile.length == 0) {
					file.delete();
					return;
				}
				for (File f : childFile) {
					deleteFile(f);
				}
				file.delete();
			}
		}
	}
	


}
