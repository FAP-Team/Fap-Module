:: Install FAP Application as windows service
::@echo off
setlocal

set APP_NAME=${app.name}
set APP_PATH=${app.path}
set PLAY_PATH=${play.path}

rem Install service
${prunsrv} //IS/fap%APP_NAME% --Description "fap:%APP_NAME%" --StartMode=exe --StartImage=%PLAY_PATH%\play.bat --StopMode=exe --StopImage=%PLAY_PATH%\play.bat --StartParams=start;%APP_PATH%;--%%prod;-Dprecompiled=true  --StopParams=stop;%APP_PATH% --LogPath=%APP_PATH%\logs
endlocal
EXIT /B