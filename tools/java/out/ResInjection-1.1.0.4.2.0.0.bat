@echo off
title NanoIconPackTool-ResInjection
set "projectDir=E:\Android\CoreProjects\NanoIconPack\"
:main
java -jar ResInjection-1.1.0.4.2.0.0.jar %projectDir%
pause
goto main