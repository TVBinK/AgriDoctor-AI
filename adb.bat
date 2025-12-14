@echo off
echo ================================================
echo    ADB Reverse Forwarding Setup
echo ================================================
echo.

REM Tìm đường dẫn ADB
set ADB_PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe

REM Kiểm tra ADB có tồn tại không
if not exist "%ADB_PATH%" (
    echo [ERROR] ADB không tìm thấy tại: %ADB_PATH%
    echo Vui lòng kiểm tra đường dẫn Android SDK
    pause
    exit /b 1
)

echo [INFO] Đang kiểm tra thiết bị kết nối...
"%ADB_PATH%" devices
echo.

echo [INFO] Đang thiết lập ADB reverse forwarding...
echo Port: 3443 -^> 3443
"%ADB_PATH%" reverse tcp:3443 tcp:3443

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] ADB reverse forwarding đã được thiết lập thành công!
    echo.
    echo [INFO] Danh sách các port đã được forward:
    "%ADB_PATH%" reverse --list
    echo.
    echo [INFO] Bây giờ ứng dụng có thể kết nối đến: https://127.0.0.1:3443
) else (
    echo [ERROR] Không thể thiết lập ADB reverse forwarding
    echo Kiểm tra:
    echo   1. Điện thoại đã kết nối qua USB
    echo   2. USB debugging đã được bật
    echo   3. Đã cho phép USB debugging trên điện thoại
)

echo.
echo ================================================
pause

