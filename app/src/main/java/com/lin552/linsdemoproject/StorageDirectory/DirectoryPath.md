
内部存储地址
String fileDir = this.getFilesDir().getAbsolutePath();   
String cacheDir = this.getCacheDir().getAbsolutePath();

fileDir:/data/user/0/com.sunsta.hong/files
cacheDir:/data/user/0/com.sunsta.hong/cache


方法	路径
Environment.getExternalStoragePublicDirectory(DIRECTORY_ALARMS)	/storage/sdcard0/Alarms
Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM)	/storage/sdcard0/DCIM
Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)	/storage/sdcard0/Download
Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES)	/storage/sdcard0/Movies
Environment.getExternalStoragePublicDirectory(DIRECTORY_MUSIC)	/storage/sdcard0/Music
Environment.getExternalStoragePublicDirectory(DIRECTORY_NOTIFICATIONS)	/storage/sdcard0/Notifications
Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES)	/storage/sdcard0/Pictures
Environment.getExternalStoragePublicDirectory(DIRECTORY_PODCASTS)	/storage/sdcard0/Podcasts
Environment.getExternalStoragePublicDirectory(DIRECTORY_RINGTONES)	/storage/sdcard0/Ringtones