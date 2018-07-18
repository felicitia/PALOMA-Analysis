FOR %%v in (*.apk) DO (
    adb install -r %%v
    start "" cmd /C "logging_util.bat %%~nv.logs.txt"
    timeout 2
    adb -e shell monkey --ignore-crashes -p %%~nv 3000
    taskkill /fi "WINDOWTITLE eq logging_util"
    adb shell pm clear %%~nv
    adb uninstall %%~nv
    timeout 2
)
pause