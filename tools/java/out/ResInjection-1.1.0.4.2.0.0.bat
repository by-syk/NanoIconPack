@echo off
title NanoIconPackTool-ResInjection
set "projectDir=E:\Android\CoreProjects\NanoIconPack\"
:main
start http://nano.by-syk.com/page/query
java -jar ResInjection-1.1.0.4.2.0.0.jar %projectDir%
pause
goto main