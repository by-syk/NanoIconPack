@echo off
title NanoIconPackTool-ResInjection
set "projectDir=E:\Android\CoreProjects\NanoIconPack\"
start http://nano.by-syk.com/page/query
:main
java -jar ResInjection-1.1.0.4.2.0.0.jar %projectDir%
pause
goto main