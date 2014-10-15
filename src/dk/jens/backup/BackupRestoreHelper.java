package dk.jens.backup;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class BackupRestoreHelper
{
    final static String TAG = OAndBackup.TAG;
    public static int backup(Context context, File backupDir, AppInfo appInfo, ShellCommands shellCommands, int backupMode)
    {
        int ret = 0;
        File backupSubDir = new File(backupDir, appInfo.getPackageName());
        if(!backupSubDir.exists())
            backupSubDir.mkdirs();
        else if(appInfo.getSourceDir().length() > 0)
            shellCommands.deleteOldApk(backupSubDir, appInfo.getSourceDir());

        if(appInfo.isSpecial())
        {
            ret = shellCommands.backupSpecial(backupSubDir, appInfo.getLabel(), appInfo.getDataDir(), appInfo.getFilesList());
            appInfo.setBackupMode(AppInfo.MODE_DATA);
        }
        else
        {
            ret = shellCommands.doBackup(context, backupSubDir, appInfo.getLabel(), appInfo.getDataDir(), appInfo.getSourceDir(), backupMode);
            appInfo.setBackupMode(backupMode);
        }

        shellCommands.logReturnMessage(context, ret);
        LogFile.writeLogFile(backupSubDir, appInfo);
        return ret;
    }
    public static int restore(Context context, File backupDir, AppInfo appInfo, ShellCommands shellCommands, int mode)
    {
        int apkRet, restoreRet, permRet;
        apkRet = restoreRet = permRet = 0;
        File backupSubDir = new File(backupDir, appInfo.getPackageName());
        String apk = new LogFile(backupSubDir, appInfo.getPackageName()).getApk();
        String dataDir = appInfo.getDataDir();
        if((mode == AppInfo.MODE_APK || mode == AppInfo.MODE_BOTH) && apk != null && apk.length() > 0)
            apkRet = shellCommands.restoreApk(backupSubDir, appInfo.getLabel(), apk, appInfo.isSystem(), context.getApplicationInfo().dataDir);
        if(mode == AppInfo.MODE_DATA || mode == AppInfo.MODE_BOTH)
        {
            if(appInfo.isInstalled() || mode == AppInfo.MODE_BOTH)
            {
                if(appInfo.isSpecial())
                {
                    restoreRet = shellCommands.restoreSpecial(backupSubDir, appInfo.getLabel(), appInfo.getDataDir(), appInfo.getFilesList());
                    permRet = shellCommands.setPermissionsSpecial(appInfo.getDataDir(), appInfo.getFilesList());
                }
                else
                {
                    restoreRet = shellCommands.doRestore(context, backupSubDir, appInfo.getLabel(), appInfo.getPackageName(), appInfo.getLogInfo().getDataDir());

                    permRet = shellCommands.setPermissions(dataDir);
                }
            }
            else
            {
                Log.e(TAG, "cannot restore data without restoring apk, package is not installed: " + appInfo.getPackageName());
                apkRet = 1;
                shellCommands.writeErrorLog(appInfo.getPackageName(), context.getString(R.string.restoreDataWithoutApkError));
            }
        }
        shellCommands.logReturnMessage(context, apkRet + restoreRet + permRet);
        return apkRet + restoreRet + permRet;
    }
}