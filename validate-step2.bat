@echo off
echo === STEP 2 VALIDATION VIA GAMETEST ===
echo.
echo Este script executa validacao automatica do Step 2 sem precisar do cliente
echo Vai verificar se o sistema de eventos funciona sem erros criticos
echo.

cd /d "%~dp0"

echo Executando GameTest para validacao do Step 2...
echo.

.\gradlew.bat runGameTestServer --args="--gametest.enabled=true --gametest.classes=com.createlittlecontraptions.gametests.Step2ValidationGameTest"

echo.
echo === VALIDACAO CONCLUIDA ===
echo Verifique os logs acima para resultados do teste
pause
